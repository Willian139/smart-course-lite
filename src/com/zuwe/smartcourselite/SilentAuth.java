package com.zuwe.smartcourselite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 静默续期：在隐藏的 WebView 里复用浏览器已有的 CAS 会话重新走一次单点登录。
 * 学校 JWT 的有效期很短，CAS 会话却可以长期保留，所以只要 Cookie 还在，
 * 这一步用户完全看不见；CAS 会话也没了才会落到登录表单，由调用方提示重新授权。
 */
public final class SilentAuth {

    /** CAS 登录入口，service 指回学校的 CAS 客户端，成功后会跳回带 token 的页面。 */
    private static final String CAS_ENTRY =
            "https://authserver6.zuwe.edu.cn/authserver/login?service="
                    + "https%3A%2F%2Fskl.zuwe.edu.cn%2Fskl%2Fcas%2Flogin";

    public interface Callback {
        void onResult(boolean ok);
    }

    private final Activity activity;
    private final ViewGroup host;
    private final Callback callback;
    private final Handler main = new Handler(Looper.getMainLooper());

    private WebView web;
    private boolean done;

    public SilentAuth(Activity activity, ViewGroup host, Callback callback) {
        this.activity = activity;
        this.host = host;
        this.callback = callback;
    }

    public void start(long timeoutMs) {
        if (this.host == null) {
            finish(false);
            return;
        }
        WebView view = new WebView(this.activity);
        this.web = view;
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(false);
        view.setAlpha(0.01f);
        this.host.removeAllViews();
        this.host.addView(view, new ViewGroup.LayoutParams(2, 2));
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, WebResourceRequest request) {
                return capture(request.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView wv, String url, Bitmap favicon) {
                super.onPageStarted(wv, url, favicon);
                capture(url);
            }

            @Override
            public void onPageFinished(WebView wv, String url) {
                super.onPageFinished(wv, url);
                capture(url);
            }
        });
        this.main.postDelayed(new Runnable() {
            @Override public void run() {
                finish(false);
            }
        }, timeoutMs);
        try {
            view.loadUrl(CAS_ENTRY);
        } catch (Exception e) {
            finish(false);
        }
    }

    /** 命中 token 即保存并结束；返回 true 表示这次跳转被我们吃掉了。 */
    private boolean capture(String url) {
        if (this.done || url == null) {
            return false;
        }
        String token = OAuthCallback.authorizationFromUrl(url);
        if (!OAuthCallback.looksLikeJwt(token)) {
            return false;
        }
        String cookie = "";
        try {
            cookie = CookieManager.getInstance().getCookie("https://skl.zuwe.edu.cn");
        } catch (Exception ignored) {
        }
        boolean saved = AuthManager.saveFromWeb(this.activity, token, cookie);
        finish(saved);
        return true;
    }

    private void finish(final boolean ok) {
        if (this.done) {
            return;
        }
        this.done = true;
        try {
            if (this.host != null) {
                this.host.removeAllViews();
            }
        } catch (Exception ignored) {
        }
        final WebView view = this.web;
        this.web = null;
        if (view != null) {
            this.main.post(new Runnable() {
                @Override public void run() {
                    try {
                        view.destroy();
                    } catch (Exception ignored) {
                    }
                }
            });
        }
        if (this.callback != null) {
            this.callback.onResult(ok);
        }
    }
}
