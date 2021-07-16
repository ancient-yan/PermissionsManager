@file:JvmName("AutoDisposeUtils")

package com.android.base.rx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import com.uber.autodispose.*
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.*
import io.reactivex.disposables.Disposable
import timber.log.Timber


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-10 14:41
 */
interface AutoDisposeLifecycleScopeProviderEx<T> : LifecycleScopeProvider<T> {

    fun <T> Flowable<T>.autoDispose(): FlowableSubscribeProxy<T> {
        return this.`as`(autoDisposable(this@AutoDisposeLifecycleScopeProviderEx))
    }

    fun <T> Observable<T>.autoDispose(): ObservableSubscribeProxy<T> {
        return this.`as`(autoDisposable(this@AutoDisposeLifecycleScopeProviderEx))
    }

    fun Completable.autoDispose(): CompletableSubscribeProxy {
        return this.`as`(autoDisposable<Any>(this@AutoDisposeLifecycleScopeProviderEx))
    }

    fun <T> Maybe<T>.autoDispose(): MaybeSubscribeProxy<T> {
        return this.`as`(autoDisposable(this@AutoDisposeLifecycleScopeProviderEx))
    }

    fun <T> Single<T>.autoDispose(): SingleSubscribeProxy<T> {
        return this.`as`(autoDisposable(this@AutoDisposeLifecycleScopeProviderEx))
    }

}

interface AutoDisposeLifecycleOwnerEx : LifecycleOwner {

    fun <T> Flowable<T>.autoDispose(): FlowableSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx)
    }

    fun <T> Observable<T>.autoDispose(): ObservableSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx)
    }

    fun Completable.autoDispose(): CompletableSubscribeProxy {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx)
    }

    fun <T> Maybe<T>.autoDispose(): MaybeSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx)
    }

    fun <T> Single<T>.autoDispose(): SingleSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx)
    }

    fun <T> Flowable<T>.autoDispose(event: Lifecycle.Event): FlowableSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx, event)
    }

    fun <T> Observable<T>.autoDispose(event: Lifecycle.Event): ObservableSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx, event)
    }

    fun Completable.autoDispose(event: Lifecycle.Event): CompletableSubscribeProxy {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx, event)
    }

    fun <T> Maybe<T>.autoDispose(event: Lifecycle.Event): MaybeSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx, event)
    }

    fun <T> Single<T>.autoDispose(event: Lifecycle.Event): SingleSubscribeProxy<T> {
        return this.bindLifecycle(this@AutoDisposeLifecycleOwnerEx, event)
    }

}

fun <T> Flowable<T>.bindLifecycle(lifecycleOwner: LifecycleOwner): FlowableSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
}

fun <T> Flowable<T>.bindLifecycle(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event): FlowableSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, event)))
}

fun <T> Observable<T>.bindLifecycle(lifecycleOwner: LifecycleOwner): ObservableSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
}

fun <T> Observable<T>.bindLifecycle(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event): ObservableSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, event)))
}

fun Completable.bindLifecycle(lifecycleOwner: LifecycleOwner): CompletableSubscribeProxy {
    return this.`as`(autoDisposable<Any>(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
}

fun Completable.bindLifecycle(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event): CompletableSubscribeProxy {
    return this.`as`(autoDisposable<Any>(AndroidLifecycleScopeProvider.from(lifecycleOwner, event)))
}

fun <T> Maybe<T>.bindLifecycle(lifecycleOwner: LifecycleOwner): MaybeSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
}

fun <T> Maybe<T>.bindLifecycle(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event): MaybeSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, event)))
}

fun <T> Single<T>.bindLifecycle(lifecycleOwner: LifecycleOwner): SingleSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
}

fun <T> Single<T>.bindLifecycle(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event): SingleSubscribeProxy<T> {
    return this.`as`(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, event)))
}

fun <T> ObservableSubscribeProxy<T>.subscribed(): Disposable = this.subscribe(RxKit.logResultHandler(), RxKit.logErrorHandler())
fun <T> FlowableSubscribeProxy<T>.subscribed(): Disposable = this.subscribe(RxKit.logResultHandler(), RxKit.logErrorHandler())
fun <T> SingleSubscribeProxy<T>.subscribed(): Disposable = this.subscribe(RxKit.logResultHandler(), RxKit.logErrorHandler())
fun <T> MaybeSubscribeProxy<T>.subscribed(): Disposable = this.subscribe(RxKit.logResultHandler(), RxKit.logErrorHandler())
fun CompletableSubscribeProxy.subscribed(): Disposable = this.subscribe(RxKit.logCompletedHandler(), RxKit.logErrorHandler())

fun <T> ObservableSubscribeProxy<T>.subscribeIgnoreError(action: (T) -> Unit): Disposable = this.subscribe(
        {
            action(it)
        },
        {
            Timber.e(it, "Kotlin Extends ignoreError: ")
        }
)

fun <T> FlowableSubscribeProxy<T>.subscribeIgnoreError(action: (T) -> Unit): Disposable = this.subscribe(
        {
            action(it)
        },
        {
            Timber.e(it, "Kotlin Extends ignoreError: ")
        }
)

fun <T> SingleSubscribeProxy<T>.subscribeIgnoreError(action: (T) -> Unit): Disposable = this.subscribe(
        {
            action(it)
        },
        {
            Timber.e(it, "Kotlin Extends ignoreError: ")
        }
)

fun <T> MaybeSubscribeProxy<T>.subscribeIgnoreError(action: (T) -> Unit): Disposable = this.subscribe(
        {
            action(it)
        },
        {
            Timber.e(it, "Kotlin Extends ignoreError: ")
        }
)

fun CompletableSubscribeProxy.subscribeIgnoreError(action: () -> Unit): Disposable = this.subscribe(
        {
            action()
        },
        {
            Timber.e(it, "Kotlin Extends ignoreError: ")
        }
)