package com.gwchina.parent.main.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.base.utils.android.UnitConverter;
import com.gwchina.lssw.parent.home.R;

/**
 * @author hujie
 * Email: hujie1991@126.com
 * Date : 2019-08-15 16:51
 */
public class ChildCardIndicator extends LinearLayout {
    public ChildCardIndicator(Context context) {
        super(context);
        init();
    }

    public ChildCardIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChildCardIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
    }

    private ImageView[] mViews;

    public void addIndicatorView(int count) {
        removeAllViews();
        mViews = new ImageView[count];
        for (int i = 0; i < count; i++) {
            ImageView view = new ImageView(getContext());
            view.setBackgroundResource(R.drawable.mine_child_card_indicator_bg);
            LayoutParams params = new LayoutParams(UnitConverter.dpToPx(13), UnitConverter.dpToPx(2));
            if (i != count - 1) {
                params.rightMargin = UnitConverter.dpToPx(10);
            }
            addView(view, params);
            mViews[i] = view;
            view.setEnabled(i == 0);
        }
    }

    public void setSelect(int index) {
        if (mViews != null && index < mViews.length) {
            for (int i = 0; i < mViews.length; i++) {
                mViews[i].setEnabled(i == index);
            }
        }
    }
}
