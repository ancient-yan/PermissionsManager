package com.gwchina.parent.family.presentation.group

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.presentation.group.adapter.GroupManageListAdapter
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.family_fragment_del_group.*
import javax.inject.Inject

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-22 16:29
 *      Desc : 删除分组
 */
class DeleteGroupFragment : InjectorBaseListFragment<GroupPhone>() {

    private val groupDatas: MutableList<GroupPhone> = mutableListOf()

    private val adapter: GroupManageListAdapter by lazy { GroupManageListAdapter(this, true) }

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }
    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    override fun provideLayout(): Any? = R.layout.family_fragment_del_group

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
//        arguments?.let {
//            groupDatas.addAll(it.getParcelableArrayList("datas"))
//        }

        groupDatas.addAll(viewModel.groupPhones.filterNot {
            it.group_name == "未分组"
        })

        adapter.selectListID.observe(this, Observer {
            it?.let { selects ->
                btnFamilyDel.isEnabled = selects.isNotEmpty()
            }
        })

        btnFamilyCancel.setOnClickListener {
            exitFragment(false)
        }

        btnFamilyDel.setOnClickListener {
            showDeleteGroupDialog()
        }
        initRecyclerView()
    }

    private fun deleteGroup() {
        viewModel.delGroup(adapter.selectIdList.joinToString(","))
                .observe(this, Observer {
                    it?.onSuccess {
                        dismissLoadingDialog()
                        familyEventCenter.notifyGroupListNeedRefresh(adapter.selectItemList)
                        adapter.removeSelectItem()
                        viewModel.groupPhones.clear()
                        viewModel.groupPhones.addAll(adapter.items)
                        btnFamilyDel.isEnabled = false
                        exitFragment()
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }?.onLoading {
                        showLoadingDialog(false)
                    }
                })
    }

    private fun showDeleteGroupDialog() {
        showConfirmDialog {
            messageId = R.string.delete_family_group_tips
            this.positiveListener = {
                deleteGroup()
            }
        }
    }

    private fun initRecyclerView() {
        recyFamilyDelGroup.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        recyFamilyDelGroup.adapter = adapter

        replaceData(groupDatas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.clearSelectItem()
    }
}