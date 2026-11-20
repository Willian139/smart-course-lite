package cn.zuwe.hygiene;

import android.R;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.zuwe.smartcourselite.PageMotion;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes.dex */
public final class HygieneLoginActivity extends Activity {
    private static final String AUTHORIZE = "https://authserver6.zuwe.edu.cn/authserver/oauth2.0/authorize?response_type=code&client_id=TQb1fihf&redirect_uri=https%3A%2F%2Fihome.zuwe.edu.cn%2Fappsys%2FzjweuCasLogin%2FmobileLogin&state=";
    private static final String AUTH_BASE = "https://authserver6.zuwe.edu.cn";
    public static final String EXTRA_RESULT_USER = "hygiene_login_user";
    private static final String IHOME = "https://ihome.zuwe.edu.cn";
    private static final String SCAN_JS = "(function(){var o=[],i,k,v;function push(t,s){try{if(!s)return;for(i=0;i<s.length;i++){k=s.key(i);v=s.getItem(k);if(v&&v.length>0&&v.length<3000)o.push(t+k+String.fromCharCode(1)+v);}}catch(e){}}push('L:',localStorage);push('S:',sessionStorage);try{var c=document.cookie||'';if(c)o.push('C:document.cookie'+String.fromCharCode(1)+c);}catch(e){}return o.join(String.fromCharCode(2));})()";
    private HygieneApi api;
    private ProgressBar bar;
    private int bg;
    private int bgAlt;
    private int cardBg;
    private int faint;
    private boolean finished;
    private int green;
    private TextView hint;
    private int ink;
    private int line;
    private int muted;
    private int scans;
    private HygieneSession session;
    private boolean verifying;
    private WebView web;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private boolean dark = true;
    private final Runnable scanTick = new Runnable() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.1
        @Override // java.lang.Runnable
        public void run() {
            if (HygieneLoginActivity.this.finished || HygieneLoginActivity.this.verifying || HygieneLoginActivity.this.web == null) {
                return;
            }
            if (HygieneLoginActivity.access$308(HygieneLoginActivity.this) <= 150) {
                HygieneLoginActivity.this.web.evaluateJavascript(HygieneLoginActivity.SCAN_JS, new ValueCallback<String>() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.1.1
                    @Override // android.webkit.ValueCallback
                    public void onReceiveValue(String str) {
                        HygieneLoginActivity.this.onLocalStorage(str);
                    }
                });
                HygieneLoginActivity.this.main.postDelayed(this, 1000L);
            } else {
                HygieneLoginActivity.this.fail("登录等待超时，请重试");
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void clearIhomeCookies() {
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie("https://ihome.zuwe.edu.cn/");
            if (cookie != null && !cookie.isEmpty()) {
                for (String str : cookie.split(";")) {
                    String trim = str.trim().split("=")[0].trim();
                    if (!trim.isEmpty()) {
                        cookieManager.setCookie("https://ihome.zuwe.edu.cn/", trim + "=; Max-Age=0; Path=/");
                    }
                }
                cookieManager.flush();
            }
        } catch (Exception unused) {
        }
    }

    static int access$308(HygieneLoginActivity hygieneLoginActivity) {
        int i = hygieneLoginActivity.scans;
        hygieneLoginActivity.scans = i + 1;
        return i;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        setTheme(R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        requestWindowFeature(1);
        this.dark = getSharedPreferences("smartcourse_prefs", 0).getBoolean("theme_dark", true);
        applyPalette();
        HygieneSession hygieneSession = new HygieneSession(this);
        this.session = hygieneSession;
        this.api = new HygieneApi(hygieneSession);
        getWindow().setStatusBarColor(this.bg);
        getWindow().setNavigationBarColor(this.bg);
        getWindow().getDecorView().setSystemUiVisibility(this.dark ? 0 : 8192);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(this.bg);
        linearLayout.setFitsSystemWindows(true);
        TextView textView = new TextView(this);
        textView.setText("统一身份认证");
        textView.setTextSize(18.0f);
        textView.setTextColor(this.ink);
        textView.setTypeface(Typeface.DEFAULT, 1);
        textView.setPadding(dp(18), dp(16), dp(18), 0);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this);
        this.hint = textView2;
        textView2.setText("正在打开学校统一认证…\n显示登录页时用学号/密码完成一次即可，之后长期免登录。");
        this.hint.setTextSize(12.0f);
        this.hint.setTextColor(this.muted);
        this.hint.setLineSpacing(dp(3), 1.0f);
        this.hint.setPadding(dp(18), dp(8), dp(18), dp(10));
        linearLayout.addView(this.hint);
        ProgressBar progressBar = new ProgressBar(this, null, R.attr.progressBarStyleHorizontal);
        this.bar = progressBar;
        progressBar.setIndeterminate(true);
        linearLayout.addView(this.bar, new LinearLayout.LayoutParams(-1, dp(3)));
        WebView webView = new WebView(this);
        this.web = webView;
        webView.getSettings().setJavaScriptEnabled(true);
        this.web.getSettings().setDomStorageEnabled(true);
        this.web.getSettings().setDatabaseEnabled(true);
        this.web.setWebViewClient(new WebViewClient() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.2
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, WebResourceRequest webResourceRequest) {
                if (!webResourceRequest.getUrl().toString().startsWith("weixin://")) {
                    return false;
                }
                HygieneLoginActivity.this.toast("这里不能跳微信，请用页面上的“统一身份认证”（学号/密码）登录。");
                return true;
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView2, String str) {
                super.onPageFinished(webView2, str);
                HygieneLoginActivity.this.main.removeCallbacks(HygieneLoginActivity.this.scanTick);
                HygieneLoginActivity.this.main.postDelayed(HygieneLoginActivity.this.scanTick, 600L);
            }
        });
        linearLayout.addView(this.web, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        final TextView textView3 = new TextView(this);
        textView3.setTextSize(11.0f);
        textView3.setTextColor(this.faint);
        textView3.setVisibility(8);
        textView3.setPadding(dp(16), dp(6), dp(16), 0);
        linearLayout.addView(textView3, new LinearLayout.LayoutParams(-1, -2));
        Button button = new Button(this);
        button.setText("取消");
        button.setAllCaps(false);
        button.setTextSize(13.0f);
        button.setTextColor(this.muted);
        button.setStateListAnimator(null);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(20));
        gradientDrawable.setStroke(dp(1), this.line);
        button.setBackground(gradientDrawable);
        button.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                HygieneLoginActivity.this.finish();
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(44));
        layoutParams.setMargins(dp(16), dp(6), dp(16), dp(10));
        linearLayout.addView(button, layoutParams);
        setContentView(linearLayout);
        PageMotion.installBackdrop(this, this.bg);
        PageMotion.fromBottom(linearLayout, 620L);
        clearIhomeCookies();
        clearIhomeCookies();
        this.web.loadUrl(AUTHORIZE + UUID.randomUUID().toString().replace("-", ""));
        this.main.postDelayed(new Runnable() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.4
            @Override // java.lang.Runnable
            public void run() {
                if (HygieneLoginActivity.this.finished || HygieneLoginActivity.this.verifying) {
                    return;
                }
                textView3.setVisibility(0);
                textView3.setText("如果页面提示「账号已在其他地方登录」：请先在微信或其它设备里退出智慧校园，再点下方\"重新载入\"，本页会自动带着统一认证会话重新登录。");
            }
        }, 6000L);
        TextView textView4 = new TextView(this);
        textView4.setText("重新载入");
        textView4.setTextSize(13.0f);
        textView4.setTextColor(this.green);
        textView4.setGravity(17);
        textView4.setPadding(dp(16), dp(8), dp(16), dp(4));
        textView4.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                HygieneLoginActivity.this.clearIhomeCookies();
                HygieneLoginActivity.this.finished = false;
                HygieneLoginActivity.this.verifying = false;
                HygieneLoginActivity.this.hint.setText("正在重新载入授权页…");
                HygieneLoginActivity.this.web.loadUrl(HygieneLoginActivity.AUTHORIZE + UUID.randomUUID().toString().replace("-", ""));
            }
        });
        linearLayout.addView(textView4, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX WARN: Code restructure failed: missing block: B:58:0x00dd, code lost:
    
        if (r1.contains(r7) != false) goto L89;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x00df, code lost:
    
        r1.add(r7);
     */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00c4  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x00c6  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void onLocalStorage(java.lang.String r14) {
        /*
            Method dump skipped, instructions count: 298
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: cn.zuwe.hygiene.HygieneLoginActivity.onLocalStorage(java.lang.String):void");
    }

    private void verifyAll(final List<String> list) {
        this.verifying = true;
        this.hint.setText("已拿到登录凭据，正在向学校接口校验…");
        this.io.execute(new Runnable() { // from class: cn.zuwe.hygiene.HygieneLoginActivity.6
            /* JADX WARN: Code restructure failed: missing block: B:29:0x0039, code lost:
            
                r3 = "null";
             */
            /* JADX WARN: Removed duplicated region for block: B:21:0x0095 A[SYNTHETIC] */
            /* JADX WARN: Removed duplicated region for block: B:25:0x0091 A[SYNTHETIC] */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct add '--show-bad-code' argument
            */
            public void run() {
                /*
                    r8 = this;
                    java.util.List r0 = r2
                    java.util.Iterator r0 = r0.iterator()
                    r1 = 0
                    r2 = r1
                L8:
                    boolean r3 = r0.hasNext()
                    if (r3 != 0) goto L11
                    r4 = r1
                    goto L80
                L11:
                    java.lang.Object r2 = r0.next()
                    java.lang.String r2 = (java.lang.String) r2
                    cn.zuwe.hygiene.HygieneLoginActivity r3 = cn.zuwe.hygiene.HygieneLoginActivity.this     // Catch: java.lang.Exception -> L70
                    cn.zuwe.hygiene.HygieneApi r3 = cn.zuwe.hygiene.HygieneLoginActivity.access$700(r3)     // Catch: java.lang.Exception -> L70
                    cn.zuwe.hygiene.HygieneApi r3 = r3.withToken(r2)     // Catch: java.lang.Exception -> L70
                    org.json.JSONObject r3 = r3.userInfo()     // Catch: java.lang.Exception -> L70
                    cn.zuwe.hygiene.HygieneApi.ensureSuccess(r3)     // Catch: java.lang.Exception -> L70
                    java.lang.String r4 = cn.zuwe.hygiene.HygieneApi.usernameOf(r3)     // Catch: java.lang.Exception -> L70
                    if (r4 == 0) goto L37
                    boolean r5 = r4.isEmpty()     // Catch: java.lang.Exception -> L70
                    if (r5 == 0) goto L35
                    goto L37
                L35:
                    r3 = r1
                    goto L76
                L37:
                    if (r3 != 0) goto L3c
                    java.lang.String r3 = "null"
                    goto L40
                L3c:
                    java.lang.String r3 = r3.toString()     // Catch: java.lang.Exception -> L70
                L40:
                    int r5 = r3.length()     // Catch: java.lang.Exception -> L70
                    r6 = 180(0xb4, float:2.52E-43)
                    if (r5 <= r6) goto L5e
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L70
                    r5.<init>()     // Catch: java.lang.Exception -> L70
                    r7 = 0
                    java.lang.String r3 = r3.substring(r7, r6)     // Catch: java.lang.Exception -> L70
                    r5.append(r3)     // Catch: java.lang.Exception -> L70
                    java.lang.String r3 = "…"
                    r5.append(r3)     // Catch: java.lang.Exception -> L70
                    java.lang.String r3 = r5.toString()     // Catch: java.lang.Exception -> L70
                L5e:
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L70
                    r5.<init>()     // Catch: java.lang.Exception -> L70
                    java.lang.String r6 = "接口通了但没有用户字段："
                    r5.append(r6)     // Catch: java.lang.Exception -> L70
                    r5.append(r3)     // Catch: java.lang.Exception -> L70
                    java.lang.String r3 = r5.toString()     // Catch: java.lang.Exception -> L70
                    goto L76
                L70:
                    r3 = move-exception
                    java.lang.String r3 = r3.getMessage()
                    r4 = r1
                L76:
                    if (r4 == 0) goto L8f
                    boolean r5 = r4.isEmpty()
                    if (r5 != 0) goto L8f
                    r1 = r2
                    r2 = r3
                L80:
                    cn.zuwe.hygiene.HygieneLoginActivity r0 = cn.zuwe.hygiene.HygieneLoginActivity.this
                    android.os.Handler r0 = cn.zuwe.hygiene.HygieneLoginActivity.access$300(r0)
                    cn.zuwe.hygiene.HygieneLoginActivity$6$1 r3 = new cn.zuwe.hygiene.HygieneLoginActivity$6$1
                    r3.<init>()
                    r0.post(r3)
                    return
                L8f:
                    if (r3 != 0) goto L95
                    java.lang.String r2 = "用户信息为空"
                    goto L8
                L95:
                    r2 = r3
                    goto L8
                */
                throw new UnsupportedOperationException("Method not decompiled: cn.zuwe.hygiene.HygieneLoginActivity.AnonymousClass6.run():void");
            }
        });
    }

    public void fail(String str) {
        this.finished = true;
        this.main.removeCallbacks(this.scanTick);
        toast(str);
        if (isFinishing()) {
            return;
        }
        showRetry(str);
    }

    private void showRetry(String str) {
        this.hint.setText(str + "\n可以点页面下方返回后重新进入。");
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.finished = true;
        this.main.removeCallbacks(this.scanTick);
        WebView webView = this.web;
        if (webView != null) {
            webView.stopLoading();
            this.web.destroy();
            this.web = null;
        }
        this.io.shutdownNow();
        super.onDestroy();
    }

    private void applyPalette() {
        if (this.dark) {
            this.bg = Color.parseColor("#181A16");
            this.bgAlt = Color.parseColor("#252820");
            this.cardBg = Color.parseColor("#20221D");
            this.line = Color.parseColor("#34382F");
            this.ink = Color.parseColor("#ECEDE6");
            this.muted = Color.parseColor("#A3A69B");
            this.faint = Color.parseColor("#7A7E72");
            this.green = Color.parseColor("#95A48C");
            return;
        }
        this.bg = Color.parseColor("#F4F3EE");
        this.bgAlt = Color.parseColor("#EAE9E1");
        this.cardBg = Color.parseColor("#FFFFFF");
        this.line = Color.parseColor("#E4E2D9");
        this.ink = Color.parseColor("#282A25");
        this.muted = Color.parseColor("#85877F");
        this.faint = Color.parseColor("#ADAEA6");
        this.green = Color.parseColor("#6F7C69");
    }

    private int dp(int i) {
        return (int) ((i * getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void toast(String str) {
        Toast.makeText(this, str, 1).show();
    }
}
