package com.gwchina.sdk.base.third.share

enum class SharePlatform {
    WeChat, WeChatMoment, QQ
}

/**
 * [thumbData]可传类型：资源文件、图片url、本地路径
 */
class ShareURL(
        val platform: SharePlatform,
        val url: String,
        val title: String,
        val description: String,
        val thumbData: Any? = null
)


