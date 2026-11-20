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

/* loaded from: classes.dex */
public final class SilentAuth {
    private static final String CAS_ENTRY = "https://authserver6.zuwe.edu.cn/authserver/login?service=https%3A%2F%2Fskl.zuwe.edu.cn%2Fskl%2Fcas%2Flogin";
    private final Activity activity;
    private final Callback callback;
    private boolean done;
    private final ViewGroup host;
    private final Handler main = new Handler(Looper.getMainLooper());
    private WebView web;

    public interface Callback {
        void onResult(boolean z);
    }

    public SilentAuth(Activity activity, ViewGroup viewGroup, Callback callback) {
        this.activity = activity;
        this.host = viewGroup;
        this.callback = callback;
    }

    public void start(long j) {
        if (this.host == null) {
            finish(false);
            return;
        }
        WebView webView = new WebView(this.activity);
        this.web = webView;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(false);
        webView.setAlpha(0.01f);
        this.host.removeAllViews();
        this.host.addView(webView, new ViewGroup.LayoutParams(2, 2));
        webView.setWebViewClient(new WebViewClient() { // from class: com.zuwe.smartcourselite.SilentAuth.1
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, WebResourceRequest webResourceRequest) {
                return SilentAuth.this.capture(webResourceRequest.getUrl().toString());
            }

            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView2, String str, Bitmap bitmap) {
                super.onPageStarted(webView2, str, bitmap);
                SilentAuth.this.capture(str);
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView2, String str) {
                super.onPageFinished(webView2, str);
                SilentAuth.this.capture(str);
            }
        });
        this.main.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.SilentAuth.2
            @Override // java.lang.Runnable
            public void run() {
                SilentAuth.this.finish(false);
            }
        }, j);
        try {
            webView.loadUrl(CAS_ENTRY);
        } catch (Exception unused) {
            finish(false);
        }
    }

    public boolean capture(String str) {
        String str2;
        if (this.done || str == null) {
            return false;
        }
        String authorizationFromUrl = OAuthCallback.authorizationFromUrl(str);
        if (!OAuthCallback.looksLikeJwt(authorizationFromUrl)) {
            return false;
        }
        try {
            str2 = CookieManager.getInstance().getCookie("https://skl.zuwe.edu.cn");
        } catch (Exception unused) {
            str2 = "";
        }
        finish(AuthManager.saveFromWeb(this.activity, authorizationFromUrl, str2));
        return true;
    }

    public void finish(boolean z) {
        if (this.done) {
            return;
        }
        this.done = true;
        try {
            ViewGroup viewGroup = this.host;
            if (viewGroup != null) {
                viewGroup.removeAllViews();
            }
        } catch (Exception unused) {
        }
        final WebView webView = this.web;
        this.web = null;
        if (webView != null) {
            this.main.post(new Runnable() { // from class: com.zuwe.smartcourselite.SilentAuth.3
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        webView.destroy();
                    } catch (Exception unused2) {
                    }
                }
            });
        }
        Callback callback = this.callback;
        if (callback != null) {
            callback.onResult(z);
        }
    }
}
