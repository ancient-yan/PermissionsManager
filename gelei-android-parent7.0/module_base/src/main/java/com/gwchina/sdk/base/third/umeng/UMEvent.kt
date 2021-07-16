package com.gwchina.sdk.base.third.umeng

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-09-29 13:48
 *      埋点事件定义
 */
object UMEvent {

    object ClickEvent {
        //132
        //获取验证码
        const val PAGE_REGISTER_SMS = "page_register_sms"
        //点击注册或者登录
        const val PAGE_REGISTER_BUTTON = "page_register_button"
        //填写孩子信息，点击下一步
        const val PAGE_BIND_BTN_NEXT = "page_bind_btn_next"
        //点击开始扫描
        const val PAGE_BIND_BTN_SCAN = "page_bind_btn_scan"
        //查看适配机型列表
        const val PAGE_BIND_BTN_LIST = "page_bind_btn_list"
        //跳过开屏广告
        const val BTN_SKIPAD = "btn_skipad"
        //点击开屏广告
        const val BTN_CLICKAD = "btn_clickad"
        //绑定引导-首页_暂不绑定
        const val HOMEPAGE_BTN_NOBUND = "homepage_btn_nobund"
        //绑定引导-首页_绑定设备
        const val HOMEPAGE_BTN_BUND = "homepage_btn_bund"
        //添加设备
        const val HOMEPAGE_BTN_ADDDEVICE = "homepage_btn_adddevice"
        //成长日记
        const val HOMEPAGE_BTN_DIARY = "homepage_btn_diary"
        //成长树
        const val HOMEPAGE_BTN_TREE = "homepage_btn_tree"
        //相册-点击更换封面-确定
        const val HOMEPAGE_BTN_CHANGEPHOTO_YES = "homepage_btn_changephoto_yes"
        //相册-点击更换封面-取消
        const val HOMEPAGE_BTN_CHANGEPHOTO_NO = "homepage_btn_changephoto_no"
        //临时可用-开启
        const val HOMEPAGE_BTN_USE = "homepage_btn_use"
        //临时可用-关闭
        const val HOMEPAGE_BTN_CLOSE = "homepage_btn_close"
        //切换孩子
        const val HOMEPAGE_BTN_SWITCHCHILD = "homepage_btn_switchchild"
        //切换设备
        const val HOMTPAGE_BTN_SWITCHDEVICE = "homtpage_btn_switchdevice"
        //守护时间
        const val HOMEPAGE_BTN_TIME = "homepage_btn_time"
        //守护应用
        const val HOMEPAGE_BTN_SOFT = "homepage_btn_soft"
        //守护上网
        const val HOMEPAGE_BTN_GUARDWEB = "homepage_btn_guardweb"
        //亲情号码
        const val HOMEPAGE_BTN_FAMILYNUMBER = "homepage_btn_familynumber"
        //广告卡片
        const val HOMEPAGE_BTN_AD = "homepage_btn_ad"
        //守护周报
        const val HOMEPAGE_BTN_WEEKLY = "homepage_btn_weekly"
        //首页_应用审批_允许
        const val HOMEPAGE_BTN_AUDITING_ALLOW = "homepage_btn_auditing_allow"
        //首页_应用审批_禁止
        const val HOMEPAGE_BTN_AUDITING_PROHIBIT = "homepage_btn_auditing_prohibit"
        //首页_亲情号码_允许
        const val HOMEPAGE_BTN_FAMILYNUMBER_ALLOW = "homepage_btn_familynumber_allow"
        //首页_亲情号码_禁止
        const val HOMEPAGE_BTN_FAMILYNUMBER_PROHIBIT = "homepage_btn_familynumber_prohibit"
        //今日已用
        const val HOMEPAGE_BTN_TODAYUESD = "homepage_btn_todayuesd"
        //首页_今日偏爱
        const val HOMEPAGE_BTN_TODAYFAVOURITE = "homepage_btn_todayfavourite"
        //首页_今日步数
        const val HOMEPAGE_BTN_TODAYSTEPNUM = "homepage_btn_todaystepnum"
        //首页_孩子位置
        const val HOMEPAGE_BTN_LOCATION = "homepage_btn_location"
        //应用精选-查看更多
        const val HOMEPAGE_BTN_MOREAPP = "homepage_btn_moreapp"
        //应用精选-给孩子安装
        const val HOMEPAGE_BTN_APPINSTALL = "homepage_btn_appinstall"
        //守护等级_轻度
        const val GROWTLEVEV_BTN_LIGHTGUARD = "growtlevev_btn_lightguard"
        //守护等级_中度
        const val GROWTLEVEV_BTN_MODERATEGRARD = "growtlevev_btn_moderategrard"
        //守护等级_重度
        const val GROWTLEVEV_BTN_SERIOUSGRARD = "growtlevev_btn_seriousgrard"
        //时间守护_创建时间守护计划
        const val TIMEVIEW_BTN_START = "timeview_btn_start"
        //时间守护_点击使用备用计划
        const val TIMEVIEW_START_BTN_SPAREPLAN = "timeview_start_btn_spareplan"
        //时间守护_点击自行制定计划
        const val TIMEVIEW_START_BTN_ADDPLAN = "timeview_start_btn_addplan"
        //设置每日可用时长
        const val TIMEVIEW_START_BTN_SETSHICHANG = "timeview_start_btn_setShiChang"
        //设置守护日
        const val TIMEVIEW_START_BTN_SETDATE = "timeview_start_btn_setdate"
        //添加新计划
        const val TIMEVIEW_START_BTN_ADD_NEW_PLAN = "timeview_start_btn_add_new_plan"
        //时间守护_完成时间计划设置
        const val TIMEVIEW_START_BTN_FINISH = "timeview_start_btn_finish"
        //时间守护_复制时间计划
        const val TIMEVIEW_BTN_COPYPLAN = "timeview_btn_copyplan"
        //时间守护_改变时段
        const val TIMEVIEW_BTN_CHANGESHIDUAN = "timeview_btn_changeShiDuan"
        //时间守护_改变时长
        const val TIMEVIEW_BTN_CHANGESHICHANG = "timeview_btn_changeShiChang"
        //时间守护_删除时间计划
        const val TIMEVIEW_BTN_DETELEPLAN = "timeview_btn_deteleplan"
        //时间守护_备用计划
        const val TIMEVIEW_BTN_SPAREPLAN = "timeview_btn_spareplan"
        //时间守护_备用计划_制定备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_ADD = "timeview_btn_spareplan_add"
        //时间守护_备用计划_完成制定备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_FINISH = "timeview_btn_spareplan_finish"
        //时间守护_备用计划_编辑备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_EDITOR = "timeview_btn_spareplan_editor"
        //时间守护_备用计划_编辑备用计划_改变时长
        const val TIMEVIEW_BTN_SPAREPLAN_EDITOR_CHANGESHICHANG = "timeview_btn_spareplan_editor_changeShiChang"
        //时间守护_备用计划_编辑备用计划_改变时段
        const val TIMEVIEW_BTN_SPAREPLAN_EDITOR_CHANGESHIDUAN = "timeview_btn_spareplan_editor_changeShiDuan"
        //时间守护_备用计划_启用备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_START = "timeview_btn_spareplan_start"
        //时间守护_备用计划_展开备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_SPREAD = "timeview_btn_spareplan_spread"
        //时间守护_备用计划_删除备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_DELETE = "timeview_btn_spareplan_delete"
        //时间守护_备用计划_停用备用计划
        const val TIMEVIEW_BTN_SPAREPLAN_STOP = "timeview_btn_spareplan_stop"
        //时间守护_备用计划_停用备用计划_保留计划
        const val TIMEVIEW_BTN_SPAREPLAN_STOP_RETAIN = "timeview_btn_spareplan_stop_retain"
        //时间守护_备用计划_停用备用计划_更新计划
        const val TIMEVIEW_BTN_SPAREPLAN_STOP_RENEW = "timeview_btn_spareplan_stop_renew"
        //应用守护_创建应用守护计划
        const val SOFTVIEW_BTN_START = "softview_btn_start"
        //应用守护_设置锁屏可用
        const val SOFTVIEW_START_BTN_FREE = "softview_start_btn_free"
        //应用守护_设置禁止使用
        const val SOFTVIEW_START_BTN_PROHIBIT = "softview_start_btn_prohibit"
        //应用守护_完成应用手机计划设置
        const val SOFTVIEW_START_BTN_FINISH = "softview_start_btn_finish"
        //应用守护_添加限时分组
        const val SOFTVIEW_BTN_ADDGROUP = "softview_btn_addgroup"
        //应用守护_添加限时分组_完成
        const val SOFTVIEW_BTN_SADDGROUP_FINISH = "softview_btn_saddgroup_finish"
        //应用守护_应用分组_添加分组应用
        const val SOFTVIEW_GROUP_BTN_ADDSOFT = "softview_group_btn_addsoft"
        //应用守护_应用分组_修改分组使用时长
        const val SOFTVIEW_GROUP_BTN_CHANGESHICHANG = "softview_group_btn_changeShiChang"
        //应用守护_应用分组_修改分组使用时段
        const val SOFTVIEW_GROUP_BTN_CHANGESHIDUAN = "softview_group_btn_changeShiDuan"
        //应用守护_删除限时分组
        const val SOFTVIEW_GROUP_BTN_DETELEGROUP = "softview_group_btn_detelegroup"
        //应用守护_单个应用_禁用
        const val SOFTVIEW_EDITOR_BTN_PROHIBIT = "softview_editor_btn_prohibit"
        //应用守护_单个应用_自由使用
        const val SOFTVIEW_EDITOR_BTN_FREE = "softview_editor_btn_free"
        //应用守护_单个应用_限时使用
        const val SOFTVIEW_EDITOR_BTN_TIMELIMIT = "softview_editor_btn_timelimit"
        //应用守护_单个应用_修改时长
        const val SOFTVIEW_EDITOR_BTN_CHANGESHICHANG = "softview_editor_btn_changeShiChang"
        //应用守护_单个应用_修改时段
        const val SOFTVIEW_EDITOR_BTN_CHANGESHIDUAN = "softview_editor_btn_changeShiDuan"
        //应用守护_禁止使用_添加应用
        const val SOFTVIEW_PROHIBIT_BTN_ADD = "softview_prohibit_btn_add"
        //应用守护_自由可用_添加应用
        const val SOFTVIEW_FREE_BTN_ADD = "softview_free_btn_add"
        //应用守护_限时可用_添加应用
        const val SOFTVIEW_TIMELIMIT_BTN_ADD = "softview_timelimit_btn_add"
        //应用守护_待审批
        const val SOFTVIEW_AUDITING = "softview_auditing"
        //应用守护_待审批_允许应用
        const val SOFTVIEW_AUDITING_BTN_ALLOW = "softview_auditing_btn_allow"
        //应用守护_待审批_禁止应用
        const val SOFTVIEW_AUDITING_BTN_PROHIBIT = "softview_auditing_btn_prohibit"
        //应用守护_待审批_审批记录
        const val SOFTVIEW_AUDITING_REPORT = "softview_auditing_report"
        //守护上网_开启
        const val GUARDWEB_BTN_OPEN = "guardweb_btn_open"
        //守护上网_关闭
        const val GUARDWEB_BTN_CLOSE = "guardweb_btn_close"
        //守护上网_拦截记录
        const val GUARDWEB_BTN_INTERCEPT = "guardweb_btn_intercept"
        //守护上网_上网管理
        const val GUARDWEB_BTN_WEBMANAGE = "guardweb_btn_webmanage"
        //守护上网_添加黑名单
        const val GUARDWEB_BTN_WEBBLACK = "guardweb_btn_webblack"
        //守护上网_添加黑名单_完成
        const val GUARDWEB_BTN_WEBBLACK_FINISH = "guardweb_btn_webblack_finish"
        //守护上网_添加白名单
        const val GUARDWEB_BTN_WEBWHITE = "guardweb_btn_webwhite"
        //守护上网_添加白名单_完成
        const val GUARDWEB_BTN_WEBWHITE_FINISH = "guardweb_btn_webwhite_finish"
        //守护上网_黑名单_删除
        const val GUARDWEB_BTN_WEBBLACK_DELETE = "guardweb_btn_webblack_delete"
        //守护上网_黑名单_删除_完成
        const val GUARDWEB_BTN_WEBBLACK_DELETE_FINISH = "guardweb_btn_webblack_delete_finish"
        //删除白名单网址
        const val GUARDWEB_BTN_WEBWHITE_DELETE = "guardweb_btn_webwhite_delete"
        //删除白名单网址完成
        const val GUARDWEB_BTN_WEBWHITE_DELETE_FINISH = "guardweb_btn_webwhite_delete_finish"
        //亲情号码_开启
        const val FAMILYNUMBER_BTN_OPEN = "familynumber_btn_open"
        //亲情号码_关闭
        const val FAMILYNUMBER_BTN_CLOSE = "familynumber_btn_close"
        //亲情号码_拨打/接听范围设置
        const val FAMILYNUMBER_BTN_SCOPE = "familynumber_btn_scope"
        //亲情号码_拨打/接听范围设置_所有号码拨打开启
        const val FAMILYNUMBER_BTN_SCOPE_ALLCALL_OPEN = "familynumber_btn_scope_allcall_open"
        //亲情号码_拨打/接听范围设置_所有号码拨打关闭
        const val FAMILYNUMBER_BTN_SCOPE_ALLCALL_CLOSE = "familynumber_btn_scope_allcall_close"
        //亲情号码_拨打/接听范围设置_所有号码接听开启
        const val FAMILYNUMBER_BTN_SCOPE_ALLANSWER_OPEN = "familynumber_btn_scope_allanswer_open"
        //亲情号码_拨打/接听范围设置_所有号码接听关闭
        const val FAMILYNUMBER_BTN_SCOPE_ALLANSWER_CLOSE = "familynumber_btn_scope_allanswer_close"
        //亲情号码_添加亲情号码
        const val FAMILYNUMBER_BTN_ADDNUMBER = "familynumber_btn_addnumber"
        //亲情号码_添加亲情号码_完成
        const val FAMILYNUMBER_BTN_ADDNUMBER_FINISH = "familynumber_btn_addnumber_finish"
        //亲情号码_待批准
        const val FAMILYNUMBER_BTN_AUDITING = "familynumber_btn_auditing"
        //亲情号码_待批准_允许
        const val FAMILYNUMBER_BTN_AUDITING_ALLOW = "familynumber_btn_auditing_allow"
        //亲情号码_待批准_拒绝
        const val FAMILYNUMBER_BTN_AUDITING_PROHIBIT = "familynumber_btn_auditing_prohibit"
        //亲情号码_添加亲情号码_添加分组
        const val FAMILYNUMBER_BTN_ADDNUMBER_ADDGROUP = "familynumber_btn_addnumber_addgroup"
        //亲情号码_添加亲情号码_添加分组_完成
        const val FAMILYNUMBER_BTN_ADDNUMBER_ADDGROUP_FINISH = "familynumber_btn_addnumber_addgroup_finish"
        /*
         *以下是H5埋点
        //成长树_成长记录
         const val TREE_BTN_GWORTREPORT = "tree_btn_gwortreport"
         //成长树_浇水
         const val TREE_BTN_WATER = "tree_btn_water"
         //成长树_成长进度
         const val TREE_BTN_GWORTRATE = "tree_btn_gwortrate"
         //成长树_成长值获取
         const val TREE_BTN_GWORTGET = "tree_btn_gwortget"
         //数据统计_使用时长排行榜
         const val DATA_BTN_TIMELIST = "data_btn_timelist"
         //数据统计_应用偏好排行榜
         const val DATA_BTN_COUNTSOFT = "data_btn_countsoft"
         //数据统计_步数排行榜
         const val DATA_BTN_STEPLIST = "data_btn_steplist"
         //数据统计_数据定位
         const val DATA_BTN_LOCATION = "data_btn_location"
         //数据统计_切换设备
         const val DATA_BTN_SWITCHDEVICE = "data_btn_switchdevice"
         //数据统计_日榜
         const val DATA_BTN_DAYLIST = "data_btn_daylist"
         //数据统计_周榜
         const val DATA_BTN_WEEKLIST = "data_btn_weeklist"
         //数据统计_月榜
         const val DATA_BTN_MONTHLIST = "data_btn_monthlist"
         //数据统计_给孩子安装
         const val DATA_BTN_APPINSTALL = "data_btn_appinstall"*/
        //应用推荐_应用专题
        const val COMMEND_BTN_TYPE = "commend_btn_type"
        //应用推荐_给孩子安装
        const val COMMEND_BTN_APPINSTALL = "commend_btn_appinstall"
        //我的_个人信息
        const val MYVIEW_BTN_PERSONAL = "myview_btn_personal"
        //我的_孩子信息
        const val MYVIEW_BTN_CHILDREN = "myview_btn_children"
        //我的_新增设备绑定
        const val MYVIEW_BTN_TOBINDDEVICE = "myview_btn_tobinddevice"
        //我的_已绑设备
        const val MYVIEW_BTN_BINDDEVICE = "myview_btn_binddevice"
        //我的_守护等级
        const val MYVIEW_BTN_GROWTLEVEV = "myview_btn_growtlevev"
        //我的_点击会员中心
        const val MYVIEW_BTN_MEMBERCENTER = "myview_btn_membercenter"
        //我的_守护报告
        const val MYVIEW_BTN_REPORT = "myview_btn_report"
        //我的_邀请好友
        const val MYVIEW_BTN_INVITEFRIEND = "myview_btn_invitefriend"
        //我的_消息通知
        const val MYVIEW_BTN_NEWS = "myview_btn_news"
        //我的_帮助与反馈
        const val MYVIEW_BTN_HELP = "myview_btn_help"
        //我的_关于我们
        const val MYVIEW_BTN_GELEI = "myview_btn_gelei"
        //已绑设备_解绑设备_确定
        const val BINDDEVICE_BTN_UNTYING_CONFIRM = "binddevice_btn_untying_confirm"
        //已绑设备_守护等级
        const val BINDDEVICE_BTN_GROWTLEVEV = "binddevice_btn_growtlevev"
        //已绑设备_添加新设备
        const val BINDDEVICE_BTN_ADDDEVICE = "binddevice_btn_adddevice"
        //续费会员
        const val MEMBERCENTER_BTN_RENEW = "membercenter_btn_renew"
        //会员记录
        const val MEMBERCENTER_BTN_RECORD = "membercenter_btn_record"

    }

    object PageEvent {
        //16
        //进入到注册页面
        const val PAGE_REGISTER = "page_register"
        //进入到绑定页面
        const val PAGE_BIND = "page_bind"
        //设备绑定成功
        const val PAGE_BIND_SUCCESS = "page_bind_success"
        //进入到守护模式页面
        const val PAGE_GROWTLEVEV = "page_growtlevev"
        //时间守护_曝光
        const val PAGE_TIMEVIEW = "page_timeview"
        //进入到应用守护页面
        const val PAGE_SOFTVIEW = "page_softview"
        //守护上网_曝光
        const val PAGE_GUARDWEB = "page_guardweb"
        //亲情号码_曝光
        const val PAGE_FAMILYNUMBER = "page_familynumber"
        //进入到成长树页面
        const val PAGE_TREE = "page_tree"
        //进入到数据统计页面
        const val PAGE_DATA = "page_data"
        //应用推荐_曝光
        const val PAGE_COMMEND = "page_commend"
        //进入到个人信息页面
        const val PAGE_PERSONAL = "page_personal"
        //进入到孩子信息页面
        const val PAGE_CHILDREN = "page_children"
        //进入到已绑设备页面
        const val PAGE_BINDDEVICE = "page_binddevice"
        //守护报告_曝光
        const val PAGE_GUARDREPORT = "page_guardreport"
        //会员中心页面_曝光
        const val PAGE_MEMBERCENTER = "page_membercenter"

    }
}