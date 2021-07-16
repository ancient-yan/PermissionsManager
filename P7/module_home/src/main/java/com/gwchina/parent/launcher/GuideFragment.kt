package com.gwchina.parent.launcher

import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.BaseFragment
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import kotlinx.android.synthetic.main.launcher_guide_item_layout.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-09-09 15:54
 */
class GuideFragment : BaseFragment() {

    private var currentPage: Int = 0

    companion object {
        val titles = arrayOf(R.string.launcher_guide_title0, R.string.launcher_guide_title1, R.string.launcher_guide_title2, R.string.launcher_guide_title3)
        val imageResource = arrayOf(R.drawable.launcher_img_guide1, R.drawable.launcher_img_guide2, R.drawable.launcher_img_guide3, R.drawable.launcher_img_guide4)
        val desc = arrayOf(R.string.launcher_guide_desc0, R.string.launcher_guide_desc1, R.string.launcher_guide_desc2)

        fun createGuideFragment(currentPage: Int): GuideFragment {
            val fragment = GuideFragment()
            val bundle = Bundle()
            bundle.putInt("currentPage", currentPage)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.launcher_guide_item_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        currentPage = arguments?.getInt("currentPage")!!
        imageView.setImageResource(imageResource[currentPage])
        titleTv.text = getString(titles[currentPage])
        if (currentPage < 3) {
            descTv.visible()
            openTv.gone()
            descTv.text = getString(desc[currentPage])
        } else {
            descTv.invisible()
            openTv.visible()
        }
        imageView.setImageResource(imageResource[currentPage])
        openTv.setOnClickListener {
            AppContext.appRouter().build(RouterPath.Main.PATH).navigation()
            activity?.finish()
        }
    }
}