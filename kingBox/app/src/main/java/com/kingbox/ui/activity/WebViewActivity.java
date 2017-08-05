package com.kingbox.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingbox.R;
import com.kingbox.jsbridge.BridgeWebView;
import com.kingbox.jsbridge.DefaultHandler;
import com.kingbox.utils.Config;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 广告页(banner)
 * Created by Administrator on 2016/11/9.
 */
public class WebViewActivity extends BaseActivity {

    /**
     * 标题
     */
    @BindView(R.id.center_title_tv)
    TextView titleTV = null;

    @BindView(R.id.user_img)
    ImageView userImg;

    @BindView(R.id.play_img)
    ImageView playImg;

    @BindView(R.id.webview)
    BridgeWebView webView = null; // 网页显示的web

    @BindView(R.id.top_layout)
    RelativeLayout bannerTopLayout = null;

    @BindView(R.id.bottom_layout)
    RelativeLayout bottomLayout = null;

    @BindView(R.id.video_fullView)
    FrameLayout video_fullView = null;

    private View xCustomView;
    private WebChromeClient.CustomViewCallback xCustomViewCallback;
    private myWebChromeClient xwebchromeclient;

    private String title = "";
    private String lastTitle = "";  // 记录上一次标题，用于返回处理标题显示(内嵌网页的情况)

    private int RESULT_CODE = 0;

    private ValueCallback<Uri> mUploadMessage;

    //private Banner banner;
    //private Boolean isExit;

    private String url = "";

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        userImg.setVisibility(View.GONE);

        Intent intent = getIntent();
        //banner = (Banner) intent.getSerializableExtra("banner");
        //isExit = intent.getBooleanExtra("isExit", false);
        url = intent.getStringExtra("url");
        type = intent.getStringExtra("type");
        if (TextUtils.isEmpty(type)) {
            playImg.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.VISIBLE);
        }
        initView(url);
    }

    private void initView(String link) {
        if (!TextUtils.isEmpty(link)) {

            // WebView cookies清理
            CookieSyncManager.createInstance(this);
            CookieSyncManager.getInstance().startSync();
            CookieManager.getInstance().removeSessionCookie();

            // 清理cache 和历史记录
            webView.clearCache(true);
            webView.clearHistory();

            WebSettings webSettings = webView.getSettings();
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setJavaScriptEnabled(true);
            // 设置可以访问文件
            webSettings.setAllowFileAccess(true);
            // 设置默认缩放方式尺寸是far
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            // 支持缩放
            webSettings.setSupportZoom(true);
            // 缩放按钮
            webSettings.setBuiltInZoomControls(true);
            webSettings.setUseWideViewPort(true);// 这个很关键
            webSettings.setLoadWithOverviewMode(true);

            webView.setInitialScale(-1);// 为25%，最小缩放等级
            xwebchromeclient = new myWebChromeClient();
            webView.setWebChromeClient(xwebchromeclient);
            webView.setDownloadListener(new DownloadListener() {

                @Override
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
                    // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

            webView.setDefaultHandler(new DefaultHandler());

            webView.loadUrl(link);
        }
    }

    @OnClick({R.id.play_img, R.id.back_img, R.id.refresh_img, R.id.favourite_img, R.id.home_img})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_img:   // 播放
                String url = "http://list.donewe.com/kkflv/index.php?url=" + webView.getUrl();
                webView.loadUrl(url);

                //webView.loadUrl("http://list.donewe.com/kkflv/index.php?url=http://www.iqiyi.com/v_19rr7tg8js.html?fc=87bbded392d221f5");
                //webView.loadUrl("http://list.donewe.com/kkflv/index.php?url=http://www.iqiyi.com/v_19rr75dxvw.html?fc=87bbded392d221f5");
                break;
            case R.id.back_img:  // 返回
                if (webView.canGoBack()/* && !isExit*/) {
                    webView.goBack();

                    // 处理返回时标题显示问题(内嵌网页的情况)
                    title = lastTitle;
                    titleTV.setText(title);
                } else {
                    finish();
                }
                break;
            case R.id.refresh_img:   // 刷新
                webView.loadUrl(webView.getUrl());
                break;
            case R.id.favourite_img:    // 收藏
                break;
            case R.id.home_img:   // 主页
                Config.isBackHome = true;
                finish();
                break;
        }
    }

    public class myWebChromeClient extends WebChromeClient {

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
            this.openFileChooser(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
            this.openFileChooser(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            pickFile();
        }

        // 播放网络视频时全屏会被调用的方法
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            webView.setVisibility(View.INVISIBLE);
            // 如果一个视图已经存在，那么立刻终止并新建一个
            if (xCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            bannerTopLayout.setVisibility(View.GONE);
            video_fullView.addView(view);
            xCustomView = view;
            xCustomViewCallback = callback;
            video_fullView.setVisibility(View.VISIBLE);
        }

        // 视频播放退出全屏会被调用的
        @Override
        public void onHideCustomView() {
            if (xCustomView == null)// 不是全屏播放状态
                return;
            bannerTopLayout.setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            xCustomView.setVisibility(View.GONE);
            video_fullView.removeView(xCustomView);
            xCustomView = null;
            video_fullView.setVisibility(View.GONE);
            xCustomViewCallback.onCustomViewHidden();
            webView.setVisibility(View.VISIBLE);
        }

        // 获取标题
        @Override
        public void onReceivedTitle(WebView view, String titles) {
            super.onReceivedTitle(view, titles);
            lastTitle = title;
            // 获取网页标题
            title = titles;
            titleTV.setText(title);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (consoleMessage.message().contains("Uncaught ReferenceError")) {
                /*if (null == meet) {
                    meet = new Meet();
                }
                meet.setMeetTitle(title);
                meet.setShareImgUrl(advPic);
                meet.setShareLink(advLink);
                meet.setMid(advMid);

                if(shareFlag){
                    shareFlag = false;
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = SHARE_CODE; // 成功标识
                            handler.handleMessage(message);
                        }
                    }, 400);
                }*/
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    private void deleteCacheFolder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearCacheFolder(WebViewActivity.this, getCacheDir(),
                        System.currentTimeMillis());
            }
        }).start();
    }

    /**
     * 清除内存中缓存数据
     *
     * @param context
     * @param dir
     * @param numDays
     * @return
     */
    public static int clearCacheFolder(Context context, File dir, long numDays) {
        context.deleteDatabase("webview.db");
        context.deleteDatabase("webviewCache.db");
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(context, child,
                                numDays);
                    }
                    if (child.lastModified() < numDays) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.setVisibility(View.GONE);
            webView.destroy();
        }
        deleteCacheFolder();
        video_fullView.removeAllViews();
        super.onDestroy();
    }

    @Override
    public void finish() {

        // 处理webView放大缩小的广播崩溃问题
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        if (null != view) {
            view.removeAllViews();
        }
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 判断是否是全屏
     *
     * @return
     */
    public boolean inCustomView() {
        return (xCustomView != null);
    }

    /**
     * 全屏时按返加键执行退出全屏方法
     */
    public void hideCustomView() {
        xwebchromeclient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /* 返回键的捕捉 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (inCustomView()) {
                hideCustomView();
                return true;
            } else if (webView.canGoBack() /*&& !isExit*/) {
                webView.goBack();
                return true;
            } else {
                finish();
                return false;
            }
        }
        return false; // false 是不管
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }
}
