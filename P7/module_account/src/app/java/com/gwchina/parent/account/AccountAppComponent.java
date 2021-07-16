package com.gwchina.parent.account;

import com.gwchina.parent.account.injection.AccountModule;
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
                //this module
                AccountModule.class,
        }
)
@Singleton
interface AccountAppComponent {

    void inject(AccountAppContext accountAppContext);

}