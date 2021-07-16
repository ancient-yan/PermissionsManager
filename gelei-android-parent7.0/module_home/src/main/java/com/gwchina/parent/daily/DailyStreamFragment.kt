package com.gwchina.parent.daily

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.gone
import com.android.base.kotlin.onDebouncedClick
import com.android.base.kotlin.visible
import com.android.base.rx.bindLifecycle
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.adapter.DailyCommentAdapter
import com.gwchina.parent.daily.data.Comment
import com.gwchina.parent.daily.data.CommentResult
import com.gwchina.parent.daily.data.LifeRecord
import com.gwchina.parent.daily.data.Reply
import com.gwchina.parent.daily.presentation.DailyViewModel
import com.gwchina.parent.daily.presentation.DiaryTextBuilder
import com.gwchina.parent.daily.widget.ReplyView
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.daily_main_layout.*
import kotlinx.android.synthetic.main.daily_stream_empty_layout.*
import javax.inject.Inject

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-07 19:58
 */
class DailyStreamFragment : InjectorBaseListFragment<LifeRecord>() {

    @Inject
    lateinit var dailyNavigator: DailyNavigator
    private lateinit var rightView: View
    private var currentKey: String = ""

    private val viewModel by lazy {
        getViewModel<DailyViewModel>(viewModelFactory)
    }

    private val dailyAdapter by lazy {
        DailyCommentAdapter(requireContext()) { position, dataList ->
            dailyNavigator.openPicPrePage(position, dataList)
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.daily_main_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setDataManager(dailyAdapter)
        recyclerView.adapter = setupLoadMore(dailyAdapter)

        dailyAdapter.onCommentClickListener = object : DailyCommentAdapter.OnCommentClickListener {
            override fun onItemLongClickListener(position: Int, dailyId: String?) {
                showConfirmDialog {
                    this.message = resources.getString(R.string.confirm_delete_diary_tips)
                    this.negativeListener = {
                        it.dismiss()
                    }
                    this.positiveListener = {
                        it.dismiss()
                        dailyId?.let { id -> viewModel.deleteDaily(id, position) }
                    }
                }
            }

            override fun onCommentClick(position: Int, lifeRecordId: String?, view: View) {
                replyView.setEditTextHint(context?.getString(R.string.daily_say_something), lifeRecordId, ReplyView.ADD_COMMENT, "", "")
                setCacheByKey(lifeRecordId)
                replyView.showSoftInput(view, replyView, recyclerView)
                replyView.setOnSendClickListener { content, recordId, replyType, commentId, _ ->
                    viewModel.commentDaily(recordId, content, commentId, position, type = replyType, cacheKey = currentKey)
                }
            }

            override fun onReplyComment(lifeRecordId: String?, position: Int, commentPosition: Int, reply_comment_id: String?, replyName: String?, view: View) {
                replyView.setEditTextHint(context?.getString(R.string.daily_reply4_mask, replyName), lifeRecordId, ReplyView.REPLY_COMMENT, reply_comment_id, replyName)
                setCacheByKey(reply_comment_id)
                replyView.showSoftInput(view, replyView, recyclerView)
                replyView.setOnSendClickListener { content, recordId, replyType, commentId, _ -> viewModel.commentDaily(recordId, content, commentId, position, commentPosition, type = replyType, cacheKey = currentKey) }
            }

            override fun onReplyItemClick(lifeRecordId: String?, position: Int, commentPosition: Int, replyPosition: Int, reply_comment_id: String?, replyName: String?, view: View) {
                replyView.setEditTextHint(context?.getString(R.string.daily_reply4_mask, replyName), lifeRecordId, ReplyView.REPLY_REPLY, reply_comment_id, replyName)
                setCacheByKey(reply_comment_id)
                replyView.showSoftInput(view, replyView, recyclerView)
                replyView.setOnSendClickListener { content, recordId, replyType, commentId, _ -> viewModel.commentDaily(recordId, content, commentId, position, commentPosition, replyPosition, replyType, cacheKey = currentKey) }
            }

            override fun onNewMessageClick() {
                dailyNavigator.openMessageListPage()
            }
        }

        replyView.observeSoftKeyboard(activity)

        val guardReportItemProvider = DailyItemProvider.findSelf(gwTitleLayout.menu)
        guardReportItemProvider.setOnMenuClickListener(showImageView = true, showTextView = false) {
            dailyNavigator.openMessageListPage()
        }
        rightView = guardReportItemProvider.getMenuItemView()
        gwTitleLayout.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        publishDaily.onDebouncedClick {
            dailyNavigator.openPublishDailyPage()
        }

        viewModel.dailyCommentRecord.observe(this, Observer {
            it?.onLoading {
                showLoadingDialog()
            }?.onSuccess { commentData ->
                val itemPosition = commentData!!.itemPosition
                val commentResult = commentData.commentResult
                val commentPosition = commentData.commentPosition
                val cacheKey = commentData.cacheKey
                replyView.clearCacheBykey(cacheKey)
                when (commentData.type) {
                    ReplyView.ADD_COMMENT -> {
                        val comment = createComment(commentResult)
                        dailyAdapter.addComment(comment, itemPosition)
                    }
                    ReplyView.REPLY_COMMENT, ReplyView.REPLY_REPLY -> {
                        val reply = createReply(commentResult)
                        dailyAdapter.addReply(reply, itemPosition, commentPosition)
                    }
                }
                replyView.resetEditText()
                dismissLoadingDialog()
            }?.onError { err ->
                errorHandler.handleError(err)
                dismissLoadingDialog()
            }
        })

        viewModel.dailyDelete.observe(this, Observer {
            it?.onLoading {
                showLoadingDialog()
            }?.onSuccess { position ->
                position?.let { it1 -> dailyAdapter.itemRemoved(it1) }
                if (dailyAdapter.dataSize == 0) {
                    showEmptyLayout()
                }
                if (dailyAdapter.dataSize == 1 && dailyAdapter.getItem(0).isFooterType) {
                    dailyAdapter.itemRemoved(0)
                    showEmptyLayout()
                }
                dismissLoadingDialog()
            }?.onError { err ->
                errorHandler.handleError(err)
                dismissLoadingDialog()
            }
        })
        if (!viewModel.appDataSource.userLogined()) {
            showEmptyLayout()
            return
        }
        autoRefresh()
        //发布成功自动刷新
        viewModel.refreshEventCenter.getRefreshDailyEvent.observe(this, Observer {
            if (it == DailyPublishFragment.REFRESH_DAILY_CODE) {
                autoRefresh()
            }
        })
    }

    private fun setCacheByKey(key: String?) {
        if (key != null) {
            currentKey = key
            replyView.setKey(currentKey)
            val cache = replyView.getCacheByKey(currentKey)
            if (!cache.isNullOrEmpty()) {
                replyView.setEditText(cache)
            } else {
                replyView.setEditText("")
            }
        }
    }

    override fun onStartLoad() {
        super.onStartLoad()
        viewModel.dailyRecord(if (isRefreshing) 0 else pager.itemCount)
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    if (it.message_count > 0) {
                        dailyAdapter.notifyDataChange(it.message_count)
                        it.life_record?.add(0, LifeRecord())
                    }
                    processListResultWithStatus(it.life_record)
                }, {
                    processListErrorWithStatus(it)
                })
    }

    override fun showContentLayout() {
        super.showContentLayout()
        rightView.visible()
        recyclerView.visible()
        noDataLayout.gone()
    }

    //7.0.1需求 最后一条添加
    override fun loadMoreCompleted(hasMore: Boolean) {
        super.loadMoreCompleted(hasMore)
        if (!dailyAdapter.items.isNullOrEmpty()&&!hasMore) {
            if (!dailyAdapter.items.any { it.isFooterType }) {
                dailyAdapter.add(LifeRecord(isFooterType = true))
            }
        }
    }

    override fun showEmptyLayout() {
        rightView.gone()
        recyclerView.gone()
        noDataLayout.visible()
        val dtb = DiaryTextBuilder(context!!)
        val content = dtb.createExpandableContent(context!!.resources.getString(R.string.daily_initialized_content), R.color.red_level1)
        tv_no_data_content.text = content[0]
    }

    override fun showErrorLayout() {
        if (!viewModel.appDataSource.userLogined()) {
            showEmptyLayout()
        } else {
            super.showErrorLayout()
        }
    }

    //创建评论
    private fun createComment(commentResult: CommentResult?): Comment {
        val comment = Comment()
        commentResult?.apply {
            comment.comment_id = comment_id
            comment.comment_user_id = comment_user_id
            comment.comment_user_name = comment_user_name
            comment.comment_user_head = comment_user_head
            comment.content = content
            comment.reply = null
        }
        return comment
    }

    //创建回复
    private fun createReply(commentResult: CommentResult?): Reply {
        val reply = Reply()
        commentResult?.apply {
            reply.comment_id = comment_id
            reply.comment_user_id = comment_user_id
            reply.reply_user_id = reply_user_id
            reply.comment_user_name = comment_user_name
            reply.reply_user_name = reply_user_name
            reply.content = content
        }
        return reply
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RouterPath.Account.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            autoRefresh()
        }
    }

}