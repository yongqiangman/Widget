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
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yqman on 2016/6/13.
 * 当前类用于创建一个可以放大缩小的ImageView、核心是通过Matrix实现
 * 参考郭林博客：http://blog.csdn.net/guolin_blog/article/details/11100327
 * 但是相对于他添加了双击放大缩小功能，同时改善了他之前无法给该控件设置监听器的问题，继承自Imageview
 * 使得在使用的时候可以将其看成一个普通的ImageView，只不过它还具有放大缩小的功能
 */
public class ZoomImageView extends AppCompatImageView {

    private static final int INIT_STATE = 0;
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_OUT = 2;
    private static final int MOVE_STATE = 3;
    private int mLocalState;
    private Bitmap mBitmap;
    private Matrix mMatrix = new Matrix();
    private float mTotalRatio;
    private float mInitRatio;
    /**
     * 记录图片在矩阵上的横向偏移值，图片的左上角顶点在不做任何处理的情况下是显示在onLayout区域中的左上角位置的
     * 如果图片的宽度小于屏幕宽度，那么该值必然大于0，因为要居中显示，
     * 相反则该值必然小于0，目的同上
     */
    private float mTotalTranslateX;
    /**
     * 记录图片在矩阵上的纵向偏移值，具体内容同上
     */
    private float mTotalTranslateY;
    /**
     * 记录当前图片的宽度，图片被缩放时，这个值会一起变动
     */
    private float mCurrentBitmapWidth;
    /**
     * 记录当前图片的高度，图片被缩放时，这个值会一起变动
     */
    private float mCurrentBitmapHeight;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLocalState = INIT_STATE;
    }

    @Override
    protected void onDetachedFromWindow() {
        mLocalState = INIT_STATE;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() instanceof BitmapDrawable) {
            if (mBitmap != ((BitmapDrawable) getDrawable()).getBitmap()) {
                mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            }
        } else {
            super.onDraw(canvas);
            return;
        }
        switch (mLocalState) {
            case INIT_STATE:
                initDraw(canvas);
                break;
            case ZOOM_IN:
                zoom(canvas);
                break;
            case ZOOM_OUT:
                zoom(canvas);
                break;
            case MOVE_STATE:
                moveDraw(canvas);
                break;
        }
    }

    /**
     * 使用了Matrix的postTranslate和postScale方法，前者用于偏移,后者用于放大缩小
     */
    private void initDraw(Canvas canvas) {
        mMatrix.reset();
        int bitmapWidth = mBitmap.getWidth();
        int bitmapHeight = mBitmap.getHeight();
        if (bitmapWidth > width || bitmapHeight > height) {
            if (bitmapWidth - width > bitmapHeight - height) {
                // 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
                float ratio = width / (bitmapWidth * 1.0f);
                mMatrix.postScale(ratio, ratio);
                float translateY = (height - (bitmapHeight * ratio)) / 2f;
                // 在纵坐标方向上进行偏移，以保证图片居中显示
                mMatrix.postTranslate(0, translateY);
                mTotalTranslateY = translateY;
                mTotalRatio = mInitRatio = ratio;
            } else {
                // 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
                float ratio = height / (bitmapHeight * 1.0f);
                mMatrix.postScale(ratio, ratio);
                float translateX = (width - (bitmapWidth * ratio)) / 2f;
                // 在横坐标方向上进行偏移，以保证图片居中显示
                mMatrix.postTranslate(translateX, 0);
                mTotalTranslateX = translateX;
                mTotalRatio = mInitRatio = ratio;
            }
            mCurrentBitmapWidth = bitmapWidth * mInitRatio;
            mCurrentBitmapHeight = bitmapHeight * mInitRatio;
        } else {
            // 当图片的宽高都小于屏幕宽高时，图片放大
            if (width - bitmapWidth < height - bitmapHeight) {
                // 当图片宽度相对于高度更加接近屏幕边缘，将图片等比例放大，bitmap宽度扩充到屏幕边界
                float ratio = width / (bitmapWidth * 1.0f);
                mMatrix.postScale(ratio, ratio);
                float translateY = (height - (bitmapHeight * ratio)) / 2f;
                // 在纵坐标方向上进行偏移，以保证图片居中显示
                mMatrix.postTranslate(0, translateY);
                mTotalTranslateY = translateY;
                mTotalRatio = mInitRatio = ratio;
            } else {
                // 当图片宽度相对于高度更加接近屏幕边缘，将图片等比例放大，bitmap宽度扩充到屏幕边界
                float ratio = height / (bitmapHeight * 1.0f);
                mMatrix.postScale(ratio, ratio);
                float translateX = (width - (bitmapWidth * ratio)) / 2f;
                // 在横坐标方向上进行偏移，以保证图片居中显示
                mMatrix.postTranslate(translateX, 0);
                mTotalTranslateX = translateX;
                mTotalRatio = mInitRatio = ratio;
            }
            mCurrentBitmapWidth = bitmapWidth * mInitRatio;
            mCurrentBitmapHeight = bitmapHeight * mInitRatio;
        }
        canvas.drawBitmap(mBitmap, mMatrix, null);
    }

    private void zoom(Canvas canvas) {
        mMatrix.reset();
        // 将图片按总缩放比例进行缩放
        mMatrix.postScale(mTotalRatio, mTotalRatio);
        float scaledWidth = mBitmap.getWidth() * mTotalRatio;
        float scaledHeight = mBitmap.getHeight() * mTotalRatio;
        float translateX;
        float translateY;
        // 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
        if (mCurrentBitmapWidth < width) {
            translateX = (width - scaledWidth) / 2f;
        } else {
            translateX = mTotalTranslateX * scaledRatio + centerPointX * (1 - scaledRatio);
            // 进行边界检查，保证图片缩放后在水平方向上不会偏移出屏幕
            if (translateX > 0) {
                translateX = 0;
            } else if (width - translateX > scaledWidth) {
                translateX = width - scaledWidth;
            }
        }
        // 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
        if (mCurrentBitmapHeight < height) {
            translateY = (height - scaledHeight) / 2f;
        } else {
            translateY = mTotalTranslateY * scaledRatio + centerPointY * (1 - scaledRatio);
            // 进行边界检查，保证图片缩放后在垂直方向上不会偏移出屏幕
            if (translateY > 0) {
                translateY = 0;
            } else if (height - translateY > scaledHeight) {
                translateY = height - scaledHeight;
            }
        }
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        mMatrix.postTranslate(translateX, translateY);
        mTotalTranslateX = translateX;
        mTotalTranslateY = translateY;
        mCurrentBitmapWidth = scaledWidth;
        mCurrentBitmapHeight = scaledHeight;
        canvas.drawBitmap(mBitmap, mMatrix, null);
    }

    private void moveDraw(Canvas canvas) {
        mMatrix.reset();
        // 根据手指移动的距离计算出总偏移值,进入这里之前已经对边界进行过判断了
        float translateX = mTotalTranslateX + moveXDistance;
        float translateY = mTotalTranslateY + moveYDistance;
        // 先按照已有的缩放比例对图片进行缩放
        mMatrix.postScale(mTotalRatio, mTotalRatio);
        // 再根据移动距离进行偏移
        mMatrix.postTranslate(translateX, translateY);
        mTotalTranslateX = translateX;
        mTotalTranslateY = translateY;
        canvas.drawBitmap(mBitmap, mMatrix, null);

    }

    private int width;
    private int height;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
        //得到当前View的在屏幕中占有的宽度和高度
    }

    private double lastFingerDis;
    private float lastXMove = -1;
    private float lastYMove = -1;
    private float moveXDistance;
    private float moveYDistance;
    private float scaledRatio;
    private boolean zoomEnableFlag = false;
    private long lastActionDownTime = System.currentTimeMillis();

    /**
     * ACTION_DOWN：这个很好理解，当屏幕检测到有手指按下之后就触发到这个事件。
     * ACTION_POINTER_DOWN：这个是实现多点的关键，当屏幕检测到有多个手指同时按下之后，就触发了这个事件。
     * 通过getAction()可以获得当前屏幕的事件类型：ACTION_DOWN, ACTION_MOVE, ACTION_UP, or ACTION_CANCEL。
     * 但是如果像获得如上的ACTION_POINTER_DOWN，则得和ACTION_MASK相与才能得到ACTION_POINTER_DOWN事件。
     * ACTION_DOWN is for the first finger that touches the screen. This starts the gesture. The pointer data for
     * this finger is always at index 0 in the MotionEvent.
     * ACTION_POINTER_DOWN is for extra fingers that enter the screen beyond the first. The pointer data for this
     * finger is at the index returned by getActionIndex().
     * ACTION_POINTER_UP is sent when a finger leaves the screen but at least one finger is still touching it. The
     * last data sample about the finger that went up is at the index returned by getActionIndex().
     * ACTION_UP is sent when the last finger leaves the screen. The last data sample about the finger that went up
     * is at index 0. This ends the gesture.
     *
     * @return 只有在move和放大的时候才消耗当前事件，否则都不消耗即返回false；
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
         * 下面的这段代码主要是针对ViewPager和ZoomImageView共同出现时事件冲突的代码。
         * ViewPager实现ViewGroup的onInterceptTouchEvent方法，该方法保证其能在ViewPager的childrenView
         * 的DispatchOnTouchEvent方法前执行。因此进行如下的修正，当ZoomImageView在放大的状态时禁止ViewPager执行
         * onInterceptTouchEvent方法，子视图通过getParent().requestDisallowInterceptTouchEvent设定ViewPager是否执行
         * onInterceptTouchEvent方法。
         * 参考链接：http://blog.csdn.net/guolin_blog/article/details/12646775
         */
        if (mInitRatio == mTotalRatio) {
            getParent().requestDisallowInterceptTouchEvent(false);
        } else {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * 尤其对于事件MotionEvent.ACTION_DOWN必须返回true否则无法再次接收到新的事件！
                 * 默认返回一个true，前者执行点击事件
                 */
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    /* 当有两个手指按在屏幕上时，计算两指之间的距离*/
                    lastFingerDis = distanceBetweenFingers(event);
                    zoomEnableFlag = true;
                }
                break; //不消耗该事件交给父类处理
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    //手指离开屏幕时将临时值还原
                    lastXMove = -1;
                    lastYMove = -1;
                    zoomEnableFlag = false;
                }
                break;//同样的多点触控手指抬起放下不消耗事件
            case MotionEvent.ACTION_UP:
                // 手指离开屏幕时将临时值还原
                lastXMove = -1;
                lastYMove = -1;
                zoomEnableFlag = false;

                long time = System.currentTimeMillis();
                if (time - lastActionDownTime < 300) {
                    lastActionDownTime = time;
                    moveState();
                    return true; //如果是双击操作则消耗该事件
                }
                lastActionDownTime = time;
                break; //不是双击 交给父类处理事件

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && (mTotalRatio != mInitRatio)) {
                    float xMove = event.getX();
                    float yMove = event.getY();
                    if (lastXMove == -1 || lastYMove == -1) {
                        lastYMove = yMove;
                        lastXMove = xMove;
                    }
                    mLocalState = MOVE_STATE;
                    moveXDistance = xMove - lastXMove;
                    moveYDistance = yMove - lastYMove;
                    //为了方便理解，可以将图片看成是放大的图片，即当前图片的宽度大于屏幕宽度，如果还居中显示，则totalTranslateX必然小于0
                    if (mTotalTranslateX + moveXDistance > 0) {
                        //证明已经显示了图片的左边界，对应totalTranslateX=0
                        moveXDistance = 0;
                    } else if (width - (mTotalTranslateX + moveXDistance) > mCurrentBitmapWidth) {
                        //证明已经显示了图片的右边界，totalTranslateX + moveXDistance为左边隐藏的区域，
                        // width为正在显示的区域，两者加起来大于当前图片宽度则证明到达右边界
                        moveXDistance = 0;
                    }
                    //纵坐标和横坐标原理一致，不再赘述
                    if (mTotalTranslateY + moveYDistance > 0) {
                        moveYDistance = 0;
                    } else if (height - (mTotalTranslateY + moveYDistance) > mCurrentBitmapHeight) {
                        moveYDistance = 0;
                    }
                    // 调用onDraw()方法绘制图片
                    invalidate();
                    lastXMove = xMove;
                    lastYMove = yMove;
                    return true;
                } else if (event.getPointerCount() == 2) {
                    // 有两个手指按在屏幕上移动时，为缩放状态
                    centerPointBetweenFingers(event);
                    double fingerDis = distanceBetweenFingers(event);
                    if (fingerDis > lastFingerDis) {
                        mLocalState = ZOOM_OUT;
                    } else {
                        mLocalState = ZOOM_IN;
                    }
                    // 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
                    if ((mLocalState == ZOOM_OUT && mTotalRatio < 4 * mInitRatio)
                            || (mLocalState == ZOOM_IN && mTotalRatio > mInitRatio)) {
                        scaledRatio = (float) (fingerDis / lastFingerDis);
                        mTotalRatio = mTotalRatio * scaledRatio;
                        if (mTotalRatio > 4 * mInitRatio) {
                            mTotalRatio = 4 * mInitRatio;
                        } else if (mTotalRatio < mInitRatio) {
                            mTotalRatio = mInitRatio;
                        }
                        // 调用onDraw()方法绘制图片
                        invalidate();
                        lastFingerDis = fingerDis;
                    }
                    return true;
                }
                break;
            default:
                break;
        }
        return (zoomEnableFlag || (mTotalRatio != mInitRatio)) || super.onTouchEvent(event);
    }

    /**
     * 双击恢复初始状态
     */
    private void moveState() {
        if (mLocalState != INIT_STATE) {
            mLocalState = INIT_STATE;
            invalidate();
        }
    }

    /**
     * 计算手指距离
     */
    private double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 计算两个手指之间中心点的坐标。
     */
    private float centerPointX;
    private float centerPointY;

    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        centerPointX = (xPoint0 + xPoint1) / 2;
        centerPointY = (yPoint0 + yPoint1) / 2;
    }
}
