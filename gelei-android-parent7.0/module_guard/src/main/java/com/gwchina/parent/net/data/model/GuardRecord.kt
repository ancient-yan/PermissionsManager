package com.gwchina.parent.net.data.model

import com.gwchina.sdk.base.utils.formatMillisecondsToDayDesc
import javax.inject.Inject

data class GuardRecord(
        val create_time: Long,
        val record_id: String,
        val type: String?,
        val url: String?,
        val url_name: String?,
        var time: String?
)


class GuardRecordMapper @Inject constructor() {

    fun transformToGuardRecordList(list: List<GuardRecord>?): List<GuardRecord> {

        return list?.map {

            val createTime = it.create_time
            it.time = formatMillisecondsToDayDesc(createTime)
            it

        } ?: emptyList()

    }

}