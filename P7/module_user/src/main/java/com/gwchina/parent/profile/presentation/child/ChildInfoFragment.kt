package com.gwchina.parent.profile.presentation.child

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.utils.android.TintUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.Navigator
import com.gwchina.parent.profile.presentation.common.ChildInfoFunctionTipDialog
import com.gwchina.parent.profile.presentation.common.EnterNicknameDialogFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.TempTimePeriodRule
import com.gwchina.sdk.base.router.RouterPath.Profile.CHILD_NEED_SHOW_PERMISSION_KEY
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.*
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.dialog.showListDialog
import com.gwchina.sdk.base.widget.dialog.showSelectChildSexDialog
import com.gwchina.sdk.base.widget.dialog.showSelectDurationDialog
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import com.gwchina.sdk.base.widget.picker.maxChildDate
import com.gwchina.sdk.base.widget.picker.minChildDate
import com.gwchina.sdk.base.widget.picker.selectDate
import com.gwchina.sdk.base.widget.views.BaseItem
import com.gwchina.sdk.base.widget.views.ItemConfiguration
import com.gwchina.sdk.base.widget.views.ItemManager
import com.gwchina.sdk.base.widget.views.TextItem
import kotlinx.android.synthetic.main.profile_fragment_child_info.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 *@author Wangwb
 *      Email: 253123123@qq.com
 *      Date : 2019-01-01 18:19
 *      Modified by Ztiany at 2019-03-18
 */
class ChildInfoFragment : InjectorBaseFragment() {

    companion object {
        private const val ENTRANCE_CHILD_NICK_NAME = 2
        private const val ENTRANCE_CHILD_SEX = 3
        private const val ENTRANCE_CHILD_BIRTHDAY = 4
        private const val ENTRANCE_CHILD_GRADE = 5

        private const val FUNCTION_TIP_FLAG = "function_tip_flag"

        private const val LOOK_SCREEN_SUCCESS_FLAG = "look_screen_success_flag"
        private const val TEMPORARY_AVAILABILITY_SUCCESS_FLAG = "temporary_availability_success_flag"

        const val LOCK_SCREEN = 1
        const val TEMPORARY_AVAILABILITY = 0

        fun getFragment(needShowPermissionDialog: Boolean): ChildInfoFragment {
            val fragment = ChildInfoFragment()
            val bundle = Bundle()
            bundle.putBoolean(CHILD_NEED_SHOW_PERMISSION_KEY, needShowPermissionDialog)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var navigator: Navigator

    private var needShowPermissionDialog: Boolean = false

    private val itemManager by lazy {
        ItemManager(requireContext(), ItemConfiguration(colorFromId(R.color.transparent)))
    }

    private val navigationIcon by lazy {
        TintUtils.tint(drawableFromId(R.drawable.icon_back)?.mutate(), colorFromId(R.color.white))
    }

    private val deviceAdapter by lazy {
        ChildDeviceStateAdapter(requireContext())
    }

    private lateinit var childInfoViewModel: ChildInfoViewModel
    private val childInfo = ChildInfoViewModel.ChildInfo()

    private var isFirstInit = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childInfoViewModel = getViewModel(viewModelFactory)
        subscribeViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_CHILDREN)
        needShowPermissionDialog = arguments?.getBoolean(CHILD_NEED_SHOW_PERMISSION_KEY) ?: false
    }

    override fun provideLayout() = R.layout.profile_fragment_child_info

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        itemManager.setup(rvChildInfo, buildItemList())
        itemManager.setOnItemClickedListener { _, baseItem ->
            processItemClicked(baseItem.id)
        }
        gtlChildInfoTitle.toolbar.setTitleTextColor(Color.WHITE)
        gtlChildInfoTitle.toolbar.navigationIcon = navigationIcon

        rvChildDevice.layoutManager = LinearLayoutManager(context)
        rvChildDevice.adapter = deviceAdapter
    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setTranslucentSystemUi(activity, true, false)
        activity.setStatusBarDarkMode()
    }

    /**
     * 显示功能提示的dialog
     */
    private fun showFunctionTipDialog(child: Child) {
        val isShowTipDialog = AppSettings.settingsStorage().getBoolean(FUNCTION_TIP_FLAG, true)
        val severeDevice = child.device_list?.any {
            //至少有一个重度守护&&会员未过期
            it.guard_level.isSevereMode() && it.status == 1
        }
        if (severeDevice == true && isShowTipDialog) {
            llChildContent.invisible()
            ChildInfoFunctionTipDialog(requireActivity()) {
                AppSettings.settingsStorage().putBoolean(FUNCTION_TIP_FLAG, false)
                llChildContent.visible()
            }.show()
        }
    }

    /**
     * 显示开启临时锁屏的确认dialog
     */
    private fun showLookScreenConfirmDialog(device: Device) {
        showConfirmDialog {
            message = getString(R.string.lock_screen_confirm_tips)
            positiveId = R.string.continue_lock_screen
            negativeId = R.string.no_lock_screen
            positiveListener = {
                toSetTempLookScreen(device)
            }
        }
    }

    /**
     * 设置临时锁屏
     */
    private fun toSetTempLookScreen(device: Device) {
        val nextTime = childInfoViewModel.getNextLookScreenTime(device.device_id)
        showSelectDurationDialog(requireContext()) {
            tips = if (nextTime.isNextDay) {
                context.getString(R.string.end_of_the_day_mask, formatSecondsToTimeText(nextTime.timeSeconds, true))
            } else {
                context.getString(R.string.next_look_period_mask, formatSecondsToTimeText(nextTime.timeSeconds, true))
            }
            durationRange = TimePeriod(Time(0, 0), Time(4, 50))
            must = true
            positiveListener = { _, duration ->
                processOnLookScreenTimeSelected(duration, device)
            }
        }
    }

    /**
     * 处理临时锁屏选择时间
     */
    private fun processOnLookScreenTimeSelected(duration: Time, device: Device) {
        val nextTime = childInfoViewModel.getNextLookScreenTime(device.device_id)
        if (duration.isNotZero()) {
            if (duration.toSeconds() > nextTime.timeSeconds) {
                showExceedTimeDialog(LOCK_SCREEN)
            } else if (!AppSettings.settingsStorage().getBoolean(LOOK_SCREEN_SUCCESS_FLAG, false) && childInfoViewModel.deviceIsTimeRule(device.device_id)) {
                showOperationSuccessDialog(LOCK_SCREEN) {
                    childInfoViewModel.addTemporarilyPlan(duration.toMinutes(), device.device_id, LOCK_SCREEN)
                }
            } else {
                childInfoViewModel.addTemporarilyPlan(duration.toMinutes(), device.device_id, LOCK_SCREEN)
            }
        }
    }

    /**
     * 显示开启临时锁屏或者临时可用成功的dialog
     */
    private fun showOperationSuccessDialog(type: Int, action: () -> Unit) {
        val resId = if (type == LOCK_SCREEN) R.string.lock_screen_success_tips else R.string.temporary_availability_success_tips
        val tag = if (type == LOCK_SCREEN) LOOK_SCREEN_SUCCESS_FLAG else TEMPORARY_AVAILABILITY_SUCCESS_FLAG
        showConfirmDialog {
            messageId = resId
            positiveId = R.string.i_got_it
            checkBoxId = R.string.do_not_tips_again
            checkBoxChecked = true
            noNegative()
            positiveListener2 = { _, checked ->
                AppSettings.settingsStorage().putBoolean(tag, checked)
                action.invoke()
            }
        }?.setCanceledOnTouchOutside(false)
    }

    /**
     * 显示选择时间超过时间段的dialog
     */
    private fun showExceedTimeDialog(type: Int) {
        val resId = if (type == LOCK_SCREEN) R.string.lock_screen_exceed_tips else R.string.temporary_availability_exceed_tips
        showConfirmDialog {
            message = getString(resId)
            positiveId = R.string.i_got_it
            noNegative()
        }?.setCanceledOnTouchOutside(false)
    }

    private fun processItemClicked(id: Int) {
        when (id) {
            //修改昵称
            ENTRANCE_CHILD_NICK_NAME -> showEnterNameDialog()
            // 修改性别
            ENTRANCE_CHILD_SEX -> selectChildSex()
            // 修改生日
            ENTRANCE_CHILD_BIRTHDAY -> selectBirthday()
            // 修改年级
            ENTRANCE_CHILD_GRADE -> selectGrade()
        }
    }

    private fun showEnterNameDialog() {
        EnterNicknameDialogFragment.show(childInfo.nickname, childFragmentManager) {
            updateChildName(it)
            childInfo.nickname = it
            childInfoViewModel.updateChildInfo(childInfo)
        }
    }

    private fun selectGrade() {
        showListDialog {
            itemsId = R.array.child_grade_array
            selectedPosition = childInfo.grade
            positiveListener = { grade: Int, item: CharSequence ->
                childInfo.grade = grade
                itemManager.updateTextItem(ENTRANCE_CHILD_GRADE) {
                    it.subtitle = item.toString()
                }
                doUpdate()
            }
        }
    }

    private fun selectBirthday() {
        val splitBirthday = splitBirthday(childInfo.birthday).toCalendar()
        selectDate {
            initDate = splitBirthday
            minDate = minChildDate()
            maxDate = maxChildDate()

            onDateSelectedListener = { year: Int, monthOfYear: Int, dayOfMonth: Int ->
                childInfo.birthday = composeDate(year, monthOfYear, dayOfMonth)
                itemManager.updateTextItem(ENTRANCE_CHILD_BIRTHDAY) {
                    it.subtitle = composeDate(year, monthOfYear, dayOfMonth, "-")
                }

                doUpdate()

                //update selected
                splitBirthday.set(Calendar.YEAR, year)
                splitBirthday.set(Calendar.MONTH, monthOfYear)
                splitBirthday.set(Calendar.DAY_OF_MONTH, dayOfMonth - 1)
            }
        }
    }

    private fun selectChildSex() {
        showSelectChildSexDialog(childInfo.sex) {
            childInfo.sex = it
            updateChildSex(it)
            updateChildAvatar(it)
            doUpdate()
        }
    }

    private fun doUpdate() {
        childInfoViewModel.updateChildInfo(childInfo)
                .observe(this, Observer {
                    it?.onError { err ->
                        errorHandler.handleError(err)
                    }?.onSuccess {
                        showMessage(getString(R.string.save_successfully))
                    }
                })
    }

    private fun buildItemList(): List<BaseItem> {
        return listOf(
                TextItem(ENTRANCE_CHILD_NICK_NAME, title = getString(R.string.nickname), hasArrow = true, backgroundColorIdRes = R.color.transparent),
                TextItem(ENTRANCE_CHILD_SEX, title = getString(R.string.sex), hasArrow = true, backgroundColorIdRes = R.color.transparent),
                TextItem(ENTRANCE_CHILD_BIRTHDAY, title = getString(R.string.birthday_date), hasArrow = true, backgroundColorIdRes = R.color.transparent),
                TextItem(ENTRANCE_CHILD_GRADE, title = getString(R.string.grade), hasArrow = true, backgroundColorIdRes = R.color.transparent))
    }

    private var lastChild: Child? = null

    private fun subscribeViewModel() {
        childInfoViewModel.child
                .observe(this, Observer {
                    it?.let {
                        Timber.e("target===child===${it}")
                        Timber.e("target===lastChild===${lastChild}")
                        //比较内容相等

                        showDevice(it)
                        showChildInfo(it)
                        lastChild = it
                    }
                })

        childInfoViewModel.open
                .observe(this, Observer {
                    it?.let {
                        if (it.showLoading) {
                            showLoadingDialog()
                        } else {
                            Timber.e("====open")
                            dismissLoadingDialog()
                        }
                        it.message?.let { msg ->
                            showMessage(msg)
                        }
                    }
                })

        childInfoViewModel.close
                .observe(this, Observer {
                    it?.let {
                        if (it.showLoading) {
                            showLoadingDialog()
                        } else {
                            Timber.e("====close")
                            dismissLoadingDialog()
                        }
                        it.message?.let { msg ->
                            showMessage(msg)
                        }
                    }
                })
    }

    private fun showDevice(child: Child) {
        if (child.device_list.isNullOrEmpty()) {
            //显示绑定设备
            flChildNoDevice.visible()
            rvChildDevice.gone()

            tvChildNoDevice.enableSpanClickable()
            tvChildNoDevice.text = SpanUtils()
                    .append(AppContext.getContext().getString(R.string.child_no_binding_phone))
                    .append(AppContext.getContext().getString(R.string.child_click_binding))
                    .setClickSpan(newGwStyleClickSpan(AppContext.getContext(), AppContext.getContext().colorFromId(com.app.base.R.color.green_main)) {
                        navigator.openAddDevicePage(child.child_user_id)
                    })
                    .create()
        } else {
            //显示设备列表
            deviceAdapter.onClose = { device_id: String, tempTimePeriodRule: TempTimePeriodRule ->
                tempTimePeriodRule.rule_id.ifNonNull {
                    showConfirmDialog {
                        messageId = if (tempTimePeriodRule.mode == LOCK_SCREEN) R.string.confirm_to_delete_look_screen else R.string.confirm_to_delete_temporarily_usable
                        positiveListener = {
                            childInfoViewModel.deleteTemporarilyPlan(this@ifNonNull, device_id)
                        }
                    }
                }
            }
            deviceAdapter.onOppen = {
                val vipRule = childInfoViewModel.appDataSource.user().vipRule
                val isCanUsable = vipRule?.temp_use_enabled == FLAG_POSITIVE_ACTION
                val isCanLockable = vipRule?.temp_lock_enabled == FLAG_POSITIVE_ACTION
                if (isYes(it.rule_time_flag) && it.surplus_used_time > 0) {
                    //没有锁屏，开启临时锁屏
                    if (isMemberGuardExpired(it.status) || (!isCanLockable)) {
                        OpenMemberDialog.show(requireContext()) {
                            message = requireContext().getString(R.string.temporary_lock_screen_vip_tip_mask, vipRule?.temp_lock_enabled_minimum_level)
                            messageDesc = requireContext().getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.temp_lock_enabled_minimum_level)
                            positiveText = requireContext().getString(R.string.open_vip_to_experience_mask, vipRule?.temp_lock_enabled_minimum_level)
                        }
                    } else {
                        if (needShowPermissionDialog) {
                            showPermissionTipDialog()
                        } else {
                            showLookScreenConfirmDialog(it)
                        }
                    }
                } else {
                    //锁屏，开启临时可用
                    if (isMemberGuardExpired(it.status) || (!isCanUsable)) {
                        OpenMemberDialog.show(requireContext()) {
                            message = requireContext().getString(R.string.temporary_availability_vip_tip_mask, vipRule?.temp_use_enabled_minimum_level)
                            messageDesc = requireContext().getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.temp_use_enabled_minimum_level)
                            positiveText = requireContext().getString(R.string.open_vip_to_experience_mask, vipRule?.temp_use_enabled_minimum_level)
                        }
                    } else {
                        toSetTempUsable(it)
                    }
                }
            }
            val result = sortDeviceListByStatus(child.device_list)
            Timber.e("target===aaaaaaaaaaaaa")
            deviceAdapter.setDataSource(result, true)
            flChildNoDevice.gone()
            rvChildDevice.visible()

            if (isFirstInit) {
                isFirstInit = false
                showFunctionTipDialog(child)
            }
        }
    }

    //如果家长会员过期了，对当前孩子的设备列表按照是否守护进行排序 守护的设备排在前面
    private fun sortDeviceListByStatus(deviceList: List<Device>?): List<Device>? {
        if (deviceList?.all { it.status == 1 } == true) return deviceList
        return deviceList?.sortedByDescending { it.status }
    }

    private fun toSetTempUsable(device: Device) {
        val nextTime = childInfoViewModel.getNextUsablePeriodTime(device.device_id)
        showSelectDurationDialog(requireContext()) {
            tips = if (nextTime.isNextDay) {
                context.getString(R.string.end_of_the_day_mask, formatSecondsToTimeText(nextTime.timeSeconds, true))
            } else {
                context.getString(R.string.next_usable_period_mask, formatSecondsToTimeText(nextTime.timeSeconds, true))
            }
            durationRange = TimePeriod(Time(0, 0), Time(4, 50))
            must = true
            positiveListener = { _, duration ->
                processOnTempUsableTimeSelected(duration, device)
            }
        }
    }

    private fun processOnTempUsableTimeSelected(duration: Time, device: Device) {
        val realNextTime = childInfoViewModel.getNextUsablePeriodTime(device.device_id)
        if (duration.isNotZero()) {
            if (duration.toSeconds() > realNextTime.timeSeconds) {
                showExceedTimeDialog(TEMPORARY_AVAILABILITY)
            } else {
                if (!AppSettings.settingsStorage().getBoolean(TEMPORARY_AVAILABILITY_SUCCESS_FLAG, false)) {
                    showOperationSuccessDialog(TEMPORARY_AVAILABILITY) {
                        childInfoViewModel.addTemporarilyPlan(duration.toMinutes(), device.device_id, TEMPORARY_AVAILABILITY)
                    }
                } else {
                    childInfoViewModel.addTemporarilyPlan(duration.toMinutes(), device.device_id, TEMPORARY_AVAILABILITY)
                }
            }
        }
    }

    private fun showChildInfo(child: Child) {
        childInfo.birthday = child.birthday ?: ""
        childInfo.grade = child.grade
        childInfo.nickname = child.nick_name ?: ""
        childInfo.sex = child.sex

        updateChildAvatar(child.sex)
        updateChildName(child.nick_name ?: "")
        updateChildSex(child.sex)

        itemManager.updateTextItem(ENTRANCE_CHILD_BIRTHDAY) {
            val birthday = splitBirthday(child.birthday)
            it.subtitle = composeDate(birthday.year, birthday.monty, birthday.day, "-")
        }

        itemManager.updateTextItem(ENTRANCE_CHILD_GRADE) {
            val gradeArray = resources.getStringArray(R.array.child_grade_array)
            if (child.grade < gradeArray.size) {
                it.subtitle = gradeArray[child.grade]
            }
        }
    }

    private fun updateChildSex(sex: Int) {
        itemManager.updateTextItem(ENTRANCE_CHILD_SEX) {
            it.subtitle = if (sex == SEX_MALE) {
                getString(R.string.child_sex_male)
            } else {
                getString(R.string.child_sex_female)
            }
        }
    }

    private fun updateChildName(nick_name: String) {
        tvChildName.text = nick_name
        itemManager.updateTextItem(ENTRANCE_CHILD_NICK_NAME) {
            it.subtitle = nick_name
        }
    }

    private fun updateChildAvatar(sex: Int) {
        ivChildAvatar.setImageResource(
                if (sex == SEX_MALE) {
                    R.drawable.img_head_boy_38
                } else {
                    R.drawable.img_head_girl_38
                })
    }

    private fun showPermissionTipDialog() {
        showConfirmDialog {
            this.message = getString(R.string.home_no_permission_dialog_title)
            this.positiveId = R.string.home_no_permission_state3
            this.negativeId = R.string.i_got_it
            this.messageGravity = Gravity.START
            positiveListener = {
                navigator.openChildDevicePermissionInfo()
            }
        }?.apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

}