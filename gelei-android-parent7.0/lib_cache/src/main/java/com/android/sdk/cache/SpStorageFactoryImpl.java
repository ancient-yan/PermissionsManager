package com.android.sdk.cache;

import android.content.Context;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 17:35
 */
public class SpStorageFactoryImpl implements StorageFactory {

    @Override
    public Builder newBuilder(Context context) {
        return new SpStorageBuilder(context);
    }

    class SpStorageBuilder extends Builder {

        SpStorageBuilder(Context context) {
            super(context);
        }

        @Override
        public Storage build() {
            return new SpStorageImpl(context.getApplicationContext(), false);
        }

    }

}
