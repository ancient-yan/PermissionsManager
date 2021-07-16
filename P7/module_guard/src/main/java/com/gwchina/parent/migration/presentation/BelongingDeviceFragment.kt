package com.gwchina.parent.migration.presentation

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.clearComponentDrawable
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.leftDrawable
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.parent.migration.data.MigratingDevice
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.router.SelectedLevelInfo
import com.gwchina.sdk.base.utils.calculateAgeByBirthday
import com.gwchina.sdk.base.utils.splitBirthday
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.migration_fragment_belonging.*
import java.util.*
import javax.inject.Inject

/**
 * 确定设备归属关系
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 15:25
 */
class BelongingDeviceFragment : InjectorBaseFragment() {

    @Inject lateinit var migrationNavigator: MigrationNavigator

    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    private var currentDevice: MigratingDevice? = null
    private var currentChild: UploadingChild? = null
    private var currentId: String? = null

    private val childAdapter by lazy {
        ChildAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.migration_fragment_belonging

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        rvMigrationChild.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom = dip(20)
            }
        })

        rvMigrationChild.adapter = childAdapter

        childAdapter.onSelectedChildListener = {
            currentDevice?.let { device ->
                currentChild = it
                val (year, month, day) = splitBirthday(it.birthday)
                val id = UUID.randomUUID().toString()
                currentId = id
                migrationNavigator.openGuardLevelForGetLevelInfo(id, device.device_type?:"", calculateAgeByBirthday(year, month, day))
            }
        }

        tvMigrationContinue.setOnClickListener {
            migrationNavigator.openChildInfoCollectingPage()
        }

        showBelongingDevice()
    }

    override fun onResume() {
        super.onResume()
        migrationViewModel.updateMigrationStep(MigratingData.MIGRATING_STEP_BELONGING)
    }

    private fun subscribeViewModel() {
        migrationViewModel.childList.observe(this, Observer {
            childAdapter.setDataSource(it ?: emptyList(), true)
            if ((it?.size ?: 0 < 3)) {
                tvMigrationContinue.setTextColor(colorFromId(R.color.green_level1))
                tvMigrationContinue.setText(R.string.add_child)
                tvMigrationContinue.leftDrawable(R.drawable.icon_add)
            } else {
                tvMigrationContinue.setTextColor(colorFromId(R.color.gray_level2))
                tvMigrationContinue.text = getString(R.string.max_child_count_tips)
                tvMigrationContinue.clearComponentDrawable()
            }
        })
    }

    private fun showBelongingDevice() {
        val nextUnBelongedDevice = migrationViewModel.nextUnBelongedDevice
        if (nextUnBelongedDevice == null) {
            processMigratingResult(migrationViewModel.submitChildDeviceList())
        } else {
            currentDevice = nextUnBelongedDevice
            tvMigrationWhoseDevice.text = "这台【%s】是谁的呢".format(nextUnBelongedDevice.device_name)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RouterPath.GuardLevel.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedLevelInfo = data.getParcelableExtra<SelectedLevelInfo>(RouterPath.GuardLevel.SELECTED_LEVEL_INFO)
            val child = currentChild
            val device = currentDevice
            if (selectedLevelInfo != null && child != null && device != null && selectedLevelInfo.sessionId == currentId) {
                migrationViewModel.deviceBelongingToChild(device, selectedLevelInfo.level, selectedLevelInfo.guardItem, child)
                showBelongingDevice()
            }
        }
    }

    private fun processMigratingResult(submitChildDeviceList: LiveData<Resource<Any>>) {
        submitChildDeviceList.observe(this, Observer { resource ->
            resource?.onError {
                dismissLoadingDialog()
                showRetryDialog()
                errorHandler.handleError(it)
            }?.onLoading {
                showLoadingDialog(false)
            }?.onSuccess {
                dismissLoadingDialog()
                setMigrationEnded()
                migrationNavigator.openMainPage()
                activity?.finish()
            }
        })
    }

    private fun showRetryDialog() {
        showConfirmDialog {
            message = "数据提交失败，请重试。"
            positiveText = "重试"
            noNegative()
            positiveListener = {
                processMigratingResult(migrationViewModel.submitChildDeviceList())
            }
        }?.setCancelable(false)
    }

}