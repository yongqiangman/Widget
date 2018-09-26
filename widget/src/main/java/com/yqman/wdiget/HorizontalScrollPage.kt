package com.yqman.wdiget

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import java.lang.ref.WeakReference

class HorizontalScrollPage : FrameLayout {
    companion object {
        private const val POLL_FLAG = 99
        private const val DEFAULT_DELAY_TIME_SECOND : Int = 5
    }

    private val mViewPager : ViewPager
    var mItemClickListener : OnItemClickedListener? = null
    var mItemSelectedListener : OnItemSelectedListener? = null
    private var mImageResource = ArrayList<String>()
    var mPrePosition : Int = 0
    private val mHandler = PollHandler()
    private val mRollingFrequency : Long
    var mImageLoader : ImageLoader? = null

    constructor(context : Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes) {
        val array = context.obtainStyledAttributes(attributes, R.styleable.HorizontalScrollPage)
        mRollingFrequency = array.getInt(R.styleable.HorizontalScrollPage_yqman_widget_rollingFrequencySecond,
                DEFAULT_DELAY_TIME_SECOND) * (1000.toLong())
        array.recycle()
        mViewPager = ViewPager(context)
        mViewPager.layoutParams = generateDefaultLayoutParams()
        addView(mViewPager)
        mViewPager.addOnPageChangeListener(ScrollPagerChangeListener())
        initData()
        startSinglePolling()
    }

    fun setImageResource(resource : ArrayList<String>) {
        mImageResource = resource
        initData()
    }

    private fun initData() {
        mViewPager.adapter = Adapter()
        mPrePosition = 0
        mItemSelectedListener?.apply {
            onItemSelect(mPrePosition, 0, mImageResource.size)
        }
    }

    private fun stopSinglePolling() {
        mHandler.removeMessages(POLL_FLAG)
    }

    private fun startSinglePolling() {
        if (!mHandler.hasMessages(POLL_FLAG)) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(POLL_FLAG, this), mRollingFrequency)
        }
    }

    private fun updateImageViewResource(imageView: ImageView, pos: Int) {
        mImageLoader?.updateImageView(imageView, mImageResource[pos % mImageResource.size])
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.apply {
            if (action == MotionEvent.ACTION_DOWN) {
                stopSinglePolling()
            } else if (action == MotionEvent.ACTION_UP) {
                startSinglePolling()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun pollUpdateView() {
        mViewPager.currentItem = mPrePosition + 1
        startSinglePolling()
    }

    inner class Adapter : PagerAdapter() {

        private val mImageViews : SparseArray<WeakReference<ImageView>> = SparseArray()

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object` == view
        }

        override fun getCount(): Int {
            return if (mImageResource.size > 0) Integer.MAX_VALUE else 0
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView : ImageView = obtainCacheImageView(position)?:createImageView(position)
            container.addView(imageView)
            updateImageViewResource(imageView, position)
            return imageView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val imageView = obtainCacheImageView(position)
            container.removeView(imageView)
        }

        private fun obtainCacheImageView(pos : Int) : ImageView? {
            return mImageViews[pos]?.get()
        }

        private fun createImageView(pos : Int) : ImageView {
            val imageView = ImageView(context)
            imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            imageView.setOnClickListener {
                mItemClickListener?.apply {
                    onClickItem(pos % mImageResource.size)
                }
            }
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            mImageViews.put(pos, WeakReference(imageView))
            return imageView
        }
    }

    inner class ScrollPagerChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            // do nothing
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // do nothing
        }

        override fun onPageSelected(position: Int) {
            mItemSelectedListener?.apply {
                val realPrePos = (mPrePosition % mImageResource.size)
                val realCurrentPos = (position % mImageResource.size)
                onItemSelect(realPrePos, realCurrentPos, mImageResource.size)
            }
            mPrePosition = position
        }
    }


    class PollHandler : Handler() {

        override fun dispatchMessage(msg: Message?) {
            msg?.apply {
                if (what == POLL_FLAG) {
                    val horizontalDotView : HorizontalScrollPage = msg.obj as HorizontalScrollPage
                    horizontalDotView.pollUpdateView()
                }
            }
            if (msg?.what == POLL_FLAG) {

            }
        }
    }

    interface OnItemClickedListener {
        /**
         * 点击的位置
         */
        fun onClickItem(pos : Int)
    }

    interface OnItemSelectedListener {
        /**
         * @param prePos 之前的位置
         * @param selected 当前选中的位置
         * @param sum page页面总数
         */
        fun onItemSelect(prePos : Int, selected : Int, sum : Int)
    }

    /**
     * 网络图片加载库
     */
    interface ImageLoader {
        fun updateImageView(imageView: ImageView, url : String)
    }
}