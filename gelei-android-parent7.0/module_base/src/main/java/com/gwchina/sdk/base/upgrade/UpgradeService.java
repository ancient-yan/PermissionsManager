package com.gwchina.sdk.base.upgrade;


import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.android.base.utils.common.StringChecker;
import com.app.base.R;
import com.blankj.utilcode.util.AppUtils;
import com.gwchina.sdk.base.config.DirectoryManager;

import java.io.File;

import timber.log.Timber;

public class UpgradeService extends IntentService {

    public static final String DOWNLOAD_APK_RESULT_ACTION = "com.gwchina.sdk.base.upgrade.result";

    public static final String IS_FORCE = "is_force";
    public static final String IS_SUCCESS = "is_success";
    public static final String FLAG_KEY = "flag_key";
    public static final String APK_FILE_KEY = "apk_path_key";
    private static final String URL_KEY = "url_key";
    private static final String VERSION_KEY = "version_key";

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private Handler mHandler;
    private boolean mIsForceUpdate;

    private static final int ID = 10;
    private static final String CHANNEL_ID = "gw-china-upgrade";

    private class FlagDownloaderListener implements ApkDownloader.ApkDownloaderListener {

        private String mFlag;

        private FlagDownloaderListener(String flag) {
            mFlag = flag;
        }

        @Override
        public void onProgress(long total, long progress) {
            Timber.d("onProgress() called with: total = [" + total + "], progress = [" + progress + "]");
            mHandler.post(() -> showProcess(total, progress));
        }

        @Override
        public void onSuccess(File desFile) {
            Timber.d("onSuccess() called with: desFile = [" + desFile + "]");
            mHandler.post(() -> {
                cancelNotification();
                mHandler.post(() -> notifyDownloadResult(mFlag, desFile));
            });
        }

        @Override
        public void onFail(Exception e) {
            Timber.d("onFail() called with: e = [" + e + "]");
            mHandler.post(() -> notifyDownloadResult(mFlag, null));
        }
    }

    public UpgradeService() {
        super("GW-UpgradeService");
    }

    private void showProcess(long total, long progress) {
        mBuilder.setProgress((int) total, (int) progress, false);
        mNotificationManager.notify(ID, mBuilder.build());
    }

    public static void start(Context context, String taskFlag, String updateUrl, String versionName, boolean isForceUpdate) {
        if (StringChecker.isEmpty(updateUrl) || StringChecker.isEmpty(versionName)) {
            return;
        }
        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra(URL_KEY, updateUrl);
        intent.putExtra(IS_FORCE, isForceUpdate);
        intent.putExtra(VERSION_KEY, versionName);
        intent.putExtra(FLAG_KEY, taskFlag);
        context.startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(URL_KEY);
        String versionName = intent.getStringExtra(VERSION_KEY);
        String currentFlag = intent.getStringExtra(FLAG_KEY);
        mIsForceUpdate = intent.getBooleanExtra(IS_FORCE, false);
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(versionName)) {
            new ApkDownloader(url, DirectoryManager.createAppDownloadPath(versionName), new FlagDownloaderListener(currentFlag)).start();
        }
    }

    private void cancelNotification() {
        if (mBuilder != null) {
            mBuilder.setProgress(0, 0, false);
            mNotificationManager.notify(ID, mBuilder.build());
            mNotificationManager.cancel(ID);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "upgrade", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("for show downloading apk progress");
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
        }

        mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(AppUtils.getAppName())
                .setContentText(getString(R.string.check_version_downloading_apk))
                .setSmallIcon(R.drawable.icon_notification);

        mHandler = new Handler(Looper.getMainLooper());
    }

    private void notifyDownloadResult(String flag, File desFile) {
        Intent intent = new Intent();
        intent.setAction(DOWNLOAD_APK_RESULT_ACTION);
        intent.putExtra(IS_FORCE, mIsForceUpdate);
        intent.putExtra(FLAG_KEY, flag);
        intent.putExtra(IS_SUCCESS, desFile != null);
        if (desFile != null) {
            intent.putExtra(APK_FILE_KEY, desFile);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}