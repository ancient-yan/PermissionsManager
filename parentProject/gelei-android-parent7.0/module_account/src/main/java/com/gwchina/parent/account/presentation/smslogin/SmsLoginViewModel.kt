package com.gwchina.parent.account.presentation.smslogin

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.account.common.NEW_USER_FLAG
import com.gwchina.parent.account.data.AccountRepository
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.models.LoginAttachment
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 15:08
 */
class SmsLoginViewModel @Inject constructor(
        private val accountRepository: AccountRepository
) : ArchViewModel() {

    private val _sendSmsCode: MediatorLiveData<Resource<Any>> = MediatorLiveData()
    private val _login: MutableLiveData<Resource<LoginResult>> = MutableLiveData()

    val sendSmsCode: LiveData<Resource<Any>>
        get() = _sendSmsCode

    val login: LiveData<Resource<LoginResult>>
        get() = _login

    fun obtainCode(cellphoneNum: String) {
        if (_sendSmsCode.value?.isLoading == true) {
            return
        }

        _sendSmsCode.value = Resource.loading()

        accountRepository.sendLoginSmsCode(cellphoneNum)
                .subscribe(
                        {
                            _sendSmsCode.postValue(Resource.success())
                        },
                        {
                            _sendSmsCode.postValue(Resource.error(it))
                        }
                )
    }

    fun smsLogin(cellphone: String, code: String) {
        accountRepository.smsLogin(cellphone, code)
                .subscribeWithLiveData(_login) {
                    LoginResult(
                            isNewUser = NEW_USER_FLAG == it.register_flag,
                            isWechat = false,
                            loginAttachment = it.subjoin_info
                    )
                }
    }

    fun wechatLogin(openid: String) {
        accountRepository.wechatLogin(openid)
                .subscribeWithLiveData(_login) {
                    LoginResult(
                            isNewUser = isYes(it.register_flag),
                            loginAttachment = it.subjoin_info,
                            isWechat = true,
                            wechatCode = openid)
                }
    }

}

data class LoginResult(
        val isNewUser: Boolean,
        val isWechat: Boolean,
        val loginAttachment: LoginAttachment?,
        val wechatCode: String = ""
)