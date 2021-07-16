package com.gwchina.parent.message.presentation

import com.gwchina.parent.message.data.Message
import com.gwchina.sdk.base.utils.formatMillisecondsToDetailDesc
import javax.inject.Inject

/*消息类型*/
internal const val TIME_FAIL = "TIME_FAIL"
internal const val TIME_TEMP_FAIL = "TIME_TEMP_FAIL"
internal const val TIME_SUCCEED = "TIME_SUCCEED"
internal const val TIME_TEMP_SUCCEED = "TIME_TEMP_SUCCEED"
internal const val SOFT_FAIL = "SOFT_FAIL"
internal const val SOFT_APPROVAL_FAIL = "SOFT_APPROVAL_FAIL"
internal const val SOFT_APPROVAL = "SOFT_APPROVAL"
internal const val SOFT_UNINSTALL = "SOFT_UNINSTALL"
internal const val SOFT_REC_INSTALL_FAIL = "REC_INSTALL_FAIL"
internal const val TREE_FALL = "TREE_FALL"
internal const val TREE_YELLOW = "TREE_YELLOW"
internal const val DEVICE_LOWPOWER = "DEVICE_LOWPOWER"
internal const val ADD_MEMBER = "ADD_MEMBER"

data class MessageWrapper(val message: Message, val timeDesc: String)

class MessageMapper @Inject constructor() {

    fun map(list: List<Message>?): List<MessageWrapper> {
        if (list == null) {
            return emptyList()
        }
        return list.map {
            MessageWrapper(it, formatMillisecondsToDetailDesc(it.create_time))
        }
    }

}