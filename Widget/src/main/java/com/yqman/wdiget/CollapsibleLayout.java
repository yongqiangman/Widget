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

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

public class CollapsibleLayout extends FrameLayout {

    private final View mRootView;
    private final View mContentView;
    private final View mExpandedTitleView;
    private final View mCollapsedTitleView;
    private boolean mCurrentEnableCollapsible = true;
    private OnCollapsibleListener mOnCollapsibleListener;

    public CollapsibleLayout(@NonNull Context context) {
        this(context, null);
    }

    public CollapsibleLayout(@NonNull Context context,
                             @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsibleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleLayout);
        int contentLayoutRes = array.getResourceId(R.styleable.CollapsibleLayout_yqman_widget_content_layout, 0);
        int expandedTitleLayoutRes = array.getResourceId(R.styleable.CollapsibleLayout_yqman_widget_expanded_title_layout, 0);
        int collapsedTitleLayoutRes = array.getResourceId(R.styleable.CollapsibleLayout_yqman_widget_collapsed_title_layout, 0);
        final int contentScrimRes = array.getResourceId(R.styleable.CollapsibleLayout_yqman_widget_content_scrim_resource, 0);
        array.recycle();
        if (contentLayoutRes != 0 && expandedTitleLayoutRes != 0 && collapsedTitleLayoutRes != 0) {
            mRootView = LayoutInflater.from(context).inflate(R.layout.yqman_widget_collapsible_layout, null);
            addView(mRootView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            final AppBarLayout appBarLayout = mRootView.findViewById(R.id.app_bar_layout);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                    if (mOnCollapsibleListener != null) {
                        final float ratio =  Math.abs(i / (appBarLayout.getTotalScrollRange() + 0f));
                        mOnCollapsibleListener.onOffsetChanged(ratio);
                    }
                }
            });
            /*step1：初始化内容*/
            final ViewStub contentView = mRootView.findViewById(R.id.collapsible_layout_content_root_view);
            contentView.setLayoutResource(contentLayoutRes);
            mContentView = contentView.inflate();
            /*step2：初始化折叠装填的内容*/
            final ViewStub rootView = mRootView.findViewById(R.id.collapsible_layout_collapsed_root_view);
            rootView.setLayoutResource(collapsedTitleLayoutRes);
            rootView.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub stub, View inflated) {
                    CollapsingToolbarLayout collapsingToolbarLayout =
                            mRootView.findViewById(R.id.collapsible_layout_title_root_view);
                    if(contentScrimRes != 0) {
                        collapsingToolbarLayout.setContentScrimResource(contentScrimRes);
                    }
                    inflated.measure(0, 0);
                    collapsingToolbarLayout.setMinimumHeight(inflated.getMeasuredHeight());
                }
            });
            mCollapsedTitleView = rootView.inflate();
            /*step3：初始化展开下的内容*/
            final ViewStub expandedView = mRootView.findViewById(R.id.collapsible_layout_expanded_root_view);
            expandedView.setLayoutResource(expandedTitleLayoutRes);
            mExpandedTitleView = expandedView.inflate();

        } else {
            final String msg = new StringBuilder()
                    .append("contentLayoutRes=").append(contentLayoutRes)
                    .append("expandedTitleLayoutRes=").append(expandedTitleLayoutRes)
                    .append("collapsedTitleLayoutRes=").append(collapsedTitleLayoutRes)
                    .append(" not allowed to be empty").toString();
            throw new IllegalArgumentException(msg);
        }
    }

    public View getContentView() {
        return mContentView;
    }

    public View getExapandedTitleView() {
        return mExpandedTitleView;
    }

    public View getCollapsedTitleView() {
        return mCollapsedTitleView;
    }

    /**
     * 可折叠的titleBar是否可支持滑动，不让滑动是为了解决控件底部的非titleBar内容无法全部显示和居中的问题
     * 但是该方法会导致折叠titleBar变成展开状态，及时调用该方法时为折叠状态
     */
    public void enableCollapsible(Boolean isEnable) {
        // 重复的状态则不做出任何响应
        if (mCurrentEnableCollapsible == isEnable) {
            return;
        }
        mCurrentEnableCollapsible = isEnable;
        final CollapsingToolbarLayout collapsingToolbarLayout =
                mRootView.findViewById(R.id.collapsible_layout_title_root_view);
        final AppBarLayout.LayoutParams layoutParams =
                (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        if (isEnable) {
            layoutParams.setScrollFlags(layoutParams.getScrollFlags() | SCROLL_FLAG_SCROLL);
        } else {
            layoutParams.setScrollFlags(layoutParams.getScrollFlags() & ~SCROLL_FLAG_SCROLL);
        }
        collapsingToolbarLayout.setLayoutParams(layoutParams);
    }

    /**
     * 展示titleBar
     * 后面的setBehavior解决app_bar_layout为GONE后还有空白的问题
     */
    private void hideTitleBar() {
        mRootView.findViewById(R.id.app_bar_layout).setVisibility(View.GONE);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mContentView.getLayoutParams();
        layoutParams.setBehavior(null);
        mContentView.requestLayout();
    }

    /**
     * 隐藏titleBar
     * 后面的setBehavior解决app_bar_layout为GONE后还有空白的问题
     */
    private void showTitleBar() {
        mRootView.findViewById(R.id.app_bar_layout).setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mContentView.getLayoutParams();
        layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        mContentView.requestLayout();
    }

    public void setOnCollapsibleListener(OnCollapsibleListener onCollapsibleListener) {
        mOnCollapsibleListener = onCollapsibleListener;
    }

    public interface OnCollapsibleListener {
        /**
         *
         * @param ratio 0 代表未折叠，1 代表已经全部折叠
         */
        void onOffsetChanged(float ratio);
    }
}
