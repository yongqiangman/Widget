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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yqman on 2016/5/12.
 */
public class SimpleLoadMoreFooter extends BaseLoadMoreFooter {
    private Context mContext;
    private LinearLayout mLinearLayout;

    public SimpleLoadMoreFooter(Context context) {
        mContext = context.getApplicationContext();
        initView();
    }

    private void initView() {
        mLinearLayout =
                (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.yqman_widget_view_footer_loadmore, null);
        TextView textView = mLinearLayout.findViewById(R.id.load_footer_textView);
        textView.setText(mContext.getResources().getString(R.string.yqman_widget_loading));
        mLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public View getView() {
        return mLinearLayout;
    }

    @Override
    public void completeLoadMore() {
        mLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void startLoadMore() {
        super.startLoadMore();
        mLinearLayout.setVisibility(View.VISIBLE);
    }
}