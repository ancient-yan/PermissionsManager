package com.gwchina.parent.message.data

/**消息*/
data class Message(
        val create_time: Long,
        val is_read: String?="",
        val msg_content: String?="",
        val msg_title: String?="",
        val msg_type: String?,
        val record_id: String,
        val msg_icon: String?,
        val send_time: Long
)
