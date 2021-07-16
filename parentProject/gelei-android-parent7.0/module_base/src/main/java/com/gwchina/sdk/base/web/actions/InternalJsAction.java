package com.gwchina.sdk.base.web.actions;

import android.support.annotation.Nullable;

import com.gwchina.sdk.base.web.BaseWebFragment;
import com.gwchina.sdk.base.web.ResultReceiver;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-01-24 14:45
 */
public class InternalJsAction {

    private static final String SAVE_IMAGE_METHOD = "savePhoto";
    private static final String REQUEST_UPDATE_APP = "requestUpdateApp";

    public static void doAction(BaseWebFragment fragment, String method, @Nullable String[] args, @Nullable ResultReceiver resultReceiver) {
        if (SAVE_IMAGE_METHOD.equals(method)) {
            SaveImageAction.action(fragment, args);
        } else if (REQUEST_UPDATE_APP.equals(method)) {
            new UpgradeAppAction(fragment).run();
        } else {
            CommonActionsKt.doAction(method, args, resultReceiver, fragment);
        }

    }

}
