package com.yqman.wdiget.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;

public class DrawableFactory {
    private static final String TAG = "DrawableFactory";
    private static final boolean DEBUG = true;
    private Resources resources;
    private Context context;
    private int screen_width;
    private int screen_height;

    private DrawableFactory(Context context) {
        this.context = context;
        resources = context.getResources();
        Point outSize = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(outSize);
        screen_height = outSize.y;
        screen_width = outSize.x;

    }

    /**
     * 以屏幕宽度为基准，压缩sample倍得到Bitmap
     *
     * @param resID  资源ID
     * @param sample 压缩比例 如果压缩比例为2 原始图片高度宽度为200*400 最后得到的图片是一个100*200的图片
     *
     * @return Bitmap值
     */
    public Bitmap getBitmapByScreenWidth(int resID, int sample) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resID, options);
        options.inSampleSize = calculateInSampleSize(options, screen_width / sample, screen_width / sample);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resID, options);
    }

    public Bitmap getBitmapByScreenWidth(String imgPath, int sample) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        options.inSampleSize = calculateInSampleSize(options, screen_width / sample, screen_width / sample);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgPath, options);
    }

    /**
     * @param bytes  需要解码bytes
     * @param sample 值如果小于1那么证明是不需要压缩的
     */
    public Bitmap getBitmapByScreenWidth(byte[] bytes, int sample) {
        if (sample < 1) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            options.inSampleSize = calculateInSampleSize(options, screen_width / sample, screen_width / sample);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
    }

    /**
     * 根据预期的宽度和高度 计算需要压缩多少倍
     *
     * @param options   处理对象
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     *
     * @return 压缩比
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (DEBUG) {
            Log.d(TAG, "Width = " + width);
        }
        if (DEBUG) {
            Log.d(TAG, "Height = " + height);
        }
        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
                if (DEBUG) {
                    Log.d(TAG, "inSampleSize = " + inSampleSize);
                }
            }
        }
        return inSampleSize;
    }

    /**
     * 根据指定的宽度和高度 对目标图片进行压缩处理，保持原图片比例不变
     */
    public Bitmap getBitmapKeepScale(int resID, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resID, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resID, options);
    }

    /**
     * 返回的是指定的width和height比例
     */
    public Bitmap getBitmap(int resID, int width, int height) {
        Bitmap bitmap = getBitmapByScreenWidth(resID, 1);
        return getTrimBitmap(bitmap, width, height);
    }

    public Bitmap getBitmap(String filePath, int width, int height) {
        Bitmap bitmap = getBitmap(filePath, width, height);
        return getTrimBitmap(bitmap, width, height);
    }

    /**
     * 根据指定width和height对Bitmap进行一次裁剪，使其不至于被拉伸后变形
     */
    private Bitmap getTrimBitmap(Bitmap bitmap, int width, int height) {
        int bt_height = bitmap.getHeight();
        int bt_width = bitmap.getWidth();
        int bt_widthStart = 0;
        int bt_heightStart = 0;
        float scale = (float) width / height;
        float scaleTmp = (float) bt_width / bt_height;
        if (scaleTmp > scale) {  //Bitmap的宽度超标，需要裁剪，取中间部分
            int tmp = (int) (scale * bt_height);
            bt_widthStart = (bt_width - tmp) / 2;
            bt_width = tmp;
        } else { //Bitmap的高度超标，需要裁剪，取中间部分
            int tmp = (int) (bt_width / scale);
            bt_heightStart = (bt_height - tmp) / 2;
            bt_height = tmp;
        }
        bitmap = Bitmap.createBitmap(bitmap, bt_widthStart, bt_heightStart, bt_width, bt_height);
        return bitmap;
    }

    /**
     * 初始Bitmap对象的缩放裁剪过程
     *
     * @param bmp    初始Bitmap对象
     * @param radius 圆形图片直径大小
     *
     * @return 返回一个圆形的缩放裁剪过后的Bitmap对象
     */
    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap tmpBitmap;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            bmp = getTrimBitmap(bmp, radius, radius);
            tmpBitmap = Bitmap.createScaledBitmap(bmp, radius, radius, false);
            /*
             * 这个方法很酷炫，你可以忽略bmp的大小，然后给定后面的width和height系统就会返回你想要的大小的bitmap
             * 底层是通过对图片进行拉伸处理，因此如果原图和目标图形比例差距过大容易导致图片变形。
             * 注意：它不会裁剪只是拉伸,因此我们使用getTrimBitmap方法进行了预先的裁剪
             */
        } else {
            tmpBitmap = bmp;
        }
        Bitmap output = Bitmap.createBitmap(tmpBitmap.getWidth(), tmpBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight());//tmpBitmap是预期大小的Bitmap

        paint.setAntiAlias(true); //消除锯齿
        paint.setFilterBitmap(true); //对位图进行滤波处理

        paint.setDither(true);
        paint.setColor(Color.parseColor("#BAB399")); //画笔的颜色

        canvas.drawARGB(0, 0, 0, 0);//alpha无色透明
        canvas.drawCircle(tmpBitmap.getWidth() / 2 + 0.7f,
                tmpBitmap.getHeight() / 2 + 0.7f, tmpBitmap.getWidth() / 2 + 0.1f, paint);//x、y坐标 半径 画笔

        //核心部分，设置两张图片的相交模式，在这里就是上面绘制的Circle和下面绘制的Bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//混合模式
        canvas.drawBitmap(tmpBitmap, rect, rect, paint);
        return output;
    }

    /**
     * @param bmp 操作的bitmap目标文件
     *
     * @return 返回当前bitmap的对应filePath得到的Uri
     */
    public Uri getLocalBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(context.getCacheDir(),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}