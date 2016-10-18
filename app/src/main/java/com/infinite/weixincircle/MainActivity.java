package com.infinite.weixincircle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.infinite.weixincircle.utils.ToastUtils;
import com.infinite.weixincircle.widget.CustomDialogClickListener;
import com.infinite.weixincircle.widget.MyDialog;
import com.infinite.weixincircle.widget.photopicker.utils.ImageCaptureManager;
import com.infinite.weixincircle.widget.photopicker.utils.PhotoPickerIntent;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PermissionListener {

    @BindView(R.id.bt_publish)
    Button mBtPublish;
    @BindView(R.id.ll_root)
    LinearLayout mLlRoot;
    private MyDialog mMediaDialog;
    private ImageCaptureManager captureManagerl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mBtPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMediaPicker();
            }
        });
    }

    private void showMediaPicker() {
        if (mMediaDialog == null) {
            mMediaDialog = new MyDialog("", "", new String[]{getString(R.string.string_capture_photo),
                    getString(R.string.string_select_photo),
                    getString(R.string.string_recored_video)}, null, null, true,
                    new CustomDialogClickListener() {
                        @Override
                        public void onItemClick(DialogInterface dialog, int which) {
                            super.onItemClick(dialog, which);
                            Intent intent = null;
                            switch (which) {
                                case 0:
                                    AndPermission.with(MainActivity.this)
                                            .requestCode(100)
                                            .permission(Manifest.permission.CAMERA)
                                            .send();
                                    break;
                                case 1:
                                    PhotoPickerIntent photoPickerIntent = new PhotoPickerIntent(MainActivity.this);
                                    photoPickerIntent.setPhotoCount(9);
                                    photoPickerIntent.setShowCamera(true);
                                    photoPickerIntent.setShowGif(false);
                                    photoPickerIntent.putExtra("from", 11);
                                    startActivity(photoPickerIntent);
                                    break;
                                case 2:

                                    //系统录制
//                                    intent = new Intent(MainActivity.this, VideoRecordActivity.class);
//                                    intent.putExtra("from", 12);
//                                    startActivity(intent);
                                    break;
                                case 3:
//                                    intent = new Intent(getActivity(), AudioRecordActivity.class);
//                                    intent.putExtra("from", 13);
//                                    startActivity(intent);
                                    break;
                            }
                        }
                    });
        }
        mMediaDialog.show(getSupportFragmentManager(), "mMediaDialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    @Override
    public void onSucceed(int requestCode) {
        if (requestCode == 100) {
            try {
                Intent captureIntent = null;
                if (captureManagerl == null) {
                    captureManagerl = new ImageCaptureManager(this);
                }
                captureIntent = captureManagerl.dispatchTakePictureIntent();
                startActivityForResult(captureIntent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailed(int requestCode) {
        if (requestCode == 100) {
            ToastUtils.showToast(getString(R.string.string_run_camera_failed));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            captureManagerl.galleryAddPic();
            String path = captureManagerl.getCurrentPhotoPath();
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                ArrayList<String> url = new ArrayList<>();
                url.add(path);
                Intent intent = new Intent(this, PublishActivity.class);
                intent.putExtra("images_path", url);
                intent.putExtra(PublishActivity.PUBLISH_MEDIA_TYPE, 1);
                startActivity(intent);
            } else {
                ToastUtils.showToast(getString(R.string.string_capture_photo_failed));
            }
        }
    }
}
