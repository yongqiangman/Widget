/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.wdiget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 圆形ImageView
 */
public class CircleImageView extends AppCompatImageView {
    private Bitmap localBitmap; //存储计算好的圆形图标,否则每次计算会占用大量内存

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        //空值判断，必要步骤，避免由于没有设置src导致的异常错误
        if (drawable == null) {
            return;
        }
        //必要步骤，避免由于初始化之前导致的异常错误
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (!(drawable instanceof BitmapDrawable)) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        if (null == b) {
            return;
        }
        if (b == localBitmap) { //即图片信息没有变化则直接绘制存储的图片即可，内存优化部分
            canvas.drawBitmap(localBitmap, 0, 0, null);
            return;
        }
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
        int w = getWidth();
        Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
        localBitmap = roundBitmap;
        setImageBitmap(localBitmap);
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
        Bitmap sourceBitmap;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            bmp = getTrimBitmap(bmp, radius, radius);
            sourceBitmap = Bitmap.createScaledBitmap(bmp, radius, radius, false);
            /* 这个方法很酷炫，你可以忽略bmp的大小，然后给定后面的width和height系统就会返回你想要的大小的bitmap
             * 底层是通过对图片进行拉伸处理，因此如果原图和目标图形比例差距过大容易导致图片变形。
             * 注意：它不会裁剪只是拉伸,因此我们使用getTrimBitmqap进行了裁剪
             * */
        } else {
            sourceBitmap = bmp;
        }
        Bitmap output = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());//sbmp是预期大小的Bitmap

        paint.setAntiAlias(true); //消除锯齿
        paint.setFilterBitmap(true); //对位图进行滤波处理

        paint.setDither(true);
        paint.setColor(Color.parseColor("#BAB399")); //画笔的颜色

        canvas.drawARGB(0, 0, 0, 0);//alpha无色透明
        canvas.drawCircle(sourceBitmap.getWidth() / 2 + 0.7f,
                sourceBitmap.getHeight() / 2 + 0.7f, sourceBitmap.getWidth() / 2 + 0.1f, paint);//x、y坐标 半径 画笔

        //核心部分，设置两张图片的相交模式，在这里就是上面绘制的Circle和下面绘制的Bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//混合模式
        canvas.drawBitmap(sourceBitmap, rect, rect, paint);
        return output;
    }

    /**
     * 对bitmap进行裁剪操作
     */
    private Bitmap getTrimBitmap(Bitmap bitmap, int width, int height) {
        int bt_height = bitmap.getHeight();
        int bt_width = bitmap.getWidth();
        int bt_widthStart = 0;
        int bt_heightStart = 0;
        float scale = (float) width / height;
        float scaleTmp = (float) bt_width / bt_height;
        if (scaleTmp > scale) {
            int tmp = (int) (scale * bt_height);
            bt_widthStart = (bt_width - tmp) / 2;
            bt_width = tmp;
        } else {
            int tmp = (int) (bt_width / scale);
            bt_heightStart = (bt_height - tmp) / 2;
            bt_height = tmp;
        }
        bitmap = Bitmap.createBitmap(bitmap, bt_widthStart, bt_heightStart, bt_width, bt_height);
        return bitmap;
    }
}