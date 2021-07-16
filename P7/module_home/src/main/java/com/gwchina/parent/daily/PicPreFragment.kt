package com.gwchina.parent.daily

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.base.app.fragment.BaseFragment
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.imageloader.LoadListener
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.gwchina.lssw.parent.home.R
import kotlinx.android.synthetic.main.daily_pic_pre_fragment_layout.*
import java.io.File

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-15 11:34
 */
//图片预览
class PicPreFragment : BaseFragment() {

    private val picPathList = mutableListOf<String?>()
    private val photoViewList = mutableListOf<PhotoView>()
    private var mPosition = 0

    companion object {
        private const val KEY = "daily_position_key"
        private const val DATA = "daily_pic_data"

        fun newInstance(position: Int, picPathList: ArrayList<String>) = PicPreFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY, position)
                putStringArrayList(DATA, picPathList)
            }
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.daily_pic_pre_fragment_layout
    }

    private fun setData(position: Int, picPath: List<String>) {
        this.mPosition = position
        this.picPathList.clear()
        this.picPathList.addAll(picPath)
        gwTitleLayout.setTitle("${position + 1}/${picPath.size}")
        photoViewList.clear()
        picPath.forEach { _ ->
            photoViewList.add(generateImageView())
        }
    }

    private fun generateImageView(): PhotoView {
        return PhotoView(context)
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        val position = arguments?.getInt(KEY)
        val data = arguments?.getStringArrayList(DATA)
        position?.let { data?.let { it1 -> setData(it, it1) } }
        viewPager.adapter = PrePicAdapter()
        viewPager.currentItem = mPosition
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                gwTitleLayout.setTitle("${p0 + 1}/${picPathList.size}")
            }

        })
    }

    inner class PrePicAdapter : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }

        override fun getCount(): Int {
            return picPathList.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = photoViewList[position]
            if (picPathList[position]!!.startsWith("http")) {
                ImageLoaderFactory.getImageLoader().display(photoView, picPathList[position], object : LoadListener<Drawable> {
                    override fun onLoadStart() {
                        showLoadingDialog()
                    }

                    override fun onLoadSuccess(resource: Drawable?) {
                        dismissLoadingDialog()
                    }

                    override fun onLoadFail() {
                        dismissLoadingDialog()
                    }
                })
            } else {
                val file = File(picPathList[position])
                displayFile(file, photoView)
            }
            photoView.setOnClickListener { activity?.onBackPressed() }
            container.addView(photoView)
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    private fun displayFile(file: File, imageView: ImageView) {
        Glide.with(this).load(file).into(imageView)
    }

}