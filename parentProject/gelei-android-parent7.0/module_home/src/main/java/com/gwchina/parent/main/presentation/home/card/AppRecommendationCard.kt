package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.graphics.Rect
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.interfaces.OnItemClickListener
import com.android.base.kotlin.*
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.Soft
import com.gwchina.parent.main.data.SoftRecommendInfo
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.parent.main.presentation.home.card.AppRecommendationCard.Companion.DUMMY_FLAG
import com.gwchina.parent.recommend.presentation.showInstallingTips

import com.gwchina.sdk.base.data.api.APP_INSTALL_STATUS_INSTALLED
import com.gwchina.sdk.base.data.api.APP_INSTALL_STATUS_INSTALLING
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.utils.getGradeNameByGrade
import kotlinx.android.synthetic.main.home_card_app_recommendation.view.*
import kotlinx.android.synthetic.main.home_card_app_recommendation_item.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-01 10:57
 */
class AppRecommendationCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        internal const val DUMMY_FLAG = -1

    }

    private lateinit var interactor: CardInteractor
    private lateinit var appRecoAdapter: AppRecoAdapter

    private var appRecommendation: SoftRecommendInfo? = null
    private var appRecommendationList: List<Any>? = null

    private val dummySoftList: List<Soft> by lazy {
        listOf(
                Soft(type_name = "小初高拍照搜题提分利器", soft_name = "作业帮", install_flag = DUMMY_FLAG),
                Soft(type_name = "在线少儿英语外教", soft_name = "DaDa英语", install_flag = DUMMY_FLAG),
                Soft(type_name = "小初中高多学科在线学习平台", soft_name = "洋葱数学", install_flag = DUMMY_FLAG)
        )
    }

    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.home_card_app_recommendation, this)
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        setupClickListeners()
        setupList()
        subscribeData()
    }

    private fun setupClickListeners() {
        clHomeAppRecoHeader.onDebouncedClick {
            appRecommendation?.rec_group_id?.let(interactor.navigator::openAppRecommendationPage)
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_MOREAPP)
        }
        ivHomeAppRecoDesc.onDebouncedClick {
            interactor.navigator.commonJump()
        }
    }

    private fun setupList() {
        appRecoAdapter = AppRecoAdapter(interactor.host)

        appRecoAdapter.onCheckAppDetailListener = object : OnItemClickListener<Soft>() {
            override fun onClick(view: View, soft: Soft) {
                if (soft.install_flag == DUMMY_FLAG) {
                    interactor.navigator.commonJump()
                } else {
                    appRecommendation?.rec_group_id?.let(interactor.navigator::openAppRecommendationPage)
                    StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_MOREAPP)
                }
            }
        }

        //首页给孩子安装
        appRecoAdapter.onInstallListener = object : OnItemClickListener<Soft>() {
            override fun onClick(view: View, soft: Soft) {
                interactor.host.showInstallingTips {
                    interactor.cardDataProvider.installSoft(soft)
                    StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_APPINSTALL)
                }
            }
        }

        rvHomeAppReco.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, dip(30))
            }
        })

        rvHomeAppReco.adapter = appRecoAdapter
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeUser {
            if (!it.logined() || it.currentChild == null || it.currentDevice == null) {
                setupViewsWhenNoDevice()
            } else {
                clHomeAppRecoHeader.visible()
                ivHomeAppRecoDesc.gone()
                tvHomeAppRecoGrade.text = context.getString(R.string.home_card_app_recommendation_grade_info_mask, getGradeNameByGrade(it.currentChild?.grade ?: -1))
            }
        }

        interactor.cardDataProvider.observeHomeData {
            appRecommendation = it?.softRecommendInfo
            tvHomeAppRecoGrade.text = context.getString(R.string.home_card_app_recommendation_grade_info_mask, it?.softRecommendInfo?.group_name ?: "")

            if (appRecommendationList != it?.peerRecommendation) {
                appRecoAdapter.setDataSource(it?.peerRecommendation ?: emptyList(), true)
                appRecommendationList = it?.peerRecommendation
            }
        }

    }

    private fun setupViewsWhenNoDevice() {
        appRecoAdapter.setDataSource(dummySoftList, true)
        clHomeAppRecoHeader.gone()
        ivHomeAppRecoDesc.visible()
    }

}

private class AppRecoAdapter(private val host: Fragment) : SimpleRecyclerAdapter<Soft>(host.requireContext()) {

    var onInstallListener: OnItemClickListener<Soft>? = null
    var onCheckAppDetailListener: OnItemClickListener<Soft>? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.home_card_app_recommendation_item

    override fun bind(viewHolder: KtViewHolder, item: Soft) {
        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(onCheckAppDetailListener)

        viewHolder.tvItemAppName.text = item.soft_name
        viewHolder.tvHomeItemAppDesc.text = item.getTypeName()

        if (item.install_flag == DUMMY_FLAG) {
            viewHolder.ivHomeItemAppIcon.setImageResource(R.color.gray_level4)
            viewHolder.tvItemAppName.setTextColor(host.colorFromId(R.color.gray_level2))
            viewHolder.tvHomeItemAppDesc.setTextColor(host.colorFromId(R.color.gray_level3))
        } else {
            ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivHomeItemAppIcon, item.soft_icon)
            viewHolder.tvItemAppName.setTextColor(host.colorFromId(R.color.gray_level1))
            viewHolder.tvHomeItemAppDesc.setTextColor(host.colorFromId(R.color.gray_level2))
        }

        viewHolder.tvHomeItemInstallStatus.setOnClickListener(null)

        when {
            item.install_flag == DUMMY_FLAG -> {
                viewHolder.tvHomeItemInstallStatus.showDummy()
            }
            item.install_flag == APP_INSTALL_STATUS_INSTALLING -> {
                viewHolder.tvHomeItemInstallStatus.showInstalling()
            }
            item.install_flag == APP_INSTALL_STATUS_INSTALLED -> {
                viewHolder.tvHomeItemInstallStatus.showInstalled()
            }
            else -> {
                viewHolder.tvHomeItemInstallStatus.showNotInstall()
                viewHolder.tvHomeItemInstallStatus.tag = item
                viewHolder.tvHomeItemInstallStatus.setOnClickListener(onInstallListener)
            }
        }
    }

    private fun TextView.showNotInstall() {
        isEnabled = true
        setText(R.string.install_for_child)
        setTextColor(colorFromId(R.color.green_level1))
    }

    private fun TextView.showDummy() {
        isEnabled = false
        setText(R.string.install_for_child)
        setTextColor(colorFromId(R.color.gray_level3))
    }

    private fun TextView.showInstalled() {
        isEnabled = false
        setTextColor(colorFromId(R.color.gray_level3))
        setText(R.string.installed)
    }

    private fun TextView.showInstalling() {
        isEnabled = false
        setTextColor(colorFromId(R.color.gray_level2))
        setText(R.string.installing)
    }

}