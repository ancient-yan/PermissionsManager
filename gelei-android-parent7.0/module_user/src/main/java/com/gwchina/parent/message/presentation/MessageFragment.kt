package com.gwchina.parent.message.presentation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.kotlin.dip
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.message.data.Message
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.message_fragment.*


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-23 14:48
 */
class MessageFragment : InjectorBaseListFragment<MessageWrapper>() {

    private val viewModel by lazy {
        getViewModel<MessageViewModel>(viewModelFactory)
    }

    private lateinit var clearListItem: MenuItem

    private val messageAdapter by lazy { MessageAdapter(this) }

    override fun provideLayout() = R.layout.message_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //title
        clearListItem = gtlMessageTitle.menu.add(R.string.delete)
                .setIcon(R.drawable.icon_delete_black)
                .setOnMenuItemClickListener {
                    askClearTheMessageList()
                    true
                }

        clearListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        //list
        messageAdapter.onMessageClicked = {
            view.post {
                processMessageClicked(it)
            }
        }

        setDataManager(messageAdapter)
        rvMessageList.addItemDecoration(MarginDecoration(dip(14), dip(17), dip(14), 0))
        rvMessageList.adapter = setupLoadMore(messageAdapter)
        autoRefresh()
    }

    private fun askClearTheMessageList() {
        showConfirmDialog {
            message = "确定清空消息列表吗？"
            positiveListener = {
                clearMessageList()
            }
        }
    }

    override fun onStartLoad() {
        viewModel.loadMessage(if (isLoadingMore) pager.itemCount else 0)
                .doOnSubscribe { showLoadingIfEmpty() }
                .autoDispose()
                .subscribe(
                        {
                            processListResultWithStatus(it.orElse(null))
                            clearListItem.isVisible = !isEmpty
                        },
                        {
                            processListErrorWithStatus(it)
                            clearListItem.isVisible = !isEmpty
                        }
                )
    }

    private fun clearMessageList() {
        viewModel.clearMessage()
                .doOnSubscribe { showRequesting() }
                .autoDispose()
                .subscribe(
                        {
                            messageAdapter.clear()
                            showEmptyLayout()
                            clearListItem.isVisible = !isEmpty
                        },
                        {
                            showContentLayout()
                            errorHandler.handleError(it)
                        }
                )
    }

    private fun processMessageClicked(message: Message) {
        when (message.msg_type) {
            SOFT_APPROVAL -> {
                appRouter.build(RouterPath.Main.PATH)
                        .withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_APP_APPROVAL)
                        .navigation()
            }
        }
    }

}