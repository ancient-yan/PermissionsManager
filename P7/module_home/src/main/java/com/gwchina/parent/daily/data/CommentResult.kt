package com.gwchina.parent.daily.data

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 16:27
 */
data class CommentResult(
        val comment_id: String = "",
        val comment_user_head: String? = "",
        val comment_user_id: String? = "",
        val comment_user_name: String? = "",
        val content: String? = "",
        val reply_user_id: String = "",
        val reply_user_name: String? = ""
)