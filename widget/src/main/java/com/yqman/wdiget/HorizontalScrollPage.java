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
    private static final int DEFAULT_DELAY_TIME_SECOND = 5;

    private ViewPager mViewPager;
    private OnItemClickedListener mClickListener;
    private OnItemSelectedListener mSelectedListener;
    private ArrayList<String> mImageResource;
    private int mPrePosition = 0;
    private PollHandler mHandler = new PollHandler();
    private Long mRollingFrequency;
    private ImageLoader mImageLoader;
    private boolean mCanPollPage = false;

    public HorizontalScrollPage(Context context) {
        this(context, null);
    }

    public HorizontalScrollPage(Context context, AttributeSet attributes) {
        super(context, attributes);
        TypedArray array = context.obtainStyledAttributes(attributes, R.styleable.HorizontalScrollPage);
        mRollingFrequency = array.getInt(R.styleable.HorizontalScrollPage_yqman_widget_rollingFrequencySecond,
                DEFAULT_DELAY_TIME_SECOND) * (1000L);
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
        if (mCanPollPage && mImageResource != null && !mImageResource.isEmpty()) {
            if (!mHandler.hasMessages(POLL_FLAG)) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(POLL_FLAG, this), mRollingFrequency);
            }
        }
    }

    private void updateImageViewResource(ImageView imageView, int pos) {
        if (mImageLoader != null) {
            mImageLoader.updateImageView(imageView, mImageResource.get(pos % mImageResource.size()));
        }
    }

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
        return super.dispatchTouchEvent(ev);
    }

    private void pollUpdateView() {
        mViewPager.setCurrentItem(mPrePosition + 1);
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
            if (mImageResource == null || mImageResource.size() == 0) {
                return 0;
            }
            if (mImageResource.size() == 1) {
                return 1;
            }
            return Integer.MAX_VALUE;
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
            ImageView imageView = new ImageView(getContext());
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
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            return imageView;
        }
    }

    private class ScrollPagerChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mSelectedListener != null) {
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