package com.gwchina.parent.main.presentation.approval

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.kotlin.dip
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.presentation.home.card.PhoneApprovalItemViewBinder
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import kotlinx.android.synthetic.main.home_fragment_phone_approval.*
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 * 应用审批（全部孩子，全部设备）
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-18 11:42
 */
class PhoneApprovalFragment : InjectorBaseStateFragment() {

    @Inject lateinit var mainNavigator: MainNavigator

    private val adapter by lazy {
        MultiTypeAdapter(context)
    }

    private val viewModel by lazy {
        getViewModel<PhoneApprovalViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.home_fragment_phone_approval

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        rvHomePhoneApproval.layoutManager = LinearLayoutManager(context)
        rvHomePhoneApproval.addItemDecoration(MarginDecoration(0, dip(10), 0, 0))
        setupAdapter()
        rvHomePhoneApproval.adapter = adapter
    }

    private fun setupAdapter() {
        val softAuditItemViewBinder = PhoneApprovalItemViewBinder(requireContext()) {
            viewModel.childCount > 1
        }

        adapter.register(softAuditItemViewBinder)

        softAuditItemViewBinder.onAllowListener = {
            viewModel.approvalPhone(it, true)
        }
        softAuditItemViewBinder.onForbidListener = {
            viewModel.approvalPhone(it, false)
        }
    }

    private fun subscribeViewModel() {
        viewModel.phoneApprovalList
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isSuccess -> processResultWithStatus(it.data()) { list ->
                            adapter.replaceAll(list)
                        }
                        it.isError -> processErrorWithStatus(it.error())
                        it.isLoading && adapter.isEmpty -> showBlank()
                    }
                })

        viewModel.approval
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> showLoadingDialog()
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            showMessage("审批成功")
                        }
                        it.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                    }
                })
    }

    override fun onRefresh() {
        super.onRefresh()
        viewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        autoRefresh()
    }

}