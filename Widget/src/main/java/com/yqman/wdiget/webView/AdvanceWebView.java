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

package com.yqman.wdiget.webView;

import com.yqman.wdiget.CustomDialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by yqman on 2016/6/20.
 * 一个自定义的对WebView进行了常规设置的类封装
 */
public class AdvanceWebView extends WebView {
    private WebSettings mWebSettings;

    public AdvanceWebView(Context context) {
        this(context, null);
    }

    public AdvanceWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WebViewClient webViewClient = buildWebViewClient();
        WebChromeClient webChromeClient = buildWebChromeClient(context);
        mWebSettings = getSettings();
        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        simpleConfigure();
        android.webkit.CookieSyncManager.createInstance(context.getApplicationContext());
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
    }

    protected WebViewClient buildWebViewClient() {
        return new SimpleWebViewClient();
    }

    protected WebChromeClient buildWebChromeClient(Context context) {
        return new SimpleWebChromeClient(context);
    }

    private void simpleConfigure() {
        mWebSettings.setJavaScriptEnabled(true);//支持JavaScript

        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);

        mWebSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(false);

        mWebSettings.setAppCacheEnabled(true);//允许使用AppCache
        String appCacheDir =
                getContext().getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();//获取缓存地址
        mWebSettings.setAppCachePath(appCacheDir);//设置缓存地址
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//设置缓存文件的权限

        mWebSettings.setAllowFileAccess(true);//允许访问文件数据
        mWebSettings.setAllowContentAccess(true);

        mWebSettings.setBlockNetworkImage(false); //是否加载网络图片
        mWebSettings.setDomStorageEnabled(true);//允许使用DOM Storage

        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    /**
     * Created by yqman on 2016/6/20.
     * 处理全屏、位置信息、警告弹窗等情况
     */
    private class SimpleWebChromeClient extends WebChromeClient {
        private Context mContext;

        private SimpleWebChromeClient(Context context) {
            mContext = context;
        }

        //Step1首先调用这个方法
        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
        }

        //Android中处理JS的警告
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            //构建一个Builder来显示网页中的对话框
            new CustomDialog.Builder(mContext)
                    .setTitle("Alert")
                    .setContentText(message)
                    .setSingleConfirmText(android.R.string.ok)
                    .setSingleConfirmListener(new CustomDialog.OnClickListener() {
                        @Override
                        public void onClick() {
                            result.confirm();
                        }
                    })
                    .setCancelOnTouchOutside(false)
                    .setNeedShiedReturnKey(true)
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new CustomDialog.Builder(mContext)
                    .setTitle("Confirm")
                    .setContentText(message)
                    .setSingleConfirmText(android.R.string.ok)
                    .setSingleConfirmListener(new CustomDialog.OnClickListener() {
                        @Override
                        public void onClick() {
                            result.confirm();
                        }
                    })
                    .setCancelOnTouchOutside(false)
                    .setNeedShiedReturnKey(true)
                    .show();
            return true;
        }

        //网页加载过程中不断调用这个方法，newProgress取值范围0~100
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

            if (mContext instanceof Activity) {
                Activity activity = (Activity) mContext;
                int data = newProgress * 100;
                if (data == 10000) {
                    activity.setProgressBarIndeterminateVisibility(false);
                } else {
                    activity.setProgressBarIndeterminateVisibility(true);
                    activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, data);
                }
            }
        }

        //设置Title，给Activity的ActionBar设置值
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        //设置icon，有些网页会返回图标
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }

        // 在Android N and later releases (API level > M) 版本中，
        // 如果html5期望访问客户的地理位置信息，那么链接必须是https协议，否则系统默认禁止这样的行为。
        // 既不会调用下面的方法
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        //控制台消息,如打开百度有如下该方法会接收到如下的一个message：“The page at 'https://www.baidu.com/' was loaded over HTTPS”
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }

        //点击html中的<input type="file"...>标签时调用该方法
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }

        /**
         * 为了支持全屏,重写如下两个方法,即进行一些形如activity.setContentView的方法调用
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

        }

        /**
         * 恢复activity原本显示的View，activity.setContentView
         */
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }
    }

    /**
     * Created by yqman on 2016/6/20.
     * 处理网页加载方式，监听网页加载前后的动作
     */
    private class SimpleWebViewClient extends WebViewClient {

        /**
         * 针对点击网页中的链接时会调用该方法
         *
         * @return true 自行处理， false系统处理
         * true ：
         * 交给其它应用打开,如浏览器
         * Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
         * activity.startActivity(intent);
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        //页面开始加载前
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        //页面加载完毕
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        /**
         * 该方法之后就是执行onLoadResource方法。打开一个网页可能会多次调用这个方法，因为有页面的内容有需要网上加载资源，如png
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        /**
         * 紧跟shouldInterceptRequest方法 加载资源
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        /**
         * 该方法会在对webView进行放大缩小时调用，取值范围是1~15
         */
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }
    }
}
