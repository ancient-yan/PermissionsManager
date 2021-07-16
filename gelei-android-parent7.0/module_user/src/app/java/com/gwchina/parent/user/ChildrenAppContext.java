package com.gwchina.parent.user;


import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.di.AppModule;

public class ChildrenAppContext extends AppContext {

    @Override
    protected void injectAppContext() {
        DaggerChildrenAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build()
                .inject(this);
    }

}