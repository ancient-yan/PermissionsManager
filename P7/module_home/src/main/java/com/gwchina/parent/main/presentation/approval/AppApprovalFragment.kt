package com.gwchina.parent.main.presentation.approval

import android.arch.lifecycle.Observer
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.SoftApprovalRelation
import com.gwchina.parent.main.presentation.home.card.SoftApprovalItemViewBinder
import com.gwchina.parent.main.presentation.home.card.SoftApprovalRelationItemViewBinder
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import kotlinx.android.synthetic.main.home_fragment_app_approval.*
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 * 应用审批（全部孩子，全部设备）
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-18 11:42
 */
class AppApprovalFragment : InjectorBaseStateFragment() {

    @Inject lateinit var mainNavigator: MainNavigator

    private val adapter by lazy {
        MultiTypeAdapter(context)
    }

    private val viewModel by lazy {
        getViewModel<AppApprovalViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.home_fragment_app_approval

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        rvHomeAppApproval.layoutManager = LinearLayoutManager(context)
        setupAdapter()
        setupItemMargin()
        rvHomeAppApproval.adapter = adapter
    }

    private fun setupItemMargin() {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val separatingColor = colorFromId(R.color.gray_separate)
        val strokeColor = colorFromId(R.color.gray_cutting_line)
        rvHomeAppApproval.addItemDecoration(buildItemDecoration(paint, separatingColor, strokeColor))
    }

    private fun buildItemDecoration(paint: Paint, separatingColor: Int, strokeColor: Int): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val item = adapter.getItem(parent.getChildAdapterPosition(view))
                if (item is SoftApprovalRelation) {
                    outRect.top = dip(28)
                    outRect.bottom = dip(28)
                } else {
                    outRect.top = dip(10)
                    outRect.bottom = dip(10)
                }
            }

            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                val childCount = parent.childCount
                for (index in 0 until childCount) {
                    val childAt = parent.getChildAt(index)
                    if (adapter.getItem(parent.getChildAdapterPosition(childAt)) is SoftApprovalRelation) {
                        paint.color = separatingColor
                        c.drawRect(0F, childAt.top - dip(28F), parent.measuredWidth.toFloat(), childAt.top - dip(18F), paint)
                        paint.color = strokeColor
                        val y = childAt.bottom + dip(18F)
                        c.drawLine(0F, y, parent.measuredWidth.toFloat(), y, paint)
                    }
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter.register(SoftApprovalRelationItemViewBinder())
        val softAuditItemViewBinder = SoftApprovalItemViewBinder(this)
        adapter.register(softAuditItemViewBinder)
        softAuditItemViewBinder.onAllowListener = {
            mainNavigator.openAppApprovalPage(it)
        }
        softAuditItemViewBinder.onForbidListener = {
            viewModel.forbidNewApp(it)
        }
    }

    private fun subscribeViewModel() {
        viewModel.appApprovalList
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isSuccess -> processResultWithStatus(it.data()) { list ->
                            adapter.replaceAll(list)
                        }
                        it.isError -> processErrorWithStatus(it.error())
                        it.isLoading && adapter.isEmpty -> showBlank()
                    }
                })

        viewModel.forbidding
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> showLoadingDialog()
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            showMessage(it.data())
                        }
                        it.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                    }
                })
    }

    override fun onRefresh() {
        super.onRefresh()
        viewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        autoRefresh()
    }

}