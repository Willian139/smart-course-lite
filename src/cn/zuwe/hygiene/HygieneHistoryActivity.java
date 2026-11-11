package cn.zuwe.hygiene;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zuwe.smartcourselite.AboutFooter;
import com.zuwe.smartcourselite.SvgIcon;
import com.zuwe.smartcourselite.UiPolish;

/** 原生宿舍卫生检查：统一认证登录一次、token 长期复用；样式跟随 App 主题。 */
public final class HygieneHistoryActivity extends Activity {
    private static final int REQ_LOGIN = 7;
    private static final String KEY_LIST_CACHE = "hyg_list_cache_";
    private static final String PREFS = "smartcourse_prefs";
    private static final String KEY_DARK = "theme_dark";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private HygieneSession session;
    private HygieneApi api;
    private LinearLayout content;
    private int pageNo = 1, totalPages = 1;
    private boolean busy;
    private boolean showingDetail;
    private JSONObject lastPage;
    private final java.util.Map<String, JSONObject> detailCache = new java.util.HashMap<>();

    private boolean dark = true;
    private int bg, bgAlt, cardBg, line, ink, muted, faint, green, red;
    private long loadedAt = 0L;

    @Override public void onCreate(Bundle b) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sp = getSharedPreferences(PREFS, 0);
        dark = sp.getBoolean(KEY_DARK, true);
        applyPalette();
        session = new HygieneSession(this);
        windowChrome();
        build();
        api = new HygieneApi(session);
        if (session.isAuthenticated()) {
            renderCachedList();
            loadHistory(true);
        } else {
            startLogin(null);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        // 5 分钟内从后台回来不重拉，避免一进一出就转圈或重复授权
        if (loadedAt > 0L && System.currentTimeMillis() - loadedAt < 5L * 60L * 1000L) return;
        if (session.isAuthenticated() && loadedAt > 0L && !busy) loadHistory(true);
    }

    private void applyPalette() {
        if (dark) {
            bg = Color.parseColor("#131312"); bgAlt = Color.parseColor("#1B1B19");
            cardBg = Color.parseColor("#262622"); line = Color.parseColor("#3A3A35");
            ink = Color.parseColor("#EEEAE0"); muted = Color.parseColor("#A5A198");
            faint = Color.parseColor("#706E67"); green = Color.parseColor("#89997A");
            red = Color.parseColor("#B1847B");
        } else {
            bg = Color.parseColor("#EFEDE6"); bgAlt = Color.parseColor("#E6E3DA");
            cardBg = Color.parseColor("#FBFAF6"); line = Color.parseColor("#DAD6CB");
            ink = Color.parseColor("#191916"); muted = Color.parseColor("#77746B");
            faint = Color.parseColor("#AAA79D"); green = Color.parseColor("#66735D");
            red = Color.parseColor("#936F68");
        }
    }

    private void windowChrome() {
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        int flags = dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void build() {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(bg);
        shell.setFitsSystemWindows(true);

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(dp(18), dp(20), dp(18), dp(12));
        TextView eyebrow = txt("HOUSEKEEPING / 宿舍卫生", 10, faint, false);
        eyebrow.setLetterSpacing(0.12f);
        head.addView(eyebrow);
        TextView title = txt("卫生检查记录", 26, ink, true);
        title.setLetterSpacing(-0.02f);
        head.addView(title, top(6));
        TextView sub = txt("统一认证登录一次，之后长期免重新登录", 11, faint, false);
        head.addView(sub, top(5));
        column.addView(head);
        column.addView(hairline());

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(28));
        column.addView(content, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout about = AboutFooter.build(this, ink, muted, faint, line, cardBg, bgAlt, green);
        about.setPadding(dp(18), 0, dp(18), dp(28));
        column.addView(about, new LinearLayout.LayoutParams(-1, -2));
        sc.addView(column, new LinearLayout.LayoutParams(-1, -2));
        shell.addView(sc, new FrameLayout.LayoutParams(-1, -1));

        // 折叠顶栏：随滚动淡入，标题居中（仿 iOS）
        FrameLayout bar = new FrameLayout(this);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(bg);
        bar.setBackground(barBg);
        bar.setAlpha(0f);
        final TextView barTitle = txt("卫生检查记录", 16, ink, true);
        barTitle.setGravity(Gravity.CENTER);
        barTitle.setAlpha(0f);
        bar.addView(barTitle, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout back = roundIconChip(SvgIcon.CHEV_L, 34);
        back.setContentDescription("返回");
        back.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(dp(34f), dp(34f), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        backLp.leftMargin = dp(12f);
        bar.addView(back, backLp);

        View barLine = new View(this);
        barLine.setBackgroundColor(line);
        bar.addView(barLine, new FrameLayout.LayoutParams(-1, dp(1f), Gravity.BOTTOM));

        shell.addView(bar, new FrameLayout.LayoutParams(-1, dp(52f), Gravity.TOP));
        setContentView(shell);
        com.zuwe.smartcourselite.PageMotion.installBackdrop(this, bg);
        com.zuwe.smartcourselite.PageMotion.fromTop(shell, 1200L);

        final int barH = dp(52f);
        sc.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int y = sc.getScrollY();
            float t = Math.min(1f, Math.max(0f, y / (float) Math.max(1, head.getHeight() - barH)));
            bar.setAlpha(t);
            barTitle.setAlpha(t * t);
            head.setAlpha(1f - 0.9f * t);
            head.setTranslationY(-y * 0.22f);
        });
    }

    /** 圆形图标按钮（iOS 风格，无文字）。 */
    private FrameLayout roundIconChip(String path, int sizeDp) {
        FrameLayout chip = new FrameLayout(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bgAlt);
        d.setCornerRadius(dp(sizeDp / 2f));
        chip.setBackground(d);
        SvgIcon.IconView icon = SvgIcon.view(this, path, ink, sizeDp - 14);
        chip.addView(icon, new FrameLayout.LayoutParams(dp(sizeDp - 14), dp(sizeDp - 14), Gravity.CENTER));
        chip.setClickable(true);
        UiPolish.pressable(chip);
        return chip;
    }

    // ------------------------------------------------------------- 登录

    private void verifyThenLoad() {
        showingDetail = false;
        content.removeAllViews();
        addText(content, "正在校验已有登录状态…", 13, muted, false, 8);
        final String token = session.token();
        io.execute(new Runnable() { @Override public void run() {
            String sn = null, err = null;
            try {
                JSONObject info = api.userInfo();
                HygieneApi.ensureSuccess(info);
                sn = HygieneApi.usernameOf(info);
                if (sn.isEmpty()) err = "登录状态已失效";
            } catch (Exception e) { err = e.getMessage() == null ? "登录状态已失效" : e.getMessage(); }
            final String user = sn, error = err;
            main.post(new Runnable() { @Override public void run() {
                if (user != null) { session.save(token, user, user); loadHistory(user, true); }
                else showLogin(error);
            } });
        } });
    }

    private void startLogin(String reason) {
        showLogin(reason);
        try {
            com.zuwe.smartcourselite.PageMotion.captureBackdrop(this);
            startActivityForResult(new Intent(this, HygieneLoginActivity.class), REQ_LOGIN);
            overridePendingTransition(0, 0);
        }
        catch (Exception e) { toast("无法打开统一认证登录页：" + e.getMessage()); }
    }

    private void showLogin(String reason) {
        showingDetail = false;
        content.removeAllViews();
        card(box -> {
            addText(box, "需要一次统一身份认证", 16, ink, true, 0);
            if (reason != null && !reason.isEmpty()) addText(box, "上次登录不可用：" + reason, 11, red, false, 6);
            addText(box, "登录成功后登录凭据会加密保存在本机，以后直接进入，不用反复登录。", 12, muted, false, 8);
            addText(box, "与课表用的是同一个统一认证（authserver6）：课表认证仍有效时通常自动通过。", 11, faint, false, 6);
            addButton(box, "打开统一认证登录", true, () -> {
                com.zuwe.smartcourselite.PageMotion.captureBackdrop(this);
                startActivityForResult(new Intent(this, HygieneLoginActivity.class), REQ_LOGIN);
                overridePendingTransition(0, 0);
            });
            addButton(box, "重新校验已有登录状态", false, this::verifyThenLoad);
        });
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_LOGIN) return;
        if (res == RESULT_OK) verifyThenLoad();
        else if (!session.isAuthenticated()) showLogin("未完成登录");
    }

    public void onHygieneAuthorizationCompleted(String token, String personSn) {
        if (token == null || token.isEmpty() || personSn == null || personSn.isEmpty()) { toast("授权结果不完整"); return; }
        session.save(token, personSn, personSn);
        verifyThenLoad();
    }

    // ------------------------------------------------------------- 本地缓存 / 错误判定

    /** 无网/进后台被杀后重启时先把上次的记录画出来，避免空白与重复授权。 */
    private void renderCachedList() {
        String raw = getSharedPreferences(PREFS, 0).getString(KEY_LIST_CACHE + session.personSn(), "");
        if (raw == null || raw.isEmpty()) return;
        try {
            JSONObject pg = new JSONObject(raw);
            renderListPage(pg, true);
        } catch (Exception ignored) {
        }
    }

    private void saveListCache(JSONObject pg) {
        if (pg == null) return;
        try {
            getSharedPreferences(PREFS, 0).edit()
                    .putString(KEY_LIST_CACHE + session.personSn(), pg.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    /** 只有真正的认证失效才要求重新授权；网络异常绝不踢回登录页。 */
    private static boolean isAuthError(String msg) {
        if (msg == null) return false;
        return msg.contains("HTTP 401") || msg.contains("HTTP 403") || msg.contains("未登录")
                || msg.contains("登录超时") || msg.contains("登录状态已失效") || msg.contains("无效身份")
                || msg.contains("token 无效") || msg.contains("请先登录");
    }

    // ------------------------------------------------------------- 列表

    private void loadHistory(boolean reset) {
        if (session.token().isEmpty()) { startLogin("尚未登录"); return; }
        if (session.personSn().isEmpty()) { verifyThenLoad(); return; }
        if (busy) return;
        loadHistory(session.personSn(), reset);
    }

    private void loadHistory(String personSn, boolean reset) {
        if (busy) return;
        if (reset) pageNo = 1;
        final int p = pageNo;
        run("正在加载历史记录…", () -> {
            JSONObject root = api.historyPage(p, 10, "", personSn);
            HygieneApi.ensureSuccess(root);
            JSONObject pg = HygieneApi.findPage(root);
            if (pg == null) throw new IOException("分页响应缺少 page 节点");
            return pg;
        }, pg -> {
            lastPage = pg;
            renderListPage(pg, reset);
            saveListCache(pg);
            loadedAt = System.currentTimeMillis();
        });
    }

    /** 列表渲染：只保留当前学期（中国高校：秋季 8/1–次年1/31，春季 2/1–7/31）。 */
    private void renderListPage(JSONObject pg, boolean reset) {
        showingDetail = false;
        if (reset) content.removeAllViews();
        totalPages = Math.max(1, pg.optInt("totalPage", pg.optInt("pages", 1)));
        JSONArray rows = HygieneApi.findList(pg);
        int total = pg.optInt("totalCount", pg.optInt("total", rows.length()));
        if (reset) renderHeader(total);
        int shown = 0;
        for (int i = 0; i < rows.length(); i++) {
            JSONObject o = rows.optJSONObject(i);
            if (o == null) continue;
            if (!inCurrentSemester(first(o, "recordTime", "createTime", "checkTime"))) continue;
            renderRow(o);
            shown++;
        }
        if (shown == 0) addText(content, "本学期暂无检查记录", 13, faint, false, 10);
        String[] sem = currentSemester();
        addText(content, "仅保留本学期 " + sem[0] + " ~ " + sem[1] + " 的记录", 10, faint, false, 4);
        renderPager();
    }

    private String[] currentSemester() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int y = c.get(java.util.Calendar.YEAR), m = c.get(java.util.Calendar.MONTH) + 1;
        if (m >= 8) return new String[]{y + "-08-01", (y + 1) + "-01-31"};
        if (m == 1) return new String[]{(y - 1) + "-08-01", y + "-01-31"};
        return new String[]{y + "-02-01", y + "-07-31"};
    }

    private boolean inCurrentSemester(String stamp) {
        if (stamp == null || stamp.length() < 10) return true;
        String d = stamp.substring(0, 10);
        if (d.length() != 10 || d.charAt(4) != '-') return true;
        String[] sem = currentSemester();
        return d.compareTo(sem[0]) >= 0 && d.compareTo(sem[1]) <= 0;
    }

    private void renderHeader(int total) {
        card(box -> {
            addText(box, "个人检查档案", 15, ink, true, 0);
            addText(box, "共 " + total + " 条记录 · 第 " + pageNo + " / " + totalPages + " 页", 11, muted, false, 5);
            String user = session.user();
            if (!user.isEmpty()) addText(box, "当前账号：" + user, 11, faint, false, 4);
        });
    }

    private void renderRow(JSONObject o) {
        if (o == null) return;
        String id = first(o, "id", "historyRoomId", "recordId");
        String room = first(o, "roomName", "fullName");
        String date = first(o, "recordTime", "createTime", "checkTime");
        String score = first(o, "totalScore", "score");
        card(box -> {
            addText(box, (room.isEmpty() ? "宿舍检查" : room) + "  ·  " + (date.isEmpty() ? "时间未知" : date), 14, ink, true, 0);
            addText(box, "检查计划：" + first(o, "planName", "inspectionPlanName"), 11, faint, false, 5);
            addText(box, "总分 " + (score.isEmpty() ? "—" : score) + " 分", 20, scoreColor(score), true, 8);
            addText(box, "卫生分 " + first(o, "hygieneScore", "hygieneDeductScore") + "   违纪分 " + first(o, "peccancyScore", "peccancyDeductScore"), 11, muted, false, 4);
            addButton(box, "查看检查详情", false, () -> { if (id.isEmpty()) toast("记录缺少详情 ID"); else loadDetail(id); });
        });
    }

    private void loadDetail(final String id) {
        JSONObject cached = detailCache.get(id);
        if (cached != null) { renderDetail(cached); return; }
        showingDetail = true;
        content.removeAllViews();
        addText(content, "正在加载检查详情…", 13, muted, false, 8);
        run("正在加载检查详情…", () -> {
            JSONObject root = api.historyDetail(id);
            HygieneApi.ensureSuccess(root);
            JSONObject data = HygieneApi.unwrapData(root);
            JSONObject d = data.optJSONObject("historyRoom");
            if (d == null) d = data.optJSONObject("detail");
            return d == null ? data : d;
        }, d -> { detailCache.put(id, d); renderDetail(d); });
    }

    private void renderDetail(JSONObject d) {
        showingDetail = true;
        content.removeAllViews();
        addButton(content, "← 返回历史记录", false, this::backToList);
        card(box -> {
            addText(box, first(d, "fullName", "roomName"), 16, ink, true, 0);
            addText(box, "检查计划：" + first(d, "planName"), 11, faint, false, 5);
            addText(box, "检查时间：" + first(d, "recordTime", "checkTime"), 11, muted, false, 6);
            addText(box, "总分 " + first(d, "totalScore", "score") + " 分", 23, scoreColor(first(d, "totalScore", "score")), true, 10);
            addText(box, "卫生分 " + first(d, "hygieneScore") + "   违纪分 " + first(d, "peccancyScore"), 11, muted, false, 4);
            addText(box, "检查人员：" + first(d, "createName", "checkerName"), 11, faint, false, 6);
        });
        compactItems("卫生评分明细", array(d, "hygieneItems", "hygieneItemList"));
        compactItems("违纪扣分明细", array(d, "peccancyItems", "peccancyItemList"));
        photos("现场照片", array(d, "urlList", "photoList"));
        photos("违纪照片", array(d, "urlListToPeccancy", "peccancyPhotoList"));
    }

    /** 紧凑扣分明细：整块小卡片，一行标题 + 一行「项目 −分」小字。 */
    private void compactItems(String title, JSONArray arr) {
        if (arr == null || arr.length() == 0) return;
        List<String> parts = new ArrayList<>();
        double total = 0;
        boolean numeric = true;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject x = arr.optJSONObject(i);
            if (x == null) continue;
            String name = first(x, "standardName", "itemName");
            if (name.isEmpty()) name = "评分项目";
            String sc = first(x, "stardardScore", "standardScore", "deductScore", "score");
            if (sc.isEmpty()) { parts.add(name); numeric = false; continue; }
            parts.add(name + " −" + sc.replace("-", ""));
            try { total += Math.abs(Double.parseDouble(sc)); } catch (Exception e) { numeric = false; }
        }
        if (parts.isEmpty()) return;
        final String joined = join(parts, "　｜　");
        final String totalLine = numeric && total > 0 ? "合计扣 " + trim(total) + " 分" : null;
        card(box -> {
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            TextView t = txt(title, 12, ink, true);
            head.addView(t, new LinearLayout.LayoutParams(0, -2, 1f));
            if (totalLine != null) head.addView(txt(totalLine, 11, red, true));
            box.addView(head);
            TextView detail = txt(joined, 11, muted, false);
            detail.setLineSpacing(dp(3), 1f);
            box.addView(detail, top(5));
        });
    }

    // ------------------------------------------------------------- 照片

    private void photos(String title, JSONArray arr) {
        if (arr == null || arr.length() == 0) return;
        final List<String> urls = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            String u = arr.optString(i, "").trim();
            if (u.isEmpty()) continue;
            if (u.startsWith("/")) u = HygieneApi.BASE + u;
            if (!u.startsWith("http")) continue;
            urls.add(u);
        }
        if (urls.isEmpty()) return;
        card(box -> {
            box.addView(txt(title + " · 点图查看大图", 12, ink, true));
            HorizontalScrollView hs = new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            hs.addView(row, new HorizontalScrollView.LayoutParams(-2, dp(96)));
            box.addView(hs, top(8));
            for (int i = 0; i < urls.size(); i++) {
                final int idx = i;
                ImageView iv = new ImageView(this);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GradientDrawable bgd = new GradientDrawable();
                bgd.setColor(bgAlt); bgd.setCornerRadius(dp(8));
                iv.setBackground(bgd);
                iv.setContentDescription("检查照片 " + (i + 1));
                iv.setOnClickListener(v -> previewGallery(urls, idx));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(84), dp(84));
                lp.rightMargin = dp(8);
                row.addView(iv, lp);
                loadThumb(urls.get(i), iv);
            }
        });
    }

    private void loadThumb(final String url, final ImageView iv) {
        io.execute(() -> {
            Bitmap bm = downloadBitmap(url, 320);
            if (bm != null) main.post(() -> { try { iv.setImageBitmap(bm); } catch (Exception ignored) {} });
        });
    }

    /** 全屏图集：左右滑动切换，图片异步加载，不再跳浏览器。 */
    private void previewGallery(final List<String> urls, int startIndex) {
        final int screenW = getResources().getDisplayMetrics().widthPixels;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#0E0E0D"));
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout pages = new LinearLayout(this);
        pages.setOrientation(LinearLayout.HORIZONTAL);
        hs.addView(pages, new HorizontalScrollView.LayoutParams(-2, -1));
        for (int i = 0; i < urls.size(); i++) {
            LinearLayout page = new LinearLayout(this);
            page.setOrientation(LinearLayout.VERTICAL);
            TextView loading = txt("加载中…", 11, Color.parseColor("#706E67"), false);
            loading.setGravity(Gravity.CENTER);
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setContentDescription("检查照片 " + (i + 1));
            page.addView(iv, new LinearLayout.LayoutParams(-1, 0, 1f));
            TextView cap = txt("第 " + (i + 1) + " / " + urls.size() + " 张 · 左右滑动切换", 11, Color.parseColor("#A5A198"), false);
            cap.setGravity(Gravity.CENTER);
            cap.setPadding(0, dp(4), 0, dp(8));
            page.addView(cap, new LinearLayout.LayoutParams(-1, -2));
            pages.addView(page, new LinearLayout.LayoutParams(screenW, -1));
            loadFull(urls.get(i), iv, screenW, loading);
        }
        box.addView(hs, new LinearLayout.LayoutParams(-1, 0, 1f));
        Button close = ghost("关闭");
        close.setTextColor(Color.parseColor("#EEEAE0"));
        close.setOnClickListener(v -> { if (photoDialog != null) { photoDialog.dismiss(); photoDialog = null; } });
        box.addView(close, new LinearLayout.LayoutParams(-1, dp(46)));
        photoDialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar);
        photoDialog.setContentView(box);
        photoDialog.show();
        final int offset = Math.max(0, startIndex) * screenW;
        hs.post(() -> hs.scrollTo(offset, 0));
    }

    private void loadFull(final String url, final ImageView iv, final int maxSide, final TextView loading) {
        io.execute(() -> {
            Bitmap bm = downloadBitmap(url, Math.max(480, maxSide));
            main.post(() -> {
                try {
                    if (bm != null) { iv.setImageBitmap(bm); loading.setVisibility(View.GONE); }
                    else loading.setText("这张照片加载失败");
                } catch (Exception ignored) {}
            });
        });
    }

    private android.app.Dialog photoDialog;

    private Bitmap downloadBitmap(String url, int maxSide) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000); c.setReadTimeout(30000);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) SmartCourseLite");
            String cookie = android.webkit.CookieManager.getInstance().getCookie(HygieneApi.BASE + "/");
            if (cookie != null && !cookie.isEmpty()) c.setRequestProperty("Cookie", cookie);
            if (c.getResponseCode() != 200) return null;
            InputStream in = c.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            in.close(); c.disconnect();
            byte[] data = out.toByteArray();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            int sample = 1;
            if (maxSide > 0) {
                while (opts.outWidth / (sample * 2) >= maxSide || opts.outHeight / (sample * 2) >= maxSide) sample *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            return BitmapFactory.decodeByteArray(data, 0, data.length, o2);
        } catch (Throwable t) { return null; }
    }

    // ------------------------------------------------------------- 分页

    private void renderPager() {
        if (totalPages <= 1) return;
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        Button prev = ghost("上一页");
        prev.setEnabled(pageNo > 1);
        prev.setOnClickListener(v -> { pageNo--; loadHistory(false); });
        Button next = ghost("下一页");
        next.setEnabled(pageNo < totalPages);
        next.setOnClickListener(v -> { pageNo++; loadHistory(false); });
        row.addView(prev, new LinearLayout.LayoutParams(0, dp(40), 1f));
        TextView p = txt(pageNo + " / " + totalPages, 11, muted, false);
        p.setGravity(Gravity.CENTER);
        row.addView(p, new LinearLayout.LayoutParams(dp(72), -2));
        row.addView(next, new LinearLayout.LayoutParams(0, dp(40), 1f));
        content.addView(row, top(10));
    }

    // ------------------------------------------------------------- 工具

    private interface Work<T> { T run() throws Exception; }
    private interface Done<T> { void accept(T v); }
    private <T> void run(String label, Work<T> w, Done<T> done) {
        if (busy) return;
        busy = true;
        io.execute(() -> {
            try {
                T v = w.run();
                main.post(() -> { busy = false; done.accept(v); });
            } catch (Exception e) {
                final String msg = e.getMessage() == null ? label + "失败" : e.getMessage();
                main.post(() -> {
                    busy = false;
                    if (isAuthError(msg)) {
                        showLogin("登录状态已失效");
                    } else {
                        toast("网络异常，稍后重试：" + msg);
                    }
                });
            }
        });
    }

    private interface Card { void build(LinearLayout box); }
    private void card(Card c) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setPadding(dp(14), dp(13), dp(14), dp(13));
        GradientDrawable bgd = new GradientDrawable();
        bgd.setColor(cardBg); bgd.setCornerRadius(dp(12)); bgd.setStroke(dp(1), line);
        b.setBackground(bgd);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        content.addView(b, lp);
        c.build(b);
    }

    private TextView txt(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void addText(LinearLayout p, String s, int sp, int color, boolean bold, int topDp) {
        TextView t = txt(s, sp, color, bold);
        if (topDp > 0) t.setPadding(0, dp(topDp), 0, 0);
        p.addView(t);
    }

    private Button ghost(String text) {
        Button b = new Button(this);
        b.setText(text); b.setAllCaps(false); b.setTextSize(12f); b.setTextColor(muted);
        b.setStateListAnimator(null);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bgAlt); d.setCornerRadius(dp(20)); d.setStroke(dp(1), line);
        b.setBackground(d);
        return b;
    }

    private Button primary(String text) {
        Button b = ghost(text);
        b.setTextColor(Color.parseColor("#161611"));
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable d = new GradientDrawable();
        d.setColor(green); d.setCornerRadius(dp(20));
        b.setBackground(d);
        return b;
    }

    private void addButton(LinearLayout p, String s, boolean primaryStyle, Runnable r) {
        Button b = primaryStyle ? primary(s) : ghost(s);
        b.setOnClickListener(v -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(42));
        lp.topMargin = dp(8);
        p.addView(b, lp);
    }

    private View hairline() {
        View v = new View(this);
        v.setBackgroundColor(line);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        return v;
    }

    private LinearLayout.LayoutParams top(int dpValue) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(dpValue);
        return lp;
    }

    private String first(JSONObject o, String... keys) {
        for (String k : keys) {
            Object v = o.opt(k);
            if (v != null && v != JSONObject.NULL) {
                String s = String.valueOf(v);
                if (!s.isEmpty() && !"null".equals(s)) return s;
            }
        }
        return "";
    }

    private JSONArray array(JSONObject o, String... keys) {
        for (String k : keys) { JSONArray a = o.optJSONArray(k); if (a != null) return a; }
        return null;
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) { if (i > 0) sb.append(sep); sb.append(parts.get(i)); }
        return sb.toString();
    }

    private static String trim(double d) {
        if (Math.abs(d - Math.rint(d)) < 0.001) return String.valueOf((long) Math.rint(d));
        return String.valueOf(d);
    }

    private int scoreColor(String s) {
        try {
            double n = Double.parseDouble(s);
            if (n >= 90) return green;
            if (n >= 80) return Color.parseColor(dark ? "#AE7D30" : "#8A6322");
        } catch (Exception ignored) {}
        return red;
    }

    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }

    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private void backToList() {
        showingDetail = false;
        if (lastPage != null) renderListPage(lastPage, true);
        else loadHistory(false);
    }

    @Override public void onBackPressed() {
        if (showingDetail) { backToList(); return; }
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        io.shutdownNow();
        if (photoDialog != null) { photoDialog.dismiss(); photoDialog = null; }
        super.onDestroy();
    }
}
