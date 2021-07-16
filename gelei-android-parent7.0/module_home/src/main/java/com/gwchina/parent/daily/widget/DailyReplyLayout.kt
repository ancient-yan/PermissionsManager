package com.gwchina.parent.daily.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.data.Reply
import com.gwchina.sdk.base.utils.foldText

/**
 *@author hujl
 *@Email: hujl@163.com
 *@Date : 2019-08-05 19:27
 * 日记回复详情
 */
class DailyReplyLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val commentDataList = mutableListOf<Reply>()
    //当前用户id
    private lateinit var userId: String

    private var mUserTextColor: Int = ContextCompat.getColor(context, R.color.green_level1)
    private var mContentTextColor: Int = ContextCompat.getColor(context, R.color.gray_level2)
    private var mContentTextSize: Float = 13f

    init {
        orientation = VERTICAL
    }

    fun notifyCommentListChanged(commentDataList: List<Reply>?, userId: String) {
        this.userId = userId
        removeAllViews()
        this.commentDataList.clear()
        if (commentDataList != null) {
            this.commentDataList.addAll(commentDataList)
        }
        notifyChanged()
    }

    private fun notifyChanged() {
        this.commentDataList.forEachIndexed { index, commentBean ->
            addCommentView(commentBean, index)
        }
    }

    private fun addCommentView(reply: Reply, index: Int) {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.bottomMargin = 10
        val textView = generateCommentView(reply)
        this.addView(textView, params)
        textView.setOnClickListener {
            onCommentItemClickListener?.onItemClick(reply, index)
        }
    }

    private fun generateCommentView(reply: Reply): TextView {
        val commentTextView = TextView(context)
        commentTextView.setTextColor(mContentTextColor)
        commentTextView.textSize = mContentTextSize
        commentTextView.setLineSpacing(10f, 1.0f)

        val replyName = if (userId == reply.comment_user_id) context.getString(R.string.daily_me) else reply.comment_user_name.foldText(10)
        val byReplyName = if (userId == reply.reply_user_id) context.getString(R.string.daily_me) else reply.reply_user_name.foldText(10)
        val spanStringBuilder = SpannableStringBuilder(context.getString(R.string.daily_reply3_mask, replyName, byReplyName, reply.content))
        val foregroundColorSpan = ForegroundColorSpan(mUserTextColor)
        spanStringBuilder.setSpan(foregroundColorSpan, 0, replyName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanStringBuilder.setSpan(ForegroundColorSpan(mUserTextColor), replyName.length + 2, replyName.length + 2 + byReplyName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        commentTextView.text = spanStringBuilder
        return commentTextView
    }

    var onCommentItemClickListener: OnCommentItemClickListener? = null

    interface OnCommentItemClickListener {
        fun onItemClick(replierPerson: Reply, position: Int)
    }

}