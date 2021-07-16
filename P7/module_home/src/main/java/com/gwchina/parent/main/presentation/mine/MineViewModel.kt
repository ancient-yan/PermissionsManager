package com.gwchina.parent.main.presentation.mine

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.parent.main.data.MineResponse
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.logined
import timber.log.Timber
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-05 15:48
 */
class MineViewModel @Inject constructor(
        private val mineDataSource: MainRepository,
        private val appDataSource: AppDataSource
) : ArchViewModel() {

    private val _user = MutableLiveData<User>()
    private val _minePageResponse = MutableLiveData<Resource<MineResponse>>()

    val minePageResponse: LiveData<Resource<MineResponse>>
        get() = _minePageResponse

    val user: LiveData<User>
        get() = _user

    init {
        appDataSource.observableUser()
                .autoDispose()
                .subscribeIgnoreError {
                    _user.postValue(it)
                    if (!it.logined()) {
                        _minePageResponse.postValue(Resource.success(MineResponse()))
                    }
                }
    }

    fun refreshPageInfo() {
        val user = appDataSource.user()
        Timber.d("user $user")
        Timber.d("user ${user.logined()}")
        if (user.logined()) {
            mineDataSource.minePageInfo()
                    .autoDispose()
                    .subscribe(
                            {
                                val orElse = it.orElse(null)
                                //更新到的用户昵称与本地的用户昵称不一致时，更新本地的用户昵称
                                if (orElse != null && !orElse.nick_name.isNullOrEmpty() && orElse.nick_name != user.patriarch.nick_name) {
                                    val patriarch = appDataSource.user().patriarch
                                    appDataSource.updatePatriarch(patriarch.copy(nick_name = orElse.nick_name))
                                }
                                _minePageResponse.postValue(Resource.success(orElse))
                            },
                            {
                                _minePageResponse.postValue(Resource.error(it))
                            })
        }
    }

}