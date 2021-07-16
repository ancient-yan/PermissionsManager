package com.gwchina.parent.binding.presentation

import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.text.InputType
import android.view.View
import com.android.base.interfaces.adapter.TextWatcherAdapter
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.visible
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.common.NewChildProcessor
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.composeDate
import com.gwchina.sdk.base.utils.getRecommendedGradeByBirthday
import com.gwchina.sdk.base.widget.dialog.showListDialog
import com.gwchina.sdk.base.widget.picker.maxChildDate
import com.gwchina.sdk.base.widget.picker.minChildDate
import com.gwchina.sdk.base.widget.picker.selectDate
import kotlinx.android.synthetic.main.binding_fragment_child_info.*
import java.util.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-23 11:42
 *      b绑定孩子使用的
 */
class ChildInfoCollectFragment : InjectorBaseFragment() {

    @Inject
    internal lateinit var newChildProcessor: NewChildProcessor

    private val childInfo = ChildInfo()

    private var gradeModifiedByUser = false

    private val gradeArray by lazy {
        resources.getTextArray(R.array.child_grade_array)
    }

    override fun provideLayout() = R.layout.binding_fragment_child_info

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)

        //调整ui位置
        view.post {
            adjustPosition()
        }
        //检查按钮状态
        checkButtonEnable()
        //名字
        setupNameListener()
        //性别
        setupSexSelector()
        //生日
        setupBirthdaySelector()
        //年级
        setupGradeSelector()
        //关系
        setupRelationship()
        //下一步
        btnBindingNext.setOnClickListener {
            newChildProcessor.newNewChildInfoCollected(childInfo)
            StatisticalManager.onEvent(UMEvent.ClickEvent.PAGE_BIND_BTN_NEXT)
        }
    }

    private fun setupNameListener() {
        tilBindingChildName.editText.addTextChangedListener(object : TextWatcherAdapter {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                childInfo.name = s.toString()
                checkButtonEnable()
            }
        })
    }

    private fun adjustPosition() {
        val text = tvBindingBirthdayTitle.text.toString()
        val width = tvBindingBirthdayTitle.paint.measureText(text) + dip(45)/*30 edge margin + 15 ui margin*/
        val percent = width / ScreenUtils.getScreenWidth().toFloat()
        val set = ConstraintSet()
        set.clone(clBindingChildInfoContent)
        set.setGuidelinePercent(R.id.guideBindingMiddle, percent)
        set.applyTo(clBindingChildInfoContent)
    }

    private fun setupRelationship() {
        vBindingRelativeFather.tag = RELATIONSHIP_FATHER
        vBindingRelativeMother.tag = RELATIONSHIP_MOTHER
        ivBindingRelativeOther.tag = RELATIONSHIP_OTHER

        composeToSingleSelectable(vBindingRelativeFather, vBindingRelativeMother, ivBindingRelativeOther) {
            childInfo.relationship = it.tag as Int
            tvBindingRelativeFather.setTextColor(getSelectionColor(childInfo.relationship, RELATIONSHIP_FATHER))
            collectTvRelativeMother.setTextColor(getSelectionColor(childInfo.relationship, RELATIONSHIP_MOTHER))
            tvBindingRelativeOther.setTextColor(getSelectionColor(childInfo.relationship, RELATIONSHIP_OTHER))
            adjustToBottomPosition(it, ivBindingRelativeSel)
            checkButtonEnable()
        }
    }

    private fun setupGradeSelector() {
        etBindingGrade.inputType = InputType.TYPE_NULL

        etBindingGrade.setOnClickListener {

            val selected = childInfo.grade

            showListDialog {
                items = gradeArray
                selectedPosition = selected

                positiveListener = { which: Int, item: CharSequence ->
                    etBindingGrade.setText(item)
                    //值的索引即传参，正好是对应的
                    childInfo.grade = which
                    checkButtonEnable()
                    gradeModifiedByUser = true
                }
            }
        }
    }

    private fun setupBirthdaySelector() {
        etBindingBirthday.inputType = InputType.TYPE_NULL

        val selectedDate = maxChildDate()

        etBindingBirthday.setOnClickListener {

            selectDate {
                initDate = selectedDate
                minDate = minChildDate()
                maxDate = maxChildDate()

                onDateSelectedListener = { year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    //remember selected
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear - 1)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    //assignment
                    childInfo.birthday = composeDate(year, monthOfYear, dayOfMonth)
                    etBindingBirthday.setText(composeDate(year, monthOfYear, dayOfMonth, separator = "-"))
                    checkButtonEnable()

                    //help select grade
                    if (!gradeModifiedByUser) {
                        val gradle = getRecommendedGradeByBirthday(year = year, month = monthOfYear)
                        childInfo.grade = gradle
                        if (gradeArray.size > gradle && gradle >= 0) {
                            etBindingGrade.setText(gradeArray[gradle])
                        }
                    }
                }
            }//select data end

        }
    }

    private fun setupSexSelector() {
        composeToSingleSelectable(ivBindingSexMale, ivBindingSexFemale) {
            if (it == ivBindingSexMale) {
                childInfo.sex = SEX_MALE
            } else {
                childInfo.sex = SEX_FEMALE
            }
            tvBindingSexMale.setTextColor(getSelectionColor(childInfo.sex, SEX_MALE))
            tvBindingSexFemale.setTextColor(getSelectionColor(childInfo.sex, SEX_FEMALE))
            adjustToBottomPosition(it, ivBindingSexSel)
            checkButtonEnable()
        }
    }

    private fun getSelectionColor(value: Int, flag: Int): Int {
        return if (value == flag) {
            colorFromId(R.color.gray_level1)
        } else {
            colorFromId(R.color.gray_level3)
        }
    }

    private fun checkButtonEnable() {
        btnBindingNext.isEnabled = childInfo.completedInfo()
    }

    private fun adjustToBottomPosition(base: View, moving: View) {
        moving.visible()
        val set = ConstraintSet()
        set.clone(clBindingChildInfoContent)
        set.connect(moving.id, ConstraintSet.BOTTOM, base.id, ConstraintSet.BOTTOM)
        set.connect(moving.id, ConstraintSet.END, base.id, ConstraintSet.END)
        set.applyTo(clBindingChildInfoContent)
    }
}