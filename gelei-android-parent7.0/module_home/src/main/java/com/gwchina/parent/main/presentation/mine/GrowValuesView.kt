package com.gwchina.parent.main.presentation.mine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.home.R


/**
 * 我的界面 - 孩子的成长进度条
 *
 *@author Wangwb
 *      Email: 253123123@qq.com
 *      Date : 2019-01-14 17:49
 *      Modify from Ztiany at 2019-03-22
 */
class GrowValuesView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaintDef: Paint
    private val mPaintProgress: Paint
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private val mProgressRect = RectF()
    private val mBgRect = RectF()

    private var mColorDef: Int = 0
    private var mColorProgress: Int = 0
    private var mColorBorder: Int = 0

    private var mBorderWidth: Float = 0.0f
    private var mBlockWidth: Float = 0.0f

    private var mAllGrade: Int = 10
    private var mCurrentProgress = 0F
    private var mCurrentProgressAnim = 0F

    private var mValueAnimator: ValueAnimator? = null

    init {
        mColorDef = colorFromId(R.color.opacity_10_green_main)
        mColorProgress = colorFromId(R.color.green_gradient_bg)
        mColorBorder = colorFromId(R.color.white)

        mBorderWidth = dip(2F)

        mPaintDef = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintDef.isDither = true

        mPaintProgress = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintProgress.isDither = true
        mPaintProgress.color = mColorProgress
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth
        mHeight = measuredHeight

        //每个等级的宽度=总宽度-等级之间的间隙的总和-最左边和最右边的两个半圆形圈的宽度(即高度height)，等到的结果再除以总等级数
        mBlockWidth = (width - mBorderWidth * (mAllGrade - 1)) / mAllGrade

        mPaintProgress.shader = LinearGradient(0f, 0F, mWidth.toFloat(), mHeight.toFloat()
                , intArrayOf(colorFromId(R.color.green_gradient_start), colorFromId(R.color.green_gradient_end)),
                null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val round = mHeight / 2.toFloat()
        //draw bg
        mPaintDef.color = mColorDef
        mBgRect.set(0F, 0F, mWidth.toFloat(), mHeight.toFloat())
        canvas.drawRoundRect(mBgRect, round, round, mPaintDef)

        //draw divider
        var offset = 0F
        mPaintDef.color = mColorBorder
        for (i in 1 until mAllGrade) {
            offset += mBlockWidth
            canvas.drawRect(offset, 0F, offset + mBorderWidth, mHeight.toFloat(), mPaintDef)
            offset += mBorderWidth
        }

        //draw grade
        if (mCurrentProgress > 0) {
            mProgressRect.set(0F, 0F, mCurrentProgressAnim * mWidth, mHeight.toFloat())
            canvas.drawRoundRect(mProgressRect, round, round, mPaintProgress)
        }
    }

    /**
     * 设置成长进度
     *
     * @param curGradeAndProgress 当前等级: 已经完成的等级
     * */
    fun setCurrentProgress(curGradeAndProgress: Float) {
        if (curGradeAndProgress == mCurrentProgress) {
            return
        }

        mCurrentProgress = curGradeAndProgress

        if (mCurrentProgress == 0F) {
            invalidate()
            return
        }

        mValueAnimator?.cancel()
        mValueAnimator = ValueAnimator.ofFloat(0f, mCurrentProgress).apply {
            addUpdateListener {
                mCurrentProgressAnim = it.animatedValue as Float
                invalidate()
            }
            interpolator = DecelerateInterpolator()
            duration = 300
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator?.cancel()
    }

}