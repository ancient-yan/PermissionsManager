package com.gwchina.parent.level.presentation

import android.text.Html
import com.android.base.app.dagger.ActivityScope
import com.gwchina.parent.level.data.GroupItem
import com.gwchina.parent.level.data.GuardItem
import com.gwchina.parent.level.data.GuardLevel
import com.gwchina.sdk.base.data.api.isFlagPositive
import javax.inject.Inject


@ActivityScope
class Mapper @Inject constructor() {

    internal fun findItemIdList(guardLevelVO: GuardLevelVO): List<String> {

        val guardItemIdList = mutableListOf<String>()

        guardLevelVO.items.filter { it.item_list.isNotEmpty() }.forEach { groupItemVO ->
            guardItemIdList.addAll(groupItemVO.item_list.filter { it.isSelected }.map { it.id })
        }

        return guardItemIdList
    }

    internal fun findItemCodeList(guardLevelVO: GuardLevelVO): List<String> {
        val guardItemIdList = mutableListOf<String>()

        guardLevelVO.items.filter { it.item_list.isNotEmpty() }.forEach { groupItemVO ->
            guardItemIdList.addAll(groupItemVO.item_list.filter { it.isSelected }.map { it.code })
        }

        return guardItemIdList
    }

    internal fun convertToLevelItemVO(list: List<GuardLevel>?, oldGuardLevel: Int?, oldGuardItemList: List<com.gwchina.sdk.base.data.models.GuardItem>?): List<GuardLevelVO> {
        if (list.isNullOrEmpty()) {
            return emptyList()
        }

        return list.map {
            if (it.guard_level_code == oldGuardLevel) {
                convertToLevelItemVO(it, oldGuardItemList)
            } else {
                convertToLevelItemVO(it, null)
            }
        }
    }

    private fun convertToLevelItemVO(level: GuardLevel, oldGuardItemList: List<com.gwchina.sdk.base.data.models.GuardItem>?): GuardLevelVO {
        val items = level.group_list?.map {
            convertToGuardGroupItemVO(it, oldGuardItemList)
        } ?: emptyList()

        return GuardLevelVO(
                level.guard_level_id,
                level.guard_level_code,
                level.guard_level_name ?: "",
                Html.fromHtml(level.guard_level_desc),
                items
        )
    }

    private fun convertToGuardGroupItemVO(groupItem: GroupItem, oldGuardItemList: List<com.gwchina.sdk.base.data.models.GuardItem>?): GuardGroupItemVO {
        val items = groupItem.item_list?.map {
            convertToGuardItemVO(it, isGuardItemSelected(oldGuardItemList, it.guard_item_code
                    ?: "", true))
        } ?: emptyList()

        return GuardGroupItemVO(
                groupItem.group_name,
                groupItem.member_only,
                items)
    }

    private fun convertToGuardItemVO(guardItem: GuardItem, selected: Boolean): GuardItemVO {
        return GuardItemVO(
                guardItem.guard_item_id,
                guardItem.guard_item_code ?: "",
                guardItem.guard_item_name ?: "",
                guardItem.guard_item_desc ?: "",
                guardItem.guard_item_icon.replaceAfterLastStartFromDelimiterIndex(".", "_checked.png"),
                selected,
                isFlagPositive(guardItem.is_item_req)
        )
    }

    private fun isGuardItemSelected(oldGuardItemList: List<com.gwchina.sdk.base.data.models.GuardItem>?, itemCode: String, defaultVal: Boolean): Boolean {
        if (oldGuardItemList == null) {
            return defaultVal
        }

        oldGuardItemList.find {
            it.guard_item_code == itemCode
        } ?: return false

        return true
    }

    private fun String?.replaceAfterLastStartFromDelimiterIndex(delimiter: String, replacement: String): String {
        if (this.isNullOrEmpty()) {
            return ""
        }
        val index = lastIndexOf(delimiter)
        return if (index == -1) this else replaceRange(index, length, replacement)
    }

}