package com.gwchina.parent.family.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.core.Result
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.family.data.model.Approval
import com.gwchina.parent.family.data.model.FamilyPhoneInfo
import com.gwchina.parent.family.data.model.GroupPhone
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

@ActivityScope
class FamilyPhoneRepository @Inject constructor(
        private val familyPhoneApi: FamilyPhoneApi
) {

    /**
     * 设置亲情号码拦截开关
     */
    fun setFamilyPhoneGuard(child_user_id: String,
                            enabled: Boolean?,
                            is_call_out: Boolean?,
                            is_call_in: Boolean?): Completable {

        var enabledStr: String? = null
        var isCallOutStr: String? = null
        var isCallInStr: String? = null
        enabled?.let {
            enabledStr = if (it) "1" else "0"
        }

        is_call_out?.let {
            isCallOutStr = if (it) "1" else "0"
        }

        is_call_in?.let {
            isCallInStr = if (it) "1" else "0"
        }


        return familyPhoneApi.setFamilyPhoneGuard(child_user_id, enabledStr, isCallOutStr, isCallInStr)
                .resultChecker()
                .ignoreElements()

    }

    /**
     * 获取分组信息
     */
    fun getAllGroupPhone(child_user_id: String): Flowable<Optional<List<GroupPhone>>> {
        return familyPhoneApi
                .getAllGroupPhone(child_user_id)
                .optionalExtractor()
    }

    /**
     * 获取分组信息
     */
    fun getGroupPhone(child_user_id: String, group_id: String): Observable<Optional<GroupPhone>> {
        return familyPhoneApi
                .getGroupPhone(child_user_id, group_id)
                .optionalExtractor()
    }


    /**
     * 添加联系人
     */
    fun addFamilyPhone(phone: String,
                       phone_remark: String,
                       group_name: String?,
                       group_id: String?,
                       child_user_id: String): Completable {
        return familyPhoneApi.addFamilyPhone(phone, phone_remark, group_name, group_id, child_user_id)
                .resultChecker()
                .ignoreElements()
    }


    /**
     * 编辑联系人
     */
    fun editFamilyPhone(phone: String,
                        phone_remark: String,
                        group_id: String?,
                        rule_id: String,
                        child_user_id: String): Completable {

        return familyPhoneApi.editFamilyPhone(phone, phone_remark, group_id, rule_id, child_user_id)
                .resultChecker()
                .ignoreElements()

    }


    /**
     * 删除联系人
     */
    fun delFamilyPhone(rule_id: String,
                       child_user_id: String): Completable {
        return familyPhoneApi.delFamilyPhone(rule_id, child_user_id)
                .resultChecker()
                .ignoreElements()
    }


    /**
     * 添加分组
     */
    fun addGroup(group_name: String,
                 child_user_id: String): Observable<Result<Unit>> {
        return familyPhoneApi.addGroup(group_name, child_user_id)
                .resultChecker()
    }


    /**
     * 删除分组  多个分组以,分割
     */
    fun delGroup(group_ids: String,
                 child_user_id: String): Completable {
        return familyPhoneApi.delGroup(group_ids, child_user_id)
                .resultChecker()
                .ignoreElements()
    }

    /**
     * 更新分组
     */
    fun updateGroup(group_ids: String,//分组ID  多个用“,”隔开
                    group_name: String?,
                    is_call_out: String?,// 是否限制呼出 1是0否
                    is_call_in: String?,// 是否限制呼入
                    child_user_id: String): Completable {
        return familyPhoneApi.updateGroup(group_ids, group_name, is_call_out, is_call_in, child_user_id)
                .resultChecker()
                .ignoreElements()

    }


    /**
     * 获取待审批列表
     */
    fun getApprovalRecord(child_user_id: String): Flowable<Optional<List<Approval>>> {
        return familyPhoneApi.getApprovalRecord(child_user_id)
                .optionalExtractor()

    }

    /**
     * 审批手机号
     */
    fun approvalPhone(record_id: String,
                      rule_type: String,
                      child_user_id: String): Completable {

        return familyPhoneApi.approvalPhone(record_id, rule_type, child_user_id)
                .resultChecker()
                .ignoreElements()
    }


    fun getFamilyPhoneInfo(child_user_id: String): Flowable<Optional<FamilyPhoneInfo>> {
        return familyPhoneApi.getFamilyPhoneInfo(child_user_id)
                .optionalExtractor()

    }


}