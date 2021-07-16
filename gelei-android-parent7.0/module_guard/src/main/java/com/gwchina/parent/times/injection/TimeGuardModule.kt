package com.gwchina.parent.times.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.times.TimeGuardActivity
import com.gwchina.parent.times.data.TimeGuardApi
import com.gwchina.parent.times.presentation.guide.ChooseMakingPlanWayFragment
import com.gwchina.parent.times.presentation.guide.TimeGuardGuideFragment
import com.gwchina.parent.times.presentation.guide.TimeGuardGuideViewModel
import com.gwchina.parent.times.presentation.make.MakeTimeGuardPlanFragment
import com.gwchina.parent.times.presentation.make.MakeTimeGuardPlanViewModel
import com.gwchina.parent.times.presentation.make.MakeTimeGuardPlanFragment2
import com.gwchina.parent.times.presentation.spare.EditSparePlanFragment
import com.gwchina.parent.times.presentation.spare.EditSparePlanViewModel
import com.gwchina.parent.times.presentation.spare.SparePlanFragment
import com.gwchina.parent.times.presentation.spare.SparePlanViewModel
import com.gwchina.parent.times.presentation.table.TimeGuardTableFragment
import com.gwchina.parent.times.presentation.table.TimeGuardTableViewModel
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Named


@Module
abstract class TimeGuardModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, TimeProviderModule::class, TimeGuardInternalModule::class])
    internal abstract fun contributeGuardActivityInjector(): TimeGuardActivity

}

@Module
internal class TimeProviderModule {

    @Named(DEVICE_ID_KEY)
    @Provides
    fun provideChildDeviceId(timeGuardActivity: TimeGuardActivity): String {
        return timeGuardActivity.childDeviceId ?: ""
    }

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(timeGuardActivity: TimeGuardActivity): String {
        return timeGuardActivity.childUserId ?: ""
    }

}

@Module
internal abstract class TimeGuardInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideGuardApi(serviceFactory: ServiceFactory): TimeGuardApi {
            return serviceFactory.create(TimeGuardApi::class.java)
        }

    }

    @Binds
    @IntoMap
    @ViewModelKey(MakeTimeGuardPlanViewModel::class)
    abstract fun provideSetGuardTimePlanViewModel(setGuardTimePlanViewModel: MakeTimeGuardPlanViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeGuardTableViewModel::class)
    abstract fun provideTimeGuardTableViewModel(setGuardTimePlanViewModel: TimeGuardTableViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeGuardGuideViewModel::class)
    abstract fun provideTimeGuardGuideViewModel(timeGuardGuideViewModel: TimeGuardGuideViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SparePlanViewModel::class)
    abstract fun provideSparePlanViewModel(sparePlanViewModel: SparePlanViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditSparePlanViewModel::class)
    abstract fun provideEditSparePlanViewModel(editSparePlanViewModel: EditSparePlanViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSetGuardTimePlanFragmentInjector(): MakeTimeGuardPlanFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSetGuardTimePlanFragment2Injector(): MakeTimeGuardPlanFragment2

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeTimeGuardGuideFragmentInjector(): TimeGuardGuideFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeTimeGuardTableFragmentInjector(): TimeGuardTableFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeChooseMakingPlanWayFragmentInjector(): ChooseMakingPlanWayFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSparePlanFragmentInjector(): SparePlanFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeEditSparePlanFragmentInjector(): EditSparePlanFragment

}