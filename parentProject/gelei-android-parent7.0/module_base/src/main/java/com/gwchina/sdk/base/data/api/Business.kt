/**定义一些业务上的常量，以及通用的标识判断*/
@file:JvmName("Business")

package com.gwchina.sdk.base.data.api

///////////////////////////////////////////////////////////////////////////
// 后台返回统一的true和false状态
///////////////////////////////////////////////////////////////////////////
const val INVALIDATE = -1
const val FLAG_POSITIVE = 1
const val FLAG_NEGATIVE = 0

//会员等级是否能进入页面
const val FLAG_POSITIVE_ACTION="1"
//const val FLAG_NEGATIVE_ACTION="0"

fun makeFlag(yesOrNo: Boolean) = if (yesOrNo) FLAG_POSITIVE else FLAG_NEGATIVE

fun isFlagPositive(flag: Int?): Boolean {
    return flag == FLAG_POSITIVE
}

fun isDefault(flagCode: Int): Boolean {
    return FLAG_POSITIVE == flagCode
}

fun isSelected(flag: Int): Boolean {
    return FLAG_POSITIVE == flag
}

const val YES_FLAG = "Y"
const val NO_FLAG = "N"

fun isYes(flag: String?): Boolean {
    return YES_FLAG.equals(flag, ignoreCase = true)
}

fun isNo(flag: String?): Boolean {
    return NO_FLAG.equals(flag, ignoreCase = true)
}

///////////////////////////////////////////////////////////////////////////
// 短信类型
///////////////////////////////////////////////////////////////////////////
const val SMS_LOGIN = "Login"//登陆（登陆/注册页面默认用登陆）
const val SMS_REGISTER = "Register"//	注册（暂且未用到）
const val SMS_FORGETPWD = "ForgetPwd"//忘记密码
const val UNBIND_CHILD_DEVICE = "UNBIND_CHILD_DEVICE"//解绑设备
const val WECHAT_BIND = "WeChatBind"//微信绑定

///////////////////////////////////////////////////////////////////////////
// 会员类别 	1 SVIP(原来的会员)，2 电信VIP,3 C端VIP, 0则不是会员
///////////////////////////////////////////////////////////////////////////
const val MEMBER_SVIP="01"
const val MEMBER_CT_VIP="02"
const val MEMBER_C_VIP="03"
const val MEMBER_NO_VIP="0"

///////////////////////////////////////////////////////////////////////////
// 性别
///////////////////////////////////////////////////////////////////////////
const val SEX_MALE = 1
const val SEX_FEMALE = 0
fun Int.isMale() = this == SEX_MALE
fun Int.isFemale() = this == SEX_FEMALE

///////////////////////////////////////////////////////////////////////////
// 与孩子关系
///////////////////////////////////////////////////////////////////////////
const val RELATIONSHIP_OTHER = 0
const val RELATIONSHIP_FATHER = 1
const val RELATIONSHIP_MOTHER = 2

///////////////////////////////////////////////////////////////////////////
// 守护等级：1 轻度 2 中度 3 重度
///////////////////////////////////////////////////////////////////////////
const val GUARD_LEVEL_MILD = 1
const val GUARD_LEVEL_MODERATE = 2
const val GUARD_LEVEL_SEVERE = 3

/**轻度模式*/
fun Int?.isMildMode() = this != null && this == GUARD_LEVEL_MILD

/**中度模式*/
fun Int?.isModerateMode() = this != null && this == GUARD_LEVEL_MODERATE

/**重度模式*/
fun Int?.isSevereMode() = this != null && this == GUARD_LEVEL_SEVERE

/**没有设置守护模式*/
fun Int?.isNotGuardMode() = this !in GUARD_LEVEL_MILD..GUARD_LEVEL_SEVERE

///////////////////////////////////////////////////////////////////////////
// 设备状态：0 离线 1 在线 2 设备未注册 3 异常
///////////////////////////////////////////////////////////////////////////
const val DEVICE_STATUS_OFFLINE = 0
const val DEVICE_STATUS_ONLINE = 1
const val DEVICE_STATUS_UNREGISTERED = 2
const val DEVICE_STATUS_ABNORMAL = 3

///////////////////////////////////////////////////////////////////////////
// 守护项目编号
///////////////////////////////////////////////////////////////////////////
const val GUARD_ITEM_FAMILY_NUMBER = "009"//亲情号码
const val GUARD_ITEM_TIME_GUARD = "008"//时间守护
const val GUARD_ITEM_INTERNET_GUARD = "007"//上网守护
const val GUARD_ITEM_APP_GUARD = "006"//应用守护
const val GUARD_ITEM_STEPS_STATISTICS = "005"//计步
const val GUARD_ITEM_LOCATION = "004"//定位
const val GUARD_ITEM_INTERNET_STATISTICS = "003"//上网统计
const val GUARD_ITEM_APP_STATISTICS = "002"//应用偏好
const val GUARD_ITEM_TIME_STATISTICS = "001"//用机时长

// /////////////////////////////////////////////////////////////////////////
// APP 守护类型标记：9 缺省、0-待批准、1-自由使用、2-限时可用、3-禁用
///////////////////////////////////////////////////////////////////////////
/**待批准*/
const val APP_RULE_TYPE_PENDING_APPROVAL = 0
/**缺省*/
const val APP_RULE_TYPE_DEFAULT = 9
/**自由可用*/
const val APP_RULE_TYPE_FREE = 1
/**限时可用*/
const val APP_RULE_TYPE_LIMITED = 2
/**禁用*/
const val APP_RULE_TYPE_DISABLE = 3
/**高危*/
const val APP_RULE_TYPE_RISK = 4

///////////////////////////////////////////////////////////////////////////
// 设备同步模块
///////////////////////////////////////////////////////////////////////////
/**同步守护等级指令*/
const val INSTRUCTION_SYNC_LEVEL = "GUARD_LEVEL_SYN"
/**同步时间守护指令*/
const val INSTRUCTION_SYNC_TIME = "GUARD_TIME_SYN"
/**同步应用守护指令*/
const val INSTRUCTION_SYNC_APP = "GUARD_SOFT_SYN"
/**同步网址守护指令*/
const val INSTRUCTION_SYNC_URL = "GUARD_URL_SYN"
/**同步亲情号码指令*/
const val INSTRUCTION_SYNC_PHONE = "GUARD_PHONE_SYN"
/**同步IOS设备监督模式指令*/
const val INSTRUCTION_IOS_SUPERVISED_FLAG = "IOS_SUPERVISED_FLAG"

///////////////////////////////////////////////////////////////////////////
// 用户会员状态
///////////////////////////////////////////////////////////////////////////
/**00-待生效*/
const val MEMBER_STATUS_PENDING = "00"
/**01-已生效*/
const val MEMBER_STATUS_VALID = "01"
/**02-无效*/
const val MEMBER_STATUS_INVALID = "02"

///////////////////////////////////////////////////////////////////////////
// 孩子与设备的守护状态(会员相关，会员过期后，只保留一个设备，其他设备均失效)：1-正常; 0-失效，守护过期
///////////////////////////////////////////////////////////////////////////
const val MEMBER_GUARD_STATUS_NORMAL = 1
const val MEMBER_GUARD_STATUS_EXPIRED = 0
fun isMemberGuardExpired(status: Int) = status == MEMBER_GUARD_STATUS_EXPIRED

///////////////////////////////////////////////////////////////////////////
// 广告位置：01-首页卡片，03-绑定流程广告，04-会员页面广告
///////////////////////////////////////////////////////////////////////////
const val AD_POSITION_HOME = "01"
const val AD_POSITION_BINDING = "03"
const val AD_POSITION_MEMBER = "04"

///////////////////////////////////////////////////////////////////////////
// 广告类型：01-固定位置，03-开屏，04-弹屏
///////////////////////////////////////////////////////////////////////////
const val AD_TYPE_FIX = "01"
const val AD_TYPE_LAUNCH = "02"
const val AD_TYPE_DIALOG = "04"

///////////////////////////////////////////////////////////////////////////
// 用户会员状态过期，是否选择了保留设备
///////////////////////////////////////////////////////////////////////////
const val EXPIRE_RETAIN_FLAG_NOT_SET = 2
const val EXPIRE_RETAIN_FLAG_SET = 1

///////////////////////////////////////////////////////////////////////////
// 设备类型
///////////////////////////////////////////////////////////////////////////
const val DEVICE_TYPE_IOS = "02"
const val DEVICE_TYPE_ANDROID = "03"
fun isIOS(deviceType: String?) = DEVICE_TYPE_IOS == deviceType
fun isAndroid(deviceType: String?) = DEVICE_TYPE_ANDROID == deviceType

///////////////////////////////////////////////////////////////////////////
// 软件安装状态
///////////////////////////////////////////////////////////////////////////
//状态 : 0 待接收 或 1 安装中 --》显示：安装中状态 : 2 已安装 --》显示：已安装状态 : 3 安装失败 或 4 未安装 --》显示：给孩子安装
const val APP_INSTALL_STATUS_DEFAULT = 0
const val APP_INSTALL_STATUS_INSTALLING = 1
const val APP_INSTALL_STATUS_INSTALLED = 2
const val APP_INSTALL_STATUS_INSTALL_FAILED = 3
const val APP_INSTALL_STATUS_NOT_INSTALLED = 4


///////////////////////////////////////////////////////////////////////////
// 6.0 迁移到 7.0 标识
///////////////////////////////////////////////////////////////////////////
/** 0 格雷盒子没有设备（不需要迁移）*/
const val MIGRATING_NONE = "0"
/** 1 有设备，部分可迁移（返回不可迁移设备名称）*/
const val MIGRATING_PART_SUPPORTED = "1"
/** 2 有设备，全部不可迁移（返回不可迁移设备名称）*/
const val MIGRATING_ALL_UNSUPPORTED = "2"
/** 3 有设备，全部可迁移（孩子端全部完成升级）*/
const val MIGRATING_ALL_UNSUPPORTED_CHILD_NOT_UPGRADE = "3"
/** 4 有设备，但未升级到格雷7.0或iOS孩子端未安装描述文件*/
const val MIGRATING_ALL_UNSUPPORTED_CHILD_UPGRADED = "4"

//////////////////////////////////////////////////////////////////////////
//四大守护模块是否可用标识
/////////////////////////////////////////////////////////////////////////
/**守护网址不可用*/
const val GUARD_URL_DISABLE = "guard_url"
/**亲情号码不可用*/
const val FAMILIARITY_PHONE = "familiarity_phone"
