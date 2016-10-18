package com.infinite.weixincircle.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.infinite.weixincircle.R;
import com.infinite.weixincircle.utils.EventDispatchManager;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements EventDispatchManager.SubscriberListener {

    protected final String TAG = this.getClass().getSimpleName();

    protected View mRootView;

    protected boolean mHasInit = false;

    protected IBasePresenter mPresenter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mHasInit) {
            EventDispatchManager.getInstance().register(this);
            mPresenter = getPresenter();
            findViews();
            initData();
        }
        mHasInit = true;
    }


    protected IBasePresenter getPresenter() {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRootView != null) {
            ViewGroup parent = ((ViewGroup) mRootView.getParent());
            if (parent != null) {
                parent.removeView(mRootView);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventDispatchManager.getInstance().unRegister(this);
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    protected View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        }
    }

    protected abstract int getLayoutId();

    /**
     * 查找控件
     */
    protected void findViews() {
        if (mPresenter != null) {
            mPresenter.setTag(TAG);
        }
        ButterKnife.bind(this, mRootView);
    }

    /**
     * 初始化数据
     */
    protected void initData() {
    }

    @Override
    public void onEventMain(EventDispatchManager.MyEvent event) {

    }
}
