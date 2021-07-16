package com.gwchina.parent.daily.data

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-12 17:53
 */
data class DailyRecord(
        val child_count: Int = 0,
        var life_record: MutableList<LifeRecord>? = null,
        val message_count: Int = 0
)

data class LifeRecord(
        val `receiver`: List<Receiver>? = null,
        var comment: MutableList<Comment>? = null,
        val content: String? = null,
        val create_time: Long = 0,
        val life_photo: List<LifePhoto>? = null,
        val life_record_id: String? = null,
        val text_id: String? = null,
        val user_id: String? = null,
        var isExpand: Int = 0,
        var isFooterType: Boolean = false
)

data class Receiver(
        val nick_name: String? = null,
        val rec_user_id: String? = null
)

data class LifePhoto(
        val life_record_id: String? = null,
        val photo_id: String? = null,
        val photo_path: String? = null
)

data class Comment(
        var comment_id: String? = null,
        var comment_user_head: String? = null,
        var comment_user_id: String? = null,
        var comment_user_name: String? = null,
        var content: String? = null,
        var create_time: Long = 0,
        var reply: MutableList<Reply>? = null
)

data class Reply(
        var comment_id: String? = null,
        var comment_user_head: String? = null,
        var comment_user_id: String? = null,
        var comment_user_name: String? = null,
        var content: String? = null,
        var create_time: Long = 0,
        var reply_comment_id: String? = null,
        var reply_user_id: String? = null,
        var reply_user_name: String? = null
)