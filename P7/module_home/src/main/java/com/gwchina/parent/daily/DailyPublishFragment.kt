package com.gwchina.parent.daily

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.invisible
import com.android.base.kotlin.onDebouncedClick
import com.android.base.kotlin.textWatcher
import com.android.base.kotlin.visible
import com.android.base.permission.AutoPermissionRequester
import com.android.base.utils.android.SoftKeyboardUtils
import com.android.sdk.mediaselector.MediaSelector
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.adapter.AddPicAdapter
import com.gwchina.parent.daily.presentation.DailyViewModel
import com.gwchina.parent.daily.widget.ChooseChildrenDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.yanzhenjie.permission.runtime.Permission
import kotlinx.android.synthetic.main.daily_publish_fragment_layout.*
import javax.inject.Inject

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-08 14:34
 * 写日记
 */
class DailyPublishFragment : InjectorBaseStateFragment() {

    companion object {
        const val REFRESH_DAILY_CODE = 0x100
        const val IS_FROM_HOMEPAGE_KEY = "isFromHomePage"

        fun newInstance(isFromHomePage: Boolean = false): DailyPublishFragment {
            val fragment = DailyPublishFragment()
            val bundle = Bundle()
            bundle.putBoolean(IS_FROM_HOMEPAGE_KEY, isFromHomePage)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var isFromHomePage = false

    @Inject
    lateinit var dailyNavigator: DailyNavigator
    private var hasText = false
    private var hasPic = false
    private val viewModel by lazy {
        getViewModel<DailyViewModel>(viewModelFactory)
    }

    lateinit var adapter: AddPicAdapter
    private var picPathList = arrayListOf<String>()
    private var childIdList = arrayListOf<String>()

    var dialog: ChooseChildrenDialog? = null

    val ms = MediaSelector(this, object : MediaSelector.Callback {

        override fun onTakeMultiPictureSuccess(pictures: MutableList<String>?) {
            if (picPathList.contains(""))
                picPathList.remove("")
            pictures?.let { picPathList.addAll(it) }
            hasPic = picPathList.size > 0
            adapter.notifyData()
            enableRightButton()
        }
    })

    override fun provideLayout(): Any? {
        return R.layout.daily_publish_fragment_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        isFromHomePage = arguments?.getBoolean(IS_FROM_HOMEPAGE_KEY) ?: false
        val mWriteChildrenLL = writeChildrenLL
        val mWriteChildrenTv = writeChildrenTv
        tvCancel.setOnClickListener {
            if (hasPic || hasText) {
                showConfirmDialog {
                    this.message = resources.getString(R.string.content_not_save_tips)
                    this.negativeListener = {
                        it.dismiss()
                    }
                    this.positiveListener = {
                        it.dismiss()
                        SoftKeyboardUtils.hideSoftInput(et_content)
                        activity?.onBackPressed()
                    }
                }
            } else {
                SoftKeyboardUtils.hideSoftInput(et_content)
                activity?.onBackPressed()
            }
        }
        val childList = viewModel.getChildData()?.filter { it.boundDevice() }
        if (childList != null && childList.isNotEmpty()) {
            childIdList.clear()
            if (childList.size == 1) {
                childIdList.add(childList[0].child_user_id)
                showSoftKeyboard()
            } else {
                context?.let { context ->
                    if (dialog == null)
                        dialog = ChooseChildrenDialog(context, childList, {
                            childIdList.clear()
                            val children = it.mapIndexed { _, i ->
                                childIdList.add(childList[i].child_user_id)
                                childList[i].nick_name
                            }.joinToString(separator = "、@", prefix = "写给：@")
                            mWriteChildrenLL.visibility = View.VISIBLE
                            mWriteChildrenTv.text = children
                            showSoftKeyboard()
                        }, {
                            activity?.onBackPressed()
                        })
                }
                dialog?.show()
            }
        } else {
            ToastUtils.showLong("数据异常，孩子id不存在")
        }

        writeChildrenTv.onDebouncedClick {
            dialog?.show()
        }

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = AddPicAdapter(context!!, picPathList)
        recyclerView.adapter = adapter
        val preImgList = arrayListOf<String>()
        adapter.onItemClickListener = object : AddPicAdapter.OnItemClickListener {
            override fun onPreViewItemClick(position: Int, picList: ArrayList<String>) {
                preImgList.clear()
                preImgList.addAll(picList)
                if (preImgList.contains(""))
                    preImgList.remove("")
                dailyNavigator.openPicPrePage(position, preImgList)
                SoftKeyboardUtils.hideSoftInput(activity)
            }

            override fun addPic() {
                AutoPermissionRequester.with(this@DailyPublishFragment)
                        .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                        .onGranted {
                            ms.takeMultiPicture(false, (9 - picPathList.size + 1))
                        }.request()
            }

            override fun onDelete(position: Int) {
                hasPic = if (picPathList.contains("")) {
                    picPathList.size > 1
                } else {
                    picPathList.size > 0
                }
                enableRightButton()
            }
        }

        dailyExampleTv.onDebouncedClick {
            dailyNavigator.openExamplePage()
        }

        et_content.textWatcher {
            afterTextChanged {
                val count = et_content.text.toString().trimEnd().length
                val color: Int
                when {
                    count >= 500 -> {
                        color = ContextCompat.getColor(context!!, R.color.red_level1)
                        arrive_word_num.visible()
                    }
                    count > 0 -> {
                        color = ContextCompat.getColor(context!!, R.color.green_main)
                        arrive_word_num.invisible()
                    }
                    else -> {
                        arrive_word_num.invisible()
                        color = ContextCompat.getColor(context!!, R.color.gray_level3)
                    }
                }
                currentWordNumTv.setTextColor(color)
                currentWordNumTv.text = count.toString()
                hasText = count > 0
                enableRightButton()
            }
        }

        viewModel.dailyPublish.observe(this, Observer {
            it?.onSuccess {
                ToastUtils.showLong(getString(R.string.daily_publish_success))
                //发布事件
                viewModel.refreshEventCenter.setRefreshDailyEvent(REFRESH_DAILY_CODE)
                if (isFromHomePage) {
                    dailyNavigator.replacePublishDailyPage()
                } else {
                    activity?.onBackPressed()
                }
            }
            it?.onLoading {
                showLoadingDialog()
            }
            it?.onError { throwable ->
                dismissLoadingDialog()
                errorHandler.handleError(throwable)
            }
        })

        publishDailyTv.onDebouncedClick {
            publishDaily()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ms.onActivityResult(requestCode, resultCode, data)
    }

    private fun enableRightButton() {
        if (hasText || hasPic) {
            publishDailyTv.isEnabled = true
            publishDailyTv.background = context?.let { ContextCompat.getDrawable(it, R.drawable.daily_publish_button_enable) }
        } else {
            publishDailyTv.isEnabled = false
            publishDailyTv.background = context?.let { ContextCompat.getDrawable(it, R.drawable.daily_publish_button_disable) }
        }
    }

    private fun publishDaily() {
        val content = et_content.text.toString().trimEnd()
        if (picPathList.contains("")) picPathList.remove("")
        viewModel.dailyPublish.postValue(Resource.loading())
        if (picPathList.size > 0) {
            viewModel.dailyRepository.compressMulti(picPathList).map {
                it.map { file -> file.absolutePath }
            }.subscribe({
                viewModel.dailyRepository.doUpload(it.toMutableList()).subscribe(
                        { fileInfoList -> viewModel.publishDaily(fileInfoList.map { fileInfo -> fileInfo.url }, childIdList, content) },
                        { throwable ->
                            viewModel.dailyPublish.postValue(Resource.error(throwable))
                        }
                )
            }, {
                viewModel.dailyPublish.postValue(Resource.error(it))
            })
        } else {
            viewModel.publishDaily(picPathList, childIdList, content)
        }
    }

    //强制显示软键盘
    private fun showSoftKeyboard() {
        et_content.isFocusable = true
        et_content.isFocusableInTouchMode = true
        et_content.requestFocus()
        //打开软键盘
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}