package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.util.AttributeSet
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.AllInstructionState
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.widget.views.InstructionStateView
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundLinearLayout
import kotlinx.android.synthetic.main.home_card_instruction.view.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-23 13:37
 */
class InstructionStateCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : QMUIRoundLinearLayout(context, attrs, defStyleAttr) {

    /**重试发生指令*/
    var onRetryClickListener: ((instructionType: String) -> Unit)? = null

    var mIsvHomeLevel: InstructionStateView
    var mIsvHomeTime: InstructionStateView
    var mIsvHomeApp: InstructionStateView
    var mIsvHomeNet: InstructionStateView
    var mIsvHomePhone: InstructionStateView

    init {
        orientation = VERTICAL
        setPadding(0, dip(5), 0, dip(5))
        setBackgroundResource(R.drawable.shape_red_op6_round5)
        inflate(context, R.layout.home_card_instruction, this)
        mIsvHomeLevel = isvHomeLevel
        mIsvHomeTime = isvHomeTime
        mIsvHomeApp = isvHomeApp
        mIsvHomeNet = isvHomeNet
        mIsvHomePhone = isvHomePhone
        setupViews()
    }

    private fun setupViews() {
        isvHomeLevel.onRetryClickListener = {
            onRetryClickListener?.invoke(INSTRUCTION_SYNC_LEVEL)
        }

        isvHomeTime.onRetryClickListener = {
            onRetryClickListener?.invoke(INSTRUCTION_SYNC_TIME)
        }

        isvHomeApp.onRetryClickListener = {
            onRetryClickListener?.invoke(INSTRUCTION_SYNC_APP)
        }

        isvHomeNet.onRetryClickListener = {
            onRetryClickListener?.invoke(INSTRUCTION_SYNC_URL)
        }

        isvHomePhone.onRetryClickListener = {
            onRetryClickListener?.invoke(INSTRUCTION_SYNC_PHONE)
        }
    }

    /**
     * [syncStateMap] key为同步指令名-设备id value:该指令是否同步成功
     */
    fun showState(allInstructionState: AllInstructionState, deviceName: String) {
        val childDeviceId = AppContext.appDataSource().user().currentDevice?.device_id
        val childUserId = AppContext.appDataSource().user().currentChild?.child_user_id
        if (childDeviceId == null || childUserId == null) return
        val map = mutableMapOf<String, Int>()
        map[INSTRUCTION_SYNC_PHONE] = allInstructionState.guard_phone_syn
        map[INSTRUCTION_SYNC_TIME] = allInstructionState.guard_time_syn
        map[INSTRUCTION_SYNC_APP] = allInstructionState.guard_soft_syn
        map[INSTRUCTION_SYNC_URL] = allInstructionState.guard_url_syn
        map[INSTRUCTION_SYNC_LEVEL] = allInstructionState.guard_level_syn
        SyncManager.getInstance().homeSyncState(context, map, childUserId, childDeviceId) {
            it.forEach { (key, syncResult) ->
                when (key.split("-")[1]) {
                    INSTRUCTION_SYNC_LEVEL -> showStateInfo(isvHomeLevel, syncResult, getString(R.string.instruction_level_name))
                    INSTRUCTION_SYNC_TIME -> showStateInfo(isvHomeTime, syncResult, getString(R.string.instruction_time_name))
                    INSTRUCTION_SYNC_APP -> showStateInfo(isvHomeApp, syncResult, getString(R.string.instruction_app_name))
                    INSTRUCTION_SYNC_URL -> showStateInfo(isvHomeNet, syncResult, getString(R.string.instruction_net_name))
                    INSTRUCTION_SYNC_PHONE -> showStateInfo(isvHomePhone, syncResult, getString(R.string.instruction_phone_name))
                }
            }
        }
    }

    private fun showStateInfo(stateView: InstructionStateView, syncResult: Int, stateName: String) {
        when (syncResult) {
            0 -> stateView.showInitFailedState(stateName)
            1 -> stateView.gone()
            2 -> stateView.showSyncingState(stateName)
        }
    }

    private fun getString(resId: Int) = context.getString(resId)


}
