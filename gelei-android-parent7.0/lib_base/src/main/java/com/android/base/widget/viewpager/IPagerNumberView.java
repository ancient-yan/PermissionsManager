package com.android.base.widget.viewpager;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-02 12:35
 */
public interface IPagerNumberView {

    void setViewPager(ZViewPager viewPager);

    void setPageSize(int i);

    void setPageScrolled(int position, float positionOffset);

}
