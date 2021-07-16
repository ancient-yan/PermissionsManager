package com.gwchina.parent.family.presentation.group

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.FamilyPhoneNavigator
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.presentation.group.adapter.GroupManageListAdapter
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.parent.family.widget.AddAndUpdateGroupDialog
import com.gwchina.parent.family.widget.showAddAndUpdateGroupDialog
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.family_fragment_group_manage.*
import javax.inject.Inject

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-22 15:47
 *      Desc : 分组管理
 */
class GroupManageFragment : InjectorBaseListFragment<GroupPhone>() {

    private val groupDatas: MutableList<GroupPhone> = mutableListOf()

    @Inject
    lateinit var navigator: FamilyPhoneNavigator

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    private val adapter: GroupManageListAdapter by lazy { GroupManageListAdapter(this, false) }

    override fun provideLayout(): Any? = R.layout.family_fragment_group_manage

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {

        viewModel.getAllGroupPhone(showLoading = true)

        gtlFamilyGroupManageBack.toolbar.apply {
            inflateMenu(R.menu.family_del_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.familyDel -> {
                        navigator.openDelGroupPage()
                    }
                }
                true
            }
        }
        familyEventCenter.groupListRefreshEvent()
                .observe(this, Observer {
                    it?.let { list ->
                        groupDatas.removeAll(list)
                        replaceData(groupDatas)
                        familyEventCenter.notifyGroupPhoneListNeedRefresh()
                    }
                })

        notifyGroupData()

        fbFamilyGroupAdd.setOnClickListener {
            showAddAndUpdateGroupDialog(requireContext()) {
                positiveListener = { dialog, groupName ->
                    viewModel.addGroupAndRefreshGroupList(groupName)
                            .observe(this@GroupManageFragment, Observer {
                                handleResult(it, dialog)
                            })
                }
            }
        }
        initRecyclerView()
    }

    private fun notifyGroupData() {
        viewModel.groupPhoneLiveData.observe(this, Observer {
            handleResult(it)
        })
    }

    private fun handleResult(it: Resource<List<GroupPhone>>?, dialog: AddAndUpdateGroupDialog? = null) {
        it?.onSuccess { datas ->
            dismissLoadingDialog()
            groupDatas.clear()
            datas?.let { list -> groupDatas.addAll(list.filterNot { it.group_name == "未分组" }) }
            replaceData(groupDatas)
            familyEventCenter.notifyGroupPhoneListNeedRefresh()
            dialog?.dismiss()
        }?.onError { throwable ->
            dismissLoadingDialog()
            errorHandler.handleError(throwable)
            dialog?.cannotAddedTips(true, errorHandler.createMessage(throwable).toString())
        }?.onLoading {
            showLoadingDialog()
            dialog?.cannotAddedTips(false)
        }
    }

    private fun initRecyclerView() {
        rvFamilyGroupList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        rvFamilyGroupList.adapter = adapter
        adapter.onItemClickListener = {
            val position = it.tag as Int
            val item = adapter.getItem(position)
            navigator.openGroupSettingPage(item)
        }
        replaceData(groupDatas)
    }

}