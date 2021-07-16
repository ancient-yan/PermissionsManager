package com.gwchina.parent.screenshot.presentation

import android.arch.lifecycle.Observer
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.fragment.BaseListFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.*
import com.android.base.utils.android.ViewUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.screenshot.Navigator
import com.gwchina.parent.screenshot.ScreenshotUtils
import com.gwchina.parent.screenshot.data.ScreenshotData
import com.gwchina.parent.screenshot.data.ScreenshotStatisticsData
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.home_remote_screentshot_layout.*
import me.drakeet.multitype.register
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-16 17:14
 */
class RemoteScreenshotFragment : InjectorBaseStateFragment() {
    @Inject
    lateinit var navigator: Navigator

    private val mDisposableList = mutableListOf<Disposable>()

    private lateinit var popPresenter: PopPresenter
    private lateinit var adapter: MultiTypeAdapter
    private lateinit var animationDrawable: AnimationDrawable
    private lateinit var deleteMenu: MenuItem

    private var mSelectedDay = 0
    private var mScreenshotDataList: MutableList<ScreenshotData>? = null
    private var mScreenListMap: MutableMap<Int, MutableList<ScreenshotData>> = mutableMapOf()
    private val mCurrentChildAge = AppContext.appDataSource().user().currentChild?.age ?: 0

    //是否显示了12-18岁的年龄提醒
    private var mShowAgeTips = false

    private var mScreenshotStatisticsData: ScreenshotStatisticsData? = null

    private var mRlTips: RelativeLayout? = null
    private var mCardView: CardView? = null
    private var mBtnStartScreenshot: Button? = null
    private var mScreenRecyclerView: RecyclerView? = null

    //退出再次进来的时候是否需要查询截屏结果
    private var mNeedQueryScreenshotResult = false

    //上一次截屏缓存
    private var screenParams: ScreenshotUtils.ScreenParams? = null

    //记录已经删除成功的id集合
    private var mDeletedRecordIdList: List<String>? = null

    /**
     * 截屏成功后点击缩略图跳转的临时数据
     */
    private val mTempScreenshotDataList = mutableListOf<ScreenshotData>()

    companion object {
        /**
         * 3.同一个孩子的不同设备的首次使用都会提示；
         * 4.点击开始截屏后该提示消失，消失后不再出现；
         */
        private val AGE_TIPS_KEY = AppContext.appDataSource().user().currentDevice?.device_id + "_AGE_TIPS_KEY"
    }

    val viewModel by lazy {
        getViewModelFromActivity<ScreenshotViewModel>(viewModelFactory)
    }

    override fun provideLayout(): Any? {
        return R.layout.home_remote_screentshot_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        mRlTips = rlTips
        mCardView = cardView
        mBtnStartScreenshot = btnStartScreenshot
        mScreenRecyclerView = screenRecyclerView

        popPresenter = PopPresenter(this)
        animationDrawable = ivAnim.background as AnimationDrawable
        deleteMenu = gtlScreenshot.menu.add(R.string.delete)
                .setIcon(R.drawable.icon_delete_black)
                .alwaysShow()
                .onMenuItemClick {
                    mScreenshotDataList?.let {
                        navigator.openDeleteScreenshotPage(it)
                    }
                }
                .setVisible(false)

        screenRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = MultiTypeAdapter(requireContext()).apply {
            register(ScreenItemBinders(this@RemoteScreenshotFragment, false).apply {
                onItemClickListener = { _, position ->
                    val realPosition = when (mSelectedDay) {
                        0 -> position
                        1 -> (mScreenListMap[0]?.size ?: 0) + position
                        else -> (mScreenListMap[0]?.size ?: 0) + (mScreenListMap[1]?.size
                                ?: 0) + position
                    }
                    mScreenshotDataList?.let { navigator.openImageViewerPage(realPosition, it) }
                }
            })
        }
        screenRecyclerView.adapter = adapter
        setListener()
        subscribeViewModel()

        //上一次的截屏指令
        screenParams = ScreenshotUtils.getLastScreenParams()
        if (screenParams == null || screenParams!!.isSuccess) {
            btnStartScreenshot.isEnabled = true
        } else {
            //查询失败
            if (abs(screenParams!!.createTime - System.currentTimeMillis()) > 30 * 1000) {
                btnStartScreenshot.isEnabled = true
            } else {
                mNeedQueryScreenshotResult = true
            }
        }

        //截屏列表
        autoRefresh()
    }

    private fun queryScreenshotResult() {
        if (screenParams == null) return
        //截屏中动画
        startScreenAnimator()
        viewModel.getScreenshotDetailInterval(screenParams!!.recordId, screenParams!!.createTime)
        btnStartScreenshot.isEnabled = false
        mNeedQueryScreenshotResult = false
    }

    private fun subscribeViewModel() {
        //截屏详情
        viewModel.screenshotItem.observe(this, Observer {
            it?.onLoading {
                startScreenAnimator()
            }?.onSuccess { data ->
                //显示截屏中
                if (data?.pic_hash == null || data.url == null) {
                    btnStartScreenshot.isEnabled = false
                } else {
                    doDelayTask(3000, {

                        tvScreenshot.gone()
                        mTempScreenshotDataList.clear()
                        if (mScreenshotDataList?.any { it.record_id == data.record_id } == false) {
                            mTempScreenshotDataList.add(data)
                        }
                        mScreenshotDataList?.let { it1 -> mTempScreenshotDataList.addAll(it1) }
                        ImageLoaderFactory.getImageLoader().display(ivScreen, data.size_url)
                        cardView.isClickable = true
                        animationDrawable.stop()
                        ivAnim?.gone()
                        tvTips?.gone()
                        if (data.is_normal == 0) {
                            doDelayTask(5000, {
                                rlTips?.visible()
                                tvFailedContent?.text = getString(R.string.home_screen_shot_screenOff)
                                btnReason?.gone()
                            }) {
                                mRlTips?.gone()
                            }
                        }
                    }) {
                        mBtnStartScreenshot?.isEnabled = true
                        screenSuccessAnimator(data)
                        popPresenter.setToday(mSelectedDay)
                    }
                }
            }?.onError {
                screenshotFailed()
                doDelayTask(5000, {
                    rlTips.visible()
                    btnReason.visible()
                    tvFailedContent.text = it.message
                }) {
                    mRlTips?.gone()
                }
            }
        })
        //截屏列表
        viewModel.screenshotList.observe(this, Observer {
            it?.onSuccess { data ->
                setLayoutVisible(true, data)
                mScreenshotDataList = data?.toMutableList()
                mScreenListMap.clear()
                if (data.isNullOrEmpty()) {
                    //无数据的时候去请求数据统计
                    viewModel.screenStatisticsData(true)
                    adapter.replaceAll(emptyList())
                } else {
                    showContentLayout()
                    data.apply {
                        val screenListMap = ScreenshotUtils.parseData(this)
                        mScreenListMap.putAll(screenListMap as Map<out Int, MutableList<ScreenshotData>>)
                        mScreenListMap.apply {
                            adapter.replaceAll(this[mSelectedDay] as List<Any>?)
                        }
                        if (mScreenListMap[mSelectedDay].isNullOrEmpty()) {
                            showEmptyLayout(R.string.home_screen_empty3)
                        }
                    }
                }
                if (mNeedQueryScreenshotResult) {
                    queryScreenshotResult()
                }
            }?.onError { err ->
                processErrorWithStatus(err)
                setLayoutVisible(false)
            }
        })
        //数据统计
        viewModel.screenStatisticsData.observe(this, Observer {
            it?.onError { err ->
                errorHandler.handleError(err)
                btnStartScreenshot.isEnabled = true
                if (ivAnim.isVisible() && animationDrawable.isRunning) {
                    screenshotFailed()
                }
                refreshCompleted()
            }?.onSuccess { data ->
                refreshCompleted()
                data?.apply {
                    mScreenshotStatisticsData = this
                    //弹框
                    if (!isOpenPage) {
                        if (sum_three_count >= 50) {
                            fullScreenDialog()
                        } else {
                            if (day_count >= 6) {
                                moreThan6TimesDialog()
                            } else {
                                if (!isOpenPage) {
                                    btnStartScreenshot.isEnabled = false
                                    //开始截屏
                                    viewModel.addScreenshot()
                                }
                            }
                        }
                    }
                    //无数据时候不同的提示语
                    if (mSelectedDay == 0) {
                        if (this.day_count == 0) {
                            if (this.sum_count == 0) {
                                showEmptyLayout(R.string.home_screen_empty2)
                            } else {
                                showEmptyLayout(R.string.home_screen_empty3)
                            }
                        } else if (this.day_count == this.day_del_count) {
                            showEmptyLayout(R.string.home_screen_empty1)
                        }
                    }
                    //从未截屏时候的状态
                    if (sum_count == 0) {
                        showAgeTips()
                    } else {
                        tvDate.visible()
                    }
                }
            }
        })
        viewModel.refreshEventCenter.getRefreshEvent().observe(this, Observer {
            mDeletedRecordIdList = it
            autoRefresh()
        })
    }

    private fun screenshotFailed() {
        doDelayTask(3000, {
            animationDrawable.stop()
            ivAnim.gone()
            tvScreenshot.gone()
            ivFailed.visible()
        }) {
            mBtnStartScreenshot?.isEnabled = true
            screenFailedAnimator()
        }
    }

    private fun setLayoutVisible(isVisible: Boolean, data: List<ScreenshotData>? = null) {
        tvDate?.visibleOrGone(isVisible)
        mBtnStartScreenshot?.visibleOrGone(isVisible)
        deleteMenu.isVisible = !data.isNullOrEmpty()
    }

    override fun onRefresh() {
        super.onRefresh()
        viewModel.getScreenshotList()
    }

    private fun setListener() {
        tvDate.onDebouncedClick {
            popPresenter.showPop()
        }
        popPresenter.onItemClickListener = {
            mSelectedDay = it
            mScreenListMap.apply {
                if (!this[mSelectedDay].isNullOrEmpty()) {
                    adapter.replaceAll(this[mSelectedDay] as List<ScreenshotData>)
                    showContentLayout()
                } else {
                    showEmptyLayout(if (mScreenshotStatisticsData?.sum_count == 0) R.string.home_screen_empty2 else R.string.home_screen_empty3)
                    mScreenshotStatisticsData?.let { screenshotStatisticsData ->
                        if (mSelectedDay == 0 && screenshotStatisticsData.day_count == screenshotStatisticsData.day_del_count && screenshotStatisticsData.sum_three_count > 0) {
                            showEmptyLayout(R.string.home_screen_empty1)
                        }
                    }
                }
            }
        }
        btnReason.onDebouncedClick {
            rlTips.gone()
            reasonDialog()
        }
        btnStartScreenshot.onDebouncedClick {
            if (mShowAgeTips) {
                tvTips.gone()
                AppSettings.settingsStorage().getBoolean(AGE_TIPS_KEY, true)
            }
            viewModel.screenStatisticsData(false)
        }

        cardView.onDebouncedClick {
            if (mTempScreenshotDataList.isNotEmpty())
                navigator.openImageViewerPage(0, mTempScreenshotDataList)
        }
    }

    /**
     * 开始截屏动画
     */
    private fun startScreenAnimator() {
        cardView.isClickable = false
        ivScreen.setImageResource(R.color.gray_separate)
        ViewUtils.visible(cardView, ivAnim, tvScreenshot)
        ivFailed.gone()
        if (!animationDrawable.isRunning) {
            animationDrawable.start()
        }
        deleteMenu.isEnabled = false
    }

    /**
     * 截屏失败动画
     */
    private fun screenFailedAnimator() {
        val startLocation = IntArray(2)
        mCardView?.getLocationOnScreen(startLocation)
        mCardView?.let { popPresenter.doTranslationXAnimator(it, startLocation[0].toFloat()) }
        deleteMenu.isEnabled = true
    }

    /**
     * 截屏成功动画
     */
    private fun screenSuccessAnimator(screenshotData: ScreenshotData) {
        deleteMenu.isEnabled = true
        if (mCardView == null) return
        val startLocation = IntArray(2)
        mCardView!!.getLocationOnScreen(startLocation)
        val endLocation = IntArray(2)
        mScreenRecyclerView?.getLocationOnScreen(endLocation)
        popPresenter.doAnimator(mCardView!!, startLocation[0].toFloat(), startLocation[1].toFloat(), endLocation[0].toFloat() + dip(15), endLocation[1].toFloat()) {
            /**
             * 截屏成功的数据集合中不包含该数据并且删除的数据集合中也不包含该数据
             */
            if (mScreenshotDataList?.any { it.record_id == screenshotData.record_id } == false && (mDeletedRecordIdList == null || !mDeletedRecordIdList!!.contains(screenshotData.record_id))) {
                adapter.addAt(0, screenshotData)
                mScreenshotDataList?.add(0, screenshotData)
                if (mScreenListMap[0].isNullOrEmpty()) {
                    mScreenListMap[0] = mutableListOf(screenshotData)
                } else {
                    mScreenListMap[0].apply {
                        this?.add(0, screenshotData)
                    }
                }
                if (!ScreenshotUtils.isMapDataMatch(mScreenListMap)) {
                    onRefresh()
                }
            }
            setLayoutVisible(true, mScreenshotDataList)
            mScreenRecyclerView?.scrollToPosition(0)
            if (mScreenshotDataList.isNullOrEmpty()) {
                showEmptyLayout(R.string.home_screen_empty1)
            } else {
                showContentLayout()
            }
        }
    }

    private fun doDelayTask(time: Int, task: () -> Unit, delayTask: () -> Unit) {
        task.invoke()
        val disposable = Observable.timer(time.toLong(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    delayTask.invoke()
                }.subscribe()
        mDisposableList.add(disposable)
    }

    override fun onDestroy() {
        mDisposableList.forEach {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        viewModel.disposable()
        super.onDestroy()
    }

    private fun moreThan6TimesDialog() {
        showConfirmDialog {
            this.messageId = R.string.home_screen_6_times
            this.negativeId = R.string.screen_goon
            this.positiveId = R.string.screen_stop
            this.negativeListener = {
                btnStartScreenshot.isEnabled = false
                viewModel.addScreenshot()
            }
        }?.setCanceledOnTouchOutside(false)
    }

    private fun fullScreenDialog() {
        showConfirmDialog {
            this.messageId = R.string.home_screen_50_count
            this.negativeId = R.string.screen_delete
            this.positiveId = R.string.i_got_it
            this.negativeListener = {
                mScreenshotDataList?.let { it1 -> navigator.openDeleteScreenshotPage(it1) }
            }
        }?.setCanceledOnTouchOutside(false)
    }

    private fun reasonDialog() {
        showConfirmDialog {
            this.titleId = R.string.home_screen_error_reason_title
            this.messageGravity = Gravity.START
            this.messageId = R.string.home_screen_error_reason_content
            this.noNegative()
            this.positiveId = R.string.i_got_it
        }?.setCanceledOnTouchOutside(false)
    }

    private fun showEmptyLayout(stringsId: Int) {
        stateLayoutConfig
                .disableOperationWhenRequesting(true)
                .setStateMessage(BaseListFragment.EMPTY, getText(stringsId))
                .setMessageGravity(BaseListFragment.EMPTY, Gravity.CENTER)
        super.showEmptyLayout()
    }

    private fun showAgeTips() {
        tvDate.gone()
        if (mCurrentChildAge >= 12) {
            if (!AppSettings.settingsStorage().getBoolean(AGE_TIPS_KEY, false)) {
                mShowAgeTips = true
                tvTips.visible()
                tvTips.text = getString(R.string.home_screen_year_old_tip_mask, mCurrentChildAge)
            }
        }
    }


}