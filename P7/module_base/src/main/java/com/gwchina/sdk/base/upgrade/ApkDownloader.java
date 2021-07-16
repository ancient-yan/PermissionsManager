package com.gwchina.sdk.base.upgrade;

import com.android.base.utils.common.CloseUtils;
import com.android.base.utils.common.FileUtils;
import com.android.sdk.net.exception.NetworkErrorException;
import com.gwchina.sdk.base.AppContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载APK
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-01-04 10:40
 */
final class ApkDownloader {

    private String mUrl;
    private ApkDownloaderListener mApkDownloaderListener;
    private File mDesFile;
    private File mTempDesFile;
    private static final String TEMP_MASK = "temp_%s";
    private long mNotifyTime = 0;

    ApkDownloader(String url, String desFilePath, ApkDownloaderListener apkDownloaderListener) {
        mUrl = url;
        mApkDownloaderListener = apkDownloaderListener;
        mDesFile = new File(desFilePath);
        String name = mDesFile.getName();
        mTempDesFile = new File(mDesFile.getParent(), String.format(TEMP_MASK, name));
    }

    void start() {
        OkHttpClient.Builder builder = AppContext.httpClient().newBuilder();
        builder.interceptors().clear();
        OkHttpClient okHttpClient = builder.build();

        Request request = new Request.Builder()
                .url(mUrl)
                .build();

        ResponseBody body;
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                body = response.body();
            } else {
                mApkDownloaderListener.onFail(new NetworkErrorException());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mApkDownloaderListener.onFail(e);
            return;
        }
        //检查body
        if (body == null) {
            mApkDownloaderListener.onFail(new NetworkErrorException());
            return;
        }
        if (mDesFile.exists() && mDesFile.length() == body.contentLength()) {
            //todo md5 comparison
            mApkDownloaderListener.onSuccess(mDesFile);
        } else {
            FileUtils.makeFilePath(mDesFile);
            FileUtils.makeFilePath(mTempDesFile);
            boolean success = startDownload(body);
            if (success) {
                boolean renameToSuccess = mTempDesFile.renameTo(mDesFile);
                if (renameToSuccess) {
                    mApkDownloaderListener.onSuccess(mDesFile);
                }
            }
        }
    }

    private boolean startDownload(ResponseBody body) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            byte[] buf = new byte[1024 * 2];//缓冲器
            int len;//每次读取的长度
            assert body != null;
            final long total = body.contentLength();//总的长度
            is = body.byteStream();
            long sum = 0;
            fos = new FileOutputStream(mTempDesFile);

            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                if ((System.currentTimeMillis() - mNotifyTime) >= 300) {
                    mNotifyTime = System.currentTimeMillis();
                    mApkDownloaderListener.onProgress(total, sum);
                }
            }

            mApkDownloaderListener.onProgress(total, total);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            mApkDownloaderListener.onFail(e);
            return false;
        } finally {
            CloseUtils.closeIOQuietly(is, fos);
        }
    }

    interface ApkDownloaderListener {

        void onProgress(long total, long progress);

        void onSuccess(File desFile);

        void onFail(Exception e);
    }

}
