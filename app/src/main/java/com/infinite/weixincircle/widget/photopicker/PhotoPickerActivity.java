package com.infinite.weixincircle.widget.photopicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;


import com.infinite.weixincircle.PublishActivity;
import com.infinite.weixincircle.R;
import com.infinite.weixincircle.base.BaseTitleActivity;
import com.infinite.weixincircle.config.Dictionary;
import com.infinite.weixincircle.utils.EventDispatchManager;
import com.infinite.weixincircle.utils.ToastUtils;
import com.infinite.weixincircle.widget.CustomDialogClickListener;
import com.infinite.weixincircle.widget.MyAppBar;
import com.infinite.weixincircle.widget.MyDialog;
import com.infinite.weixincircle.widget.photopicker.entity.Photo;
import com.infinite.weixincircle.widget.photopicker.event.OnItemCheckListener;
import com.infinite.weixincircle.widget.photopicker.fragment.BrowseFragment;
import com.infinite.weixincircle.widget.photopicker.fragment.PhotoPickerFragment;

import java.util.List;

public class PhotoPickerActivity extends BaseTitleActivity {

    private PhotoPickerFragment mPickerFragment;
    private BrowseFragment mBrowseFragment;

    public final static String EXTRA_MAX_COUNT = "MAX_COUNT";
    public final static String EXTRA_SHOW_CAMERA = "SHOW_CAMERA";
    public final static String EXTRA_SHOW_GIF = "SHOW_GIF";
    public final static String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";

    private MenuItem menuDoneItem;

    public final static int DEFAULT_MAX_COUNT = 9;

    private int mMaxCount = DEFAULT_MAX_COUNT;


    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private int mFrom = 0;


    @Override
    protected void findViews() {
        super.findViews();
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        setShowGif(showGif);

        mMaxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        mFrom = getIntent().getIntExtra("from", 0);
        mPickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPickerFragment);

        mPickerFragment.getPhotoGridAdapter().setShowCamera(showCamera);

        mPickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

                int total = selectedItemCount + (isCheck ? -1 : 1);
                menuDoneItem.setEnabled(total > 0);
                if (mMaxCount <= 1) {
                    List<Photo> photos = mPickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo)) {
                        photos.clear();
                        mPickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (total > mMaxCount) {
                    ToastUtils.showToast(getString(R.string.string_over_max_count_tips, mMaxCount));
                    return false;
                }
                menuDoneItem.setTitle(getString(R.string.string_done_with_count, total, mMaxCount));
                return true;
            }
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo_picker;
    }


    @Override
    public void onBackPressed() {
        if (mBrowseFragment != null && mBrowseFragment.isVisible()) {
//      mBrowseFragment.runExitAnimation(new Runnable() {
//        public void run() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
//        }
//      });
        } else {
            List<Photo> list = mPickerFragment.getPhotoGridAdapter().getSelectedPhotos();
            if (list != null && list.size() > 0) {
                MyDialog dialog = new MyDialog("提示", "确定放弃已经选择的图片吗？", "确定", "取消", true, new CustomDialogClickListener() {
                    @Override
                    public void onPostiveClick(DialogInterface dialog, int which) {
                        super.onPostiveClick(dialog, which);
                        PhotoPickerActivity.super.onBackPressed();
                    }
                });
                dialog.show(getSupportFragmentManager(), "dialog");
            } else {
                super.onBackPressed();
            }
        }
    }


    public void addImagePagerFragment(BrowseFragment imagePagerFragment) {
        this.mBrowseFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.mBrowseFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            menuDoneItem.setEnabled(false);
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done) {
            if (mFrom == 11) {
                Intent intent = new Intent(this, PublishActivity.class);
                intent.putExtra(PublishActivity.PUBLISH_MEDIA_TYPE, -1);
                intent.putExtra("images_path", mPickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths());
                startActivity(intent);
            } else {
                EventDispatchManager.getInstance()
                        .dispatchEvent(new EventDispatchManager.MyEvent(Dictionary.EventType.TYPE_SELECT_PICTURE_OK,
                                mPickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths()));
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }

    @Override
    public MyAppBar.TitleConfig getTitleViewConfig() {
        return buildDefaultConfig(getString(R.string.string_select_photo_title));
    }
}
