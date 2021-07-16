package com.gwchina.parent.launcher

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.AppBaseActivity
import com.gwchina.sdk.base.router.RouterPath
import kotlinx.android.synthetic.main.launcher_guide_activity.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-09-09 14:02
 */
class GuideActivity : AppBaseActivity() {

    private var lastPosition: Int = 0

    private val fragmentList = mutableListOf<Fragment>()

    override fun layout() = R.layout.launcher_guide_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUi()
        super.onCreate(savedInstanceState)
        (0..3).forEach {
            fragmentList.add(GuideFragment.createGuideFragment(it))
        }
        vpLauncherGuide.adapter = GuidePagerAdapter()
        vpLauncherGuide.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(p0: Int) {
                tvLauncherSkip.visibility = if (p0 != fragmentList.size - 1) View.VISIBLE else View.GONE
                resetBg()
                (bottomIndicator.getChildAt(p0) as ImageView).setImageResource(R.drawable.launcher_guide_selected)
                lastPosition = p0
            }

        })

        tvLauncherSkip.setOnClickListener {
            AppContext.appRouter().build(RouterPath.Main.PATH).navigation()
            finish()
        }
    }

    private fun resetBg() {
        (bottomIndicator.getChildAt(lastPosition) as ImageView).setImageResource(R.drawable.launcher_guide_normal)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    private fun hideSystemUi() {
        try {
            SystemBarCompat.setTranslucentSystemUi(this, true, true)
            val systemUiVisibility = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private inner class GuidePagerAdapter : FragmentPagerAdapter(supportFragmentManager) {

        override fun getItem(p0: Int): Fragment {
            return fragmentList[p0]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

    }
}