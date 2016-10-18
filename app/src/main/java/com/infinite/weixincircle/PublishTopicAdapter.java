package com.infinite.weixincircle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.infinite.weixincircle.utils.CommonUtils;

import java.util.List;

public class PublishTopicAdapter extends BaseAdapter {
    public List<String> mPics;
    private Context mContent;
    int padding = CommonUtils.dpTopx(MyApp.getInstance(), 4.0f);

    public PublishTopicAdapter(Context context, List<String> mPics) {
        this.mPics = mPics;
        mContent = context;
    }

    @Override
    public int getCount() {
        return mPics == null ? 0 : this.mPics.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mPics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new GridView.LayoutParams(-1, -1));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setPadding(padding, padding, padding, padding);
        if (position == mPics.size() - 1 && position < 3) {
            imageView.setImageResource(R.drawable.ic_add_pic);
        } else {
        }
        return imageView;
    }
}
