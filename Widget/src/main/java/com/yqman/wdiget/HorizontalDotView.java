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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HorizontalDotView extends LinearLayout {

    private static final String TAG = "HorizontalScrollPage";
    private int mLength;
    private int mSelectedResource;
    private int mNormalResource;
    private final int mLeftMargin;

    public HorizontalDotView(Context context) {
        this(context, null);
    }

    public HorizontalDotView(Context context, AttributeSet attributes) {
        super(context, attributes);
        TypedArray array = context.obtainStyledAttributes(attributes, R.styleable.HorizontalDotView);
        mSelectedResource = array.getResourceId(R.styleable.HorizontalDotView_yqman_widget_dot_normal_resource, R
                .drawable.yqman_widget_default_dot_unable);
        mNormalResource = array.getResourceId(R.styleable.HorizontalDotView_yqman_widget_dot_selected_resource, R
                .drawable.yqman_widget_default_dot_enable);
        mLeftMargin = array.getDimensionPixelSize(R.styleable.HorizontalDotView_yqman_widget_dotMargin, 10);
        array.recycle();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    public boolean updateLength(int length) {
        if (length == mLength) {
            return false;
        }
        removeAllViews();
        mLength = length > 0 ? length : 0;
        for (int index = 0; index < mLength; index++) {
            ImageView dot = new ImageView(getContext());
            dot.setImageResource(mNormalResource);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (index > 0) {
                // 圆点大小
                params.leftMargin = mLeftMargin;
            }
            dot.setLayoutParams(params);
            addView(dot);
        }
        return true;
    }

    public void updatePos(int currentPos, int size) {
        if (!updateLength(size)) {
            for (int index = 0; index < getChildCount(); index++) {
                if (getChildAt(index) != null && getChildAt(index) instanceof ImageView) {
                    ImageView view = (ImageView) getChildAt(index);
                    view.setImageResource(mNormalResource);
                }
            }
        }
        if (getChildAt(currentPos) != null && getChildAt(currentPos) instanceof ImageView) {
            ImageView view = (ImageView) getChildAt(currentPos);
            view.setImageResource(mSelectedResource);
        }
    }
}
