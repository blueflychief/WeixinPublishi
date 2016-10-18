package com.infinite.weixincircle.widget.photobrowse;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;


import com.infinite.weixincircle.R;
import com.infinite.weixincircle.base.BaseTitleActivity;
import com.infinite.weixincircle.config.Dictionary;
import com.infinite.weixincircle.utils.EventDispatchManager;
import com.infinite.weixincircle.utils.GlideLoaderUtil;
import com.infinite.weixincircle.utils.ImageUtils;
import com.infinite.weixincircle.utils.ToastUtils;
import com.infinite.weixincircle.utils.ViewUtils;
import com.infinite.weixincircle.widget.CustomDialogClickListener;
import com.infinite.weixincircle.widget.DotIndicatorView;
import com.infinite.weixincircle.widget.HackyViewPager;
import com.infinite.weixincircle.widget.MyAppBar;
import com.infinite.weixincircle.widget.MyDialog;
import com.infinite.weixincircle.widget.photobrowse.adapter.PhotoBrowseAdapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;

public class PhotoBrowseActivity extends BaseTitleActivity implements
        PhotoBrowseAdapter.OnImageClickListener,
        PhotoBrowseAdapter.SavePictureCallback {

    @BindView(R.id.pv_viewpager)
    HackyViewPager pvViewpager;
    @BindView(R.id.dv_indicator)
    DotIndicatorView dvIndicator;
    private List<String> mPics;
    private int mCurrent;
    private PhotoBrowseAdapter mAdapter;
    private boolean mPicturesChanged = false;
    private int mBrowseType = Dictionary.EventType.TYPE_BROWSE_DEFAULT;  //浏览类型
    private String mCurrentPicUrl;


    private SaveHandler mHandlerThread;
    private Handler mHandler;

    @Override
    public MyAppBar.TitleConfig getTitleViewConfig() {
        Intent intent = getIntent();
        if (intent != null) {
            mPics = intent.getStringArrayListExtra("pics");
            mCurrent = intent.getIntExtra("current", 1);
            mBrowseType = intent.getIntExtra("type", Dictionary.EventType.TYPE_BROWSE_DEFAULT);
        } else {
            ToastUtils.showToast(getString(R.string.error_data_delay_try));
            finish();
        }
        if (mBrowseType == Dictionary.EventType.TYPE_BROWSE_WITH_DELETE) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_deletc_gray);
            imageView.setPadding(4, 4, 4, 4);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int p = pvViewpager.getCurrentItem();
                    mAdapter.remove(p);
                    dvIndicator.setDotNumber(mPics.size());
                    mPicturesChanged = true;
                    if (mAdapter.getCount() == 0) {
                        onBackPressed();
                    }
                }
            });
            return buildDefaultConfig(getString(R.string.string_view_photo))
                    .setRightObject(new View[]{imageView});
        } else {
            return buildDefaultConfig(getString(R.string.string_view_photo));
        }
    }


    @Override
    public void onBackPressed() {
        if (mPicturesChanged) {
            EventDispatchManager.getInstance().dispatchEvent(new EventDispatchManager.MyEvent(Dictionary.EventType.TYPE_BROWSE_PICTURE_OK, mPics));
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo_browse;
    }

    @Override
    protected void findViews() {
        super.findViews();
        if (mBrowseType == Dictionary.EventType.TYPE_BROWSE_DEFAULT) {
            mToolbar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        dvIndicator.setDotNumber(mPics.size());
        dvIndicator.setCurrentDot(mCurrent + 1);
        mAdapter = new PhotoBrowseAdapter(mPics);
        if (mBrowseType == Dictionary.EventType.TYPE_BROWSE_DEFAULT) {
            mAdapter.setSavePicture(true);
            mAdapter.setSavePictureCallback(this);
            mHandlerThread = new SaveHandler("save", this);
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);
        }
        mAdapter.setOnImageClickListener(this);
        pvViewpager.setAdapter(mAdapter);
        pvViewpager.setCurrentItem(mCurrent);
        pvViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dvIndicator.setCurrentDot(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onPicClick() {
        onBackPressed();
    }

    @Override
    public void onSavePicture(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        MyDialog dialog = new MyDialog(
                getString(R.string.string_tips),
                getString(R.string.string_save_pic_desc),
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                true,
                new CustomDialogClickListener() {
                    @Override
                    public void onPostiveClick(DialogInterface dialog, int which) {
                        mCurrentPicUrl = url;
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(1);
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                });
        dialog.show(getSupportFragmentManager(), "Dialog");
    }

    @Override
    public void onEventMain(EventDispatchManager.MyEvent event) {

    }


    static class SaveHandler extends HandlerThread implements Handler.Callback {
        private WeakReference<PhotoBrowseActivity> mActivity;

        public SaveHandler(String name, PhotoBrowseActivity activity) {
            super(name);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public boolean handleMessage(Message msg) {
            final PhotoBrowseActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        ViewUtils.showLoadingDialog(activity, activity.getString(R.string.string_saving_image));
                        break;
                    case 2:
                        //保存图片
                        final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "temp";
                        String fileName = "_image_" + System.currentTimeMillis() + ".jpg";
                        if (ImageUtils.savePicture(GlideLoaderUtil.downloadImage(activity, activity.mCurrentPicUrl), dir, fileName)) {
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri uri = Uri.fromFile(new File(dir + File.separator + fileName));
                            intent.setData(uri);
                            activity.sendBroadcast(intent);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showToast(activity.getString(R.string.string_saving_image_to, dir));
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showToast(R.string.string_save_pic_failed);
                                }
                            });
                        }
                        activity.mHandler.sendEmptyMessage(3);
                        break;
                    case 3:
                        ViewUtils.closeLoadingDialog();
                        break;
                }
            }
            return true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
