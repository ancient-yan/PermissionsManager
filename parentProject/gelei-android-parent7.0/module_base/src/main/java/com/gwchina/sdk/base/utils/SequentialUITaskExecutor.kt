package com.gwchina.sdk.base.utils

import android.os.Looper
import android.support.annotation.UiThread
import com.android.base.app.BaseKit
import com.android.base.rx.observeOnUI
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.sdk.base.AppContext
import io.reactivex.Flowable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 串行 UI 任务执行器
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-26 15:16
 */
object SequentialUITaskExecutor {

    /**调度策略：只执行一次，不管成功还是失败*/
    const val SCHEDULE_POLICY_ONCE = 1

    /**调度策略：反复执行直到成功，失败后添加到队尾*/
    const val SCHEDULE_POLICY_TILL_SUCCESS = 2

    /**调度策略：反复执行，成功后添加到队尾后续继续执行*/
    const val SCHEDULE_POLICY_REPEATED = 3

    private var runningAction: UITask? = null
    private val readyActions = ArrayDeque<UITask>()

    init {
        Flowable.interval(0, 15, TimeUnit.SECONDS)
                .observeOnUI()
                .subscribeIgnoreError {
                    if (BaseKit.get().isForeground) {
                        promoteAndExecute()
                    }
                }
    }

    fun schedulePolicyName(policy: Int): String {
        return when (policy) {
            SCHEDULE_POLICY_ONCE -> "schedule_policy_once"
            SCHEDULE_POLICY_TILL_SUCCESS -> "schedule_policy_till_success"
            SCHEDULE_POLICY_REPEATED -> "schedule_policy_repeated"
            else -> ""
        }
    }

    /**执行一个任务*/
    fun execute(action: UITask) {
        Timber.d("执行任务：$action")
        if (isRunningInMainThread()) {
            executeInMainThread(action)
        } else {
            runOnUI(Runnable {
                executeInMainThread(action)
            })
        }
    }

    private fun runOnUI(runnable: Runnable) {
        AppContext.schedulerProvider().ui().scheduleDirect(runnable)
    }

    private fun isRunningInMainThread() = Looper.myLooper() == Looper.getMainLooper()

    private fun executeInMainThread(action: UITask, addToFirst: Boolean = false) {
        val dialogAction = runningAction
        if ((dialogAction != null && dialogAction.id == action.id) || readyActions.find { it.id == action.id } != null) {
            return
        }
        addTask(action, addToFirst)
        promoteAndExecute()
    }

    private fun addTask(action: UITask, addToFirst: Boolean = false) {
        if (addToFirst) {
            readyActions.offerFirst(action)
        } else {
            readyActions.offerLast(action)
        }
    }

    internal fun finished(actionId: String, success: Boolean) {
        if (isRunningInMainThread()) {
            finishAtMainThread(actionId, success)
        } else {
            runOnUI(Runnable {
                finishAtMainThread(actionId, success)
            })
        }
    }

    internal fun finished(action: UITask, success: Boolean) {
        Timber.d("完成任务：$action, success = $success")
        if (isRunningInMainThread()) {
            finishInMainThread(action, success)
        } else {
            runOnUI(Runnable {
                finishInMainThread(action, success)
            })
        }
    }

    private fun finishAtMainThread(actionId: String, success: Boolean) {
        val action = runningAction
        if (action != null && actionId == action.id) {
            finishInMainThread(action, success)
        }
    }

    private fun finishInMainThread(action: UITask, success: Boolean) {
        if (action == runningAction) {
            runningAction = null

            if (shouldReAddToQueue(action, success)) {
                addTask(action, false)
                Timber.d("finishInMainThread, re add task：$action")
            }

        } else {
            if (action.isCanceled()) {
                val each = readyActions.iterator()
                while (each.hasNext()) {
                    if (action.id == each.next().id) {
                        each.remove()
                    }
                }
            }
        }
    }

    private fun shouldReAddToQueue(action: UITask, success: Boolean) =
            !action.isCanceled() && (SCHEDULE_POLICY_REPEATED == action.schedulePolicy || (!success && action.schedulePolicy == SCHEDULE_POLICY_TILL_SUCCESS))

    private fun promoteAndExecute() {
        if (runningAction != null) {
            return
        }

        if (readyActions.isEmpty()) {
            return
        }

        var task: UITask

        while (!readyActions.isEmpty()) {
            task = readyActions.removeFirst()
            if (!task.isCanceled()) {
                runningAction = task
                break
            }
        }

        try {
            runningAction?.run()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}

/**一个任务抽象，在任务执行完毕后，必须调用 [finished] 方法，不同任务的 [id] 必须保证唯一。*/
abstract class UITask(val id: String, val schedulePolicy: Int) {

    private var cancel = false

    fun finished(success: Boolean = true) {
        SequentialUITaskExecutor.finished(this, success)
    }

    @UiThread
    abstract fun run()

    @UiThread
    fun cancel() {
        cancel = true
        finished()
    }

    fun isCanceled() = cancel

    override fun toString(): String {
        return "SequentialAction(id='$id', schedulePolicy=${SequentialUITaskExecutor.schedulePolicyName(schedulePolicy)}, isCanceled=${cancel})"
    }

}