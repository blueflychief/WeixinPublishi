package com.infinite.weixincircle.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bugtags.library.Bugtags;
import com.infinite.weixincircle.config.ActivityManager;
import com.infinite.weixincircle.utils.CommonUtils;
import com.infinite.weixincircle.utils.EventDispatchManager;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements EventDispatchManager.SubscriberListener {

    protected final String TAG = this.getClass().getSimpleName();

    protected IBasePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        EventDispatchManager.getInstance().register(this);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY);  //防止在WebView中长按复制出现标题栏显示错误

        initWindows();

        mPresenter = getPresenter();

        if (mPresenter != null) {
            mPresenter.setTag(TAG);
        }

        setContentView(getLayoutId());

        ButterKnife.bind(this);

        findViews();

        initData();
    }

    protected abstract int getLayoutId();

    protected IBasePresenter getPresenter() {
        return null;
    }

    protected void findViews() {
        //设置toolbar颜色 必须在setContentView 后调用
        if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN) {
//            StatusBarCompat.compat(this);
        }
    }

    protected void initWindows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//4.4到5.0
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    protected void initData() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
        if (mPresenter != null) {
            mPresenter.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
        CommonUtils.dismissSoftKeyBoard(this);
        if (mPresenter != null) {
            mPresenter.pause();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
            mPresenter = null;
        }
        EventDispatchManager.getInstance().unRegister(this);
        ActivityManager.getInstance().removeActivity(this);
    }

    protected void setTransparentNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    @Override
    public void onEventMain(EventDispatchManager.MyEvent event) {

    }
}
