package com.gwchina.sdk.base.data


private const val CATEGORY_HOST = "接口环境"
private const val H5_HOST = "H5环境"
private const val SALE_HOST="分销环境"

internal fun getBaseUrl(): String {
    return EnvironmentContext.selected(CATEGORY_HOST).url
}

internal fun getBaseWebUrl(): String {
    return EnvironmentContext.selected(H5_HOST).url
}

internal fun getSaleBaseUrl():String{
    return EnvironmentContext.selected(SALE_HOST).url
}

internal fun addAllHost() {
    EnvironmentContext.startEdit {
        add(CATEGORY_HOST, Environment("测试", "https://mstes.dev.zhixike.net/greenguard/"))
        add(CATEGORY_HOST, Environment("开发", "https://msdev.dev.zhixike.net/greenguard/"))
        add(CATEGORY_HOST, Environment("压测", "https://mspres.dev.zhixike.net/greenguard/"))
        add(CATEGORY_HOST, Environment("正式", "https://ms.gwchina.cn/greenguard/"))
        add(CATEGORY_HOST, Environment("测试(雷勇)","http://192.168.2.129:12710/"))
        add(CATEGORY_HOST, Environment("测试(王仕林)", "http://192.168.2.112:12710/"))

        add(H5_HOST, Environment("测试", "https://g8dtes.dev.zhixike.net/gelei-guard-h5/v7.1.0/"))
        add(H5_HOST, Environment("压测", "https://g8dpres.dev.zhixike.net/gelei-guard-h5/v7.1.0/"))
        add(H5_HOST, Environment("开发", "https://g8ddev.dev.zhixike.net/gelei-guard-h5/v7.1.0/"))
        add(H5_HOST, Environment("本地(夏双喜)", "http://192.168.2.96:5000/"))
        add(H5_HOST, Environment("本地(夏双喜2)", "http://192.168.1.103:5000/"))
        add(H5_HOST, Environment("本地(叶秀恋)", "http://172.168.30.43:5000/"))
        add(H5_HOST, Environment("正式", "https://greenguard-h5.gwchina.cn/gelei-guard-h5/v7.1.0/"))

        add(SALE_HOST,Environment("测试", "https://m.earn.kf.gwchina.cn"))
        add(SALE_HOST,Environment("正式", "https://m.earn.gwchina.cn"))
    }
}

internal fun addReleaseHost() {
    EnvironmentContext.startEdit {
        add(CATEGORY_HOST, Environment("正式", "https://ms.gwchina.cn/greenguard/"))
        add(H5_HOST, Environment("正式", "https://greenguard-h5.gwchina.cn/gelei-guard-h5/v7.1.0/"))
        add(SALE_HOST,Environment("正式", "https://m.earn.gwchina.cn"))
    }
}