package com.gwchina.sdk.base.widget.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.base.kotlin.*
import com.android.base.rx.*
import com.app.base.R
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.sync.State
import com.gwchina.sdk.base.sync.SyncState
import com.gwchina.sdk.base.sync.SyncStateManager
import com.gwchina.sdk.base.widget.dialog.TipsManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.widget_instruction_state.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-23 14:12
 */
open class InstructionStateView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onRetryClickListener: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        setPadding(dip(10), dip(9), dip(10), dip(9))
        gravity = Gravity.CENTER_VERTICAL

        View.inflate(context, R.layout.widget_instruction_state, this)

        btnWidgetInstructionState.setOnClickListener {
            onRetryClickListener?.invoke()
        }

    }

    fun setStateDesc(desc: String, isSyncing: Boolean = false) {
        tvWidgetInstructionState.text = desc
        if (isSyncing) {
            btnWidgetInstructionState.gone()
            tvWidgetInstructionState.setTextColor(colorFromId(R.color.gray_level2))
        } else {
            btnWidgetInstructionState.visible()
            tvWidgetInstructionState.setTextColor(colorFromId(R.color.red_level1))
        }
    }

    //初始同步
     fun showSyncFailedState(instructionName:String, deviceName:String) {
        visible()
        setStateDesc(context.getString(R.string.instruction_sync_failed_tips_mask, instructionName, deviceName), false)
    }


     fun showInitFailedState(instructionName:String) {
        visible()
        setStateDesc(context.getString(R.string.child_offline_instruction_sync_failed_tips_mask, instructionName))
    }

     fun showSyncingState(instructionName:String) {
        visible()
        setStateDesc(context.getString(R.string.instruction_syncing_tips_mask, instructionName), true)
    }


}

class AutoInstructionStateView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : InstructionStateView(context, attrs, defStyleAttr) {

    private lateinit var instructionName: String
    private lateinit var instructionFlag: String
    private lateinit var childUserId: String
    private lateinit var childDeviceId: String

    private val deviceName = AppContext.appDataSource().user().currentDevice?.device_name

    private var compositeDisposable: CompositeDisposable? = null

    private val instructionSyncService = AppContext.serviceManager().instructionSyncService
    private var isInstructionSynced: Boolean = false

    private var mState: State? = null

    init {
        visibility = View.GONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Timber.e("AAAAAAAAA==" + "onAttachedToWindow")
        onRetryClickListener = {
            sendAppRulesInstruction()
            setStateDesc(context.getString(R.string.instruction_syncing_tips_mask, instructionName), true)
        }
    }

    private var mIsHomePage: Boolean = false

    fun init(instructionName: String, instructionFlag: String, childUserId: String, childDeviceId: String, state: State? = null, isHomePage: Boolean = false) {

        mIsHomePage = isHomePage
        this.instructionName = instructionName
        this.instructionFlag = instructionFlag
        this.childUserId = childUserId
        this.childDeviceId = childDeviceId

        if (state == null) {
            Timber.e("state========null")
            loadAppRulesSyncStatus()
        } else {
            mState = state
            when {
                state.syncState == SyncState.SYNC_FAILED -> showSyncFailedLayout()
                state.syncState == SyncState.SYNC_SUCCESS -> {
                    showSyncSuccessLayout()
                    loadAppRulesSyncStatus()
                }
                else -> { //同步中的状态
                    showSyncingLayout()
                    val lastTime = state.lastTime
                    Timber.e("time2=====" + lastTime)
                    if (lastTime > 0) {
                        startInterval(lastTime)
                    }
                }
            }
            Timber.e("init==============1" + instructionFlag + "======" + state.syncState)
        }
    }

    private fun showSyncSuccessLayout() {
        gone()
    }

    private fun showInitLayout() {
        visible()
        setStateDesc(context.getString(R.string.instruction_sync_failed_tips_mask, instructionName, deviceName), false)
    }

    private fun showSyncFailedLayout() {
        visible()
        setStateDesc(context.getString(R.string.child_offline_instruction_sync_failed_tips_mask, instructionName))
    }

    private fun showSyncingLayout() {
        visible()
        setStateDesc(context.getString(R.string.instruction_syncing_tips_mask, instructionName), true)
    }

    private fun createCompositeDisposable(): CompositeDisposable {
        return newCompositeIfDisposed(compositeDisposable).also {
            compositeDisposable = it
        }
    }

    private var intervalDisposable: Disposable? = null

    private var mLastTime = 0L

    private fun startInterval(lastTime: Long) {
        intervalDisposable = Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .observeOnUI()
                .take(lastTime).map {
                    lastTime - it
                }
                .subscribe {
                    mLastTime = it
                    if (it > 1) {
                        mState?.lastTime = it
                        mState?.syncState = SyncState.SYNCING
                        //每隔5s请求一次
                        if (it.rem(5) == 0L) {
                            loadAppRulesSyncStatusByInterval()
                        }
                    } else {
                        mState?.lastTime = 0
                        mState?.syncState = SyncState.SYNC_FAILED
                        showInitLayout()
                        ToastUtils.showShort(R.string.sync_failed)
                    }
                    SyncStateManager.syncMap[instructionFlag] = mState
                    Timber.e("剩余时间：" + instructionFlag + "=====" + SyncStateManager.syncMap[instructionFlag]?.lastTime)
                    if (mIsHomePage) {
                        SyncStateManager.homePageSyncingMap()[instructionFlag] = it > 1
                    }
                }
    }

    /**
     * 重复查询同步状态
     */
    private fun loadAppRulesSyncStatusByInterval() {
        instructionSyncService.instructionSyncState(instructionFlag, childUserId, childDeviceId)
                .observeOnUI()
                .subscribeIgnoreError {
                    val state = SyncStateManager.syncMap[instructionFlag]
                    if (!it.isSynced()) {
                        showSyncingLayout()
                    } else {
                        intervalDisposable?.dispose()
                        state?.syncState = SyncState.SYNC_SUCCESS
                        isInstructionSynced = true
                        TipsManager.showMessage(context.getString(R.string.sync_successfully))
                        gone()
                        hideHomePageLayout()
                        if (mIsHomePage) {
                            SyncStateManager.homePageSyncingMap()[instructionFlag] = false
                        }
                    }
                    state?.let {
                        SyncStateManager.syncMap[instructionFlag] = it
                    }
                }
                .addTo(createCompositeDisposable())
    }

    private fun hideHomePageLayout() {
        if (parent.javaClassName() == "com.gwchina.parent.main.presentation.home.card.InstructionStateCard") {
            var isAllChildGone = true
            val viewGroup = parent as ViewGroup
            val childCount = viewGroup.childCount
            for (i in 0 until childCount) {
                if (viewGroup.getChildAt(i).isVisible()) {
                    isAllChildGone = false
                }
            }
            if (isAllChildGone) {
                viewGroup.gone()
            }
        }
    }


    private fun loadAppRulesSyncStatus(delay: Boolean = false, showTips: Boolean = false) {
        Timber.e("AAAAAA=" + instructionFlag)
        instructionSyncService.instructionSyncState(instructionFlag, childUserId, childDeviceId)
                .run {
                    if (delay) {
                        this.delaySubscription(2, TimeUnit.SECONDS)
                    } else {
                        this
                    }
                }
                .observeOnUI()
                .retryWhen(Int.MAX_VALUE, 5000)
                .subscribeIgnoreError {
                    val state = State(SyncState.SYNC_SUCCESS)
                    if (!it.isSynced()) {
                        state.syncState = SyncState.SYNC_FAILED
                        setStateDesc(context.getString(R.string.child_offline_instruction_sync_failed_tips_mask, instructionName))
                        visible()
                        Timber.e("AAAAAA=" + isVisible())
                    } else {
                        isInstructionSynced = true
                        if (showTips) {
                            TipsManager.showMessage(context.getString(R.string.sync_successfully))
                        }
                        gone()
                    }
                    Timber.e("AAAAAA=put===" + instructionFlag)
                    SyncStateManager.syncMap[instructionFlag] = state
                }
                .addTo(createCompositeDisposable())
    }

    private fun sendAppRulesInstruction() {
        instructionSyncService.sendSyncInstruction(instructionFlag, childUserId, childDeviceId)
                .observeOnUI()
                .subscribe(
                        {
                            //loadAppRulesSyncStatus(true, true)
                            mState = State(SyncState.SYNCING, 60)
                            startInterval(60)
                        },
                        {
                            AppContext.errorHandler().handleError(it)
                            setStateDesc(context.getString(R.string.child_offline_instruction_sync_failed_tips_mask, instructionName))
                        }
                )
                .addTo(createCompositeDisposable())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.disposeChecked()
        intervalDisposable?.disposeChecked()
        if (mIsHomePage) {
            SyncStateManager.syncMap.clear()
        } else {
            if (mState?.syncState != SyncState.SYNCING) return
            SyncStateManager.syncMap[instructionFlag] = State(SyncState.SYNCING, mLastTime)
            val intent = Intent()
            intent.putExtra("instructionFlag", instructionFlag)
            intent.action = SyncStateManager.LOCAL_ACTION
            SyncStateManager.getLocalBroadcastManager(context).sendBroadcast(intent)
        }
        Timber.e("AAAAAAAAA=" + "onDetachedFromWindow")
    }

    fun resetState(){
        compositeDisposable.disposeChecked()
        intervalDisposable?.disposeChecked()
        SyncStateManager.reset()
    }
}