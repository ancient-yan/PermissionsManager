package com.gwchina.parent.main.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.main.MainActivity
import com.gwchina.parent.main.MainFragment
import com.gwchina.parent.main.data.MainApi
import com.gwchina.parent.main.presentation.approval.AppApprovalFragment
import com.gwchina.parent.main.presentation.approval.AppApprovalViewModel
import com.gwchina.parent.main.presentation.approval.PhoneApprovalFragment
import com.gwchina.parent.main.presentation.approval.PhoneApprovalViewModel
import com.gwchina.parent.main.presentation.device.BoundDeviceFragment
import com.gwchina.parent.main.presentation.device.DeviceManagingFragment
import com.gwchina.parent.main.presentation.device.DeviceViewModel
import com.gwchina.parent.main.presentation.home.HomeFragment
import com.gwchina.parent.main.presentation.home.HomeUsingRecordFragment
import com.gwchina.parent.main.presentation.home.HomeViewModel
import com.gwchina.parent.main.presentation.location.ChildLocationActivity
import com.gwchina.parent.main.presentation.location.ChildLocationFragment
import com.gwchina.parent.main.presentation.location.ChildLocationViewModel
import com.gwchina.parent.main.presentation.mine.MineFragment
import com.gwchina.parent.main.presentation.mine.MineViewModel
import com.gwchina.parent.main.presentation.weekly.WeeklyListFragment
import com.gwchina.parent.main.presentation.weekly.WeeklyListViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class MainModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, MainInternalModule::class])
    internal abstract fun contributeMainActivityInjector(): MainActivity

}

@Module
abstract class MainInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideHomeApi(serviceFactory: ServiceFactory): MainApi {
            return serviceFactory.create(MainApi::class.java)
        }

    }

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun provideHomeViewModule(homeViewModule: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MineViewModel::class)
    abstract fun provideMineViewModule(mineViewModel: MineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppApprovalViewModel::class)
    abstract fun provideAppApprovalViewModel(appApprovalViewModel: AppApprovalViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PhoneApprovalViewModel::class)
    abstract fun providePhoneApprovalViewModel(phoneApprovalViewModel: PhoneApprovalViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeviceViewModel::class)
    abstract fun provideBoundDeviceViewModel(boundDeviceViewModel: DeviceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WeeklyListViewModel::class)
    abstract fun provideBoundWeeklyListViewModel(boundDeviceViewModel: WeeklyListViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeMainFragmentInjector(): MainFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeIndexFragmentInjector(): HomeFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeMineFragmentInjector(): MineFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppApprovalFragmentInjector(): AppApprovalFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributePhoneApprovalFragmentInjector(): PhoneApprovalFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeviceManagingFragmentInjector(): DeviceManagingFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBoundDeviceFragmentInjector(): BoundDeviceFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeWeeklyListFragmentInjector(): WeeklyListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeHomeUsingRecordFragmentInjector(): HomeUsingRecordFragment

}