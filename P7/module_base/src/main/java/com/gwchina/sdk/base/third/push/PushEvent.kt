package com.gwchina.sdk.base.third.push

import com.android.base.rx.RxBus

import io.reactivex.Flowable

data class Example(val id: Int)

/**
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-06-19 09:53
 */
object PushEvent {

    fun subscribeExampleEvent(): Flowable<Example> {
        return RxBus.getDefault().toObservable(Example::class.java)
    }

    internal fun publishMessage(message: Any) {
        RxBus.getDefault().send(message)
    }

}
