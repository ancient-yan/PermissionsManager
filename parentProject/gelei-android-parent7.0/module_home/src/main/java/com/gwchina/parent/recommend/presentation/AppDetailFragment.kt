package com.gwchina.parent.recommend.presentation

import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.data.SoftItem
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.utils.formatDecimal
import com.gwchina.sdk.base.utils.setStatusBarLightMode
import kotlinx.android.synthetic.main.reco_fragment_detail.*

/**
 * 应用推荐详情
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 17:31
 */
class AppDetailFragment : InjectorBaseFragment() {

    companion object {
        private const val SOFTWARE_KEY = "software_key"

        fun newInstance(softItem: SoftItem) = AppDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(SOFTWARE_KEY, softItem)
            }
        }
    }

    private lateinit var softItem: SoftItem
    private lateinit var viewModel: RecommendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        softItem = arguments?.getParcelable(SOFTWARE_KEY) ?: throw IllegalStateException("没有 SoftItem")

        viewModel = getViewModelFromActivity(viewModelFactory)
    }

    override fun provideLayout() = R.layout.reco_fragment_detail

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        ImageLoaderFactory.getImageLoader().display(this, ivRecoSoftIcon, softItem.soft_icon)
        tvRecoSoftName.text = softItem.soft_name
        tvRecoSoftPhrase.text = softItem.rec_phrase
        tvRecoReasonOfRecommendation.text = softItem.rec_desc
        rbRecoRating.star = softItem.rec_level
        tvRecoScoring.text = getString(R.string.scoring_mask, formatDecimal(softItem.rec_level.toFloat(), 1))

        setInstallStatus()
    }

    private fun setInstallStatus() {
        when {
            softItem.isInstalled() -> {
                btnRecoInstall.isEnabled = false
                btnRecoInstall.setText(R.string.installed)
            }
            softItem.isInstalling() -> {
                btnRecoInstall.isEnabled = false
                btnRecoInstall.setText(R.string.installing)
            }
            else -> {
                btnRecoInstall.isEnabled = true
                btnRecoInstall.setText(R.string.install_for_child)
                btnRecoInstall.setOnClickListener {
                    showInstallingTips {
                        doInstall()
                    }
                }
            }
        }
    }

    private fun doInstall() {
        showLoadingDialog(false)

        viewModel.installForChild(softItem)
                .doOnTerminate { dismissLoadingDialog() }
                .subscribe(
                        {
                            setInstallStatus()
                        },
                        {
                            errorHandler.handleError(it)
                        }
                )
    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setTranslucentSystemUi(activity, false, false)
        activity.setStatusBarLightMode()
    }

}