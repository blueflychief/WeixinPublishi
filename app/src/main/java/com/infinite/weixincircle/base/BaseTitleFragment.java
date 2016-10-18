package com.infinite.weixincircle.base;

import android.view.View;

import com.infinite.weixincircle.R;
import com.infinite.weixincircle.widget.MyAppBar;


public abstract class BaseTitleFragment extends BaseFragment {
    protected MyAppBar mToolbar;

    @Override
    protected void findViews() {
        super.findViews();
        mToolbar = (MyAppBar) findViewById(R.id.my_toolbar);
        mToolbar.setTitleConfig(getTitleViewConfig());
     /*   ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        //不显示Toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);*/
    }

    public abstract MyAppBar.TitleConfig getTitleViewConfig();

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 创建一个默认的标题配置
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
            getActivity().finish();
        }
    };
}
