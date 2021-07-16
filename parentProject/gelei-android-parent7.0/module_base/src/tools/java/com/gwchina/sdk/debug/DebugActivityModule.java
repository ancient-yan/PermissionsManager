package com.gwchina.sdk.debug;

import com.android.base.app.dagger.ActivityScope;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DebugActivityModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract DebugActivity bindBaseDebugActivity();

}
