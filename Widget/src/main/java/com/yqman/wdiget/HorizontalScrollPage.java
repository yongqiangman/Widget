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

import java.util.ArrayList;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HorizontalScrollPage extends FrameLayout implements GenericLifecycleObserver {
    private static final int POLL_FLAG = 99;
    private static final int DEFAULT_DELAY_TIME_SECOND = -1;
    private static final int MAX_ITEM_COUNT = Integer.MAX_VALUE;
    private static final int OFFSET_VALUE = 100;

    private ViewPager mViewPager;
    private OnItemClickedListener mClickListener;
    private OnItemSelectedListener mSelectedListener;
    private ArrayList<String> mImageResource;
    private int mPrePosition = 0;
    private PollHandler mHandler = new PollHandler();
    private int mRollingFrequency;
    private ImageLoader mImageLoader;
    private boolean mCanPollPage = false;
    /**
     * 是否支持ImageView的放缩
     */
    private boolean mEnableZoomImage = false;
    /**
     * 是否支持无限循环
     */
    private boolean mEnableInfiniteSliding = true;

    public HorizontalScrollPage(Context context) {
        this(context, null);
    }

    public HorizontalScrollPage(Context context, AttributeSet attributes) {
        super(context, attributes);
        TypedArray array = context.obtainStyledAttributes(attributes, R.styleable.HorizontalScrollPage);
        mRollingFrequency = array.getInt(R.styleable.HorizontalScrollPage_yqman_widget_rollingFrequencySecond,
                DEFAULT_DELAY_TIME_SECOND);
        mEnableZoomImage = array.getBoolean(R.styleable.HorizontalScrollPage_yqman_widget_enableZoomImage,
                false);
        mEnableInfiniteSliding = array.getBoolean(R.styleable.HorizontalScrollPage_yqman_widget_enableInfiniteSliding,
                true);
        array.recycle();
        mViewPager = new ViewPager(context);
        mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mViewPager);
        mViewPager.addOnPageChangeListener(new ScrollPagerChangeListener());
        initData();
    }

    public void setImageResource(ArrayList<String> resource) {
        mImageResource = resource;
        initData();
        final PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null && adapter.getCount() == MAX_ITEM_COUNT) {
            mViewPager.setCurrentItem(mImageResource.size() * OFFSET_VALUE);
        }
        startSinglePolling();
    }

    private void initData() {
        mViewPager.setAdapter(new Adapter());
        mPrePosition = 0;
        if (mSelectedListener != null) {
            mSelectedListener.onItemSelect(0, mImageResource.size());
        }
    }

    private void stopSinglePolling() {
        mHandler.removeMessages(POLL_FLAG);
    }

    private void startSinglePolling() {
        if (mRollingFrequency != DEFAULT_DELAY_TIME_SECOND
                && mCanPollPage
                && mImageResource != null
                && !mImageResource.isEmpty()) {
            if (!mHandler.hasMessages(POLL_FLAG)) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(POLL_FLAG, this), mRollingFrequency * 1000L);
            }
        }
    }

    private void updateImageViewResource(ImageView imageView, int pos) {
        if (mImageLoader != null) {
            mImageLoader.updateImageView(imageView, mImageResource.get(pos % mImageResource.size()));
        }
    }

    /**
     * 遇到pointerIndex out of range问题，
     * 参考https://kaywu.xyz/2016/09/25/pointer-index-out-of-range/
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev != null) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                stopSinglePolling();
            } else if (action == MotionEvent.ACTION_UP) {
                startSinglePolling();
            }
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void pollUpdateView() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter == null || adapter.getCount() < 0) {
            return;
        }
        final int nextPosition = mPrePosition + 1;
        if (nextPosition >= adapter.getCount()) {
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(nextPosition);
        }
        startSinglePolling();
    }

    public void registerLifecycleObserver(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        Lifecycle.State state = source.getLifecycle().getCurrentState();
        if (state == Lifecycle.State.RESUMED) {
            mCanPollPage = true;
            startSinglePolling();
            return;
        }
        mCanPollPage = false;
        stopSinglePolling();
        if (state == Lifecycle.State.DESTROYED) {
            source.getLifecycle().removeObserver(this);
        }
    }

    private class Adapter extends PagerAdapter {

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object == view;
        }

        /**
         * 只有一个banner则不支持滑动
         */
        @Override
        public int getCount() {
            final int size;
            if (mImageResource == null) {
                size = 0;
            } else {
                size = mImageResource.size();
            }
            if (size > 1 && mEnableInfiniteSliding) {
                return MAX_ITEM_COUNT;
            } else {
                return size;
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = createImageView(position);
            container.addView(imageView);
            updateImageViewResource(imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }

        private ImageView createImageView(final int pos) {
            ImageView imageView;
            if (mEnableZoomImage) {
                imageView = new ZoomImageView(getContext());
            } else {
                imageView = new ImageView(getContext());
            }
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onClickItem(pos % mImageResource.size());
                    }
                }
            });
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;
        }
    }

    private class ScrollPagerChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mSelectedListener != null && mImageResource.size() != 0) {
                int realCurrentPos = (position % mImageResource.size());
                mSelectedListener.onItemSelect(realCurrentPos, mImageResource.size());
            }
            mPrePosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public static class PollHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            int what = msg.what;
            if (what == POLL_FLAG) {
                HorizontalScrollPage horizontalDotView = (HorizontalScrollPage) msg.obj;
                horizontalDotView.pollUpdateView();
            }
        }
    }

    public void setItemClickListener(OnItemClickedListener itemClickListener) {
        mClickListener = itemClickListener;
    }

    public void setItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        mSelectedListener = itemSelectedListener;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public interface OnItemClickedListener {
        /**
         * 点击的位置
         */
        void onClickItem(int pos);
    }

    public interface OnItemSelectedListener {
        /**
         * @param selected 当前选中的位置
         * @param sum      page页面总数
         */
        void onItemSelect(int selected, int sum);
    }

    /**
     * 网络图片加载库
     */
    public interface ImageLoader {
        void updateImageView(ImageView imageView, String url);
    }
}