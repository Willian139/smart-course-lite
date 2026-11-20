package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 让教务系统在应用内的网页里自己完成登录：
 * 密码加密、验证码、统一身份认证全部交给网页。
 * 登录成功后有两条取数据的路：
 * 1) 取 Cookie 交给 JwApi 正常请求；
 * 2) 拿不到 Cookie 时，直接在这个网页里用 JS 发课表请求（用页面自己的会话）。
 */
public final class JwWebLogin {

    public static final String HOST = "https://ijw.zuwe.edu.cn";
    public static final String LOGIN_URL = HOST + "/jwglxt/xtgl/login_slogin.html";
    public static final String INDEX_URL = HOST + "/jwglxt/xtgl/index_initMenu.html?jsdm=xs&echarts=1";
    private static final String SCHEME = "jwdata:";

    private JwWebLogin() {
    }

    public interface Callback {
        void onCookie(String cookie);

        void onJson(String body);
    }

    public static void show(final JwUi ui, final Callback cb) {
        final Activity a = ui.activity();
        final JwApi api = ui.api();
        final Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(a, 10), dp(a, 10), dp(a, 10), dp(a, 10));

        TextView tip = new TextView(a);
        tip.setText("需连接校园网（宿舍 Wi-Fi / 手机流量打不开教务）。在此登录后点最下面的按钮取课表。");
        tip.setTextSize(11f);
        tip.setTextColor(0xFF5A6572);
        root.addView(tip);

        final TextView statusView = new TextView(a);
        statusView.setTextSize(11f);
        statusView.setTextColor(0xFF5A6572);
        statusView.setPadding(0, dp(a, 6), 0, dp(a, 4));
        statusView.setText("提示：登录成功后点最下面的按钮");

        final boolean[] pendingJs = new boolean[1];
        final boolean[] done = new boolean[1];
        final Handler h = new Handler(Looper.getMainLooper());

        final WebView wv = new WebView(a);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(wv, true);

        // 注意：必须在本 WebView 加载任何页面前注入，否则当前文档里拿不到 JwBridge
        final Bridge bridge = new Bridge(wv, cb, statusView, d, done);
        wv.addJavascriptInterface(bridge, "JwBridge");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url == null || url.indexOf("about:blank") >= 0) return;
                if (pendingJs[0]) {
                    pendingJs[0] = false;
                    runJsFetch(view, api, statusView, done);
                    return;
                }
                if (url.indexOf("login_slogin") < 0) {
                    statusView.setTextColor(0xFF1E8E3E);
                    statusView.setText("已离开登录页，可以点下面的按钮取课表");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith(SCHEME)) {
                    String data = url.substring(SCHEME.length());
                    try {
                        data = URLDecoder.decode(data, "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }
                    bridge.handle(data);
                    return true;
                }
                return false;
            }
        });

        LinearLayout.LayoutParams lpWv = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        lpWv.topMargin = dp(a, 8);
        root.addView(wv, lpWv);
        root.addView(statusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpRow.topMargin = dp(a, 8);
        root.addView(row, lpRow);

        Button ok = new Button(a);
        ok.setText("已登录，获取课表");
        ok.setAllCaps(false);
        ok.setTextSize(13f);
        ok.setTextColor(Color.WHITE);
        ok.setBackground(round(0xFF2F6BFF, dp(a, 10)));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cur = wv.getUrl();
                CookieManager.getInstance().flush();
                String c = CookieManager.getInstance().getCookie(LOGIN_URL);
                if (c == null || c.indexOf("JSESSIONID") < 0) {
                    String c2 = CookieManager.getInstance().getCookie(HOST + "/jwglxt/");
                    if (c2 != null && c2.indexOf("JSESSIONID") >= 0) c = c2;
                }
                JwLog.w(a, "web", "cookie=" + c + " url=" + cur);
                if (c != null && c.indexOf("JSESSIONID") >= 0) {
                    statusView.setTextColor(0xFF1E8E3E);
                    statusView.setText("已拿到会话，正在取课表…");
                    d.dismiss();
                    cb.onCookie(c);
                    return;
                }
                statusView.setTextColor(0xFF5A6572);
                statusView.setText("没拿到 Cookie，改在这个网页里直接请求课表…");
                if (cur != null && cur.startsWith(HOST) && cur.indexOf("login_slogin") < 0) {
                    runJsFetch(wv, api, statusView, done);
                } else {
                    pendingJs[0] = true;
                    wv.loadUrl(INDEX_URL);
                }
            }
        });
        row.addView(ok, new LinearLayout.LayoutParams(0, dp(a, 42), 1.6f));

        Button cancel = new Button(a);
        cancel.setText("取消");
        cancel.setAllCaps(false);
        cancel.setTextSize(13f);
        cancel.setTextColor(0xFF5A6572);
        cancel.setBackground(round(0xFFF1F3F6, dp(a, 10)));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        LinearLayout.LayoutParams lpCancel = new LinearLayout.LayoutParams(0, dp(a, 42), 1f);
        lpCancel.leftMargin = dp(a, 8);
        row.addView(cancel, lpCancel);

        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface di) {
                try {
                    wv.loadUrl("about:blank");
                    wv.destroy();
                } catch (Throwable ignored) {
                }
            }
        });

        wv.loadUrl(LOGIN_URL);
        d.setContentView(root);
        d.show();
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    /** 在这个网页的会话里直接 POST 课表接口，绕过 Cookie 读取。 */
    private static void runJsFetch(final WebView wv, final JwApi api,
                                   final TextView status, final boolean[] done) {
        done[0] = false;
        status.setTextColor(0xFF5A6572);
        status.setText("正在网页里请求课表…（最多等 15 秒）");
        final Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new TimeoutTask(status, done), 15000);

        String body = api.classBody(JwApi.curXnm(), JwApi.curXqm());
        String js = "(function(){try{"
                + "if(typeof JwBridge==='undefined'){location.href='jwdata:NOBRIDGE';return;}"
                + "var x=new XMLHttpRequest();"
                + "x.open('POST','/jwglxt/kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505',true);"
                + "x.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');"
                + "x.setRequestHeader('X-Requested-With','XMLHttpRequest');"
                + "x.onload=function(){try{JwBridge.onData(x.responseText);}"
                + "catch(e){location.href='jwdata:'+encodeURIComponent(x.responseText);}};"
                + "x.onerror=function(){try{JwBridge.onData('XERR');}catch(e){}};"
                + "x.send(" + jsStr(body) + ");"
                + "}catch(e){try{JwBridge.onData('JSERR '+e);}"
                + "catch(e2){location.href='jwdata:JSERR%20'+encodeURIComponent(String(e));}}})()";
        try {
            wv.evaluateJavascript(js, null);
        } catch (Throwable t) {
            status.setTextColor(0xFFC0392B);
            status.setText("网页里执行失败：" + t.getMessage());
        }
    }

    /** 超时兜底（提交后一直没回调）。 */
    private static final class TimeoutTask implements Runnable {
        private final TextView status;
        private final boolean[] done;

        TimeoutTask(TextView status, boolean[] done) {
            this.status = status;
            this.done = done;
        }

        @Override
        public void run() {
            if (done[0]) return;
            status.setTextColor(0xFFC0392B);
            status.setText("网页里 15 秒没回数据：多半还没登录成功，或网络被校园网认证挡住了");
        }
    }

    /** 网页里 XHR 拿回数据后通过 JS 桥交回来。 */
    public static final class Bridge implements Runnable {
        private final WebView wv;
        private final Callback cb;
        private final TextView status;
        private final Dialog d;
        private final boolean[] done;
        private final Handler h = new Handler(Looper.getMainLooper());
        private String pending;

        Bridge(WebView wv, Callback cb, TextView status, Dialog d, boolean[] done) {
            this.wv = wv;
            this.cb = cb;
            this.status = status;
            this.d = d;
            this.done = done;
        }

        @JavascriptInterface
        public void onData(String value) {
            pending = value;
            h.post(this);
        }

        @Override
        public void run() {
            String v = pending;
            pending = null;
            handle(v);
        }

        void handle(String text) {
            if (done[0]) return;
            JwLog.w(wv.getContext(), "web", "jsfetch len=" + (text == null ? -1 : text.length()));
            if (text == null || text.length() == 0) {
                status.setTextColor(0xFFC0392B);
                status.setText("网页里没取到数据（返回空）");
                return;
            }
            if (text.indexOf("kbList") >= 0 || text.indexOf("weekNum") >= 0) {
                done[0] = true;
                status.setTextColor(0xFF1E8E3E);
                status.setText("已经取到课表，正在整理…");
                d.dismiss();
                cb.onJson(text);
                return;
            }
            status.setTextColor(0xFFC0392B);
            String snip = JwApi.plainOf(text);
            if (snip.length() > 60) snip = snip.substring(0, 60);
            status.setText("网页里没登进去或会话已失效（" + snip + "）");
        }
    }

    private static String jsStr(String s) {
        StringBuilder sb = new StringBuilder("'");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' || c == '\\') sb.append('\\').append(c);
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else sb.append(c);
        }
        return sb.append("'").toString();
    }

    private static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static android.graphics.drawable.GradientDrawable round(int color, int radiusPx) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }
}
