package com.gwchina.sdk.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import com.android.base.app.fragment.Fragments;
import com.android.base.utils.android.ViewUtils;
import com.app.base.R;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-12 16:43
 */
public class GwTitleLayout extends LinearLayout {

    private static final String TAG = GwTitleLayout.class.getSimpleName();

    private String mTitle;
    private Toolbar mToolbar;
    private int mMenuResId;
    private Drawable mNavigationIcon;
    private boolean mShowCuttingLime;
    private View.OnClickListener onNavigationOnClickListener;
    private static final int INVALIDATE_ID = -1;

    public GwTitleLayout(@NonNull Context context) {
        this(context, null);
    }

    public GwTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GwTitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //get resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GwTitleLayout);
        mTitle = typedArray.getString(R.styleable.GwTitleLayout_gtl_title);
        mMenuResId = typedArray.getResourceId(R.styleable.GwTitleLayout_gtl_menu_id, INVALIDATE_ID);
        mShowCuttingLime = typedArray.getBoolean(R.styleable.GwTitleLayout_gtl_show_cutting_line, false);
        mNavigationIcon = typedArray.getDrawable(R.styleable.GwTitleLayout_gtl_navigation_icon);
        typedArray.recycle();

        //setup views
        setOrientation(VERTICAL);
        inflate(context, R.layout.widget_gw_title_layout, this);
        setupViews();
    }

    private void setupViews() {
        mToolbar = findViewById(R.id.common_toolbar);
        View cuttingLineView = findViewById(R.id.widgetGwTitleCuttingLine);
        setupToolbar();
        cuttingLineView.setVisibility(mShowCuttingLime ? View.VISIBLE : View.GONE);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setTitle(String title) {
        mTitle = title;
        mToolbar.setTitle(mTitle);
    }

    public void setTitle(int titleId) {
        mTitle = getContext().getString(titleId);
        mToolbar.setTitle(mTitle);
    }

    private void setupToolbar() {
        //nav
        mToolbar.setContentInsetStartWithNavigation(0);
        mToolbar.setNavigationOnClickListener(this::onNavigationOnClick);
        if (getBackground() != null) {
            mToolbar.setBackground(getBackground());
        }
        //title
        mToolbar.setTitle(mTitle);
        //icon
        setNavigationIcon();
        //menu
        if (mMenuResId != INVALIDATE_ID) {
            mToolbar.inflateMenu(mMenuResId);
        }
    }

    private void setNavigationIcon() {
        if (mNavigationIcon != null) {
            mToolbar.setNavigationIcon(mNavigationIcon);
        } else {
            mToolbar.setNavigationIcon(R.drawable.icon_back);
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

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        if (mToolbar != null) {
            mToolbar.setBackground(background);
        }
    }

}