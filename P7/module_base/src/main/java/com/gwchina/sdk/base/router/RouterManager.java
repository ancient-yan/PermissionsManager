package com.gwchina.sdk.base.router;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;
import com.app.base.BuildConfig;

import java.util.List;

/**
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-06-22 14:36
 */
public class RouterManager {

    public static void init(Application application) {
        if (BuildConfig.openDebug) {
            //此设置需放在init()方法之前调用，否则会造成异常
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(application);
    }

    static <T extends IProvider> T navigation(Class<T> iProviderClass) {
        return ARouter.getInstance().navigation(iProviderClass);
    }

    /**
     * @param path 路径
     * @return 路由表
     */
    static IPostcard build(String path) {
        Postcard build = ARouter.getInstance().build(path);
        return new PostcardImpl(build);
    }

    /**
     * @param path 路径
     * @return 路由表
     */
    static IPostcard build(Uri path) {
        Postcard build = ARouter.getInstance().build(path);
        return new PostcardImpl(build);
    }

    public static void inject(Object object) {
        ARouter.getInstance().inject(object);
    }

    @SuppressWarnings("unused")
    public static void restartApp(Activity activity, Class<? extends Activity> target) {
        Intent intent = new Intent(activity, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * ARouter 不支持 Fragment.startActivityForResult()，暂时使用此方法分发
     */
    @SuppressWarnings("WeakerAccess")
    public static void dispatchActivityResult(FragmentManager fragmentManager, int requestCode, int resultCode, Intent data) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments.isEmpty()) {
            return;
        }
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
                dispatchActivityResult(fragment.getChildFragmentManager(), requestCode, resultCode, data);
            }
        }
    }

}