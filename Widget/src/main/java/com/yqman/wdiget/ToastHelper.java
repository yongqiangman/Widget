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
import android.util.Log;
import android.widget.Toast;

/**
 * Created by manyongqiang on 2018/2/5.
 */

public class ToastHelper {
    private static final String TAG = "ToastHelper";
    private volatile static Toast toast;
    private static int version = android.os.Build.VERSION.SDK_INT;
    private static final int MAX_NEED_CANCEL_VERSION = 10;

    public static void showToast(Context context, String text) {
        initToast(context, text);
        if (toast == null) {
            return;
        }
        toast.show();
    }

    public static void showToast(Context context, int resId) {
        initToast(context, resId);
        if (toast == null) {
            return;
        }
        toast.show();
    }

    private static void initToast(Context context, int resId) {
        initToast(context, context.getResources().getString(resId));
    }

    private static void initToast(Context context, String text) {
        if (toast == null) {
            if (context != null) {
                toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
        }

        try {
            if (toast != null) {
                toast.setText(text);
                if (version <= MAX_NEED_CANCEL_VERSION) {
                    toast.cancel();
                }
            }
        } catch (Exception ignore) {
            Log.d(TAG, " ignore:" + ignore.getMessage());
        }

    }
}
