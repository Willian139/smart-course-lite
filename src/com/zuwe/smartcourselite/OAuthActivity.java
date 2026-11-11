package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** 官方登录页：无标题栏的 WebView 容器。 */
public final class OAuthActivity extends Activity {
    private static final String ENTRY = "https://skl.zuwe.edu.cn/";

    private boolean completed;
    private TextView hint;

    @Override
    protected void onCreate(Bundle bundle) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(244, 242, 236));
        root.setFitsSystemWindows(true);

        this.hint = new TextView(this);
        this.hint.setText("正在打开智慧思政官方登录入口…\n请在页面内按平台正常流程完成微信授权。授权成功后，本应用会自动接收并加密保存凭证。");
        this.hint.setTextColor(Color.rgb(48, 47, 44));
        this.hint.setTextSize(12f);
        this.hint.setPadding(dp(20f), dp(18f), dp(20f), dp(14f));
        root.addView(this.hint, new LinearLayout.LayoutParams(-1, -2));

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3f)));

        WebView webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        PageMotion.installBackdrop(this, Color.rgb(244, 242, 236));
        PageMotion.fromBottom(root, 620L);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return captureOrContinue(request.getUrl().toString(), view);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                captureOrContinue(url, view);
            }

            private boolean captureOrContinue(String url, WebView view) {
                if (OAuthActivity.this.completed) {
                    return true;
                }
                if (url.startsWith("weixin://")) {
                    try {
                        OAuthActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception e) {
                        OAuthActivity.this.hint.setText("需要安装微信才能继续官方授权。");
                    }
                    return true;
                }
                String authorization = OAuthCallback.authorizationFromUrl(url);
                if (OAuthCallback.looksLikeJwt(authorization)) {
                    OAuthActivity.this.completed = true;
                    try {
                        String cookies = "";
                        try {
                            cookies = CookieManager.getInstance().getCookie("https://skl.zuwe.edu.cn");
                        } catch (Exception ignored) {
                        }
                        AuthManager.saveFromWeb(OAuthActivity.this, authorization, cookies);
                        OAuthActivity.this.hint.setText("授权成功，凭证已加密保存。正在返回课表…");
                        OAuthActivity.this.setResult(RESULT_OK);
                        OAuthActivity.this.finish();
                    } catch (Exception e) {
                        OAuthActivity.this.hint.setText("已收到授权结果，但保存失败：" + e.getMessage());
                    }
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(ENTRY);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
