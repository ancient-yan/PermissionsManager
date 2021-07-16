package com.gwchina.parent.family.presentation.group

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.FamilyPhoneNavigator
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.presentation.group.adapter.GroupRangeSettingAdapter
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.family_fragment_group_range_setting.*
import javax.inject.Inject

class GroupRangeSettingFragment : InjectorBaseListFragment<GroupPhone>() {
    companion object {
        const val CALL = "call"
        const val ANSWER = "answer"
    }

    private val currentChild by lazy { AppContext.appDataSource().user().currentChild }

    private var pageType = CALL

    private val adapter: GroupRangeSettingAdapter by lazy {
        GroupRangeSettingAdapter(this, pageType,
                if (pageType == CALL)
                    viewModel.is_call_out == "1"
                else
                    viewModel.is_call_in == "1")
    }

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter
    @Inject
    lateinit var navigator: FamilyPhoneNavigator

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    override fun provideLayout(): Any? = R.layout.family_fragment_group_range_setting

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            pageType = it.getString("type")
        }

        initRecyclerView()
        if (pageType == CALL) {
            if (viewModel.is_call_out != "1") {
                if (viewModel.groupPhones.isNotEmpty()) {
                    replaceData(viewModel.groupPhones)
                } else {
                    showEmptyLayout()
                }
            } else {
                replaceData(null)
            }
        } else {
            if (viewModel.is_call_in != "1") {
                if (viewModel.groupPhones.isNotEmpty()) {
                    replaceData(viewModel.groupPhones)
                } else {
                    showEmptyLayout()
                }
            } else {
                replaceData(null)
            }
        }

    }

    private fun initRecyclerView() {
        rvFamilyGuardList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        rvFamilyGuardList.adapter = adapter
        val isMultiDevices = currentChild?.moreThanOneDevice()
        adapter.onItemCheckedChangeListener = { position, isChecked, button ->
            if (position == GroupRangeSettingAdapter.TYPE_HEAD) {
                //不限制
                changeLimitState(isChecked)
                showFamilyNumTips(isMultiDevices, pageType)
            } else if (position == GroupRangeSettingAdapter.TYPE_ALL) {
                val groupIds = viewModel.groupPhones.filter {
                    if (pageType == CALL) {
                        it.is_call_out == if (isChecked) "0" else "1"
                    } else {
                        it.is_call_in == if (isChecked) "0" else "1"
                    }
                }.map { it.group_id }
                if (groupIds.isNotEmpty()) {
                    if (pageType == CALL) {
                        if (isChecked) {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_SCOPE_ALLCALL_OPEN)
                        } else {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_SCOPE_ALLCALL_CLOSE)
                        }
                        updateGroup(position, groupIds.joinToString(","), if (isChecked) "1" else "0", null, null)
                    } else {
                        if (isChecked) {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_SCOPE_ALLANSWER_OPEN)
                        } else {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_SCOPE_ALLANSWER_CLOSE)
                        }
                        updateGroup(position, groupIds.joinToString(","), null, if (isChecked) "1" else "0", null)
                    }
                }
            } else {
                val item = adapter.getItem(position)
                if (pageType == CALL) {
                    updateGroup(position, item.group_id, if (isChecked) "1" else "0", null, null)
                } else {
                    updateGroup(position, item.group_id, null, if (isChecked) "1" else "0", null)
                }
            }

        }
    }

    private fun changeLimitState(checked: Boolean) {
        viewModel.setFamilyPhoneGuard(null,
                if (pageType == CALL) checked else null,
                if (pageType == ANSWER) checked else null)
                .observe(this, Observer {
                    it?.onSuccess {
                        dismissLoadingDialog()
                        adapter.changeLimitState(checked)
                        if (checked) {
                            replaceData(null)
                        } else {
                            replaceData(viewModel.groupPhones)
                        }
                        familyEventCenter.notifyGroupPhoneListNeedRefresh()
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                        adapter.changeLimitState(!checked)
                    }?.onLoading {
                        showLoadingDialog(false)
                    }

                })
    }

    private fun updateGroup(position: Int, group_ids: String, is_call_out: String?, is_call_in: String?, group_name: String?) {
        viewModel.updateGroup(group_ids, is_call_out, is_call_in, group_name)
                .observe(this, Observer { resource ->
                    resource?.onSuccess {
                        dismissLoadingDialog()
                        if (position == GroupRangeSettingAdapter.TYPE_ALL) {
                            viewModel.groupPhones
                                    .map {
                                        if (is_call_out != null && is_call_out.isNotBlank()) {
                                            it.is_call_out = is_call_out
                                        }

                                        if (is_call_in != null && is_call_in.isNotBlank()) {
                                            it.is_call_in = is_call_in
                                        }
                                    }
                            adapter.notifyDataSetChanged()
                        } else {
                            val item = adapter.getItem(position)
                            if (is_call_out != null && is_call_out.isNotBlank()) {
                                item.is_call_out = is_call_out
                            }

                            if (is_call_in != null && is_call_in.isNotBlank()) {
                                item.is_call_in = is_call_in
                            }
                            adapter.notifyAllItemChanged()
                            adapter.notifyNormalItemChanged(position)
                        }
                        familyEventCenter.notifyGroupPhoneListNeedRefresh()
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                        if (position == GroupRangeSettingAdapter.TYPE_ALL) {
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.notifyNormalItemChanged(position)
                        }
                    }?.onLoading {
                        showLoadingDialog()
                    }
                })
    }

    private fun showFamilyNumTips(multiDevices: Boolean?, key: String) {
        if (!AppSettings.settingsStorage().getBoolean(key, false) && multiDevices != null && multiDevices) {
            showConfirmDialog {
                message = context.resources.getString(R.string.family_number_switch_tips_task, currentChild?.nick_name)
                positiveText = context.resources.getString(R.string.i_got_it)
                checkBoxText = context.resources.getString(R.string.do_not_tips_again)
                checkBoxChecked = true
                negativeText = null
                positiveListener2 = { _, isChecked ->
                    AppSettings.settingsStorage().putBoolean(key, isChecked)
                }
            }?.setCanceledOnTouchOutside(false)
        }
    }

}