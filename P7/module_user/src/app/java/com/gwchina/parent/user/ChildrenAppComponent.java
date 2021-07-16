package com.gwchina.parent.user;

import com.gwchina.parent.member.injection.MemberModule;
import com.gwchina.sdk.base.di.AppModule;
import com.gwchina.sdk.base.di.DataModule;
import com.gwchina.sdk.debug.DebugActivityModule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(
        modules = {
                AppModule.class,
                DataModule.class,
                AndroidInjectionModule.class,
                AndroidSupportInjectionModule.class,
                //debug
                DebugActivityModule.class,
                ChildModule.class,
                MemberModule.class
        }
)
@Singleton
interface ChildrenAppComponent {

    void inject(com.gwchina.parent.user.ChildrenAppContext childrenAppContext);

}