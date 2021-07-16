package com.gwchina.sdk.base.widget.member

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.fragment.BaseDialogFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.utils.android.ResourceUtils
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.EXPIRE_RETAIN_FLAG_NOT_SET
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.push.PushManager
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import kotlinx.android.synthetic.main.dialog_member_expired.*
import kotlinx.android.synthetic.main.dialog_member_expired_item_child.*
import kotlinx.android.synthetic.main.dialog_member_expired_item_device.*
import me.drakeet.multitype.register

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 11:17
 */
class MemberExpiredDialog : BaseDialogFragment() {

    companion object {

        fun showMemberExpired(fragmentManager: FragmentManager): MemberExpiredDialog? {
            var baseDialogFragment: MemberExpiredDialog?=null
            val fragment = fragmentManager.findFragmentByTag(MemberExpiredDialog::class.java.name)
            if (fragment == null) {
                baseDialogFragment = MemberExpiredDialog().apply { show(fragmentManager, MemberExpiredDialog::class.java.name) }
            }
            return baseDialogFragment
        }
    }

    private val viewModel by lazy {
        getViewModel<MemberViewModel>(MemberViewModelFactory())
    }

    private val adapter by lazy {
        MultiTypeAdapter(requireContext())
    }

    private val appRouter = AppContext.appRouter()
    private val errorHandler = AppContext.errorHandler()

    private val deviceItemViewBinder = DeviceItemViewBinder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog_Common)
        subscribeViewModel()

        //禁用推送跳转
        PushManager.getInstance().setPushProcessorEnable(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isCancelable = false
    }

    override fun provideLayout() = R.layout.dialog_member_expired

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.setWindowAnimations(R.style.Style_Anim_Bottom_In)
        }
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        rvDialogMemberDevices.layoutManager = LinearLayoutManager(context)
        setItemMargin()
        adapter.register(ChildItemViewBinder())
        adapter.register(deviceItemViewBinder)
        rvDialogMemberDevices.adapter = adapter

        //确认选择的设备
        tvDialogMemberConfirm.setOnClickListener {
            viewModel.setRetainedDevice(deviceItemViewBinder.selectedDeviceId)
        }

        //去续费
        tvDialogMemberRenewalFee.setOnClickListener {
            AppContext.appRouter().build(RouterPath.MemberCenter.PATH)
                    .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                    .navigation()
        }
    }

    private fun setItemMargin() {
        rvDialogMemberDevices.addItemDecoration(object : RecyclerView.ItemDecoration() {

            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorFromId(R.color.gray_cutting_line)
            }

            private val offset = dip(10)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (adapter.getItem(parent.getChildAdapterPosition(view)) is Child) {
                    outRect.top = offset
                } else {
                    outRect.top = 0
                }
            }

            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDrawOver(c, parent, state)
                for (index in 0 until parent.childCount) {
                    val view = parent.getChildAt(index)
                    if (adapter.getItem(parent.getChildAdapterPosition(view)) is Child) {
                        c.drawRect(view.left.toFloat(), (view.top - offset).toFloat(), view.right.toFloat(), view.top.toFloat(), paint)
                    }
                }
            }
        })
    }

    private fun subscribeViewModel() {
        viewModel.user
                .observe(this, Observer {
                    showChildrenAndDevicesChecked(it ?: User.NOT_LOGIN)
                })

        viewModel.settingRetainedDeviceStatus
                .observe(this, Observer {
                    it?.onError { error ->
                        dismissLoadingDialog()
                        errorHandler.handleError(error)
                    }?.onLoading {
                        showLoadingDialog(false)
                    }?.onSuccess {
                        dismissLoadingDialog()
                        dismissAllowingStateLoss()
                        MemberExpiredForceChooseTask.markFlag()
                        appRouter.build(RouterPath.Main.PATH).withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_HOME).navigation()
                    }
                })
    }

    private fun showChildrenAndDevicesChecked(user: User) {
        if (!shouldShowMemberExpiredForceChooseDialog(user) && user.deviceCount() > 1 && EXPIRE_RETAIN_FLAG_NOT_SET == user.member_info?.expire_setting_retain_flag) {
            dismissAllowingStateLoss()
        } else {
            val extractChildAndDevice = user.extractChildAndDevice(true)
            if (deviceItemViewBinder.selectedDeviceId.isEmpty() || user.findDevice(deviceItemViewBinder.selectedDeviceId) == null) {
                deviceItemViewBinder.selectedDeviceId = user.currentChildDeviceId ?: ""
            }
            adapter.setDataSource(extractChildAndDevice, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //启用推送跳转
        PushManager.getInstance().setPushProcessorEnable(true)
        //完成过期提示任务
        MemberExpiredForceChooseTask.finish()
    }

}

private class ChildItemViewBinder : ItemViewBinder<Child, KtViewHolder>() {

    override fun onBindViewHolder(holder: KtViewHolder, item: Child) {
        holder.tvDialogMemberItemChildName.text = ResourceUtils.getString(R.string.devices_of_x_mask, item.nick_name)
        holder.ivDialogMemberItemChildAvatar.setImageResource(mapChildAvatarSmall(item.sex))
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            KtViewHolder(inflater.inflate(R.layout.dialog_member_expired_item_child, parent, false))

}

internal class DeviceItemViewBinder : ItemViewBinder<Device, KtViewHolder>() {

    var selectedDeviceId: String = ""

    private val _itemClickListener = View.OnClickListener {
        selectedDeviceId = (it.tag as Device).device_id
        adapter.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            KtViewHolder(inflater.inflate(R.layout.dialog_member_expired_item_device, parent, false))

    override fun onBindViewHolder(holder: KtViewHolder, item: Device) {
        holder.tvDialogMemberDeviceName.text = item.device_name
        if (item.index > 0) {
            holder.tvDialogMemberDeviceIndex.visible()
            holder.tvDialogMemberDeviceIndex.text = item.index.toString()
        } else {
            holder.tvDialogMemberDeviceIndex.invisible()
        }
        if (selectedDeviceId == item.device_id) {
            holder.ivDialogMemberCheckStatus.isSelected = true
            holder.itemView.setOnClickListener(null)
        } else {
            holder.ivDialogMemberCheckStatus.isSelected = false
            holder.itemView.tag = item
            holder.itemView.setOnClickListener(_itemClickListener)
        }
    }

}