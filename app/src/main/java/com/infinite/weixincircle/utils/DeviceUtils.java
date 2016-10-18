package com.infinite.weixincircle.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;
import java.util.regex.Pattern;

public class DeviceUtils {


    /**
     * 获取屏幕的宽高
     *
     * @param ctx
     * @return
     */
    public static int[] getScreenWH(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return new int[]{point.x, point.y};
    }

    public static int getStatusBarHeight(Context c) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = c.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    //是否有虚拟导航按键
    public static boolean hasNavigationBar(Context ctx) {
        //判断是否有物理返回键、菜单键
        boolean hasMenuKey = ViewConfiguration.get(ctx)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);
        return !hasMenuKey && !hasBackKey;
    }

    //获取NavigationBar高度
    public static int getNavigationBarHeight(Context ctx) {
        if (hasNavigationBar(ctx)) {
            Resources resources = ctx.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

}
