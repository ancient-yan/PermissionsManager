package com.gwchina.parent.main.presentation.home

import com.gwchina.parent.main.data.*
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.*
import com.gwchina.sdk.base.data.models.Device

data class HomeVO(
        val childInfo: TopChildInfo? = null,
        val deviceInfo: Device? = null,
        val approvalInfo: ApprovalInfoVO? = null,
        val useOverview: UseOverview? = null,
        val usingTrajectory: List<UsingTrajectoryItem>? = null,
        val location: ChildLocation? = null,
        val peerRecommendation: List<Soft>? = null,
        val weekly: Weekly? = null,
        val softRecommendInfo: SoftRecommendInfo? = null
)


data class UsingTrajectoryItem(
        val type: Int,
        val content: String = ""
) {
    companion object {
        //使用记录的最后一行
        const val  TYPE_FOOTER=0
        const val TYPE_DAY = 1
        const val TYPE_HALF_DAY = 2
        const val TYPE_ITEM = 3
        const val TYPE_EMPTY = 4
    }
}


data class ApprovalInfoVO(
        /**all approval info, include [ApprovalClass]、[MultiSoftApproval] 、[PhoneApprovalInfo]、[SoftApprovalAbbreviation]、[PhoneApprovalAbbreviation]*/
        val approvalList: List<Any>
) {

    data class ApprovalClass(val type: Int) {
        companion object {
            const val APP = 1
            const val PHONE = 2
        }
    }

    data class MultiSoftApproval(
            /**soft approval info, include [SoftApprovalRelation] 、 [SoftWrapper]*/
            val softList: List<Any>
    )

    data class SoftApprovalRelation(
            val childName: CharSequence,
            val deviceIndex: Int
    )

    data class SoftWrapper(
            val soft: Soft,
            val childUserId: String,
            val childDeviceId: String
    )

    data class SoftApprovalAbbreviation(
            val size: Int,
            val appIcons: List<String>
    )

    data class PhoneApprovalAbbreviation(val size: Int)

}

data class LocationDetail(
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val address: String = ""
)