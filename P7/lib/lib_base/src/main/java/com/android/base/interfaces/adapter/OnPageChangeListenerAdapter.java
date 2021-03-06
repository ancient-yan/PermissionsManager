package com.android.base.interfaces.adapter;

import android.support.v4.view.ViewPager;

/**
 * @author Ztiany
 *         Email: ztiany3@gmail.com
 *         Date : 2017-12-12 17:35
 */
public interface OnPageChangeListenerAdapter extends ViewPager.OnPageChangeListener {

    @Override
    default void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    default void onPageSelected(int position) {
    }

    @Override
    default void onPageScrollStateChanged(int state) {
    }

}
