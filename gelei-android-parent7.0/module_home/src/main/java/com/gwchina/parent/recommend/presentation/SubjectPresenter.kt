package com.gwchina.parent.recommend.presentation

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.utils.android.UnitConverter
import com.android.base.utils.android.ViewUtils
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.data.SubjectInfo
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.reco_fragment_main.*
import kotlinx.android.synthetic.main.recommend_subject_item.view.*

/**
 * 专题卡片控制器
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-14 11:46
 */
class SubjectPresenter(private val host: RecommendFragment) {

    private val viewPager = host.vpMainAppSubject
    private val itemCache = mutableListOf<SubjectCardView>()
    private lateinit var subjectAdapter: SubjectAdapter

    init {
        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = 10
        subjectAdapter = SubjectAdapter(itemCache)
        viewPager?.adapter = subjectAdapter
    }

    fun showSubjects(subjects: List<SubjectInfo>?) {
        if (subjects.isNullOrEmpty()) {
            host.tvSubjectTitle.gone()
            host.vpMainAppSubject.gone()
            return
        }
        setSubjectsData(subjects)
        host.tvSubjectTitle.visible()
        host.vpMainAppSubject.visible()
    }

    private val mItemOnClickListener = View.OnClickListener {
        val subjectInfo = it.tag as SubjectInfo
        host.navigator.showSubjectDetail(subjectInfo.rec_subject_id)
        StatisticalManager.onEvent(UMEvent.ClickEvent.COMMEND_BTN_TYPE)
    }

    private fun setSubjectsData(subjects: List<SubjectInfo>) {
        subjects.forEachIndexed { index, subjectInfo ->
            if (itemCache.size <= index) {
                itemCache.add(SubjectCardView(host.requireContext(), clickListener = mItemOnClickListener).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            }
            itemCache[index].setData(subjectInfo)
        }
        ViewUtils.measureWithScreenSize(itemCache[0])
        viewPager.layoutParams = viewPager.layoutParams.apply {
            height = itemCache[0].measuredHeight
        }

//        if (subjects.size <= 1) {
//            setViewPagerMargin(UnitConverter.dpToPx(14))
//            viewPager.pageMargin = 0
//        } else {
            setViewPagerMargin(UnitConverter.dpToPx(20))
            viewPager.pageMargin = UnitConverter.dpToPx(10)
//        }
        subjectAdapter.list = subjects
    }


    private fun setViewPagerMargin(margin: Int) {
        val layoutParams = viewPager.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = margin
            layoutParams.rightMargin = margin
            viewPager.layoutParams = layoutParams
        }
    }

    private class SubjectCardView @JvmOverloads constructor(
            context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, clickListener: OnClickListener
    ) : FrameLayout(context, attrs, defStyleAttr) {

        init {
            val inflate = inflate(context, R.layout.recommend_subject_item, null)
            addView(inflate,  ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenWidth()/2 - UnitConverter.dpToPx(30)))
            setOnClickListener(clickListener)
        }

        fun setData(subject: SubjectInfo) {
            tag = subject
            ImageLoaderFactory.getImageLoader().display(ivSubjectCover, subject.subject_banner_url)
        }
    }


    private class SubjectAdapter(private val itemCache: List<SubjectCardView>) : PagerAdapter() {

        var list: List<SubjectInfo> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return itemCache[position].apply {
                container.addView(this)
            }
        }

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }

        override fun getCount() = list.size

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            container.removeView(item as View)
        }

        override fun getItemPosition(`object`: Any) = POSITION_NONE
    }
}
