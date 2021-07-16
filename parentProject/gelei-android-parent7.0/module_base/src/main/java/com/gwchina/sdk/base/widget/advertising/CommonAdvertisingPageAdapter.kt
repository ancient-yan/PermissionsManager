package com.gwchina.sdk.base.widget.advertising

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.base.adapter.pager.ViewPagerAdapter
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.linker.JumpInterceptor
import com.gwchina.sdk.base.linker.SchemeJumper
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent

/**全局通用广告跳转*/
class CommonAdvertisingPageAdapter(
        private val context: Context,
        data: List<Advertising>
) : ViewPagerAdapter<Advertising, ViewPagerAdapter.ViewHolder>(data) {


    private val displayConfig = DisplayConfig.create()
            .setErrorPlaceholder(R.color.gray_separate)
            .setLoadingPlaceholder(R.color.gray_separate)

    @Suppress
    var onItemClickListener: ((Advertising) -> Unit)? = null

    var onInterceptJumpToAppPage: JumpInterceptor? = null

    private val _onClickListener = View.OnClickListener {
        StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_AD)
        val advertising = it.getTag(R.id.common_item_id) as Advertising

        if (onItemClickListener != null) {
            onItemClickListener?.invoke(advertising)
            return@OnClickListener
        }

        if (advertising.jump_args.contains(RouterPath.Browser.INVITATION_FRIENDS)) {
            if (needLogin()) {
                SchemeJumper.handleSchemeJump(context, advertising.jump_target?: "", advertising.jump_args, onInterceptJumpToAppPage)
            }
        } else {
            SchemeJumper.handleSchemeJump(context, advertising.jump_target?: "", advertising.jump_args, onInterceptJumpToAppPage)
        }
    }

    private fun needLogin(): Boolean {
        //need login
        val user = AppContext.appDataSource().user()
        //是否登录
        if (!user.logined()) {
            AppContext.appRouter().build(RouterPath.Account.PATH).navigation()
            return false
        }
        return true
    }

    override fun createViewHolder(container: ViewGroup): ViewHolder {
        val itemView = AppCompatImageView(container.context)
        itemView.scaleType = ImageView.ScaleType.CENTER_CROP
        return ViewHolder(itemView)
    }

    override fun onBindData(viewHolder: ViewHolder, item: Advertising) {
        val imageView = viewHolder.itemView as ImageView
        ImageLoaderFactory.getImageLoader().display(imageView, item.ad_photo_url, displayConfig)
        viewHolder.itemView.setTag(R.id.common_item_id, item)
        viewHolder.itemView.setOnClickListener(_onClickListener)
    }

}
