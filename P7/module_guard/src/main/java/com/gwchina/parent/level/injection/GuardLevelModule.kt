package com.gwchina.parent.level.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.level.GuardLevelActivity
import com.gwchina.parent.level.data.LevelApi
import com.gwchina.parent.level.presentation.SetGuardLevelFragment
import com.gwchina.parent.level.presentation.SetGuardLevelViewModel
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.router.ChildDeviceInfo
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.utils.emptyElse
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.annotation.Nullable
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 20:00
 */
@Module
abstract class GuardLevelModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, GuardLevelPropertiesModule::class, BindingInternalModule::class])
    internal abstract fun contributeGuardLevelActivityInjector(): GuardLevelActivity

}

@Module
internal class GuardLevelPropertiesModule {

    @Named(DEVICE_ID_KEY)
    @Provides
    fun provideDeviceId(guardLevelActivity: GuardLevelActivity): String {
        return guardLevelActivity.childDeviceId.emptyElse("")
    }

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(guardLevelActivity: GuardLevelActivity): String {
        return guardLevelActivity.childUserId.emptyElse("")
    }

    @Provides
    @Nullable
    fun provideChildDeviceInfo(guardLevelActivity: GuardLevelActivity): ChildDeviceInfo? {
        return guardLevelActivity.childDeviceInfo
    }

    @Provides
    fun whetherChoosingLevel(guardLevelActivity: GuardLevelActivity): Boolean {
        return guardLevelActivity.pageKey == RouterPath.GuardLevel.ACTION_CHOOSING_LEVEL
    }

}

@Module
abstract class BindingInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideBindingApi(serviceFactory: ServiceFactory): LevelApi {
            return serviceFactory.create(LevelApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(SetGuardLevelViewModel::class)
    abstract fun provideSetGuardLevelViewModel(setGuardLevelViewModel: SetGuardLevelViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSetGuardLevelFragmentInjector(): SetGuardLevelFragment

}