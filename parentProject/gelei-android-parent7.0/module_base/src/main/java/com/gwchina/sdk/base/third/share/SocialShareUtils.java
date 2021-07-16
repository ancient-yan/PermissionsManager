package com.gwchina.sdk.base.third.share;

import android.app.Activity;

import com.android.sdk.social.qq.QQManager;
import com.android.sdk.social.qq.QQShareInfo;
import com.android.sdk.social.qq.ShareResultCallback;
import com.android.sdk.social.wechat.WeChatManager;
import com.android.sdk.social.wechat.WeChatShareInfo;
import com.gwchina.sdk.base.widget.dialog.TipsManager;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-08-27 11:18
 */
public class SocialShareUtils {

    /**
     * 带图片的异步分享
     *
     * @param activity      上下文环境
     * @param shareURL      分享数据
     * @param shareListener 回调
     */
    public static void share(Activity activity, ShareURL shareURL, ShareListener shareListener) {
        SharePlatform platform = shareURL.getPlatform();
        if (platform == SharePlatform.QQ) {
            shareToQQ(activity, shareURL, shareListener);
        } else if (platform == SharePlatform.WeChat || platform == SharePlatform.WeChatMoment) {
            shareToWeChat(shareURL, shareListener);
        } else {
            throw new IllegalArgumentException("不支持的平台");
        }
    }

    private static void shareToQQ(Activity activity, ShareURL shareURL, ShareListener shareListener) {

        QQManager qqManager = new QQManager(activity);

        if (!qqManager.isQQInstalled(activity)) {
            TipsManager.showMessage("哎呀，您还未安装QQ");
            return;
        }
        if (shareListener != null) {
            shareListener.onStart();
        }

        ShareImageLoader shareImageLoader = new QQShareImageLoader((data, type) -> {

            QQShareInfo qqShareInfo = new QQShareInfo();
            qqShareInfo.setTargetUrl(shareURL.getUrl())
                    .setSummary(shareURL.getDescription())
                    .setTitle(shareURL.getTitle());


            if (data != null)
                if (type == QQShareImageLoader.LOCAL_PATH) {
                    qqShareInfo.setLocalImage(data);
                } else if (type == QQShareImageLoader.REMOTE_URL) {
                    qqShareInfo.setImage(data);
                }
            qqManager.shareToQQ(activity, qqShareInfo, new ShareResultCallback() {
                @Override
                public void onSuccess() {
                    if (shareListener != null)
                        shareListener.onSuccess();
                }

                @Override
                public void onError() {
                    if (shareListener != null)
                        shareListener.onFailed();
                }

                @Override
                public void onCancel() {
                    if (shareListener != null)
                        shareListener.onCancel();
                }
            });
            return null;
        });
        display(shareURL, shareImageLoader);
    }

    private static void shareToWeChat(ShareURL shareURL, ShareListener shareListener) {
        if (!WeChatManager.getInstance().isInstalledWeChat()) {
            TipsManager.showMessage("哎呀，您还未安装微信");
            return;
        }

        if (shareListener != null) {
            shareListener.onStart();
        }

        ShareImageLoader shareImageLoader = new WeChatShareImageLoader(bytes -> {

            WeChatShareInfo.Url url = new WeChatShareInfo.Url();
            url.setWebpageUrl(shareURL.getUrl());
            url.setDescription(shareURL.getDescription());
            url.setTitle(shareURL.getTitle());

            if (bytes != null) {
                url.setThumbBmp(bytes);
            }

            SharePlatform platform = shareURL.getPlatform();
            url.setScene(platform == SharePlatform.WeChat ? WeChatShareInfo.SCENE_FRIEND : WeChatShareInfo.SCENE_MOMENT);
            boolean share = WeChatManager.getInstance().share(url, new WeChatManager.WeChatShareCallback() {
                @Override
                public void onSuccess() {
                    if (shareListener != null) {
                        shareListener.onSuccess();
                    }
                }

                @Override
                public void onCancel() {
                    if (shareListener != null) {
                        shareListener.onCancel();
                    }
                }

                @Override
                public void onFailed(BaseResp baseResp) {
                    if (shareListener != null) {
                        shareListener.onFailed();
                    }
                }
            });
            Timber.d("share successfully: " + share);
            return null;
        });

        display(shareURL, shareImageLoader);
    }

    private static void display(ShareURL shareURL, ShareImageLoader shareImageLoader) {
        Object thumbData = shareURL.getThumbData();
        if (thumbData == null) {
            shareImageLoader.displayNull();
            return;
        }
        if (thumbData instanceof String) {
            if (((String) thumbData).startsWith("http")) {
                shareImageLoader.displayUrl((String) thumbData);
            } else {
                shareImageLoader.displayLocalPath((String) thumbData);
            }
        } else if (thumbData instanceof Integer) {
            shareImageLoader.displayResource((Integer) thumbData);
        } else { //不支持的数据类型
            shareImageLoader.displayNull();
        }
    }

    public interface ShareListener {

        void onStart();

        void onSuccess();

        void onFailed();

        void onCancel();
    }

}