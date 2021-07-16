package com.gwchina.sdk.base.widget.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CheckableImageButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.base.interfaces.adapter.TextWatcherAdapter;
import com.app.base.R;

import java.lang.reflect.Field;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-14 20:17
 */
public class ClearableTextInputLayout extends TextInputLayout {

    private View mPasswordToggleView;
    private boolean mEnableClearView = true;
    private FrameLayout mInputFrame;
    private ImageButton mClearView;
    private EditText mEditText;
    private TextWatcherAdapter mWatcher;
    private View.OnClickListener mClearClickListener;

    private int mIncrementMarginRight;
    private int mIncrementMarginBottom;

    public ClearableTextInputLayout(Context context) {
        this(context, null);
    }

    public ClearableTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearableTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getInputFrame();

        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.ClearableTextInputLayout);

        mEnableClearView = typedArray.getBoolean(R.styleable.ClearableTextInputLayout_ctil_enable_clear_drawable, true);
        Drawable clearDrawable = typedArray.getDrawable(R.styleable.ClearableTextInputLayout_ctil_clear_drawable);

        if (mEnableClearView) {
            if (clearDrawable == null) {
                clearDrawable = ContextCompat.getDrawable(context, R.drawable.icon_clear);
            }
        }

        typedArray.recycle();

        if (mEnableClearView && mInputFrame != null) {
            mClearView = (CheckableImageButton) LayoutInflater.from(getContext()).inflate(R.layout.widget_text_input_password_icon, mInputFrame, false);
            mClearView.setImageDrawable(clearDrawable);
            mInputFrame.addView(mClearView);
            mClearView.setOnClickListener(createClearListener());
        }
    }

    private void getInputFrame() {
        try {
            Field inputFrame = TextInputLayout.class.getDeclaredField("inputFrame");
            inputFrame.setAccessible(true);
            Object obj = inputFrame.get(this);
            if (obj instanceof FrameLayout) {
                this.mInputFrame = (FrameLayout) obj;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof EditText) {
            if (mClearView != null) {
                mClearView.bringToFront();
            }

            if (mEditText != child) {
                mEditText = (EditText) child;
                if (TextUtils.isEmpty(mEditText.getText().toString())) {
                    mClearView.setVisibility(INVISIBLE);
                } else {
                    mClearView.setVisibility(VISIBLE);
                }
                mEditText.addTextChangedListener(createWatcher());
            }
        }
    }

    private TextWatcher createWatcher() {
        if (mWatcher == null) {
            mWatcher = new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s)) {
                        mClearView.setVisibility(INVISIBLE);
                        setError(null);
                    } else {
                        mClearView.setVisibility(VISIBLE);
                    }
                }
            };
        }
        return mWatcher;
    }

    private OnClickListener createClearListener() {
        if (mClearClickListener == null) {
            mClearClickListener = v -> {
                if (mEditText != null) {
                    mEditText.setText("");
                }
            };
        }
        return mClearClickListener;
    }

    @Override
    public void setPasswordVisibilityToggleEnabled(boolean enabled) {
        super.setPasswordVisibilityToggleEnabled(enabled);
        fixClearViewPosition();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        fixClearViewPosition();
    }

    private void fixClearViewPosition() {
        getPasswordToggleView();
        if (mPasswordToggleView != null && mPasswordToggleView.getVisibility() == VISIBLE) {
            setMargins(mClearView, mPasswordToggleView.getMeasuredWidth() + mIncrementMarginRight);
        } else {
            setMargins(mClearView, mIncrementMarginRight);
        }
    }

    private void getPasswordToggleView() {
        if (mPasswordToggleView == null) {
            Field mPasswordToggleView;
            try {
                mPasswordToggleView = TextInputLayout.class.getDeclaredField("passwordToggleView");
                mPasswordToggleView.setAccessible(true);
                this.mPasswordToggleView = (View) mPasswordToggleView.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setMargins(View view, int margin) {
        ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
        layoutParams.rightMargin = margin;
        layoutParams.bottomMargin = mIncrementMarginBottom;
        view.setLayoutParams(layoutParams);
    }

    public void setEnableClearView(boolean enableClearView) {
        mEnableClearView = enableClearView;
        if (mClearView != null) {
            mClearView.setVisibility(enableClearView ? VISIBLE : INVISIBLE);
        }
    }

    public void setClearViewPosition(int marginBottom, int marginRight) {
        mIncrementMarginRight = marginRight;
        mIncrementMarginBottom = marginBottom;
        fixClearViewPosition();
    }

}
