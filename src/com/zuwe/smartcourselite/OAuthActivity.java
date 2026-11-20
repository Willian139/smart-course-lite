package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/* loaded from: classes.dex */
public final class OAuthActivity extends Activity {
    private static final String ENTRY = "https://skl.zuwe.edu.cn/";
    private boolean completed;
    private TextView hint;
    private WebView webView;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(Color.rgb(244, 242, 236));
        linearLayout.setFitsSystemWindows(true);
        TextView textView = new TextView(this);
        this.hint = textView;
        textView.setText("正在打开智慧思政官方登录入口…\n请在页面内按平台正常流程完成微信授权。授权成功后，本应用会自动接收并加密保存凭证。");
        this.hint.setTextColor(Color.rgb(48, 47, 44));
        this.hint.setTextSize(12.0f);
        this.hint.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(14.0f));
        linearLayout.addView(this.hint, new LinearLayout.LayoutParams(-1, -2));
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        linearLayout.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(3.0f)));
        WebView webView = new WebView(this);
        this.webView = webView;
        linearLayout.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(linearLayout);
        PageMotion.installBackdrop(this, Color.rgb(244, 242, 236));
        PageMotion.fromBottom(linearLayout, 620L);
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(-1);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        settings.setMixedContentMode(0);
        this.webView.setWebViewClient(new WebViewClient() { // from class: com.zuwe.smartcourselite.OAuthActivity.1
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, WebResourceRequest webResourceRequest) {
                return OAuthActivity.this.handleUrl(webView2, webResourceRequest.getUrl().toString());
            }

            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView2, String str, Bitmap bitmap) {
                super.onPageStarted(webView2, str, bitmap);
                OAuthActivity.this.handleUrl(webView2, str);
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, int i, String str, String str2) {
                super.onReceivedError(webView2, i, str, str2);
                OAuthActivity.this.hint.setText("加载失败（" + i + "）：" + str + "，点此重试");
                OAuthActivity.this.hint.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.OAuthActivity.1.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        OAuthActivity.this.webView.loadUrl(OAuthActivity.ENTRY);
                        OAuthActivity.this.completed = false;
                        OAuthActivity.this.hint.setText("正在打开智慧思政官方登录入口…\n请在页面内按平台正常流程完成微信授权。授权成功后，本应用会自动接收并加密保存凭证。");
                        OAuthActivity.this.hint.setOnClickListener(null);
                    }
                });
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedSslError(WebView webView2, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
            }
        });
        this.webView.loadUrl(ENTRY);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handleUrl(WebView webView, String str) {
        String str2;
        if (this.completed) {
            return true;
        }
        if (str.startsWith("weixin://")) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
            } catch (Exception unused) {
                this.hint.setText("需要安装微信才能继续官方授权。");
            }
            return true;
        }
        String authorizationFromUrl = OAuthCallback.authorizationFromUrl(str);
        if (!OAuthCallback.looksLikeJwt(authorizationFromUrl)) {
            return false;
        }
        this.completed = true;
        try {
            str2 = CookieManager.getInstance().getCookie("https://skl.zuwe.edu.cn");
        } catch (Exception unused2) {
            str2 = "";
        }
        try {
            AuthManager.saveFromWeb(this, authorizationFromUrl, str2);
            this.hint.setText("授权成功，凭证已加密保存。正在返回课表…");
            setResult(-1);
            finish();
        } catch (Exception e) {
            this.hint.setText("已收到授权结果，但保存失败：" + e.getMessage());
        }
        return true;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            WebView webView = this.webView;
            if (webView != null && webView.canGoBack()) {
                this.webView.goBack();
                return true;
            }
            setResult(0);
            finish();
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        WebView webView = this.webView;
        if (webView != null) {
            webView.stopLoading();
            this.webView.setWebViewClient(null);
            ((ViewGroup) this.webView.getParent()).removeView(this.webView);
            this.webView.destroy();
            this.webView = null;
        }
        super.onDestroy();
    }

    private int dp(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }
}
