package com.gwchina.parent.daily.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.data.Comment
import com.gwchina.parent.daily.data.Reply
import com.gwchina.parent.daily.widget.DailyReplyLayout
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.foldText
import kotlinx.android.synthetic.main.daily_adapter_reply_item_layout.view.*

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-06 15:58
 * 日记流-每条评论中的回复的适配器
 */
class ReplyAdapter(var context: Context, private var dataList: List<Comment>, var onCommentClick: (Comment, Int, String?, View) -> Unit) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var userId = AppContext.appDataSource().user().patriarch.user_id

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ReplyViewHolder {
        val itemView = inflater.inflate(R.layout.daily_adapter_reply_item_layout, p0, false)
        return ReplyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, @SuppressLint("RecyclerView") itemPosition: Int) {
        holder.commentAuthorTv.text = if (userId == dataList[itemPosition].comment_user_id) context.getString(R.string.daily_me) else dataList[itemPosition].comment_user_name.foldText(10)
        holder.commentContentTv.text = dataList[itemPosition].content
        if (dataList[itemPosition].reply.isNullOrEmpty()) {
            holder.dailyReplyLayout.gone()
        } else {
            holder.dailyReplyLayout.visible()
            holder.dailyReplyLayout.notifyCommentListChanged(dataList[itemPosition].reply, userId)
            holder.dailyReplyLayout.onCommentItemClickListener = object : DailyReplyLayout.OnCommentItemClickListener {
                override fun onItemClick(replierPerson: Reply, position: Int) {
                    onReplyItemClickListener?.onItemClick(itemPosition, position, replierPerson, holder.dailyReplyLayout.getChildAt(position))
                }
            }
        }
        holder.commentContentTv.setOnClickListener {
            onCommentClick(dataList[itemPosition], itemPosition, dataList[itemPosition].comment_id, it)
        }
    }

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentAuthorTv: TextView = itemView.tv_comment_author
        val commentContentTv: TextView = itemView.tv_comment_content
        val dailyReplyLayout: DailyReplyLayout = itemView.dailyReplyLayout
    }

    var onReplyItemClickListener: OnReplyItemClickListener? = null

    interface OnReplyItemClickListener {
        fun onItemClick(parentPosition: Int, position: Int, replierPerson: Reply, view: View)
    }
}

