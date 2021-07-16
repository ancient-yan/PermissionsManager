package com.gwchina.parent.main.presentation.device

import android.arch.lifecycle.Observer
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModel
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.router.RouterPath
import kotlinx.android.synthetic.main.bound_device_fragment_managing.*
import me.drakeet.multitype.register

/**
 * 设备管理
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-16 19:37
 */
class DeviceManagingFragment : InjectorBaseFragment() {

    private val viewModel by lazy {
        getViewModel<DeviceViewModel>(viewModelFactory)
    }

    private val adapter by lazy {
        MultiTypeAdapter(context)
    }

    private lateinit var currentUser: User

    private val deviceUnbindProcessor by lazy {
        DeviceUnbindProcessor(this, viewModel, errorHandler)
    }

    private val childItemViewBinder = ChildItemViewBinder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToViewModel()
    }

    override fun provideLayout() = R.layout.bound_device_fragment_managing

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        rvDeviceManaging.layoutManager = LinearLayoutManager(context)
        setItemBinder()
        setItemMargin()
        rvDeviceManaging.adapter = adapter
    }

    private fun setItemBinder() {
        childItemViewBinder.apply {
            onAddNewDeviceListener = {
                //adapter 中有判断，不是会员则隐藏添加设备按钮
                appRouter.build(RouterPath.Binding.PATH)
                        .withString(RouterPath.Binding.CHILD_USER_ID_KEY, it.child_user_id)
                        .navigation(requireActivity(), RouterPath.Binding.REQUEST_CODE)
            }
            adapter.register(this)
        }
        DeviceItemViewBinder().apply {
            onUnbindDeviceListener = {
                deviceUnbindProcessor.showAskUnbindDeviceDialog(
                        currentUser.findChildByDeviceId(it.device_id)?.child_user_id ?: "",
                        it, createPendingUnbindDeviceName(it))
            }
            adapter.register(this)
        }
    }

    private fun setItemMargin() {
        rvDeviceManaging.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (adapter.getItem(parent.getChildAdapterPosition(view)) is Child) {
                    outRect.top = dip(10)
                } else {
                    outRect.top = 0
                }
            }
        })
    }

    private fun subscribeToViewModel() {
        viewModel.user
                .observe(this, Observer {
                    it ?: return@Observer
                    currentUser = it
                    deviceUnbindProcessor.phoneNumber = it.patriarch.phone ?: ""
                    val childAndDevice = it.extractChildAndDevice()
                    childItemViewBinder.isMember = it.isMember
                    adapter.setDataSource(childAndDevice, true)
                })
    }

}