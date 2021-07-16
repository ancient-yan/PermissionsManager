package com.android.sdk.social.wechat;

import android.support.annotation.Nullable;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-08-27 17:04
 */
public class WeChatShareInfo {

    public static final int SCENE_FRIEND = 1;
    public static final int SCENE_MOMENT = 2;
    public static final int SCENE_FAVORITE = 3;

    abstract static class ShareContent {
    }

    public static class Url extends ShareContent {

        private int scene;
        private String webpageUrl;
        private String title;
        private String description;
        @Nullable
        private byte[] thumbBmp;

        public void setScene(int scene) {
            this.scene = scene;
        }

        String getWebpageUrl() {
            return webpageUrl;
        }

        public void setWebpageUrl(String webpageUrl) {
            this.webpageUrl = webpageUrl;
        }

        String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Nullable
        byte[] getThumbBmp() {
            return thumbBmp;
        }

        public void setThumbBmp(@Nullable byte[] thumbBmp) {
            this.thumbBmp = thumbBmp;
        }

        private int getScene() {
            return scene;
        }
    }

    static SendMessageToWX.Req buildReq(ShareContent shareContent) {
        if (shareContent instanceof Url) {
            return buildUrlReq((Url) shareContent);
        }
        throw new UnsupportedOperationException("不支持的分享内容");
    }

    private static SendMessageToWX.Req buildUrlReq(Url shareContent) {
        //初始化一个WXWebpageObject，填写url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareContent.getWebpageUrl();

        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareContent.getTitle();
        msg.description = shareContent.getDescription();
        if (shareContent.getThumbBmp() != null) {
            msg.thumbData = shareContent.getThumbBmp();
        }

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = msg;
        req.scene = mapScene(shareContent.getScene());
        req.userOpenId = WeChatManager.getAppId();
        return req;
    }

    private static int mapScene(int scene) {
        if (scene == SCENE_FAVORITE) {
            return SendMessageToWX.Req.WXSceneFavorite;
        }
        if (scene == SCENE_FRIEND) {
            return SendMessageToWX.Req.WXSceneSession;
        }
        if (scene == SCENE_MOMENT) {
            return SendMessageToWX.Req.WXSceneTimeline;
        }
        throw new UnsupportedOperationException("不支持的场景");
    }

}