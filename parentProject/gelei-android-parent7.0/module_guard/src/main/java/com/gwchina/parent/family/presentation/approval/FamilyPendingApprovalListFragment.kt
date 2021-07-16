package com.gwchina.parent.family.presentation.approval

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.StateLayoutConfig
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.rx.bindLifecycle
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.Approval
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.family_fragment_pending_approval.*
import javax.inject.Inject

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-23 14:48
 *      Desc : 亲情号码待审批
 */
class FamilyPendingApprovalListFragment : InjectorBaseListFragment<Approval>() {

    val viewModel by lazy { getViewModel<ApprovalViewModel>(viewModelFactory) }
    val adapter: FamilyPendingApprovalAdapter by lazy { FamilyPendingApprovalAdapter(this) }

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    override fun provideLayout(): Any? = R.layout.family_fragment_pending_approval

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
        autoRefresh()
    }

    private fun initRecyclerView() {
        rvFamilyApprovalList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        rvFamilyApprovalList.adapter = adapter
        stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.no_pending_approval))

        adapter.onAgreeOrRefuseClickListener = { v, position ->
            if (v.tag as String == FamilyPendingApprovalAdapter.AGREE){
                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_AUDITING_ALLOW)
            }else{
                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_AUDITING_PROHIBIT)
            }
            val item = adapter.getItem(position)
            viewModel.approvalPhone(item.record_id, if (v.tag as String == FamilyPendingApprovalAdapter.AGREE) "1" else "2")
                    .observe(this, Observer {
                        it?.onSuccess {
                            dismissLoadingDialog()
                            autoRefresh()
                            if (isEmpty) {
                                viewModel.refreshRedDotEventCenter.setRefreshRedDotEvent(true)
                            }
                            if (v.tag as String == FamilyPendingApprovalAdapter.AGREE) {
                                ToastUtils.showShort("已同意")
                                familyEventCenter.notifyGroupPhoneListNeedRefresh()
                            } else {
                                ToastUtils.showShort("已拒绝")
                            }
                        }?.onError { e ->
                            dismissLoadingDialog()
                            errorHandler.handleError(e)
                        }?.onLoading {
                            showLoadingDialog(false)
                        }
                    })
        }
    }

    override fun onStartLoad() {
        viewModel.getApprovalRecord()
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it.orElse(null))
                }, {
                    processListErrorWithStatus(it)
                })
    }

}