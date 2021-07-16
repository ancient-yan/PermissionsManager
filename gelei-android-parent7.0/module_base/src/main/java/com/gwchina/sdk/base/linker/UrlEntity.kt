package com.gwchina.sdk.base.linker

import java.util.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-05-15 18:28
 */
class UrlEntity(
        /**基础url*/
        var baseUrl: String,
        /** url参数*/
        var params: Map<String, String>
) {

    val id: String?
        get() = params[JUMP_PARAM_ID]

    companion object {

        /**
         * 解析url
         *
         * @param url
         * @return
         */
        fun from(url: String?): UrlEntity? {
            var localUrl = url ?: return null
            localUrl = localUrl.trim { it <= ' ' }
            if (localUrl == "") {
                return null
            }
            val urlParts = localUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val baseUrl = urlParts[0]
            //没有参数
            if (urlParts.size == 1) {
                return UrlEntity(baseUrl, emptyMap())
            }
            //有参数
            val params = urlParts[1].split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val map = HashMap<String, String>()
            for (param in params) {
                val keyValue = param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (keyValue.size >= 2) {
                    map[keyValue[0]] = keyValue[1]
                }
            }
            return UrlEntity(baseUrl, map)
        }

    }

}