package com.infinite.weixincircle.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.infinite.weixincircle.R;
import com.infinite.weixincircle.widget.MyAppBar;


public abstract class BaseTitleActivity extends BaseActivity {

    private FrameLayout mContentLayout;

    protected MyAppBar mToolbar;
    protected View mSplitLine;


    @Override
    protected void initWindows() {
        super.initWindows();
        getDelegate().setContentView(R.layout.activity_base);

        mContentLayout = (FrameLayout) findViewById(R.id.content);
        mSplitLine = findViewById(R.id.split_line);
        mToolbar = (MyAppBar) findViewById(R.id.my_toolbar);
        mToolbar.setTitleConfig(getTitleViewConfig());
        setSupportActionBar(mToolbar);

        //不显示Toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void setContentView(int layoutResID) {
        if (layoutResID != 0) {
            mContentLayout.removeAllViews();
            getLayoutInflater().inflate(layoutResID, mContentLayout, true);
        }
    }

    @Override
    public void setContentView(View view) {
        mContentLayout.removeAllViews();
        mContentLayout.addView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mContentLayout.addView(view, params);
    }

    public abstract MyAppBar.TitleConfig getTitleViewConfig();

    /**
     * 1> 设置左边返回按钮的整件
     * 2> 设置标题文本
     *
     * @param title
     * @return
     */
    public MyAppBar.TitleConfig buildDefaultConfig(String title) {
        MyAppBar.TitleConfig config = new MyAppBar.TitleConfig(title);
        config.leftViewListener = mBackOnClickListener;
        return config;
    }

    /**
     * 默认左侧按钮点击事件
     */
    public View.OnClickListener mBackOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
