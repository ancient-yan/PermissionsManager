package com.gwchina.parent.screenshot.presentation


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.onDebouncedClick
import com.android.base.kotlin.toArrayList
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.screenshot.ScreenshotUtils
import com.gwchina.parent.screenshot.data.ScreenshotData
import com.gwchina.sdk.base.app.InjectorBaseFragment
import kotlinx.android.synthetic.main.home_remote_screentshot_delete_layout.*
import me.drakeet.multitype.register
import timber.log.Timber

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-16 18:26
 *      删除截屏
 */
class DeleteScreenshotFragment : InjectorBaseFragment() {

    private lateinit var adapter: MultiTypeAdapter
    private var isAllSelected = false

    private val viewModel by lazy {
        getViewModelFromActivity<ScreenshotViewModel>(viewModelFactory)
    }

    companion object {
        fun getInstance(data: List<ScreenshotData>): DeleteScreenshotFragment {
            val fragment = DeleteScreenshotFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList("data", data.toArrayList())
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.home_remote_screentshot_delete_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        adapter = MultiTypeAdapter(context)
        val screenshotPicList = arguments?.getParcelableArrayList<ScreenshotData>("data")
        val screenshotPicListMap = screenshotPicList?.let { ScreenshotUtils.parseData(it) }
        screenshotPicListMap?.forEach {
            it.value.forEach {
                it.isSelected = false
            }
        }
        recyclerView.adapter = adapter
        adapter.register(ScreenTitleBinders(this))
        adapter.register(ScreenshotItemBinders(this).apply {
            mOnItemClickListener = {
                Timber.e(hasSelected().toString())
                tvDelete.isEnabled = hasSelected()
                isAllSelected = isAllIdsSelected()
                val drawable = ContextCompat.getDrawable(requireContext(), if (isAllSelected) R.drawable.home_screen_delete_selected else R.drawable.home_screen_delete_normal)
                tvSelect.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            }
        })
        screenshotPicListMap?.apply {
            adapter.replaceAll(parseData(this))
        }
        setListener()

    }

    private fun parseData(map: Map<Int, List<ScreenshotData>>): List<Any> {
        val data = mutableListOf<Any>()
        if (map.isNullOrEmpty()) return emptyList()
        map.entries.forEachIndexed { index, entry ->
            var date = ""
            when (index) {
                0 -> {
                    date = "今天"
                }
                1 -> {
                    date = "昨天"
                }
                2 -> {
                    date = "前天"
                }
            }
            if (entry.value.isNotEmpty()) {
                data.add(date)
                data.add(entry.value)
            }
        }
        return data
    }

    private fun setListener() {
        tvSelect.onDebouncedClick {
            isAllSelected = !isAllSelected
            adapter.items.forEach {
                if (it is String) {
                    return@forEach
                } else {
                    if (it is List<*>) {
                        it.forEach { screenData ->
                            if (screenData is ScreenshotData) {
                                screenData.isSelected = isAllSelected
                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            val drawable = ContextCompat.getDrawable(requireContext(), if (isAllSelected) R.drawable.home_screen_delete_selected else R.drawable.home_screen_delete_normal)
            tvSelect.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            tvDelete.isEnabled = hasSelected()
        }
        tvCancel.onDebouncedClick {
            exitFragment()
        }

        tvDelete.onDebouncedClick {
            viewModel.delPicList(getAllSelectedIds()).observe(this, Observer {
                it?.onLoading {
                    showLoadingDialog()
                }?.onSuccess {
                    dismissLoadingDialog()
                    viewModel.refreshEventCenter.setRefreshEvent(getAllSelectedIds())
                    exitFragment()
                }?.onError { throwable ->
                    dismissLoadingDialog()
                    errorHandler.handleError(throwable)
                }
            })
        }
    }

    private fun getAllSelectedIds(): List<String> {
        val list = mutableListOf<String>()
        adapter.items.filterIsInstance<List<ScreenshotData>>().forEach {
            it.forEach { screenshotData ->
                if (screenshotData.isSelected)
                    list.add(screenshotData.record_id ?: "")
            }
        }
        return list
    }

    private fun isAllIdsSelected(): Boolean {
        var allSelected = true
        run outer@{
            adapter.items.filterIsInstance<List<ScreenshotData>>().forEach {
                it.forEach { screenshotData ->
                    if (!screenshotData.isSelected) {
                        allSelected = false
                        return@outer
                    }
                }
            }
        }
        return allSelected
    }

    private fun hasSelected(): Boolean {
        var hasSelected = false
        run outer@{
            adapter.items.filterIsInstance<List<ScreenshotData>>().forEach {
                it.forEach { screenshotData ->
                    if (screenshotData.isSelected) {
                        hasSelected = true
                        return@outer
                    }
                }
            }
        }
        return hasSelected
    }
}