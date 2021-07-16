package com.gwchina.parent.member.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.member.MemberActivity
import com.gwchina.parent.member.data.MemberApi
import com.gwchina.parent.member.presentation.center.MemberCenterFragment
import com.gwchina.parent.member.presentation.center.MemberCenterViewModel
import com.gwchina.parent.member.presentation.payresult.PayResultFragment
import com.gwchina.parent.member.presentation.payresult.PayResultViewModel
import com.gwchina.parent.member.presentation.purchase.PurchaseMemberFragment
import com.gwchina.parent.member.presentation.purchase.PurchaseMemberViewModel
import com.gwchina.parent.member.presentation.record.PurchaseRecordFragment
import com.gwchina.parent.member.presentation.record.PurchaseRecordViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:38
 */
@Module
abstract class MemberModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, MemberInternalModule::class])
    internal abstract fun contributeMemberActivity(): MemberActivity

    @Module
    internal abstract class MemberInternalModule {

        @Module
        companion object {

            @Provides
            @JvmStatic
            fun provideMemberApi(serviceFactory: ServiceFactory): MemberApi {
                return serviceFactory.create(MemberApi::class.java)
            }
        }

        @Binds
        @IntoMap
        @ViewModelKey(MemberCenterViewModel::class)
        abstract fun provideMemberCenterViewModel(memberCenterViewModel: MemberCenterViewModel): ViewModel

        @FragmentScope
        @ContributesAndroidInjector
        abstract fun contributeMemberCenterFragmentInjector(): MemberCenterFragment

        @Binds
        @IntoMap
        @ViewModelKey(PurchaseMemberViewModel::class)
        abstract fun providePurchaseMemberViewModel(purchaseMemberViewModel: PurchaseMemberViewModel): ViewModel

        @FragmentScope
        @ContributesAndroidInjector
        abstract fun contributePurchaseMemberFragmentInjector(): PurchaseMemberFragment

        @Binds
        @IntoMap
        @ViewModelKey(PurchaseRecordViewModel::class)
        abstract fun providePurchaseRecordViewModel(purchaseRecordViewModel: PurchaseRecordViewModel): ViewModel

        @FragmentScope
        @ContributesAndroidInjector
        abstract fun contributePurchaseRecordFragmentInjector(): PurchaseRecordFragment

        @FragmentScope
        @ContributesAndroidInjector
        abstract fun contributePayResultFragmentInjector(): PayResultFragment


        @Binds
        @IntoMap
        @ViewModelKey(PayResultViewModel::class)
        abstract fun providePayReultViewModelViewModel(payViewModel: PayResultViewModel): ViewModel
    }
}