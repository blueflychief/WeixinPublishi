package com.infinite.weixincircle.config;

import android.content.Context;


public class Dictionary {

    /**
     * EventBus事件类型
     */
    public static final class EventType {

        //选择图片成功
        public static final int TYPE_SELECT_PICTURE_OK = 12;
        //浏览图片成功
        public static final int TYPE_BROWSE_PICTURE_OK = 13;

        //浏览图片默认类型
        public static final int TYPE_BROWSE_DEFAULT = 14;
        //浏览图片，可删除图片
        public static final int TYPE_BROWSE_WITH_DELETE = 15;

        //音频录制成功
        public static final int TYPE_AUDIO_RECORD_OK = 16;
        //音频删除
        public static final int TYPE_AUDIO_RECORD_DELETE = 17;

        //视频录制成功
        public static final int TYPE_VIDEO_RECORD_OK = 18;
        public static final int TYPE_VIDEO_RECORD_DELETE = 19;

    }


}
