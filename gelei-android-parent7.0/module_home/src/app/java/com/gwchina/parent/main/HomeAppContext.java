package com.gwchina.parent.main;


import com.gwchina.sdk.base.di.AppModule;
import com.gwchina.sdk.debug.DebugAppContext;

public class HomeAppContext extends DebugAppContext {

    @Override
    protected void injectAppContext() {
        DaggerHomeAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build()
                .inject(this);
    }

}