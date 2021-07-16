package com.gwchina.parent.net.presentation.netManagement.fragment

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
import com.android.base.kotlin.ifNonNull
import com.android.base.rx.bindLifecycle
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.NetGuardNavigator
import com.gwchina.parent.net.common.NetEventCenter
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.parent.net.presentation.netManagement.adapter.SiteInfoAdapter
import com.gwchina.parent.net.presentation.netManagement.viewmodel.NetManagementViewModel
import com.gwchina.parent.net.widget.AddUrlDialog
import com.gwchina.parent.net.widget.AddUrlDialogBuilder
import com.gwchina.parent.net.widget.showAddUrlDialog
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.net_fragment_management_list.*
import javax.inject.Inject

class URLManagementFragment : InjectorBaseListFragment<SiteInfo>() {

    companion object {
        const val BLACKLIST = "blacklist"
        const val WHITELIST = "whitelist"
    }

    private var type: String = BLACKLIST
    private val adapter: SiteInfoAdapter by lazy { SiteInfoAdapter(this, if (type == BLACKLIST) SiteInfoAdapter.BLACKLIST else SiteInfoAdapter.WHITELIST) }

    private val viewmodel: NetManagementViewModel by lazy { getViewModel<NetManagementViewModel>(viewModelFactory) }

    @Inject
    lateinit var netGuardNavigator: NetGuardNavigator

    @Inject
    lateinit var netEventCenter: NetEventCenter

    override fun provideLayout(): Any? = R.layout.net_fragment_management_list

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {

        arguments?.let {
            it.getString("type").ifNonNull { type = this }
        }

        initRecyclerView()
        stateLayoutConfig.setStateIcon(StateLayoutConfig.EMPTY, R.drawable.net_img_danger)
        if (type == BLACKLIST) {
            net_tv_tips.text = getString(R.string.blacklist_tips)
            net_btn_add.text = getString(R.string.add_blacklist)
            stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.blacklist_empty_tips))
            stateLayoutConfig.setStateAction(StateLayoutConfig.EMPTY, getString(R.string.add_blacklist))
        } else {
            net_tv_tips.text = getString(R.string.whitelist_tips)
            net_btn_add.text = getString(R.string.add_whitelist)
            stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.whitelist_empty_tips))
            stateLayoutConfig.setStateAction(StateLayoutConfig.EMPTY, getString(R.string.add_whitelist))
        }

        net_btn_add.setOnClickListener {
            showAddUrlDialog()
        }

        netEventCenter.netDeleteTypeEvent()
                .observe(this, Observer {
                    it?.let { type ->
                        if (type == this@URLManagementFragment.type)
                            netGuardNavigator.openDelUrlPage(type = type)
                    }
                })


        netEventCenter.urlListRefreshEvent()
                .observe(this, Observer {
                    it?.let { type ->
                        if (type == this@URLManagementFragment.type)
                            autoRefresh()
                    }

                })
        autoRefresh()
    }


    private fun initRecyclerView() {
        rvSiteInfoList.layoutManager = LinearLayoutManager(context)
        setDataManager(adapter)
        rvSiteInfoList.adapter = setupLoadMore(adapter)
        loadMoreController.setAutoHiddenWhenNoMore(true)
        adapter.onClickListener = {
            val position = it.tag as Int
            val item = adapter.getItem(position)
            showAddUrlDialog(this.requireContext()) {
                url = item.url
                urlName = item.url_name
                type = if (this@URLManagementFragment.type == BLACKLIST)
                    AddUrlDialogBuilder.update_blacklist_url
                else
                    AddUrlDialogBuilder.update_whitelist_url
                positiveListener = { dialog, url, urlName ->
                    addOrUpdateUrl(dialog, url, urlName, item.rule_id, position)
                }
            }
        }
    }

    override fun onRetry(state: Int) {
        if (state == StateLayoutConfig.EMPTY) {
            showAddUrlDialog()
        } else {
            super.onRetry(state)
        }
    }

    private fun showAddUrlDialog() {
        if(type == BLACKLIST) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBBLACK)
        } else {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBWHITE)
        }
        showAddUrlDialog(this.requireContext()) {
            type = if (this@URLManagementFragment.type == BLACKLIST)
                AddUrlDialogBuilder.add_blacklist_url
            else
                AddUrlDialogBuilder.add_whitelist_url
            positiveListener = { dialog, url, urlName ->
                addOrUpdateUrl(dialog, url, urlName)
            }
        }
    }

    private fun addOrUpdateUrl(dialog: AddUrlDialog, url: String, urlName: String, rule_id: String = "", position: Int = 0) {
        if(type == BLACKLIST) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBBLACK_FINISH)
        } else {
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBWHITE_FINISH)
        }
        val listType: String =
                if (dialog.type == AddUrlDialogBuilder.add_blacklist_url
                        || dialog.type == AddUrlDialogBuilder.update_blacklist_url)
                    "0"
                else
                    "1"

        when (dialog.type) {
            AddUrlDialogBuilder.add_blacklist_url,
            AddUrlDialogBuilder.add_whitelist_url -> {
                viewmodel.addUrl("2", url, urlName, listType)
                        .observe(this, Observer {
                            it?.onSuccess {
                                dismissLoadingDialog()
                                dialog.dismiss()
                                autoRefresh()
                            }?.onError {
                                dismissLoadingDialog()
                                dialog.cannotAddedTips(true, errorHandler.createMessage(it).toString())
                                dialog.setSubmitEnable(false)
                            }?.onLoading {
                                showLoadingDialog(false)
                                dialog.cannotAddedTips(false)
                            }
                        })
            }
            AddUrlDialogBuilder.update_blacklist_url,
            AddUrlDialogBuilder.update_whitelist_url -> {
                viewmodel.updateUrl("2", url, urlName, rule_id, listType)
                        .observe(this, Observer {
                            it?.onSuccess {
                                dismissLoadingDialog()
                                dialog.dismiss()
                                val item = adapter.getItem(position)
                                item.url = url
                                item.url_name = urlName
                                val removeAt = adapter.items.removeAt(position)
                                adapter.items.add(0, removeAt)
                                adapter.notifyDataSetChanged()
                            }?.onError {
                                dismissLoadingDialog()
                                errorHandler.handleError(it)
                            }?.onLoading {
                                showLoadingDialog(false)
                            }
                        })
            }
        }

    }

    override fun onStartLoad() {

        viewmodel.getSiteList(if (type == BLACKLIST) "0" else "1", if (isRefreshing) 0 else pager.itemCount)
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it.orElse(null))

                    if (pager.itemCount > 0) {
                        if (net_btn_add.visibility != View.VISIBLE) {
                            net_btn_add.visibility = View.VISIBLE
                        }
                    }else{
                        net_btn_add.visibility = View.GONE
                    }

                }, {
                    processListErrorWithStatus(it)
                })
    }
}