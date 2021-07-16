package com.gwchina.sdk.base.data.app

import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.models.*
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 17:38
 */
interface AppDataSource {

    //====================================================================================================
    // 设备初始化
    //====================================================================================================
    /**初始化设备信息*/
    fun uploadDevice(deviceSn: String, deviceName: String, appVersion: String): Completable

    /**获取上报的设备 id*/
    fun deviceId(): String

    /**观察获取上报的设备 id，某些药业务可能需要等到上报的设备 id 后才能进行***/
    fun observableDeviceId(): Flowable<String>

    /**将初始化推送 sdk 获取到的推送唯一标识推送到服务器*/
    fun uploadPushId(deviceId: String, registerId: String): Completable

    //====================================================================================================
    // 用户登录与注销相关
    //====================================================================================================
    /**获取登录用后返回的 app_token*/
    fun appToken(): String

    /**登录之后，保存用户数据*/
    fun saveUser(loginInfo: LoginData, app_token: String, expire_time: Long)

    /**获取到用户vip配置后更新用户信息*/
    fun syncUserVipRule(vipRule: VipRule)

    /** 同步所有孩子信息，同步完毕后，从[observableUser] 获取的观察者将会收到包含新的孩子列表的[User]*/
    fun syncChildren(): Completable

    /**同步用户所有数据，同时也会刷新token，同步完毕后，从[observableUser] 获取的观察者将会收到新的[User]*/
    fun syncUser(): Completable

    /**同步获取获取用户是否已经登录，判断条件为[.user]返回值不为null*/
    fun userLogined(): Boolean

    /**同步获取用户信息。大部分情况下此方法从内存中读取用户信息，如果内存中没有用户信息才会从本地缓存加载。如果用户没有登录，则返回[User.NOT_LOGIN]*/
    fun user(): User

    /**观察用户信息，当用户信息被修改后，总是可以得到通知，这是一个全局多播的观察者，注意在不需要观察用户信息的时候取消订阅*/
    fun observableUser(): Flowable<User>

    /**更新默认孩子和设备*/
    fun updateDefaultChild(childUserId: String, childDeviceId: String)

    /**更新家长信息*/
    fun updatePatriarch(patriarch: Patriarch)

    /**更新孩子信息*/
    fun updateChild(newChild: Child)

    /**更新设备信息*/
    fun updateDevice(newDevice: Device)

    /**更新会员状态*/
    fun updateMember(member: Member)

    /**退出登录*/
    fun logout()

    //====================================================================================================
    // 全局通用
    //====================================================================================================
    /**发送验证码，[smsType]  参考 [com.gwchina.sdk.base.data.api.SMS_LOGIN] 及其相关类型*/
    fun sendSmsCode(smsType: String, cellphone: String): Completable

    /**验证验证码，[smsType]  参考 [com.gwchina.sdk.base.data.api.SMS_LOGIN] 及其相关类型*/
    fun validateSmsCode(smsType: String, cellphone: String, smsCode: String): Completable

    /**获取广告列表，type 用于制定获取哪个位置的广告，值参考 [com.gwchina.sdk.base.data.api.AD_POSITION_HOME] 及其相关类型*/
    fun advertisingList(adFilter: AdvertisingFilter? = null, forceRemote: Boolean = false): Flowable<Optional<List<Advertising>>>

    /**
     * 检查孩子端设备权限
     */
    fun childDevicePermissionChecker(childUserId: String,deviceId: String): Flowable<Optional<PrivilegeData>>

}

interface AdvertisingFilter {
    fun filter(ad: Advertising): Boolean
}