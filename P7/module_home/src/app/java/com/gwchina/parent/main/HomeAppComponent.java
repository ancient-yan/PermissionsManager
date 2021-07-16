package com.gwchina.parent.main;

import com.gwchina.parent.main.injection.MainModule;
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
                MainModule.class
        }
)
@Singleton
interface HomeAppComponent {

    void inject(HomeAppContext homeAppContext);

}