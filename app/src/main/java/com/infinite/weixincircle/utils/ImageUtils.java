package com.infinite.weixincircle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {


    /**
     * 推荐用这个方式
     *
     * @param imagePath
     * @param saveFile
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap compressImage(String imagePath, String saveFile, int maxWidth, int maxHeight) {
        return compressImage(imagePath, saveFile, maxWidth, maxHeight, 100, true);
    }


    /**
     * 循环压缩图片
     *
     * @param mPics
     * @return
     */
    public static List<String> compressImageList(List<String> mPics) {
        String filePrefix;
        String fileSuffix;
        String newFile;
        List<String> newImages = new ArrayList<>();
        for (String path : mPics) {
            if (!TextUtils.isEmpty(path)) {
                filePrefix = path.substring(0, path.lastIndexOf("."));
                fileSuffix = path.substring(path.lastIndexOf(".") + 1);
                newFile = filePrefix + "_temp." + fileSuffix;
                ImageUtils.compressImage(path, newFile, 1200, 1200);
                if (new File(newFile).exists()) {
                    newImages.add(newFile);
                }
            }
        }
        return newImages;
    }


    /**
     * 压缩图片
     *
     * @param imagePath    图片路径
     * @param saveFile     保存路径
     * @param maxHeight    最大高度
     * @param maxWidth     最大宽度
     * @param quality      压缩质量
     * @param adjustRotate 是否矫正方向
     * @return
     */
    public static Bitmap compressImage(String imagePath, String saveFile, int maxWidth, int maxHeight, int quality, boolean adjustRotate) {
        long start = System.currentTimeMillis();
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
        int actualHeight = options.outHeight;  //图片实际高
        int actualWidth = options.outWidth;     //图片实际宽

        float imgRatio = actualWidth * 1.0f / actualHeight;
        float maxRatio = maxWidth * 1.0f / maxHeight;

//     按比例计算最终宽高
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight * 1.0f / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth * 1.0f / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = maxWidth;
            } else {
                actualHeight = maxHeight;
                actualWidth = maxWidth;
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options.outHeight, options.outWidth, actualWidth, actualHeight);

        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
//        Paint提供了FILTER_BITMAP_FLAG标示，这样的话在处理bitmap缩放的时候，就可以达到双缓冲的效果，图片处理的过程就更加顺畅了
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      矫正图片的方向
        if (adjustRotate) {
            ExifInterface exif;
            try {
                exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(saveFile);
            if (quality > 100 || quality < 0) {
                quality = 100;
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return scaledBitmap;

    }


    /**
     * 计算出缩放比
     *
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(int actHeight, int actWidth, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (actHeight > reqHeight || actWidth > reqWidth) {
            final int heightRatio = Math.round((float) actHeight / (float) reqHeight);
            final int widthRatio = Math.round((float) actWidth / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = actWidth * actHeight;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    /**
     * 从uri解析图片
     */
    public static Bitmap decodeUriAsBitmap(Context ctx, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 获取图片的宽高
     *
     * @param imagePath
     * @return
     */
    public static int[] getBitmapWH(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    /**
     * 屏幕截取任意View
     *
     * @param view
     * @param filePath
     * @return
     */
    public static Bitmap getViewShot(View view, String filePath, String fileName) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        storeBitmapToFile(filePath, fileName, bitmap);
        return bitmap;
    }


    public static boolean savePicture(Bitmap bitmap, String path, String fileName) {
        if (bitmap == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(fileName)) {
            return false;
        }

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (fileDir.exists()) {
            String filePath = path + File.separator + fileName;
            return ImageUtils.storeBitmapToFile(filePath, bitmap);
        }
        return false;
    }

    /**
     * 将bitmap保存到本地
     *
     * @param filePath
     * @param bitmap
     */
    public static boolean storeBitmapToFile(String filePath, String fileName, Bitmap bitmap) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        String fileAll = null;
        File file = new File(filePath);
        if (!TextUtils.isEmpty(fileName)) {
            if (!file.exists()) {
                file.mkdirs();
            }
            fileAll = filePath + File.separator + fileName;
        } else {
            if (filePath.contains("/")) {
                String t = filePath.substring(0, filePath.lastIndexOf("/"));
                if (!TextUtils.isEmpty(t)) {
                    File path = new File(t);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    fileAll = filePath;
                }
            }
        }
        return !TextUtils.isEmpty(fileAll) && outputFile(fileAll, bitmap);
    }

    private static boolean outputFile(String filePath, Bitmap bitmap) {
        FileOutputStream out = null;
        if (bitmap != null) {
            try {
                out = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (new File(filePath).exists() && new File(filePath).length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 对图片进行压缩,先进行宽高压缩，再进行质量压缩
     *
     * @param path    图片路径
     * @param width   输出宽度
     * @param height  输出高度
     * @param quality 压缩质量
     * @param size    图片最大size，单位kb
     * @return
     */
    public static Bitmap compressImage(String path, int width, int height, int quality, int size) {
        //先判断图片角度
        if ((new File(path)).exists()) {
            int angle = getPicRotate(path);
            //按比例进行大小压缩
            Bitmap image = compressImage(path, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int ratio = baos.toByteArray().length / 128 / size;
//            Log.e("baos.length:", String.valueOf(baos.toByteArray().length));
//            Log.e("compressImage----ratio:", String.valueOf(ratio));`
            if (baos.toByteArray().length / 128 > size) {
//                Log.e("baos.length:", String.valueOf(baos.toByteArray().length));
                image.compress(Bitmap.CompressFormat.JPEG, quality / ratio, baos);  //再次压缩
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
                try {
                    baos.close();
                    isBm.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //写到本地，
                File file = convertBitmapToFile(bitmap, Environment.getExternalStorageDirectory().getPath() + File.separator + "temp", "~new_img.jpg", 100);
//            //按需旋转压缩后的本地图片
                Bitmap new_bitmap = reviewPicRotate(BitmapFactory.decodeFile(file.getAbsolutePath()), file.getAbsolutePath(), angle);
                return new_bitmap;
            }
            //写到本地，
            File file = convertBitmapToFile(image, Environment.getExternalStorageDirectory().getPath() + File.separator + "temp", "~new_img.jpg", 100);
//        //按需旋转压缩后的本地图片
            Bitmap new_bitmap = reviewPicRotate(BitmapFactory.decodeFile(file.getAbsolutePath()), file.getAbsolutePath(), angle);
            return new_bitmap;
        }
        return null;
    }


    /**
     * 保存图片到File
     *
     * @param bm
     * @param path
     * @param fileName
     * @param quality
     * @return
     */
    public static File convertBitmapToFile(Bitmap bm, String path, String fileName, int quality) {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bm.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @param path
     * @return
     */
    public static Bitmap reviewPicRotate(Bitmap bitmap, String path) {
        int degree = getPicRotate(path);
        if (degree != 0) {
            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(degree); // 旋转angle度
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
        }
        return bitmap;
    }

    public static Bitmap reviewPicRotate(Bitmap bitmap, String path, int angle) {
        if (angle != 0) {
            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(angle); // 旋转angle度
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
        }
        return bitmap;
    }

    /**
     * 读取图片文件旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片旋转的角度
     */
    public static int getPicRotate(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.i("getPicRotate--------", String.valueOf(degree));
        return degree;
    }

    /**
     * 图片宽高压缩
     *
     * @param filePath
     * @param width
     * @param height
     * @return
     */
    public static Bitmap compressImage(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options.outHeight, options.outWidth, width, height);
//        Log.e("compressImage---ratio",String.valueOf(options.inSampleSize));
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    //屏幕截取任意View
    public static Bitmap getViewShot(View view, String file_path) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        storeBitmapToFile(file_path, bitmap);
        return bitmap;
    }


    /**
     * 将bitmap保存到本地
     *
     * @param file_path
     * @param bitmap
     */
    public static boolean storeBitmapToFile(@NonNull String file_path, Bitmap bitmap) {
        return storeBitmapToFile(file_path, null, bitmap);
    }
}
