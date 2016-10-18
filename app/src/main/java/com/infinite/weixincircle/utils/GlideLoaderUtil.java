package com.infinite.weixincircle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.infinite.weixincircle.R;
import com.infinite.weixincircle.widget.photobrowse.LoadImageCallback;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static com.bumptech.glide.Glide.with;



public class GlideLoaderUtil {
    public static void loadImageWithPath(Context mContext, String path, int defaultId, ImageView image) {
        with(mContext)
                .load(new File(path))
                .centerCrop()
                .thumbnail(0.1f)
                .placeholder(defaultId == -1 ? R.drawable.bg_transparent_shape : defaultId)
                .error(defaultId == -1 ? R.drawable.bg_transparent_shape : defaultId)
                .into(image);
    }

    /**
     * 加载普通图片
     *
     * @param mContext
     * @param url
     * @param defaultId 为-1时加载灰色图片
     * @param iv
     */
    public static void loadNormalImage(Context mContext, String url, int defaultId, ImageView iv) {
        loadNormalImage(mContext, url, defaultId, iv, true, null);
    }

    /**
     * 如果需要在图片加载成功之后做回调用此方法
     *
     * @param mContext
     * @param url
     * @param defaultId
     * @param iv
     * @param cache
     * @param callback
     */
    public static void loadNormalImage(Context mContext, String url, final int defaultId, final ImageView iv, boolean cache, final LoadImageCallback callback) {
        if (TextUtils.isEmpty(url)) {
            iv.setImageResource(defaultId == -1 ? R.drawable.ic_default_square_small : defaultId);
            return;
        }
        with(mContext)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(defaultId == -1 ? R.drawable.ic_default_square_small : defaultId)
                .error(defaultId == -1 ? R.drawable.ic_default_square_small : defaultId)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            iv.setImageBitmap(bitmap);
                            if (callback != null && iv.getDrawable() != null) {
                                callback.onBitmapLoaded(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        iv.setImageResource(defaultId == -1 ? R.drawable.bg_transparent_shape : defaultId);
                    }
                });
    }

    /**
     * 下载一张图片
     * @param ctx
     * @param url
     * @return
     */
    public static Bitmap downloadImage(Context ctx, String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        try {
            return Glide.with(ctx).load(url).asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
