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
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.data.model.Phone
import com.gwchina.parent.family.presentation.group.adapter.DeletePhoneListAdapter
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.family_fragment_del_group.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-10-17 15:08
 *      删除号码
 */
class DeletePhoneFragment : InjectorBaseListFragment<Phone>() {

    private val phoneDatas: MutableList<Phone> = mutableListOf()

    private lateinit var groupPhone: GroupPhone

    private var selectedList: List<String>? = null

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    private val adapter: DeletePhoneListAdapter by lazy {
        DeletePhoneListAdapter(this)
    }

    companion object {
        fun getInstance(groupPhone: GroupPhone): DeletePhoneFragment {
            val deletePhoneFragment = DeletePhoneFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", groupPhone)
            deletePhoneFragment.arguments = bundle
            return deletePhoneFragment
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.family_fragment_del_group
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        gtlFamilyDelGroupBack.setTitle("删除号码")
        arguments?.let {
            groupPhone = it.getParcelable("data")
            groupPhone.phone_list?.let { it1 -> phoneDatas.addAll(it1) }
        }
        initRecyclerView()
        adapter.onItemClickListener = {
            btnFamilyDel.isEnabled = !it.isNullOrEmpty()
            selectedList = it
        }
        btnFamilyDel.setOnClickListener {
            showConfirmDialog {
                this.messageId = R.string.family_delete_phone_tips
                this.positiveListener = {
                    performDelete()
                }
            }
        }
        btnFamilyCancel.setOnClickListener {
            exitFragment(false)
        }
    }

    private fun initRecyclerView() {
        recyFamilyDelGroup.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        recyFamilyDelGroup.adapter = adapter
        replaceData(phoneDatas)
    }

    private fun performDelete() {
        selectedList?.joinToString(separator = ",")?.let { it1 ->
            viewModel.delFamilyPhone(it1)
                    .observe(this@DeletePhoneFragment, Observer {
                        it?.onSuccess {
                            dismissLoadingDialog()
                            ToastUtils.showShort("已删除")
                            selectedList?.forEach {
                                phoneDatas.remove(phoneDatas.find { phone -> phone.rule_id == it })
                            }
                            replaceData(phoneDatas)
                            //刷新分组设置
                            //刷新分组管理
                            viewModel.getFamilyPhoneInfo(true)
                            viewModel.familyEventCenter.notifyGroupPhoneListNeedRefresh()
                            if (phoneDatas.isEmpty()) {
                                exitFragment()
                            }
                        }?.onError {
                            dismissLoadingDialog()
                            errorHandler.handleError(it)
                        }?.onLoading {
                            showLoadingDialog()
                        }
                    })
        }
    }
}