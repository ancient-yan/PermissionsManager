package com.gwchina.sdk.base.data.app

import android.content.Context
import com.android.base.app.dagger.ContextType
import com.android.base.rx.SchedulerProvider
import com.android.sdk.cache.flowableOptional
import com.android.sdk.net.kit.concatMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.android.sdk.net.service.ServiceFactory
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.debug.ifOpenDebug
import com.gwchina.sdk.base.utils.GwDevices
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局数仓库类实现
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-14 16:14
 */
@Singleton
internal class AppRepository @Inject constructor(
        @ContextType private val context: Context,
        serviceFactory: ServiceFactory,
        private val schedulerProvider: SchedulerProvider
) : AppDataSource {

    companion object {
        //constants
        private const val PUSH_PLATFORM = "1"
        private const val PUSH_TYPE = "001"
        // forever
        private const val DEVICE_ID_KEY = "parent_device_id"
        private const val AD_LIST_KEY = "ad_list_key"
        //cache key,  user associated
        private const val APP_TOKEN_KEY = "parent_app_token"
        private const val APP_USER_KEY = "parent_user"
    }

    internal val storageManager = StorageManager(context, this)
    private val observableUser = BehaviorProcessor.create<User>()
    private val observableDeviceId = BehaviorProcessor.create<String>()
    private val appApi = serviceFactory.create(AppApi::class.java)
    private val appStorage = storageManager.userStorage()
    private val stableStorage = storageManager.stableStorage()

    private var currentUser = User.NOT_LOGIN

    init {
        //init notify user
        currentUser = appStorage.getEntity(APP_USER_KEY, User::class.java) ?: User.NOT_LOGIN
        ifOpenDebug {
            Timber.d("init user start------------------------------------------------->")
            Timber.d(JsonUtils.toJson(currentUser))
            Timber.d("init user end<-------------------------------------------------")
        }
        //init notify device id
        observableUser.onNext(currentUser)
        deviceId().let {
            if (it.isNotEmpty()) {
                observableDeviceId.onNext(it)
            }
        }
    }

    override fun syncUser(): Completable {
        return appApi.updateToken(appToken())
                .optionalExtractor()
                .doOnNext { optional ->
                    optional.ifPresent {
                        saveUser(it.login_info, it.app_token, it.expire_time)
                    }
                }
                .ignoreElements()
    }

    override fun uploadDevice(deviceSn: String, deviceName: String, appVersion: String): Completable {
        return appApi.uploadDevice(
                GwDevices.DEVICE_TYPE,
                GwDevices.APP_PLATFORM,
                deviceSn, deviceName, appVersion)
                .optionalExtractor()
                .doOnNext {
                    if (it.isPresent) {
                        val deviceId = it.get().device_id
                        saveDeviceId(deviceId)
                    }
                }.ignoreElements()
    }

    private fun saveDeviceId(deviceId: String) {
        stableStorage.putString(DEVICE_ID_KEY, deviceId)
        observableDeviceId.onNext(deviceId)
    }

    override fun uploadPushId(deviceId: String, registerId: String): Completable {
        return appApi.uploadPushId(deviceId, registerId, PUSH_PLATFORM, PUSH_TYPE)
                .resultChecker()
                .ignoreElements()
    }

    override fun deviceId(): String = stableStorage.getString(DEVICE_ID_KEY, "")

    override fun observableDeviceId() = observableDeviceId

    override fun appToken(): String = appStorage.getString(APP_TOKEN_KEY, "")

    override fun userLogined() = user().logined()

    @Synchronized
    override fun user() = currentUser

    override fun observableUser(): Flowable<User> {
        return observableUser
    }

    override fun syncChildren(): Completable {
        return appApi.updateChildList()
                .optionalExtractor()
                .doOnNext { optional ->
                    optional.ifPresent(::updateChildren)
                }
                .ignoreElements()
    }

    override fun updatePatriarch(patriarch: Patriarch) {
        val user = user()
        if (user != User.NOT_LOGIN && user.patriarch != patriarch) {
            val newUser = user.copy(patriarch = patriarch)
            saveUserSynchronized(newUser)
        }
    }

    override fun updateDevice(newDevice: Device) {
        val user = user()
        val childList = user.childList
        if (user != User.NOT_LOGIN && !childList.isNullOrEmpty()) {
            var targetChild: Child? = null
            var targetDevice: Device? = null
            var targetIndex: Int = -1
            childList.forEach {
                val deviceList = it.device_list
                if (!deviceList.isNullOrEmpty()) {
                    deviceList.forEachIndexed { index, tempDevice ->
                        if (newDevice.device_id == tempDevice.device_id) {
                            targetDevice = tempDevice
                            targetChild = it
                            targetIndex = index
                            return@forEach
                        }
                    }
                }
            }
            val child = targetChild
            if (child != null && targetDevice != null) {
                val newDeviceList = mutableListOf<Device>()
                newDeviceList.addAll(child.device_list ?: emptyList())
                newDeviceList[targetIndex] = newDevice
                updateChild(child.copy(device_list = newDeviceList))
            }
        }
    }

    override fun updateChild(newChild: Child) {
        val user = user()
        val childList = user.childList
        if (user != User.NOT_LOGIN && !childList.isNullOrEmpty()) {
            val target = childList.asSequence().indexOfFirst { item -> item.child_user_id == newChild.child_user_id }
            if (target != -1 && childList[target] != newChild) {
                val newList = mutableListOf<Child>()
                newList.addAll(childList)
                newList[target] = newChild
                val newUser = if (user.currentChild?.child_user_id == newChild.child_user_id) {
                    user.copy(childList = newList, currentChild = newChild)
                } else {
                    user.copy(childList = newList)
                }
                saveUserSynchronized(newUser)
            }
        }
    }

    override fun updateMember(member: Member) {
        val user = user()
        if (user.logined()) {
            val newUser = user.copy(member_info = member)
            saveUserSynchronized(newUser)
        }
    }

    private fun updateChildren(childResponse: SyncChildrenResponse) {
        val user = user()
        ///check if not changed
        val sameDefaultIds = user.currentChildId == childResponse.default_child_user_id && user.currentChildDeviceId == childResponse.default_child_device_id

        if (sameDefaultIds && user.childList == childResponse.child_device_list) {
            return
        }
        //new data
        val newDefaultChild = childResponse.child_device_list.asSequence().find { item -> item.child_user_id == childResponse.default_child_user_id }
        val newDefaultDevice = newDefaultChild?.device_list?.asSequence()?.find { item -> item.device_id == childResponse.default_child_device_id }
        //copy
        val newUsers = user.copy(
                childList = childResponse.child_device_list,
                currentChild = newDefaultChild,
                currentDevice = newDefaultDevice)
        //save
        saveUserSynchronized(newUsers)
    }

    override fun saveUser(loginInfo: LoginData, app_token: String, expire_time: Long) {
        //save token
        appStorage.putString(APP_TOKEN_KEY, app_token)

        //save user
        val patriarch: Patriarch = loginInfo.patriarch ?: Patriarch()
        val childList = loginInfo.childList ?: emptyList<Child>()
        val childUserId: String? = loginInfo.childUserId
        val childDeviceId: String? = loginInfo.childDeviceId

        val defaultChild = childList.asSequence().find {
            it.child_user_id == childUserId
        }

        val defaultDevice = defaultChild?.device_list?.asSequence()?.find {
            it.device_id == childDeviceId
        }
        var vipRule: VipRule? = null
        if (user() != User.NOT_LOGIN) {
            if (user().vipRule != null && user().vipRule!!.member_type == loginInfo.memberInfo?.member_type) {
                vipRule = user().vipRule
            }
        }
        saveUserSynchronized(User(patriarch, childList, loginInfo.memberInfo, defaultChild, defaultDevice, vipRule = vipRule))
    }

    /**更新vip信息*/
    override fun syncUserVipRule(vipRule: VipRule) {
        val user = user()
        user.vipRule = vipRule
        saveUserSynchronized(user)
    }

    override fun updateDefaultChild(childUserId: String, childDeviceId: String) {
        val user = user()
        val childList = user.childList ?: return

        if (user.currentChildId == childUserId && user.currentChildDeviceId == childDeviceId) {
            return
        }

        val defaultChild = childList.asSequence().find {
            it.child_user_id == childUserId
        }

        val defaultDevice = defaultChild?.device_list?.asSequence()?.find {
            it.device_id == childDeviceId
        }

        saveUserSynchronized(user.copy(currentChild = defaultChild, currentDevice = defaultDevice))
    }

    @Synchronized
    private fun saveUserSynchronized(user: User) {
        if (currentUser == user) {
            ifOpenDebug {
                Timber.w("save same user, ignore it-------------------------------------------------")
                Timber.w(JsonUtils.toJson(user))
            }
            return
        }
        currentUser = user
        appStorage.putEntity(APP_USER_KEY, currentUser)
        observableUser.onNext(currentUser)
        ifOpenDebug {
            Timber.w("save new user -------------------------------------------------")
            Timber.w(JsonUtils.toJson(user))
        }
    }

    override fun logout() {
        synchronized(this) {
            Timber.d("start logout")
            currentUser = User.NOT_LOGIN
            storageManager.clearUserAssociated()
            Timber.d("logout success")
        }
        observableUser.onNext(currentUser)
    }

    override fun sendSmsCode(smsType: String, cellphone: String): Completable {
        return appApi.sendCode(smsType, cellphone).resultChecker().ignoreElements()
    }

    override fun validateSmsCode(smsType: String, cellphone: String, smsCode: String): Completable {
        return appApi.validateCode(smsType, cellphone, smsCode).resultChecker().ignoreElements()
    }

    override fun advertisingList(adFilter: AdvertisingFilter?, forceRemote: Boolean): Flowable<Optional<List<Advertising>>> {
        val storage = storageManager.stableStorage()

        val remote = appApi.loadAppAdvertisingList().optionalExtractor()

        val source = if (forceRemote) {
            remote.doOnNext {
                storage.putEntity(AD_LIST_KEY, it.orElse(null))
            }
        } else {
            val local = storage.flowableOptional<List<Advertising>>(AD_LIST_KEY).subscribeOn(schedulerProvider.io())

            concatMultiSource(remote,/*remote*/ local/*local*/) {
                storage.putEntity(AD_LIST_KEY, it)
            }

        }

        val currentTimeMillis = System.currentTimeMillis()

        val checker: (Advertising) -> Boolean = {
            (it.begin_time == 0L && it.end_time == 0L) || (it.begin_time <= currentTimeMillis && it.end_time > currentTimeMillis)
        }

        return source.map {
            if (it.isPresent) {
                val list = it.get()
                if (adFilter == null) {
                    Optional.of(list.filter { ad -> checker(ad) })
                } else {
                    Optional.of(list.filter { ad -> adFilter.filter(ad) && checker(ad) })
                }
            } else {
                it
            }
        }
    }

    override fun childDevicePermissionChecker(childUserId: String, deviceId: String): Flowable<Optional<PrivilegeData>> {
        return appApi.childDevicePermissionChecker(childUserId, deviceId).optionalExtractor()
    }
}