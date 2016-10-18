package com.infinite.weixincircle.utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventDispatchManager {
    private Queue<SubscriberListener> mSubscriberListeners = new ConcurrentLinkedQueue<>();

    private EventDispatchManager() {
        EventBus.getDefault().register(this);
    }

    private static EventDispatchManager sEventDispatchManager = new EventDispatchManager();

    public static EventDispatchManager getInstance() {
        return sEventDispatchManager;
    }

    /**
     * 事件信息
     */
    public static final class MyEvent {
        public int eventType;
        public Object data;
        public Object data2;

        public MyEvent(int eventType) {
            this.eventType = eventType;
        }

        public MyEvent(int eventType, Object data) {
            this.eventType = eventType;
            this.data = data;
        }

        public MyEvent(int eventType, Object data, Object data2) {
            this(eventType, data);
            this.eventType=eventType;
            this.data= data;
            this.data2 = data2;
        }
    }

    /**
     * 注册一个事件
     */
    public void register(SubscriberListener listener) {
        if (!mSubscriberListeners.contains(listener)) {
            mSubscriberListeners.add(listener);
        }
    }

    /**
     * 反注册一个事件
     */
    public void unRegister(SubscriberListener listener) {
        if (mSubscriberListeners.contains(listener)) {
            mSubscriberListeners.remove(listener);
        }
    }

    /**
     * 分发一个事件
     *
     * @param event
     */
    public void dispatchEvent(MyEvent event) {
        EventBus.getDefault().post(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(MyEvent event) {
        if (!mSubscriberListeners.isEmpty()) {
            Iterator<SubscriberListener> iterator = mSubscriberListeners.iterator();
            while (iterator.hasNext()) {
                SubscriberListener listener = iterator.next();
                if (listener != null) {
                    listener.onEventMain(event);
                }
            }
        }
    }

    /**
     * 内存不足 清除event
     */
    @Override
    protected void finalize() throws Throwable {
        mSubscriberListeners.clear();
        EventBus.getDefault().unregister(this);
        super.finalize();
    }

    /**
     * 事件订阅者回调接口
     * 注：在UI主线程运行
     */
    public interface SubscriberListener {
        void onEventMain(MyEvent event);
    }
}
