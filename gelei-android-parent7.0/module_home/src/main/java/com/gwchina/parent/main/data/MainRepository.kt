package com.gwchina.parent.main.data

import com.android.base.app.dagger.ActivityScope
import com.android.base.rx.SchedulerProvider
import com.android.sdk.cache.flowableOptional
import com.android.sdk.cache.getEntity
import com.android.sdk.net.kit.*
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_DISABLE
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE
import com.gwchina.sdk.base.data.api.UNBIND_CHILD_DEVICE
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.models.AllInstructionState
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.PrivilegeData
import com.gwchina.sdk.base.data.services.ServiceManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 11:51
 */

@ActivityScope
class MainRepository @Inject constructor(
        private val mainApi: MainApi,
        private val storageManager: StorageManager,
        private val schedulerProvider: SchedulerProvider,
        private val appDataSource: AppDataSource,
        internal val serviceManager: ServiceManager
) {

    companion object {
        //home
        private const val HOME_DATA_CACHE_KEY = "home_data_cache_key_v2"
        //mine
        private const val MINE_PAGE_CACHE = "mine_page_cache"
        //location
        private const val NEWEST_LOCATION_KEY = "newest_location_key"
    }

    fun loadHomeData(): Flowable<CombinedResult<HomeResponse>> {

        val remote = mainApi.loadHomePageData("", "")
                .optionalExtractor()

        val local = storageManager.currentDeviceStorage()
                .flowableOptional<HomeResponse>(HOME_DATA_CACHE_KEY)
                .subscribeOn(schedulerProvider.io())

        return combineMultiSource(remote, local) {
            storageManager.currentDeviceStorage().putEntity(HOME_DATA_CACHE_KEY, it)
            it?.member_info?.let(appDataSource::updateMember)
        }

    }

    fun switchChildAndDevice(childUserId: String, childDeviceId: String): Flowable<Optional<HomeResponse>> {
        return mainApi.loadHomePageData(childUserId, childDeviceId)
                .optionalExtractor()
                .doOnNext {
                    it.ifPresent { data ->
                        appDataSource.updateDefaultChild(data.top_child_info?.child_user_id
                                ?: "", data.device_guard_item?.device_id ?: "")
                        data?.member_info?.let(appDataSource::updateMember)
                        val deviceStorage = storageManager.deviceStorage(childDeviceId)
                        deviceStorage.putEntity(HOME_DATA_CACHE_KEY, data)
                    }
                }
    }

    fun instructionsStatus(childUserId: String, childDeviceId: String): Flowable<AllInstructionState> {
        return serviceManager.instructionSyncService.allInstructionSyncState(childUserId, childDeviceId)
    }

    fun sendSyncInstructions(instructionType: String, childUserId: String, childDeviceId: String): Completable {
        return serviceManager.instructionSyncService.sendSyncInstruction(instructionType, childUserId, childDeviceId)
    }

    fun prohibitNewApp(ruleId: String, childUserId: String, childDeviceId: String): Completable {
        return mainApi.forbidApp(childUserId, childDeviceId, ruleId, APP_RULE_TYPE_DISABLE.toString(), FLAG_POSITIVE.toString(), "")
                .resultChecker()
                .ignoreElements()
    }

    fun setTempUsable(tempUsableMinutes: Int, childUserId: String, childDeviceId: String): Flowable<Optional<TempUsable>> {
        return mainApi.setTempUsable(childUserId, childDeviceId, tempUsableMinutes)
                .optionalExtractor()
    }

    fun deleteTempUsable(tempUsableId: String, childUserId: String, childDeviceId: String): Completable {
        return mainApi.deleteTempUsable(childUserId, childDeviceId, tempUsableId)
                .resultChecker()
                .ignoreElements()
    }

    fun installAppForChild(childUserId: String, childDeviceId: String, softName: String, bundleId: String): Completable {
        return mainApi.installAppForChild(
                childUserId,
                childDeviceId,
                softName,
                bundleId,
                "1"/*推荐来源界面 rec_source ，1 表示应用推荐*/
        ).resultChecker().ignoreElements()
    }

    fun updateDeviceInfo(newDeviceInfo: Device?) {
        var entity = storageManager.currentDeviceStorage().getEntity<HomeResponse>(HOME_DATA_CACHE_KEY)
                ?: return
        entity = entity.copy(device_guard_item = newDeviceInfo)
        storageManager.currentDeviceStorage().putEntity(HOME_DATA_CACHE_KEY, entity)
    }

    fun newestChildLocation(childUserId: String, childDeviceId: String): Flowable<Optional<ChildLocation>> {
        return mainApi.getChildLocation(childUserId, childDeviceId)
                .optionalExtractor()
    }

    fun sendSyncLocationInstruction(childUserId: String, childDeviceId: String): Completable {
        return mainApi.sendSyncChildLocationInstruction(childUserId, childDeviceId)
                .resultChecker()
                .ignoreElements()
    }

    fun saveNewestLocation(deviceId: String, location: ChildLocation) {
        storageManager.deviceStorage(deviceId).putEntity(NEWEST_LOCATION_KEY, location)
    }

    fun lastLocation(deviceId: String): ChildLocation? {
        return storageManager.deviceStorage(deviceId).getEntity(NEWEST_LOCATION_KEY)
    }

    fun allPendingApprovalAppList(): Flowable<Optional<SoftApproval>> {
        return mainApi.loadAllPendingApprovalAppList().optionalExtractor()
    }

    fun allPendingApprovalPhoneList(): Flowable<Optional<List<PhoneApprovalInfo>>> {
        return mainApi.loadAllPendingApprovalPhoneList().optionalExtractor()
    }

    fun sendUnbindSmsCode(phoneNumber: String): Completable {
        return appDataSource.sendSmsCode(UNBIND_CHILD_DEVICE, phoneNumber)
    }

    fun unbindDevice(childUserId: String, childDeviceId: String, smsCode: String): Completable {
        return mainApi.unBindChild(childUserId, childDeviceId, smsCode)
                .resultChecker()
                .flatMapCompletable {
                    storageManager.deviceStorage(childDeviceId).clearAll()
                    appDataSource.syncChildren()
                }
    }

    fun minePageInfo(): Flowable<Optional<MineResponse>> {

        val remote = mainApi.loadMinePageInfo().optionalExtractor()

        val local = storageManager.userStorage()
                .flowableOptional<MineResponse>(MINE_PAGE_CACHE)

        return concatMultiSource(remote, local) {
            storageManager.userStorage().putEntity(MINE_PAGE_CACHE, it)
        }
    }

    fun approvalPhone(recordId: String, allow: Boolean, childId: String): Completable {
        return mainApi.approvalPhone(recordId, if (allow) "1" else "2", childId)
                .resultChecker()
                .ignoreElements()
    }

    /**
     * 加载周报列表数据
     */
    fun loadWeeklyListData(): Observable<Optional<List<WeeklyInfo>>> {
        return mainApi.loadWeeklyListData()
                .optionalExtractor()
    }

    fun upHomeTopImg(childUserId: String, imgPath: String): Observable<Optional<String>> {
        return mainApi.upHomeTopImg(childUserId, imgPath).optionalExtractor()
    }

    /**
     * 手机使用记录
     */
    fun homeUsingRecordData(): Observable<Optional<UsingRecord>> {
        return mainApi.homePageUsingRecord(appDataSource.user().currentChild?.child_user_id
                ?: "", appDataSource.user().currentDevice?.device_id ?: "").optionalExtractor()
    }

    /***
     * 孩子设备权限详情
     */
    fun childDevicePermissionChecker(childUserId:String,childDeviceId:String): Flowable<Optional<PrivilegeData>> {
        return mainApi.childDevicePermissionChecker(childUserId, childDeviceId).optionalExtractor()
    }
}