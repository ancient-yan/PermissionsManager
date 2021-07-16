package com.gwchina.parent.times.presentation.spare

import com.gwchina.sdk.base.utils.TimePeriod

data class SparePlanHeader(
        val batchId: String,
        val batchName: String,
        val weeklyTimeDesc: CharSequence,
        val weeklyTime: Int,
        var using: Boolean,
        var expanded: Boolean,
        val content: List<SparePlanContent>
)

data class SparePlanContent(
        val planId: String,
        val contentDesc: CharSequence,
        val timePeriod: List<TimePeriod>,
        val enableTime: Int = 0,
        val days: List<Int>,
        val dayIds: List<String>,
        val isBottom: Boolean
)

data class SparePlanBottom(
        val batchId: String,
        var using: Boolean
)