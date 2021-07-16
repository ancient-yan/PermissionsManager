package com.gwchina.parent.times.presentation.make

import android.os.Parcelable
import com.android.base.kotlin.toArrayList
import kotlinx.android.parcel.Parcelize

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-25 13:53
 */
@Parcelize
class MakingPlanInfo(
        val notOptionalDays: List<Int>? = null,
        val selectedDays: List<Int>? = null,
        val usedName: List<String>? = null,
        val makingType: Int
) : Parcelable {

    companion object {

        const val TYPE_NORMAL = 1
        const val TYPE_SPARE = 2

        fun newInstanceForMakingSparePlan(usedName: List<String>? = null) = MakingPlanInfo(
                usedName = usedName.toArrayList(),
                makingType = TYPE_SPARE
        )

        fun newInstanceForMakingNormalPlan(notOptionalDays: List<Int>? = null, selectedDays: List<Int>? = null) = MakingPlanInfo(
                notOptionalDays.toArrayList(),
                selectedDays.toArrayList(),
                makingType = TYPE_NORMAL
        )
    }

}