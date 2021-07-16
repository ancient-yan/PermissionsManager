package com.gwchina.parent.screenshot.presentation

import android.arch.lifecycle.Observer
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.imageloader.LoadListener
import com.android.base.kotlin.alwaysShow
import com.android.base.kotlin.onMenuItemClick
import com.github.chrisbanes.photoview.PhotoView
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.screenshot.Navigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.daily_pic_pre_fragment_layout.*
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-15 11:34
 */
//图片预览
class ScreenshotPicPreFragment : InjectorBaseFragment() {

    @Inject
    lateinit var navigator: Navigator
    //是否删除过图片
    private var hasDeletedPic = false

    //记录删除成功的id集合
    private val deletedRecordIdList = mutableListOf<String>()

    private val viewModel by lazy {
        getViewModelFromActivity<ScreenshotViewModel>(viewModelFactory)
    }

    lateinit var mPicData: ArrayList<PicPreItem>
    private val photoViewList = mutableListOf<PhotoView>()
    private var mPosition = 0

    lateinit var adapter: PrePicAdapter

    @Parcelize
    data class PicPreItem(val record_id: String, val title: String, val path: String) : Parcelable

    companion object {
        private const val KEY = "screen_position_key"
        private const val DATA = "screen_pic_data"

        fun newInstance(position: Int, picPathList: ArrayList<PicPreItem>) = ScreenshotPicPreFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY, position)
                putParcelableArrayList(DATA, picPathList)
            }
        }
    }

    override fun provideLayout(): Any? {
        return R.layout.home_pic_pre_fragment_layout
    }

    private fun setData(position: Int, dataList: List<PicPreItem>) {
        this.mPosition = position
        gwTitleLayout.setTitle(dataList[position].title)
        photoViewList.clear()
        dataList.forEach { _ ->
            photoViewList.add(generateImageView())
        }
        gwTitleLayout.menu.add(R.string.delete)
                .setIcon(R.drawable.icon_delete_black)
                .alwaysShow()
                .onMenuItemClick {
                    deletePic(mPicData[mPosition].record_id, mPosition)
                }
    }

    private fun generateImageView(): PhotoView {
        return PhotoView(context)
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        val position = arguments?.getInt(KEY) ?: 0
        mPicData = arguments?.getParcelableArrayList<PicPreItem>(DATA) as ArrayList<PicPreItem>
        setData(position, mPicData)
        adapter = PrePicAdapter()
        viewPager.adapter = adapter
        viewPager.currentItem = mPosition
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                mPosition = p0
                gwTitleLayout.setTitle(mPicData[p0].title)
            }

        })
    }

    private fun deletePic(id: String, position: Int) {
        viewModel.delPicList(mutableListOf(id)).observe(this, Observer {
            it?.onLoading {
                showLoadingDialog()
            }?.onSuccess {
                hasDeletedPic = true
                if (!deletedRecordIdList.contains(id)) {
                    deletedRecordIdList.add(id)
                }
                dismissLoadingDialog()
                if (mPicData.size > 1) {
                    if (position == mPicData.size) {
                        mPosition = position - 1
                    }
                    mPicData.removeAt(position)
                    photoViewList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    viewPager.currentItem = mPosition
                    gwTitleLayout.setTitle(mPicData[mPosition].title)
                } else {
                    exitFragment()
                }
            }?.onError { throwable ->
                dismissLoadingDialog()
                errorHandler.handleError(throwable)
            }
        })
    }

    inner class PrePicAdapter : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }

        override fun getCount(): Int {
            return mPicData.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = photoViewList[position]
            photoView.scaleType = ImageView.ScaleType.CENTER_CROP
            if (mPicData[position].path.startsWith("http")) {
                ImageLoaderFactory.getImageLoader().display(photoView, mPicData[position].path, object : LoadListener<Drawable> {
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
            }
            photoView.setOnLongClickListener {
                viewModel.savePic(mPicData[position].path, this@ScreenshotPicPreFragment)
                true
            }
            container.addView(photoView)
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    override fun onDestroy() {
        if (hasDeletedPic) {
            viewModel.refreshEventCenter.setRefreshEvent(deletedRecordIdList)
        }
        super.onDestroy()
    }

}