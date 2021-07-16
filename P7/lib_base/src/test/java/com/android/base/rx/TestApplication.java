package com.android.base.rx;

import android.app.Application;
import android.content.Context;

import com.android.base.app.BaseKit;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-28 17:50
 */
public class TestApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BaseKit.get().getApplicationDelegate().attachBaseContext(base);
    }

}
