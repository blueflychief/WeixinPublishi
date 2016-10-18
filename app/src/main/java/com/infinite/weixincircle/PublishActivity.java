package com.infinite.weixincircle;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinite.weixincircle.base.BaseTitleActivity;
import com.infinite.weixincircle.base.IBasePresenter;
import com.infinite.weixincircle.config.Dictionary;
import com.infinite.weixincircle.utils.CommonUtils;
import com.infinite.weixincircle.utils.EventDispatchManager;
import com.infinite.weixincircle.utils.ImageUtils;
import com.infinite.weixincircle.utils.PopupWindowUtils;
import com.infinite.weixincircle.utils.ToastUtils;
import com.infinite.weixincircle.utils.ViewUtils;
import com.infinite.weixincircle.widget.AddPictureView;
import com.infinite.weixincircle.widget.MyAppBar;
import com.infinite.weixincircle.widget.photobrowse.PhotoBrowseActivity;
import com.infinite.weixincircle.widget.photopicker.utils.ImageCaptureManager;
import com.infinite.weixincircle.widget.photopicker.utils.PhotoPickerIntent;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PublishActivity extends BaseTitleActivity implements
        AddPictureView.OnPreviewListener, View.OnClickListener, PermissionListener {

    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.tv_word_count)
    TextView tvWordCount;
    @BindView(R.id.iv_add_video_or_audio)
    ImageView ivAddVideoAudio;
    @BindView(R.id.apv_add_pic)
    AddPictureView apvAddPic;
    @BindView(R.id.ll_root)
    LinearLayout llRoot;

    public static final String PUBLISH_MEDIA_TYPE = "publish_media_type";
    public static final int TYPE_NONE = 0;     //文字
    public static final int TYPE_PICTURE = 1;   //图片
    public static final int TYPE_VIDEO = 2;     //视频
    public static final int TYPE_AUDIO = 3;     //音频
    @BindView(R.id.iv_add_video_play)
    ImageView mIvAddVideoPlay;

    private PublishTopicAdapter mAdapter;
    private List<String> mImageList;
    public static final String KEY_CONTENT = "content";
    private int mMediaType = TYPE_NONE;  //文字0,图片abs(1),视频2,音频3
    private PopupWindow mPopupwindow;
    public final static int RESULT_PICKER = 1;
    public final static int RESULT_BORWSE = 2;
    private ArrayList<String> mSelectedPhotos;
    private String mAudioPath;
    private String mVideoPath;
    private final static int PICTURE_MAX = 9;   //目前需求是最多选三张

    private MyHandler mHandlerThread;
    private Handler mHandler;
    private String mContextString;
    private ImageCaptureManager captureManager;

    @Override
    public MyAppBar.TitleConfig getTitleViewConfig() {
        TextView mTvPublish = new TextView(this);
        mTvPublish.setTextSize(14);
        mTvPublish.setTextColor(getResources().getColor(R.color.public_text_black));
        mTvPublish.setText(R.string.string_publish);
        mTvPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextString = etContent.getText().toString().trim();

            }
        });
        return buildDefaultConfig(getString(R.string.string_publish_topic_title)).setRightObject(new View[]{mTvPublish});
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_publish;
    }

    @Override
    protected IBasePresenter getPresenter() {
        return null;
    }

    private CharSequence temp;
    private int editStart;
    private int editEnd;

    @Override
    protected void findViews() {
        super.findViews();
        mImageList = new ArrayList<>();
        mAdapter = new PublishTopicAdapter(this, mImageList);
        apvAddPic.setOnPreviewListener(this);
        ivAddVideoAudio.setOnClickListener(this);
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editStart = etContent.getSelectionStart();
                editEnd = etContent.getSelectionEnd();
                int len = 140 - temp.length();
                tvWordCount.setText(len + "字");
//                if (temp.length() > 140) {
//                    ToastUtils.showToast(getString(R.string.string_word_than_limited));
//                    etContent.setText(s.toString().substring(0, 140));
//                    etContent.setSelection(140);
//                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        Intent intent = getIntent();
        mSelectedPhotos = new ArrayList<>();
        if (intent != null) {
            mMediaType = intent.getIntExtra(PUBLISH_MEDIA_TYPE, TYPE_NONE);
            mAudioPath = intent.getStringExtra("audio_path");
            mVideoPath = intent.getStringExtra("video_path");
            mContextString = intent.getStringExtra(KEY_CONTENT);
            mSelectedPhotos = intent.getStringArrayListExtra("images_path");
        }
        if (!TextUtils.isEmpty(mContextString)) {
            etContent.setText(mContextString);
            tvWordCount.setText((140 - mContextString.length()) + "字");
        }
        mHandlerThread = new MyHandler("publish", this);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);
        showMediaIcon();
    }


    @Override
    public void onPreview(int pos, boolean showDelete) {
        Intent intent = new Intent(this, PhotoBrowseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("pics", mSelectedPhotos);
        bundle.putInt("current", pos);
        bundle.putInt("type", Dictionary.EventType.TYPE_BROWSE_WITH_DELETE);
        bundle.putString("flag", "view");
        intent.putExtras(bundle);
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(apvAddPic,
                apvAddPic.getWidth() / 2, apvAddPic.getHeight() / 2, 0, 0);
        ActivityCompat.startActivityForResult(this, intent, RESULT_BORWSE, compat.toBundle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mMediaType != TYPE_NONE) && (mVideoPath == null) && (mAudioPath == null) && (mSelectedPhotos == null || mSelectedPhotos.isEmpty())) {
            mMediaType = TYPE_PICTURE;
            showMediaIcon();
        }
    }

    @Override
    public void onPick() {
        if (((mVideoPath == null) && (mAudioPath == null) && (mSelectedPhotos == null || mSelectedPhotos.isEmpty()))
                || (mMediaType == TYPE_NONE)) {
            showPopwindow(true);
        } else if (mMediaType == TYPE_PICTURE || mMediaType == -1) {
            if (mSelectedPhotos.isEmpty()) {
                showPopwindow(true);
            } else {
                showPopwindow(false);
//                goToPickPhoto(mMediaType);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_capture_photo:
                mMediaType = TYPE_PICTURE;
                mPopupwindow.dismiss();
                break;
            case R.id.tv_pick_photo:
                mMediaType = -1;
                mPopupwindow.dismiss();
                break;
            case R.id.tv_record_video:
                mPopupwindow.dismiss();
                mMediaType = TYPE_VIDEO;
                break;
            case R.id.tv_record_audio:
                mPopupwindow.dismiss();
                mMediaType = TYPE_AUDIO;
                break;
            case R.id.iv_add_video_or_audio:

                break;
        }
//        showMediaIcon();
        goToPickPhoto(mMediaType);
        gotoAudioOrVideo(v);
    }

    private void gotoAudioOrVideo(View v) {
        Intent intent = null;
        if (mMediaType == TYPE_VIDEO) {
//            if (!TextUtils.isEmpty(mVideoPath)) {
//                intent = new Intent(this, LittleVideoPlayActivity.class);
//                intent.putExtra("video_path", mVideoPath);
//                intent.putExtra("is_edit", true);
//            } else {
////                intent = new Intent(this, RecordVideoActivity.class); //javacv录制
//                intent = new Intent(this, VideoRecordActivity.class);//系统录制
//            }
            startActivity(intent);
            return;
        }

        if (mMediaType == TYPE_AUDIO) {
//            if (!TextUtils.isEmpty(mAudioPath)) {
//                intent = new Intent(this, ListenRecAudioActivity.class);
//                intent.putExtra("audio_path", mAudioPath);
//                intent.putExtra("is_edit", true);
//            } else {
//                intent = new Intent(this, AudioRecordActivity.class);
//            }
            startActivity(intent);
        }
    }

    private void goToPickPhoto(int type) {
        if (type == 1) {
            AndPermission.with(this)
                    .requestCode(100)
                    .permission(Manifest.permission.CAMERA)
                    .send();
        } else if (type == -1) {
            PhotoPickerIntent intent = new PhotoPickerIntent(this);
            intent.setPhotoCount(PICTURE_MAX - mSelectedPhotos.size());
            intent.setShowCamera(true);
            intent.setShowGif(false);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    @Override
    public void onSucceed(int requestCode) {
        if (requestCode == 100) {
            captureManager = new ImageCaptureManager(this);
            Intent captureIntent = null;
            try {
                captureIntent = captureManager.dispatchTakePictureIntent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivityForResult(captureIntent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onFailed(int requestCode) {
        if (requestCode == 100) {
            ToastUtils.showToast(getString(R.string.string_run_camera_failed));
        }
    }

    @Override
    public void onEventMain(EventDispatchManager.MyEvent event) {
        super.onEventMain(event);
        if (event.eventType == Dictionary.EventType.TYPE_AUDIO_RECORD_OK) {
            mAudioPath = (String) event.data;
            showMediaIcon();
        }

        if (event.eventType == Dictionary.EventType.TYPE_VIDEO_RECORD_OK) {
            mVideoPath = (String) event.data;
            showMediaIcon();
        }

        if (event.eventType == Dictionary.EventType.TYPE_VIDEO_RECORD_DELETE) {
            if ((boolean) event.data) {
                mVideoPath = null;
                mMediaType = TYPE_PICTURE;
                showMediaIcon();
            }
        }
        if (event.eventType == Dictionary.EventType.TYPE_AUDIO_RECORD_DELETE) {
            if ((boolean) event.data) {
                mAudioPath = null;
                mMediaType = TYPE_PICTURE;
                showMediaIcon();
            }
        }

        if (event.eventType == Dictionary.EventType.TYPE_SELECT_PICTURE_OK
                || event.eventType == Dictionary.EventType.TYPE_BROWSE_PICTURE_OK) {
            List<String> photos = (List<String>) event.data;
            if (photos != null) {
                if (event.eventType == Dictionary.EventType.TYPE_BROWSE_PICTURE_OK) {
                    mSelectedPhotos.clear();
                }
                mSelectedPhotos.addAll(photos);
            }
            apvAddPic.setPaths(mSelectedPhotos);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO) {
                captureManager.galleryAddPic();
                mSelectedPhotos.add(captureManager.getCurrentPhotoPath());
                apvAddPic.setPaths(mSelectedPhotos);
            }
            if (requestCode == 100) {
                List<String> p = data.getStringArrayListExtra("images_path");
                if (p != null && p.size() > 0) {
                    mSelectedPhotos.addAll(p);
                    apvAddPic.setPaths(mSelectedPhotos);
                }
            }
        }

    }

    private void showPopwindow(boolean showVideo) {
//        if (mPopupwindow != null && !mPopupwindow.isShowing()) {
//            mPopupwindow.showAtLocation(llRoot, Gravity.BOTTOM, 0, 0);
//        } else {
        View mPopwindowView = View.inflate(this, R.layout.view_select_media_type, null);
        mPopupwindow = PopupWindowUtils.createPopWindowFromBottom(llRoot, mPopwindowView);
        mPopwindowView.findViewById(R.id.tv_capture_photo).setOnClickListener(this);
        mPopwindowView.findViewById(R.id.tv_pick_photo).setOnClickListener(this);
        mPopwindowView.findViewById(R.id.tv_record_audio).setOnClickListener(this);
        mPopwindowView.findViewById(R.id.tv_record_video).setOnClickListener(this);
        if (!showVideo) {
            mPopwindowView.findViewById(R.id.tv_record_video).setVisibility(View.GONE);
        }
//        }
    }

    private void showMediaIcon() {
        switch (Math.abs(mMediaType)) {
            case TYPE_NONE:
                apvAddPic.setVisibility(View.GONE);
                ivAddVideoAudio.setVisibility(View.GONE);
                mIvAddVideoPlay.setVisibility(View.GONE);
                break;
            case TYPE_PICTURE:
                apvAddPic.setVisibility(View.VISIBLE);
                ivAddVideoAudio.setVisibility(View.GONE);
                mIvAddVideoPlay.setVisibility(View.GONE);
                if (mSelectedPhotos == null) {
                    mSelectedPhotos = new ArrayList<>();
                }
                apvAddPic.setPaths(mSelectedPhotos);
                break;
            case TYPE_VIDEO:

                if (!TextUtils.isEmpty(mVideoPath) && new File(mVideoPath).exists()) {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mVideoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, CommonUtils.dpTopx(this, 60), CommonUtils.dpTopx(this, 60), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    if (bitmap != null) {
                        ivAddVideoAudio.setImageBitmap(bitmap);
                        mIvAddVideoPlay.setImageResource(R.drawable.ic_circle_start);
                        mIvAddVideoPlay.setVisibility(View.VISIBLE);
                    } else {
                        mIvAddVideoPlay.setVisibility(View.GONE);
                        ivAddVideoAudio.setImageResource(R.drawable.ic_add_pic);
                    }
                } else {
                    mIvAddVideoPlay.setVisibility(View.GONE);
                    ivAddVideoAudio.setImageResource(R.drawable.ic_add_pic);
                }
                apvAddPic.setVisibility(View.GONE);
                ivAddVideoAudio.setVisibility(View.VISIBLE);
                break;
            case TYPE_AUDIO:
                apvAddPic.setVisibility(View.GONE);
                mIvAddVideoPlay.setVisibility(View.GONE);
                ivAddVideoAudio.setVisibility(View.VISIBLE);
                ivAddVideoAudio.setImageResource(R.drawable.ic_audio_record);
                break;
        }
    }


    static class MyHandler extends HandlerThread implements Handler.Callback {
        private WeakReference<PublishActivity> mActivity;

        public MyHandler(String name, PublishActivity activity) {
            super(name);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public boolean handleMessage(Message msg) {
            PublishActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        ViewUtils.showLoadingDialog(activity, activity.getString(R.string.string_publishing_topic));
                        break;
                    case 2:
                        activity.compressUploadImages();
                        break;
                    case 3:
                        //发表
                        break;
                    case 4:
                        ViewUtils.closeLoadingDialog();
                        break;
                }
            }
            return true;
        }
    }

    /**
     * 循环压缩图片
     */
    private void compressUploadImages() {
        List<String> newImages = ImageUtils.compressImageList(mSelectedPhotos);
        if (newImages.size() > 0) {
            mSelectedPhotos.clear();
            mSelectedPhotos.addAll(newImages);
            mHandler.sendEmptyMessage(3);
        } else {
            mHandler.sendEmptyMessage(4);
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
