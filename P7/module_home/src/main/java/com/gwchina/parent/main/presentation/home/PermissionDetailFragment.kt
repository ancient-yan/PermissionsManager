package com.gwchina.parent.main.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.fragment.BaseFragment
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.models.PermissionDetail
import kotlinx.android.synthetic.main.home_permission_detail_item_layout.*
import kotlinx.android.synthetic.main.home_permission_detail_layout.*
import me.drakeet.multitype.register

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2020-02-06 14:59
 * 权限详情
 */
class PermissionDetailFragment : BaseFragment() {

    private var permissions: ArrayList<PermissionDetail>? = null

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter(requireContext())
    }

    override fun provideLayout(): Any? {
        return R.layout.home_permission_detail_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        permissions = arguments?.getParcelableArrayList<PermissionDetail>("data")
        val binder = PermissionDetailBinder()
        rvPermissionDetail.adapter = adapter
        adapter.register(binder)
        permissions?.let {
            adapter.replaceAll(it as List<PermissionDetail>)
        }
        val childName = AppContext.appDataSource().user().currentChild?.nick_name
        val currentDeviceName = AppContext.appDataSource().user().currentDevice?.device_name
        tvTips.text = getString(R.string.home_permission_detail_tips_mask, childName, currentDeviceName, permissions?.count { it.state == 0 })
    }

    class PermissionDetailBinder : ItemViewBinder<PermissionDetail, KtViewHolder>() {
        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
            return KtViewHolder(inflater.inflate(R.layout.home_permission_detail_item_layout, parent, false))
        }

        override fun onBindViewHolder(holder: KtViewHolder, item: PermissionDetail) {
            holder.tvPermissionName.text = item.privilege_name
            //state:1是 0否
            holder.iv_icon.setImageResource(if (item.state == 1) R.drawable.icon_status_normal else R.drawable.icon_status_disable)
        }

    }
}