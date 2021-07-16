package com.roughike.bottombar;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.android.base.utils.android.compat.SystemBarCompat;
import com.app.base.R;

/**
 * Created by iiro on 17.8.2016.
 */
final class NavbarUtils {
    private static final int RESOURCE_NOT_FOUND = 0;

    @IntRange(from = 0)
    static int getNavbarHeight(@NonNull Context context) {
        Resources res = context.getResources();
        int navBarIdentifier = res.getIdentifier("navigation_bar_height", "dimen", "android");
        return navBarIdentifier != RESOURCE_NOT_FOUND
                ? res.getDimensionPixelSize(navBarIdentifier) : 0;
    }

    static boolean shouldDrawBehindNavbar(@NonNull Context context) {
        return isPortrait(context)
                && hasSoftKeys(context);
    }

    private static boolean isPortrait(@NonNull Context context) {
        Resources res = context.getResources();
        return res.getBoolean(R.bool.bb_bottom_bar_is_portrait_mode);
    }

    /**
     * http://stackoverflow.com/a/14871974
     */
    private static boolean hasSoftKeys(@NonNull Context context) {
        return SystemBarCompat.hasNavigationBar(context);
    }

}
