package com.gwchina.parent.profile.presentation.patriarch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.profile.data.DeliveryAddress
import com.gwchina.parent.profile.data.PatriarchData
import com.gwchina.parent.profile.data.ProfileRepository
import com.gwchina.parent.profile.data.RefreshPatriarchInfoEventCenter
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Patriarch
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 *@author Wangwb
 *        Date : 2018/12/24 3:47 PM
 */
class PatriarchInfoViewModel @Inject constructor(
        private var profileRepository: ProfileRepository,
        internal val appDataSource: AppDataSource,
        internal val refreshPatriarchInfoEventCenter: RefreshPatriarchInfoEventCenter,
        val errorHandler: ErrorHandler
) : ArchViewModel() {

    private val _patriarchDetail = MutableLiveData<Resource<PatriarchData>>()

    internal val patriarchDetail: LiveData<Resource<PatriarchData>>
        get() = _patriarchDetail

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    fun doLogout() {
        appDataSource.logout()
    }

    fun updatePatriarch(nick_name: String, birthday: String, area_code: String): LiveData<Resource<Any>> {
        return profileRepository.updatePatriarchNickName(nick_name, birthday, area_code).toResourceLiveData()
    }


    fun getPatriarchData() {
        _patriarchDetail.value = Resource.loading()
        Observable.zip(profileRepository.getPatriarchDetail(), profileRepository.getDeliveryAddress(),
                BiFunction<Optional<Patriarch>, Optional<List<DeliveryAddress>>, PatriarchData> { t1, t2 -> PatriarchData(t1, t2) }
        ).autoDispose()
                .subscribe(
                        {
                            _patriarchDetail.postValue(Resource.success(it))
                            //更新家长信息
                            it.patriarchDetail?.get()?.let { it1 -> appDataSource.updatePatriarch(it1) }
                        }, {
                    _patriarchDetail.postValue(Resource.error(it))
                }
                )
    }

    fun addAddress(receiverName: String, receiverPhone: String, provinceCode: String, cityCode: String, districtCode: String, address: String): LiveData<Resource<Any>> {

        return profileRepository.addDeliveryAddress(receiverName, receiverPhone, provinceCode, cityCode, districtCode, address).toResourceLiveData()
    }

    fun updateAddress(recordId: String, receiverName: String, receiverPhone: String, provinceCode: String, cityCode: String, districtCode: String, address: String): LiveData<Resource<Any>> {

        return profileRepository.updateDeliveryAddress(recordId, receiverName, receiverPhone, provinceCode, cityCode, districtCode, address).toResourceLiveData()
    }

    fun deleteAddress(recordId: String): LiveData<Resource<Any>> {

        return profileRepository.deleteDeliveryAddress(recordId).toResourceLiveData()
    }
}

