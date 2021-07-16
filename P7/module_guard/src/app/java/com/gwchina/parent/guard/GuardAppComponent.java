package com.gwchina.parent.guard;

import com.gwchina.parent.apps.injection.AppGuardModule;
import com.gwchina.parent.level.injection.GuardLevelModule;
import com.gwchina.parent.family.injection.FamilyPhoneModel;
import com.gwchina.parent.net.injection.NetGuardModule;
import com.gwchina.parent.times.injection.TimeGuardModule;
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
                AppGuardModule.class,
                TimeGuardModule.class,
                LevelModule.class,
                NetGuardModule.class,
                FamilyPhoneModel.class
        }
)
@Singleton
public interface GuardAppComponent {

    void inject(GuardAppContext appContext);

}
