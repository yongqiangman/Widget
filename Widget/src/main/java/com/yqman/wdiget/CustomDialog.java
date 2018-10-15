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

/**
 * Created by manyongqiang on 2018/2/5.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by manyongqiang on 2017/7/13.
 * 此处的{@link CustomDialog}更新了底部取消和确认按钮的显示样式
 * {@link CustomDialog.Builder#setCustomViewId(int)}支持自定义View样式
 * {@link CustomDialog}提供多余6个采用滚动条的方式展示。
 */

public class CustomDialog implements View.OnClickListener {
    private Dialog mDialog;
    private FrameLayout mCustomLayout;

    private Context mContext;
    private View mTitleContentView;
    private TextView mTitleTV;
    private TextView mSubTitleTV;
    private TextView mContentTextTV;
    private TextView mBottomTitleTV;

    private View mTwoButtonLayoutView;
    private Button mCancelBtn;
    private Button mConfirmBtn;

    private View mOneButtonLayout;
    private Button mSingleConfirmBtn;
    private ImageView mImageCloseView;

    private OnClickListener mBottomTextViewListener;
    private OnClickListener mCancelListener;
    private OnClickListener mConfirmListener;
    private OnCancelShowDialogListener mCancelShowDialogListener;

    private OnClickListener mSingleConfirmListener;
    private OnCreateCustomViewListener mCreateCustomViewListener;
    private DialogInterface.OnShowListener mShowListener;
    private DialogInterface.OnDismissListener mDismissListener;

    private String mTitle;
    private String mSubTitle;
    private String mContentText;
    private String mBottomText;
    private String mCancelText;
    private String mConfirmText;
    private String mSingleConfirmText;
    private int mCustomViewId = -1;
    private boolean mCancelOnTouchOutside = false;
    private boolean mNeedShiedReturnKey = false;
    private boolean mNeedShowCloseImage = false;

    private CustomDialog() {

    }

    private boolean isNotEmptyString(String checkStr) {
        return checkStr == null || checkStr.isEmpty();
    }

    private void init() {
        mDialog = new Dialog(mContext, R.style.YQMAN_CustomDialogTheme);
        initDialogView();
        // 设置对话框内容
        if (isNotEmptyString(mTitle)) {
            mTitleContentView.setVisibility(View.VISIBLE);
            mTitleTV.setVisibility(View.VISIBLE);
            mTitleTV.setText(mTitle);
        }
        if (isNotEmptyString(mSubTitle)) {
            mSubTitleTV.setVisibility(View.VISIBLE);
            mSubTitleTV.setText(mSubTitle);
        }
        if (isNotEmptyString(mContentText)) {
            mContentTextTV.setVisibility(View.VISIBLE);
            mContentTextTV.setText(mContentText);
        }
        if (isNotEmptyString(mBottomText)) {
            mBottomTitleTV.setVisibility(View.VISIBLE);
            mBottomTitleTV.setText(mBottomText);
        }
        if (isNotEmptyString(mCancelText)) {
            mTwoButtonLayoutView.setVisibility(View.VISIBLE);
            mCancelBtn.setText(mCancelText);
        }
        if (isNotEmptyString(mConfirmText)) {
            mTwoButtonLayoutView.setVisibility(View.VISIBLE);
            mConfirmBtn.setText(mConfirmText);
        }
        if (isNotEmptyString(mSingleConfirmText)) {
            mOneButtonLayout.setVisibility(View.VISIBLE);
            mSingleConfirmBtn.setText(mSingleConfirmText);
        }

        if (mNeedShowCloseImage) {
            mImageCloseView.setVisibility(View.VISIBLE);
            mImageCloseView.setOnClickListener(this);
        }

        if (mCustomViewId != -1) { // 默认-1
            mCustomLayout.setVisibility(View.VISIBLE);
            View customView = LayoutInflater.from(mContext).inflate(mCustomViewId, mCustomLayout);
            if (mCreateCustomViewListener != null) {
                mCreateCustomViewListener.onCreate(customView);
            }
        }

        if (mShowListener != null) {
            mDialog.setOnShowListener(mShowListener);
        }
        if (mDismissListener != null) {
            mDialog.setOnDismissListener(mDismissListener);
        }
        // cancelOnTouchOutside默认设置false 即点击对话框外面不关闭对话框
        mDialog.setCanceledOnTouchOutside(mCancelOnTouchOutside);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                notifyCloseDialogByUser();
            }
        });
        // needShiedReturnKey默认false 不屏蔽返回按键，即点击返回按键关闭对话框
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // 弹出该对话框时屏蔽返回按键
                if (!mNeedShiedReturnKey && mCancelShowDialogListener != null) {
                    notifyCloseDialogByUser();
                }
                return (mNeedShiedReturnKey && keyCode == KeyEvent.KEYCODE_BACK);
            }
        });
    }

    private void initDialogView() {
        View contentView;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.yqman_widget_custom_dialog, null);

        mTitleContentView = contentView.findViewById(R.id.yqman_widget_title_content);
        mTitleTV = (TextView) contentView.findViewById(R.id.title);
        mSubTitleTV = (TextView) contentView.findViewById(R.id.yqman_widget_sub_title);
        mContentTextTV = (TextView) contentView.findViewById(R.id.yqman_widget_content_text);
        mBottomTitleTV = (TextView) contentView.findViewById(R.id.yqman_widget_bottom_title);
        mBottomTitleTV.setOnClickListener(this);

        mTwoButtonLayoutView = contentView.findViewById(R.id.yqman_widget_bottom_two_button_layout);
        mCancelBtn = (Button) contentView.findViewById(R.id.yqman_widget_cancel);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn = (Button) contentView.findViewById(R.id.yqman_widget_confirm);
        mConfirmBtn.setOnClickListener(this);
        mImageCloseView = (ImageView) contentView.findViewById(R.id.yqman_widget_img_close);

        mOneButtonLayout = contentView.findViewById(R.id.yqman_widget_bottom_one_button_layout);
        mSingleConfirmBtn = (Button) contentView.findViewById(R.id.yqman_widget_single_confirm_button);
        mSingleConfirmBtn.setOnClickListener(this);

        mCustomLayout = (FrameLayout) contentView.findViewById(R.id.yqman_widget_customContent);

        mDialog.setContentView(contentView);
        // 设置对话框属性
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = mContext.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setGravity(Gravity.CENTER);
            mDialog.getWindow().setWindowAnimations(R.style.YQMAN_CustomDialog_Animation);
        }
    }

    public interface OnClickListener {
        void onClick();
    }

    /**
     * 在dialog显示的时候用户点击屏幕外面，或者返回按键时触发的监听器
     */
    public interface OnCancelShowDialogListener {
        void onCancelShowDialog();
    }

    public interface OnCreateCustomViewListener {
        void onCreate(View customView);
    }

    private void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * 用户点击对话框的右上角、点击dialog外部区域、点击返回按键导致对话框消失触发该方法，告知用户
     */
    private void notifyCloseDialogByUser() {
        if (mCancelShowDialogListener != null) {
            mCancelShowDialogListener.onCancelShowDialog();
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.yqman_widget_bottom_title) {
            if (mBottomTextViewListener != null) {
                mBottomTextViewListener.onClick();
            }
        } else if (v.getId() == R.id.yqman_widget_cancel) {
            if (mCancelListener != null) {
                mCancelListener.onClick();
            }
        } else if (v.getId() == R.id.yqman_widget_confirm) {
            if (mConfirmListener != null) {
                mConfirmListener.onClick();
            }
        } else if (v.getId() == R.id.yqman_widget_single_confirm_button) {
            if (mSingleConfirmListener != null) {
                mSingleConfirmListener.onClick();
            }
        } else if (v.getId() == R.id.yqman_widget_img_close) {
            notifyCloseDialogByUser();
        }

    }

    public static class Builder {
        private CustomDialog mEvanDialog = new CustomDialog();

        public Builder(Context context) {
            mEvanDialog.mContext = context;
        }

        /**
         * 设置显示dialog的关闭ImageView；
         */
        public Builder needShowCloseImageView() {
            mEvanDialog.mNeedShowCloseImage = true;
            return this;
        }

        public Builder setTitle(int resId) {
            mEvanDialog.mTitle = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setTitle(String title) {
            mEvanDialog.mTitle = title;
            return this;
        }

        public Builder setSubTitle(int resId) {
            mEvanDialog.mSubTitle = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setSubTitle(String subTitle) {
            mEvanDialog.mSubTitle = subTitle;
            return this;
        }

        public Builder setContentText(int resId) {
            mEvanDialog.mContentText = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setContentText(String contentText) {
            mEvanDialog.mContentText = contentText;
            return this;
        }

        public Builder setBottomText(int resId) {
            mEvanDialog.mBottomText = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setBottomText(String bottomText) {
            mEvanDialog.mBottomText = bottomText;
            return this;
        }

        public Builder setCancelText(int resId) {
            mEvanDialog.mCancelText = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setCancelText(String cancelText) {
            mEvanDialog.mCancelText = cancelText;
            return this;
        }

        public Builder setConfirmText(int resId) {
            mEvanDialog.mConfirmText = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setConfirmText(String confirmText) {
            mEvanDialog.mConfirmText = confirmText;
            return this;
        }

        public Builder setSingleConfirmText(int resId) {
            mEvanDialog.mSingleConfirmText = mEvanDialog.mContext.getResources().getString(resId);
            return this;
        }

        public Builder setSingleConfirmText(String singleConfirmText) {
            mEvanDialog.mSingleConfirmText = singleConfirmText;
            return this;
        }

        public Builder setCancelListener(OnClickListener cancelListener) {
            mEvanDialog.mCancelListener = cancelListener;
            return this;
        }

        public Builder setConfirmListener(OnClickListener confirmListener) {
            mEvanDialog.mConfirmListener = confirmListener;
            return this;
        }

        public void setBottomTextViewListener(OnClickListener bottomTextViewListener) {
            mEvanDialog.mBottomTextViewListener = bottomTextViewListener;
        }

        public Builder setSingleConfirmListener(OnClickListener singleConfirmListener) {
            mEvanDialog.mSingleConfirmListener = singleConfirmListener;
            return this;
        }

        public Builder setShowListener(DialogInterface.OnShowListener showListener) {
            mEvanDialog.mShowListener = showListener;
            return this;
        }

        public Builder setDismissListener(DialogInterface.OnDismissListener dismissListener) {
            mEvanDialog.mDismissListener = dismissListener;
            return this;
        }

        public Builder setCancelShowDialogListener(
                OnCancelShowDialogListener cancelShowDialogListener) {
            mEvanDialog.mCancelShowDialogListener = cancelShowDialogListener;
            return this;
        }

        public Builder setCreateCustomViewListener(
                OnCreateCustomViewListener createCustomViewListener) {
            mEvanDialog.mCreateCustomViewListener = createCustomViewListener;
            return this;
        }

        public Builder setCustomViewId(int customViewId) {
            mEvanDialog.mCustomViewId = customViewId;
            return this;
        }

        public Builder setCancelOnTouchOutside(boolean cancelOnTouchOutside) {
            mEvanDialog.mCancelOnTouchOutside = cancelOnTouchOutside;
            return this;
        }

        public Builder setNeedShiedReturnKey(boolean needShiedReturnKey) {
            mEvanDialog.mNeedShiedReturnKey = needShiedReturnKey;
            return this;
        }

        public Dialog show() {
            mEvanDialog.init();
            mEvanDialog.mDialog.show();
            return mEvanDialog.mDialog;
        }
    }
}
