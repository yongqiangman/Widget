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

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 使用该Dialog需要注意Activity不应该有如下配置，否者横竖屏切换过程中容易导致控件显示异常，因为setMeasuredDimension(width, height);无效
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */

public class ScrollPickView extends View {
    private static final String TAG = "ScrollPickView";
    private static final int DEFAULT_OFFSET = 2;
    private static final int AFTER_ANIMATION_TIME_GAP = 10; // 动画10ms刷新一次
    private static final float ITEM_HEIGHT_DP = 35;
    // 相对于itemHeight对应的最大字体大小
    private static final float LARGE_TEXT_SIZE_RELATIVE_ITEM_HEIGHT = (4 / 5f);
    // 相对于LargeTextSize对应的最小字体大小
    private static final float SMALL_TEXT_SIZE_RELATIVE_LARGE_TEXT_ = (3 / 5f);
    // 最大字体长度
    private static final int MAX_FONT_LENGTH = 5;

    // onMeasure里面实时算出来的 以px为单位
    private float mTotalHeightPx;
    private float mHalfTotalHeightPx;
    private float mTotalWidthPx;
    private float mItemHeightPx;
    private float mHalfItemHeightPx;
    private float mOffsetItemHeightPx;
    private float mLargeTextSizePx;
    private float mSmallTextSizePx;


    // 横屏需要保存的数据
    // 设置ScrollSelector的颜色
    private int mSelectedItemTextColor;
    private int mNormalItemTextColor;
    // 设置ScrollSelector的背景颜色
    private int mSelectBackgroundColor;
    private int mNormalBackgroundColor;
    private int mSelectItemLineColor;

    private int mOffset;
    private int mMinValue;
    private int mMaxValue;
    private int mValueLength;
    // 是否循环的标志位
    private boolean mIsLoopFlag;
    // 当前显示的中间位置，包含了mOffset的值
    private int mCurrentPosition;
    private int mInitPosition; // 用于记录调用setValue，setValueRange后应该显示的初始值，在onMeasure方法中才会用

    // 实时更新的数据
    private float mCurrentY;
    private float mLastY;
    private double mScrollSpeed;
    float mLeftTopX;
    float mLeftTopY;
    float mRightBottomX;
    float mRightBottomY;
    private OnValueChangedListener mOnValueChangedListener;
    private SpeedCalculate mSpeedCalculate;
    private Paint mPaint;
    private AfterAnimationTask mAfterAnimationTask;
    private Timer mTimer;
    private Formatter mFormatter = new Formatter() {
        @Override
        public String getFormatString(int value) {
            return String.valueOf(value);
        }
    };
    // 处理最后的显示动画
    private LocalHandler mHandler = new LocalHandler();
    private int mLoopStateMaxPosition = 2147483;

    public ScrollPickView(Context context) {
        this(context, null);
    }

    public ScrollPickView(Context context,
                          @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollPickView(Context context,
                          @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollPickView, defStyleAttr, 0);
        mOffset = a.getInteger(R.styleable.ScrollPickView_yqman_widget_scroll_pick_item_offset, DEFAULT_OFFSET);
        mCurrentPosition = mOffset;
        mSelectedItemTextColor = a.getColor(R.styleable.ScrollPickView_yqman_widget_scroll_pick_select_item_text_color,
                getResources().getColor(R.color.yqman_widget_scroll_pick_selected_item_text));
        mNormalItemTextColor = a.getColor(R.styleable.ScrollPickView_yqman_widget_scroll_pick_normal_item_text_color,
                getResources().getColor(R.color.yqman_widget_scroll_pick_normal_item_text));
        mSelectBackgroundColor = a.getColor(R.styleable.ScrollPickView_yqman_widget_scroll_pick_select_background_color,
                getResources().getColor(R.color.yqman_widget_scroll_pick_selected_background_color));
        mNormalBackgroundColor = a.getColor(R.styleable.ScrollPickView_yqman_widget_scroll_pick_normal_background_color,
                getResources().getColor(R.color.yqman_widget_scroll_pick_normal_background_color));
        mSelectItemLineColor = a.getColor(R.styleable.ScrollPickView_yqman_widget_scroll_pick_select_item_line_color,
                getResources().getColor(R.color.yqman_widget_scroll_pick_selected_item_line_color));
        mMinValue = a.getInteger(R.styleable.ScrollPickView_yqman_widget_scroll_pick_min_value, 0);
        mMaxValue = a.getInteger(R.styleable.ScrollPickView_yqman_widget_scroll_pick_max_value, 0);
        mValueLength = mMaxValue - mMinValue + 1;
        mIsLoopFlag = a.getBoolean(R.styleable.ScrollPickView_yqman_widget_scroll_pick_enable_loop, false);
        a.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mAfterAnimationTask = new AfterAnimationTask(mHandler, this);
        mTimer = new Timer();
    }

    // 根据当前视图大小动态设定item的textSize的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredHeight = dip2px(ITEM_HEIGHT_DP) * (2 * mOffset + 1);
        int desiredWidth = dip2px(ITEM_HEIGHT_DP) * MAX_FONT_LENGTH; // 2017年 5个字
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
        if (getHeight() == 0 || getWidth() == 0 ) {
            return;
        }
        mTotalHeightPx = getHeight();
        mTotalWidthPx = getWidth();
        mHalfTotalHeightPx = mTotalHeightPx / 2f;
        mItemHeightPx =  mTotalHeightPx / (2 * mOffset + 1);
        mHalfItemHeightPx = mItemHeightPx / 2f;
        mOffsetItemHeightPx = mItemHeightPx * mOffset;
        if (mItemHeightPx * LARGE_TEXT_SIZE_RELATIVE_ITEM_HEIGHT < (mTotalWidthPx / MAX_FONT_LENGTH)) {
            mLargeTextSizePx = mItemHeightPx * LARGE_TEXT_SIZE_RELATIVE_ITEM_HEIGHT;
        } else {
            mLargeTextSizePx = (mTotalWidthPx / MAX_FONT_LENGTH);
        }
        mSmallTextSizePx = mLargeTextSizePx * SMALL_TEXT_SIZE_RELATIVE_LARGE_TEXT_;

        mLeftTopX = 0;
        mLeftTopY = mOffset * mItemHeightPx;
        mRightBottomX = mTotalWidthPx;
        mRightBottomY = mLeftTopY + mItemHeightPx;

        mLoopStateMaxPosition = (int) (Integer.MAX_VALUE / mTotalHeightPx);
        if (mInitPosition >= mOffset) {
            stopAfterAnimation();
            mCurrentY = (mInitPosition - mOffset) * mItemHeightPx; // 设置了初始值还未显示的情况
            mInitPosition = 0; // 清空该初始值
            mCurrentPosition = 0; // 是的onRestore的值无效
        } else if (mCurrentPosition >= mOffset) {
            stopAfterAnimation();
            mCurrentY = (mCurrentPosition - mOffset) * mItemHeightPx; // onRestore的情况
        }
        invalidate(); // 强刷一次，不然可能存在onDraw方法在此之前就调用了，onMeasure调用了不触发onDraw方法
    }

    /**
     * dp转化为当前系统的px
     * @param dpValue 待转化的px值
     */
    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 处理横屏的情况
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState state = new SavedState(parcelable);
        state.mSelectedItemTextColor = mSelectedItemTextColor;
        state.mNormalItemTextColor = mNormalItemTextColor;
        state.mSelectBackgroundColor = mSelectBackgroundColor;
        state.mNormalBackgroundColor = mNormalBackgroundColor;
        state.mSelectItemLineColor = mSelectItemLineColor;

        state.mOffset = mOffset;
        state.mCurrentPosition = mCurrentPosition;
        state.mMinValue = mMinValue;
        state.mMaxValue = mMaxValue;
        state.mValueLength = mValueLength;
        state.mIsLoopFlag = mIsLoopFlag;
        return state;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mSelectedItemTextColor = savedState.mSelectedItemTextColor;
        mNormalItemTextColor =  savedState.mNormalItemTextColor;
        mSelectBackgroundColor = savedState.mSelectBackgroundColor;
        mNormalBackgroundColor = savedState.mNormalBackgroundColor;
        mSelectItemLineColor = savedState.mSelectItemLineColor;

        mOffset = savedState.mOffset;
        mCurrentPosition = savedState.mCurrentPosition;
        mMinValue = savedState.mMinValue;
        mMaxValue = savedState.mMaxValue;
        mValueLength = savedState.mValueLength;
        mIsLoopFlag = savedState.mIsLoopFlag;
        stopAfterAnimation(); // 横屏切换前停止动画
        invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTotalHeightPx != 0) {
            drawBackground(canvas);
            updateView(canvas);
        }
    }

    /**
     * 根据{@link ScrollPickView#mCurrentY}更新显示样式
     */
    private void updateView(Canvas canvas) {


        if (mCurrentY < 0) {
            mCurrentY = 0;
        } else if (mCurrentY > (getPositionCount() * mItemHeightPx - mTotalHeightPx)) {
            mCurrentY = (getPositionCount() * mItemHeightPx - mTotalHeightPx);
        }
        float realY = mCurrentY  + mOffsetItemHeightPx + mHalfItemHeightPx;
        mCurrentPosition = (int) (realY / mItemHeightPx);
        for (int index = mCurrentPosition - mOffset - 1;
             index < mCurrentPosition + mOffset + 1; index++) {
            if (index < 0 || index >= getPositionCount()) {
                continue;
            }
            if (index != mCurrentPosition) {
                drawOtherValue(canvas, realY, index);
            } else {
                drawCurrentValue(canvas, realY);
            }
        }
    }

    /**
     * 画背景
     */
    private void drawBackground(Canvas canvas) {
        canvas.drawColor(mNormalBackgroundColor);
        mPaint.setStrokeWidth(2);

        mPaint.setColor(mSelectBackgroundColor);
        canvas.drawRect(mLeftTopX, mLeftTopY, mRightBottomX, mRightBottomY, mPaint); // 设置中间选中位置的背景颜色

        mPaint.setColor(mSelectItemLineColor);
        canvas.drawLine(mLeftTopX, mLeftTopY, mRightBottomX, mLeftTopY, mPaint); // 这里是画两条先线在视图中央
        canvas.drawLine(mLeftTopX, mRightBottomY, mRightBottomX, mRightBottomY, mPaint);
    }

    private void drawOtherValue(Canvas canvas,  float realY, int position) {
        double scale = getScale(realY, position);
        int alpha  = translateToAlpha(scale);
        int nextSize = (int) (mSmallTextSizePx + (mLargeTextSizePx - mSmallTextSizePx) * scale);
        mPaint.setTextSize(nextSize);
        mPaint.setColor(mNormalItemTextColor);
        mPaint.setAlpha(alpha);
        mPaint.setStrokeWidth(3);
        String text = getDisplayString(position);
        canvas.drawText(text, mTotalWidthPx / 2, getDisplayYBaseLine(realY, position), mPaint);
    }

    private void drawCurrentValue(Canvas canvas,  float realY) {
        double scale = getScale(realY, mCurrentPosition);
        int alpha  = translateToAlpha(scale);
        int nextSize = (int) (mSmallTextSizePx + (mLargeTextSizePx - mSmallTextSizePx) * scale);
        mPaint.setTextSize(nextSize);
        mPaint.setColor(mSelectedItemTextColor);
        mPaint.setAlpha(alpha);
        String text = getDisplayString(mCurrentPosition);
        canvas.drawText(text, mTotalWidthPx / 2, getDisplayYBaseLine(realY, mCurrentPosition), mPaint);
    }

    private float getDisplayYBaseLine(float realY, int position) {
        float positionY = position * mItemHeightPx;
        float absDistance = Math.abs(positionY - realY);
        if (positionY < realY) {
            // 中心线上面
            float top = mHalfTotalHeightPx - absDistance;
            float bottom = top + mItemHeightPx;
            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            return (float) ((top + bottom - fontMetrics.bottom - fontMetrics.top) / (double) 2);
        } else {
            float top = mHalfTotalHeightPx + absDistance;
            float bottom = top + mItemHeightPx;
            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            return  (float) ((top + bottom - fontMetrics.bottom - fontMetrics.top) / (double) 2);
        }
    }

    private String getDisplayString(int position) {
        if (position < mOffset) {
            return "";
        }
        if (!mIsLoopFlag && position > (getPositionCount() - mOffset - 1)) {
            return "";
        }
        int value = ((position - mOffset) % mValueLength) + mMinValue;
        return mFormatter.getFormatString(value);
    }

    /**
     * 获取大小伸缩比例
     * @return 字体大小缩小的比例 0 则最小字体； 1 则最大的字体
     */
    private double getScale(float realY, int position) {
        float y = realY - mHalfItemHeightPx;
        float upper = Math.abs(y - mItemHeightPx * position);
        if (upper > mOffsetItemHeightPx) {
            upper = mOffsetItemHeightPx;
        } if (upper < 0) {
            upper = 0;
        }
        return  1 - (upper / (mOffsetItemHeightPx + 0.1f));
    }

    /**
     * 将大小伸缩比例转换成alpha值
     * @param scale 0 ~ 1
     * @return 0.5 ~ 1
     */
    private int translateToAlpha(double scale) {
        float alpha;
        if (scale > 1) {
            alpha = 1;
        } else if (scale < 0.5) {
            alpha = 0.5f;
        } else {
            alpha = (float) scale;
        }
        return (int) (alpha * 255);
    }

    /**
     * 开启循环显示
     * 默认不循环显示
     */
    public void turnOnLoop() {
        mIsLoopFlag = true;
        invalidate();
    }

    /**
     * 关闭循环显示
     * 默认不循环显示
     */
    public void turnOffLoop() {
        mIsLoopFlag = false;
        invalidate();
    }

    /**
     * 返回用户设置的显示值信息
     * @return position @IntRange(from = minValue, to = maxValue)
     */
    public int getValue() {
        return ((mCurrentPosition - mOffset) % mValueLength) + mMinValue;
    }

    /**
     * 设置监听器
     * @param listener 位置改变回调器
     */
    public void setOnValueChangedListener(OnValueChangedListener listener) {
        mOnValueChangedListener = listener;
    }

    /**
     * 设置显示的起止位置
     * @param minValue 开始位置
     * @param maxValue 结束位置
     */
    public void setValueRange(int minValue, int maxValue) {
        if (minValue == mMinValue && maxValue == mMaxValue) {
            return;
        }
        int value = getValue(); // 在给mMinValue、和mMaxValue赋值之前调用
        mMinValue = minValue;
        mMaxValue = maxValue;
        mValueLength = mMaxValue - mMinValue + 1;
        if (value >= mMinValue && value <= mMaxValue) {
            updateCurrentYAndPosition(value);
        } else {
            if (Math.abs(value - mMinValue) < Math.abs(value - mMaxValue)) {
                updateCurrentYAndPosition(mMinValue);
            } else {
                updateCurrentYAndPosition(mMaxValue);
            }
        }
        invalidate();
    }

    /**
     * 设置初始化显示位置
     * @param  value @IntRange(from = minValue, to = maxValue)
     */
    public void setInitValue(final int value) {
        if (value >= mMinValue &&  value <= mMaxValue) {
            updateCurrentYAndPosition(value);
            invalidate();
        }
    }

    /**
     * 调用该方法前需要保证mMinValue，mMaxValue都已经设定好了，且newValue在mMinValue，mMaxValue之间
     * 根据设定的新值需改mCurrentY和mCurrentPosition值
     * @param newValue 设定的新值
     */
    private void updateCurrentYAndPosition(int newValue) {
        if (mItemHeightPx == 0) { // 还未完成onMeasure方法
            stopAfterAnimation();
            mInitPosition = newValue - mMinValue + mOffset; // 执行到此处表明onMeasure方法还没被执行，这里暂存该初始位置信息；
            mCurrentPosition = mInitPosition; // 有时候还没绘制导致无法更新mCurrentPosition值，但是又立即调用getValue导致出错
            return;
        }
        int oldValue = getValue(); // 根据当前mCurrentPosition得到的相应value值
        if (newValue == oldValue) { // 当前mCurrentPosition对应value就是预定的值，表明不需要刷新
            return;
        }
        int adjust = newValue - oldValue; // + 则往下挪 - 则往上挪
        float y = mCurrentY + adjust * mItemHeightPx;
        if (y >= 0 && y <= (getPositionCount() * mItemHeightPx - mTotalHeightPx)) { // 检查Y是否超限
            stopAfterAnimation();
            mCurrentPosition += adjust;
            mCurrentY = y;
            return;
        }
        stopAfterAnimation();
        mCurrentPosition = (newValue - mMinValue) + mOffset;
        mCurrentY = (newValue - mMinValue) * mItemHeightPx;
    }

    public void setFormatter(@NonNull Formatter formatter) {
        mFormatter = formatter;
    }

    private int getPositionCount() {
        if (mIsLoopFlag) {
            return mLoopStateMaxPosition;
        } else {
            return mValueLength + (2 * mOffset);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(event);
                break;
            case MotionEvent.ACTION_UP:
                handleUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMove(event);
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * handle user down screen action
     */
    private void handleDown(MotionEvent e) {
        mLastY = e.getY();
        mSpeedCalculate = new SpeedCalculate();
        mSpeedCalculate.startCalculate();
        stopAfterAnimation();
    }


    /**
     * handle user move screen action
     */
    private void handleMove(MotionEvent e) {
        mCurrentY += mLastY - e.getY() ;
        if (mCurrentY < 0) {
            mCurrentY = 0;
        } else if (mCurrentY > (getPositionCount() * mItemHeightPx - mTotalHeightPx)){
            mCurrentY = (getPositionCount() * mItemHeightPx - mTotalHeightPx);
        }
        mLastY = e.getY();
        invalidate();
    }

    /**
     * handle user up screen action
     */
    private void handleUp(MotionEvent e) {
        startAfterAnimation();
    }

    /**
     * startStopAnimation after usr up aciton;
     * 依据用户最后一秒的速度决定抬起手指后滑动多远的距离
     */
    private void startAfterAnimation() {
        mSpeedCalculate.stopCalculate();
        mHandler.mStopAnimation = false;
        mAfterAnimationTask = new AfterAnimationTask(mHandler, this);
        mTimer.schedule(mAfterAnimationTask, 0, AFTER_ANIMATION_TIME_GAP);
    }

    /**
     * 停止滚动动画；
     * 在View小时，restore，手点击屏幕，或者更改mCurrentY之前调用该方法，因为下一次使用心得mCurrentY进行绘制
     */
    private void stopAfterAnimation() {
        mHandler.mStopAnimation = true;
        if (mAfterAnimationTask != null) {
            mAfterAnimationTask.cancel();
        }
    }

    private class SpeedCalculate extends Thread {
        private boolean stopRecordFlag = false;
        private double currentSpeed = 0;
        private double maxSpeed = 0;
        private static final int CALCULATE_TIME_GAP = 100; // 计算时间间隔ms
        @Override
        public void run() {
            while(!stopRecordFlag) {
                try {
                    float lastY = mCurrentY;
                    sleep(CALCULATE_TIME_GAP);
                    currentSpeed = (mCurrentY - lastY) / (CALCULATE_TIME_GAP + 0.001f);
                    if (Math.abs(currentSpeed) > Math.abs(maxSpeed)) {
                        maxSpeed = currentSpeed;
                    }
                    mScrollSpeed = currentSpeed;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        /**
         * 开始记录
         */
        private void startCalculate() {
            stopRecordFlag = false;
            start();
        }
        /**
         * 停止记录
         */
        private void stopCalculate() {
            stopRecordFlag = true;
        }
    }

    private static class AfterAnimationTask extends TimerTask {
        private Handler handler;
        private WeakReference<ScrollPickView> mView;
        private boolean initFlag;

        AfterAnimationTask(Handler handler, ScrollPickView view) {
            this.handler = handler;
            mView = new WeakReference<>(view);
            initFlag = true;
        }

        @Override
        public void run() {
            if (mView.get() != null) {
                if (initFlag) {
                    initFlag =  false;
                    handler.sendMessage(handler.obtainMessage(LocalHandler.FIRST_TIME, mView.get()));
                } else {
                    handler.sendMessage(handler.obtainMessage(LocalHandler.OTHER_TIME, mView.get()));
                }
            }
        }
    }

    private static class LocalHandler extends Handler {
        private static final int FIRST_TIME = 100;
        private static final int OTHER_TIME = 101;
        // 滚动定义为高速的门限值
        private static final float HIGH_SPEED = 0.2f;
        // 减速器
        private static final float DECELERATION_SPEED = 0.1f;
        // 停止动画门限
        private static final int STOP_PIXEL_THRESHOLD = 4;
        // 停止动画每次滚动的像素
        private static final float LAST_MOVE_PIXEL = 1.5f;
        int mTargetValue;
        double mTargetY;
        boolean mIsLowSpeed;
        boolean mStopAnimation; // 动画总控制器
        @Override
        public void handleMessage(Message msg) {
            ScrollPickView scrollPickView = (ScrollPickView) msg.obj;
            if (mStopAnimation) {
                removeMessages(FIRST_TIME); // 清空消息队列
                removeMessages(OTHER_TIME);
                return;
            }
            if (msg.what == FIRST_TIME) {
                mTargetY = 0;
                mIsLowSpeed = false;
            }

            double speed = scrollPickView.mScrollSpeed;
            if (scrollPickView.mScrollSpeed > 0) {
                if (scrollPickView.mScrollSpeed > HIGH_SPEED) {
                    scrollPickView.mScrollSpeed -= DECELERATION_SPEED;
                    mIsLowSpeed = false;
                } else {
                    if (!mIsLowSpeed) {
                        mIsLowSpeed = true;
                        updateTargetY(scrollPickView);
                    }
                }
            } else {
                if (scrollPickView.mScrollSpeed < - HIGH_SPEED) {
                    scrollPickView.mScrollSpeed += DECELERATION_SPEED;
                    mIsLowSpeed = false;
                } else {
                    if (!mIsLowSpeed) {
                        mIsLowSpeed = true;
                        updateTargetY(scrollPickView);
                    }
                }
            }
            double nextY;
            if (mIsLowSpeed) {
                if (Math.abs(scrollPickView.mCurrentY - mTargetY) < STOP_PIXEL_THRESHOLD) {
                    scrollPickView.stopAfterAnimation();
                    scrollPickView.mCurrentY = (float) mTargetY;
                    scrollPickView.invalidate();
                    if (scrollPickView.mOnValueChangedListener != null) {
                        scrollPickView.mOnValueChangedListener.onValueChange(mTargetValue);
                    }
                    return;
                } else if (scrollPickView.mCurrentY > mTargetY){
                    nextY = scrollPickView.mCurrentY - LAST_MOVE_PIXEL;
                } else {
                    nextY = scrollPickView.mCurrentY + LAST_MOVE_PIXEL;
                }
            } else {
                nextY = scrollPickView.mCurrentY + speed * AFTER_ANIMATION_TIME_GAP;
            }

            if (nextY <= 0) {
                scrollPickView.stopAfterAnimation();
                scrollPickView.mCurrentY = 0;
                if (scrollPickView.mOnValueChangedListener != null) {
                    scrollPickView.mOnValueChangedListener.onValueChange(scrollPickView.mMinValue);
                }
            } else if (nextY >= (scrollPickView.getPositionCount() * scrollPickView.mItemHeightPx
                                         - scrollPickView.mTotalHeightPx)){
                scrollPickView.stopAfterAnimation();
                scrollPickView.mCurrentY = (scrollPickView.getPositionCount() * scrollPickView.mItemHeightPx
                                                    - scrollPickView.mTotalHeightPx);
                if (scrollPickView.mOnValueChangedListener != null) {
                    scrollPickView.mCurrentPosition = scrollPickView.getPositionCount() - scrollPickView.mOffset - 1;
                    // 循环滚动情况，最后位置不是最大值，需要重新计算
                    scrollPickView.mOnValueChangedListener.onValueChange(scrollPickView.getValue());
                }
            } else {
                scrollPickView.mCurrentY = (float) nextY;
            }
            scrollPickView.invalidate();
        }

        private void updateTargetY(ScrollPickView scrollPickView) {
            float realY = scrollPickView.mCurrentY  + scrollPickView.mOffsetItemHeightPx + scrollPickView.mHalfItemHeightPx;
            int targetPosition = (int) (realY / scrollPickView.mItemHeightPx);
            mTargetY = (targetPosition - scrollPickView.mOffset) * scrollPickView.mItemHeightPx;
            scrollPickView.mCurrentPosition = (int) ((mTargetY + scrollPickView.mOffsetItemHeightPx + scrollPickView
                    .mHalfItemHeightPx) / scrollPickView.mItemHeightPx); // 提前计算下Y对引得position
            mTargetValue = scrollPickView.getValue(); // 方法底层对position进行装换得到value值
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAfterAnimation();
    }

    public interface Formatter {
        String getFormatString(int value);
    }

    /**
     * 数值改变的回调器; 在更改了mCurrentY后调用
     */
    public interface OnValueChangedListener {
        void onValueChange(int value);
    }

    private static class SavedState extends BaseSavedState {
        private int mSelectedItemTextColor;
        private int mNormalItemTextColor;
        // 设置ScrollSelector的背景颜色
        private int mSelectBackgroundColor;
        private int mNormalBackgroundColor;
        private int mSelectItemLineColor;

        private int mOffset;
        private int mCurrentPosition;
        private int mMinValue;
        private int mMaxValue;
        private int mValueLength;
        private boolean mIsLoopFlag;

        SavedState(Parcel source) {
            super(source);
            mSelectedItemTextColor = source.readInt();
            mNormalItemTextColor = source.readInt();
            mSelectBackgroundColor = source.readInt();
            mNormalBackgroundColor = source.readInt();
            mSelectItemLineColor = source.readInt();

            mOffset = source.readInt();
            mCurrentPosition = source.readInt();
            mMinValue = source.readInt();
            mMaxValue = source.readInt();
            mValueLength = source.readInt();
            mIsLoopFlag = Boolean.valueOf(source.readString());

        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(mSelectedItemTextColor );
            out.writeInt(mNormalItemTextColor);
            out.writeInt(mSelectBackgroundColor);
            out.writeInt(mNormalBackgroundColor);
            out.writeInt(mSelectItemLineColor);

            out.writeInt(mOffset);
            out.writeInt(mCurrentPosition);
            out.writeInt(mMinValue);
            out.writeInt(mMaxValue);
            out.writeInt(mValueLength);
            out.writeString(Boolean.toString(mIsLoopFlag));
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>(){

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
