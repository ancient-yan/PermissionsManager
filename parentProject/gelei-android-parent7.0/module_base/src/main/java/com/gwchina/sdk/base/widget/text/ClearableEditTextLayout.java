package com.gwchina.sdk.base.widget.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.android.base.utils.android.SoftKeyboardUtils;
import com.android.base.utils.android.UnitConverter;
import com.app.base.R;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-23 12:10
 */
public class ClearableEditTextLayout extends FrameLayout {

    private EditText mEditText;
    private View mClearView;

    private static final int EDIT_PADDING_END = 50;
    private int mOriginPaddingRight;
    private int mOriginPaddingLeft;
    private int mCompatBtnPaddingEnd;

    public ClearableEditTextLayout(Context context) {
        this(context, null);
    }

    public ClearableEditTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ClearableEditTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.widget_text_input_password_icon_padding, this);
        mCompatBtnPaddingEnd = UnitConverter.dpToPx(EDIT_PADDING_END);
        mClearView = findViewById(R.id.widgetBtnClear);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // EditText's underline
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
            flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
            mEditText = (EditText) child;
            mOriginPaddingRight = mEditText.getPaddingRight();
            mOriginPaddingLeft = mEditText.getPaddingLeft();


            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean empty = TextUtils.isEmpty(s);

                    boolean textCenter = isTextCenter();

                    mClearView.setVisibility(empty ? INVISIBLE : VISIBLE);
                    mEditText.setPadding(
                            empty ? mOriginPaddingLeft : textCenter ? mCompatBtnPaddingEnd : mOriginPaddingLeft,
                            mEditText.getPaddingTop(),
                            empty ? mOriginPaddingRight : mCompatBtnPaddingEnd,
                            mEditText.getPaddingBottom()
                    );
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            mClearView.setOnClickListener(v ->
                    {
                        mEditText.setText("");
                        //获取焦点弹出软键盘
                        mEditText.setFocusable(true);
                        mEditText.setFocusableInTouchMode(true);
                        mEditText.requestFocus();
                        InputMethodManager inputManager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(mEditText, 0);
                    }
            );

            super.addView(child, index, flp);

            mClearView.bringToFront();
        } else {
            super.addView(child, index, params);
        }
    }

    private boolean isTextCenter() {
        int gravity = mEditText.getGravity();
        final int layoutDirection = getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
        return (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL;
    }

    public EditText getEditText() {
        return mEditText;
    }

}
