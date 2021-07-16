package com.gwchina.parent.net.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.net.NetGuardActivity
import com.gwchina.parent.net.data.NetGuardApi
import com.gwchina.parent.net.presentation.home.fragment.NetGuardFragment
import com.gwchina.parent.net.presentation.home.fragment.NetGuardGuideFragment
import com.gwchina.parent.net.presentation.home.viewmodel.NetGuardViewModel
import com.gwchina.parent.net.presentation.netManagement.fragment.DeleteUrlListFragment
import com.gwchina.parent.net.presentation.netManagement.fragment.NetWorkManagementFragment
import com.gwchina.parent.net.presentation.netManagement.fragment.URLManagementFragment
import com.gwchina.parent.net.presentation.netManagement.viewmodel.NetManagementViewModel
import com.gwchina.parent.net.presentation.record.fragment.NetGuardRecordFragment
import com.gwchina.parent.net.presentation.record.viewmodel.NetGuardRecordViewModel
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-15 10:20
 */
@Module
abstract class NetGuardModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, NetGuardProviderModule::class, NetGuardInternalModule::class])
    internal abstract fun contributeNetGuardActivityInjector(): NetGuardActivity

}

@Module
internal class NetGuardProviderModule {

    @Named(DEVICE_ID_KEY)
    @Provides
    fun provideChildDeviceId(netGuardActivity: NetGuardActivity): String {
        return netGuardActivity.childDeviceId ?: ""
    }

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(netGuardActivity: NetGuardActivity): String {
        return netGuardActivity.childUserId ?: ""
    }

}

@Module
internal abstract class NetGuardInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideAppGuardApi(serviceFactory: ServiceFactory): NetGuardApi {
            return serviceFactory.create(NetGuardApi::class.java)
        }

    }

    @Binds
    @IntoMap
    @ViewModelKey(NetGuardViewModel::class)
    abstract fun provideNetGuardViewModel(netGuardViewModel: NetGuardViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(NetGuardRecordViewModel::class)
    abstract fun provideNetGuardRecordViewModel(recordViewModel: NetGuardRecordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NetManagementViewModel::class)
    abstract fun provideNetManagementViewModel(recordViewModel: NetManagementViewModel): ViewModel


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeNetGuardFragmentInjector(): NetGuardFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeNetGuardRecordFragmentInjector(): NetGuardRecordFragment


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeNetWorkManagementFragmentInjector(): NetWorkManagementFragment


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeURLManagementFragmentInjector(): URLManagementFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeleteUrlListFragmentInjector(): DeleteUrlListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeNetGuardGuideFragmentInjector(): NetGuardGuideFragment

}
