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

import java.util.ArrayList;

import com.yqman.wdiget.recyclerView.item.BaseLoadMoreFooter;
import com.yqman.wdiget.recyclerView.item.BaseRefreshHeader;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class BaseRecyclerViewAdapter<M>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 头、尾View容器
     */
    private final int HEAD_TYPE_FLAG = Integer.MIN_VALUE;
    private final int FOOTER_TYPE_FLAG = Integer.MIN_VALUE + 1000;
    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private ArrayList<View> mFooterViews = new ArrayList<>();

    /**
     * 上拉、下拉刷新视图
     */
    private BaseRefreshHeader mBaseRefreshHeader;
    private BaseLoadMoreFooter mBaseLoadMoreFooter;
    /**
     * 没有更多内容的视图
     */
    private View mNoMoreFooter;

    private boolean mEnableLoadMoreEvent = false;
    /**
     * Item数据监视器
     */
    protected ItemClickListener<M> mItemClickListener;

    public interface ItemClickListener<E> {
        void onItemClick(@NonNull E item);
    }

    public void setItemClickListener(ItemClickListener<M> itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void addHeaderView(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
        mHeaderViews.add(view);
    }

    public int getHeaderViewLength() {
        return mHeaderViews.size();
    }

    public void addFooterView(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
        if (isLastFootViewEqualMoreSate()) {
            mFooterViews.add(mFooterViews.size() - 1, view);
        } else {
            mFooterViews.add(view);
        }
    }

    public int getFooterViewLength() {
        return mFooterViews.size();
    }

    /**
     * 下拉上拉刷新操作
     */
    public void setRefreshHeader(@NonNull BaseRefreshHeader refreshHeader) {
        if (this.mBaseRefreshHeader != null) {
            mHeaderViews.remove(0);
        }
        this.mBaseRefreshHeader = refreshHeader;
        View view = refreshHeader.getView();
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
        mHeaderViews.add(0, view);
    }

    public BaseRefreshHeader getRefreshHeader() {
        return mBaseRefreshHeader;
    }

    public void setLoadMoreFooter(@NonNull BaseLoadMoreFooter baseLoadMoreFooter) {
        mBaseLoadMoreFooter = baseLoadMoreFooter;
        View view = baseLoadMoreFooter.getView();
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setNoMoreFooter(@NonNull View view) {
        mNoMoreFooter = view;
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public BaseLoadMoreFooter getLoadMoreFooter() {
        return mEnableLoadMoreEvent ? mBaseLoadMoreFooter : null;
    }

    /**
     * 开启上拉加载更多的选项
     */
    public void enableLoadMoreEvent(boolean enable) {
        mEnableLoadMoreEvent = enable;
        if (isLastFootViewEqualMoreSate()) {
            mFooterViews.remove(mFooterViews.size() - 1);
        }
        if (mEnableLoadMoreEvent) {
            if (mBaseLoadMoreFooter != null) {
                mFooterViews.add(mBaseLoadMoreFooter.getView());
            }
        } else {
            if (mNoMoreFooter != null) {
                mFooterViews.add(mNoMoreFooter);
            }
        }
    }

    private boolean isLastFootViewEqualMoreSate() {
        View lastView = mFooterViews.size() > 0 ? mFooterViews.get(mFooterViews.size() - 1) : null;
        if (lastView == null) {
            return false;
        }
        if (lastView == mNoMoreFooter) { // 最后一个是底线
            return true;
        }
        if (mBaseLoadMoreFooter != null && lastView == mBaseLoadMoreFooter.getView()) { // 最后一个是上拉加载更多
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return getChildrenItemCount() + mHeaderViews.size() + mFooterViews.size();
    }

    protected abstract int getChildrenItemCount();

    @Override
    public int getItemViewType(int position) {
        if (mHeaderViews.size() > 0 && position < mHeaderViews.size()) {
            return HEAD_TYPE_FLAG + position;
        }
        if (mFooterViews.size() > 0 && position > getChildrenItemCount() - 1 + mHeaderViews.size()) {
            int footerPosition = position - (getChildrenItemCount() + mHeaderViews.size());
            return FOOTER_TYPE_FLAG + footerPosition;
        }
        return getItemChildrenViewType(position);
    }

    /**
     * 留给子类自定义ViewHolder类型，返回值必须为正整数
     */
    protected int getItemChildrenViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType >= 0) {
            return onCreateChildrenViewHolder(parent, viewType);
        }
        if (viewType < FOOTER_TYPE_FLAG) {
            int headPosition = viewType - HEAD_TYPE_FLAG;
            return new RecyclerView.ViewHolder(mHeaderViews.get(headPosition)){}; //从0开始
        }
        int footerPosition = viewType - FOOTER_TYPE_FLAG;
        return new RecyclerView.ViewHolder(mFooterViews.get(footerPosition)){}; //从0开始
    }

    protected abstract RecyclerView.ViewHolder onCreateChildrenViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * 获取到当前item的ViewHolder对应的位置，减去了head的内容
     * @param viewHolder 当前要计算的viewHolder
     */
    protected int getItemPosition(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getLayoutPosition() - getHeaderViewLength();
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) >= 0) {
            onBindChildrenViewHolder(holder, position - mHeaderViews.size());
        }
    }

    protected abstract void onBindChildrenViewHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    /**
     * 对GridLayoutManager的处理
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) >= 0) {
                        return 0;
                    } else {
                        return gridLayoutManager.getSpanCount();
                    }
                }
            });
        }
    }

    /**
     * 对StaggeredGridLayoutManager的处理
     */
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
            int position = holder.getLayoutPosition();
            if (getItemViewType(position) < 0) {
                lp.setFullSpan(true);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (getItemViewType(position) >= 0) {
            position = position - mHeaderViews.size();
            onChildrenViewDetachedFromWindow(holder, position);
        }
    }

    protected void onChildrenViewDetachedFromWindow(RecyclerView.ViewHolder holder, int position) { }
}