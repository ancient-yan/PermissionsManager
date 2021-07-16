package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.migration_fragment_confirming.*
import javax.inject.Inject


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 15:02
 */
class MigrationConfirmingFragment : InjectorBaseFragment() {

    @Inject
    lateinit var migrationNavigator: MigrationNavigator

    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    override fun provideLayout() = com.gwchina.lssw.parent.guard.R.layout.migration_fragment_confirming

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        tvMigrationSupportedPhones.setOnClickListener {
            migrationNavigator.openSupportedDeviceListPage()
        }

        tvMigrationWhatNew.setOnClickListener {
            NewFeatureDialog(requireContext()).show()
        }

        btnMigrationBackOldVersion.setOnClickListener {
            showConfirmDialog {
                message = "安装完成后，登录旧版app即可恢复守护"
                positiveText = "安装旧版"
                negativeText = "取消"
                positiveListener = {
                    processBackToOldVersion(migrationViewModel.backToOldVersion())
                }
            }
        }

        tvMigrationStartNewVersion.setOnClickListener {
            showConfirmDialog {
                message = "开启新版后不支持退回旧版本，是否仍要开启7.0版本？"
                positiveText = "确定开启"
                negativeText = "取消"
                positiveListener = {
                    processStartNewVersion(migrationViewModel.startNewVersion())
                }
            }
        }

        tvMigrationContactService.setOnClickListener {
            showCallServiceDialog()
        }

    }

    private fun toDownloadOldParentClient() {
        val uri = Uri.parse("https://imtt.dd.qq.com/16891/apk/F8F1D6FDFF950BC2DF53BB3405BC87B8.apk?fsname=com.gwchina.lssw.parent_6.5.9_6590.apk&csr=1bbd")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun processStartNewVersion(startNewVersion: LiveData<Resource<Any>>) {
        startNewVersion.observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                setMigrationEnded()
                migrationNavigator.openMainPage()
            }?.onError { error ->
                dismissLoadingDialog()
                errorHandler.handleError(error)
            }?.onLoading {
                showLoadingDialog(false)
            }
        })
    }

    private fun processBackToOldVersion(backToOldVersion: LiveData<Resource<Any>>) {
        backToOldVersion.observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                setMigrationEnded()
                toDownloadOldParentClient()
            }?.onError { error ->
                dismissLoadingDialog()
                errorHandler.handleError(error)
            }?.onLoading {
                showLoadingDialog(false)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        migrationViewModel.updateMigrationStep(MigratingData.MIGRATING_STEP_CONFIRMING)
    }

    override fun handleBackPress() = true

}