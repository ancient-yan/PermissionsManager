package com.gwchina.parent.family.presentation.group

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.Resource
import com.android.base.rx.bindLifecycle
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.FamilyPhoneNavigator
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.data.model.Phone
import com.gwchina.parent.family.presentation.home.FamilyPhoneListAdapter
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.family_fragment_group_setting.*
import javax.inject.Inject

class GroupSettingFragment : InjectorBaseListFragment<Phone>() {

    private val adapter: FamilyPhoneListAdapter by lazy { FamilyPhoneListAdapter(this) }
    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    @Inject
    lateinit var navigator: FamilyPhoneNavigator

    private var groupPhone: GroupPhone = GroupPhone()

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    override fun provideLayout(): Any? = R.layout.family_fragment_group_setting

    override
    fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            groupPhone = it.getParcelable("groupPhone")
        }

        gtlEditGroupTitle.toolbar.apply {
            inflateMenu(R.menu.family_del_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.familyDel -> {
                        navigator.openDelPhonePage(groupPhone)
                    }
                }
                true
            }
        }
        setRightIconVisible()

        tvFamilyGroupName.text = groupPhone.group_name

        tvFamilyGroupName.setOnClickListener {
            navigator.openGroupModifyPage(groupName = tvFamilyGroupName.text as String, childFragmentManager = childFragmentManager) { content, isSuccess ->
                if (isSuccess) {
                    tvFamilyGroupName.text = content
                    //刷新分组设置&&刷新分组管理
                    viewModel.getFamilyPhoneInfo(true)
                    familyEventCenter.notifyGroupPhoneListNeedRefresh()
                }
                updateGroup(group_ids = groupPhone.group_id, is_call_in = groupPhone.is_call_in, is_call_out = groupPhone.is_call_out, group_name = content)
            }
        }
        initRecyclerView()
        fbFamilyAdd.setOnClickListener {
            navigator.openAddContactsPage(groupPhone.group_id)
        }

        familyEventCenter.groupPhoneListRefreshEvent()
                .observe(this, Observer {
                    getGroupPhone()
                })
    }

    private fun getGroupPhone() {
        viewModel.getGroupPhone(groupPhone.group_id)
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    val groupPhones = it.orElse(GroupPhone())
                    this.groupPhone = groupPhones
                    processListResultWithStatus(groupPhones.phone_list)
                }, {
                    processListErrorWithStatus(it)
                })
    }

    private fun initRecyclerView() {
        rvFamilyPhoneList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        rvFamilyPhoneList.adapter = adapter
        adapter.onItemClickListener = {
            val position = it.tag as Int
            val item = adapter.getItem(position)
            navigator.openEditContactsPage(item)
        }
        if (groupPhone.phone_list == null || groupPhone.phone_list.isNullOrEmpty()) {
            showEmptyLayout()
        } else {
            replaceData(groupPhone.phone_list)
        }
    }

    private fun setRightIconVisible() {
        gtlEditGroupTitle.menu.findItem(R.id.familyDel).isVisible = !groupPhone.phone_list.isNullOrEmpty()
    }

    private fun updateGroup(group_ids: String, is_call_out: String?, is_call_in: String?, group_name: String?): LiveData<Resource<Any>> {
        return viewModel.updateGroup(group_ids, is_call_out, is_call_in, group_name)
    }
}