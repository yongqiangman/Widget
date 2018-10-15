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

package com.yqman.wdiget.recyclerView;

import com.yqman.wdiget.recyclerView.item.BaseLoadMoreFooter;
import com.yqman.wdiget.recyclerView.item.BaseRefreshHeader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 下拉上拉刷新使用的变量
 * 需要该RecyclerView配套AdvanceAdapter一起使用
 */
public class AdvanceRecycleView extends RecyclerView {
    private static final float Y_INIT_POSITION = -1;
    private float mYInitPosition = Y_INIT_POSITION;
    private BaseRefreshHeader mRefreshHeader;
    private BaseLoadMoreFooter mLoadMoreFooter;
    private final int mScreenHeight;

    public AdvanceRecycleView(Context context) {
        this(context, null);
    }

    public AdvanceRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE: /*没有滑动*/
                if (mRefreshHeader != null || getRefreshHeader()) {
                    mRefreshHeader.setChangeEnable();
                }
                if (isFooterView()) {
                    mLoadMoreFooter.startLoadMore();
                }
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING: /*滑动状态，手指在屏幕*/
                if (mRefreshHeader != null || getRefreshHeader()) {
                    mRefreshHeader.setChangeEnable();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING: /*滑动状态，手指不在屏幕*/
                if (mRefreshHeader != null || getRefreshHeader()) {
                    mRefreshHeader.setChangeUnable();
                }
                break;
            default:
                break;
        }
        super.onScrollStateChanged(state);
    }

    /**
     * 判断底部加载更多是否显示
     */
    private boolean isFooterView() {
        Adapter adapter = getAdapter();
        if (adapter instanceof BaseRecyclerViewAdapter) {
            mLoadMoreFooter = ((BaseRecyclerViewAdapter) adapter).getLoadMoreFooter();
            if (mLoadMoreFooter == null) {
                return false;
            }
            if (mLoadMoreFooter.getView().getParent() == null) {
                return false;
            }
            int[] position = new int[2];
            //两个int存的是左上角的坐标
            mLoadMoreFooter.getView().getLocationInWindow(position);
            //针对item总的高度还不足以能够填充整个屏幕则loadMore不显示
            return mScreenHeight - position[1] <= mLoadMoreFooter.getView().getHeight();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mRefreshHeader != null || getRefreshHeader()) {
                mRefreshHeader.setChangeEnable();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mYInitPosition == Y_INIT_POSITION) {
            mYInitPosition = e.getRawY();//当前触摸事件所处的位置
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mYInitPosition = e.getRawY();
                if (mRefreshHeader != null || getRefreshHeader()) {
                    mRefreshHeader.setChangeEnable();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float increasedY = e.getRawY() - mYInitPosition;
                mYInitPosition = e.getRawY();
                if (isTopView()) {
                    if (mRefreshHeader.moveTo(increasedY)) {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mYInitPosition = -1;
                if (isTopView()) {
                    if (mRefreshHeader.getState() == BaseRefreshHeader.SIMPLE) {
                        mRefreshHeader.initState();
                    } else {
                        mRefreshHeader.startRefresh();
                    }
                }
            default:
                break;

        }
        // 上面的代码不消耗事件只是取出事件中相关信息
        return super.onTouchEvent(e);
    }

    /**
     * 判断顶部刷新是否显示
     */
    private boolean isTopView() {
        Adapter adapter = getAdapter();
        if (adapter instanceof BaseRecyclerViewAdapter) {
            mRefreshHeader = ((BaseRecyclerViewAdapter) adapter).getRefreshHeader();
            if (mRefreshHeader == null) {
                return false;
            }
            View view = mRefreshHeader.getView();
            return view.getParent() != null;
        }
        return false;
    }

    private boolean getRefreshHeader() {
        Adapter adapter = getAdapter();
        if (adapter instanceof BaseRecyclerViewAdapter) {
            mRefreshHeader = ((BaseRecyclerViewAdapter) adapter).getRefreshHeader();
            if (mRefreshHeader == null) {
                return false;
            }
            return true;
        }
        return false;
    }
}