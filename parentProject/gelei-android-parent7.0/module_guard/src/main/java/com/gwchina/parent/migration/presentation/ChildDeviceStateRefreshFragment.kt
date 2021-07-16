package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.parent.migration.data.MigratingDevice
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.data.api.isAndroid
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.dialog.showCustomDialog
import kotlinx.android.synthetic.main.migration_fragment_device_state.*
import kotlinx.android.synthetic.main.migration_item_device.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-09-05 15:04
 */
class ChildDeviceStateRefreshFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var migrationNavigator: MigrationNavigator

    private val deviceAdapter by lazy {
        DeviceAdapter(requireContext())
    }

    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        migrationViewModel.loadMigrationDeviceList()
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.migration_fragment_device_state

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        stateLayoutConfig.disableOperationWhenRequesting(true)

        tvMigrationIosDescriptionFileGuide.setOnClickListener {
            migrationNavigator.openIOSDescriptionFileUpdatingGuide()
        }

        btnMigrationRefresh.setOnClickListener {
            migrationViewModel.loadMigrationDeviceList()
        }

        tvMigrationStartNewVersion.setOnClickListener {
            showConfirmDialog {
                message = "开启新版后不支持退回旧版本，是否仍要开启7.0版本？"
                positiveText = "确定开启"
                negativeText = "取消"
                positiveListener = {
                    doStartNewVersionChecked()
                }
            }
        }

        tvMigrationContactService.setOnClickListener {
            showCallServiceDialog()
        }

        view.post {
            setupScrollListener()
        }

        rvMigrationDevice.addItemDecoration(MarginDecoration(0, dip(10), 0, 0))
        rvMigrationDevice.adapter = deviceAdapter
        deviceAdapter.onItemClickListener = { isAndroidDevice ->
            showCustomDialog {
                layoutId = R.layout.migration_tips_layout
                noNegative()
                onLayoutPrepared = {
                    val tvMessage = it.findViewById<TextView>(R.id.tv_message)
                    val migrationGuide = it.findViewById<TextView>(R.id.tvMigrationIosDescriptionFileGuide)
                    if (isAndroidDevice) {
                        tvMessage.text = getString(R.string.migration_android_tip)
                        migrationGuide.gone()
                    } else {
                        tvMessage.text = getString(R.string.migration_ios_tip)
                        migrationGuide.visible()
                    }
                    migrationGuide.setOnClickListener { migrationNavigator.openIOSDescriptionFileUpdatingGuide() }
                }

                positiveId = R.string.i_got_it
                positiveListener = {
                    it.dismiss()
                }
            }?.setCanceledOnTouchOutside(false)
        }
    }

    private fun setupScrollListener() {
        if (llMigrationHeader == null && svMigrationContent == null) {
            return
        }
        val scrollOffset = dip(125)/*margin top*/ - llMigrationHeader.measuredHeight
        val colorDrawable = ColorDrawable(Color.WHITE)
        llMigrationHeader.background = colorDrawable

        svMigrationContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY >= scrollOffset) {
                colorDrawable.alpha = 255
            } else {
                colorDrawable.alpha = (scrollY.toFloat() / scrollOffset * 255).toInt()
            }
        })
    }

    private fun doStartNewVersionChecked() {
        val list = migrationViewModel.supportedDevice.value?.get()
        if (list.isNullOrEmpty()) {
            return
        }
        //全部未升级
        if (list.all { device -> !device.canDoMigrating() }) {
            processGiveAllDeviceAndStartNewVersion(migrationViewModel.startNewVersion())
        } else {//部分未升级
            migrationNavigator.openMigrationGuidePage()
        }
    }

    private fun processGiveAllDeviceAndStartNewVersion(startNewVersion: LiveData<Resource<Any>>) {
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

    override fun onBackPressed() = true

    override fun onRefresh() {
        super.onRefresh()
        migrationViewModel.loadMigrationDeviceList()
    }

    override fun onResume() {
        super.onResume()
        migrationViewModel.updateMigrationStep(MigratingData.MIGRATING_STEP_DEVICE_STATE)
    }

    private fun subscribeViewModel() {
        migrationViewModel.supportedDevice.observe(this, Observer {
            it?.onSuccess { list ->
                dismissLoadingDialog()
                showContentLayout()
                deviceAdapter.replaceAll(list)
                if (!list.isNullOrEmpty() && list.all { device -> device.canDoMigrating() }) {
                    migrationNavigator.openMigrationGuidePage()
                }
            }?.onLoading {
                if (deviceAdapter.isEmpty) {
                    showLoadingDialog(false)
                } else {
                    showRequesting()
                }
            }?.onError { err ->
                dismissLoadingDialog()
                if (deviceAdapter.isEmpty) {
                    processErrorWithStatus(err)
                } else {
                    errorHandler.handleError(err)
                }
            }
        })
    }

}

private class DeviceAdapter(context: Context) : SimpleRecyclerAdapter<MigratingDevice>(context) {

    var onItemClickListener: ((isAndroidDevice: Boolean) -> Unit)? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.migration_item_device

    override fun bind(viewHolder: KtViewHolder, item: MigratingDevice) {
        viewHolder.tvMigrationItemDeviceName.text = item.device_name

        if (isAndroid(item.device_type)) {
            if (isFlagPositive(item.child_device_upgrade_flag)) {
                viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_normal)
                viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.gray_level2))
                viewHolder.tvMigrationItemDeviceState.text = "版本已升级，可开启守护"
            } else {
                viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_disable)
                viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.red_level1))
                viewHolder.tvMigrationItemDeviceState.text = "版本未升级，不能开启守护"
            }
        } else {
            if (isFlagPositive(item.child_device_upgrade_flag)) { //已升级
                if (isFlagPositive(item.ios_description_flag)) {
                    viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_normal)
                    viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.gray_level2))
                    viewHolder.tvMigrationItemDeviceState.text = "版本已升级，描述文件已更新，可开启守护"
                } else {
                    viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_disable)
                    viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.red_level1))
                    viewHolder.tvMigrationItemDeviceState.text = "描述文件未更新，不能开启守护"
                }
            } else {
                if (isFlagPositive(item.ios_description_flag)) {
                    viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_normal)
                    viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.gray_level2))
                    viewHolder.tvMigrationItemDeviceState.text = "版本未升级，描述文件已更新，可开启守护"
                } else {
                    viewHolder.ivMigrationItemState.setImageResource(R.drawable.icon_status_disable)
                    viewHolder.tvMigrationItemDeviceState.setTextColor(mContext.colorFromId(R.color.red_level1))
                    viewHolder.tvMigrationItemDeviceState.text = "版本未升级，描述文件未更新，不能开启守护"
                }
            }
        }
        viewHolder.itemView.setOnClickListener {
            if (!isFlagPositive(item.child_device_upgrade_flag)) {
                onItemClickListener?.invoke(isAndroid(item.device_type))
            }
        }
    }

}