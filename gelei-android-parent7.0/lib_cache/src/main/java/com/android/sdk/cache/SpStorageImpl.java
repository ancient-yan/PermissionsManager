package com.android.sdk.cache;

import android.content.Context;
import android.support.annotation.Nullable;

import com.github.dmstocking.optional.java.util.Optional;

import java.lang.reflect.Type;

import io.reactivex.Flowable;

/**
 * @author hujl
 * Email: hujlin@163.com
 * Date : 2020-02-28 19:27
 */
public class SpStorageImpl implements Storage {
    private SpCacheHelper mSpCacheHelper;

    public SpStorageImpl(Context context, Boolean useApply) {
        mSpCacheHelper = new SpCacheHelper(context, context.getPackageName(), useApply);
    }

    @Override
    public void putString(String key, String value) {
        try {
            if (value == null) {
                remove(key);
                return;
            }
            mSpCacheHelper.putString(key, value);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    @Override
    public String getString(String key, String defaultValue) {
        try {
            return mSpCacheHelper.getString(key, defaultValue);
        } catch (Error error) {
            error.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public String getString(String key) {
        try {
            return mSpCacheHelper.getString(key, "");
        } catch (Error error) {
            error.printStackTrace();
        }
        return null;
    }

    @Override
    public void putLong(String key, long value) {
        try {
            mSpCacheHelper.putLong(key, value);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            return mSpCacheHelper.getLong(key, defaultValue);
        } catch (Error error) {
            error.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public void putInt(String key, int value) {
        try {
            mSpCacheHelper.putInt(key, value);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return mSpCacheHelper.getInt(key, defaultValue);
        } catch (Error error) {
            error.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        try {
            mSpCacheHelper.putBoolean(key, value);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return mSpCacheHelper.getBoolean(key, defaultValue);
        } catch (Error error) {
            error.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public void remove(String key) {
        try {
            mSpCacheHelper.remove(key);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    @Override
    public void clearAll() {
        mSpCacheHelper.clear();
    }

    @Override
    public void putEntity(String key, Object entity, long cacheTime) {
        CommonImpl.putEntity(key, entity, cacheTime, this);
    }

    @Override
    public void putEntity(String key, Object entity) {
        CommonImpl.putEntity(key, entity, 0, this);
    }

    @Override
    public <T> T getEntity(String key, Type type) {
        return CommonImpl.getEntity(key, type, this);
    }

    @Override
    public <T> Flowable<T> flowable(String key, Type type) {
        return CommonImpl.flowableEntity(key, type, this);
    }

    @Override
    public <T> Flowable<Optional<T>> flowableOptional(String key, Type type) {
        return CommonImpl.flowableOptionalEntity(key, type, this);
    }
}
