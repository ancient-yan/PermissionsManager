package com.gwchina.parent.main.presentation.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.kotlin.touchOff
import com.android.base.rx.*
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.common.MainEventCenter
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.parent.main.data.PhoneApprovalInfo
import com.gwchina.parent.main.data.Soft
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.SoftWrapper
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.app.AdvertisingFilter
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.utils.generateDeviceFlag
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-02 18:40
 */
class HomeViewModel @Inject constructor(
        @ContextType private val context: Context,
        internal val mainRepository: MainRepository,
        val appDataSource: AppDataSource,
        private val schedulerProvider: SchedulerProvider,
        private val homeDataMapper: HomeMapper,
        private val errorHandler: ErrorHandler,
        private val mainEventCenter: MainEventCenter
) : ArchViewModel() {

    private val _homePageData = MutableLiveData<Resource<HomeVO>>()
    private val _operationStatus = MutableLiveData<OperationStatus>()
    private val _advertising = MutableLiveData<List<Advertising>>()
    private val _user = MutableLiveData<User>()
    private val _instructionsState = MutableLiveData<AllInstructionState>()
    private val _usingRecordData = MutableLiveData<Resource<List<UsingTrajectoryItem>>>()
    private val _childDevicePermission = MutableLiveData<Resource<PrivilegeData>>()

    val homeData: LiveData<Resource<HomeVO>>
        get() = _homePageData

    val user: LiveData<User>
        get() = _user

    val operationStatus: LiveData<OperationStatus>
        get() = _operationStatus

    private val childUserId: String
        get() = appDataSource.user().currentChildId ?: ""

    private val childDeviceId: String
        get() = appDataSource.user().currentChildDeviceId ?: ""

    val advertising: LiveData<List<Advertising>>
        get() = _advertising

    val instructionsState: LiveData<AllInstructionState>
        get() = _instructionsState

    val usingRecordData: LiveData<Resource<List<UsingTrajectoryItem>>>
        get() = _usingRecordData

    val childDevicePermission: LiveData<Resource<PrivilegeData>>
        get() = _childDevicePermission

    private var homeDataDisposable: CompositeDisposable? = null
    private var switchChildDeviceDisposable: CompositeDisposable? = null
    private var adDisposable: Disposable? = null
    private var synchronizingUser: Disposable? = null

    init {
        appDataSource
                .observableUser()
                .autoDispose()
                .subscribeIgnoreError {
                    processWhenUserStatusChanged(it)
                }
    }

    private fun processWhenUserStatusChanged(user: User) {
        _user.postValue(user)
        if (!user.logined()) {
            mainEventCenter.setMineTabRedDotVisible(false)
            homeDataDisposable?.dispose()
            _operationStatus.postValue(OperationStatus())
            _homePageData.postValue(null)
            _instructionsState.postValue(null)
        }
    }

    private fun newHomeDataDisposableIfNeed(): CompositeDisposable {
        val compositeDisposable = RxKit.newCompositeIfUnsubscribed(homeDataDisposable)
        this.homeDataDisposable = compositeDisposable
        return compositeDisposable
    }

    private fun newSwitchChildDisposableIfNeed(): CompositeDisposable {
        val compositeDisposable = RxKit.newCompositeIfUnsubscribed(switchChildDeviceDisposable)
        this.switchChildDeviceDisposable = compositeDisposable
        return compositeDisposable
    }

    fun refreshHomePage() {
        //每次刷新都拉取广告
        loadAdvertising()
        //首页数据
        val logined = appDataSource.user().logined()
        Timber.d("refreshHomePage $logined")
        if (logined) {
            if (_homePageData.value?.isLoading != true) {
                loadHomePageData()
            }
        } else {
            _homePageData.postValue(Resource.success())
            _childDevicePermission.postValue(Resource.success())
        }
    }

    private fun loadInstructionState() {
        mainRepository.instructionsStatus(childUserId, childDeviceId)
                .subscribeIgnoreError {
                    _instructionsState.postValue(it)
                }.addTo(newHomeDataDisposableIfNeed())
    }

    fun sendSyncInstruction(instructionType: String) {
        mainRepository.sendSyncInstructions(instructionType, childUserId, childDeviceId)
                .subscribe(
                        {
                            loadInstructionState()
                        },
                        {
                            _operationStatus.touchOff()
                            errorHandler.handleError(it)
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    private val advertisingFilter = object : AdvertisingFilter {
        override fun filter(ad: Advertising): Boolean {
            return ad.ad_position == AD_POSITION_HOME
        }
    }

    private fun loadAdvertising() {
        val disposable = adDisposable
        if (disposable == null || disposable.isDisposed) {
            adDisposable = appDataSource.advertisingList(advertisingFilter)
                    .retryWhen(5, 3000)
                    .onTerminateDetach()
                    .subscribeIgnoreError { _advertising.postValue(it.orElse(null)) }
        }
    }

    private fun loadHomePageData() {
        _homePageData.postValue(Resource.loading())

        mainRepository.loadHomeData()
                .observeOn(schedulerProvider.computation())
                .map {
                    val error = it.error
                    if (error != null) {
                        Resource.error(error)
                    } else {
                        Resource.success(homeDataMapper.transform(it.data))
                    }
                }
                .doOnComplete {

                    /*加载指令状态*/
                    loadInstructionState()
                    /*同步孩子状态*/
                    syncChildrenStatus()
                    /*获取设备权限情况*/
                    childDevicePermissionChecker()
                }
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        {
                            _homePageData.value = it
                        },
                        {
                            _homePageData.postValue(Resource.error(it))
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    private fun syncChildrenStatus() {
        appDataSource.syncChildren()
                .toFlowable<Any>()
                .delaySubscription(3000, TimeUnit.MILLISECONDS)
                .subscribed()
    }

    private fun childDevicePermissionChecker() {
        if (childUserId.isEmpty() || childDeviceId.isEmpty()||appDataSource.user().currentDevice?.isIOS()==true|| isYes(appDataSource.user().currentDevice?.custom_device_flag)){
            _childDevicePermission.postValue(Resource.success())
            return
        }
        mainRepository.childDevicePermissionChecker(childUserId, childDeviceId)
                .subscribe({
                    _childDevicePermission.postValue(Resource.success(it.get()))
                }, {
                    _childDevicePermission.postValue(Resource.error(it))
                }).addTo(newHomeDataDisposableIfNeed())
    }

    fun addTemporarilyUsable(tempUsableMinutes: Int) {
        _operationStatus.postValue(OperationStatus(showLoading = true))

        mainRepository.setTempUsable(tempUsableMinutes, childUserId, childDeviceId)
                .subscribe(
                        {
                            val data = _homePageData.value?.get()
                            if (it.isPresent && data != null) {
                                val usableResponse = it.get()
                                _operationStatus.postValue(OperationStatus(message = context.getString(R.string.set_successfully)))
                                val newDeviceInfo = data.deviceInfo?.copy(
                                        temp_usable_time = TempTimePeriodRule(usableResponse.rule_id, FLAG_NEGATIVE, usableResponse.usable_begin_time, usableResponse.usable_end_time)
                                )
                                val newData = data.copy(deviceInfo = newDeviceInfo)
                                _homePageData.postValue(Resource.success(newData))//notify new data
                                mainRepository.updateDeviceInfo(newDeviceInfo)
                            } else {
                                _operationStatus.postValue(OperationStatus())
                            }
                        },
                        {
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    fun deleteTemporarilyUsable(tempUsableId: String) {
        _operationStatus.postValue(OperationStatus(showLoading = true))

        mainRepository.deleteTempUsable(tempUsableId, childUserId, childDeviceId)
                .subscribe(
                        {
                            _operationStatus.postValue(OperationStatus(message = "关闭成功", needRefresh = true))
                        },
                        {
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    fun approvalNewNumber(phoneApprovalInfo: PhoneApprovalInfo, allow: Boolean) {
        _operationStatus.postValue(OperationStatus(showLoading = true))
        mainRepository.approvalPhone(phoneApprovalInfo.record_id, allow, phoneApprovalInfo.user_id)
                .subscribe(
                        {
                            _operationStatus.postValue(OperationStatus(needRefresh = true, message = "审批成功"))
                        },
                        {
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                )
    }

    fun forbidNewApp(softWrapper: SoftWrapper) {
        _operationStatus.postValue(OperationStatus(showLoading = true))

        mainRepository.prohibitNewApp(softWrapper.soft.rule_id, softWrapper.childUserId, softWrapper.childDeviceId)
                .subscribe(
                        {
                            _operationStatus.postValue(
                                    OperationStatus(
                                            message = context.getString(R.string.x_add_to_y_list_mask, softWrapper.soft.soft_name, context.getString(com.app.base.R.string.disabled)),
                                            needRefresh = true
                                    )
                            )
                        },
                        {
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    fun installSoft(soft: Soft) {
        _operationStatus.postValue(OperationStatus(showLoading = true))

        mainRepository.installAppForChild(childUserId, childDeviceId, soft.soft_name, soft.bundle_id)
                .subscribe(
                        {
                            _operationStatus.postValue(OperationStatus(showLoading = false))
                            val data = _homePageData.value?.get()
                            if (data != null) {
                                data.peerRecommendation?.find {
                                    it.bundle_id == soft.bundle_id
                                }?.let {
                                    val newList = mutableListOf<Soft>()
                                    newList.addAll(data.peerRecommendation)
                                    newList[newList.indexOf(it)] = it.copy(install_flag = APP_INSTALL_STATUS_INSTALLING)
                                    _homePageData.postValue(Resource.success(data.copy(peerRecommendation = newList)))
                                }
                            }
                        },
                        {
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                ).addTo(newHomeDataDisposableIfNeed())
    }

    fun switchChild(child: Child, device: Device? = null) {
        homeDataDisposable?.dispose()
        switchChildDeviceDisposable?.dispose()

        _homePageData.postValue(Resource.loading()) //notify loading
        _operationStatus.postValue(OperationStatus(showLoading = true))

        mainRepository.switchChildAndDevice(child.child_user_id, device?.device_id ?: "")
                .observeOn(schedulerProvider.computation())
                .map {
                    val data = it.orElse(null)
                    mainEventCenter.setMineTabRedDotVisible(isFlagPositive(data.exist_tag))
                    Optional.ofNullable(homeDataMapper.transform(data))
                }
                .onTerminateDetach()
                .subscribe(
                        {

                            //某些情况下UI 变化太快，造成突兀
                            processSwitchChildSuccessfully(it)
                        },
                        {
                            //notify switching error
                            _operationStatus.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                            //reload previous data
                            loadHomePageData()
                        }
                ).addTo(newSwitchChildDisposableIfNeed())
    }

    private fun processSwitchChildSuccessfully(it: Optional<HomeVO?>) {
        schedulerProvider.ui().scheduleDirect(
                {
                    //清掉之前的指令状态
                    _instructionsState.postValue(null)
                    //分发首页数据
                    _homePageData.postValue(Resource.success(it.orElse(null)))
                    _operationStatus.postValue(OperationStatus(resetScroll = true))
                    /*加载指令状态*/
                    loadInstructionState()
                    /*同步孩子状态*/
                    syncChildrenStatus()
                    /*获取设备权限情况*/
                    childDevicePermissionChecker()
                }, 100, TimeUnit.MILLISECONDS)
                .addTo(newSwitchChildDisposableIfNeed())
    }

    /**
     * 手机使用记录
     */
    fun loadUsingRecordData() {
        _usingRecordData.postValue(Resource.loading())
        mainRepository.homeUsingRecordData().subscribe(
                {
                    _usingRecordData.postValue(Resource.success(homeDataMapper.buildUsingTrajectoryList(it.get())))
                },
                {
                    _usingRecordData.postValue(Resource.error(it))
                })
    }

    val deviceFlag = LiveDataReactiveStreams.fromPublisher(
            appDataSource.observableUser()
                    .map { generateDeviceFlag(context, it, childDeviceId) }
    )

    override fun onCleared() {
        super.onCleared()
        homeDataDisposable?.dispose()
        switchChildDeviceDisposable?.dispose()
        synchronizingUser?.dispose()
        adDisposable?.dispose()
    }

}

data class OperationStatus(
        val showLoading: Boolean = false,
        val message: CharSequence? = null,
        val needRefresh: Boolean = false,
        val resetScroll: Boolean = false
)