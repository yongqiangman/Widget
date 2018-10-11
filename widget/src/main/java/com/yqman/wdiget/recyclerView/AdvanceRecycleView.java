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
    private float yPosition = Y_INIT_POSITION;
    private BaseRefreshHeader refreshHeader;
    private BaseLoadMoreFooter loadMoreFooter;
    private final int screen_height;

    public AdvanceRecycleView(Context context) {
        this(context, null);
    }

    public AdvanceRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screen_height = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE: /*没有滑动*/
                if (refreshHeader != null || getRefreshHeader()) {
                    refreshHeader.setChangeEnable();
                }
                if (isFooterView()) {
                    loadMoreFooter.startLoadMore();
                }
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING: /*滑动状态，手指在屏幕*/
                if (refreshHeader != null || getRefreshHeader()) {
                    refreshHeader.setChangeEnable();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING: /*滑动状态，手指不在屏幕*/
                if (refreshHeader != null || getRefreshHeader()) {
                    refreshHeader.setChangeUnable();
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
            loadMoreFooter = ((BaseRecyclerViewAdapter) adapter).getLoadMoreFooter();
            if (loadMoreFooter == null) {
                return false;
            }
            if (loadMoreFooter.getView().getParent() == null) {
                return false;
            }
            int[] position = new int[2];
            //两个int存的是左上角的坐标
            loadMoreFooter.getView().getLocationInWindow(position);
            //针对item总的高度还不足以能够填充整个屏幕则loadMore不显示
            return screen_height - position[1] <= loadMoreFooter.getView().getHeight();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (refreshHeader != null || getRefreshHeader()) {
                refreshHeader.setChangeEnable();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (yPosition == Y_INIT_POSITION) {
            yPosition = e.getRawY();//当前触摸事件所处的位置
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                yPosition = e.getRawY();
                if (refreshHeader != null || getRefreshHeader()) {
                    refreshHeader.setChangeEnable();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float increasedY = e.getRawY() - yPosition;
                yPosition = e.getRawY();
                if (isTopView()) {
                    if (refreshHeader.moveTo(increasedY)) {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                yPosition = -1;
                if (isTopView()) {
                    if (refreshHeader.getState() == BaseRefreshHeader.SIMPLE) {
                        refreshHeader.initState();
                    } else {
                        refreshHeader.startRefresh();
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
            refreshHeader = ((BaseRecyclerViewAdapter) adapter).getRefreshHeader();
            if (refreshHeader == null) {
                return false;
            }
            View view = refreshHeader.getView();
            return view.getParent() != null;
        }
        return false;
    }

    private boolean getRefreshHeader() {
        Adapter adapter = getAdapter();
        if (adapter instanceof BaseRecyclerViewAdapter) {
            refreshHeader = ((BaseRecyclerViewAdapter) adapter).getRefreshHeader();
            if (refreshHeader == null) {
                return false;
            }
            return true;
        }
        return false;
    }
}