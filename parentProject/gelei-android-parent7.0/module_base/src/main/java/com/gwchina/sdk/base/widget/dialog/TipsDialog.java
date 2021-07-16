package com.gwchina.sdk.base.widget.dialog;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.base.R;


/**
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-05-02 14:37
 */
class TipsDialog extends AppCompatDialog {

    private TextView mMessageTv;

    TipsDialog(Context context, @DrawableRes int drawableRes) {
        super(context);
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.windowAnimations = R.style.Style_Anim_Fade_In;
        setView(drawableRes);
    }

    private void setView(@DrawableRes int drawableRes) {
        setContentView(R.layout.dialog_tips);
        mMessageTv = findViewById(R.id.dialog_tips_tv_title);
        ImageView imageView = findViewById(R.id.dialog_tips_iv);
        assert imageView != null;
        imageView.setImageResource(drawableRes);
    }

    public void setMessage(CharSequence message) {
        if (!TextUtils.isEmpty(message)) {
            mMessageTv.setText(message);
        }
    }

    public void setMessage(@StringRes int messageId) {
        if (messageId != 0) {
            mMessageTv.setText(messageId);
        }
    }

}
