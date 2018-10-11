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

package com.yqman.wdiget.recyclerView.item;

import android.view.View;

public abstract class BaseRefreshHeader {
    /**
     * 下拉刷新的三个状态
     */
    public static final int SIMPLE = 0;
    private static final int RELEASE_TO_REFRESH = 1;
    private static final int REFRESH = 2;
    private int mState;
    private boolean mKeepSateFlag = false;
    private int mHeight = 0;

    private void setState(int state) {
        if (mKeepSateFlag) {
            return;
        }
        switch (state) {
            case SIMPLE: //moveTo会输出该状态
                if (mState == SIMPLE) {
                    changeDownArrow(mHeight);
                } else if (mState == RELEASE_TO_REFRESH) {
                    changeToDownArrow();
                    mState = state;
                } else {
                    hideView();
                    mState = state;
                }
                break;
            case RELEASE_TO_REFRESH: //moveTo会输出该状态
                if (mState == SIMPLE) {
                    changeToUpArrow();
                    mState = state;
                } else if (mState == RELEASE_TO_REFRESH) {
                    //do nothing
                } else {
                    //do nothing
                }
                break;
            case REFRESH: //startRefresh会设置
                if (mState == SIMPLE) {
                    //do nothing
                } else if (mState == RELEASE_TO_REFRESH) {
                    mState = state;
                    startAnimation();
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                } else {
                    //do nothing
                }
                break;
        }
    }

    public final int getState() {
        return mState;
    }

    /**
     * 设置标志位flag为true即不允许更改状态，同时如果不是refresh状态进行初始化
     */
    public final void setChangeUnable() {
        if (!mKeepSateFlag) {
            if (mState < REFRESH) {
                initState();
            }
            mKeepSateFlag = true;
        }
    }

    /**
     * 设置标志位flag为false即允许更改状态，同时如果不是refresh状态进行初始化
     */
    public final void setChangeEnable() {
        if (mKeepSateFlag) {
            if (mState < REFRESH) {
                initState();
            }
            mKeepSateFlag = false;
        }
    }

    public final void initState() {
        mKeepSateFlag = false;
        hideView();
        mState = SIMPLE;//唯一一个在setSate方法外设置mState值的地方
    }

    public final void startRefresh() {
        if (mState == RELEASE_TO_REFRESH) {
            setState(REFRESH);
        }
    }

    public final void completeRefresh() {
        initState();
    }

    /**
     * 根据接收到的delta改变到对应状态
     *
     * @param delta 显示高度需要增加的数值可能为负
     *
     * @return true消耗事件、false不消耗事件
     */
    public final boolean moveTo(float delta) {
        if (mState <= RELEASE_TO_REFRESH) {
            mHeight = getCurrentRefreshViewHeight();
            mHeight = mHeight + (int) delta;
            if (mHeight < 0) {
                return false;
            }
            //必须要做这样的操作，否则用户滑动一点直接进入到RELAESE_TO_REFRESH状态
            if (mHeight >= getReleaseToRefreshFlagHeight()) {
                setState(RELEASE_TO_REFRESH);
            } else {
                setState(SIMPLE);
            }
            return true;
        }
        return false;
    }

    /**
     * 监听器设置
     */
    public interface IRefreshListener {
        void onRefresh();
    }

    private IRefreshListener mRefreshListener;

    public final void setRefreshListener(IRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    /**
     * 停止动画,隐藏下拉刷新界面
     */
    protected abstract void hideView();

    /**
     * 开始动画
     */
    protected abstract void startAnimation();

    /**
     * 显示向下箭头 和 对应文字
     */
    protected abstract void changeDownArrow(int height);

    /**
     * 由向上到向下转换
     */
    protected abstract void changeToDownArrow();

    /**
     * 显示向上箭头 和 对应文字
     */
    protected abstract void changeToUpArrow();

    protected abstract int getReleaseToRefreshFlagHeight();

    protected abstract int getCurrentRefreshViewHeight();

    /**
     * 返回一个View给AdvanceAdapter使用
     */
    public abstract View getView();
}