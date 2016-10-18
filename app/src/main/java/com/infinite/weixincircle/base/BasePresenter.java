package com.infinite.weixincircle.base;

public abstract class BasePresenter implements IBasePresenter {
    protected String mTag;

    @Override
    public void setTag(String tag) {
        mTag = tag;
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}
