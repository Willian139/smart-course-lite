package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 周次总览：当前选择那一周的全部课程，卡片式课程表。 */
public final class WeekOverviewActivity extends Activity {
    private static final String PREFS = "smartcourse_prefs";
    private static final String KEY_DARK = "theme_dark";
    private static final String DAY_CACHE = "cache_";
    private static final String DAY_TS = "cache_ts_";
    private static final long STALE_MS = 2L * 60L * 60L * 1000L;
    private static final int SECTIONS = 12;
    private static final DateTimeFormatter FMT_MD = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA);
    private static final String[] WEEK_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newFixedThreadPool(7);

    private int bg, bgAlt, cardBg, line, ink, muted, faint, green, blue, red, onGreen;
    private boolean dark = true;
    private LocalDate monday = WeekUtil.monday(LocalDate.now());
    private int weekNo = 1;

    private TextView title, sub, status;
    private TextView[] dayName = new TextView[7];
    private TextView[] dayDate = new TextView[7];
    private LinearLayout[] cols = new LinearLayout[7];
    private LinearLayout card;
    private int labelW, colW, rowH, gap, lunchGap;
    private int[] counts = new int[7];

    private String auth = "", cookie = "";
    private int finished, total;

    @Override protected void onCreate(Bundle b) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        String iso = getIntent().getStringExtra("monday");
        if (iso != null && !iso.isEmpty()) {
            try { this.monday = LocalDate.parse(iso); } catch (Exception ignored) {}
        }
        this.weekNo = getIntent().getIntExtra("week", WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), monday));
        this.dark = getSharedPreferences(PREFS, 0).getBoolean(KEY_DARK, true);
        applyPalette();
        try {
            AuthManager.Credentials c = AuthManager.load(this);
            if (c != null) { this.auth = c.authorizationJwt; this.cookie = c.sklTokenJwt; }
        } catch (Exception ignored) {}
        build();
        load();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }

    /** 圆形图标按钮：iOS 风格纯图标、无文字。 */
    private FrameLayout roundIconChip(String path, int sizeDp) {
        FrameLayout chip = new FrameLayout(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bgAlt);
        d.setCornerRadius(dp(sizeDp / 2f));
        chip.setBackground(d);
        SvgIcon.IconView icon = SvgIcon.view(this, path, ink, sizeDp - 12);
        chip.addView(icon, new FrameLayout.LayoutParams(dp(sizeDp - 12), dp(sizeDp - 12), Gravity.CENTER));
        chip.setClickable(true);
        UiPolish.pressable(chip);
        return chip;
    }

    // ------------------------------------------------------------ 调色板

    private void applyPalette() {
        if (dark) {
            bg = Color.parseColor("#131312"); bgAlt = Color.parseColor("#1B1B19");
            cardBg = Color.parseColor("#262622"); line = Color.parseColor("#3A3A35");
            ink = Color.parseColor("#EEEAE0"); muted = Color.parseColor("#A5A198");
            faint = Color.parseColor("#706E67"); green = Color.parseColor("#89997A");
            blue = Color.parseColor("#829AAA"); red = Color.parseColor("#B1847B");
            onGreen = Color.parseColor("#161611");
        } else {
            bg = Color.parseColor("#EFEDE6"); bgAlt = Color.parseColor("#E9E6DD");
            cardBg = Color.parseColor("#FBFAF6"); line = Color.parseColor("#DAD6CB");
            ink = Color.parseColor("#191916"); muted = Color.parseColor("#77746B");
            faint = Color.parseColor("#AAA79D"); green = Color.parseColor("#66735D");
            blue = Color.parseColor("#657B88"); red = Color.parseColor("#936F68");
            onGreen = Color.parseColor("#FFFFFF");
        }
    }

    // ------------------------------------------------------------ 布局

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(16);
        bar.setPadding(dp(16), dp(10), dp(16), 0);

        // iOS 式的纯文字返回箭头：只有字形，没有底色和阴影
        TextView back = txt("\u2039", 26, muted, false);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription("返回");
        back.setClickable(true);
        back.setOnClickListener(v -> finish());
        UiPolish.pressable(back);
        bar.addView(back, new LinearLayout.LayoutParams(dp(34f), dp(34f)));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.RIGHT);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView prev = txt("\u2039", 20, faint, false);
        prev.setGravity(Gravity.CENTER);
        prev.setClickable(true);
        prev.setOnClickListener(v -> shiftWeek(-1));
        UiPolish.pressable(prev);
        nav.addView(prev, new LinearLayout.LayoutParams(dp(22f), dp(34f)));
        this.title = txt("第 " + weekNo + " 周", 26, ink, true);
        this.title.setTypeface(Typeface.create("serif", Typeface.BOLD));
        this.title.setGravity(Gravity.CENTER);
        this.title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-2, -2);
        titleLp.leftMargin = dp(6f);
        titleLp.rightMargin = dp(6f);
        nav.addView(this.title, titleLp);
        TextView next = txt("\u203a", 20, faint, false);
        next.setGravity(Gravity.CENTER);
        next.setClickable(true);
        next.setOnClickListener(v -> shiftWeek(1));
        UiPolish.pressable(next);
        nav.addView(next, new LinearLayout.LayoutParams(dp(22f), dp(34f)));
        right.addView(nav, new LinearLayout.LayoutParams(-2, -2));

        this.sub = txt("", 10, muted, false);
        this.sub.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-2, -2);
        subLp.topMargin = dp(1f);
        subLp.rightMargin = dp(26f);
        right.addView(this.sub, subLp);

        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -2, 1f);
        bar.addView(right, rightLp);
        root.addView(bar);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout padding = new LinearLayout(this);
        padding.setOrientation(LinearLayout.VERTICAL);
        padding.setPadding(dp(12), dp(14), dp(12), dp(20));
        scroll.addView(padding, new LinearLayout.LayoutParams(-1, -1));

        this.card = new LinearLayout(this);
        this.card.setOrientation(LinearLayout.VERTICAL);
        UiPolish.glass(this.card, cardBg, line, 22f);
        this.card.setPadding(dp(12), dp(14), dp(12), dp(12));
        this.card.setClipChildren(false);
        this.card.setClipToOutline(false);
        padding.addView(this.card, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout cardHead = new LinearLayout(this);
        cardHead.setGravity(16);
        TextView label = txt("本周课程", 12, ink, true);
        cardHead.addView(label);
        this.status = txt("正在加载…", 10, faint, false);
        this.status.setGravity(Gravity.RIGHT);
        cardHead.addView(this.status, new LinearLayout.LayoutParams(0, -2, 1f));
        this.card.addView(cardHead);

        int screenW = getResources().getDisplayMetrics().widthPixels;
        int cardInner = screenW - dp(24) - dp(24);
        this.gap = dp(2f);
        this.labelW = dp(22f);
        this.colW = Math.max(dp(38f), (cardInner - labelW - gap * 7) / 7);
        this.rowH = dp(46f);
        this.lunchGap = dp(11f);

        LinearLayout headRow = new LinearLayout(this);
        headRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(-2, -2);
        headLp.topMargin = dp(10f);
        this.card.addView(headRow, headLp);
        headRow.addView(new View(this), new LinearLayout.LayoutParams(labelW, 1));
        for (int i = 0; i < 7; i++) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(17);
            this.dayName[i] = txt(WEEK_NAMES[i], 9, muted, false);
            this.dayDate[i] = txt("", 12, ink, true);
            cell.addView(this.dayName[i]);
            cell.addView(this.dayDate[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(colW, -2);
            lp.leftMargin = gap;
            headRow.addView(cell, lp);
        }

        View hair = new View(this);
        hair.setBackgroundColor(line);
        LinearLayout.LayoutParams hairLp = new LinearLayout.LayoutParams(-1, dp(1f));
        hairLp.topMargin = dp(10f);
        hairLp.bottomMargin = dp(8f);
        this.card.addView(hair, hairLp);

        LinearLayout gridRow = new LinearLayout(this);
        gridRow.setOrientation(LinearLayout.HORIZONTAL);
        gridRow.setClipChildren(false);
        this.card.addView(gridRow, new LinearLayout.LayoutParams(-2, -2));

        LinearLayout sectionCol = new LinearLayout(this);
        sectionCol.setOrientation(LinearLayout.VERTICAL);
        gridRow.addView(sectionCol, new LinearLayout.LayoutParams(labelW, -2));
        for (int s = 1; s <= SECTIONS; s++) {
            TextView cell = txt(String.valueOf(s), 9, faint, false);
            cell.setGravity(17);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, rowH);
            lp.bottomMargin = gap + (s == 5 ? lunchGap : 0);
            sectionCol.addView(cell, lp);
        }
        for (int i = 0; i < 7; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setClipChildren(false);
            this.cols[i] = col;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(colW, -2);
            lp.leftMargin = gap;
            gridRow.addView(col, lp);
        }

        TextView hint = txt("点课程查看详情　·　白天课程按节次自动跨行", 10, faint, false);
        hint.setGravity(17);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
        hintLp.topMargin = dp(12f);
        this.card.addView(hint, hintLp);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        PageMotion.installBackdrop(this, bg);
        PageMotion.fromRight(root, 620L);
        UiPolish.staggerGroup(headRow);

        this.card.setAlpha(0f);
        this.card.setTranslationY(dp(14f));
        this.card.animate().alpha(1f).translationY(0f).setDuration(320).start();
        updateHeader();
    }

    private void updateHeader() {
        this.title.setText(weekNo >= 1 ? "第 " + weekNo + " 周" : "假期");
        String range = monday.format(FMT_MD) + " – " + monday.plusDays(6).format(FMT_MD);
        boolean isThisWeek = weekNo == weekOfToday();
        this.sub.setText(isThisWeek ? range + " · 本周" : range);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            this.dayDate[i].setText(String.format(Locale.CHINA, "%02d", d.getDayOfMonth()));
            boolean isToday = d.equals(today);
            this.dayDate[i].setTextColor(isToday ? green : ink);
            this.dayName[i].setTextColor(isToday ? green : muted);
        }
    }

    private int weekOfToday() {
        String saved = getSharedPreferences(PREFS, 0).getString("sem_anchor", "");
        if (!saved.isEmpty()) {
            try { return WeekUtil.weekNumber(LocalDate.parse(saved), LocalDate.now()); } catch (Exception ignored) {}
        }
        Integer real = WeekCalendar.weekFor(this, LocalDate.now());
        if (real != null) {
            return real.intValue();
        }
        return WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), LocalDate.now());
    }

    private void shiftWeek(int delta) {
        this.monday = this.monday.plusWeeks(delta);
        this.weekNo += delta;
        updateHeader();
        load();
    }

    // ------------------------------------------------------------ 数据

    private void load() {
        JSONArray[] seeded = new JSONArray[7];
        boolean any = false;
        for (int i = 0; i < 7; i++) {
            seeded[i] = readDay(monday.plusDays(i));
            if (seeded[i] != null) any = true;
        }
        if (any) {
            for (int i = 0; i < 7; i++) renderDay(i, seeded[i], false);
            flyGrid();
            updateStatus("已显示缓存 · 正在更新…");
        } else {
            for (int i = 0; i < 7; i++) renderDay(i, new JSONArray(), false);
            updateStatus("正在加载…");
        }
        fetch(seeded);
    }

    private void fetch(JSONArray[] seeded) {
        final LocalDate anchor = monday;
        this.finished = 0;
        this.total = 0;
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            LocalDate d = anchor.plusDays(i);
            long ts = getSharedPreferences(PREFS, 0).getLong(DAY_TS + d, 0L);
            boolean fresh = seeded[i] != null && System.currentTimeMillis() - ts < STALE_MS;
            if (fresh) continue;
            this.total++;
            io.execute(() -> {
                JSONArray arr = null;
                String error = null;
                try {
                    SchoolApi api = new SchoolApi(WeekOverviewActivity.this, auth, cookie);
                    arr = toJson(api.getTodayCourses(d));
                } catch (Exception e) {
                    error = e.getMessage();
                }
                final JSONArray result = arr;
                final String err = error;
                main.post(() -> {
                    finished++;
                    if (result != null) {
                        writeDay(d, result);
                        renderDay(idx, result, true);
                    } else if (seeded[idx] == null) {
                        renderDay(idx, new JSONArray(), false);
                    }
                    if (finished >= total) {
                        int n = 0;
                        for (int c : counts) n += c;
                        updateStatus(err == null ? ("共 " + n + " 门安排 · 已更新") : "部分未更新，已保留缓存");
                    } else {
                        updateStatus("更新中 " + finished + "/" + total + "…");
                    }
                });
            });
        }
        if (this.total == 0) {
            int n = 0;
            for (int c : counts) n += c;
            updateStatus("共 " + n + " 门安排 · 已是最新");
        }
    }

    private void updateStatus(String text) {
        if (status != null) status.setText(text);
    }

    /** 全表：按“周一→周日、从上到下”的顺序，让每门课逐个飞入。 */
    private void flyGrid() {
        List<View> cells = new ArrayList<>();
        for (int i = 0; i < 7; i++) collectCells(i, cells);
        playCells(cells);
    }

    /** 单列：只让这一列的课程逐个飞入，返回下一个可用序号。 */
    private int flyColumn(int idx, int order) {
        List<View> cells = new ArrayList<>();
        collectCells(idx, cells);
        playCells(cells);
        return order;
    }

    private void collectCells(int idx, List<View> out) {
        if (cols[idx] == null) return;
        for (int c = 0; c < cols[idx].getChildCount(); c++) {
            View v = cols[idx].getChildAt(c);
            if ("course".equals(v.getTag())) out.add(v);
        }
    }

    /** 逐个飞入：每格从卡片最左侧飞进自己的位置，只带回弹、不做缩放。 */
    private void playCells(final List<View> cells) {
        final int n = cells.size();
        if (n == 0) return;
        for (int i = 0; i < n; i++) {
            cells.get(i).setAlpha(0f);
        }
        final View anchor = this.card != null ? this.card : (View) cols[0];
        anchor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                anchor.getViewTreeObserver().removeOnPreDrawListener(this);
                int[] base = new int[2];
                anchor.getLocationInWindow(base);
                final int[] xs = new int[n];
                final int[] ys = new int[n];
                for (int i = 0; i < n; i++) {
                    int[] pos = new int[2];
                    cells.get(i).getLocationInWindow(pos);
                    xs[i] = pos[0];
                    ys[i] = pos[1];
                }
                // 顺序：先周一自上而下，再周二自上而下……
                List<Integer> order = new ArrayList<>();
                for (int i = 0; i < n; i++) order.add(i);
                Collections.sort(order, new java.util.Comparator<Integer>() {
                    @Override public int compare(Integer a, Integer b) {
                        if (xs[a] != xs[b]) return Integer.compare(xs[a], xs[b]);
                        return Integer.compare(ys[a], ys[b]);
                    }
                });
                for (int k = 0; k < n; k++) {
                    int i = order.get(k);
                    View v = cells.get(i);
                    // 起点的 X：卡片左缘再往左 48dp
                    v.setTranslationX((base[0] - xs[i]) - dp(48f));
                    v.animate().alpha(1f).translationX(0f)
                            .setStartDelay(k * 120L)
                            .setDuration(1000L)
                            .setInterpolator(UiPolish.BOUNCE)
                            .start();
                }
                return true;
            }
        });
    }

    private JSONArray readDay(LocalDate d) {
        String s = getSharedPreferences(PREFS, 0).getString(DAY_CACHE + d, "");
        if (s.isEmpty()) return null;
        try { return new JSONArray(s); } catch (Exception e) { return null; }
    }

    private void writeDay(LocalDate d, JSONArray arr) {
        getSharedPreferences(PREFS, 0).edit()
                .putString(DAY_CACHE + d, arr.toString())
                .putLong(DAY_TS + d, System.currentTimeMillis())
                .apply();
    }

    private JSONArray toJson(List<Course> list) {
        JSONArray arr = new JSONArray();
        for (Course c : list) {
            try {
                JSONObject o = new JSONObject();
                o.put("courseName", c.name);
                o.put("teacherName", c.teacher);
                o.put("classRoom", c.room);
                o.put("date", c.date);
                o.put("startSection", c.startSection);
                o.put("endSection", c.endSection);
                arr.put(o);
            } catch (Exception ignored) {}
        }
        return arr;
    }

    // ------------------------------------------------------------ 单元格

    private void renderDay(int idx, JSONArray list, boolean animate) {
        LinearLayout col = cols[idx];
        col.removeAllViews();
        List<Course> courses = new ArrayList<>();
        if (list != null) {
            for (int j = 0; j < list.length(); j++) {
                JSONObject o = list.optJSONObject(j);
                if (o != null) courses.add(new Course(o));
            }
        }
        Collections.sort(courses, (a, b) -> Integer.compare(a.startSection, b.startSection));
        counts[idx] = courses.size();
        int section = 1;
        while (section <= SECTIONS) {
            Course covering = covering(courses, section);
            if (covering == null) {
                col.addView(emptyCell(), rowLp(section, 1));
                section++;
                continue;
            }
            int start = covering.startSection <= 0 ? 1 : covering.startSection;
            int end = Math.min(SECTIONS, covering.endSection < start ? start : covering.endSection);
            if (start == section) {
                col.addView(courseCell(covering, end - start + 1), rowLp(start, end - start + 1));
                section = end + 1;
            } else {
                section++;
            }
        }
        if (animate) {
            flyColumn(idx, 0);
        }
    }

    /** 跨行高度：跨过第 5/6 节之间的午饭时间要多留一段空隙。 */
    private LinearLayout.LayoutParams rowLp(int start, int span) {
        int last = start + span - 1;
        boolean crossesLunch = start <= 5 && last > 5;
        boolean endsAtLunch = last == 5;
        int h = rowH * span + gap * (span - 1);
        if (span > 1 && crossesLunch) h += lunchGap;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, h);
        lp.bottomMargin = gap + (endsAtLunch ? lunchGap : 0);
        return lp;
    }

    private Course covering(List<Course> list, int section) {
        for (Course c : list) {
            int start = c.startSection <= 0 ? 1 : c.startSection;
            int end = c.endSection < start ? start : c.endSection;
            if (section >= start && section <= end) return c;
        }
        return null;
    }

    private View emptyCell() {
        View v = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(UiPolish.withAlpha(bgAlt, 0x66));
        d.setCornerRadius(dp(10f));
        v.setBackground(d);
        return v;
    }

    private View courseCell(Course c, int span) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(3), dp(3), dp(3), dp(3));
        box.setTag("course");
        GradientDrawable d = new GradientDrawable();
        d.setColor(blend(tintFor(c.name), cardBg, 0.74f));
        d.setCornerRadius(dp(10f));
        box.setBackground(d);

        TextView name = txt(c.name == null || c.name.isEmpty() ? "课程" : c.name, 9, ink, true);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(span >= 3 ? 3 : (span == 2 ? 2 : 1));
        box.addView(name);
        // 地点始终显示（单节也显示），窄格子里允许折成两行，保证不被截断
        String room = c.roomShort();
        if (!room.isEmpty()) {
            TextView r = txt(room, 7, muted, false);
            r.setGravity(Gravity.CENTER);
            r.setMaxLines(span >= 2 ? 2 : 1);
            r.setLineSpacing(0f, 0.92f);
            box.addView(r, top(1));
        }
        box.setOnClickListener(v -> showCourse(c));
        return box;
    }

    private void showCourse(Course c) {
        StringBuilder sb = new StringBuilder();
        sb.append("节次　").append(c.sections()).append('\n');
        sb.append("时间　").append(c.timeRange()).append('\n');
        if (!c.roomShort().isEmpty()) sb.append("地点　").append(c.roomShort()).append('\n');
        if (c.teacher != null && !c.teacher.trim().isEmpty()) sb.append("教师　").append(c.teacher.trim());
        new AlertDialog.Builder(this)
                .setTitle(c.name == null ? "课程" : c.name)
                .setMessage(sb.toString().trim())
                .setPositiveButton("知道了", null)
                .show();
    }

    // ------------------------------------------------------------ 小工具

    private int tintFor(String name) {
        int hash = name == null ? 0 : Math.abs(name.hashCode());
        switch (hash % 4) {
            case 0: return green;
            case 1: return blue;
            case 2: return red;
            default: return blend(green, blue, 0.5f);
        }
    }

    private int blend(int a, int b, float f) {
        float inv = 1f - f;
        return Color.rgb(
                (int) (Color.red(a) * inv + Color.red(b) * f),
                (int) (Color.green(a) * inv + Color.green(b) * f),
                (int) (Color.blue(a) * inv + Color.blue(b) * f));
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams top(float v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.topMargin = dp(v);
        return lp;
    }

    private TextView txt(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button ghostBtn(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(11f);
        b.setTextColor(muted);
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(bgAlt);
        bgDrawable.setCornerRadius(dp(17f));
        bgDrawable.setStroke(dp(1f), line);
        b.setBackground(bgDrawable);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setPadding(dp(12), 0, dp(12), 0);
        return b;
    }

    private Button arrowBtn(String label) { return null; }
    // arrowBtn 已废弃（iOS 顶栏改用纯文字箭头）
}
