package com.gwchina.sdk.base.router;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.gwchina.sdk.base.config.GlobalConstants;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-02 14:04
 */
public class RouterPath {

    public static final String PAGE_KEY = "page_key";//int
    private static final String DEVICE_ID_KEY = GlobalConstants.DEVICE_ID_KEY;
    private static final String CHILD_USER_ID_KEY = GlobalConstants.CHILD_USER_ID_KEY;

    /**
     * 启动页
     */
    public static final class Launcher {
        public static final String PATH = "/gwchina_main/launcher";
        public static final String AD_PATH = "/gwchina_main/launcher_ad";
        public static final String AD_KEY = "launcher_ad_key";
    }

    /**
     * 首页
     */
    public static final class Main {
        public static final String PATH = "/gwchina_main/main";
        //孩子位置详情
        public static final String LOCATION = "/gwchina_main/location";

        public static final String LOCATION_KEY = "location_key";

        public static final String ACTION_KEY = "action_key";//int

        /**
         * 登录过期后，需要重新登录，统一先回到主界面，然后由主界面发起登录流程
         */
        public static final int ACTION_RE_LOGIN = 1;

        /**
         * 会员七天后到期提醒
         */
        public static final int ACTION_MEMBER_EXPIRING_TIPS = 2;

        public static final int PAGE_HOME = 0;
        public static final int PAGE_MINE = 1;
        public static final int PAGE_APP_APPROVAL = 4;
    }

    public static final class Diary {
        public static final String PATH = "/gwchina_main/diary";
        public static final int PAGE_LIST = 0;
        public static final int PAGE_PUBLISH = 1;
    }

    /**
     * 登录注册相关
     */
    public static final class Account {
        public static final String PATH = "/gwchina_account/account";
        public static final int REQUEST_CODE = 1003;
        /**
         * 用于从 {@link android.app.Activity#startActivityForResult(Intent, int)}方式中获取登录类型，参考{@link #LOGIN_TYPE_NEW}和{@link #LOGIN_TYPE_OLD}
         */
        public static final String LOGIN_TYPE_KEY = "LOGIN_TYPE_KEY";//return,int
        public static final String VIP_PRESENT_DAYS_KEY = "VIP_PRESENT_DAYS_KEY";//return,String
        public static final int LOGIN_TYPE_NEW = 2;//新用户
        public static final int LOGIN_TYPE_OLD = 1;//老用户
    }

    /**
     * 内置浏览器
     */
    public static final class Browser {

        public static final String PATH = "/gwchina_browser/browser";

        public static final String URL_KEY = "url_key";//string

        /**
         * for set custom fragment.
         */
        public static final String FRAGMENT_KEY = "fragment_class_key";//string

        /**
         * define whether the fragment should show it's header. default is true
         */
        public static final String SHOW_HEADER_KEY = "show_header";//boolean

        /**
         * define the {@link com.gwchina.sdk.base.web.JsCallInterceptor}, the defined JsCallInterceptor must has a constructor without arguments.
         */
        public static final String JS_CALL_INTERCEPTOR_CLASS_KEY = "js_call_interceptor_class_key";//string with full path class name

        /**
         * pass a bundle to fragment, through {@link Fragment#getArguments()} to get fragment's arguments, then through {@link android.os.Bundle#getBundle(String)} to get this bundle.
         */
        public static final String ARGUMENTS_KEY = "arguments_key";//Bundle

        /**
         * config the web view if can use cache, default is false
         */
        public static final String CACHE_ENABLE = "cache_enable";//boolean


        /**
         * config the web view if can use cache, default is false
         */
        public static final String LOAD_NO_CACHE_ENABLE = "load_no_cache_enable";//boolean

        /**
         * title res id
         */
        public static final String WEB_TITLE = "web_title";//String

        /*--------------url--------------*/
        /*关于我们*/
        public static final String ABOUT_US = "about_us.html#/";
        /*隐私协议*/
        public static final String PRIVACY = "/gelei-guard-h5/share/privacy.html#/";
        /*用户协议*/
        public static final String AGREEMENT = "/gelei-guard-h5/share/protocol.html#/";
        /*帮助与反馈*/
        public static final String HELP_FEEDBACK = "help.html#/?roletype=1";
        /*支持的设备列表*/
        public static final String SUPPORT_DEVICE_LIST = "sup_mode.html#/adapterlist";
        /*IOS监督模式指引*/
        public static final String IOS_SUPERVISE_MODE = "sup_mode.html#/";
        /*邀请好友*/
        public static final String INVITATION_FRIENDS = "invitation_friends.html#/";//?DEVICEID=%s&USERID=%s&CHANNELID=01
        /*日记示例*/
        public static final String DIARY_EXAMPLE = "/gelei-guard-h5/share/diary_example.html#/";
        /*成长树*/
        public static final String GROWING_TREE = "growing_tree.html#/";
        /*会员协议*/
        public static final String VIP_PROTOCOL = "/gelei-guard-h5/share/protocol.html#/member";
        /*在线客服*/
        public static final String CUSTOMER = "/gelei-guard-h5/share/customer.html#/";
        /*守护统计*/
        public static final String GUARD_STATISTICS = "guard_statistics.html#/";
        /*守护周报*/
        public static final String GUARD_WEEKLY = "guardian_weekly.html#/";
        /*安装IOS描述文件*/
        public static final String IOS_DESC_FILE_INSTALL = "/gelei-guard-h5/share/descriptions.html#/install";
        /*更新IOS描述文件*/
//        public static final String IOS_DESC_FILE_UPDATE = "/gelei-guard-h5/share/descriptions.html#/update";
        public static final String IOS_DESC_FILE_UPDATE = "/gelei-guard-h5/share/descriptions.html#/update-with-partiarch";
        /*使用统计说明*/
        public static final String USING_STATISTICS_INFO = "/gelei-guard-h5/share/descriptions.html#/time-statistics";
        /*孩子端设备权限设置教程*/
        public static final String CHILD_DEVICE_PERMISSION_INFO = "help.html#/barrier-free";
        /*孩子端离线排查*/
        public static final String CHILD_DEVICE_OFFFLINE_REASON = "help.html#/offline-checked";
        /*--------------url--------------*/
    }

    /**
     * 绑定设备
     */
    public static final class Binding {
        public static final String PATH = "/gwchina_binding/binding";
        /**
         * if Binding Module have bound a new device successfully, will return {@link android.app.Activity#RESULT_OK} through onActivityResult(int, int, Intent)}
         */
        public static final int REQUEST_CODE = 1004;
        /**
         * open scanning page directly to add device for the specified child.
         */
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String

        /**
         * when Binding module have bound a new device, you can get the new device id through {@link android.app.Activity#onActivityReenter(int, Intent)}.
         */
        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String

        public static final String SELECT_CHILD_KEY = "select_child_key";//Boolean
    }


    /**
     * 设置守护等级
     */
    public static final class GuardLevel {
        public static final String PATH = "/gwchina_guard/level";

        public static final int REQUEST_CODE = 1005;

        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String

        /**
         * key for {@link ChildDeviceInfo}
         */
        public static final String CHILD_DEVICE_INFO_KEY = "child_device_info_key";

        /**
         * 是否从数据迁移跳过去的
         */
        public static final String IS_FROM_MIGRATION = "is_from_migration";

        /**
         * key for {@link SelectedLevelInfo}
         */
        public static final String SELECTED_LEVEL_INFO = "selected_level_info";

        /**
         * 给指定的设备设置守护等级，需要提供孩子 id 和设备 id。
         */
        public static final int ACTION_SETTING_LEVEL = 1;

        /**
         * 选择一个守护等级，需要提供一个 {@link ChildDeviceInfo} 对象，用户好等级后返回一个 {@link SelectedLevelInfo} 对象。
         */
        public static final int ACTION_CHOOSING_LEVEL = 2;
    }

    /**
     * 数据迁移
     */
    public static final class Migration {
        public static final String PATH = "/gwchina_migration/migration";
        /**
         * key for {@link MigrationInfo}
         */
        public static final String MIGRATION_INFO_KEY = "migration_info_key";
    }

    /**
     * IOS监督模式
     */
    public static final class IOSSuperviseMode {
        public static final String PATH = "/gwchina_guard/supervise_mode";
    }

    /**
     * 应用守护模块
     */
    public static final class AppsGuard {
        public static final String PATH = "/gwchina_guard/app";
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String
        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String

        /**
         * key for {@link AppInfo}, when need open "App Approval" page from other Activities, You need pass a AppInfo object which need  be approved
         */
        public static final String APP_INFO_KEY = "app_info_key";
    }

    /**
     * 时间守护模块
     */
    public static final class TimesGuard {
        public static final String PATH = "/gwchina_guard/time";
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String
        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String
    }

    /**
     * 上网守护模块
     */
    public static final class NetGuard {
        public static final String PATH = "/gwchina_guard/net";
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String
        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String
    }

    /**
     * 亲情号码模块
     */
    public static final class FamilyPhone {
        public static final String PATH = "/gwchina_guard/family";
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String
        public static final String DEVICE_ID_KEY = RouterPath.DEVICE_ID_KEY;//String
        public static final String PHONE_PLAN_HAS_BE_SET="phonePlanHasBeSet";
    }

    /**
     * 远程截屏模块
     */
    public static final class Screenshot {
        public static final String PATH = "/gwchina_screenshot/screenshot";
    }

    /**
     * 应用推荐
     */
    public static final class Recommend {
        public static final String PATH = "/gwchina_recommend/recommend";
        public static final String RECOMMEND_ID_KEY = "recommend_id_key";//string
    }

    /**
     * 个人信息
     */
    public static final class Profile {
        public static final String PATH = "/gwchina_profile/profile";
        public static final int PAGE_CHILD_INFO = 1;
        public static final int PAGE_PATRIARCH_INFO = 2;
        public static final String CHILD_USER_ID_KEY = RouterPath.CHILD_USER_ID_KEY;//String
        //孩子信息页面，点击开启临时锁屏是否需要判断权限弹窗
        public static final String CHILD_NEED_SHOW_PERMISSION_KEY = "child_need_show_permission_key";
    }

    /**
     * 会员中心
     */
    public static final class MemberCenter {
        public static final String PATH = "/gwchina_member_center/member_center";
        public static final int REQUEST_CODE = 1010;
        public static final int CENTER = 0;
        /**7.2起没有会员购买页面，所有跳转跳至会员中心*/
//        public static final int PURCHASE = 1;
    }

    public static final class Message {
        public static final String PATH = "/gwchina_message/message";
    }

}