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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 使用该Dialog需要注意Activity不应该有如下配置，否者横竖屏切换过程中容易导致控件显示异常
 * android:configChanges="orientation|keyboardHidden|screenSize"
 */

public class DatePickView extends FrameLayout {
    private static final String TAG = "DatePickView";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private ScrollPickView mYearSelection;
    private ScrollPickView mMonthSelection;
    private ScrollPickView mDayOfMonthSelection;
    private int mEndYear;
    private int mStartYear;
    private int mEndMonth;
    private int mStartMonth;
    private int mEndDay;
    private int mStartDay;
    private boolean mIsLimitedDate = false;

    public DatePickView(@NonNull Context context) {
        this(context, null);
    }

    public DatePickView(@NonNull Context context,
                        @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePickView(@NonNull Context context,
                        @Nullable AttributeSet attrs,
                        @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.yqman_widget_date_pick_layout, this, true);
        initView(contentView);
        initDate();
    }

    private void initView(View contentView) {
        mYearSelection = (ScrollPickView) contentView.findViewById(R.id.year);
        mYearSelection.setOnValueChangedListener(new ScrollPickView.OnValueChangedListener() {
            @Override
            public void onValueChange(int value) {
                refreshDate();
            }
        });
        mYearSelection.setFormatter(new ScrollPickView.Formatter() {
            @Override
            public String getFormatString(int value) {
                return value + "年";
            }
        });
        mMonthSelection = (ScrollPickView) contentView.findViewById(R.id.month);
        mMonthSelection.setOnValueChangedListener(new ScrollPickView.OnValueChangedListener() {
            @Override
            public void onValueChange(int value) {
                refreshDate();
            }
        });
        mMonthSelection.setFormatter(new ScrollPickView.Formatter() {
            @Override
            public String getFormatString(int value) {
                return value + "月";
            }
        });
        mDayOfMonthSelection = (ScrollPickView) contentView.findViewById(R.id.dayOfMonth);
        mDayOfMonthSelection.setOnValueChangedListener(new ScrollPickView.OnValueChangedListener() {
            @Override
            public void onValueChange(int value) {

            }
        });
        mDayOfMonthSelection.setFormatter(new ScrollPickView.Formatter() {
            @Override
            public String getFormatString(int value) {
                return value + "日";
            }
        });
    }

    private void initDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mYearSelection.setInitValue(year);
        mMonthSelection.setInitValue(month); // Calendar.MONTH 从0开始；
        mDayOfMonthSelection.setValueRange(1, getMaxDay());
        mDayOfMonthSelection.setInitValue(day);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mYearSelection.setInitValue(savedState.mYear);
        mMonthSelection.setInitValue(savedState.mMonth);
        mDayOfMonthSelection.setValueRange(1, getMaxDay());
        mDayOfMonthSelection.setInitValue(savedState.mDay);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.mYear = mYearSelection.getValue();
        savedState.mMonth = mMonthSelection.getValue();
        savedState.mDay = mDayOfMonthSelection.getValue();
        return savedState;
    }

    public void setDateRange(Calendar startCalendar, Calendar endCalendar) {
        if (startCalendar == null || endCalendar == null) {
            return;
        }
        if (startCalendar.getTime().getTime() > endCalendar.getTime().getTime()) {
            return;
        }
        mStartDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        mStartMonth = startCalendar.get(Calendar.MONTH) + 1;
        mStartYear = startCalendar.get(Calendar.YEAR);

        mEndDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        mEndMonth = endCalendar.get(Calendar.MONTH) + 1;
        mEndYear = endCalendar.get(Calendar.YEAR);
        mYearSelection.setValueRange(mStartYear, mEndYear);
        mIsLimitedDate = true;
        refreshDate(); // 手动触发一下日期控件的刷新显示范围
    }

    public void setDate(Calendar calendar) {
        if (mIsLimitedDate) { // 手动触发一下日期控件的刷新显示范围
            refreshDate();
        }

        mYearSelection.setInitValue(calendar.get(Calendar.YEAR));
        if (mIsLimitedDate) {  // setInit 方法不会触发监听器，所以需要手动触发一下日期控件的刷新显示范围
            refreshDate();
        }

        mMonthSelection.setInitValue(calendar.get(Calendar.MONTH) + 1); // Calendar.MONTH 从0开始；
        if (mIsLimitedDate) { // setInit 方法不会触发监听器，所以需要手动触发一下日期控件的刷新显示范围
            refreshDate();
        }
        mDayOfMonthSelection.setInitValue(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void enableLoopDisplay() {
        mYearSelection.turnOnLoop();
        mMonthSelection.turnOnLoop();
        mDayOfMonthSelection.turnOnLoop();
    }

    public void disableLoopDisplay() {
        mYearSelection.turnOffLoop();
        mMonthSelection.turnOffLoop();
        mDayOfMonthSelection.turnOffLoop();
    }

    public int getYear() {
        return mYearSelection.getValue();
    }

    /**
     * 从1开始
     */
    public int getMonth() {
        return mMonthSelection.getValue();
    }

    /**
     * 从1开始
     */
    public int getDayOfMonth() {
        return mDayOfMonthSelection.getValue();
    }

    public Calendar getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYearSelection.getValue());
        calendar.set(Calendar.MONTH, mMonthSelection.getValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, mDayOfMonthSelection.getValue());
        return calendar;
    }

    /**
     * 设置月日的显示范围，在设置了起止时间的时候起作用
     */
    private void refreshDate() {
        if (!mIsLimitedDate) { // 正常显示
            mMonthSelection.setValueRange(1, 12);
            mDayOfMonthSelection.setValueRange(1, getMaxDay());
            return;
        }
        if (mYearSelection.getValue() <= mStartYear) { // 到达开始日期
            if (mYearSelection.getValue() < mStartYear) {
                mYearSelection.setInitValue(mStartYear);
            }
            mMonthSelection.setValueRange(mStartMonth, 12);
            if (mMonthSelection.getValue() <= mStartMonth) {
                if (mMonthSelection.getValue() < mStartMonth) { // 未知错误修正
                    mMonthSelection.setInitValue(mStartMonth);
                }
                mDayOfMonthSelection.setValueRange(mStartDay, getMaxDay());
            } else {
                mDayOfMonthSelection.setValueRange(1, getMaxDay());
            }
            return;
        }
        if (mYearSelection.getValue() >= mEndYear) { // 到达截止日期
            if (mYearSelection.getValue() > mEndYear) {
                mYearSelection.setInitValue(mEndYear);
            }
            mMonthSelection.setValueRange(1, mEndMonth);
            if (mMonthSelection.getValue() >= mEndMonth) {
                if (mMonthSelection.getValue() > mEndMonth) {  // 未知错误修正
                    mMonthSelection.setInitValue(mEndMonth);
                }
                mDayOfMonthSelection.setValueRange(1, mEndDay);
            } else {
                mDayOfMonthSelection.setValueRange(1, getMaxDay());
            }
            return;
        }
        mMonthSelection.setValueRange(1, 12);
        mDayOfMonthSelection.setValueRange(1, getMaxDay());
    }

    private int getMaxDay() {
        int year = mYearSelection.getValue();
        int month = mMonthSelection.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DATE);
    }


    private static class SavedState extends BaseSavedState {
        private int mYear;
        private int mMonth;
        private int mDay;

        SavedState(Parcel source) {
            super(source);
            mYear = source.readInt();
            mMonth = source.readInt();
            mDay = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mYear);
            out.writeInt(mMonth);
            out.writeInt(mDay);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}