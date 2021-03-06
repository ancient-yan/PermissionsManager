package com.android.base.utils.android.compat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.base.R;

import timber.log.Timber;

/**
 * SystemBar工具类
 *
 * <p>
 * other useful utils:
 * <li>https://github.com/Zackratos/UltimateBar</li>
 * <li>https://github.com/niorgai/StatusBarCompat</li>
 * <li>https://github.com/laobie/StatusBarUtil</li>
 * <li>https://github.com/msdx/status-bar-compat</li>
 * </p>
 *
 * @author Ztiany
 * Date :   2016-03-16 21:52
 */
public class SystemBarCompat {

    private SystemBarCompat() {
        throw new UnsupportedOperationException();
    }

    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";

    ///////////////////////////////////////////////////////////////////////////
    //                                                  Kitkat
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentStatusOn19(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentSystemUi(activity, true, false);
        }
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentNavigationOn19(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentSystemUi(activity, false, true);
        }
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentOn19(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentSystemUi(activity, true, true);
        }
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static View setStatusBarColorOn19(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            return setupStatusBarView(activity, decorView, color);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                                               After L
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentStatusAfter19(Activity activity) {
        if (!AndroidVersion.above(20)) {
            return;
        }
        setTranslucentSystemUi(activity, true, false);
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentNavigationAfter19(Activity activity) {
        if (!AndroidVersion.above(20)) {
            return;
        }
        setTranslucentSystemUi(activity, false, true);
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentAfter19(Activity activity) {
        if (!AndroidVersion.above(20)) {
            return;
        }
        setTranslucentSystemUi(activity, true, true);
    }

    public static void setupStatusBarColorAfter19(Activity activity, @ColorInt int color) {
        if (!AndroidVersion.above(20)) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setStatusBarColor(color);
    }

    public static void setupNavigationBarColorAfter19(Activity activity, @ColorInt int color) {
        if (!AndroidVersion.above(20)) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(color);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTranslucentSystemUi(Activity activity, boolean status, boolean navigation) {
        Window win = activity.getWindow();
        setTranslucentSystemUi(win, status, navigation);
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        setStatusBarColorOn19(activity, color);
        setupStatusBarColorAfter19(activity, color);
    }

    public static void setTranslucentSystemUi(Window win, boolean status, boolean navigation) {
        if (!AndroidVersion.atLeast(19)) {
            return;
        }
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (status) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (navigation) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 适用于4.4，在 rootView 中添加一个与 StatusBar 高度一样的 View，用于对状态栏着色
     *
     * @param context  上下文
     * @param rootView 用于添加着色View的根View
     * @param color    着色
     * @return 被添加的View
     */
    @SuppressWarnings("WeakerAccess,unused")
    public static View setupStatusBarView(Context context, ViewGroup rootView, @ColorInt int color) {
        View statusBarTintView = rootView.findViewById(R.id.base_status_view_id);
        if (statusBarTintView == null) {
            statusBarTintView = new View(context);
            statusBarTintView.setId(R.id.base_status_view_id);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight(context));
            layoutParams.gravity = Gravity.TOP;
            statusBarTintView.setLayoutParams(layoutParams);
            rootView.addView(statusBarTintView, 0);
        }
        statusBarTintView.setBackgroundColor(color);
        return statusBarTintView;
    }

    /**
     * 获取状态栏高度
     */
    @SuppressWarnings("WeakerAccess,unused")
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(STATUS_BAR_HEIGHT_RES_NAME, "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取NavigationBar高度
     *
     * @param context 上下文
     */
    @SuppressWarnings("WeakerAccess,unused")
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier(NAV_BAR_HEIGHT_RES_NAME, "dimen", "android");
        if (id > 0 && hasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    /**
     * 获取是否存在 NavigationBar
     *
     * @see <a href='https://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically'>detect-soft-navigation-bar-availability-in-android-device-progmatically</a>, <a href='https://windysha.github.io/2018/02/07/Android-APP%E9%80%82%E9%85%8D%E5%85%A8%E9%9D%A2%E5%B1%8F%E6%89%8B%E6%9C%BA%E7%9A%84%E6%8A%80%E6%9C%AF%E8%A6%81%E7%82%B9/'>Android APP适配全面屏手机的技术要点</>
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasNavigationBar(Context context) {
        if (!hasSoftKeys(context)) {
            Timber.d("hasSoftKeys = false");
            return false;
        }

        Timber.d("hasSoftKeys = true");

        WindowManager systemService;
        if (context instanceof Activity) {
            systemService = ((Activity) context).getWindowManager();
        } else {
            systemService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        Point realSize = new Point();
        Point screenSize = new Point();
        boolean hasNavBar = false;
        DisplayMetrics metrics = new DisplayMetrics();

        systemService.getDefaultDisplay().getRealMetrics(metrics);
        realSize.x = metrics.widthPixels;
        realSize.y = metrics.heightPixels;
        systemService.getDefaultDisplay().getSize(screenSize);

        if (realSize.y != screenSize.y) {
            Timber.d("realSize.y = %d screenSize.y = %d", realSize.y, screenSize.y);
            int difference = realSize.y - screenSize.y;
            int navBarHeight = 0;
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navBarHeight = resources.getDimensionPixelSize(resourceId);
            }
            Timber.d("navBarHeight = %d ", navBarHeight);
            if (navBarHeight != 0) {
                if (difference >= navBarHeight) {
                    hasNavBar = true;
                }
            }

        }
        Timber.d("hasNavigationBar = " + hasNavBar);
        return hasNavBar;
    }

    /**
     * 判断有没有导航栏，参考：https://github.com/roughike/BottomBar/blob/master/bottom-bar/src/main/java/com/roughike/bottombar/NavbarUtils.java
     *
     * @param context 上下文
     * @return true表示有
     */
    @SuppressLint("ObsoleteSdkInt")
    private static boolean hasSoftKeys(Context context) {

        boolean hasSoftwareKeys = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    /**
     * 获取ActionBar高度
     *
     * @param activity activity
     * @return ActionBar高度
     */
    @SuppressWarnings("WeakerAccess,unused")
    public static int getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                                               View Flags
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentStatusViaViewFlags(Activity activity) {
        setTranslucentSystemUiViaViewFlags(activity, true, false);
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentNavigationViaViewFlags(Activity activity) {
        setTranslucentSystemUiViaViewFlags(activity, false, true);
    }

    @SuppressWarnings("WeakerAccess,unused")
    public static void setTranslucentSystemUiViaViewFlags(Activity activity) {
        setTranslucentSystemUiViaViewFlags(activity, true, true);
    }

    private static void setTranslucentSystemUiViaViewFlags(Activity activity, boolean status, boolean navigation) {
        Window window = activity.getWindow();
        if (AndroidVersion.atLeast(21)) {
            if (status && !navigation) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            if (navigation && !status) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
            if (navigation && status) {
                window.clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
            setupStatusBarColorAfter19(activity, Color.TRANSPARENT);
            setupNavigationBarColorAfter19(activity, Color.TRANSPARENT);
        } else if (AndroidVersion.at(19)) {
            setTranslucentSystemUi(window, status, navigation);
        }
    }

}