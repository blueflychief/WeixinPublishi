package com.infinite.weixincircle.widget.photobrowse.adapter;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.infinite.weixincircle.R;
import com.infinite.weixincircle.utils.GlideLoaderUtil;
import com.infinite.weixincircle.widget.photobrowse.LoadImageCallback;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoBrowseAdapter extends PagerAdapter {

    private List<String> mPics;
    private OnImageClickListener mOnImageClickListener;
    private boolean saveable = false;
    private SavePictureCallback mSavePictureCallback;

    public PhotoBrowseAdapter(List<String> mPics) {
        this.mPics = mPics;
    }

    public void remove(int position) {
        mPics.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPics == null ? 0 : mPics.size();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View view = View.inflate(container.getContext(), R.layout.view_photo_browse, null);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.pv_photo);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (mOnImageClickListener != null) {
                    mOnImageClickListener.onPicClick();
                }
            }
        });

        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (saveable) {
                    if (mSavePictureCallback != null) {
                        mSavePictureCallback.onSavePicture(mPics.get(position));
                    }
                }
                return true;
            }
        });
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);

        GlideLoaderUtil.loadNormalImage(container.getContext(), mPics.get(position), -1, photoView, true, new LoadImageCallback() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                progressBar.setVisibility(View.GONE);
            }
        });
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return view;
    }

    public void setSavePictureCallback(SavePictureCallback savePictureCallback) {
        mSavePictureCallback = savePictureCallback;
    }

    public interface SavePictureCallback {
        void onSavePicture(String url);
    }

    public void setSavePicture(boolean saveable) {
        this.saveable = saveable;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    public void setOnImageClickListener(OnImageClickListener listener) {
        mOnImageClickListener = listener;
    }

    public interface OnImageClickListener {
        void onPicClick();
    }

}
