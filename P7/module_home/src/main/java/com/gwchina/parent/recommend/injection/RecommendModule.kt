package com.gwchina.parent.recommend.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.recommend.RecommendActivity
import com.gwchina.parent.recommend.data.RecommendApi
import com.gwchina.parent.recommend.presentation.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class RecommendModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, RecommendInternalModule::class])
    internal abstract fun contributeRecommendActivityInjector(): RecommendActivity

}

@Module
abstract class RecommendInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideRecommendApi(serviceFactory: ServiceFactory): RecommendApi {
            return serviceFactory.create(RecommendApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(RecommendViewModel::class)
    abstract fun provideRecommendViewModel(RecommendViewModel: RecommendViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeRecommendFragmentInjector(): RecommendFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeRecommendListFragmentInjector(): RecommendListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAppDetailFragmentInjector(): AppDetailFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeSubjectDetailFragment(): SubjectDetailFragment

}