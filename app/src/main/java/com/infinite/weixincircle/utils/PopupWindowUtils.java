package com.infinite.weixincircle.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.infinite.weixincircle.MyApp;
import com.infinite.weixincircle.R;


/**
 * PopupWindow工具类
 */
public class PopupWindowUtils {


    /**
     * 从anchor的底部弹出popwindow
     *
     * @param anchor
     * @param view   popwindow中的内容
     * @return
     */
    public static PopupWindow createPopWindowFromBottom(View anchor, Object view) {
        return new PopupWindowUtils.Builder(anchor, view).create();
    }

    public static class Builder {
        private View anchor;
        private Object view;
        private int gravity = Gravity.BOTTOM;
        private boolean outside_touchable = true;
        private boolean focusable = true;
        private boolean showAtCurrentPosition = false;
        private int width = -1;
        private int height = -2;
        private int offset_x = 0;
        private int offset_y = 0;
        private int anim_id = R.style.AnimationFromBottom;


        public Builder(View anchor, Object view) {
            this.anchor = anchor;
            this.view = view;
        }


        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setShowAtCurrentPosition(boolean showAtCurrentPosition) {
            this.showAtCurrentPosition = showAtCurrentPosition;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setOffsetX(int offset_x) {
            this.offset_x = offset_x;
            return this;
        }

        public Builder setOffsetY(int offset_y) {
            this.offset_y = offset_y;
            return this;
        }

        public Builder setOutsideTouchable(boolean outside_touchable) {
            this.outside_touchable = outside_touchable;
            return this;
        }

        public Builder setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public Builder setAnimationStyle(int anim_id) {
            this.anim_id = anim_id;
            return this;
        }


        public PopupWindow createDefault(final View anchor,
                                         Object view,
                                         int gravity,
                                         boolean outside_touchable,
                                         boolean focusable,
                                         boolean showAtCurrentPosition,
                                         int width, int height,
                                         int offset_x,
                                         int offset_y,
                                         int anim_id) {
            LayoutInflater inflater = (LayoutInflater)
                    MyApp.sInstance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View pop_view = null;
            if (view instanceof Integer) {
                pop_view = inflater.inflate((Integer) view, null, false);
            }
            if (view instanceof View) {
                pop_view = (View) view;
            }
            PopupWindow popWindow = new PopupWindow(pop_view, width, height, focusable);
            popWindow.setOutsideTouchable(outside_touchable);
            popWindow.update();
            if (outside_touchable) {
                ColorDrawable dw = new ColorDrawable(0x33000000);
                popWindow.setBackgroundDrawable(dw);
            }
            if (anim_id != -1) {
                popWindow.setAnimationStyle(anim_id);
            }


            if (showAtCurrentPosition) {
                popWindow.showAsDropDown(anchor, offset_x, offset_y);
            } else {

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    anchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//                }
                popWindow.showAtLocation(anchor, gravity, offset_x, offset_y +
                        DeviceUtils.getNavigationBarHeight(MyApp.getInstance()));
            }
            return popWindow;
        }


        public PopupWindow create() {
            return createDefault(anchor, view, gravity, outside_touchable, focusable, showAtCurrentPosition, width, height, offset_x, offset_y, anim_id);
        }

    }
}


