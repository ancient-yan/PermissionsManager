package com.gwchina.parent.daily.adapter

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.data.DailyMessageListBean
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatMillisecondsToDetailDesc
import kotlinx.android.synthetic.main.daily_message_list_item_layout.*
import java.util.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-10 10:33
 */
class MessageListAdapter(var host: Fragment, private var userId: String, private var onImageItemClick: (Int, ArrayList<String>) -> Unit) : RecyclerAdapter<DailyMessageListBean, KtViewHolder>(host.requireContext()) {

    //key: 每个item的id
    private var adapterCacheMap: MutableMap<Int, ClickNineGridViewAdapter> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.daily_message_list_item_layout, parent, false)
        return KtViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: KtViewHolder, position: Int) {
        val dailyMessageListBean = getItem(position)
        val commentUserName = if (dailyMessageListBean.comment_user_id == userId) host.getString(R.string.daily_me) else dailyMessageListBean.comment_user_name
        val commentedUserName = if (dailyMessageListBean.commented_user_id == userId) host.getString(R.string.daily_me) else dailyMessageListBean.commented_user_name
        holder.nameTv.text = commentUserName.foldText(10)+" 回复 "+commentedUserName

        //内容
        holder.replyContentTv.text = dailyMessageListBean.commented_content
        holder.line.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE

        if (adapterCacheMap[position] == null) {
            val clickNineGridViewAdapter = ClickNineGridViewAdapter(mContext, ClickNineGridViewAdapter.generateImageInfoList(generatePath(dailyMessageListBean)), onImageItemClick)
            holder.nineGridView.setAdapter(clickNineGridViewAdapter)
            adapterCacheMap[position] = clickNineGridViewAdapter
        } else {
            adapterCacheMap[position]?.let { holder.nineGridView.setAdapter(it) }
        }
        val replyName = if (dailyMessageListBean.life_record_user_id == userId) host.getString(R.string.daily_me) else dailyMessageListBean.life_record_user_name

        holder.publishContentTv.text = String.format("%s: %s", replyName, if (dailyMessageListBean.life_circle_content.isNullOrEmpty()) "" else dailyMessageListBean.life_circle_content)

        holder.replyTimeTv.text = formatMillisecondsToDetailDesc(dailyMessageListBean.comment_time)
    }

    private fun generatePath(dailyMessageListBean: DailyMessageListBean): List<String?>? {
        return dailyMessageListBean.life_photo?.map { it?.photo_path }
    }

}