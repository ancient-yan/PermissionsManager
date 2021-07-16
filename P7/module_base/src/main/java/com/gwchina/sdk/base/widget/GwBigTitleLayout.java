package com.gwchina.sdk.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.base.app.fragment.Fragments;
import com.android.base.utils.android.ViewUtils;
import com.app.base.R;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-27 10:57
 */
public class GwBigTitleLayout extends LinearLayout {

    private static final String TAG = GwBigTitleLayout.class.getSimpleName();
    private static final int INVALIDATE_ID = -1;

    private String mTitle;
    private String mSubTitle;
    private TextView mTitleTv;
    private TextView mSubTitleTv;
    private Toolbar mToolbar;
    private int mMenuResId;
    private Drawable mNavigationIcon;

    private View.OnClickListener onNavigationOnClickListener;

    public GwBigTitleLayout(Context context) {
        this(context, null);
    }

    public GwBigTitleLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GwBigTitleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GwBigTitleLayout);
        mTitle = typedArray.getString(R.styleable.GwBigTitleLayout_gbtl_title);
        mSubTitle = typedArray.getString(R.styleable.GwBigTitleLayout_gbtl_sub_title);
        mMenuResId = typedArray.getResourceId(R.styleable.GwBigTitleLayout_gbtl_menu_id, INVALIDATE_ID);
        mNavigationIcon = typedArray.getDrawable(R.styleable.GwBigTitleLayout_gbtl_navigation_icon);
        typedArray.recycle();

        setOrientation(VERTICAL);
        inflate(context, R.layout.widget_gw_big_title_layout, this);
        setupViews();
    }

    private void setupViews() {
        mTitleTv = findViewById(R.id.gwTitleTv);
        mSubTitleTv = findViewById(R.id.gwSubTitleTv);
        mToolbar = findViewById(R.id.common_toolbar);
        setupToolbar();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setTitle(String title) {
        mTitle = title;
        mTitleTv.setText(mTitle);
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        mSubTitleTv.setText(subTitle);
    }

    private void setupToolbar() {
        //nav
        mToolbar.setContentInsetStartWithNavigation(0);
        mToolbar.setNavigationOnClickListener(this::onNavigationOnClick);
        if (mNavigationIcon != null) {
            mToolbar.setNavigationIcon(mNavigationIcon);
        } else {
            mToolbar.setNavigationIcon(R.drawable.icon_back);
        }
        //title
        mTitleTv.setText(mTitle);
        mSubTitleTv.setText(mSubTitle);
        //menu
        if (mMenuResId != INVALIDATE_ID) {
            mToolbar.inflateMenu(mMenuResId);
        }
    }

    public Menu getMenu() {
        return mToolbar.getMenu();
    }

    public void setOnNavigationOnClickListener(View.OnClickListener onNavigationOnClickListener) {
        this.onNavigationOnClickListener = onNavigationOnClickListener;
    }

    private void onNavigationOnClick(View v) {
        if (onNavigationOnClickListener != null) {
            onNavigationOnClickListener.onClick(v);
            return;
        }
        FragmentActivity realContext = ViewUtils.getRealContext(this);
        if (realContext != null) {
            Fragments.exitFragment(realContext, false);
        } else {
            Log.w(TAG, "perform onNavigationOnClick --> fragmentBack, but real context can not be found");
        }
    }

}