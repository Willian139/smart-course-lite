package cn.zuwe.hygiene;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 统一认证登录页：走学校 CAS OAuth（与课表登录同一个 authserver6 统一认证）。
 *
 * 设计要点：
 *  - 只登录一次：登录成功后从 H5 的 localStorage 取出 ihome token，加密保存，之后所有原生请求直接复用；
 *  - 共享课表登录态：WebView 与课表的登录 WebView 共用 CookieManager，CAS 的 CASTGC 仍在有效期内时，
 *    打开本页会自动完成授权，用户无需再输入账号密码；
 *  - 不做“猜测式登录”：拿到的 token 必须通过 /appsys/sys/user/info 校验通过才会保存。
 */
public final class HygieneLoginActivity extends Activity {
    public static final String EXTRA_RESULT_USER = "hygiene_login_user";
    private static final String AUTH_BASE = "https://authserver6.zuwe.edu.cn";
    private static final String IHOME = "https://ihome.zuwe.edu.cn";
    private static final String AUTHORIZE =
            AUTH_BASE + "/authserver/oauth2.0/authorize?response_type=code&client_id=TQb1fihf"
                    + "&redirect_uri=https%3A%2F%2Fihome.zuwe.edu.cn%2Fappsys%2FzjweuCasLogin%2FmobileLogin&state=";

    private static final String SCAN_JS =
            "(function(){var o=[],i,k,v;function push(t,s){try{if(!s)return;for(i=0;i<s.length;i++){k=s.key(i);"
          + "v=s.getItem(k);if(v&&v.length>0&&v.length<3000)o.push(t+k+String.fromCharCode(1)+v);}}catch(e){}}"
          + "push('L:',localStorage);push('S:',sessionStorage);"
          + "try{var c=document.cookie||'';if(c)o.push('C:document.cookie'+String.fromCharCode(1)+c);}catch(e){}"
          + "return o.join(String.fromCharCode(2));})()";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private HygieneSession session;
    private HygieneApi api;
    private WebView web;
    private TextView hint;
    private ProgressBar bar;
    private boolean finished, verifying;
    private int scans;
    private boolean dark = true;
    private int bg, bgAlt, cardBg, line, ink, muted, faint, green;

    private final Runnable scanTick = new Runnable() {
        @Override public void run() {
            if (finished || verifying || web == null) return;
            if (scans++ > 150) { fail("登录等待超时，请重试"); return; }
            web.evaluateJavascript(SCAN_JS, new ValueCallback<String>() {
                @Override public void onReceiveValue(String value) { onLocalStorage(value); }
            });
            main.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle b) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sp = getSharedPreferences("smartcourse_prefs", 0);
        dark = sp.getBoolean("theme_dark", true);
        applyPalette();
        session = new HygieneSession(this);
        api = new HygieneApi(session);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        root.setFitsSystemWindows(true);

        TextView title = new TextView(this);
        title.setText("统一身份认证");
        title.setTextSize(18f); title.setTextColor(ink); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(dp(18), dp(16), dp(18), 0);
        root.addView(title);

        hint = new TextView(this);
        hint.setText("正在打开学校统一认证…\n显示登录页时用学号/密码完成一次即可，之后长期免登录。");
        hint.setTextSize(12f); hint.setTextColor(muted);
        hint.setLineSpacing(dp(3), 1f);
        hint.setPadding(dp(18), dp(8), dp(18), dp(10));
        root.addView(hint);

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(3)));

        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String u = req.getUrl().toString();
                if (u.startsWith("weixin://")) {
                    toast("这里不能跳微信，请用页面上的“统一身份认证”（学号/密码）登录。");
                    return true;
                }
                return false;
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                main.removeCallbacks(scanTick);
                main.postDelayed(scanTick, 600);
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button back = new Button(this);
        back.setText("取消");
        back.setAllCaps(false);
        back.setTextSize(13f);
        back.setTextColor(muted);
        back.setStateListAnimator(null);
        GradientDrawable bd = new GradientDrawable();
        bd.setColor(bgAlt); bd.setCornerRadius(dp(20)); bd.setStroke(dp(1), line);
        back.setBackground(bd);
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { finish(); } });
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(-1, dp(44));
        backLp.setMargins(dp(16), dp(6), dp(16), dp(10));
        root.addView(back, backLp);
        setContentView(root);
        com.zuwe.smartcourselite.PageMotion.installBackdrop(this, bg);
        com.zuwe.smartcourselite.PageMotion.fromBottom(root, 620L);

        web.loadUrl(AUTHORIZE + UUID.randomUUID().toString().replace("-", ""));
    }

    private void onLocalStorage(String raw) {
        if (finished || verifying || raw == null || raw.isEmpty() || "null".equals(raw)) return;
        List<String> candidates = new ArrayList<>();
        try {
            String text = new JSONArray("[" + raw + "]").getString(0);
            for (String pair : text.split(String.valueOf((char) 2))) {
                int cut = pair.indexOf((char) 1);
                if (cut <= 0) continue;
                String key = pair.substring(0, cut);
                if (key.length() > 2 && key.charAt(1) == ':') key = key.substring(2);
                String value = pair.substring(cut + 1).trim();
                if (value.length() < 8 || value.length() > 1500) continue;
                if (value.startsWith("{") || value.startsWith("[") || value.contains(" ")) continue;
                boolean keyHintsToken = key.toLowerCase().contains("token") || key.toLowerCase().contains("auth");
                boolean looksHex32 = value.matches("(?i)[0-9a-f]{32}");
                boolean looksJwt = value.split("\\.").length == 3;
                if (looksHex32 || looksJwt || (keyHintsToken && value.length() <= 200)) {
                    if (!candidates.contains(value)) candidates.add(value);
                }
            }
        } catch (Exception ignored) { return; }
        if (candidates.isEmpty()) return;
        // 优先 32 位十六进制的 ihome token
        List<String> ordered = new ArrayList<>();
        for (String c : candidates) if (c.matches("(?i)[0-9a-f]{32}")) ordered.add(c);
        for (String c : candidates) if (!ordered.contains(c)) ordered.add(c);
        verifyAll(ordered);
    }

    private void verifyAll(final List<String> tokens) {
        verifying = true;
        hint.setText("已拿到登录凭据，正在向学校接口校验…");
        io.execute(new Runnable() {
            @Override public void run() {
                String okToken = null, okUser = null, lastError = null;
                for (String token : tokens) {
                    try {
                        JSONObject info = api.withToken(token).userInfo();
                        HygieneApi.ensureSuccess(info);
                        String user = HygieneApi.usernameOf(info);
                        if (!user.isEmpty()) { okToken = token; okUser = user; break; }
                        lastError = "用户信息为空";
                    } catch (Exception e) {
                        lastError = e.getMessage();
                    }
                }
                final String token = okToken, user = okUser, err = lastError;
                main.post(new Runnable() {
                    @Override public void run() {
                        verifying = false;
                        if (token != null) {
                            try {
                                session.save(token, user, user);
                                Intent out = new Intent();
                                out.putExtra(EXTRA_RESULT_USER, user);
                                setResult(RESULT_OK, out);
                                toast("登录成功：" + user + "，以后不用再登录。");
                                finish();
                            } catch (Exception e) { fail("保存登录状态失败：" + e.getMessage()); }
                        } else {
                            hint.setText("尚未校验通过，请继续在页面上完成登录…" + (err == null ? "" : "\n(" + err + ")"));
                        }
                    }
                });
            }
        });
    }

    private void fail(String message) {
        finished = true;
        main.removeCallbacks(scanTick);
        toast(message);
        if (!isFinishing()) showRetry(message);
    }

    private void showRetry(String message) {
        hint.setText(message + "\n可以点页面下方返回后重新进入。");
    }

    @Override protected void onDestroy() {
        finished = true;
        main.removeCallbacks(scanTick);
        if (web != null) { web.stopLoading(); web.destroy(); web = null; }
        io.shutdownNow();
        super.onDestroy();
    }

    private void applyPalette() {
        if (dark) {
            bg = Color.parseColor("#131312"); bgAlt = Color.parseColor("#1B1B19");
            cardBg = Color.parseColor("#262622"); line = Color.parseColor("#3A3A35");
            ink = Color.parseColor("#EEEAE0"); muted = Color.parseColor("#A5A198");
            faint = Color.parseColor("#706E67"); green = Color.parseColor("#89997A");
        } else {
            bg = Color.parseColor("#EFEDE6"); bgAlt = Color.parseColor("#E6E3DA");
            cardBg = Color.parseColor("#FBFAF6"); line = Color.parseColor("#DAD6CB");
            ink = Color.parseColor("#191916"); muted = Color.parseColor("#77746B");
            faint = Color.parseColor("#AAA79D"); green = Color.parseColor("#66735D");
        }
    }

    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
