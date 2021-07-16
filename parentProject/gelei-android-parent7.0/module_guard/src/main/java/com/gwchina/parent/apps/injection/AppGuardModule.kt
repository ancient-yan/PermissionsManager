package com.gwchina.parent.apps.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.apps.AppGuardActivity
import com.gwchina.parent.apps.data.AppGuardApi
import com.gwchina.parent.apps.presentation.approval.AppApprovalFragment
import com.gwchina.parent.apps.presentation.approval.AppApprovalRecordFragment
import com.gwchina.parent.apps.presentation.approval.AppApprovalRecordViewModel
import com.gwchina.parent.apps.presentation.approval.AppApprovalViewModel
import com.gwchina.parent.apps.presentation.group.AddEditGroupFragment
import com.gwchina.parent.apps.presentation.guide.*
import com.gwchina.parent.apps.presentation.rules.*
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Named


@Module
abstract class AppGuardModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, AppProviderModule::class, AppGuardInternalModule::class])
    internal abstract fun contributeAppGuardActivityInjector(): AppGuardActivity

}

@Module
internal class AppProviderModule {

    @Named(DEVICE_ID_KEY)
    @Provides
    fun provideChildDeviceId(timeGuardActivity: AppGuardActivity): String {
        return timeGuardActivity.childDeviceId ?: ""
    }

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(timeGuardActivity: AppGuardActivity): String {
        return timeGuardActivity.childUserId ?: ""
    }

}

@Module
internal abstract class AppGuardInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideAppGuardApi(serviceFactory: ServiceFactory): AppGuardApi {
            return serviceFactory.create(AppGuardApi::class.java)
        }

    }

    @Binds
    @IntoMap
    @ViewModelKey(GuideFlowViewModel::class)
    abstract fun provideAppsGuardFlowViewModel(guideFlowViewModel: GuideFlowViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppApprovalRecordViewModel::class)
    abstract fun provideAppApprovalRecordViewModel(appApprovalRecordViewModel: AppApprovalRecordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppRulesViewModel::class)
    abstract fun provideAppRulesViewModel(appRulesViewModel: AppRulesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppApprovalViewModel::class)
    abstract fun provideAppPermissionViewModel(appApprovalViewModel: AppApprovalViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppsGuardGuidFragmentInjector(): AppsGuardGuidFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFreeAppFlowFragmentInjector(): GuideFreeAppFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDisableAppFlowFragmentInjector(): GuideDisabledAppFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeLimitAppFlowFragmentInjector(): LimitAppFlowFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAddEditGroupFragmentInjector(): AddEditGroupFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppGuardRulesFragmentInjector(): AppGuardRulesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFreeAppListFragmentInjector(): FreeAppListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDisabledAppListFragmentInjector(): DisabledAppListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeLimitedAppListFragmentInjector(): LimitedAppListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppPermissionFragmentInjector(): AppApprovalFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppApprovalFragmentInjector(): AppApprovalListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppApprovalRecordFragmentInjector(): AppApprovalRecordFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeRiskAppListFragmentInjector(): RiskAppListFragment

}