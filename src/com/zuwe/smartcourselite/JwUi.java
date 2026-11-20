package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 教务系统课表面板：账号密码登录 -> 一键获取 -> 选周次 -> 看整周课表 / 填充到主界面。
 */
public final class JwUi implements View.OnClickListener, Runnable,
        CompoundButton.OnCheckedChangeListener, JwWebLogin.Callback {

    private static final int C_TEXT = 0xFF1B2430;
    private static final int C_SUB = 0xFF6B7684;
    private static final int C_LINE = 0xFFE2E7EE;
    private static final int C_ACCENT = 0xFF2F6BFF;
    private static final int C_OK = 0xFF1F8A4C;
    private static final int C_ERR = 0xFFC0392B;
    private static final int C_BG = 0xFFFFFFFF;

    private static final int[] CELL_BG = new int[]{
            0xFFE8F0FF, 0xFFE6F7EE, 0xFFFFF3E0, 0xFFF3E8FF,
            0xFFFFE9E9, 0xFFE0F7FA, 0xFFF1F8E9, 0xFFFFF8E1
    };
    private static final int[] CELL_FG = new int[]{
            0xFF1D4ED8, 0xFF146C43, 0xFF9A5B00, 0xFF6B21A8,
            0xFFB02A2A, 0xFF00696E, 0xFF33691E, 0xFF8D6E00
    };

    private final Activity activity;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Dialog dialog;
    private EditText inputUser;
    private EditText inputPass;
    private CheckBox chkShow;
    private Button btnLogin;
    private Button btnWeb;
    private Button btnFill;
    private ProgressBar progressBar;
    private TextView progressText;
    private int progDone;
    private int progTotal;
    private Button btnDiag;
    private TextView statusView;
    private TextView weekLabel;
    private HorizontalScrollView chipScroll;
    private LinearLayout chipsRow;
    private LinearLayout gridWrap;
    private TextView placeholder;

    private final JwApi api = new JwApi();
    private LinearLayout captchaBox;
    private EditText inputCaptcha;
    private ImageView captchaImg;
    private boolean captchaBusy;

    private JwApi.Result result;
    private boolean fillPending;
    private String fillMsg;
    private List<Course> fillToday;
    private boolean fillOk;
    private int weekIdx;

    // 线程回调
    private JwApi.Result pending;
    private String pendingErr;
    private byte[] pendingCapImg;
    private String pendingCapErr;

    public JwUi(Activity activity) {
        this.activity = activity;
    }

    public Activity activity() {
        return activity;
    }

    private static final String PREF = "smartcourse_prefs";
    private static final String K_USER = "jw_user";
    private static final String K_PASS = "jw_pass";
    private static final byte[] K_XOR = "SmartCourseJw2026".getBytes();

    private static String obf(String s) {
        if (s == null) return "";
        try {
            byte[] b = s.getBytes("UTF-8");
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) (b[i] ^ K_XOR[i % K_XOR.length]);
            }
            return Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Throwable t) {
            return "";
        }
    }

    private static String deobf(String s) {
        if (s == null || s.length() == 0) return "";
        try {
            byte[] b = Base64.decode(s, Base64.NO_WRAP);
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) (b[i] ^ K_XOR[i % K_XOR.length]);
            }
            return new String(b, "UTF-8");
        } catch (Throwable t) {
            return "";
        }
    }

    private void restoreCreds() {
        try {
            SharedPreferences p = activity.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String u = p.getString(K_USER, "");
            String e = p.getString(K_PASS, "");
            if (u != null && u.length() > 0 && inputUser != null) inputUser.setText(u);
            String pw = deobf(e);
            if (pw.length() > 0 && inputPass != null) inputPass.setText(pw);
        } catch (Throwable ignored) {
        }
    }

    private void rememberCreds(String user, String pass) {
        try {
            SharedPreferences p = activity.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            p.edit().putString(K_USER, user).putString(K_PASS, obf(pass)).apply();
        } catch (Throwable ignored) {
        }
    }

    public static void show(Activity activity) {
        new JwUi(activity).open();
    }

    private int dp(float v) {
        return (int) (v * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable bg(int color, int radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }

    // ------------------------------------------------------------- 界面

    private void open() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(bg(C_BG, dp(16)));
        root.setPadding(dp(14), dp(14), dp(14), dp(10));

        TextView title = new TextView(activity);
        title.setText("教务系统课表");
        title.setTextSize(17f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(C_TEXT);
        root.addView(title);

        TextView hint = new TextView(activity);
        hint.setText("用教务系统账号登录，登录后自动取回整学期课表");
        hint.setTextSize(11f);
        hint.setTextColor(C_SUB);
        hint.setPadding(0, dp(3), 0, dp(10));
        root.addView(hint);

        btnWeb = new Button(activity);
        btnWeb.setText("网页登录教务系统");
        btnWeb.setAllCaps(false);
        btnWeb.setTextSize(15f);
        btnWeb.setTextColor(Color.WHITE);
        btnWeb.setBackground(bg(C_ACCENT, dp(10)));
        btnWeb.setOnClickListener(this);
        LinearLayout.LayoutParams lpWeb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        lpWeb.topMargin = dp(2);
        root.addView(btnWeb, lpWeb);

        statusView = new TextView(activity);
        statusView.setTextSize(11f);
        statusView.setTextColor(C_SUB);
        statusView.setPadding(0, dp(7), 0, 0);
        statusView.setText("未登录");
        root.addView(statusView);

        weekLabel = new TextView(activity);
        weekLabel.setTextSize(12f);
        weekLabel.setTextColor(C_TEXT);
        weekLabel.setTypeface(Typeface.DEFAULT_BOLD);
        weekLabel.setPadding(0, dp(8), 0, dp(4));
        weekLabel.setVisibility(View.GONE);
        root.addView(weekLabel);

        chipScroll = new HorizontalScrollView(activity);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setVisibility(View.GONE);
        chipsRow = new LinearLayout(activity);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipScroll.addView(chipsRow, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(chipScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ScrollView gridScroll = new ScrollView(activity);
        LinearLayout.LayoutParams lpGridScroll = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        lpGridScroll.topMargin = dp(6);
        gridWrap = new LinearLayout(activity);
        gridWrap.setOrientation(LinearLayout.VERTICAL);

        placeholder = new TextView(activity);
        placeholder.setText("需连接校园网");
        placeholder.setTextSize(24f);
        placeholder.setTypeface(Typeface.DEFAULT_BOLD);
        placeholder.setTextColor(C_ERR);
        placeholder.setGravity(Gravity.CENTER_HORIZONTAL);
        placeholder.setPadding(0, dp(56), 0, dp(10));
        gridWrap.addView(placeholder);

        TextView placeholder2 = new TextView(activity);
        placeholder2.setText("宿舍 Wi-Fi / 手机流量打不开教务，请先连校园网再登录");
        placeholder2.setTextSize(12f);
        placeholder2.setTextColor(C_SUB);
        placeholder2.setGravity(Gravity.CENTER_HORIZONTAL);
        placeholder2.setPadding(dp(10), 0, dp(10), 0);
        gridWrap.addView(placeholder2);
        HorizontalScrollView gridH = new HorizontalScrollView(activity);
        gridH.setHorizontalScrollBarEnabled(false);
        gridH.addView(gridWrap, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        gridScroll.addView(gridH, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(gridScroll, lpGridScroll);

        btnFill = new Button(activity);
        btnFill.setText("填充整学期到主界面");
        btnFill.setAllCaps(false);
        btnFill.setTextSize(13f);
        btnFill.setTextColor(Color.WHITE);
        btnFill.setBackground(bg(C_OK, dp(10)));
        btnFill.setOnClickListener(this);
        btnFill.setVisibility(View.GONE);
        LinearLayout.LayoutParams lpFill = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        lpFill.topMargin = dp(8);
        root.addView(btnFill, lpFill);

        progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(126);
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams lpProg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        lpProg.topMargin = dp(6);
        root.addView(progressBar, lpProg);

        progressText = new TextView(activity);
        progressText.setTextSize(11f);
        progressText.setTextColor(C_SUB);
        progressText.setVisibility(View.GONE);
        root.addView(progressText);

        btnDiag = new Button(activity);
        btnDiag.setText("复制诊断信息（出问题时点这个，把内容发给我）");
        btnDiag.setAllCaps(false);
        btnDiag.setTextSize(12f);
        btnDiag.setTextColor(C_SUB);
        btnDiag.setBackground(bg(0xFFF0F2F6, dp(10)));
        btnDiag.setOnClickListener(this);
        LinearLayout.LayoutParams lpDiag = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(36));
        lpDiag.topMargin = dp(6);
        root.addView(btnDiag, lpDiag);

        Button btnClose = new Button(activity);
        btnClose.setText("关闭");
        btnClose.setAllCaps(false);
        btnClose.setTextSize(13f);
        btnClose.setTextColor(C_SUB);
        btnClose.setBackground(bg(0xFFF0F2F6, dp(10)));
        btnClose.setOnClickListener(this);
        LinearLayout.LayoutParams lpClose = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(38));
        lpClose.topMargin = dp(6);
        root.addView(btnClose, lpClose);

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            w.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // ------------------------------------------------------------- 交互

    @Override
    public void onCheckedChanged(CompoundButton b, boolean checked) {
        if (b == chkShow && inputPass != null) {
            int type = InputType.TYPE_CLASS_TEXT;
            if (!checked) {
                type = type | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
            inputPass.setInputType(type);
            if (inputPass.getText() != null) {
                inputPass.setSelection(inputPass.getText().length());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnWeb) {
            JwWebLogin.show(this, this);
        } else if (v == btnFill) {
            doFill();
        } else if (v == btnDiag) {
            copyDiag();
        } else if (v == null) {
            return;
        } else if (v.getTag() instanceof Integer) {
            weekIdx = ((Integer) v.getTag()).intValue();
            redrawChips();
            redrawGrid();
        } else {
            dialog.dismiss();
        }
    }

    public JwApi api() {
        return api;
    }

    /** JwWebLogin 在网页里直接取到课表 JSON 后回调。 */
    @Override
    public void onJson(String body) {
        try {
            JwApi.Result r = api.parseRaw(body, JwApi.curXnm(), JwApi.curXqm());
            postOk(r);
        } catch (Throwable t) {
            postErr("解析课表失败：" + t.getMessage());
        }
    }

    /** JwWebLogin 成功拿到 Cookie 后回调。 */
    @Override
    public void onCookie(String cookie) {
        if (cookie == null || cookie.length() == 0) {
            status("没拿到网页登录的会话，请重试", C_ERR);
            return;
        }
        status("正在用网页会话获取课表…", C_SUB);
        JwLog.w(activity, "ui", "login via webview cookie");
        new JwWorker(this, api, cookie, JwApi.curXnm(), JwApi.curXqm()).start();
    }

    private void doLogin() {
        if (btnLogin == null || inputUser == null || inputPass == null) return;
        String user = inputUser.getText() == null ? "" : inputUser.getText().toString().trim();
        String pass = inputPass.getText() == null ? "" : inputPass.getText().toString();
        if (user.length() == 0 || pass.length() == 0) {
            status("请先填写账号和密码", C_ERR);
            return;
        }
        String xnm = JwApi.curXnm();
        String xqm = JwApi.curXqm();
        btnLogin.setEnabled(false);
        status("正在登录教务系统…", C_SUB);
        rememberCreds(user, pass);
        JwLog.w(activity, "ui", "login start user=" + user + " xnm=" + xnm + " xqm=" + xqm);
        String cap = inputCaptcha.getText() == null ? "" : inputCaptcha.getText().toString().trim();
        JwWorker worker = new JwWorker(this, api, user, pass, cap, xnm, xqm);
        worker.start();
    }

    public void postOk(JwApi.Result r) {
        pending = r;
        pendingErr = null;
        handler.post(this);
    }

    public void postErr(String msg) {
        pending = null;
        pendingErr = msg == null ? "未知错误" : msg;
        handler.post(this);
    }

    @Override
    public void run() {
        if (progTotal > 0) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(progTotal);
                progressBar.setProgress(progDone);
            }
            if (progressText != null) {
                progressText.setVisibility(View.VISIBLE);
                progressText.setText("正在写整学期课表：" + progDone + " / " + progTotal + " 天");
            }
            if (progDone >= progTotal) progTotal = 0;
            return;
        }
        if (fillPending) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (progressText != null) progressText.setVisibility(View.GONE);
            fillPending = false;
            if (btnFill != null) btnFill.setEnabled(true);
            status(fillMsg == null ? "填充完成" : fillMsg, fillOk ? C_OK : C_ERR);
            if (fillOk && fillToday != null && activity instanceof MainActivity) {
                try {
                    ((MainActivity) activity).render(fillToday);
                } catch (Throwable ignored) {
                }
            }
            return;
        }
        if (pendingErr != null) {
            status("失败：" + pendingErr, C_ERR);
            JwLog.w(activity, "ui", "fail: " + pendingErr);
            pendingErr = null;
            return;
        }
        if (pending == null) return;
        result = pending;
        pending = null;
        weekIdx = guessWeek();
        redrawChips();
        redrawGrid();
        btnFill.setVisibility(View.VISIBLE);
        String who = result.student.length() > 0 ? result.student + " · " : "";
        status("成功（" + result.source + "）：" + who + result.items.size() + " 条课程，"
                + result.weeks.size() + " 个教学周", C_OK);
        JwLog.w(activity, "ui", "ok source=" + result.source + " items=" + result.items.size()
                + " weeks=" + result.weeks.size());
    }

    /** 后台探测验证码；系统不需要验证码时保持隐藏。 */
    public void refreshCaptcha() {
        if (captchaBusy) return;
        captchaBusy = true;
        new JwCaptchaTask(this, api).start();
    }

    /** 由 JwCaptchaTask 在后台线程调用。 */
    public void postCaptcha(byte[] img, String err) {
        pendingCapImg = img;
        pendingCapErr = err;
        handler.post(this);
    }

    private void applyCaptcha(byte[] img, String err) {
        captchaBusy = false;
        if (captchaImg == null || captchaBox == null || inputCaptcha == null) return;
        if (img == null || img.length == 0) {
            if (err != null && err.length() > 0) {
                status("验证码加载失败：" + err, C_ERR);
            }
            return;
        }
        try {
            Bitmap bm = BitmapFactory.decodeByteArray(img, 0, img.length);
            if (bm == null) return;
            captchaImg.setImageBitmap(bm);
            captchaBox.setVisibility(View.VISIBLE);
            inputCaptcha.setText("");
        } catch (Throwable t) {
            // ignore
        }
    }

    private int guessWeek() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < result.weeks.size(); i++) {
            JwApi.WeekRange w = result.weeks.get(i);
            if (w.start != null && w.end != null
                    && !today.isBefore(w.start) && !today.isAfter(w.end)) {
                return i;
            }
        }
        return 0;
    }

    public void status(String text, int color) {
        statusView.setText(text);
        statusView.setTextColor(color);
    }

    private void redrawChips() {
        chipsRow.removeAllViews();
        chipScroll.setVisibility(View.VISIBLE);
        if (result == null) return;
        for (int i = 0; i < result.weeks.size(); i++) {
            JwApi.WeekRange w = result.weeks.get(i);
            TextView chip = new TextView(activity);
            chip.setText("第" + w.week + "周");
            chip.setTextSize(11f);
            boolean on = i == weekIdx;
            chip.setTextColor(on ? Color.WHITE : C_TEXT);
            chip.setBackground(bg(on ? C_ACCENT : 0xFFF0F2F6, dp(14)));
            chip.setPadding(dp(11), dp(5), dp(11), dp(5));
            chip.setTag(Integer.valueOf(i));
            chip.setOnClickListener(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(6);
            chipsRow.addView(chip, lp);
        }
    }

    private void redrawGrid() {
        gridWrap.removeAllViews();
        placeholder = null;
        if (result == null || result.weeks.isEmpty()) return;
        if (weekIdx < 0 || weekIdx >= result.weeks.size()) weekIdx = 0;
        JwApi.WeekRange wk = result.weeks.get(weekIdx);
        weekLabel.setVisibility(View.VISIBLE);
        weekLabel.setText("第 " + wk.week + " 周 · " + wk.start + " 至 " + wk.end);

        int cellW = dp(62);
        int cellH = dp(46);
        int headH = dp(24);
        String[] dayNames = new String[]{"一", "二", "三", "四", "五", "六", "日"};

        LinearLayout head = new LinearLayout(activity);
        head.setOrientation(LinearLayout.HORIZONTAL);
        TextView blank = new TextView(activity);
        blank.setText("节");
        blank.setTextSize(9f);
        blank.setTextColor(C_SUB);
        blank.setGravity(Gravity.CENTER);
        head.addView(blank, new LinearLayout.LayoutParams(dp(22), headH));
        for (int d = 0; d < 7; d++) {
            TextView t = new TextView(activity);
            t.setText(dayNames[d]);
            t.setTextSize(10f);
            t.setTextColor(C_TEXT);
            t.setGravity(Gravity.CENTER);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            LocalDate date = wk.start == null ? null : wk.start.plusDays(d);
            if (date != null) {
                t.setText(dayNames[d] + "\n" + (date.getMonthValue()) + "/" + date.getDayOfMonth());
                t.setTextSize(8.5f);
                t.setLines(2);
            }
            head.addView(t, new LinearLayout.LayoutParams(cellW, headH));
        }
        gridWrap.addView(head);

        for (int period = 1; period <= 12; period++) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView p = new TextView(activity);
            p.setText(String.valueOf(period));
            p.setTextSize(8.5f);
            p.setTextColor(C_SUB);
            p.setGravity(Gravity.CENTER);
            row.addView(p, new LinearLayout.LayoutParams(dp(22), cellH));
            for (int d = 1; d <= 7; d++) {
                JwApi.KbItem hit = find(d, period, wk.week);
                TextView cell = new TextView(activity);
                cell.setTextSize(7.5f);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(dp(1), dp(1), dp(1), dp(1));
                cell.setLines(4);
                if (hit != null) {
                    cell.setText(shortName(hit.name) + "\n" + hit.room);
                    int idx = Math.abs(hit.name.hashCode()) % CELL_BG.length;
                    cell.setBackground(bg(CELL_BG[idx], dp(4)));
                    cell.setTextColor(CELL_FG[idx]);
                } else {
                    cell.setBackground(bg(0xFFF7F8FA, dp(4)));
                    cell.setTextColor(C_SUB);
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cellW, cellH);
                lp.setMargins(dp(1), dp(1), dp(1), dp(1));
                row.addView(cell, lp);
            }
            gridWrap.addView(row);
        }
    }

    private JwApi.KbItem find(int weekDay, int period, int week) {
        if (result == null) return null;
        for (int i = 0; i < result.items.size(); i++) {
            JwApi.KbItem it = result.items.get(i);
            if (it.weekDay != weekDay) continue;
            if (period < it.start || period > it.end) continue;
            if (!it.inWeek(week)) continue;
            return it;
        }
        return null;
    }

    private static String shortName(String name) {
        if (name == null) return "";
        String s = name;
        int cut = s.indexOf('（');
        if (cut < 0) cut = s.indexOf('(');
        if (cut > 2) s = s.substring(0, cut);
        if (s.length() > 6) s = s.substring(0, 6);
        return s;
    }

    private void copyDiag() {
        try {
            String text = JwLog.dump();
            if (text == null || text.length() == 0) {
                text = "（还没有日志，先点一次「登录并获取课表」）";
            }
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    activity.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("jw", text));
            Toast.makeText(activity, "诊断信息已复制，粘贴发给我即可", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(activity, "复制失败：" + t.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void doFill() {
        if (result == null || result.weeks.isEmpty()) {
            status("请先登录并获取课表", C_ERR);
            return;
        }
        if (!(activity instanceof MainActivity)) {
            status("当前界面不支持填充", C_ERR);
            return;
        }
        btnFill.setEnabled(false);
        progDone = 0;
        progTotal = 0;
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(126);
            progressBar.setProgress(0);
        }
        if (progressText != null) {
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("正在写整学期课表：准备中…");
        }
        status("正在把整学期教务教室覆盖到课表（后台进行，界面不会卡）…", C_SUB);
        new JwFillTask(this, (MainActivity) activity, result).start();
    }

    /** JwFillTask 每处理几天回调一次进度。 */
    public void postProgress(int done, int total) {
        progDone = done;
        progTotal = total;
        handler.post(this);
    }

    /** JwFillTask 合并完成后在后台线程回调。 */
    public void postFill(String msg, List<Course> todayList, boolean ok) {
        fillPending = true;
        fillMsg = msg;
        fillToday = todayList;
        fillOk = ok;
        handler.post(this);
    }

    static final class MergeStat {
        final List<Course> list = new ArrayList<Course>();
        int added;
        int changed;
        int filled;
    }

    /** 这一天到底有没有缓存（有但解析不出来时不能覆盖）。 */
    /** -1=没有这个键，0=空数组，>0=有记录，-2=有内容但解析不了 */
    static int cacheSize(Context c, LocalDate date) {
        try {
            SharedPreferences p = c.getSharedPreferences("smartcourse_prefs", 0);
            String raw = p.getString("cache_" + date.toString(), null);
            if (raw == null) return -1;
            if (raw.trim().length() == 0) return 0;
            return new JSONArray(raw).length();
        } catch (Throwable t) {
            return -2;
        }
    }

    static boolean cachePresent(Context c, LocalDate date) {
        try {
            String s = c.getSharedPreferences("smartcourse_prefs", 0)
                    .getString("cache_" + date.toString(), null);
            return s != null && s.length() > 0;
        } catch (Throwable t) {
            return true;
        }
    }

    /** 读某天已有的缓存（来自智慧思政）。 */
    static List<Course> loadCacheCourses(Context c, LocalDate date) {
        List<Course> out = new ArrayList<Course>();
        try {
            String s = c.getSharedPreferences("smartcourse_prefs", 0)
                    .getString("cache_" + date.toString(), null);
            if (s == null || s.length() == 0) return out;
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                try {
                    out.add(new Course(o));
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    /**
     * 以智慧思政为准合并：
     * - 同一节次、同一门课：只补空缺的教室/老师；
     * - 同一节次却是另一门课：保留智慧思政的，标「改课」；
     * - 教务有、智慧思政没有：视为没课，不写入；
     * - 智慧思政有、教务课表里没有：保留并标「加课」。
     */
    static MergeStat mergeCourses(List<Course> old, List<Course> now) {
        MergeStat st = new MergeStat();
        boolean[] used = new boolean[now.size()];
        for (int i = 0; i < old.size(); i++) {
            Course a = old.get(i);
            String name = stripMarks(a.name);
            String room0 = a.room == null ? "" : a.room.trim();
            String teacher0 = a.teacher == null ? "" : a.teacher.trim();
            int hit = findIndex(now, used, name, a.startSection, a.endSection);
            String room = room0;
            String teacher = teacher0;
            if (hit >= 0) {
                used[hit] = true;
                Course b = now.get(hit);
                // 两边都有就按智慧思政的来；智慧思政没有教室才用教务的
                if (isEmpty(room0) && !isEmpty(b.room)) {
                    room = b.room.trim();
                    st.filled++;
                }
                if (teacher.length() == 0 && b.teacher != null && b.teacher.trim().length() > 0) {
                    teacher = b.teacher.trim();
                }
            }
            if (room.equals(room0) && teacher.equals(teacher0)) {
                kept_add(st, name.equals(a.name == null ? "" : a.name) ? a : withName(a, name));
            } else {
                kept_add(st, withAll(a, name, teacher, room));
            }
        }
        List<Course> dd = dedupe(st.list);
        st.list.clear();
        st.list.addAll(dd);
        return st;
    }

    /** 同一个课程名、同一节次的条目只留一条（清掉之前版本造成的重复）。 */
    private static List<Course> dedupe(List<Course> list) {
        List<Course> out = new ArrayList<Course>();
        for (int i = 0; i < list.size(); i++) {
            Course a = list.get(i);
            boolean dup = false;
            for (int j = 0; j < out.size(); j++) {
                Course b = out.get(j);
                if (!similar(stripMarks(a.name), stripMarks(b.name))) continue;
                if (a.startSection == b.startSection
                        || a.startSection == 0 || b.startSection == 0) {
                    dup = true;
                    break;
                }
            }
            if (!dup) out.add(a);
        }
        return out;
    }

    private static int findIndex(List<Course> now, boolean[] used, String name, int start, int end) {
        for (int i = 0; i < now.size(); i++) {
            if (used[i]) continue;
            Course b = now.get(i);
            if (b.startSection != start) continue;
            if (similar(name, b.name)) return i;
        }
        for (int i = 0; i < now.size(); i++) {
            if (used[i]) continue;
            Course b = now.get(i);
            if (b.startSection > start || b.endSection < end) continue;
            if (similar(name, b.name)) return i;
        }
        // 3) 节次对不上也认，只要课程名像，避免同一门课出现两遍
        for (int i = 0; i < now.size(); i++) {
            if (used[i]) continue;
            if (similar(name, now.get(i).name)) return i;
        }
        return -1;
    }

    private static void kept_add(MergeStat st, Course c) {
        st.list.add(c);
    }

    private static Course findSame(List<Course> now, String name, int start, int end) {
        for (int i = 0; i < now.size(); i++) {
            Course b = now.get(i);
            if (b.startSection != start) continue;
            if (similar(name, b.name)) return b;
        }
        for (int i = 0; i < now.size(); i++) {
            Course b = now.get(i);
            if (b.startSection > start || b.endSection < end) continue;
            if (similar(name, b.name)) return b;
        }
        return null;
    }

    private static boolean similar(String a, String b) {
        String x = cleanName(a);
        String y = cleanName(b);
        if (x.length() == 0 || y.length() == 0) return false;
        if (x.equals(y)) return true;
        return x.startsWith(y) || y.startsWith(x);
    }

    private static boolean isEmpty(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.length() == 0) return true;
        return t.indexOf("未排") >= 0 || t.indexOf("详见") >= 0 || t.equals("-");
    }

    /** 去掉历史上误加到课程名后面的标记。 */
    private static String stripMarks(String s) {
        if (s == null) return "";
        String r = s;
        for (int i = 0; i < 8; i++) {
            String n = r.replace("（改课）", "").replace("（加课）", "")
                    .replace("(改课)", "").replace("(加课)", "");
            if (n.equals(r)) break;
            r = n;
        }
        return r.trim();
    }

    private static boolean sameCourse(String a, String b) {
        String x = cleanName(a);
        String y = cleanName(b);
        return x.length() > 0 && x.equals(y);
    }

    private static String cleanName(String s) {
        if (s == null) return "";
        String r = s.replace("（加课）", "").replace("（改课）", "");
        r = r.replace("(", "").replace(")", "").replace(" ", "").replace("（", "").replace("）", "");
        return r.trim();
    }

    private static Course withName(Course c, String name) {
        return withAll(c, name, c.teacher, c.room);
    }

    private static Course withAll(Course c, String name, String teacher, String room) {
        try {
            JSONObject o = new JSONObject();
            o.put("courseName", name);
            o.put("teacherName", teacher == null ? "" : teacher);
            o.put("classRoom", room == null ? "" : room);
            o.put("date", c.date);
            o.put("startSection", c.startSection);
            o.put("endSection", c.endSection);
            o.put("week", c.week);
            o.put("weekDay", c.weekDay);
            return new Course(o);
        } catch (Throwable t) {
            return c;
        }
    }

    private List<Course> coursesFor(int weekIdx2, int weekDay) {
        List<Course> out = new ArrayList<Course>();
        if (result == null || weekIdx2 < 0 || weekIdx2 >= result.weeks.size()) return out;
        JwApi.WeekRange wk = result.weeks.get(weekIdx2);
        if (wk.start == null) return out;
        LocalDate date = wk.start.plusDays(weekDay - 1);
        for (int i = 0; i < result.items.size(); i++) {
            JwApi.KbItem it = result.items.get(i);
            if (it.weekDay != weekDay) continue;
            if (!it.inWeek(wk.week)) continue;
            try {
                JSONObject o = new JSONObject();
                o.put("courseName", it.name);
                o.put("teacherName", it.teacher);
                o.put("classRoom", it.room);
                o.put("date", date.toString());
                o.put("startSection", it.start);
                o.put("endSection", it.end);
                o.put("week", wk.week);
                o.put("weekDay", weekDay);
                out.add(new Course(o));
            } catch (Throwable t) {
                // 单条失败忽略
            }
        }
        return out;
    }
}