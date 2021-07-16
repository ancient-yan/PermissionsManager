package com.gwchina.parent.daily.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import kotlinx.android.synthetic.main.daily_write_pic_item_layout.view.*

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-08 16:54
 * 写日记添加照片适配器
 */
class AddPicAdapter(var context: Context, private var picPathList: ArrayList<String>) : RecyclerView.Adapter<AddPicAdapter.AddPicViewHolder>() {

    private val maxPicNum = 9

    init {
        if (picPathList.size < maxPicNum && !picPathList.contains("")) {
            picPathList.add("")
        }
    }

    fun notifyData() {
        if (picPathList.size < maxPicNum) {
            picPathList.add("")
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AddPicViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.daily_write_pic_item_layout, p0, false)
        return AddPicViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return picPathList.size
    }

    override fun onBindViewHolder(holder: AddPicViewHolder, position: Int) {
        if (picPathList[position].isEmpty()) {
            holder.deleteImgIv.gone()
            holder.imageView.setImageResource(R.drawable.daily_icon_add_photo)
        } else {
            holder.deleteImgIv.visible()
            ImageLoaderFactory.getImageLoader().display(holder.imageView, picPathList[position])
        }
        holder.imageView.setOnClickListener {
            if (picPathList[position].isEmpty()) {
                onItemClickListener?.addPic()
            } else {
                onItemClickListener?.onPreViewItemClick(position, picPathList)
            }
        }
        holder.deleteImgIv.setOnClickListener {
            picPathList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            if (!picPathList.contains("")) {
                picPathList.add("")
                notifyItemInserted(picPathList.size - 1)
            }
            onItemClickListener?.onDelete(position)
        }
    }

    class AddPicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.imageView!!
        val deleteImgIv = itemView.deleteImgIv!!
    }

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onPreViewItemClick(position: Int, picList: ArrayList<String>)
        fun addPic()
        fun onDelete(position: Int)
    }
}