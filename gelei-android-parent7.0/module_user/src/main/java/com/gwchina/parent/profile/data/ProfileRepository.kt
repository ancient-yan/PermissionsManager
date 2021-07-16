package com.gwchina.parent.profile.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.Patriarch
import com.gwchina.sdk.base.utils.calculateAgeByBirthday
import com.gwchina.sdk.base.utils.splitBirthday
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author Wangwb
 *      Date : 2018/12/26 6:37 PM
 *      Modified by Ztiany at 2019-03-18
 */
@ActivityScope
class ProfileRepository @Inject constructor(
        private val profileApi: ProfileApi,
        private val appDataSource: AppDataSource
) {

    fun updateChildInfo(child_user_id: String, nick_name: String, sex: String, birthday: String, grade: String): Completable {
        return profileApi.updateChildInfo(child_user_id, nick_name, sex, birthday, grade)
                .resultChecker()
                .ignoreElements()
                .doOnComplete {
                    updateCache(child_user_id, birthday, nick_name, sex, grade)
                }
    }

    fun updateDeviceInfo(deviceInfo: Device) {
        appDataSource.updateDevice(deviceInfo)
    }

    private fun updateCache(child_user_id: String, birthday: String, nick_name: String, sex: String, grade: String) {
        val target = appDataSource.user().childList?.find { it.child_user_id == child_user_id }
        val selected = splitBirthday(birthday)
        if (target != null && selected.toStringList().all { it.isNotEmpty() }) {
            appDataSource.updateChild(target.copy(
                    nick_name = nick_name,
                    sex = sex.toInt(),
                    birthday = birthday,
                    grade = grade.toInt(),
                    age = calculateAgeByBirthday(selected.year, selected.monty, selected.day)
            ))
        } else {
            appDataSource.syncChildren()
        }
    }

    /**
     * [birthday]:格式YYYYMMdd
     */
    fun updatePatriarchNickName(nick_name: String, birthday: String, area_code: String): Completable {
        return profileApi.updatePatriarchNickName(nick_name, birthday, area_code)
                .resultChecker()
                .ignoreElements()
                .doOnComplete {
                    val patriarch = appDataSource.user().patriarch
                    appDataSource.updatePatriarch(patriarch.copy(nick_name = nick_name, birthday = birthday, area_code = area_code))
                }
    }

    fun getPatriarchDetail(): Observable<Optional<Patriarch>> {
        return profileApi.getPatriarchDetail().optionalExtractor()
    }

    fun getDeliveryAddress(): Observable<Optional<List<DeliveryAddress>>> {
        return profileApi.getDeliveryAddress().optionalExtractor()
    }

    fun addDeliveryAddress(receiverName: String, receiverPhone: String, provinceCode: String, cityCode: String, districtCode: String, address: String): Completable {
        return profileApi.addDeliveryAddress(receiverName, receiverPhone, provinceCode, cityCode, districtCode, address)
                .resultChecker()
                .ignoreElements()
    }

    fun updateDeliveryAddress(recordId: String, receiverName: String, receiverPhone: String, provinceCode: String, cityCode: String, districtCode: String, address: String): Completable {
        return profileApi.updateDeliveryAddress(recordId, receiverName, receiverPhone, provinceCode, cityCode, districtCode, address)
                .resultChecker()
                .ignoreElements()
    }

    fun deleteDeliveryAddress(recordId: String): Completable {
        return profileApi.deleteDeliveryAddress(recordId)
                .resultChecker()
                .ignoreElements()
    }

    fun setTempUsable(tempUsableMinutes: Int, childUserId: String, childDeviceId: String, mode: Int): Flowable<Optional<TempUsable>> {
        return profileApi.setTempUsable(childUserId, childDeviceId, mode, tempUsableMinutes)
                .optionalExtractor()
    }

    fun deleteTempUsable(tempUsableId: String, childUserId: String, childDeviceId: String): Completable {
        return profileApi.deleteTempUsable(childUserId, childDeviceId, tempUsableId)
                .resultChecker()
                .ignoreElements()
    }

    fun getChildDeviceTimeRule(childUserId: String): Flowable<Optional<List<TimeGuardPlan>>> {
        return profileApi.getChildDeviceTimeRule(childUserId)
                .optionalExtractor()
    }
}