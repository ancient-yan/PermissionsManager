package com.gwchina.parent.binding.data

import com.android.base.app.dagger.ActivityScope
import com.android.base.rx.retryWhen
import com.android.sdk.net.kit.resultExtractor
import com.gwchina.parent.binding.presentation.ChildInfo
import com.gwchina.parent.binding.presentation.DeviceInfo
import com.gwchina.sdk.base.data.app.AppDataSource
import io.reactivex.Flowable
import javax.inject.Inject

@ActivityScope
class BindingRepository @Inject constructor(
        private val bindingApi: BindingApi,
        private val appDataSource: AppDataSource
) {

    companion object {
        private const val MAX_RETRY = 10
        private const val RETRY_DELAY = 2000L
    }

    /*后台返回了详细的设备信息，暂时使用 token 接口对用户数据进行刷新*/
    fun addDeviceForChild(child_user_id: String, childDeviceInfo: DeviceInfo): Flowable<BindingResponse> {
        return bindingApi.bindDeviceForChild(child_user_id, childDeviceInfo.deviceSn, childDeviceInfo.manufacture, childDeviceInfo.osVersion, childDeviceInfo.romVersion)
                .resultExtractor()
                .flatMap {
                    appDataSource.syncChildren().retryWhen(MAX_RETRY, RETRY_DELAY).andThen(Flowable.just(it))
                }
    }

    /*后台返回了详细的设备信息，暂时使用 token 接口对用户数据进行刷新*/
    fun bindNewChild(childInfo: ChildInfo, childDeviceInfo: DeviceInfo): Flowable<BindingResponse> {
        return bindingApi.bindChildDevice(childInfo.name, childInfo.sex, childInfo.birthday, childInfo.grade, childInfo.relationship, childDeviceInfo.deviceSn, childDeviceInfo.manufacture, childDeviceInfo.osVersion, childDeviceInfo.romVersion)
                .resultExtractor()
                .flatMap {
                    appDataSource.syncChildren().retryWhen(MAX_RETRY, RETRY_DELAY).andThen(Flowable.just(BindingResponse(it.child_user_id, it.device.first_bind_device, it.device.custom_device_flag)))
                }
    }

}