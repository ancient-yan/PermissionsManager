package com.gwchina.parent.family.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.family.FamilyPhoneActivity
import com.gwchina.parent.family.data.FamilyPhoneApi
import com.gwchina.parent.family.presentation.add.AddAndEditContactsFragment
import com.gwchina.parent.family.presentation.approval.ApprovalViewModel
import com.gwchina.parent.family.presentation.approval.FamilyPendingApprovalListFragment
import com.gwchina.parent.family.presentation.group.*
import com.gwchina.parent.family.presentation.home.FamilyPhoneFragment
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Named


@Module
abstract class FamilyPhoneModel {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, FamilyPhoneInternalModule::class, FamilyPhoneProviderModule::class])
    internal abstract fun contributeFamilyPhoneActivityInjector(): FamilyPhoneActivity

}


@Module
internal class FamilyPhoneProviderModule {

    @Named(DEVICE_ID_KEY)
    @Provides
    fun provideChildDeviceId(familyPhoneActivity: FamilyPhoneActivity): String {
        return familyPhoneActivity.childDeviceId ?: ""
    }

    @Named(CHILD_USER_ID_KEY)
    @Provides
    fun provideChildUserId(familyPhoneActivity: FamilyPhoneActivity): String {
        return familyPhoneActivity.childUserId ?: ""
    }

}

@Module
internal abstract class FamilyPhoneInternalModule {


    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideFamilyPhoneApi(serviceFactory: ServiceFactory): FamilyPhoneApi {
            return serviceFactory.create(FamilyPhoneApi::class.java)
        }

    }

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeFamilyPhoneFragmentInjector(): FamilyPhoneFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAddAndEditContactsFragmentInjector(): AddAndEditContactsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeGroupManageFragmentInjector(): GroupManageFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeleteGroupFragmentInjector(): DeleteGroupFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeletePhoneFragmentInjector(): DeletePhoneFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeGroupSettingFragmentInjector(): GroupSettingFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributePendingApprovalListFragmentInjector(): FamilyPendingApprovalListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeGroupRangeManageFragmentInjector(): GroupRangeManageFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeGroupRangeSettingFragmentInjector(): GroupRangeSettingFragment

    @Binds
    @IntoMap
    @ViewModelKey(FamilyPhoneViewModel::class)
    abstract fun provideFamilyPhoneViewModel(familyPhoneViewModel: FamilyPhoneViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ApprovalViewModel::class)
    abstract fun provideApprovalViewModel(approvalViewModel: ApprovalViewModel): ViewModel


}