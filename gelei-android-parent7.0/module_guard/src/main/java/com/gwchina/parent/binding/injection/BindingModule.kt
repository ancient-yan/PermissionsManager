package com.gwchina.parent.binding.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.binding.BindingActivity
import com.gwchina.parent.binding.BindingNavigator
import com.gwchina.parent.binding.common.BindingProcessStatusKeeper
import com.gwchina.parent.binding.common.NewChildProcessor
import com.gwchina.parent.binding.data.BindingApi
import com.gwchina.parent.binding.presentation.*
import com.gwchina.parent.migration.presentation.MigrationViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class BindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, BindingInternalModule::class, InnerProvideModule::class])
    abstract fun contributeBindingActivityInjector(): BindingActivity

}

@Module
class InnerProvideModule {

    @Provides
    internal fun provideBindingProcessStatusKeeper(bindingActivity: BindingActivity): BindingProcessStatusKeeper {
        return bindingActivity.bindingProcessStatusKeeper
    }

    @ActivityScope
    @Provides
    fun provideNewChildProcessor(bindingActivity: BindingActivity, bindingNavigator: BindingNavigator): NewChildProcessor {

        return object : NewChildProcessor {
            override fun newNewChildInfoCollected(childInfo: ChildInfo) {
                bindingActivity.bindingProcessStatusKeeper.setAddingDeviceForNewChild(childInfo)
                bindingNavigator.openScanChildGuidePage()
            }
        }
    }

}

@Module
abstract class BindingInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideBindingApi(serviceFactory: ServiceFactory): BindingApi {
            return serviceFactory.create(BindingApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(BindingViewModel::class)
    abstract fun provideBindingViewModel(bindingViewModel: BindingViewModel): ViewModel


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeChildInfoFragmentInjector(): ChildInfoCollectFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeScanChildGuideFragmentInjector(): ScanChildGuideFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeScannerFragmentInjector(): ScannerFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSelectChildFragmentInjector(): SelectChildFragment

}