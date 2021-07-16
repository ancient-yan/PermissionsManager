package com.gwchina.parent.net.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.cache.flowableOptional
import com.android.sdk.net.kit.CombinedResult
import com.android.sdk.net.kit.combineMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.net.data.model.AppRuleInfo
import com.gwchina.parent.net.data.model.GuardRecord
import com.gwchina.parent.net.data.model.RuleUrlInfo
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-15 17:35
 *      Desc : 上网守护model层
 */
@ActivityScope
class NetGuardRepository @Inject constructor(
        private val netGuardApi: NetGuardApi,
        private val storageManager: StorageManager
) {

    companion object {
        private const val RULE_URL_INFO = "rule_url_info"
        private const val OPEN_PATTERN = "0"
        private const val INTERCEPTION_PATTERN = "1"
    }

    /**
     * 获取上网守护基本情况
     */
    fun getRuleUrlBaseInfo(child_user_id: String, child_device_id: String): Flowable<CombinedResult<RuleUrlInfo>> {

        val remote = netGuardApi.getRuleUrlBaseInfo(child_user_id, child_device_id).optionalExtractor()

        val local = storageManager.stableStorage().flowableOptional<RuleUrlInfo>(RULE_URL_INFO)

        return combineMultiSource(remote, local) {
            if (it != null) {
                storageManager.stableStorage().putEntity(RULE_URL_INFO, it)
            }
        }
    }

    /**
     * 更新上网守护模式
     */
    fun updateRuleUrlPattern(pattern_id: String, isChecked: Boolean, child_user_id: String, child_device_id: String): Completable {

        val patternType = if (isChecked) INTERCEPTION_PATTERN else OPEN_PATTERN

        return netGuardApi.updateRuleUrlPattern(pattern_id, patternType, child_user_id, child_device_id)
                .resultChecker()
                .ignoreElements()

    }

    /**
     * 获取拦截记录
     */
    fun getGuardRecordList(pos: Int, limit: Int, child_user_id: String, child_device_id: String): Observable<Optional<List<GuardRecord>>> {
        return netGuardApi.getGuardRecordList(pos, limit, child_user_id, child_device_id).optionalExtractor()
    }

    /**
     * 获取黑白名单网址列表
     */
    fun getSiteList(list_type: String, pos: Int, limit: Int, child_user_id: String, child_device_id: String): Flowable<Optional<List<SiteInfo>>> {
        return netGuardApi.getSiteList(list_type, pos, limit, child_user_id, child_device_id).optionalExtractor()
    }

    /**
     * 添加网络地址
     */
    fun addUrl(rule_type: String, url: String, url_name: String?, list_type: String, child_user_id: String, child_device_id: String): Completable {

        return netGuardApi.addUrl(rule_type, url, url_name, list_type, child_user_id, child_device_id)
                .resultChecker()
                .ignoreElements()
    }

    /**
     * 更新地址
     */
    fun updateUrl(rule_type: String,
                  url: String,
                  url_name: String?,
                  rule_id: String,
                  list_type: String,
                  child_user_id: String,
                  child_device_id: String): Completable {

        return netGuardApi.updateUrl(rule_type, url, url_name, rule_id, list_type, child_user_id, child_device_id)
                .resultChecker()
                .ignoreElements()
    }

    /**
     * 删除网址
     */
    fun deleteUrls(rule_id_list: List<String>, child_user_id: String, child_device_id: String): Completable {

        return netGuardApi.deleteUrls(JsonUtils.toJson(rule_id_list), child_user_id, child_device_id)
                .resultChecker()
                .ignoreElements()
    }

    /**
     * 查询应用守护信息
     */
    fun queryAppInfo(child_user_id: String, child_device_id: String, bundle_id: String): Observable<Optional<AppRuleInfo>> {
        return netGuardApi.queryAppInfo(bundle_id, child_user_id, child_device_id).optionalExtractor()
    }

}