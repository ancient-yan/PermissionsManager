package com.gwchina.parent.main.presentation.home

import android.content.Context
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.ContextType
import com.gwchina.parent.main.data.*
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.ApprovalClass
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.PhoneApprovalAbbreviation
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findChildByDeviceId
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.utils.*
import timber.log.Timber
import javax.inject.Inject


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 13:40
 */
@ActivityScope
class HomeMapper @Inject constructor(
        @ContextType private val context: Context,
        val appDataSource: AppDataSource
) {

    companion object {
        const val MAX_APP_APPROVAL_COUNT = 3
        const val MAX_PHONE_APPROVAL_COUNT = 1
    }

    fun transform(data: HomeResponse?): HomeVO? {
        return if (data == null) {
            HomeVO()
        } else {
            HomeVO(
                    childInfo = data.top_child_info,
                    deviceInfo = data.device_guard_item,
                    approvalInfo = buildApprovalInfo(data),
                    location = data.child_location,
                    useOverview = data.use_overview,
                    peerRecommendation = data.same_age_rec_install,
                    usingTrajectory = buildUsingTrajectoryList(data),
                    softRecommendInfo = data.rec_soft_info,
                    weekly = data.last_week_guard_report
            )
        }
    }

    /**
     * 7.0.1需求改成时间段
     */
    private fun buildUsingTrajectoryList(timeLineRecord: List<TimeLineRecord>?,isRecordList:Boolean=false): List<UsingTrajectoryItem>? {
        val list = mutableListOf<UsingTrajectoryItem>()
        if (timeLineRecord.isNullOrEmpty()) {
            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.today)))
            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))

            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.yesterday)))
            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))

            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.the_day_before_yesterday)))
            list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
            return list
        }
        build3DaysUsingTrajectoryList(timeLineRecord, list,isRecordList)
        return list
    }

    /**
     * 三天使用用的数据
     */
    private fun build3DaysUsingTrajectoryList(timeLineRecord: List<TimeLineRecord>, list: MutableList<UsingTrajectoryItem>,isRecordList:Boolean=false) {
        //今天是空的
        val isTodayDataEmpty = !timeLineRecord.any { isToday(it.group_time) }
        //前天是空的
        val isTheDayBeforeYesterdayEmpty = !timeLineRecord.any { isDayBeforeYesterday(it.group_time) }
        if (isTodayDataEmpty) {
            list.add(0, UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.today)))
            list.add(1, UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
        }

        val count = timeLineRecord.sumBy {
            it.group_data?.size ?: 0
        }

        buildData(timeLineRecord, list, count,isRecordList)

        if (isTheDayBeforeYesterdayEmpty) {
            if (count < 10||isRecordList) {
                list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.the_day_before_yesterday)))
                list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
            }
        }
    }

    private fun buildData(timeLineRecord: List<TimeLineRecord>, list: MutableList<UsingTrajectoryItem>, count: Int,isRecordList:Boolean) {
        var curDay: Long = 0
        //昨天数据是空的
        val isYesterdayDataEmpty = !timeLineRecord.any { isYesterday(it.group_time) }
        //今天最后一条数据
        val lastTodayData = timeLineRecord.filter {
            isToday(it.group_time)
        }
        if (lastTodayData.isEmpty()) {
            if (isYesterdayDataEmpty) {
                list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.yesterday)))
                list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
            }
            timeLineRecord.forEach {
                if (!isSameDay(curDay, it.group_time)) {
                    if (list.lastOrNull()?.type == UsingTrajectoryItem.TYPE_DAY) {
                        list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
                    }
                    list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, formatMillisecondsToDayDesc(it.group_time)))
                    curDay = it.group_time
                }

                if (!it.group_data.isNullOrEmpty()) {
                    val formatted = formatMilliseconds(it.group_time, "HH:mm")
                    val hour = formatted.substring(0, 2).toIntOrNull() ?: 0

                    val halfDayName = if (hour > 12) {
                        "${hour}:${formatted.subSequence(3, 5)}—${hour + 1}:00"
                    } else {
                        "$hour:${formatted.subSequence(3, 5)}—${hour + 1}:00"
                    }

                    list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_HALF_DAY, halfDayName))

                    it.group_data.forEach { gd ->
                        list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_ITEM, gd.use_title))
                    }
                }
            }
        } else {
            timeLineRecord.forEachIndexed { _, it ->
                if (!isSameDay(curDay, it.group_time)) {
                    if (list.lastOrNull()?.type == UsingTrajectoryItem.TYPE_DAY) {
                        list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
                    }
                    list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, formatMillisecondsToDayDesc(it.group_time)))
                    curDay = it.group_time
                }

                if (!it.group_data.isNullOrEmpty()) {
                    val formatted = formatMilliseconds(it.group_time, "HH:mm")
                    val hour = formatted.substring(0, 2).toIntOrNull() ?: 0

                    val halfDayName = if (hour > 12) {
                        "${hour}:${formatted.subSequence(3, 5)}—${hour + 1}:00"
                    } else {
                        "$hour:${formatted.subSequence(3, 5)}—${hour + 1}:00"
                    }

                    list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_HALF_DAY, halfDayName))

                    it.group_data.forEach { gd ->
                        list.add(UsingTrajectoryItem(UsingTrajectoryItem.TYPE_ITEM, gd.use_title))
                    }
                }
            }
            var size = 1
            lastTodayData.forEach {
                if (!it.group_data.isNullOrEmpty()) {
                    size += (it.group_data.size + 1)
                }
            }
            if (isYesterdayDataEmpty) {
                if (count < 10||isRecordList) {
                    list.add(size, UsingTrajectoryItem(UsingTrajectoryItem.TYPE_DAY, context.getString(com.gwchina.lssw.parent.home.R.string.yesterday)))
                    list.add(size + 1, UsingTrajectoryItem(UsingTrajectoryItem.TYPE_EMPTY))
                }
            }
        }
    }

    private fun buildUsingTrajectoryList(data: HomeResponse): List<UsingTrajectoryItem>? {
        return buildUsingTrajectoryList(data.time_line_record)
    }

    fun buildUsingTrajectoryList(data: UsingRecord): List<UsingTrajectoryItem>? {
        return buildUsingTrajectoryList(data.time_line_record,true)
    }

    private fun buildApprovalInfo(data: HomeResponse?): ApprovalInfoVO? {

        val approvalInfo = data?.approval_info ?: return null

        val approvalList = mutableListOf<Any>()

        val latestAppApprovalTime = approvalInfo.soft_approval?.record_approval?.firstOrNull()?.rule_soft?.firstOrNull()?.update_time
        val latestPhoneApprovalTime = approvalInfo.phone_approval?.phone_approval_list?.firstOrNull()?.update_time

        if (latestAppApprovalTime != null && latestAppApprovalTime > latestPhoneApprovalTime ?: 0) {
            /*应用审批信息*/
            addAppApprovalInfo(approvalInfo, approvalList)
            //亲情号码审批信息
            addPhoneApprovalInfo(approvalInfo, approvalList)
        } else {
            //亲情号码审批信息
            addPhoneApprovalInfo(approvalInfo, approvalList)
            /*应用审批信息*/
            addAppApprovalInfo(approvalInfo, approvalList)
        }

        return ApprovalInfoVO(approvalList)
    }

    private fun addPhoneApprovalInfo(approvalInfo: ApprovalInfo, approvalList: MutableList<Any>) {
        val phoneApproval = approvalInfo.phone_approval ?: return
        val phoneApprovalList = phoneApproval.phone_approval_list

        if (!phoneApprovalList.isNullOrEmpty()) {
            approvalList.add(ApprovalClass(ApprovalClass.PHONE))
            if (phoneApproval.phone_approval_count > MAX_PHONE_APPROVAL_COUNT) {
                approvalList.add(PhoneApprovalAbbreviation(phoneApproval.phone_approval_count))
            } else {
                approvalList.add(phoneApprovalList[0])
            }
        }
    }

    private fun addAppApprovalInfo(approvalInfo: ApprovalInfo, approvalList: MutableList<Any>) {
        val softApproval = approvalInfo.soft_approval ?: return

        val approvalSize = softApproval.record_approval.fold(0, { acc, record -> acc + record.rule_soft.size })

        if (approvalSize <= 0) {
            return
        }

        approvalList.add(ApprovalClass(ApprovalClass.APP))

        // 多余 MAX_APP_APPROVAL_COUNT 个
        if (approvalSize > MAX_APP_APPROVAL_COUNT) {

            val appUrls = mutableListOf<String>()

            for (recordApproval in softApproval.record_approval) {
                for (soft in recordApproval.rule_soft) {
                    if (appUrls.size == MAX_APP_APPROVAL_COUNT) {
                        break
                    }
                    appUrls.add(soft.soft_icon ?: "")
                }
            }

            approvalList.add(ApprovalInfoVO.SoftApprovalAbbreviation(softApproval.record_count, appUrls))

            return
        }

        //少于等于 MAX_APP_APPROVAL_COUNT 个
        var appRemaining = MAX_APP_APPROVAL_COUNT
        val user = appDataSource.user()
        val multiSoftApprovalList = mutableListOf<Any>()

        for (recordApproval in softApproval.record_approval) {
            //check
            if (appRemaining == 0) {
                break
            }
            //header
            val device = user.findDevice(recordApproval.device_id)
            val child = user.findChildByDeviceId(recordApproval.device_id)
            if (softApproval.child_count > 1 || child?.device_list?.size ?: 0 > 1) {
                val childName = context.getString(com.gwchina.lssw.parent.home.R.string.from_x2_of_x1_mask, recordApproval.nick_name.foldText(8), recordApproval.device_name)
                multiSoftApprovalList.add(ApprovalInfoVO.SoftApprovalRelation(childName, device?.index
                        ?: 0))
            }
            //soft list
            if (recordApproval.rule_soft.size > appRemaining) {
                multiSoftApprovalList.addAll(recordApproval.rule_soft.subList(0, appRemaining).map { ApprovalInfoVO.SoftWrapper(it, recordApproval.child_user_id, recordApproval.device_id) })
                appRemaining -= recordApproval.rule_soft.size
                break
            } else {
                multiSoftApprovalList.addAll(recordApproval.rule_soft.map { ApprovalInfoVO.SoftWrapper(it, recordApproval.child_user_id, recordApproval.device_id) })
                appRemaining -= recordApproval.rule_soft.size
            }
        }

        approvalList.add(ApprovalInfoVO.MultiSoftApproval(multiSoftApprovalList))
    }

    fun convertSoftApproval(softApproval: SoftApproval?): List<Any> {
        softApproval ?: return emptyList()

        val user = appDataSource.user()
        val multiSoftApprovalList = mutableListOf<Any>()

        for (recordApproval in softApproval.record_approval) {

            //header
            val device = user.findDevice(recordApproval.device_id)
            val child = user.findChildByDeviceId(recordApproval.device_id)
            if (softApproval.child_count > 1 || child?.device_list?.size ?: 0 > 1) {
                val childName = context.getString(com.gwchina.lssw.parent.home.R.string.from_x2_of_x1_mask, recordApproval.nick_name.foldText(8), recordApproval.device_name)
                multiSoftApprovalList.add(ApprovalInfoVO.SoftApprovalRelation(childName, device?.index
                        ?: 0))
            }

            //soft list
            multiSoftApprovalList.addAll(recordApproval.rule_soft.map { ApprovalInfoVO.SoftWrapper(it, recordApproval.child_user_id, recordApproval.device_id) })
        }

        return multiSoftApprovalList
    }

}
