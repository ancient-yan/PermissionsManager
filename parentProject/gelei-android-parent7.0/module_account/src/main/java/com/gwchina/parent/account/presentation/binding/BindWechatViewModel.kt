package com.gwchina.parent.account.presentation.binding

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.account.common.NEW_USER_FLAG
import com.gwchina.parent.account.data.AccountRepository
import com.gwchina.sdk.base.data.models.LoginAttachment
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 15:08
 */
class BindWechatViewModel @Inject constructor(
        private val accountRepository: AccountRepository
) : ArchViewModel() {

    private val _sendSmsCode: MediatorLiveData<Resource<Any>> = MediatorLiveData()
    private val _binding: MutableLiveData<Resource<BindingResult>> = MutableLiveData()

    val sendSmsCode: LiveData<Resource<Any>>
        get() = _sendSmsCode

    val binding: LiveData<Resource<BindingResult>>
        get() = _binding

    fun obtainCode(cellphoneNum: String) {
        accountRepository.sendWechatBindingSmsCode(cellphoneNum)
                .subscribeWithLiveData(_sendSmsCode)
    }

    fun bindWechat(wechatId: String, cellphone: String, code: String) {
        accountRepository.bindWechat(wechatId, cellphone, code)
                .subscribeWithLiveData(_binding) {
                    BindingResult(
                            isNewUser = NEW_USER_FLAG == it.register_flag,
                            loginAttachment = it.subjoin_info)
                }
    }

}

data class BindingResult(
        val isNewUser: Boolean,
        val loginAttachment: LoginAttachment?
)