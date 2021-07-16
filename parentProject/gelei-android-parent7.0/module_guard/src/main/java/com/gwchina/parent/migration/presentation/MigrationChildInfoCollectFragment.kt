package com.gwchina.parent.migration.presentation

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.interfaces.adapter.TextWatcherAdapter
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.visible
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.common.NewChildProcessor
import com.gwchina.parent.binding.presentation.ChildInfo
import com.gwchina.parent.binding.presentation.composeToSingleSelectable
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.router.SelectedLevelInfo
import com.gwchina.sdk.base.utils.composeDate
import com.gwchina.sdk.base.utils.getRecommendedGradeByBirthday
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.dialog.showListDialog
import com.gwchina.sdk.base.widget.picker.maxChildDate
import com.gwchina.sdk.base.widget.picker.minChildDate
import com.gwchina.sdk.base.widget.picker.selectDate
import kotlinx.android.synthetic.main.binding_fragment_child_info.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-23 11:42
 *      数据迁移使用的添加孩子
 */
class MigrationChildInfoCollectFragment : InjectorBaseFragment() {


    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    companion object {
        private const val ADJUST_FOR_STATUS_VIEW_KEY = "adjust_for_status_view_key"

        fun newInstance(adjustForStatusView: Boolean = false): Fragment = MigrationChildInfoCollectFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ADJUST_FOR_STATUS_VIEW_KEY, adjustForStatusView)
            }
        }
    }

    @Inject
    internal lateinit var newChildProcessor: NewChildProcessor

    private val adjustForStatusView by lazy {
        arguments?.getBoolean(ADJUST_FOR_STATUS_VIEW_KEY) ?: false
    }

    private val childInfo = ChildInfo()

    private var gradeModifiedByUser = false

    private val gradeArray by lazy {
        resources.getTextArray(R.array.child_grade_array)
    }

    override fun provideLayout() = R.layout.binding_fragment_child_info

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)

        if (adjustForStatusView) {
            sivBinding.visible()
            btnBindingNext.text = getString(R.string.submit)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RouterPath.GuardLevel.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedLevelInfo = data.getParcelableExtra<SelectedLevelInfo>(RouterPath.GuardLevel.SELECTED_LEVEL_INFO)
            val child = migrationViewModel.uploadingChild
            val device = migrationViewModel.migrationDevice
            if (selectedLevelInfo != null && device != null) {
                migrationViewModel.deviceBelongingToChild(device, selectedLevelInfo.level, selectedLevelInfo.guardItem, child)
                showBelongingDevice()
            }
        }
    }

    private fun showBelongingDevice() {
        val nextUnBelongedDevice = migrationViewModel.nextUnBelongedDevice
        if (nextUnBelongedDevice == null) {
            Timber.e("执行到这里了。。。。。")
            processMigratingResult(migrationViewModel.submitChildDeviceList())
        }
    }

    private fun processMigratingResult(submitChildDeviceList: LiveData<Resource<Any>>) {
        submitChildDeviceList.observe(this, Observer { resource ->
            resource?.onError {
                dismissLoadingDialog()
                showRetryDialog()
                errorHandler.handleError(it)
            }?.onLoading {
                showLoadingDialog(false)
            }?.onSuccess {
                dismissLoadingDialog()
                setMigrationEnded()
                //openMainPage()
                appRouter.build(RouterPath.Main.PATH).navigation()
                activity?.finish()
            }
        })
    }

    private fun showRetryDialog() {
        showConfirmDialog {
            message = "数据提交失败，请重试。"
            positiveText = "重试"
            noNegative()
            positiveListener = {
                processMigratingResult(migrationViewModel.submitChildDeviceList())
            }
        }?.setCancelable(false)
    }
}