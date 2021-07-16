package com.gwchina.parent.level.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.level.presentation.*
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_MILD
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_MODERATE
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_SEVERE
import kotlinx.android.synthetic.main.level_widget_group_view.view.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-05-05 13:07
 */
class LevelGroupView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var selectedLevelView: View? = null
    private var selectedGuardLevel: GuardLevelVO? = null

    init {
        View.inflate(context, R.layout.level_widget_group_view, this)
    }

    fun setup(guardLevelVOs: List<GuardLevelVO>?, isTriangleEnable: Boolean, onLevelSelected: (GuardLevelVO) -> Unit) {
        val initLevels = guardLevelVOs ?: return

        //onclick
        val onClickListener = OnClickListener {
            onLevelChanged(it)
            //切换内容
            onLevelSelected(selectedGuardLevel!!)
        }

        levelLvMild.setOnClickListener(onClickListener)
        levelLvMild.tag = initLevels.mildGuardLevel()
        levelLvMild.setName(initLevels.mildGuardLevel().name)

        levelLvModerate.setOnClickListener(onClickListener)
        levelLvModerate.tag = initLevels.moderateGuardLevel()
        levelLvModerate.setName(initLevels.moderateGuardLevel().name)

        levelLvSevere.setOnClickListener(onClickListener)
        levelLvSevere.tag = initLevels.severeGuardLevel()
        levelLvSevere.setName(initLevels.severeGuardLevel().name)
        setTriangleEnable(isTriangleEnable)
    }

    private fun setTriangleEnable(enable: Boolean) {
        levelLvMild.setTriangleEnable(enable)
        levelLvModerate.setTriangleEnable(enable)
        levelLvSevere.setTriangleEnable(enable)
    }

    private fun onLevelChanged(it: View) {
        if (selectedLevelView == it) {
            return
        }

        //清理之前的状态
        if (selectedLevelView != null) {
            (selectedLevelView as LevelView).isChecked = false
        }

        //设置当前选择的状态
        selectedGuardLevel = it.tag as GuardLevelVO

        selectedLevelView = it
        (selectedLevelView as LevelView).isChecked = true
    }

    internal fun setDefaultSelectItem(level: Int) {
        when (level) {
            GUARD_LEVEL_MILD -> {
                levelLvMild.performClick()
            }
            GUARD_LEVEL_MODERATE -> {
                levelLvModerate.performClick()
            }
            GUARD_LEVEL_SEVERE -> {
                levelLvSevere.performClick()
            }
        }
    }

    internal fun setRecommendByAge(childAge: Int) {
        when {
            childAge.isAgeInMild() -> {
                if (selectedGuardLevel == null) {
                    levelLvMild.performClick()
                }
                levelLvMild.setRecommend(true)
                levelLvSevere.setRecommend(false)
                levelLvModerate.setRecommend(false)
            }
            childAge.isAgeInModerate() -> {
                if (selectedGuardLevel == null) {
                    levelLvModerate.performClick()
                }
                levelLvModerate.setRecommend(true)
                levelLvMild.setRecommend(false)
                levelLvSevere.setRecommend(false)
            }
            childAge.isAgeInSevere() -> {
                if (selectedGuardLevel == null) {
                    levelLvSevere.performClick()
                }
                levelLvSevere.setRecommend(true)
                levelLvMild.setRecommend(false)
                levelLvModerate.setRecommend(false)
            }
        }
    }

    internal fun syncSelectedItem(level: Int) {
        when (level) {
            GUARD_LEVEL_MILD -> {
                onLevelChanged(levelLvMild)
            }
            GUARD_LEVEL_MODERATE -> {
                onLevelChanged(levelLvModerate)
            }
            GUARD_LEVEL_SEVERE -> {
                onLevelChanged(levelLvSevere)
            }
        }
    }

}