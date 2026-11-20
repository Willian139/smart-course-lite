package cn.zuwe.hygiene;

import android.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.zuwe.smartcourselite.AboutFooter;
import com.zuwe.smartcourselite.Motion;
import com.zuwe.smartcourselite.PageMotion;
import com.zuwe.smartcourselite.SvgIcon;
import com.zuwe.smartcourselite.UiPolish;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class HygieneHistoryActivity extends Activity {
    private static final String KEY_DARK = "theme_dark";
    private static final String KEY_LIST_CACHE = "hyg_list_cache_";
    private static final String PREFS = "smartcourse_prefs";
    private static final int REQ_LOGIN = 7;
    private HygieneApi api;
    private boolean autoLoginTried;
    private int bg;
    private int bgAlt;
    private boolean busy;
    private int cardBg;
    private LinearLayout content;
    private int faint;
    private int green;
    private int ink;
    private JSONObject lastPage;
    private int line;
    private boolean loginInFlight;
    private int muted;
    private Dialog photoDialog;
    private int red;
    private HygieneSession session;
    private boolean showingDetail;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private int pageNo = 1;
    private int totalPages = 1;
    private final Map<String, JSONObject> detailCache = new HashMap();
    private boolean dark = true;
    private long loadedAt = 0;

    interface Card {
        void build(LinearLayout linearLayout);
    }

    interface Done<T> {
        void accept(T t);
    }

    interface Work<T> {
        T run() throws Exception;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        setTheme(R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        requestWindowFeature(1);
        this.dark = getSharedPreferences(PREFS, 0).getBoolean(KEY_DARK, true);
        applyPalette();
        this.session = new HygieneSession(this);
        windowChrome();
        build();
        this.api = new HygieneApi(this.session);
        if (this.session.isAuthenticated()) {
            renderCachedList();
            loadHistory(true);
        } else {
            showLogin(null);
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if ((this.loadedAt <= 0 || System.currentTimeMillis() - this.loadedAt >= 300000) && this.session.isAuthenticated() && this.loadedAt > 0 && !this.busy) {
            loadHistory(true);
        }
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
            this.red = Color.parseColor("#CD8B84");
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
        this.red = Color.parseColor("#BA716B");
    }

    private void windowChrome() {
        getWindow().setStatusBarColor(this.bg);
        getWindow().setNavigationBarColor(this.bg);
        getWindow().getDecorView().setSystemUiVisibility(this.dark ? 0 : 8192);
    }

    private void build() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(this.bg);
        frameLayout.setFitsSystemWindows(true);
        final ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        final LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(18), dp(20), dp(18), dp(12));
        TextView txt = txt("HOUSEKEEPING / 宿舍卫生", 10, this.faint, false);
        txt.setLetterSpacing(0.12f);
        linearLayout2.addView(txt);
        TextView txt2 = txt("卫生检查记录", 26, this.ink, true);
        txt2.setLetterSpacing(-0.02f);
        linearLayout2.addView(txt2, top(6));
        linearLayout2.addView(txt("统一认证登录一次，之后长期免重新登录", 11, this.faint, false), top(5));
        linearLayout.addView(linearLayout2);
        linearLayout.addView(hairline());
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.content = linearLayout3;
        linearLayout3.setOrientation(1);
        this.content.setPadding(dp(16), dp(14), dp(16), dp(28));
        linearLayout.addView(this.content, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout build = AboutFooter.build(this, this.ink, this.muted, this.faint, this.line, this.cardBg, this.bgAlt, this.green);
        build.setPadding(dp(18), 0, dp(18), dp(28));
        linearLayout.addView(build, new LinearLayout.LayoutParams(-1, -2));
        scrollView.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
        frameLayout.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
        final FrameLayout frameLayout2 = new FrameLayout(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bg);
        frameLayout2.setBackground(gradientDrawable);
        frameLayout2.setAlpha(0.0f);
        final TextView txt3 = txt("卫生检查记录", 16, this.ink, true);
        txt3.setGravity(17);
        txt3.setAlpha(0.0f);
        frameLayout2.addView(txt3, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout roundIconChip = roundIconChip(SvgIcon.CHEV_L, 34);
        roundIconChip.setContentDescription("返回");
        roundIconChip.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HygieneHistoryActivity.this.m1lambda$build$0$cnzuwehygieneHygieneHistoryActivity(view);
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp(34.0f), dp(34.0f), 19);
        layoutParams.leftMargin = dp(12.0f);
        frameLayout2.addView(roundIconChip, layoutParams);
        View view = new View(this);
        view.setBackgroundColor(this.line);
        frameLayout2.addView(view, new FrameLayout.LayoutParams(-1, dp(1.0f), 80));
        frameLayout.addView(frameLayout2, new FrameLayout.LayoutParams(-1, dp(52.0f), 48));
        setContentView(frameLayout);
        PageMotion.installBackdrop(this, this.bg);
        int intExtra = getIntent().getIntExtra("reveal_x", -1);
        int intExtra2 = getIntent().getIntExtra("reveal_y", -1);
        if (intExtra >= 0 && intExtra2 >= 0) {
            int identifier = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int dimensionPixelSize = identifier > 0 ? getResources().getDimensionPixelSize(identifier) : 0;
            frameLayout.setPivotX(intExtra);
            frameLayout.setPivotY(Math.max(0, intExtra2 - dimensionPixelSize));
        } else {
            frameLayout.setPivotX(getResources().getDisplayMetrics().widthPixels * 0.86f);
            frameLayout.setPivotY(0.0f);
        }
        frameLayout.setScaleX(0.86f);
        frameLayout.setScaleY(0.86f);
        frameLayout.setAlpha(0.0f);
        frameLayout.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
        final int dp = dp(52.0f);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.2
            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public final void onScrollChanged() {
                HygieneHistoryActivity.lambda$build$1(scrollView, linearLayout2, dp, frameLayout2, txt3);
            }
        });
    }

    void m1lambda$build$0$cnzuwehygieneHygieneHistoryActivity(View view) {
        finish();
    }

    static void lambda$build$1(ScrollView scrollView, LinearLayout linearLayout, int i, FrameLayout frameLayout, TextView textView) {
        float min = Math.min(1.0f, Math.max(0.0f, scrollView.getScrollY() / Math.max(1, linearLayout.getHeight() - i)));
        frameLayout.setAlpha(min);
        textView.setAlpha(min * min);
        linearLayout.setAlpha(1.0f - (min * 0.9f));
        linearLayout.setTranslationY((-scrollView.getScrollY()) * 0.22f);
    }

    private FrameLayout roundIconChip(String str, int i) {
        FrameLayout frameLayout = new FrameLayout(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(i / 2.0f));
        frameLayout.setBackground(gradientDrawable);
        int i2 = i - 14;
        frameLayout.addView(SvgIcon.view(this, str, this.ink, i2), new FrameLayout.LayoutParams(dp(i2), dp(i2), 17));
        frameLayout.setClickable(true);
        UiPolish.pressable(frameLayout);
        return frameLayout;
    }

    public void verifyThenLoad() {
        this.showingDetail = false;
        this.content.removeAllViews();
        addText(this.content, "正在校验已有登录状态…", 13, this.muted, false, 8);
        final String str = this.session.token();
        this.io.execute(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.3
            @Override // java.lang.Runnable
            public void run() {
                String str2;
                try {
                    JSONObject userInfo = HygieneHistoryActivity.this.api.userInfo();
                    HygieneApi.ensureSuccess(userInfo);
                    str2 = HygieneApi.usernameOf(userInfo);
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        e.getMessage();
                    }
                    str2 = null;
                }
                final String fs2 = str2;
                final String str3 = str;
                HygieneHistoryActivity.this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (fs2 != null) {
                            HygieneSession hygieneSession = HygieneHistoryActivity.this.session;
                            String str4 = str3;
                            String str5 = fs2;
                            hygieneSession.save(str4, str5, str5);
                            HygieneHistoryActivity.this.loadHistory(fs2, true);
                            return;
                        }
                        HygieneHistoryActivity.this.session.clear();
                        HygieneHistoryActivity.this.showLogin("请重新登录卫生检查");
                    }
                });
            }
        });
    }

    public void showLogin(final String str) {
        if (!this.autoLoginTried && !this.loginInFlight && !this.session.isAuthenticated()) {
            this.autoLoginTried = true;
            this.loginInFlight = true;
            toast("正在用课表凭证自动登录卫生检查…");
            PageMotion.captureBackdrop(this);
            Intent intent = new Intent(this, (Class<?>) HygieneLoginActivity.class);
            intent.addFlags(536870912);
            startActivityForResult(intent, REQ_LOGIN);
            overridePendingTransition(0, 0);
        }
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        this.content.removeAllViews();
        this.content.addView(linearLayout);
        this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.4
            @Override // java.lang.Runnable
            public void run() {
                HygieneHistoryActivity.this.m21lambda$showLogin$0$cnzuwehygieneHygieneHistoryActivity(str, linearLayout);
            }
        });
    }

    void m21lambda$showLogin$0$cnzuwehygieneHygieneHistoryActivity(String str, LinearLayout linearLayout) {
        addText(linearLayout, "需要一次统一身份认证", 16, this.ink, true, 0);
        if (str != null && !str.isEmpty()) {
            addText(linearLayout, "上次登录不可用：" + str, 11, this.red, false, 6);
        }
        addText(linearLayout, "登录成功后登录凭据会加密保存在本机，以后直接进入，不用反复登录。", 12, this.muted, false, 8);
        addText(linearLayout, "与课表用的是同一个统一认证（authserver6）：课表认证仍有效时通常自动通过。", 11, this.faint, false, 6);
        addButton(linearLayout, "打开统一认证登录", true, new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.5
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.m22lambda$showLogin$1$cnzuwehygieneHygieneHistoryActivity();
            }
        });
        addButton(linearLayout, "重新校验已有登录状态", false, new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.6
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.verifyThenLoad();
            }
        });
    }

    void m22lambda$showLogin$1$cnzuwehygieneHygieneHistoryActivity() {
        PageMotion.captureBackdrop(this);
        startActivityForResult(new Intent(this, (Class<?>) HygieneLoginActivity.class), REQ_LOGIN);
        overridePendingTransition(0, 0);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != REQ_LOGIN) {
            return;
        }
        this.loginInFlight = false;
        if (i2 == -1) {
            verifyThenLoad();
        } else {
            if (this.session.isAuthenticated()) {
                return;
            }
            showLogin("未完成登录");
        }
    }

    public void onHygieneAuthorizationCompleted(String str, String str2) {
        if (str == null || str.isEmpty() || str2 == null || str2.isEmpty()) {
            toast("授权结果不完整");
        } else {
            this.session.save(str, str2, str2);
            verifyThenLoad();
        }
    }

    private void renderCachedList() {
        String string = getSharedPreferences(PREFS, 0).getString(KEY_LIST_CACHE + this.session.personSn(), "");
        if (string == null || string.isEmpty()) {
            return;
        }
        try {
            renderListPage(new JSONObject(string), true);
        } catch (Exception unused) {
        }
    }

    private void saveListCache(JSONObject jSONObject) {
        if (jSONObject == null) {
            return;
        }
        try {
            getSharedPreferences(PREFS, 0).edit().putString(KEY_LIST_CACHE + this.session.personSn(), jSONObject.toString()).apply();
        } catch (Exception unused) {
        }
    }

    private static boolean isAuthError(String str) {
        if (str == null) {
            return false;
        }
        return str.contains("HTTP 401") || str.contains("HTTP 403") || str.contains("401") || str.contains("403") || str.contains("未登录") || str.contains("登录超时") || str.contains("登录状态已失效") || str.contains("无效身份") || str.contains("token 无效") || str.contains("请先登录") || str.toLowerCase().contains("token") || str.contains("失效") || str.contains("过期") || str.contains("重新登录") || str.contains("凭证");
    }

    private void loadHistory(boolean z) {
        if (this.session.token().isEmpty()) {
            showLogin("尚未登录");
        } else if (this.session.personSn().isEmpty()) {
            verifyThenLoad();
        } else {
            if (this.busy) {
                return;
            }
            loadHistory(this.session.personSn(), z);
        }
    }

    public void loadHistory(final String str, final boolean z) {
        if (this.busy) {
            return;
        }
        if (z) {
            this.pageNo = 1;
        }
        final int i = this.pageNo;
        run("正在加载历史记录…", new Work() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.7
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Work
            public final Object run() {
                try {
                    return HygieneHistoryActivity.this.m6lambda$loadHistory$0$cnzuwehygieneHygieneHistoryActivity(i, str);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Done() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.8
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Done
            public final void accept(Object obj) {
                HygieneHistoryActivity.this.m7lambda$loadHistory$1$cnzuwehygieneHygieneHistoryActivity(z, (JSONObject) obj);
            }
        });
    }

    JSONObject m6lambda$loadHistory$0$cnzuwehygieneHygieneHistoryActivity(int i, String str) throws Exception {
        JSONObject historyPage = this.api.historyPage(i, 10, "", str);
        HygieneApi.ensureSuccess(historyPage);
        JSONObject findPage = HygieneApi.findPage(historyPage);
        if (findPage != null) {
            return findPage;
        }
        throw new IOException("分页响应缺少 page 节点");
    }

    void m7lambda$loadHistory$1$cnzuwehygieneHygieneHistoryActivity(boolean z, JSONObject jSONObject) {
        this.lastPage = jSONObject;
        renderListPage(jSONObject, z);
        saveListCache(jSONObject);
        this.loadedAt = System.currentTimeMillis();
    }

    private void renderListPage(JSONObject jSONObject, boolean z) {
        this.showingDetail = false;
        if (z) {
            this.content.removeAllViews();
        }
        this.totalPages = Math.max(1, jSONObject.optInt("totalPage", jSONObject.optInt("pages", 1)));
        JSONArray findList = HygieneApi.findList(jSONObject);
        int optInt = jSONObject.optInt("totalCount", jSONObject.optInt("total", findList.length()));
        if (z) {
            renderHeader(optInt);
        }
        int i = 0;
        for (int i2 = 0; i2 < findList.length(); i2++) {
            JSONObject optJSONObject = findList.optJSONObject(i2);
            if (optJSONObject != null && inCurrentSemester(first(optJSONObject, "recordTime", "createTime", "checkTime"))) {
                renderRow(optJSONObject);
                i++;
            }
        }
        if (i == 0) {
            addText(this.content, "本学期暂无检查记录", 13, this.faint, false, 10);
        }
        String[] currentSemester = currentSemester();
        addText(this.content, "仅保留本学期 " + currentSemester[0] + " ~ " + currentSemester[1] + " 的记录", 10, this.faint, false, 4);
        renderPager();
    }

    private String[] currentSemester() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(1);
        int i2 = calendar.get(2) + 1;
        if (i2 >= 8) {
            return new String[]{i + "-08-01", (i + 1) + "-01-31"};
        }
        if (i2 == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(i - 1);
            sb.append("-08-01");
            return new String[]{sb.toString(), i + "-01-31"};
        }
        return new String[]{i + "-02-01", i + "-07-31"};
    }

    private boolean inCurrentSemester(String str) {
        if (str == null || str.length() < 10) {
            return true;
        }
        String substring = str.substring(0, 10);
        if (substring.length() != 10 || substring.charAt(4) != '-') {
            return true;
        }
        String[] currentSemester = currentSemester();
        return substring.compareTo(currentSemester[0]) >= 0 && substring.compareTo(currentSemester[1]) <= 0;
    }

    private void renderHeader(final int i) {
        card(new Card() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.9
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Card
            public final void build(LinearLayout linearLayout) {
                HygieneHistoryActivity.this.m13lambda$renderHeader$0$cnzuwehygieneHygieneHistoryActivity(i, linearLayout);
            }
        });
    }

    void m13lambda$renderHeader$0$cnzuwehygieneHygieneHistoryActivity(int i, LinearLayout linearLayout) {
        addText(linearLayout, "个人检查档案", 15, this.ink, true, 0);
        addText(linearLayout, "共 " + i + " 条记录 · 第 " + this.pageNo + " / " + this.totalPages + " 页", 11, this.muted, false, 5);
        String user = this.session.user();
        if (user.isEmpty()) {
            return;
        }
        addText(linearLayout, "当前账号：" + user, 11, this.faint, false, 4);
    }

    private void renderRow(final JSONObject jSONObject) {
        if (jSONObject == null) {
            return;
        }
        final String first = first(jSONObject, "id", "historyRoomId", "recordId");
        final String first2 = first(jSONObject, "roomName", "fullName");
        final String first3 = first(jSONObject, "recordTime", "createTime", "checkTime");
        final String first4 = first(jSONObject, "totalScore", "score");
        card(new Card() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.10
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Card
            public final void build(LinearLayout linearLayout) {
                HygieneHistoryActivity.this.m16lambda$renderRow$0$cnzuwehygieneHygieneHistoryActivity(first2, first3, jSONObject, first4, first, linearLayout);
            }
        });
    }

    void m16lambda$renderRow$0$cnzuwehygieneHygieneHistoryActivity(String str, String str2, JSONObject jSONObject, String str3, final String str4, LinearLayout linearLayout) {
        StringBuilder sb = new StringBuilder();
        if (str.isEmpty()) {
            str = "宿舍检查";
        }
        sb.append(str);
        sb.append("  ·  ");
        if (str2.isEmpty()) {
            str2 = "时间未知";
        }
        sb.append(str2);
        addText(linearLayout, sb.toString(), 14, this.ink, true, 0);
        addText(linearLayout, "检查计划：" + first(jSONObject, "planName", "inspectionPlanName"), 11, this.faint, false, 5);
        StringBuilder sb2 = new StringBuilder("总分 ");
        sb2.append(str3.isEmpty() ? "—" : str3);
        sb2.append(" 分");
        addText(linearLayout, sb2.toString(), 20, scoreColor(str3), true, 8);
        addText(linearLayout, "卫生分 " + first(jSONObject, "hygieneScore", "hygieneDeductScore") + "   违纪分 " + first(jSONObject, "peccancyScore", "peccancyDeductScore"), 11, this.muted, false, 4);
        addButton(linearLayout, "查看检查详情", false, new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.11
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.m17lambda$renderRow$1$cnzuwehygieneHygieneHistoryActivity(str4);
            }
        });
    }

    void m17lambda$renderRow$1$cnzuwehygieneHygieneHistoryActivity(String str) {
        if (str.isEmpty()) {
            toast("记录缺少详情 ID");
        } else {
            loadDetail(str);
        }
    }

    private void loadDetail(final String str) {
        JSONObject jSONObject = this.detailCache.get(str);
        if (jSONObject != null) {
            renderDetail(jSONObject);
            return;
        }
        this.showingDetail = true;
        this.content.removeAllViews();
        addText(this.content, "正在加载检查详情…", 13, this.muted, false, 8);
        run("正在加载检查详情…", new Work() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.12
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Work
            public final Object run() {
                try {
                    return HygieneHistoryActivity.this.m3lambda$loadDetail$0$cnzuwehygieneHygieneHistoryActivity(str);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Done() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.13
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Done
            public final void accept(Object obj) {
                HygieneHistoryActivity.this.m4lambda$loadDetail$1$cnzuwehygieneHygieneHistoryActivity(str, (JSONObject) obj);
            }
        });
    }

    JSONObject m3lambda$loadDetail$0$cnzuwehygieneHygieneHistoryActivity(String str) throws Exception {
        JSONObject historyDetail = this.api.historyDetail(str);
        HygieneApi.ensureSuccess(historyDetail);
        JSONObject unwrapData = HygieneApi.unwrapData(historyDetail);
        JSONObject optJSONObject = unwrapData.optJSONObject("historyRoom");
        if (optJSONObject == null) {
            optJSONObject = unwrapData.optJSONObject("detail");
        }
        return optJSONObject == null ? unwrapData : optJSONObject;
    }

    void m4lambda$loadDetail$1$cnzuwehygieneHygieneHistoryActivity(String str, JSONObject jSONObject) {
        this.detailCache.put(str, jSONObject);
        renderDetail(jSONObject);
    }

    private void renderDetail(final JSONObject jSONObject) {
        this.showingDetail = true;
        this.content.removeAllViews();
        addButton(this.content, "← 返回历史记录", false, new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.14
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.backToList();
            }
        });
        card(new Card() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.15
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Card
            public final void build(LinearLayout linearLayout) {
                HygieneHistoryActivity.this.m12lambda$renderDetail$0$cnzuwehygieneHygieneHistoryActivity(jSONObject, linearLayout);
            }
        });
        compactItems("卫生评分明细", array(jSONObject, "hygieneItems", "hygieneItemList"));
        compactItems("违纪扣分明细", array(jSONObject, "peccancyItems", "peccancyItemList"));
        photos("现场照片", array(jSONObject, "urlList", "photoList"));
        photos("违纪照片", array(jSONObject, "urlListToPeccancy", "peccancyPhotoList"));
    }

    void m12lambda$renderDetail$0$cnzuwehygieneHygieneHistoryActivity(JSONObject jSONObject, LinearLayout linearLayout) {
        addText(linearLayout, first(jSONObject, "fullName", "roomName"), 16, this.ink, true, 0);
        addText(linearLayout, "检查计划：" + first(jSONObject, "planName"), 11, this.faint, false, 5);
        addText(linearLayout, "检查时间：" + first(jSONObject, "recordTime", "checkTime"), 11, this.muted, false, 6);
        addText(linearLayout, "总分 " + first(jSONObject, "totalScore", "score") + " 分", 23, scoreColor(first(jSONObject, "totalScore", "score")), true, 10);
        addText(linearLayout, "卫生分 " + first(jSONObject, "hygieneScore") + "   违纪分 " + first(jSONObject, "peccancyScore"), 11, this.muted, false, 4);
        StringBuilder sb = new StringBuilder("检查人员：");
        sb.append(first(jSONObject, "createName", "checkerName"));
        addText(linearLayout, sb.toString(), 11, this.faint, false, 6);
    }

    private void compactItems(final String str, JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() == 0) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        boolean z = true;
        double d = 0.0d;
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject optJSONObject = jSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                String first = first(optJSONObject, "standardName", "itemName");
                if (first.isEmpty()) {
                    first = "评分项目";
                }
                String first2 = first(optJSONObject, "stardardScore", "standardScore", "deductScore", "score");
                if (first2.isEmpty()) {
                    arrayList.add(first);
                } else {
                    arrayList.add(first + " −" + first2.replace("-", ""));
                    try {
                        d += Math.abs(Double.parseDouble(first2));
                    } catch (Exception unused) {
                    }
                }
                z = false;
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        final String join = join(arrayList, "\u3000｜\u3000");
        final String str2 = (!z || d <= 0.0d) ? null : "合计扣 " + trim(d) + " 分";
        card(new Card() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.16
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Card
            public final void build(LinearLayout linearLayout) {
                HygieneHistoryActivity.this.m2lambda$compactItems$0$cnzuwehygieneHygieneHistoryActivity(str, str2, join, linearLayout);
            }
        });
    }

    void m2lambda$compactItems$0$cnzuwehygieneHygieneHistoryActivity(String str, String str2, String str3, LinearLayout linearLayout) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.addView(txt(str, 12, this.ink, true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (str2 != null) {
            linearLayout2.addView(txt(str2, 11, this.red, true));
        }
        linearLayout.addView(linearLayout2);
        TextView txt = txt(str3, 11, this.muted, false);
        txt.setLineSpacing(dp(3), 1.0f);
        linearLayout.addView(txt, top(5));
    }

    private void photos(final String str, JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() == 0) {
            return;
        }
        final ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            String trim = jSONArray.optString(i, "").trim();
            if (!trim.isEmpty()) {
                if (trim.startsWith("/")) {
                    trim = HygieneApi.BASE + trim;
                }
                if (trim.startsWith("http")) {
                    arrayList.add(trim);
                }
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        card(new Card() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.17
            @Override // cn.zuwe.hygiene.HygieneHistoryActivity.Card
            public final void build(LinearLayout linearLayout) {
                HygieneHistoryActivity.this.m9lambda$photos$0$cnzuwehygieneHygieneHistoryActivity(str, arrayList, linearLayout);
            }
        });
    }

    void m9lambda$photos$0$cnzuwehygieneHygieneHistoryActivity(String str, final List list, LinearLayout linearLayout) {
        linearLayout.addView(txt(str + " · 点图查看大图", 12, this.ink, true));
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        int i = 0;
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        horizontalScrollView.addView(linearLayout2, new FrameLayout.LayoutParams(-2, dp(96)));
        linearLayout.addView(horizontalScrollView, top(8));
        while (i < list.size()) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(this.bgAlt);
            gradientDrawable.setCornerRadius(dp(8));
            imageView.setBackground(gradientDrawable);
            StringBuilder sb = new StringBuilder("检查照片 ");
            int i2 = i + 1;
            sb.append(i2);
            imageView.setContentDescription(sb.toString());
            final int fi = i;
            imageView.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.18
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    HygieneHistoryActivity.this.m10lambda$photos$1$cnzuwehygieneHygieneHistoryActivity(list, fi, view);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(84), dp(84));
            layoutParams.rightMargin = dp(8);
            linearLayout2.addView(imageView, layoutParams);
            loadThumb((String) list.get(i), imageView);
            i = i2;
        }
    }

    void m10lambda$photos$1$cnzuwehygieneHygieneHistoryActivity(List list, int i, View view) {
        previewGallery(list, i);
    }

    private void loadThumb(final String str, final ImageView imageView) {
        this.io.execute(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.19
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.m8lambda$loadThumb$0$cnzuwehygieneHygieneHistoryActivity(str, imageView);
            }
        });
    }

    void m8lambda$loadThumb$0$cnzuwehygieneHygieneHistoryActivity(String str, final ImageView imageView) {
        final Bitmap downloadBitmap = downloadBitmap(str, 320);
        if (downloadBitmap != null) {
            this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.20
                @Override // java.lang.Runnable
                public final void run() {
                    imageView.setImageBitmap(downloadBitmap);
                }
            });
        }
    }

    private void previewGallery(List<String> list, int i) {
        int i2 = getResources().getDisplayMetrics().widthPixels;
        LinearLayout linearLayout = new LinearLayout(this);
        int i3 = 1;
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(Color.parseColor("#0E0E0D"));
        final HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        horizontalScrollView.addView(linearLayout2, new FrameLayout.LayoutParams(-2, -1));
        int i4 = 0;
        while (i4 < list.size()) {
            LinearLayout linearLayout3 = new LinearLayout(this);
            linearLayout3.setOrientation(i3);
            TextView txt = txt("加载中…", 11, Color.parseColor("#706E67"), false);
            txt.setGravity(17);
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            StringBuilder sb = new StringBuilder("检查照片 ");
            int i5 = i4 + 1;
            sb.append(i5);
            imageView.setContentDescription(sb.toString());
            linearLayout3.addView(imageView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            TextView txt2 = txt("第 " + i5 + " / " + list.size() + " 张 · 左右滑动切换", 11, Color.parseColor("#A5A198"), false);
            txt2.setGravity(17);
            txt2.setPadding(0, dp(4), 0, dp(8));
            linearLayout3.addView(txt2, new LinearLayout.LayoutParams(-1, -2));
            linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(i2, -1));
            loadFull(list.get(i4), imageView, i2, txt);
            i4 = i5;
            i3 = 1;
        }
        linearLayout.addView(horizontalScrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        Button ghost = ghost("关闭");
        ghost.setTextColor(Color.parseColor("#EEEAE0"));
        ghost.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.21
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HygieneHistoryActivity.this.m11lambda$previewGallery$0$cnzuwehygieneHygieneHistoryActivity(view);
            }
        });
        linearLayout.addView(ghost, new LinearLayout.LayoutParams(-1, dp(46)));
        Dialog dialog = new Dialog(this, R.style.Theme_Black_NoTitleBar);
        this.photoDialog = dialog;
        dialog.setContentView(linearLayout);
        this.photoDialog.show();
        final int max = Math.max(0, i) * i2;
        horizontalScrollView.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.22
            @Override // java.lang.Runnable
            public final void run() {
                horizontalScrollView.scrollTo(max, 0);
            }
        });
    }

    void m11lambda$previewGallery$0$cnzuwehygieneHygieneHistoryActivity(View view) {
        Dialog dialog = this.photoDialog;
        if (dialog != null) {
            dialog.dismiss();
            this.photoDialog = null;
        }
    }

    private void loadFull(final String str, final ImageView imageView, final int i, final TextView textView) {
        this.io.execute(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.23
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.m5lambda$loadFull$0$cnzuwehygieneHygieneHistoryActivity(str, i, imageView, textView);
            }
        });
    }

    void m5lambda$loadFull$0$cnzuwehygieneHygieneHistoryActivity(String str, int i, final ImageView imageView, final TextView textView) {
        final Bitmap downloadBitmap = downloadBitmap(str, Math.max(480, i));
        this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.24
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.lambda$loadFull$1(downloadBitmap, imageView, textView);
            }
        });
    }

    static void lambda$loadFull$1(Bitmap bitmap, ImageView imageView, TextView textView) {
        try {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                textView.setVisibility(8);
            } else {
                textView.setText("这张照片加载失败");
            }
        } catch (Exception unused) {
        }
    }

    private Bitmap downloadBitmap(String str, int i) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(30000);
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) SmartCourseLite");
            String cookie = CookieManager.getInstance().getCookie("https://ihome.zuwe.edu.cn/");
            if (cookie != null && !cookie.isEmpty()) {
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
            if (httpURLConnection.getResponseCode() != 200) {
                return null;
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[8192];
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            inputStream.close();
            httpURLConnection.disconnect();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            int i2 = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            if (i > 0) {
                while (true) {
                    int i3 = i2 * 2;
                    if (options.outWidth / i3 < i && options.outHeight / i3 < i) {
                        break;
                    }
                    i2 = i3;
                }
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = i2;
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options2);
        } catch (Throwable unused) {
            return null;
        }
    }

    private void renderPager() {
        if (this.totalPages <= 1) {
            return;
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(16);
        Button ghost = ghost("上一页");
        ghost.setEnabled(this.pageNo > 1);
        ghost.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.25
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HygieneHistoryActivity.this.m14lambda$renderPager$0$cnzuwehygieneHygieneHistoryActivity(view);
            }
        });
        Button ghost2 = ghost("下一页");
        ghost2.setEnabled(this.pageNo < this.totalPages);
        ghost2.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HygieneHistoryActivity.this.m15lambda$renderPager$1$cnzuwehygieneHygieneHistoryActivity(view);
            }
        });
        linearLayout.addView(ghost, new LinearLayout.LayoutParams(0, dp(40), 1.0f));
        TextView txt = txt(this.pageNo + " / " + this.totalPages, 11, this.muted, false);
        txt.setGravity(17);
        linearLayout.addView(txt, new LinearLayout.LayoutParams(dp(72), -2));
        linearLayout.addView(ghost2, new LinearLayout.LayoutParams(0, dp(40), 1.0f));
        this.content.addView(linearLayout, top(10));
    }

    void m14lambda$renderPager$0$cnzuwehygieneHygieneHistoryActivity(View view) {
        this.pageNo--;
        loadHistory(false);
    }

    void m15lambda$renderPager$1$cnzuwehygieneHygieneHistoryActivity(View view) {
        this.pageNo++;
        loadHistory(false);
    }

    private <T> void run(final String str, final Work<T> work, final Done<T> done) {
        if (this.busy) {
            return;
        }
        this.busy = true;
        this.io.execute(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.27
            @Override // java.lang.Runnable
            public final void run() {
                HygieneHistoryActivity.this.m18lambda$run$0$cnzuwehygieneHygieneHistoryActivity(work, done, str);
            }
        });
    }

    void m18lambda$run$0$cnzuwehygieneHygieneHistoryActivity(Work work, final Done done, String str) {
        final String message;
        try {
            final Object run = work.run();
            this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.28
                @Override // java.lang.Runnable
                public final void run() {
                    HygieneHistoryActivity.this.m19lambda$run$1$cnzuwehygieneHygieneHistoryActivity(done, run);
                }
            });
        } catch (Exception e) {
            if (e.getMessage() == null) {
                message = str + "失败";
            } else {
                message = e.getMessage();
            }
            this.main.post(new Runnable() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.29
                @Override // java.lang.Runnable
                public final void run() {
                    HygieneHistoryActivity.this.m20lambda$run$2$cnzuwehygieneHygieneHistoryActivity(message);
                }
            });
        }
    }

    void m19lambda$run$1$cnzuwehygieneHygieneHistoryActivity(Done done, Object obj) {
        this.busy = false;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        done.accept(obj);
    }

    void m20lambda$run$2$cnzuwehygieneHygieneHistoryActivity(String str) {
        this.busy = false;
        if (isAuthError(str)) {
            this.session.clear();
            showLogin("登录已过期，请重新登录");
        } else {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            toast("网络异常，稍后重试：" + str);
        }
    }

    private void card(Card card) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(14), dp(13), dp(14), dp(13));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.cardBg);
        gradientDrawable.setCornerRadius(dp(12));
        gradientDrawable.setStroke(dp(1), this.line);
        linearLayout.setBackground(gradientDrawable);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = dp(10);
        this.content.addView(linearLayout, layoutParams);
        card.build(linearLayout);
    }

    private TextView txt(String str, int i, int i2, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        if (z) {
            textView.setTypeface(Typeface.DEFAULT, 1);
        }
        return textView;
    }

    private void addText(LinearLayout linearLayout, String str, int i, int i2, boolean z, int i3) {
        TextView txt = txt(str, i, i2, z);
        if (i3 > 0) {
            txt.setPadding(0, dp(i3), 0, 0);
        }
        linearLayout.addView(txt);
    }

    private Button ghost(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(12.0f);
        button.setTextColor(this.muted);
        button.setStateListAnimator(null);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(20));
        gradientDrawable.setStroke(dp(1), this.line);
        button.setBackground(gradientDrawable);
        return button;
    }

    private Button primary(String str) {
        Button ghost = ghost(str);
        ghost.setTextColor(Color.parseColor("#161611"));
        ghost.setTypeface(Typeface.DEFAULT, 1);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.green);
        gradientDrawable.setCornerRadius(dp(20));
        ghost.setBackground(gradientDrawable);
        return ghost;
    }

    private void addButton(LinearLayout linearLayout, String str, boolean z, final Runnable runnable) {
        Button primary = z ? primary(str) : ghost(str);
        primary.setOnClickListener(new View.OnClickListener() { // from class: cn.zuwe.hygiene.HygieneHistoryActivity.30
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                runnable.run();
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(42));
        layoutParams.topMargin = dp(8);
        linearLayout.addView(primary, layoutParams);
    }

    private View hairline() {
        View view = new View(this);
        view.setBackgroundColor(this.line);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        return view;
    }

    private LinearLayout.LayoutParams top(int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = dp(i);
        return layoutParams;
    }

    private String first(JSONObject jSONObject, String... strArr) {
        for (String str : strArr) {
            Object opt = jSONObject.opt(str);
            if (opt != null && opt != JSONObject.NULL) {
                String valueOf = String.valueOf(opt);
                if (!valueOf.isEmpty() && !"null".equals(valueOf)) {
                    return valueOf;
                }
            }
        }
        return "";
    }

    private JSONArray array(JSONObject jSONObject, String... strArr) {
        for (String str : strArr) {
            JSONArray optJSONArray = jSONObject.optJSONArray(str);
            if (optJSONArray != null) {
                return optJSONArray;
            }
        }
        return null;
    }

    private static String join(List<String> list, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(str);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private static String trim(double d) {
        return Math.abs(d - Math.rint(d)) < 0.001d ? String.valueOf((long) Math.rint(d)) : String.valueOf(d);
    }

    private int scoreColor(String str) {
        double d;
        try {
            d = Double.parseDouble(str);
        } catch (Exception unused) {
            d = 0.0d;
        }
        if (d >= 90.0d) {
            return this.green;
        }
        if (d >= 80.0d) {
            return Color.parseColor(this.dark ? "#AE7D30" : "#8A6322");
        }
        return this.red;
    }

    private int dp(int i) {
        return (int) ((i * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void toast(String str) {
        Toast.makeText(this, str, 1).show();
    }

    public void backToList() {
        this.showingDetail = false;
        JSONObject jSONObject = this.lastPage;
        if (jSONObject != null) {
            renderListPage(jSONObject, true);
        } else {
            loadHistory(false);
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.showingDetail) {
            backToList();
        } else {
            super.onBackPressed();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.io.shutdownNow();
        Dialog dialog = this.photoDialog;
        if (dialog != null) {
            dialog.dismiss();
            this.photoDialog = null;
        }
        super.onDestroy();
    }
}
