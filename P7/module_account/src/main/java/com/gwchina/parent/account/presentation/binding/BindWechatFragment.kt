package com.gwchina.parent.account.presentation.binding

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.interfaces.adapter.TextWatcherAdapter
import com.gwchina.lssw.parent.account.R
import com.gwchina.parent.account.AccountNavigator
import com.gwchina.parent.account.common.WECHAT_ID
import com.gwchina.parent.account.presentation.createPrivacyAndAgreementText
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.utils.verify.clearErrorWhenHasFocus
import com.gwchina.sdk.base.utils.verify.matchCellphoneLength
import com.gwchina.sdk.base.utils.verify.validateCellphone
import com.gwchina.sdk.base.utils.verify.validateSmsCode
import kotlinx.android.synthetic.main.account_fragment_bind_wechat.*
import javax.inject.Inject

/**
 * 短信登录/注册
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 15:06
 */
class BindWechatFragment : InjectorBaseFragment() {

    companion object {
        fun newInstance(wechatId: String): Fragment = BindWechatFragment().apply {
            arguments = Bundle().apply {
                putString(WECHAT_ID, wechatId)
            }
        }
    }

    @Inject lateinit var accountNavigator: AccountNavigator

    private val viewModel by lazy {
        getViewModel<BindWechatViewModel>(viewModelFactory)
    }

    private val _wechatId
        get() = arguments?.getString(WECHAT_ID) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.account_fragment_bind_wechat

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        listenTextInput()
        setupViews()
    }

    private fun listenTextInput() {
        //手机号输入监听
        tilBindWechatCellphone.clearErrorWhenHasFocus()
        tilBindWechatCellphone.editText?.addTextChangedListener(object : TextWatcherAdapter {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //是否满足手机号长度
                val matchCellphoneLength = s.matchCellphoneLength()
                vclBindWechat.setCounterEnable(matchCellphoneLength)
                //是否满足手机号长度并且验证码有输入
                btnBindWechatConfirm.isEnabled = matchCellphoneLength && !vclBindWechat.code().isEmpty()
                //清空输入或输入框进行编辑则错误提示消失
                tilBindWechatCellphone.error = null
            }
        })
        //验证码输入监听
        vclBindWechat.editText.addTextChangedListener(object : TextWatcherAdapter {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnBindWechatConfirm.isEnabled = !s.isNullOrEmpty() && tilBindWechatCellphone.textStr().matchCellphoneLength()
                //是否要清空错误提示
                if (s.isNullOrEmpty()) {
                    vclBindWechat.textInputLayout.error = null
                }
            }
        })
    }

    private fun setupViews() {
        //验证码
        vclBindWechat.setCounterEnable(false)
        vclBindWechat.setOnCounterClickListener {
            if (validateCellphone(tilBindWechatCellphone)) {
                viewModel.obtainCode(tilBindWechatCellphone.textStr())
            }
        }
        //登录注册
        btnBindWechatConfirm.setOnClickListener {
            if (validateCellphone(tilBindWechatCellphone) && validateSmsCode(vclBindWechat)) {
                viewModel.bindWechat(_wechatId, tilBindWechatCellphone.textStr(), vclBindWechat.code())
            }
        }

        tvAccountAgreement.text = createPrivacyAndAgreementText(requireContext(), accountNavigator, R.string.click_completing_meaning)
        tvAccountAgreement.enableSpanClickable()
    }

    private fun subscribeViewModel() {
        //sms code
        viewModel.sendSmsCode
                .observe(this, Observer {
                    it?.onLoading {
                        showLoadingDialog()
                    }?.onError { error ->
                        dismissLoadingDialog()
                        errorHandler.handleError(error)
                    }?.onSuccess {
                        dismissLoadingDialog()
                        vclBindWechat.startCounter()
                    }
                })

        //login
        viewModel.binding
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> showLoadingDialog(false)
                        it.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            vclBindWechat?.clearCounter()
                            processBindingSuccessfully(it)
                        }
                    }
                })
    }

    private fun processBindingSuccessfully(it: Resource<BindingResult>) {
        val bindingResult = it.data()
        val loginAttachment = bindingResult.loginAttachment
        when {
            loginAttachment?.isNeedMigrating() == true -> //老用户需要做数据迁移
                accountNavigator.openOldUserMigrationPage(loginAttachment)
            bindingResult.isNewUser -> //新用户
                accountNavigator.exitAccountSuccessfully(RouterPath.Account.LOGIN_TYPE_NEW, bindingResult.loginAttachment?.register_give_describe)
            else -> {//旧用户
                showMessage(R.string.bind_successfully)
                accountNavigator.exitAccountSuccessfully(RouterPath.Account.LOGIN_TYPE_OLD, null)
            }
        }
    }

}