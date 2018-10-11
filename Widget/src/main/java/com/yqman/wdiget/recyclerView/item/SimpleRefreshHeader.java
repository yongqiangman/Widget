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

import com.yqman.wdiget.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by yqman on 2016/5/12.
 */
public class SimpleRefreshHeader extends BaseRefreshHeader {
    private LinearLayout mLinearLayout;
    private LinearLayout mContainer;
    private Context mContext;
    private ImageView mImageView;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private static final int RELEASE_REFRESH_FLAG = 200; //下拉视图最大高度

    public SimpleRefreshHeader(Context context) {
        mContext = context.getApplicationContext();
        initView();
    }

    private void initView() {
        mLinearLayout = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.yqman_widget_view_head_refresh, null);
        mContainer = mLinearLayout.findViewById(R.id.refresh_header_container);
        mImageView = mLinearLayout.findViewById(R.id.refresh_header_image);
        mTextView = mLinearLayout.findViewById(R.id.refresh_header_textView);
        mProgressBar = mLinearLayout.findViewById(R.id.refresh_header_progressbar);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mContainer.setLayoutParams(params);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateUpAnim.setDuration(180);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(180);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    protected void hideView() {
        mImageView.clearAnimation();
        mImageView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTextView.setText("");
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(lp.height, 0);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
                lp.height = (int) animation.getAnimatedValue();
                mContainer.setLayoutParams(lp);
            }
        });
        animator.start();
    }

    @Override
    protected void startAnimation() {
        mImageView.clearAnimation();
        mImageView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setText(mContext.getResources().getString(R.string.yqman_widget_loading));
    }

    @Override
    protected void changeDownArrow(int height) {
        mProgressBar.setVisibility(View.GONE);
        mImageView.setVisibility(View.VISIBLE);
        mTextView.setText(mContext.getResources().getString(R.string.yqman_widget_pull_refresh));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();

        lp.height = height > 0 ? height : 0;
        mContainer.setLayoutParams(lp);
    }

    @Override
    protected void changeToUpArrow() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        lp.height = RELEASE_REFRESH_FLAG;
        mContainer.setLayoutParams(lp);
        mImageView.setAnimation(mRotateUpAnim);
        mTextView.setText(mContext.getResources().getString(R.string.yqman_widget_release_refresh));
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void changeToDownArrow() {
        mImageView.setAnimation(mRotateDownAnim);
        mTextView.setText(mContext.getResources().getString(R.string.yqman_widget_pull_refresh));
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected int getCurrentRefreshViewHeight() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

    @Override
    protected int getReleaseToRefreshFlagHeight() {
        return RELEASE_REFRESH_FLAG;
    }

    @Override
    public View getView() {
        return mLinearLayout;
    }
}