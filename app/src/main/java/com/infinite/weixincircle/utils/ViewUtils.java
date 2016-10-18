package com.infinite.weixincircle.utils;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.infinite.weixincircle.widget.MyLoadingDialog;


public class ViewUtils {

    public static final int FROM_GALLERY = 100;
    public static final int FROM_CAPTURE = 101;
    public static final int FROM_CROP = 102;
    public static final int FROM_AUDIO = 103;
    public static final int FROM_VIDEO = 104;
    public static final int TYPE_MEDIA = 105;
    public static final int TYPE_PIC = 106;

    private static MyLoadingDialog sMLoadingDialogFragment;

    private static boolean isDialogShowing;



    /**
     * 显示加载提示框
     *
     * @param message 提示信息
     * @return
     */

    public static synchronized MyLoadingDialog showLoadingDialog(FragmentActivity activity, String message) {
        if (!isDialogShowing) {
            isDialogShowing=true;
            sMLoadingDialogFragment = MyLoadingDialog.createDialogFragment(message);
            sMLoadingDialogFragment.setCancelable(false);
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.add(sMLoadingDialogFragment, "Loading");
            ft.commitAllowingStateLoss();
        } else {
            sMLoadingDialogFragment.setMessage(message);
        }
        return sMLoadingDialogFragment;
    }

    /**
     * 关闭加载提示框
     */
    public static synchronized void closeLoadingDialog() {
        isDialogShowing=false;
        if (sMLoadingDialogFragment != null) {
            sMLoadingDialogFragment.dismissAllowingStateLoss();
            if (sMLoadingDialogFragment.isDetached()) {
                sMLoadingDialogFragment.onDetach();
            }
            sMLoadingDialogFragment = null;
        }
    }


    /**
     * 判断是否显示中
     *
     * @return
     */
    public static synchronized boolean isShowing() {
        return sMLoadingDialogFragment != null && sMLoadingDialogFragment.getDialog() != null && sMLoadingDialogFragment.getDialog().isShowing();
    }
}
