package com.gwchina.parent.level.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.Checkable
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.use
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.level_widget_level_view.view.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-30 19:46
 */
class LevelView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Checkable {

    private var normalIcon: Drawable? = null
    private var selectedIcon: Drawable? = null
    private var levelName: String = ""
    private var levelDesc: String = ""
    private var isRecommend: Boolean = false
    private var isTriangleEnable: Boolean = true

    private var checked: Boolean = false

    init {
        inflate(context, R.layout.level_widget_level_view, this)
        initAttribute(context, attrs)
        initView()
    }

    @SuppressLint("Recycle")
    private fun initAttribute(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.LevelView).use {
            normalIcon = it.getDrawable(R.styleable.LevelView_lv_icon_normal_res)
            selectedIcon = it.getDrawable(R.styleable.LevelView_lv_icon_selected_res)
            levelName = it.getString(R.styleable.LevelView_lv_name) ?: ""
            levelDesc = it.getString(R.styleable.LevelView_lv_desc) ?: ""
            checked = it.getBoolean(R.styleable.LevelView_lv_checked, false)
            isRecommend = it.getBoolean(R.styleable.LevelView_lv_is_recommend, false)
        }
    }

    private fun initView() {
        tvLevelDesc.text = levelDesc
        ivLevelSelected.setImageDrawable(selectedIcon)
        tvLevelUnSelect.setCompoundDrawablesWithIntrinsicBounds(null, normalIcon, null, null)
        setRecommend(isRecommend)
        refreshView()
    }

    /**
     * 设置名称
     */
    fun setName(name: String) {
        levelName = name
        tvLevelUnSelect.text = levelName
        tvLevelName.text = buildDescText(levelName, isRecommend)
    }

    /**
     * 设置是否推荐
     */
    fun setRecommend(isRecommend: Boolean) {
        tvLevelUnSelect.text = levelName
        this.isRecommend = isRecommend
        tvLevelName.text = buildDescText(levelName, isRecommend)
        if (isRecommend) {
            if (checked) {
                //7.1.0(2020.1.23) 当前选中的不显示左上角推荐图标
                ivRecommendSelect.gone()
                ivRecommendUnSelect.gone()
            } else {
                ivRecommendUnSelect.visible()
                ivRecommendSelect.gone()
            }

        } else {
            ivRecommendUnSelect.gone()
            ivRecommendSelect.gone()
        }
    }

    /**
     * 设置三角图标是否启用
     */
    fun setTriangleEnable(enable: Boolean) {
        isTriangleEnable = enable
        setTriangleVisibility(checked)
    }

    private fun buildDescText(name: String, isRecommend: Boolean): CharSequence {
        if (isRecommend) {
            return SpanUtils().append(name)
                    .setFontSize(20, true)
                    .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                    .setBold()
                    .append(ResourceUtils.getText(R.string.level_recommend))
                    .setForegroundColor(ContextCompat.getColor(context, R.color.green_main))
                    .setFontSize(12, true)
                    .create()
        } else {
            return SpanUtils().append(name)
                    .setFontSize(20, true)
                    .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                    .setBold()
                    .create()
        }
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun toggle() {
        isChecked = !checked
        refreshView()
    }

    override fun setChecked(checked: Boolean) {
        if (this.checked == checked) {
            return
        }
        this.checked = checked
        refreshView()
    }

    private fun refreshView() {
        if (checked) {
            tvLevelUnSelect.gone()
            tvLevelDesc.visible()
            ivLevelSelected.visible()
            tvLevelName.visible()
            levelIvTriangle.visible()
        } else {
            tvLevelUnSelect.visible()
            tvLevelDesc.gone()
            ivLevelSelected.gone()
            tvLevelName.gone()
            levelIvTriangle.gone()
        }
        if (isRecommend) {
            if (checked) {
                //7.1.0(2020.1.23) 当前选中的不显示左上角推荐图标
                ivRecommendSelect.gone()
                ivRecommendUnSelect.gone()
            } else {
                ivRecommendUnSelect.visible()
                ivRecommendSelect.gone()
            }

        } else {
            ivRecommendUnSelect.gone()
            ivRecommendSelect.gone()
        }
        setTriangleVisibility(checked)
    }

    private fun setTriangleVisibility(isChecked: Boolean) {
        if (isChecked && isTriangleEnable) {
            levelIvTriangle.visible()
        } else {
            levelIvTriangle.invisible()
        }
    }

}