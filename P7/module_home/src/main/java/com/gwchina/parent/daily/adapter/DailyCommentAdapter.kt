package com.gwchina.parent.daily.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils.getString
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.data.Comment
import com.gwchina.parent.daily.data.LifeRecord
import com.gwchina.parent.daily.data.Reply
import com.gwchina.parent.daily.presentation.DiaryTextBuilder
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.to2BitText
import com.lzy.ninegrid.NineGridView
import kotlinx.android.synthetic.main.daily_adapter_item_layout.*
import kotlinx.android.synthetic.main.daily_adapter_item_layout.iv_icon
import kotlinx.android.synthetic.main.daily_new_message_layout.*
import kotlinx.android.synthetic.main.daily_stream_empty_layout.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-14 11:41
 */
class DailyCommentAdapter(private val context: Context, private var onImageItemClick: (Int, ArrayList<String>) -> Unit) : RecyclerAdapter<LifeRecord, KtViewHolder>(context) {
    companion object {
        private const val NORMAL_CONTENT_TYPE = 1
        private const val HEAD_MESSAGE_TYPE = 2
        private const val FOOT_TYPE = 3
    }

    private var messageCount = 0

    fun notifyDataChange(messageCount: Int) {
        this.messageCount = messageCount
        notifyDataSetChanged()
    }

    fun addComment(comment: Comment, position: Int) {
        if (getItem(position).comment == null)
            getItem(position).comment = mutableListOf()
        getItem(position).comment!!.add(comment)
        notifyItemChanged(position)
    }

    fun addReply(reply: Reply, itemPosition: Int, commentPosition: Int) {
        if (getItem(itemPosition).comment == null)
            getItem(itemPosition).comment = mutableListOf()
        if (getItem(itemPosition).comment!![commentPosition].reply == null)
            getItem(itemPosition).comment!![commentPosition].reply = mutableListOf()
        getItem(itemPosition).comment!![commentPosition].reply!!.add(reply)
        notifyItemChanged(itemPosition)
    }


    fun itemRemoved(position: Int) {
        removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataSize)
    }

    override fun getItemViewType(position: Int): Int {
        if (messageCount > 0) {
            if (position == 0)
                return HEAD_MESSAGE_TYPE
        }
        if (getItem(position).isFooterType) {
            return FOOT_TYPE
        }
        return NORMAL_CONTENT_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        return when (viewType) {
            HEAD_MESSAGE_TYPE -> {
                val view = LayoutInflater.from(mContext).inflate(R.layout.daily_new_message_layout, parent, false)
                HeaderViewHolder(view)
            }
            NORMAL_CONTENT_TYPE -> {
                val view = LayoutInflater.from(mContext).inflate(R.layout.daily_adapter_item_layout, parent, false)
                ContentViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(mContext).inflate(R.layout.daily_stream_empty_layout, parent, false)
                FooterViewHolder(view)
            }
        }
    }

    class ContentViewHolder(itemView: View) : KtViewHolder(itemView)
    class HeaderViewHolder(itemView: View) : KtViewHolder(itemView)
    class FooterViewHolder(itemView: View) : KtViewHolder(itemView)

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        when (viewHolder) {
            is ContentViewHolder -> convert(viewHolder, getItem(position), position)
            is HeaderViewHolder -> {
                viewHolder.messageCount.text = String.format(context.resources.getString(R.string.daily_receive_message_mask), messageCount)
                viewHolder.messageCount.setOnClickListener {
                    this.messageCount = 0
                    removeAt(0)
                    notifyDataSetChanged()
                    onCommentClickListener?.onNewMessageClick()
                }
            }
            else -> {
                viewHolder.itemView.visible()
                val dtb = DiaryTextBuilder(context)
                val content = dtb.createExpandableContent(context.resources.getString(R.string.daily_initialized_content), R.color.red_level1)
                viewHolder.tv_no_data_content.text = content[0]
            }
        }
    }

    private var dtb = DiaryTextBuilder(context)

    init {
        NineGridView.setImageLoader(GlideImageLoader())
    }

    private val diaryCalendar: Calendar = Calendar.getInstance()

    private fun convert(viewHolder: KtViewHolder, item: LifeRecord, itemPosition: Int) {
        val currentCalendar: Calendar = Calendar.getInstance()
        viewHolder.itemView.setOnLongClickListener {
            onCommentClickListener?.onItemLongClickListener(itemPosition, item.life_record_id)
            true
        }
        val currentTimeInMillis = currentCalendar.timeInMillis
        val currentYear = currentCalendar.get(Calendar.YEAR)
        diaryCalendar.timeInMillis = item.create_time
        val diaryYear = diaryCalendar.get(Calendar.YEAR)//日记生成年
        val diaryMonth = diaryCalendar.get(Calendar.MONTH) + 1//日记生成月
        val diaryDay = diaryCalendar.get(Calendar.DAY_OF_MONTH)//日记生成日
        val diaryHour = diaryCalendar.get(Calendar.HOUR_OF_DAY)//日记生成时
        val diaryMinute = diaryCalendar.get(Calendar.MINUTE)//日记生成分

        val result = setDateOfTitle(item, currentTimeInMillis, viewHolder, diaryYear, diaryMonth, diaryDay)

        if (itemPosition == 0 || (itemPosition == 1 && getItemViewType(0) == HEAD_MESSAGE_TYPE)) {
            viewHolder.yearTopLine.invisible()
            viewHolder.line_top.invisible()
            if (diaryYear == currentYear) {
                viewHolder.ll_year.gone()
            } else {
                viewHolder.ll_year.visible()
            }
        } else {
            viewHolder.yearTopLine.visible()
            viewHolder.line_top.visible()
            diaryCalendar.timeInMillis = getItem(itemPosition - 1).create_time
            val lastYear = diaryCalendar.get(Calendar.YEAR)//日记生成年
            val lastMonth = diaryCalendar.get(Calendar.MONTH) + 1//日记生成月
            val lastDay = diaryCalendar.get(Calendar.DAY_OF_MONTH)//日记生成日
            if (diaryYear == lastYear || diaryYear == currentYear) {
                viewHolder.ll_year.gone()
            } else {
                viewHolder.ll_year.visible()
            }
            //同一天的
            val preItemResult = isYesterday(getItem(itemPosition - 1).create_time, currentTimeInMillis)
            if (lastMonth == diaryMonth && lastDay == diaryDay && lastYear == diaryYear) {
                viewHolder.tv_day.invisible()
                viewHolder.tv_month.invisible()
                viewHolder.iv_icon.invisible()
            } else {
                viewHolder.tv_day.visible()
                viewHolder.tv_month.visible()
                viewHolder.iv_icon.visible()
            }
            //今天和昨天
            if ((preItemResult == -1 && result == -1) || (preItemResult == 0 && result == 0)) {
                viewHolder.tv_month.invisible()
            }
        }
        when {
            //一分钟内
            currentTimeInMillis - item.create_time < 1000 * 60 -> viewHolder.tv_time.text = context.getString(R.string.just)
            //一小时内
            currentTimeInMillis - item.create_time < 1000 * 60 * 60 -> viewHolder.tv_time.text = getString(R.string.x_minutes_ago_mask, (currentTimeInMillis - item.create_time) / (1000 * 60))
            //24小时内
            currentTimeInMillis - item.create_time < 60 * 60 * 1000 * 24 -> viewHolder.tv_time.text = getString(R.string.x_hours_ago_mask, (currentTimeInMillis - item.create_time) / (1000 * 60 * 60))
            else -> viewHolder.tv_time.text = String.format("%s:%s", diaryHour.to2BitText(), diaryMinute.to2BitText())

        }
        if ((AppContext.appDataSource().user().childList != null && AppContext.appDataSource().user().childList!!.size > 1) || (item.receiver != null && item.receiver.size > 1)) {
            viewHolder.children.visible()
            viewHolder.children.text = item.receiver?.map { it.nick_name }?.joinToString(separator = "、@", prefix = "写给：@")
        } else {
            viewHolder.children.gone()
        }
        viewHolder.tv_content.tag = itemPosition
        if (item.content == null || item.content.isEmpty()) {
            viewHolder.ll_content.gone()
        } else {
            viewHolder.ll_content.visible()
            var color = -1
            when (result) {
                -1 -> {
                    color = R.color.red_level1
                }
                0 -> {
                    color = R.color.blue_level2
                }
                1 -> {
                    color = R.color.green_main
                }
            }
            viewHolder.tv_content.tag = itemPosition
            viewHolder.tv_content.text = dtb.createExpandableContent(item.content, color)[item.isExpand]
            dtb.onExpandContent = { p ->
                getItem(p).isExpand = 1
                notifyItemChanged(p)
            }
            dtb.onCollapseContent = { p ->
                getItem(p).isExpand = 0
                notifyItemChanged(p)
            }
            viewHolder.tv_content.movementMethod = LinkMovementMethod.getInstance()
        }

        if (item.life_photo == null || item.life_photo.isEmpty()) {
            viewHolder.nineGridView.gone()
        } else {
            viewHolder.nineGridView.visible()
            viewHolder.nineGridView.setAdapter(ClickNineGridViewAdapter(context, ClickNineGridViewAdapter.generateImageInfoList(item.life_photo.map { it.photo_path }), onImageItemClick))
        }

        viewHolder.rl_comment.setOnClickListener {
            onCommentClickListener?.onCommentClick(itemPosition, item.life_record_id, it)
        }
        val commentRecyclerView = viewHolder.rv_comment
        if (item.comment == null || item.comment!!.isEmpty()) {
            commentRecyclerView.gone()
        } else {
            commentRecyclerView.visibility = View.VISIBLE
            commentRecyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = item.comment?.let {
                ReplyAdapter(context, it) { comment, p, commentId, view ->
                    onCommentClickListener?.onReplyComment(item.life_record_id, itemPosition, p, commentId, comment.comment_user_name, view)
                }
            }
            commentRecyclerView.adapter = adapter
            adapter?.onReplyItemClickListener = object : ReplyAdapter.OnReplyItemClickListener {
                override fun onItemClick(parentPosition: Int, position: Int, replierPerson: Reply, view: View) {
                    onCommentClickListener?.onReplyItemClick(item.life_record_id, itemPosition, parentPosition, position, replierPerson.comment_id, replierPerson.comment_user_name, view)
                }
            }
        }
    }

    private fun setDateOfTitle(item: LifeRecord, currentTimeInMillis: Long, viewHolder: KtViewHolder, diaryYear: Int, diaryMonth: Int, diaryDay: Int): Int {
        val result = isYesterday(item.create_time, currentTimeInMillis)
        viewHolder.tv_day.visible()
        viewHolder.iv_icon.visible()
        when (result) {
            -1 -> {
                viewHolder.ll_year.gone()
                viewHolder.line_top.invisible()
                viewHolder.tv_month.invisible()
                viewHolder.tv_day.text = getString(R.string.today)
                viewHolder.iv_icon.setImageResource(R.drawable.daily_red_gradient_shape)
                viewHolder.iv_left.setImageResource(R.drawable.diary_icon_red_left)
            }
            0 -> {
                viewHolder.ll_year.gone()
                viewHolder.line_top.invisible()
                viewHolder.tv_month.invisible()
                viewHolder.tv_day.text = getString(R.string.yesterday)
                viewHolder.iv_icon.setImageResource(R.drawable.daily_blue_gradient_shape)
                viewHolder.iv_left.setImageResource(R.drawable.diary_icon_blue_left)
            }
            1 -> {
                viewHolder.ll_year.visible()
                viewHolder.line_top.visible()
                viewHolder.tv_month.visible()
                viewHolder.tv_year.text = "$diaryYear"
                viewHolder.tv_month.text = String.format("%d月", diaryMonth)
                viewHolder.tv_day.text = "$diaryDay"
                viewHolder.iv_icon.setImageResource(R.drawable.daily_green_gradient_shape)
                viewHolder.iv_left.setImageResource(R.drawable.diary_icon_green_left)
            }
        }
        return result
    }

    var onCommentClickListener: OnCommentClickListener? = null

    interface OnCommentClickListener {
        //评论
        fun onCommentClick(position: Int, lifeRecordId: String?, view: View)

        fun onReplyComment(lifeRecordId: String?, position: Int, commentPosition: Int, reply_comment_id: String?, replyName: String?, view: View)

        //每条回复的点击事件
        fun onReplyItemClick(lifeRecordId: String?, position: Int, commentPosition: Int, replyPosition: Int, reply_comment_id: String?, replyName: String?, view: View)

        //新消息点击事件
        fun onNewMessageClick()

        fun onItemLongClickListener(position: Int, dailyId: String?)
    }

    /**
     * @return -1 ：同一天.    0：昨天 .   1 ：至少是前天.
     */
    @SuppressLint("SimpleDateFormat")
    private fun isYesterday(oldTime: Long, newTime: Long): Int {
        //yyyy-MM-dd 00:00:00
        val format = SimpleDateFormat("yyyy-MM-dd")
        val todayStr = format.format(newTime)
        val today = format.parse(todayStr)
        //昨天 86400000=24*60*60*1000 一天
        return when {
            today.time - oldTime in 1..86400000 -> 0
            today.time - oldTime <= 0 -> //至少是今天
                -1
            else -> //至少是前天
                1
        }
    }
}