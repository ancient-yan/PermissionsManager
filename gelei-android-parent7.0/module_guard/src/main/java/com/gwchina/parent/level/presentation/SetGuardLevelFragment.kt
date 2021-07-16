package com.gwchina.parent.level.presentation

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.router.SelectedLevelInfo
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.level_fragment_setup.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-27 10:02
 */
class SetGuardLevelFragment : InjectorBaseStateFragment() {

    companion object {
        fun instance(isFromMigration: Boolean = false): SetGuardLevelFragment {
            val fragment = SetGuardLevelFragment()
            val bundle = Bundle()
            bundle.putBoolean("isFromMigration", isFromMigration)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var isFromMigration = false

    private var guardLevelVOList: List<GuardLevelVO>? = null

    private var selectedGuardLevel: GuardLevelVO? = null

    private lateinit var guardLevelUI: GuardLevelUI

    private val viewModel by lazy {
        getViewModel<SetGuardLevelViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
        isFromMigration = arguments?.getBoolean("isFromMigration") ?: false
    }

    override fun provideLayout() = R.layout.level_fragment_setup

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        guardLevelUI = GuardLevelUI(this)

        setupTitle()

        levelSvContent.post { setupFloatLayout() }

        levelBtnConfirm.setOnClickListener {
            selectedGuardLevel?.run(::doConfirm)
        }
    }

    private fun setupTitle() {
        if (isMustSetLevel() || isFromMigration) {
            levelGwTitle.toolbar.navigationIcon = null
        } else {
            toolbar.setNavigationIcon(R.drawable.icon_back)
            toolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
    }

    private fun isMustSetLevel() = !viewModel.hasSelectedLevel && !viewModel.forChoosingLevel

    private fun setupFloatLayout() = installScrollingVisibleTitleBar {

        levelGwTitle.alpha = it
        levelTvTitle.alpha = 1F - it

        when (it) {
            0F -> {
                vCuttingLine.invisible()
                levelLgvFloat.gone()
                levelVFloatMask.gone()
                levelSvContent.isVerticalScrollBarEnabled = false
            }
            1.0F -> {
                vCuttingLine.visible()
                levelLgvFloat.visible()
                levelVFloatMask.visible()
                levelSvContent.isVerticalScrollBarEnabled = true
            }
            else -> {
                vCuttingLine.visible()
                levelLgvFloat.gone()
                levelVFloatMask.gone()
                levelSvContent.isVerticalScrollBarEnabled = false
            }
        }
    }

    private fun setupLevelSwitch() {
        //check data
        val initLevels = guardLevelVOList ?: return

        if (initLevels.size < 3/*轻度、中度、重度，必须保证有三个元素*/) {
            Timber.e("守护等级数量不对")
            return
        }

        levelLgv.setup(guardLevelVOList, true) {
            selectedGuardLevel = it
            guardLevelUI.setList(it.items)
            levelTvApplicableAgeDesc.text = it.desc
            levelLgvFloat.syncSelectedItem(it.guardLevel)
        }

        levelLgvFloat.setup(guardLevelVOList, true) {
            selectedGuardLevel = it
            guardLevelUI.setList(it.items)
            levelTvApplicableAgeDesc.text = it.desc
            levelLgv.syncSelectedItem(it.guardLevel)
        }
    }

    private fun showData() {
        setupLevelSwitch()
        //设置默认选中项
        viewModel.selectedLevel?.let(levelLgv::setDefaultSelectItem)
        //设置推荐位置
        val childAge = viewModel.childAge
        levelLgv.setRecommendByAge(childAge)
        levelLgvFloat.setRecommendByAge(childAge)
    }

    private fun doConfirm(guardLevelVO: GuardLevelVO) {
        if (viewModel.forChoosingLevel) {
            exitForChoosingLevel(guardLevelVO, viewModel.childDeviceInfo?.sessionId ?: "")
        } else {
            confirmSwitchGuardLevel(guardLevelVO)
        }
    }

    private fun confirmSwitchGuardLevel(guardLevelVO: GuardLevelVO) {
        //第一次设置等级，直接修改
        if (!viewModel.hasSelectedLevel) {
            viewModel.setLevel(guardLevelVO)
        } else if (viewModel.hasDifference(guardLevelVO))/*之后的有更改则弹框提示*/ {
            showConfirmDialog {
                messageId = R.string.change_guard_mode_tips
                positiveListener = {
                    viewModel.setLevel(guardLevelVO)
                }
            }
        } else {
            //无更改，直接退出
            exitForSettingLevel()
        }
    }

    private fun subscribeViewModel() {
        //加载守护等级信息
        viewModel.guardLevelList
                .observe(this, Observer { resource ->
                    resource?.onLoading {
                        showLoadingLayout()
                    }?.onError {
                        processErrorWithStatus(resource.error())
                    }?.onSuccess {
                        processResultWithStatus(resource.orElse(null)) {
                            guardLevelVOList = it
                            showData()
                        }
                    }
                })

        //设置守护等级
        viewModel.setLevelResult
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> {
                            levelBtnConfirm?.isEnabled = false
                            showLoadingDialog()
                        }
                        it.isError -> {
                            levelBtnConfirm?.isEnabled = true
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                        it.isSuccess -> {
                            levelBtnConfirm?.isEnabled = true
                            dismissLoadingDialog()
                            showMessage(getString(R.string.set_successfully))
                            exitForSettingLevel()
                        }
                    }
                })
    }

    private fun exitForSettingLevel() {
        activity?.run {
            setResult(Activity.RESULT_OK)
            supportFinishAfterTransition()
        }
    }

    private fun exitForChoosingLevel(selectedGuardLevel: GuardLevelVO, sessionId: String) {
        activity?.run {
            val data = Intent()
            val guardItems = selectedGuardLevel.items.flatMap {
                it.item_list.filter { item -> item.must || item.isSelected }.map { item -> item.code }
            }
            data.putExtra(RouterPath.GuardLevel.SELECTED_LEVEL_INFO, SelectedLevelInfo(sessionId, selectedGuardLevel.guardLevel, guardItems))
            setResult(Activity.RESULT_OK, data)
            supportFinishAfterTransition()
        }
    }

    override fun onRefresh() {
        viewModel.retryLoadGuardLevel()
    }

    override fun handleBackPress(): Boolean {
        return isMustSetLevel() || isFromMigration
    }

}