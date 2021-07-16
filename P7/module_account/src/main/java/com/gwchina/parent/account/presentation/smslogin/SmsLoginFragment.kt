package com.gwchina.parent.account.presentation.smslogin

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.interfaces.adapter.TextWatcherAdapter
import com.android.sdk.social.wechat.WeChatManager
import com.gwchina.lssw.parent.account.R
import com.gwchina.parent.account.AccountNavigator
import com.gwchina.parent.account.presentation.createPrivacyAndAgreementText
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.utils.verify.clearErrorWhenHasFocus
import com.gwchina.sdk.base.utils.verify.matchCellphoneLength
import com.gwchina.sdk.base.utils.verify.validateCellphone
import com.gwchina.sdk.base.utils.verify.validateSmsCode
import kotlinx.android.synthetic.main.account_fragment_sms_login.*
import javax.inject.Inject

/**
 * 短信登录/注册
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 15:06
 */
class SmsLoginFragment : InjectorBaseFragment(), Injectable {

    companion object {
        private const val WECHAT_TOKEN = "SmsLoginFragment"
    }

    @Inject
    lateinit var accountNavigator: AccountNavigator

    private val smsLoginViewModel by lazy {
        getViewModel<SmsLoginViewModel>(viewModelFactory)
    }

    private val weChatManager = WeChatManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
        //进入到注册页面
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_REGISTER)
    }

    override fun provideLayout() = R.layout.account_fragment_sms_login

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        listenTextInput()
        setupViews()
    }

    private fun listenTextInput() {
        //手机号输入监听
        tilLoginCellphone.clearErrorWhenHasFocus()
        tilLoginCellphone.editText?.addTextChangedListener(object : TextWatcherAdapter {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //是否满足手机号长度
                val matchCellphoneLength = s.matchCellphoneLength()
                validateCodeLayoutLogin.setCounterEnable(matchCellphoneLength)
                //是否满足手机号长度并且验证码有输入
                btnLogin.isEnabled = matchCellphoneLength && validateCodeLayoutLogin.code().isNotEmpty()
                //清空输入或输入框进行编辑则错误提示消失
                tilLoginCellphone.error = null
            }
        })
        //验证码输入监听
        validateCodeLayoutLogin.editText.addTextChangedListener(object : TextWatcherAdapter {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLogin.isEnabled = !s.isNullOrEmpty() && tilLoginCellphone.textStr().matchCellphoneLength()
                //是否要清空错误提示
                if (s.isNullOrEmpty()) {
                    validateCodeLayoutLogin.textInputLayout.error = null
                }
            }
        })
    }

    private fun setupViews() {
        //验证码
        validateCodeLayoutLogin.setCounterEnable(false)
        validateCodeLayoutLogin.setOnCounterClickListener {
            if (validateCellphone(tilLoginCellphone)) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.PAGE_REGISTER_SMS)
                smsLoginViewModel.obtainCode(tilLoginCellphone.textStr())
            }
        }
        //登录注册
        btnLogin.setOnClickListener {
            if (validateCellphone(tilLoginCellphone) && validateSmsCode(validateCodeLayoutLogin)) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.PAGE_REGISTER_BUTTON)
                smsLoginViewModel.smsLogin(tilLoginCellphone.textStr(), validateCodeLayoutLogin.code())
            }
        }
        //微信登录
        tvLoginWeChat.setOnClickListener {
            processWeChatLoginRequest()
        }
        //协议相关
        tvLoginUserAgreement.text = createPrivacyAndAgreementText(requireContext(), accountNavigator, R.string.click_login_register_meaning)
        tvLoginUserAgreement.enableSpanClickable()
    }

    private fun processWeChatLoginRequest() {
        if (weChatManager.isInstalledWeChat) {
            weChatManager.requestAuthCode(WECHAT_TOKEN)
        } else {
            showMessage(getString(R.string.wechat_not_installed_tips))
        }
    }

    private fun subscribeViewModel() {
        //sms code
        smsLoginViewModel.sendSmsCode
                .observe(this, Observer {
                    it?.onLoading {
                        showLoadingDialog()
                    }?.onError { err ->
                        dismissLoadingDialog()
                        errorHandler.handleError(err)
                    }?.onSuccess {
                        dismissLoadingDialog()
                        validateCodeLayoutLogin.startCounter()
                    }
                })

        //login
        smsLoginViewModel.login
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            validateCodeLayoutLogin?.clearCounter()
                            processLoginSuccessfully(it)
                        }
                        it.isLoading -> {
                            showLoadingDialog(false)
                        }
                    }
                })

        weChatManager.authResult().observe(this, Observer {
            if (it != null && it.isSuccess && it.hasData()) {
                smsLoginViewModel.wechatLogin(it.result)
            }
        })
    }

    private fun processLoginSuccessfully(it: Resource<LoginResult>) {
        val loginResult = it.data()
        val loginAttachment = loginResult.loginAttachment
        if (loginAttachment?.isNeedMigrating() == true) {//老用户需要做数据迁移
            accountNavigator.openOldUserMigrationPage(loginAttachment)
        } else if (loginResult.isNewUser) {//新用户
            if (loginResult.isWechat) {
                accountNavigator.openBindWechatPage(loginResult.wechatCode)
            } else {
                accountNavigator.exitAccountSuccessfully(RouterPath.Account.LOGIN_TYPE_NEW, loginAttachment?.register_give_describe)
            }
        } else {//是登陆，登录成功后退出
            showMessage(R.string.login_successfully)
            accountNavigator.exitAccountSuccessfully(RouterPath.Account.LOGIN_TYPE_OLD, null)
        }
    }

}