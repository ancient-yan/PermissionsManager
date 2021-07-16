package com.gwchina.sdk.base.third.push;

import android.text.TextUtils;

import com.android.sdk.push.MessageHandler;
import com.android.sdk.push.PushMessage;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.router.RouterPath;
import com.gwchina.sdk.base.linker.SchemeJumper;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;


final class MessageProcessor implements MessageHandler {

    private static final String MESSAGE_TYPE_KEY = "message_type";
    private static final String MESSAGE_DATA_KEY = "data";
    private boolean mEnable;

    @Override
    public void onDirectMessageArrived(PushMessage pushMessage) {
        Timber.d("processMessage() called with: pushMessage = [" + pushMessage + "]");
        try {
            JSONObject jsonObject = new JSONObject(pushMessage.getContent());
            int messageType = jsonObject.getInt(MESSAGE_TYPE_KEY);
            processContent(messageType, jsonObject.getString(MESSAGE_DATA_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleOnNotificationMessageClicked(PushMessage pushMessage) {
        Timber.d("handleNotificationClickMessage() called with: pushMessage = [" + pushMessage + "], enable = [" + mEnable + "]");
        if (!mEnable) {
            return;
        }
        if (TextUtils.isEmpty(pushMessage.getExtra()) || "{}".equals(pushMessage.getExtra())) {
            AppContext.appRouter().build(RouterPath.Main.PATH)
                    .withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_HOME)
                    .navigation();
        } else {
            SchemeJumper.handleSchemeJumpUnParsed(AppContext.getContext(), pushMessage.getExtra());
        }
    }

    @Override
    public void onNotificationMessageArrived(PushMessage pushMessage) {
        Timber.d("handNotificationArrivedMessage() called with: pushMessage = [" + pushMessage + "]");
    }

    private void processContent(int messageType, String string) {
        Timber.d("processContent() called with: messageType = [" + messageType + "], string = [" + string + "]");
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

}