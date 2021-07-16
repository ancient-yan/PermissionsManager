package com.gwchina.parent.daily.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gwchina.lssw.parent.home.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by julong on 2018/11/19.
 */

public class ReplyView extends LinearLayout {
    //评论
    public static final int ADD_COMMENT = 1;

    //回复评论
    public static final int REPLY_COMMENT = 2;

    //回复回复
    public static final int REPLY_REPLY = 3;

    //回复内容缓存
    private Map<String, String> replyContentCache = new HashMap<>();

    private String mKey;

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getCacheByKey(String key) {
        return replyContentCache.get(key);
    }


    //回复成功清除该条缓存
    public void clearCacheBykey(String key) {
        replyContentCache.remove(key);
    }

    @IntDef({ADD_COMMENT, REPLY_COMMENT, REPLY_REPLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RePlyType {

    }

    private int type;
    //日记id
    private String recordId;
    //评论id
    private String commentId;
    private String byReplierName;

    private EditText editText;
    private TextView textView;

    public ReplyView(Context context) {
        this(context, null);
    }

    public ReplyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReplyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.daily_comment_bg));
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.daily_comment_layout, this, true);
        editText = view.findViewById(R.id.contentInputEt);
        textView = view.findViewById(R.id.sendTv);
        setListener();
    }

    private void setListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().toString().trim().length() > 0) {
                    textView.setEnabled(true);
                    textView.setTextColor(getContext().getResources().getColor(R.color.green_level1));
                } else {
                    textView.setEnabled(false);
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_level3));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                replyContentCache.put(mKey, editText.getText().toString().trim());
            }
        });
        textView.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onSend(editText.getText().toString().trim(), recordId, type, commentId, byReplierName);
            }
        });
    }

    public void observeSoftKeyboard(Activity activity) {
        //监控软键盘高度
        View decorView = activity.getWindow().getDecorView();
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = () -> {
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            //计算出可见屏幕的高度
            int displayHeight = rect.bottom - rect.top;
            //获得屏幕整体的高度
            int height = decorView.getHeight();
            boolean visible = (double) displayHeight / height < 0.8;
            if (visible) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.GONE);
            }
        };

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }


    public void setEditTextHint(String hint, String recordId, @RePlyType int replyType, String commentId, String byReplierName) {
        this.type = replyType;
        this.recordId = recordId;
        this.commentId = commentId;
        this.byReplierName = byReplierName;
        if (editText != null) {
            editText.setHint(hint);
        }
    }

    public void setEditText(String content) {
        if (editText != null) {
            editText.setText(content);
        }
    }

    public void resetEditText() {
        if (editText != null) {
            editText.setText("");
        }
    }

    public void showSoftInput(View commentView, View inputView, RecyclerView recyclerView) {
        int height = commentView.getHeight();
        int rvInputY = getYOnScreen(commentView);
        this.setVisibility(VISIBLE);
        showKeyboard(editText);
        commentView.postDelayed(() -> {
            int end = getYOnScreen(inputView);
            recyclerView.smoothScrollBy(0, height - (end - rvInputY));
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        }, 300);
    }

    private OnSendClickListener onClickListener;

    public void setOnSendClickListener(OnSendClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnSendClickListener {
        void onSend(String content, String recordId, @RePlyType int replyType, String commentId, String byReplyerName);
    }

    private int getYOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    private void showKeyboard(EditText editText) {
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) editText
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

}
