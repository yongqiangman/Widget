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

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义圆盘时钟
 */
public class ClockView extends View implements Runnable {
    private int mWidth = 0;
    private int mHeight = 0;
    private Bitmap mClockBitmap;
    private Paint mPaint;
    private int mPointLength = 0;
    private int mClockBackground = -1;
    private int mClockColor = Color.BLUE;
    private static Handler HANDLER = new Handler(Looper.getMainLooper());
    private float mScale = 1; //针对200dp的设定

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        mPaint.setColor(mClockColor);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);

        calendar.setTimeInMillis(System.currentTimeMillis());
        hourAngle = 30 * calendar.get(Calendar.HOUR);
        minuteAngle = 6 * calendar.get(Calendar.MINUTE);
        secondAngle = 6 * calendar.get(Calendar.SECOND);
        HANDLER.postDelayed(this, 500);
    }

    public int getClockBackgroud() {
        return mClockBackground;
    }

    public void setClockBackgroud(int clockBackground) {
        this.mClockBackground = clockBackground;
    }

    public int getClockColor() {
        return mClockColor;
    }

    public void setClockColor(int clockColor) {
        this.mClockColor = clockColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWidth != getWidth() || mHeight != getHeight()) { //创建表盘
            mWidth = getWidth();
            mHeight = getHeight();

            //1、创建Bitmap作为表盘
            mClockBitmap = Bitmap.createBitmap(mWidth, mHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas btCanvas = new Canvas(mClockBitmap);
            //2、画笔初始化
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            //图形绘制
            btCanvas.drawARGB(0, 0, 0, 0);//alpha无色透明

            //3、绘制圆圈
            btCanvas.translate(mWidth / 2, mHeight / 2);
            //将btCanvas的0，0。坐标映射到width / 2, height / 2位置；
            //即原始绘制起点是0,0；现在变成width / 2, height / 2

            int radius_base = mWidth > mHeight ? mHeight / 2 : mWidth / 2;
            mScale = radius_base / 200f;
            radius_base = radius_base / 10;//绘制半径分成10份,1份距离、二份是刻度、剩下全是半径
            mPointLength = radius_base * 5;
            btCanvas.drawCircle(0, 0, radius_base * 7, mPaint);

            //4、绘制圈内的商标
            btCanvas.save();
            int movePosition = radius_base * 6;
            btCanvas.translate(-movePosition, -movePosition);
            Path path = new Path();
            path.addArc(new RectF(0, 0, movePosition * 2, movePosition * 2), -140, 140);
            //这个角度-125需要根据后面显示内容的长度需要调节
            Paint citePaint = new Paint(mPaint);
            citePaint.setTextSize(17 * mScale);
            citePaint.setStrokeWidth(1);
            btCanvas.drawTextOnPath("http://blog.csdn.cn/evan_man", path, 0, 0, citePaint);
            btCanvas.restore();

            //5、绘制刻度
            Paint tmpPaint = new Paint(mPaint); //小刻度画笔对象
            tmpPaint.setStrokeWidth(1 * mScale);
            tmpPaint.setTextSize(12 * mScale);
            float y = radius_base * 7;
            int count = 60; //总刻度数
            for (int i = 0; i < count; i++) {
                if (i % 5 == 0) {
                    btCanvas.drawLine(0f, -y, 0, -(y + 12f * mScale), mPaint);
                    if (i / 5 == 0) {
                        btCanvas.drawText("12", -4f * mScale, -(y + 25f * mScale), tmpPaint);
                    } else {
                        btCanvas.drawText(String.valueOf(i / 5), -4f * mScale, -(y + 25f * mScale), tmpPaint);
                    }
                } else {
                    btCanvas.drawLine(0f, -y, 0f, -(y + 5f * mScale), tmpPaint);
                }
                btCanvas.rotate(360 / count, 0f, 0f); //旋转画纸
            }
        }
        if (mClockBackground != -1) {
            canvas.drawColor(mClockBackground);
        }
        canvas.drawBitmap(mClockBitmap, 0, 0, null); //绘制表盘

        Paint tmpPaint = new Paint(mPaint);
        tmpPaint.setTextSize(17 * mScale);
        tmpPaint.setStrokeWidth(1);
        canvas.drawText(timeString, 10, mHeight - 5, tmpPaint);
        canvas.translate(mWidth / 2, mHeight / 2);
        //hour
        canvas.drawLine(0, 0, mPointLength * getSinFromAngle(hourAngle) * 0.5f,
                -mPointLength * getCosFromAngle(hourAngle) * 0.5f, mPaint);
        //minute
        canvas.drawLine(0, 0, mPointLength * getSinFromAngle(minuteAngle) * 0.8f,
                -mPointLength * getCosFromAngle(minuteAngle) * 0.8f, mPaint);
        //seconds
        canvas.drawLine(0, 0, mPointLength * getSinFromAngle(secondAngle), -mPointLength * getCosFromAngle(secondAngle),
                mPaint);
    }

    private double baseRadian = (2 * Math.PI) / 360;

    private float getCosFromAngle(double angle) {
        return  (float) Math.cos(baseRadian * angle);

    }

    private float getSinFromAngle(double angle) {
        return  (float) Math.sin(baseRadian * angle);
    }

    private Calendar calendar = Calendar.getInstance();
    private double hourAngle = 0;
    private double minuteAngle = 0;
    private double secondAngle = 0;
    private String timeString = "";

    @Override
    public void run() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        secondAngle = 6 * calendar.get(Calendar.SECOND);
        minuteAngle = 6 * calendar.get(Calendar.MINUTE) + (secondAngle / 360) * 6;
        hourAngle = 30 * calendar.get(Calendar.HOUR) + (minuteAngle / 360) * 30;
        timeString = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM " : "PM ";
        timeString = timeString + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar
                .get(Calendar.SECOND);
        invalidate();
        HANDLER.postDelayed(this, 500);
    }

}