package com.gwchina.parent.recommend.presentation

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.adapter.pager.ViewPageInfo
import com.android.base.adapter.pager.ViewPageStateFragmentAdapter
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.Navigator
import com.gwchina.parent.recommend.data.GradeInfo
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.data.models.deviceCount
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.utils.setStatusBarLightMode
import com.gwchina.sdk.base.widget.dialog.showListDialog
import kotlinx.android.synthetic.main.reco_fragment_main.*
import javax.inject.Inject

/**
 * 应用推荐主界面
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:16
 */
class RecommendFragment : InjectorBaseStateFragment() {

    companion object {
        fun newInstance(recommendId: String) = RecommendFragment().apply {
            arguments = Bundle().apply {
                putString(RouterPath.Recommend.RECOMMEND_ID_KEY, recommendId)
            }
        }
    }

    @Inject
    lateinit var navigator: Navigator

    private lateinit var recommendId: String
    private lateinit var viewModel: RecommendViewModel
    private var switchGradeItem: SwitchGradeItemProvider? = null
    private var subjectPresenter: SubjectPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModelFromActivity(viewModelFactory)
        subscribeViewModel()

        recommendId = if (savedInstanceState != null) {
            savedInstanceState.getString(RouterPath.Recommend.RECOMMEND_ID_KEY) ?: ""
        } else {
            arguments?.getString(RouterPath.Recommend.RECOMMEND_ID_KEY) ?: ""
        }

    }

    override fun provideLayout() = R.layout.reco_fragment_main

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(RouterPath.Recommend.RECOMMEND_ID_KEY, recommendId)
        super.onSaveInstanceState(outState)
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        subjectPresenter = SubjectPresenter(this)
        loadRecommendList()
    }

    private fun loadRecommendList() {
        viewModel.loadRecommendList(recommendId)
    }

    override fun onRefresh() {
        loadRecommendList()
    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setTranslucentSystemUi(activity, false, false)
        activity.setStatusBarLightMode()
    }

    private fun setupViewPager(data: List<Category>?) {
        val viewPageStateFragmentAdapter = ViewPageStateFragmentAdapter(childFragmentManager, context)
        val list = mutableListOf<ViewPageInfo>()

        data?.run {
            this.forEach {
                list.add(ViewPageInfo(it.name, RecommendListFragment::class.java, Bundle().apply {
                    putString(RecommendListFragment.LIST_ID_KEY, it.code)
                }))
            }
        }

        viewPageStateFragmentAdapter.setDataSource(list)
        vpRecommendContent.adapter = viewPageStateFragmentAdapter
        tblRecommendTab.setViewPager(vpRecommendContent)

        if (list.isEmpty()) {
            showEmptyLayout()
        } else {
            showContentLayout()
        }
    }

    private fun setupMenu(data: List<GradeInfo>?) {
        if (switchGradeItem == null) {
            gtlRecoTitle.toolbar.inflateMenu(R.menu.reco_menu_switch_grade)
            switchGradeItem = SwitchGradeItemProvider.findSelf(gtlRecoTitle.menu)
        }
        switchGradeItem?.showTitle(data?.find { it.rec_group_id == recommendId }?.group_name)
        switchGradeItem?.setOnMenuClickListener {
            data?.let {
                showSelectGradeDialog(it)
            }
        }
    }

    private fun showSelectGradeDialog(list: List<GradeInfo>) {
        showListDialog {
            items = list.map { it.group_name }.toTypedArray()
            selectedPosition = list.indexOfFirst { it.rec_group_id == recommendId }
            positiveListener = { position, title ->
                switchGradeItem?.showTitle(title.toString())
                recommendId = list[position].rec_group_id
                loadRecommendList()
            }
        }
    }

    private fun subscribeViewModel() {
        viewModel.categories
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isError -> processErrorWithStatus(it.error())
                        it.isLoading -> showLoadingLayout()
                        it.isSuccess -> setupViewPager(it.orElse(null))
                    }
                })

        viewModel.grades
                .observe(this, Observer {
                    setupMenu(it)
                })

        viewModel.user
                .observe(this, Observer {
                    //一个孩子多个设备或者多个孩子多个设备都需要显示设备名称
                    if (it.deviceCount() > 1) {
                        val currentChild = it?.currentChild ?: return@Observer
                        val currentDevice = it.currentDevice ?: return@Observer
                        tvRecommendDeviceFlag.visible()
                        tvRecommendDeviceFlag.text = getString(R.string.install_to_device_mask, currentChild.nick_name, currentDevice.device_name)
                    } else {
                        tvRecommendDeviceFlag.gone()
                    }
                })

        viewModel.subject
                .observe(this, Observer {
                    subjectPresenter?.showSubjects(it)
                })
    }

}