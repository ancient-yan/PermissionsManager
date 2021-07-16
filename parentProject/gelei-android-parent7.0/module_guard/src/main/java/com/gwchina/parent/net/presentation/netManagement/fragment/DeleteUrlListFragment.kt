package com.gwchina.parent.net.presentation.netManagement.fragment

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.ifNonNull
import com.android.base.rx.bindLifecycle
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.ColorUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.common.NetEventCenter
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.parent.net.presentation.netManagement.adapter.SiteInfoAdapter
import com.gwchina.parent.net.presentation.netManagement.viewmodel.NetManagementViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.net_fragment_delete_url_list.*
import javax.inject.Inject

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-18 14:13
 *      Desc : 删除地址
 */

class DeleteUrlListFragment : InjectorBaseListFragment<SiteInfo>() {

    private var type: String = URLManagementFragment.BLACKLIST
    @Inject
    lateinit var netEventCenter: NetEventCenter
    private val viewModel by lazy { getViewModel<NetManagementViewModel>(viewModelFactory) }

    private val adapter: SiteInfoAdapter by lazy { SiteInfoAdapter(this, if (type == URLManagementFragment.BLACKLIST) SiteInfoAdapter.BLACKLIST else SiteInfoAdapter.WHITELIST, true) }

    override fun provideLayout(): Any? = R.layout.net_fragment_delete_url_list

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            it.getString("type").ifNonNull { type = this }
        }

        if (type == URLManagementFragment.BLACKLIST) {
            net_gtl_del_Title.setTitle(getString(R.string.del_blacklist))
        } else {
            net_gtl_del_Title.setTitle(getString(R.string.del_whitelist))
        }

        net_btn_cancel.setOnClickListener {
            exitFragment(false)
        }

        net_btn_del.setOnClickListener {
            showConfirmDialog()
        }

        adapter.selectListID.observe(this, Observer {
            it?.let { selects ->
                net_btn_del.isEnabled = selects.isNotEmpty()
            }
        })

        initRecyclerView()
        autoRefresh()
    }

    private fun initRecyclerView() {
        net_recy_url.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        net_recy_url.adapter = setupLoadMore(adapter)
        loadMoreController.setAutoHiddenWhenNoMore(true)
        setRefreshEnable(false)
    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setStatusBarColor(activity, ColorUtils.getColor(R.color.white))
    }

    private fun deleteUrls() {
        if(type == URLManagementFragment.BLACKLIST) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBBLACK_DELETE_FINISH)
        } else {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBWHITE_DELETE_FINISH)
        }
        viewModel.deleteUrls(adapter.selectIdList)
                .observe(this, Observer {
                    it?.onSuccess {
                        dismissLoadingDialog()
                        adapter.removeSelectItem()
                        netEventCenter.notifyUrlListNeedRefresh(type)
                        net_btn_del.isEnabled = false
                        showMessage(R.string.deleted)
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }?.onLoading {
                        showLoadingDialog(false)
                    }
                })
    }

    private fun showConfirmDialog() {
        showConfirmDialog {
            message = getString(if (type == URLManagementFragment.BLACKLIST) R.string.confirm_delete_black_hint else R.string.confirm_delete_white_hint)
            positiveListener = {
                deleteUrls()
            }
        }
    }

    override fun onStartLoad() {
        viewModel.getSiteList(if (type == URLManagementFragment.BLACKLIST) "0" else "1", if (isRefreshing) 0 else pager.itemCount)
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it.orElse(null))
                }, {
                    processListErrorWithStatus(it)
                })
    }

}