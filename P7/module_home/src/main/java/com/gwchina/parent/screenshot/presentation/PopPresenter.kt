package com.gwchina.parent.screenshot.presentation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.PopupWindow
import android.widget.TextView
import com.android.base.kotlin.dip
import com.android.base.kotlin.invisible
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.home.R
import kotlinx.android.synthetic.main.home_remote_screentshot_layout.*
import kotlinx.android.synthetic.main.home_screen_pop.view.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-17 16:09
 */
class PopPresenter(val host: RemoteScreenshotFragment) {

    private var popupWindow: PopupWindow? = null
    private var mToday: TextView? = null
    private var mYesterday: TextView? = null
    private var mTheDayBeforeYesterday: TextView? = null

    var onItemClickListener: ((Int) -> Unit)? = null

    @SuppressLint("InflateParams")
    fun showPop() {
        if (popupWindow == null) {
            val view = host.layoutInflater.inflate(R.layout.home_screen_pop, null)
            popupWindow = PopupWindow(view, dip(110), ViewGroup.LayoutParams.WRAP_CONTENT)
            popupWindow!!.isOutsideTouchable = true
            popupWindow!!.isFocusable = true
            popupWindow!!.isTouchable = true
            mToday = view.tvToday
            mYesterday = view.tvYesterday
            mTheDayBeforeYesterday = view.tvTheDayBeforeYesterday
            mToday?.setOnClickListener {
                dateClick(0)
            }
            mYesterday?.setOnClickListener { dateClick(1) }
            mTheDayBeforeYesterday?.setOnClickListener { dateClick(2) }
            popupWindow!!.setOnDismissListener {
                bgAlpha(1f)
            }
        }
        popupWindow?.showAsDropDown(host.tvDate)
        bgAlpha(0.5f)
    }

    private fun bgAlpha(alpha: Float) {
        val lp = host.activity?.window?.attributes
        lp?.alpha = alpha
        host.activity?.window?.attributes = lp
    }

    /**
     * 如果在非今天点击截屏，截屏成功后切换到今天
     */
    fun setToday(mCurrentDay: Int) {
        if (mCurrentDay == 0) return
        dateClick(0)
    }

    private fun dateClick(position: Int) {
        when (position) {
            0 -> {
                mToday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.green_level1))
                mYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                mTheDayBeforeYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                host.tvDate.text = host.getString(R.string.today)
            }
            1 -> {
                mToday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                mYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.green_level1))
                mTheDayBeforeYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                host.tvDate.text = host.getString(R.string.yesterday)
            }
            2 -> {
                mToday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                mYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2))
                mTheDayBeforeYesterday?.setTextColor(ContextCompat.getColor(host.requireContext(), R.color.green_level1))
                host.tvDate.text = host.getString(R.string.the_day_before_yesterday)
            }
        }
        onItemClickListener?.invoke(position)
        popupWindow?.dismiss()
    }

    /**
     * view:
     * width =110dp
     * height =180dp
     * itemView:
     * width=height = screenWidth-leftPadding-rightPadding-2*margin
     */
    fun doAnimator(view: View, startX: Float, startY: Float, endX: Float, endY: Float, onAnimationStart: (animation: Animator?) -> Unit) {
        val scaleX = ((ScreenUtils.getScreenWidth() - dip(15 * 2 + 8 * 2)) / 3.0f) / dip(110)
        val scaleY = ((ScreenUtils.getScreenWidth() - dip(15 * 2 + 8 * 2)) / 3.0f) / dip(180)
        val xAnimator = ObjectAnimator.ofFloat(view, "ScaleX", 1.0f, scaleX)
        val yAnimator = ObjectAnimator.ofFloat(view, "ScaleY", 1.0f, scaleY)
        val y = endY - dip(180) * (1 - scaleY) / 2.0f
        val translationX = ObjectAnimator.ofFloat(view, "TranslationX", 0f, endX - startX)
        val translationY = ObjectAnimator.ofFloat(view, "TranslationY", 0f, y - startY)
        val animatorSet = AnimatorSet()
        animatorSet.duration = 600
        animatorSet.play(xAnimator).with(yAnimator).with(translationX).with(translationY)
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
                animatorReset(view)
            }

            override fun onAnimationStart(animation: Animator?) {
                onAnimationStart.invoke(animation)
            }

            override fun onAnimationEnd(animation: Animator?) {
                animatorReset(view)
            }
        })
        animatorSet.start()
    }

    private fun animatorReset(view: View) {
        //reset
        view.invisible()
        view.translationX = 0f
        view.translationY = 0f
        view.scaleX = 1.0f
        view.scaleY = 1.0f
    }

    /**
     * 平移动画
     * view:
     * width =110dp
     * height =180dp
     * itemView:
     * width=height = screenWidth-leftPadding-rightPadding-2*margin
     */
    fun doTranslationXAnimator(view: View, startX: Float) {
        val translationX = ObjectAnimator.ofFloat(view, "TranslationX", 0f, -startX - dip(110))
        translationX.duration = 300
        translationX.interpolator = AccelerateInterpolator()
        translationX.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                //reset
                view.invisible()
                view.translationX = 0f
            }
        }
        )
        translationX.start()
    }
}