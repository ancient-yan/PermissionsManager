package com.gwchina.parent.member.presentation.record

import android.arch.lifecycle.Observer
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithoutStatus
import com.android.base.app.ui.submitListResultWithStatus
import com.android.base.utils.BaseUtils.getResources
import com.android.base.utils.android.UnitConverter
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.MemberNavigator
import com.gwchina.parent.member.data.PurchaseRecord
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.utils.setStatusBarLightMode
import kotlinx.android.synthetic.main.member_fragment_record.*
import javax.inject.Inject


/**
 * 购买记录
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 15:10
 */
class PurchaseRecordFragment : InjectorBaseListFragment<PurchaseRecord>() {

    private val recordAdapter by lazy { RecordAdapter(this.context!!) }
    private lateinit var recordViewModel: PurchaseRecordViewModel
    @Inject
    lateinit var memberNavigator: MemberNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置 TranslucentStatus
        SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.transparent))
        activity.setStatusBarLightMode()
        recordViewModel = getViewModel(viewModelFactory)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.member_fragment_record

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupList()
    }

    override fun onResume() {
        super.onResume()
        autoRefresh()
    }

    private fun setupList() {
        rvRecordList.layoutManager = LinearLayoutManager(context)
        rvRecordList.addItemDecoration(ContentItemDecoration())
        setDataManager(recordAdapter)
        rvRecordList.adapter = setupLoadMore(recordAdapter)
    }

    override fun onStartLoad() {
        if (isRefreshing) {
            recordViewModel.loadPurchaseRecord(0)
        } else {
            recordViewModel.loadPurchaseRecord(recordAdapter.dataSize)
        }
    }

    private fun subscribeViewModel() {
        recordViewModel.recordData
                .observe(this, Observer {
                    it ?: return@Observer
                    if (it.loadError) {
                        processListErrorWithoutStatus()
                    } else {
                        submitListResultWithStatus(it.recordList, pager.hasMore(it.lastSize))
                    }
                    loadMoreController.setAutoHiddenWhenNoMore(isEmpty)
                })
    }

    override fun onRetry(state: Int) {
        if (state == EMPTY) {
            exitFragment()
        } else {
            super.onRetry(state)
        }
    }
}

private class ContentItemDecoration : RecyclerView.ItemDecoration() {

    private val mItemVerticalMargin = UnitConverter.dpToPx(10)
    private var dividerHeight: Int = 0
    private val dividerPaint: Paint = Paint()

    init {
        dividerPaint.color = getResources().getColor(R.color.gray_bg)
        dividerHeight = mItemVerticalMargin
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until childCount - 1) {
            val view = parent.getChildAt(i)
            val top = view.bottom.toFloat()
            val bottom = (view.bottom + dividerHeight).toFloat()
            c.drawRect(left.toFloat(), top, right.toFloat(), bottom, dividerPaint)
        }
    }
}