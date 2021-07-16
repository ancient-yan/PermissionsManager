package com.gwchina.parent.apps.presentation.guide

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccessWithData
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.models.isIOS
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.SuperviseModeSynchronizer
import kotlinx.android.synthetic.main.apps_fragment_start_guide.*
import javax.inject.Inject

/**
 * 应用守护引导界面
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-16 00:46
 */
class AppsGuardGuidFragment : InjectorBaseFragment(), Injectable {

    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator

    private val guideFlowViewModel by lazy {
        getViewModelFromActivity<GuideFlowViewModel>(viewModelFactory)
    }

    private lateinit var superviseModeSynchronizer: SuperviseModeSynchronizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        superviseModeSynchronizer = SuperviseModeSynchronizer(guideFlowViewModel.childUserId, guideFlowViewModel.childDeviceId)
        subscribeViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.apps_fragment_start_guide, container, false)
    }

    private fun toSetAppsGuardChecked() {
        val device = guideFlowViewModel.device
        if (device.isIOS()) {
            superviseModeSynchronizer.startSync()
        } else {
            toSetAppsGuard()
        }
    }

    private fun toSetAppsGuard() {
        appGuardNavigator.openGuideFlowSetDisableAppPage()
    }

    private fun subscribeViewModel() {
        superviseModeSynchronizer.state.observe(this, Observer {
            it?.onSuccessWithData { result ->
                dismissLoadingDialog()
                if (result) {
                    toSetAppsGuard()
                } else {
                    appGuardNavigator.openIosSuperviseModePage()
                }
            }?.onLoading {
                showLoadingDialog(false)
            }?.onError { err ->
                dismissLoadingDialog()
                errorHandler.handleError(err)
            }
        })

        guideFlowViewModel.user.observe(this, Observer {
            if (it?.vipRule?.home_app_enter == FLAG_POSITIVE_ACTION) {
                tvAppsGuideTips.visible()
                btnAppsStart.setText(R.string.start_setup)
                btnAppsStart.setOnClickListener {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_BTN_START)
                    toSetAppsGuardChecked()
                }
            } else {
                tvAppsGuideTips.gone()
                btnAppsStart.text = getString(R.string.open_vip_to_experience_mask, guideFlowViewModel.user.value?.vipRule?.home_app_enter_minimum_level)
                btnAppsStart.setOnClickListener {
                    appRouter.build(RouterPath.MemberCenter.PATH)
                            .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                            .navigation()
                }
            }
        })
    }

}