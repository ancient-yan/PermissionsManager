package com.gwchina.sdk.base.third.bugly;

import android.app.Application;

import com.gwchina.sdk.base.debug.Debug;
import com.gwchina.sdk.base.third.channel.ChannelUtils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Locale;

import timber.log.Timber;

/**
 * <p>
 * Bugly 与 Tinker 热修复配置，目前没有开启和配置 Bugly 的全量升级功能。
 * </p>
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-3 17:07
 */
public class TinkerTools {

    //这个id对应的是Android-家长
    private static final String RELEASE_ID = "118ec9fefe";
    private static final String DEV_ID = "54bcc755e6";

    /**
     * 执行到这段代码时会发生一个Crash，Logcat的TAG=CrashReportInfo中输出
     */
    public static void testBugly() {
        CrashReport.testJavaCrash();
    }

    /**
     * 安装 tinker，务必在 MultiDex.install(this); 后调用
     */
    public static void installTinker() {
        Beta.installTinker();
    }

    /**
     * 在 Application 的 super.onCreate(); 后调用
     *
     * @param application app
     */
    public static void initBuglyTinker(Application application) {
        initTinker();
        initBugReport(application);
    }

    private static void initBugReport(Application application) {
        Timber.d("TinkerTools init Bugly");
        boolean isTestChannel = ChannelUtils.isTestChannel();
        Bugly.setIsDevelopmentDevice(application, isTestChannel);
        Bugly.setAppChannel(application, ChannelUtils.getChannel());
        Bugly.init(application, Debug.isOpenDebug() ? DEV_ID : RELEASE_ID, isTestChannel);
    }

    private static void initTinker() {
        Timber.d("TinkerTools initHotFix");
        // 设置是否开启热更新能力，默认为 true
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁
        Beta.canAutoDownloadPatch = true;
        // 设置是否提示用户重启
        Beta.canNotifyUserRestart = false;
        // 设置是否自动合成补丁
        Beta.canAutoPatch = true;
        initHotFixingListener();
        //不使用 bugly 的全量升级
        //Beta.upgradeListener
    }

    private static void initHotFixingListener() {
        //补丁回调接口，可以监听补丁接收、下载、合成的回调
        Beta.betaPatchListener = new BetaPatchListener() {

            @Override
            public void onPatchReceived(String patchFileUrl) {
                Timber.d("onPatchReceived() called with: patchFileUrl = [" + patchFileUrl + "]");
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
                Timber.d(String.format(Locale.getDefault(), "%s %d%%", Beta.strNotificationDownloading, (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)));
            }

            @Override
            public void onDownloadSuccess(String patchFilePath) {
                Timber.d("onDownloadSuccess() called with: patchFilePath = [" + patchFilePath + "]");
            }

            @Override
            public void onDownloadFailure(String msg) {
                Timber.d("onDownloadFailure() called with: msg = [" + msg + "]");
            }

            @Override
            public void onApplySuccess(String msg) {
                Timber.d("onApplySuccess() called with: msg = [" + msg + "]");
            }

            @Override
            public void onApplyFailure(String msg) {
                Timber.d("onApplyFailure() called with: msg = [" + msg + "]");
            }

            @Override
            public void onPatchRollback() {
                Timber.d("onPatchRollback() called");
            }

        };
    }

}