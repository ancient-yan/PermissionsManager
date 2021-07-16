package com.gwchina.parent.message.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.message.MessageActivity
import com.gwchina.parent.message.data.MessageApi
import com.gwchina.parent.message.presentation.MessageFragment
import com.gwchina.parent.message.presentation.MessageViewModel
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
abstract class MessageModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, MessageInternalModule::class])
    internal abstract fun contributeMessageActivity(): MessageActivity

    @Module
    internal abstract class MessageInternalModule {

        @Module
        companion object {

            @Provides
            @JvmStatic
            fun provideMemberApi(serviceFactory: ServiceFactory): MessageApi {
                return serviceFactory.create(MessageApi::class.java)
            }
        }

        @FragmentScope
        @ContributesAndroidInjector
        abstract fun contributeMessageCenterFragmentInjector(): MessageFragment

        @Binds
        @IntoMap
        @ViewModelKey(MessageViewModel::class)
        abstract fun provideMessageViewModel(messageViewModel: MessageViewModel): ViewModel

    }
}