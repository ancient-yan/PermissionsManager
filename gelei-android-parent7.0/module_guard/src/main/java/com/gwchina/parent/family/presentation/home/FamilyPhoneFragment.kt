package com.gwchina.parent.family.presentation.home

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.StateLayoutConfig
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.presentation.rules.PendingApprovalItemProvider
import com.gwchina.parent.family.FamilyPhoneNavigator
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.data.model.Phone
import com.gwchina.parent.family.widget.FamilyStateActionProcessor
import com.gwchina.parent.family.widget.GroupListPopupWindow
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_PHONE
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import com.gwchina.sdk.base.widget.views.InstructionStateView
import kotlinx.android.synthetic.main.family_fragment_phone.*
import kotlinx.android.synthetic.main.family_phone_list_head.*
import timber.log.Timber
import javax.inject.Inject

class FamilyPhoneFragment : InjectorBaseListFragment<Phone>() {

    companion object {
        const val SHOW_FAMILY_NUM_TIPS = "family_num_tips"

        fun getFragment(phonePlanHasBeSet: Boolean): FamilyPhoneFragment {
            val fragment = FamilyPhoneFragment()
            val bundle = Bundle()
            bundle.putBoolean("phonePlanHasBeSet", phonePlanHasBeSet)
            fragment.arguments = bundle
            return fragment
        }

    }

    private val currentChild by lazy { AppContext.appDataSource().user().currentChild }
    private val childList by lazy { AppContext.appDataSource().user().childList }

    override fun provideLayout(): Any? = R.layout.family_fragment_phone

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    private val adapter: FamilyPhoneListAdapter by lazy { FamilyPhoneListAdapter(this) }

    private val groupPhones: MutableList<GroupPhone> by lazy { mutableListOf<GroupPhone>() }

    private var selectGroupId: String = GroupListPopupWindow.ALL

    private var isMultiDevices: Boolean? = false

    @Inject
    lateinit var navigator: FamilyPhoneNavigator

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    private lateinit var pendingApprovalMenu: PendingApprovalItemProvider

    private var currentChecked = false

    private lateinit var deviceName: String

    private lateinit var mIsvPhone: InstructionStateView

    private var mPhonePlanHasBeSet: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_FAMILYNUMBER)
        isMultiDevices = currentChild?.moreThanOneDevice()

        mPhonePlanHasBeSet = arguments?.getBoolean("mPhonePlanHasBeSet") ?: false

        mIsvPhone = isvPhone
        gtlFamilyPhoneTitle.toolbar.apply {
            inflateMenu(R.menu.family_pending_approval_menu)
        }
        if (childList != null && childList!!.size > 1) {
            llCurrentDevice.visible()
            tvCurrentDevice.text = currentChild?.nick_name
        } else {
            llCurrentDevice.gone()
        }

        pendingApprovalMenu = PendingApprovalItemProvider.findSelf(gtlFamilyPhoneTitle.menu).apply {
            showDot = false
            setOnMenuClickListener {
                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_AUDITING)
                navigator.openApprovalPage()
            }
        }

        if (!mPhonePlanHasBeSet) {
            showInitLayout()
        }

        viewModel.refreshRedDotEventCenter.getRefreshRedDotEvent.observe(this, Observer {
            if (it != null) {
                pendingApprovalMenu.showDot = !it
            }
        })

        sbFamilySbGuard.setOnCheckedChangeListener { _, isChecked ->
            if (!checkIsVip()) {
                sbFamilySbGuard.isChecked = !isChecked
                return@setOnCheckedChangeListener
            }
            val hasShowTips = showFamilyNumTips(isMultiDevices, SHOW_FAMILY_NUM_TIPS)
            viewModel.setFamilyPhoneGuard(isChecked, null, null)
                    .observe(this, Observer {
                        it?.onSuccess {
                            dismissLoadingDialog()
                            if (isChecked) {
                                tvFamilySwitchTitle.text = getString(R.string.open_family_phone_title)
                                tvFamilySwitchSubtitle.text = getString(R.string.open_family_phone_subtitle)
                                getGroupPhone()
                                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_OPEN)
                            } else {
                                tvFamilySwitchTitle.text = getString(R.string.open_family_phone)
                                tvFamilySwitchSubtitle.text = getString(R.string.close_family_phone_subtitle)
                                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_CLOSE)
                            }
                            if (!hasShowTips)
                                showFamilyNumTips(isChecked)
                            else {
                                currentChecked = isChecked
                            }
                            mPhonePlanHasBeSet = true
                            changeEmptyStyle()
                        }?.onError { throwable ->
                            dismissLoadingDialog()
                            sbFamilySbGuard.setCheckedImmediatelyNoEvent(!sbFamilySbGuard.isChecked)
                            errorHandler.handleError(throwable)
                        }?.onLoading {
                            showLoadingDialog(false)
                        }
                    })
        }

        fbFamilyAdd.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_ADDNUMBER)
            navigator.openAddContactsPage()
        }

        familyEventCenter.groupPhoneListRefreshEvent()
                .observe(this, Observer {
                    getGroupPhone()
                })

        viewModel.familyPhoneInfoLiveData
                .observe(this, Observer {
                    it?.onSuccess { temp ->
                        temp?.let { familyPhoneInfo ->
                            this@FamilyPhoneFragment.groupPhones.clear()
                            this@FamilyPhoneFragment.groupPhones.addAll(familyPhoneInfo.group_phone_list)

                            if (familyPhoneInfo.enabled == "1") {
                                sbFamilySbGuard.setCheckedImmediatelyNoEvent(true)
                                tvFamilySwitchTitle.text = getString(R.string.open_family_phone_title)
                                tvFamilySwitchSubtitle.text = getString(R.string.open_family_phone_subtitle)
                            } else {
                                sbFamilySbGuard.setCheckedImmediatelyNoEvent(false)
                                tvFamilySwitchTitle.text = getString(R.string.open_family_phone)
                                tvFamilySwitchSubtitle.text = getString(R.string.close_family_phone_subtitle)
                            }

                            pendingApprovalMenu.showDot = familyPhoneInfo.has_no_approved == "1"
                            val phoneList = refreshFamilyPhoneList()
                            if (selectGroupId == GroupListPopupWindow.ALL) {
                                processListResultWithStatus(phoneList)
                                if (isEmpty) fbFamilyAdd.gone() else fbFamilyAdd.visible()
                                cl_title.visibility = if (isEmpty) View.GONE else View.VISIBLE
                            } else {
                                replaceData(phoneList)
                                showContentLayout()
                            }
                            changeEmptyStyle()
                        }
                    }?.onError { e ->
                        processListErrorWithStatus(e)
                        cl_title.visibility = View.GONE
                    }?.onLoading {
                        showLoadingIfEmpty()
                    }
                })
        initRecyclerView()
        initHeaderView()
        getGroupPhone()

        setupSync()
    }


    private fun setupSync() {
        val childDeviceId = AppContext.appDataSource().user().currentDevice?.device_id
        deviceName = AppContext.appDataSource().user().currentDevice?.device_name ?: ""
        val childId = AppContext.appDataSource().user().currentChild?.child_user_id
        if (childDeviceId == null || childId == null) return
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).registerReceiver(localReceiver, IntentFilter(SyncManager.getInstance().getIntentFilterAction(INSTRUCTION_SYNC_PHONE, childDeviceId)))
        SyncManager.getInstance().querySyncState(requireContext(), INSTRUCTION_SYNC_PHONE, childId, childDeviceId) { isSuccess ->
            if (isSuccess) {
                mIsvPhone.gone()
            } else {
                mIsvPhone.showInitFailedState(getString(R.string.instruction_phone_name))
            }
        }

        mIsvPhone.onRetryClickListener = {
            SyncManager.getInstance().sendSync(childDeviceId, INSTRUCTION_SYNC_PHONE, childId)
        }
    }


    private fun showFamilyNumTips(isChecked: Boolean) {
        if (adapter.dataSize > 0) {
            ToastUtils.showLong(if (isChecked) getString(R.string.family_num_opened) else getString(R.string.family_num_closed))
        }
    }

    /**没有开通亲情号码的初始页面*/
    private fun showInitLayout() {
        showEmptyLayout()
        changeEmptyStyle()
        fbFamilyAdd.gone()
    }


    private fun initRecyclerView() {
        rvFamilyPhoneList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
//        adapter.addHeaderView(headerView)
        rvFamilyPhoneList.adapter = adapter
        adapter.onItemClickListener = {
            val position = it.tag as Int
            val item = adapter.getItem(position)
            navigator.openEditContactsPage(item)
        }
    }

    private fun initHeaderView() {
        rlFamilySwitchGroup.setOnClickListener {
            val groupListPopupWindow = GroupListPopupWindow(requireContext(), selectGroupId)
            groupListPopupWindow.setdatas(groupPhones)
            groupListPopupWindow.onClickListener = { _, groupId ->
                selectGroupId = groupId
                val data = refreshFamilyPhoneList()
                replaceData(data)
                groupListPopupWindow.dismiss()
            }
            groupListPopupWindow.setOnDismissListener {
                ivFamilyArrow.setImageResource(R.drawable.family_icon_arrow_down)
            }
            ivFamilyArrow.setImageResource(R.drawable.family_icon_arrow_up)
            groupListPopupWindow.showAsDropDown(rlFamilySwitchGroup)
        }

        rlFamilyScopeSet.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_SCOPE)
            navigator.openGroupRangeManagePage()
        }

        rlFamilyGroupManage.setOnClickListener {
            navigator.openGroupManagePage()
        }
    }

    private fun refreshFamilyPhoneList(): List<Phone>? {

        var datas: List<Phone>? = null
        if (selectGroupId == GroupListPopupWindow.ALL) {
            datas = groupPhones
                    .filter {
                        if (selectGroupId == GroupListPopupWindow.ALL) {
                            it.phone_list != null
                        } else {
                            it.phone_list != null && it.group_id == selectGroupId
                        }
                    }.flatMap {
                        it.phone_list!!
                    }
            tvFamilyGroupName.text = getString(R.string.all)
        } else {
            val filter = groupPhones.filter {
                it.group_id == selectGroupId
            }

            if (filter.isNotEmpty()) {
                val group = filter.first()
                tvFamilyGroupName.text = group.group_name
                datas = group.phone_list
            }
        }
        //排序：字母->数字->特殊字符
        var result: MutableList<Phone>? = null
        if (datas != null) {
            result = mutableListOf()
            result.apply {
                addAll(datas.filter {
                    (it.ping_yin ?: "").first().isLetter()
                }.sortedBy { it.ping_yin })
                addAll(datas.filter {
                    (it.ping_yin ?: "").first().isDigit()
                }.sortedBy { it.ping_yin })
                addAll(datas.filter {
                    !(it.ping_yin ?: "").first().isLetterOrDigit()
                }.sortedBy { it.ping_yin })
            }
        }
        return result
    }

    private fun getGroupPhone() {
        viewModel.getFamilyPhoneInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun changeEmptyStyle() {
        if (mPhonePlanHasBeSet) {
            stateLayoutConfig.setStateIcon(StateLayoutConfig.EMPTY, R.drawable.img_page_empty)
            stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.family_phone_empty_tips))
            stateLayoutConfig.setStateAction(StateLayoutConfig.EMPTY, getString(R.string.manual_add))
            pendingApprovalMenu.visible = true
        } else {
            stateLayoutConfig.setStateIcon(StateLayoutConfig.EMPTY, R.drawable.family_img_empty)
            stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.family_phone_empty_tips2))
            stateLayoutConfig.setStateAction(StateLayoutConfig.EMPTY, "")
            pendingApprovalMenu.visible = false
        }
        if (stateLayoutConfig.processor is FamilyStateActionProcessor) {
            (stateLayoutConfig.processor as FamilyStateActionProcessor).setTipVisible(!mPhonePlanHasBeSet)
        }
    }

    override fun onRetry(state: Int) {
        if (state == StateLayoutConfig.EMPTY) {
            navigator.openAddContactsPage()
        } else {
            getGroupPhone()
        }
    }

    private fun showFamilyNumTips(multiDevices: Boolean?, key: String): Boolean {
        if (!AppSettings.settingsStorage().getBoolean(key, false) && multiDevices != null && multiDevices) {
            showConfirmDialog {
                message = context.resources.getString(R.string.family_number_switch_tips_task, currentChild?.nick_name)
                positiveText = context.resources.getString(R.string.i_got_it)
                checkBoxText = context.resources.getString(R.string.do_not_tips_again)
                checkBoxChecked = true
                negativeText = null
                positiveListener2 = { _, isChecked ->
                    showFamilyNumTips(currentChecked)
                    AppSettings.settingsStorage().putBoolean(key, isChecked)
                }
            }?.setCanceledOnTouchOutside(false)
            return true
        }
        return false
    }

    //是否是会员
    private fun checkIsVip(): Boolean {
        val vipRule = AppContext.appDataSource().user().vipRule
        val isCanEnter = vipRule?.home_family_enter == FLAG_POSITIVE_ACTION
        if (!isCanEnter) {
            OpenMemberDialog.show(requireContext()) {
                message = getString(R.string.family_number_need_to_open_member_mask, vipRule?.home_family_enter_minimum_level)
                messageDesc = getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.home_family_enter_minimum_level)
                positiveText = getString(R.string.open_vip_to_experience_mask, vipRule?.home_family_enter_minimum_level)
            }
        }
        return isCanEnter
    }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceId = intent?.getStringExtra(SyncManager.DEVICE_ID)
            val action = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_PHONE"]?.childDeviceId
            if (action != null && ("$action-$INSTRUCTION_SYNC_PHONE") == intent?.action) {
                Timber.e("state=====" + intent.getIntExtra(SyncManager.SYNC_STATE, 0))
                when (intent.getIntExtra(SyncManager.SYNC_STATE, 0)) {
                    1 -> mIsvPhone.showSyncingState(getString(R.string.instruction_phone_name))
                    2 -> mIsvPhone.showSyncFailedState(getString(R.string.instruction_phone_name), deviceName)
                    3 -> mIsvPhone.gone()
                }
            }
        }
    }

    override fun onDestroy() {
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).unregisterReceiver(localReceiver)
        super.onDestroy()
    }

}