package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.apps.common.*
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGroup
import com.gwchina.parent.apps.data.AppGuardRepository
import com.gwchina.parent.apps.data.AppListResponse
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.utils.generateDeviceFlag
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-27 15:43
 */
class AppRulesViewModel @Inject constructor(
        private val appGuardRepository: AppGuardRepository,
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String,
        @ContextType private val context: Context,
        private val storageManager: StorageManager,
        val appDataSource: AppDataSource
) : ArchViewModel() {

    init {
        Timber.d("childDeviceId ${childDeviceId}")
        Timber.d("childUserId ${childUserId}")
    }

    companion object {
        private const val HIGH_RISK_TABLE_SHOWED_KEY = "high_risk_table_showed_key_"
        private const val ADDED_APP_GROUP_KEY = "added_app_group_key"
    }

    private val highRiskTableShowedKey = "${HIGH_RISK_TABLE_SHOWED_KEY}_$childDeviceId"

    /**整体加载状态*/
    private val _loadingAppRulesStatus = MutableLiveData<Resource<Any>>()
    val loadingAppRulesStatus: LiveData<Resource<Any>>
        get() = _loadingAppRulesStatus

    /**Table状态*/
    private val _tableStatus = MutableLiveData<TableStatus>()
    val tableStatus: LiveData<TableStatus>
        get() = _tableStatus

    private val _freeList = MutableLiveData<List<App>>()
    val freeList: LiveData<List<App>>
        get() = _freeList

    private val _riskList = MutableLiveData<List<App>>()
    val riskList: LiveData<List<App>>
        get() = _riskList

    private val _disabledList = MutableLiveData<List<App>>()
    val disabledList: LiveData<List<App>>
        get() = _disabledList

    private val _limitedList = MutableLiveData<Pair<List<App>, List<AppGroup>>>()
    val limitedList: LiveData<Pair<List<App>, List<AppGroup>>>
        get() = _limitedList

    private val _pendingApprovalList = MutableLiveData<List<App>>()
    val pendingApprovalList: LiveData<List<App>>
        get() = _pendingApprovalList

    val hasPendingApprovalList: LiveData<Boolean> = Transformations.map(pendingApprovalList) {
        it != null && it.isNotEmpty()
    }

    /**是否有审批记录*/
    private val _hasApprovalRecords = MutableLiveData<Boolean>()
    val hasApprovalRecords: LiveData<Boolean>
        get() = _hasApprovalRecords

    val deviceFlagName = LiveDataReactiveStreams.fromPublisher(
            appDataSource.observableUser()
                    .map { generateDeviceFlag(context, it, childDeviceId) }
    )

    /**是否有自由可用的App*/
    var hasFreeAppWithoutSystemApp: Boolean = false
        private set

    /**是否有限时可用的App*/
    var hasLimitedApp: Boolean = false
        private set

    /**是否有禁止使用的App*/
    var hasDisabledApp: Boolean = false
        private set

    /**是否添加过group*/
    @Volatile
    var hasAddedGroup = false
        private set

    /**是否在加载中*/
    @Volatile
    private var isLoading: Boolean = false

    /**是否已经加载过数据*/
    var hasLoadedData = false
        private set

    /**禁止可用和限时可用最大个数限制*/
    val maxCount: Int
        get() = appDataSource.user().vipRule?.app_forbidden_timelimit_count ?: 0

    init {
        hasAddedGroup = storageManager.stableStorage().getBoolean(ADDED_APP_GROUP_KEY, false)
        loadAppRules()
    }

    private fun loadAppRules() {
        _loadingAppRulesStatus.value = Resource.loading()

        appGuardRepository.appRules(childUserId, childDeviceId)
                .doOnTerminate { isLoading = false }
                .autoDispose()
                .subscribe(
                        {
                            hasLoadedData = it.isPresent
                            distributeResult(it.orElse(null))
                            _loadingAppRulesStatus.postValue(Resource.success())
                        },
                        {
                            _loadingAppRulesStatus.postValue(Resource.error(it))
                        }
                )
    }

    private fun distributeResult(response: AppListResponse?) {
        if (response != null) {
            //自由可用的
            val freeApp = response.soft_single_list.filter { it.rule_type.isFreeUsable() || it.isMustFreeUsable() }
            //禁用的
            val disabledApp = response.soft_single_list.filter { it.rule_type.isDisabled() }
            //待批准列表
            val pendingList = response.soft_single_list.filter { it.rule_type.isPendingApproval() }
            //高危应用
            val highRiskList = response.soft_single_list.filter { it.rule_type.isHighRisk() }

            //限时可用的
            val limitedApp = Pair(
                    response.soft_single_list.filter { it.rule_type.isLimitedUsable() },
                    response.soft_group_list
            )

            hasFreeAppWithoutSystemApp = freeApp.isNotEmpty() && freeApp.any { !it.isMustFreeUsable() }
            hasLimitedApp = limitedApp.first.isNotEmpty()
            hasDisabledApp = disabledApp.isNotEmpty()

            //tab 状态
            val status = TableStatus(
                    storageManager.stableStorage().getBoolean(highRiskTableShowedKey, false),
                    highRiskList.isNotEmpty()
            )
            if (status.hasRiskApp) {
                storageManager.stableStorage().putBoolean(highRiskTableShowedKey, true)
            }
            _tableStatus.postValue(status)

            //数据
            _freeList.postValue(freeApp)
            _pendingApprovalList.postValue(pendingList)
            _disabledList.postValue(disabledApp)
            _limitedList.postValue(limitedApp)
            _riskList.postValue(highRiskList)

            _hasApprovalRecords.postValue(response.is_has_approved_record == FLAG_POSITIVE)

        } else {

            hasFreeAppWithoutSystemApp = false
            hasLimitedApp = false
            hasDisabledApp = false
            _hasApprovalRecords.postValue(false)

            _freeList.postValue(emptyList())
            _limitedList.postValue(Pair(emptyList(), emptyList()))
            _disabledList.postValue(emptyList())
            _riskList.postValue(emptyList())
        }
    }

    fun isRestricted(otherCount: Int = 0): Boolean {
        if (maxCount != 0) {
            val disabledListSize = disabledList.value?.size ?: 0
            val limitedListSize = limitedList.value?.first?.size ?: 0
            var limitedGroupSize = 0
            limitedList.value?.second?.forEach {
                limitedGroupSize += it.soft_list.size
            }
            return disabledListSize + limitedListSize + limitedGroupSize + otherCount>= maxCount
        }
        return false
    }

    fun isCanAddApp(currentApp: App, selectinMap: HashMap<String, App>): Boolean {
        //说明将要添加自由可用为禁止可用/限时可用
        if (currentApp.rule_type.isFreeUsable()) {
//            val otherCount = freeList.value?.filter { selectinMap.containsKey(it.rule_id) }?.size?: 0
            val otherCount = selectinMap.filter { it.value.rule_type.isFreeUsable() }.size
            return isRestricted(otherCount)
        }
        return false
    }

    fun refreshList() {
        if (!isLoading) {
            isLoading = true
            loadAppRules()
        }
    }

    fun appListCanBeDisable(): List<App> {
        return mutableListOf<App>().apply {
            addSingleLimited(this)
            addFree(this)
        }
    }

    fun appListCanBeFree(): List<App> {
        return mutableListOf<App>().apply {
            addSingleLimited(this)
            addDisabled(this)
        }
    }

    fun appListCanBeLimited(): List<App> {
        return mutableListOf<App>().apply {
            addDisabled(this)
            addFree(this)
        }
    }

    private fun addFree(list: MutableList<App>) {
        _freeList.value?.let {
            list.addAll(it.filter { app ->
                !app.isMustFreeUsable()
            })
        }
    }

    private fun addSingleLimited(list: MutableList<App>) {
        _limitedList.value?.let {
            list.addAll(it.first)
        }
    }

    private fun addDisabled(list: MutableList<App>) {
        _disabledList.value?.let {
            list.addAll(it)
        }
    }

    fun updateAppRuleType(list: List<App>, ruleType: Int, isApproval: Boolean = false): LiveData<Resource<Any>> {
        return appGuardRepository.updateAppRule(childUserId, childDeviceId, list.generateRuleIds(), ruleType.toString(), isApproval = isApproval).toResourceLiveData()
    }

    fun deleteAppGroup(group: AppGroup): LiveData<Resource<Any>> {
        val groupId = (group.soft_group_id ?: "")
        return appGuardRepository.deleteAppGroup(childUserId, childDeviceId, groupId).toResourceLiveData()
    }

    fun addAppGroup(group: AppGroup): LiveData<Resource<Any>> {
        return appGuardRepository.addAppGroup(childUserId, childDeviceId, group)
                .doOnComplete {
                    //是否添加过group标识
                    hasAddedGroup = true
                    storageManager.stableStorage().putBoolean(ADDED_APP_GROUP_KEY, true)
                }
                .toResourceLiveData()
    }

    fun updateAppGroup(group: AppGroup): LiveData<Resource<Any>> {
        return appGuardRepository.updateAppGroup(childUserId, childDeviceId, group).toResourceLiveData()
    }

}

data class TableStatus(
        val showedRisk: Boolean,
        val hasRiskApp: Boolean
)