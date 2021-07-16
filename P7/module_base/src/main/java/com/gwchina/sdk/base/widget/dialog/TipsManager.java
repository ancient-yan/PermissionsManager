package com.gwchina.sdk.base.widget.dialog;

import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;

import com.android.base.utils.android.ResourceUtils;
import com.app.base.R;
import com.blankj.utilcode.util.ToastUtils;


public class TipsManager {

    public static void showMessage(@StringRes int msgId) {
        showMessage(ResourceUtils.getText(msgId));
    }

    public static void showMessage(CharSequence message) {
        ToastUtils.setBgResource(R.drawable.shape_common_toast);
        ToastUtils.setMsgTextSize(14);
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.showShort(message);
    }

    public static void showMessage(View anchor, CharSequence message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showMessage(View anchor, CharSequence message, CharSequence actionText, View.OnClickListener onClickListener) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT)
                .setAction(actionText, onClickListener)
                .show();
    }

}