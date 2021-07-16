package com.gwchina.parent.profile.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.profile.ProfileActivity
import com.gwchina.parent.profile.data.ProfileApi
import com.gwchina.parent.profile.presentation.child.ChildInfoFragment
import com.gwchina.parent.profile.presentation.child.ChildInfoViewModel
import com.gwchina.parent.profile.presentation.patriarch.PatriarchAddressFragment
import com.gwchina.parent.profile.presentation.patriarch.PatriarchInfoFragment
import com.gwchina.parent.profile.presentation.patriarch.PatriarchInfoViewModel
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-18 15:53
 */
@Module
abstract class ProfileCenterModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, ProfilePropertiesModule::class, ProfileInternalModule::class])
    internal abstract fun contributeProfileActivityInjector(): ProfileActivity

}

@Module
internal class ProfilePropertiesModule {

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(profileActivity: ProfileActivity): String {
        return profileActivity.childUserId?:""
    }

}

@Module
abstract class ProfileInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideProfileApi(serviceFactory: ServiceFactory): ProfileApi {
            return serviceFactory.create(ProfileApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(PatriarchInfoViewModel::class)
    abstract fun provideInfoViewModel(patriarchInfoViewModel: PatriarchInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChildInfoViewModel::class)
    abstract fun provideChildInfoViewModel(childInfoViewModel: ChildInfoViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeChildInfoFragment(): ChildInfoFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributePatriarchInfoFragment(): PatriarchInfoFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributePatriarchAddressFragment(): PatriarchAddressFragment

}