package com.gwchina.parent.apps.presentation.rules

import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGroup

class GroupWrapper(val appGroup: AppGroup) {

    var groupIcons: List<String?> = emptyList()
    var groupDetails: String = ""
    var groupUsedTime: String = ""

}

class AppWrapper(val app: App) {

    var appUsedTime: String = ""

    var appNameWithType: CharSequence = ""

}