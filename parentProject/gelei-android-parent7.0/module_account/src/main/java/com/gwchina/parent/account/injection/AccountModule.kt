package com.gwchina.parent.account.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.account.AccountActivity
import com.gwchina.parent.account.data.AccountApi
import com.gwchina.parent.account.presentation.binding.BindWechatFragment
import com.gwchina.parent.account.presentation.binding.BindWechatViewModel
import com.gwchina.parent.account.presentation.smslogin.SmsLoginFragment
import com.gwchina.parent.account.presentation.smslogin.SmsLoginViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 15:04
 */
@Module
abstract class AccountModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, AccountInternalModule::class])
    abstract fun contributeAccountInjector(): AccountActivity

}

@Module
abstract class AccountInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideAccountApi(serviceFactory: ServiceFactory): AccountApi {
            return serviceFactory.create(AccountApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(SmsLoginViewModel::class)
    abstract fun provideIndexViewModule(smsLoginViewModel: SmsLoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BindWechatViewModel::class)
    abstract fun provideBindWechatViewModel(bindWechatViewModel: BindWechatViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeRegisterFragmentInjector(): SmsLoginFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBindWechatFragmentInjector(): BindWechatFragment

}