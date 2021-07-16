package com.gwchina.parent.message.presentation

import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.message.data.Message
import kotlinx.android.synthetic.main.message_item.*


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-24 15:40
 */
internal class MessageAdapter(
        private val host: Fragment
) : SimpleRecyclerAdapter<MessageWrapper>(host.requireContext()) {

    var onMessageClicked: ((message: Message) -> Unit)? = null

    private val itemClickListener = View.OnClickListener {
        onMessageClicked?.invoke((it.tag as MessageWrapper).message)
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.message_item

    override fun bind(viewHolder: KtViewHolder, item: MessageWrapper) {
        val message = item.message

        ImageLoaderFactory.getImageLoader().display(host,viewHolder.ivMessageType, message.msg_icon)
        viewHolder.tvMessageContent.text = message.msg_content
        viewHolder.tvMessageType.text = message.msg_title
        viewHolder.tvMessageTime.text = item.timeDesc
        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(itemClickListener)
    }

}