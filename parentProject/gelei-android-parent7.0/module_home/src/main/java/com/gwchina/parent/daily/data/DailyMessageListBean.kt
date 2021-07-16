package com.gwchina.parent.daily.data

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 18:53
 */
data class DailyMessageListBean(
        val comment_id: String="",
        val comment_time: Long,
        val comment_user_id: String="",
        val comment_user_name: String?="",
        val commented_content: String?="",
        val commented_user_id: String="",
        val commented_user_name: String?="",
        val is_delete: String?="",
        val is_look: String?="",
        val life_circle_content: String?="",
        val life_photo: List<LifePhoto?>? = null,
        val life_record_id: String="",
        val life_record_user_id: String="",
        val life_record_user_name: String?="",
        val record_id: String="",
        val user_id: String=""
)

