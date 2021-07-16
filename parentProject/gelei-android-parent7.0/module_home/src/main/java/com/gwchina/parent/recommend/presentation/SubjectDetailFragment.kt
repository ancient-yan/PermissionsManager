package com.gwchina.parent.recommend.presentation

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.drawableFromId
import com.android.base.rx.bindLifecycle
import com.android.base.utils.android.TintUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.Navigator
import com.gwchina.parent.recommend.data.SoftItem
import com.gwchina.parent.recommend.data.SubjectInfo
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.utils.setStatusBarDarkMode
import kotlinx.android.synthetic.main.main_fragment_subject_detail.*
import javax.inject.Inject

/**
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-15 09:49
 */
class SubjectDetailFragment : InjectorBaseStateFragment() {

    companion object {
        private const val SOFTWARE_KEY = "subject_key"
        fun newInstance(subjectId: String) = SubjectDetailFragment().apply {
            arguments = Bundle().apply {
                putString(SOFTWARE_KEY, subjectId)
            }
        }
    }

    @Inject
    lateinit var navigator: Navigator
    private lateinit var adapter: RecommendListAdapter
    private lateinit var viewModel: RecommendViewModel

    private val navigationIcon by lazy {
        TintUtils.tint(drawableFromId(R.drawable.icon_back)?.mutate(), colorFromId(R.color.white))
    }

    override fun provideLayout() = R.layout.main_fragment_subject_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModelFromActivity(viewModelFactory)
        subscribeViewModel()
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        gtlSubjectTitle.toolbar.navigationIcon = navigationIcon
        rvSubjectAppList.layoutManager = android.support.v7.widget.LinearLayoutManager(context)
        rvSubjectAppList.adapter = initAdapter()
        showLoadingLayout()
        loadSubjectDetail()
    }

    private fun subscribeViewModel() {
        viewModel.installEvent
                .observe(this, Observer {
                    adapter.items?.indexOfFirst { item ->
                        it == item.bundle_id
                    }?.let {
                        adapter.notifyItemChanged(it)
                    }
                })
    }

    private fun loadSubjectDetail() {
        val subjectId = arguments?.getString(SOFTWARE_KEY) ?: ""
        viewModel.doLoadSubjectDetail(subjectId)
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({ it ->
                    processResultWithStatus(it) {
                        bindTopData(it.rec_subject_info)
                        adapter.setDataSource(it.rec_subject_soft_list, true)
                    }
                }, {
                    processErrorWithStatus(it)
                })
    }



    private fun initAdapter(): RecyclerView.Adapter<*>? {
        adapter = RecommendListAdapter(this)
        adapter.onInstallListener = {
            showInstallingTips {
                doInstallForChild(it)
            }
        }
        adapter.onItemClickListener = {
            navigator.showAppDetail(it)
        }
        return adapter
    }

    @SuppressLint("CheckResult")
    private fun doInstallForChild(softItem: SoftItem) {
        showLoadingDialog(false)
        viewModel.installForChild(softItem)
                .doOnTerminate { dismissLoadingDialog() }
                .subscribe(
                        { /*no op*/ },
                        {
                            errorHandler.handleError(it)
                        }
                )
    }

    private fun bindTopData(subjectInfo: SubjectInfo?) {
        ImageLoaderFactory.getImageLoader().display(ivSubjectCover, subjectInfo?.subject_banner_url)
        tvSubjectTitle.text = subjectInfo?.subject_name
        if (!subjectInfo?.subject_details.isNullOrEmpty()) {
            val details = subjectInfo?.subject_details?.replace("\n","<br/>")
            tvSubjectDescribe.text = Html.fromHtml(details)
        }
    }

    override fun onRefresh() {
        loadSubjectDetail()
    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setTranslucentSystemUi(activity, true, false)
        activity.setStatusBarDarkMode()
    }
}