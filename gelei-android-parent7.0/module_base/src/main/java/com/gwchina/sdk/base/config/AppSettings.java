package com.gwchina.sdk.base.config;

import android.app.Application;

import com.android.sdk.cache.MMKVStorageImpl;
import com.android.sdk.cache.Storage;
import com.blankj.utilcode.util.AppUtils;

/**
 * <pre>
 *     <li>存储全局设置</li>
 *     <li>存储一些 UI 标记</li>
 * </pre>
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 11:03
 */
public class AppSettings {

    private AppSettings() {
        throw new UnsupportedOperationException();
    }

    private static final String STABLE_SETTING_SP_NAME = "stable_setting_sp_name";//spName

    /**
     * 用户解绑最后一台设备之后，需要重置为 false
     */
    public static final String TIME_OPERATION_TIPS_SHOWED_FLAG = "time_operation_tips_showed_flag";

    /**
     * 是否需要进行 6.0 到 7.0 的迁移
     */
    public static final String NEED_MIGRATION_FLAG = "need_migration_flag";

    private static Storage sStorage;
    private static boolean sSupportStatusBarLightMode = false;

    @SuppressWarnings("unused")
    public synchronized static void init(Application application) {
        sStorage = new MMKVStorageImpl(application, STABLE_SETTING_SP_NAME, false);
    }

    /**
     * 相同版本的 app 是不是第一次启动
     */
    public static boolean isFirstInSameVersion(String flag) {
        int last = sStorage.getInt(flag, -1);
        int curr = AppUtils.getAppVersionCode();
        if (last < curr) {
            sStorage.putInt(flag, curr);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断一个 flag 是否没有存储过
     *
     * @param flag 表示
     * @return true表示没有存储过
     */
    public static boolean isFirst(String flag) {
        boolean first = settingsStorage().getBoolean(flag, true);
        if (first) {
            settingsStorage().putBoolean(flag, false);
            return true;
        } else {
            return false;
        }
    }

    public static void setSupportStatusBarLightMode(boolean supportStatusBarLightMode) {
        sSupportStatusBarLightMode = supportStatusBarLightMode;
    }

    public static boolean supportStatusBarLightMode() {
        return sSupportStatusBarLightMode;
    }

    public static Storage settingsStorage() {
        return sStorage;
    }

}
