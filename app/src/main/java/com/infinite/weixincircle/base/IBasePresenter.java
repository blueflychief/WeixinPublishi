package com.infinite.weixincircle.base;


public interface IBasePresenter {
    void setTag(String tag);

    void start();

    void resume();

    void pause();

    void stop();

    void destroy();
}
