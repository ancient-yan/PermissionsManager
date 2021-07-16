package com.gwchina.sdk.base.third.channel;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.gwchina.sdk.base.AppContext;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-02-25 14:54
 */
public class ChannelUtils {

    /**
     * 测试渠道仅用于测试
     */
    private static final String TEST_CHANNEL = "DEVELOPER";

    /**
     * 改用帮帮加固打包了直接用友盟的AndroidManifest.xml中的name["UMENG_CHANNEL"]
     * 友盟：https://developer.umeng.com/docs/119267/detail/118588
     * bugly:https://bugly.qq.com/docs/user-guide/advance-features-android/?v=20181014122344#app
     * 极光：https://docs.jiguang.cn//jpush/client/Android/android_api/#channel-api
     *
     * @return
     */
    public static String getChannel() {
//        String channel = ChannelReaderUtil.getChannel(AppContext.getContext());
//        if (TextUtils.isEmpty(channel)) {
//            channel = TEST_CHANNEL;
//        }
//        Log.d("GwChinaParent", channel);
//        return channel;
        return getChannelName();
    }

    /**
     * 友盟自己的API方法获取渠道名
     *
     * @return
     */
    private static String getChannelName() {
//        String channelName = AnalyticsConfig.getChannel(AppContext.getContext());
        String channelName = getAppMetaData(AppContext.getContext(), "UMENG_CHANNEL");
        return channelName;
    }

    /**
     * 区分bugly测试设备：渠道名是DEVELOPER
     * @return
     */
    public static boolean isTestChannel() {
        return null != getChannelName() && getChannelName().equals(TEST_CHANNEL);
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo，因为友盟设置的meta-data是在application标签中
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        //key要与manifest中的配置文件标识一致
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resultData;
    }
}
