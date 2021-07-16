package com.gwchina.parent.binding.presentation

import android.arch.lifecycle.Observer
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.BindingNavigator
import com.gwchina.parent.binding.common.BindingProcessStatusKeeper
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.MAX_CHILD_COUNT
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.binding_fragment_select_child.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-11 16:47
 */
class SelectChildFragment : InjectorBaseFragment() {

    @Inject
    lateinit var bindingNavigator: BindingNavigator
    @Inject
    lateinit var bindingProcessStatusKeeper: BindingProcessStatusKeeper

    private val viewModel by lazy {
        getViewModelFromActivity<BindingViewModel>(viewModelFactory)
    }

    private val childAdapter by lazy {
        ChildAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.binding_fragment_select_child

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        rvBindingChildList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom = dip(20)
            }
        })

        rvBindingChildList.adapter = childAdapter

        childAdapter.onSelectedChildListener = {
            if (viewModel.user.value?.vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION || (it.device_list.isNullOrEmpty() && !isMemberGuardExpired(it.status))) {
                bindingProcessStatusKeeper.setAddingDeviceForExistChild(it.child_user_id)
                bindingNavigator.openScanChildGuidePage()
            } else {
                showRequiringMemberDialog()
            }
        }

        tvBindingAddChild.setOnClickListener {
            if (viewModel.user.value?.vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION) {
                bindingNavigator.openChildInfoCollectPage()
            } else {
                showRequiringMemberDialog()
            }
        }
    }

    private fun showRequiringMemberDialog() {
        OpenMemberDialog.show(requireContext()) {
            message = getString(R.string.multi_device_guard_requirement_member_tips_mask, viewModel.vipRule?.home_mine_add_device_enabled_minimum_level)
            messageDescId = R.string.multi_device_guard_requirement_member_desc
            positiveText = getString(R.string.open_vip_to_experience_mask, viewModel.vipRule?.home_mine_add_device_enabled_minimum_level)
        }
    }

    private fun subscribeViewModel() {
        viewModel.user.observe(this, Observer {
            it?.let(::showList)
        })
    }

    private fun showList(user: User) {
        val childList = user.childList?.toMutableList() ?: mutableListOf()
        childList.sortBy { it.device_list?.size ?: 0 }

        childAdapter.replaceAll(childList)

        if ((user.childList?.size ?: 0) >= MAX_CHILD_COUNT/*max child count*/) {
            tvBindingMaxChildCountTips.visible()
            tvBindingAddChild.gone()
        } else {
            tvBindingAddChild.visible()
            tvBindingMaxChildCountTips.gone()
        }
    }

}