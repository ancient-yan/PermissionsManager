package com.gwchina.parent.main.presentation.home

import com.gwchina.sdk.base.data.models.Device

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-03 14:52
 */
internal fun Device?.hasGuardItem(guardItemCode: String): Boolean {
    return this?.guard_item_list?.any {
        it.guard_item_code == guardItemCode
    } ?: false
}