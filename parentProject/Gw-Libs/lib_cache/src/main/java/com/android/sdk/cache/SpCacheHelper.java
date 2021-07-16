package com.android.sdk.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * modified from <a href='https://github.com/hongyangAndroid/SpCache'>hongyangAndroid/SpCache</a>
 */
public class SpCacheHelper {

    private static final String TAG = SpCacheHelper.class.getSimpleName();

    private final SharedPreferences mSharedPreferences;
    private final boolean mUseApply;

    public SpCacheHelper(Context applicationContext, String prefFileName) {
        this(applicationContext, prefFileName, true);
    }

    public SpCacheHelper(Context context, String prefFileName, boolean useApply) {
        if (TextUtils.isEmpty(prefFileName)) {
            throw new NullPointerException("SpCache get fileName = null");
        }
        mSharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        mUseApply = useApply;
    }

    //put
    public SpCacheHelper putInt(String key, int val) {
        return put(key, val);
    }

    public SpCacheHelper putLong(String key, long val) {
        return put(key, val);
    }

    public SpCacheHelper putString(String key, String val) {
        return put(key, val);
    }

    public SpCacheHelper putBoolean(String key, boolean val) {
        return put(key, val);
    }

    public SpCacheHelper putFloat(String key, float val) {
        return put(key, val);
    }

    //get
    public int getInt(String key, int defaultVal) {
        return (int) (get(key, defaultVal));
    }

    public long getLong(String key, long defaultVal) {
        return (long) (get(key, defaultVal));
    }

    public String getString(String key, String defaultVal) {
        return (String) (get(key, defaultVal));
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return (boolean) (get(key, defaultVal));
    }

    public float getFloat(String key, float defaultVal) {
        return (float) (get(key, defaultVal));
    }

    //contains
    public boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    //remove
    public SpCacheHelper remove(String key) {
        return _remove(key);
    }

    private SpCacheHelper _remove(String key) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor, mUseApply);
        return this;
    }

    //clear
    public SpCacheHelper clear() {
        return _clear();
    }

    private SpCacheHelper _clear() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor, mUseApply);
        return this;
    }

    private <T> SpCacheHelper put(String key, T t) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        if (t instanceof String) {
            editor.putString(key, (String) t);
        } else if (t instanceof Integer) {
            editor.putInt(key, (Integer) t);
        } else if (t instanceof Boolean) {
            editor.putBoolean(key, (Boolean) t);
        } else if (t instanceof Float) {
            editor.putFloat(key, (Float) t);
        } else if (t instanceof Long) {
            editor.putLong(key, (Long) t);
        } else {
            editor.putString(key, t.toString());
        }
        SharedPreferencesCompat.apply(editor, mUseApply);
        return this;
    }


    private Object readDisk(String key, Object defaultObject) {
        Log.e("TAG", "readDisk");
        SharedPreferences sp = getSharedPreferences();
        if (defaultObject == null) return null;
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        Log.e(TAG, "you can not read object , which class is " + defaultObject.getClass().getSimpleName());
        return null;

    }

    private Object get(String key, Object defaultVal) {
        return readDisk(key, defaultVal);
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         */
        public static void apply(final SharedPreferences.Editor editor, boolean useApply) {
            if (useApply) {
                try {
                    if (sApplyMethod != null) {
                        sApplyMethod.invoke(editor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    editor.commit();
                }
            } else {
                editor.commit();
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

}
