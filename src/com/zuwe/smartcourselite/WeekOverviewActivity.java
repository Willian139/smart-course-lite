package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.zuwe.smartcourselite.AuthManager;
import com.zuwe.smartcourselite.SvgIcon;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class WeekOverviewActivity extends Activity {
    private static final String DAY_CACHE = "cache_";
    private static final String DAY_TS = "cache_ts_";
    private static final String KEY_DARK = "theme_dark";
    private static final String PREFS = "smartcourse_prefs";
    private static final int SECTIONS = 12;
    private static final long STALE_MS = 7200000;
    private int bg;
    private int bgAlt;
    private int blue;
    private LinearLayout card;
    private int cardBg;
    private int colW;
    private int faint;
    private int finished;
    private int gap;
    private int green;
    private int ink;
    private int labelW;
    private int line;
    private int lunchGap;
    private int muted;
    private int onGreen;
    private int red;
    private int rowH;
    private TextView status;
    private TextView sub;
    private long swipeDownAt;
    private float swipeDownX;
    private float swipeDownY;
    private TextView title;
    private int total;
    private static final DateTimeFormatter FMT_MD = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA);
    private static final String[] WEEK_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newFixedThreadPool(7);
    private boolean dark = true;
    private LocalDate monday = WeekUtil.monday(LocalDate.now());
    private int weekNo = 1;
    private TextView[] dayName = new TextView[7];
    private TextView[] dayDate = new TextView[7];
    private LinearLayout[] cols = new LinearLayout[7];
    private int[] counts = new int[7];
    private String auth = "";
    private String cookie = "";

    private Button arrowBtn(String str) {
        return null;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        requestWindowFeature(1);
        String stringExtra = getIntent().getStringExtra("monday");
        if (stringExtra != null && !stringExtra.isEmpty()) {
            try {
                this.monday = LocalDate.parse(stringExtra);
            } catch (Exception unused) {
            }
        }
        this.weekNo = getIntent().getIntExtra("week", WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), this.monday));
        this.dark = getSharedPreferences(PREFS, 0).getBoolean(KEY_DARK, true);
        applyPalette();
        try {
            AuthManager.Credentials load = AuthManager.load(this);
            if (load != null) {
                this.auth = load.authorizationJwt;
                this.cookie = load.sklTokenJwt;
            }
        } catch (Exception unused2) {
        }
        build();
        load();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.io.shutdownNow();
    }

    private FrameLayout roundIconChip(String str, int i) {
        FrameLayout frameLayout = new FrameLayout(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(i / 2.0f));
        frameLayout.setBackground(gradientDrawable);
        int i2 = i - 12;
        SvgIcon.IconView view = SvgIcon.view(this, str, this.ink, i2);
        float f = i2;
        frameLayout.addView(view, new FrameLayout.LayoutParams(dp(f), dp(f), 17));
        frameLayout.setClickable(true);
        UiPolish.pressable(frameLayout);
        return frameLayout;
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
            this.blue = Color.parseColor("#9DB8CC");
            this.red = Color.parseColor("#CD8B84");
            this.onGreen = Color.parseColor("#1A1C17");
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
        this.blue = Color.parseColor("#91AEC7");
        this.red = Color.parseColor("#BA716B");
        this.onGreen = Color.parseColor("#FFFFFF");
    }

    private void build() {
        float f;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(this.bg);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(dp(16.0f), dp(10.0f), dp(16.0f), 0);
        TextView txt = txt("‹", 26.0f, this.muted, false);
        txt.setGravity(17);
        txt.setContentDescription("返回");
        txt.setClickable(true);
        txt.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WeekOverviewActivity.this.m25lambda$build$0$comzuwesmartcourseliteWeekOverviewActivity(view);
            }
        });
        UiPolish.pressable(txt);
        linearLayout2.addView(txt, new LinearLayout.LayoutParams(dp(34.0f), dp(34.0f)));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.setGravity(5);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(0);
        linearLayout4.setGravity(16);
        TextView txt2 = txt("‹", 20.0f, this.faint, false);
        txt2.setGravity(17);
        txt2.setClickable(true);
        txt2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WeekOverviewActivity.this.m26lambda$build$1$comzuwesmartcourseliteWeekOverviewActivity(view);
            }
        });
        UiPolish.pressable(txt2);
        linearLayout4.addView(txt2, new LinearLayout.LayoutParams(dp(22.0f), dp(34.0f)));
        TextView txt3 = txt("第 " + this.weekNo + " 周", 26.0f, this.ink, true);
        this.title = txt3;
        txt3.setTypeface(Typeface.create("serif", 1));
        this.title.setGravity(17);
        this.title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = dp(6.0f);
        layoutParams.rightMargin = dp(6.0f);
        linearLayout4.addView(this.title, layoutParams);
        TextView txt4 = txt("›", 20.0f, this.faint, false);
        txt4.setGravity(17);
        txt4.setClickable(true);
        txt4.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WeekOverviewActivity.this.m27lambda$build$2$comzuwesmartcourseliteWeekOverviewActivity(view);
            }
        });
        UiPolish.pressable(txt4);
        linearLayout4.addView(txt4, new LinearLayout.LayoutParams(dp(22.0f), dp(34.0f)));
        linearLayout3.addView(linearLayout4, new LinearLayout.LayoutParams(-2, -2));
        TextView txt5 = txt("", 10.0f, this.muted, false);
        this.sub = txt5;
        txt5.setGravity(5);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        float f2 = 1.0f;
        layoutParams2.topMargin = dp(1.0f);
        layoutParams2.rightMargin = dp(26.0f);
        linearLayout3.addView(this.sub, layoutParams2);
        linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(linearLayout2);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(1);
        linearLayout5.setPadding(dp(12.0f), dp(14.0f), dp(12.0f), dp(20.0f));
        int i = -1;
        scrollView.addView(linearLayout5, new LinearLayout.LayoutParams(-1, -1));
        LinearLayout linearLayout6 = new LinearLayout(this);
        this.card = linearLayout6;
        linearLayout6.setOrientation(1);
        UiPolish.glass(this.card, this.cardBg, this.line, 22.0f);
        this.card.setPadding(dp(12.0f), dp(14.0f), dp(12.0f), dp(12.0f));
        this.card.setClipChildren(false);
        this.card.setClipToOutline(false);
        linearLayout5.addView(this.card, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout linearLayout7 = new LinearLayout(this);
        linearLayout7.setGravity(16);
        linearLayout7.addView(txt("本周课程", 12.0f, this.ink, true));
        TextView txt6 = txt("正在加载…", 10.0f, this.faint, false);
        this.status = txt6;
        txt6.setGravity(5);
        linearLayout7.addView(this.status, new LinearLayout.LayoutParams(0, -2, 1.0f));
        this.card.addView(linearLayout7);
        int dp = (getResources().getDisplayMetrics().widthPixels - dp(24.0f)) - dp(24.0f);
        this.gap = dp(2.0f);
        this.labelW = dp(22.0f);
        this.colW = Math.max(dp(38.0f), ((dp - this.labelW) - (this.gap * 7)) / 7);
        this.rowH = dp(46.0f);
        this.lunchGap = dp(11.0f);
        LinearLayout linearLayout8 = new LinearLayout(this);
        linearLayout8.setOrientation(0);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.topMargin = dp(10.0f);
        this.card.addView(linearLayout8, layoutParams3);
        linearLayout8.addView(new View(this), new LinearLayout.LayoutParams(this.labelW, 1));
        int i2 = 0;
        while (true) {
            f = 9.0f;
            if (i2 >= 7) {
                break;
            }
            LinearLayout linearLayout9 = new LinearLayout(this);
            linearLayout9.setOrientation(1);
            linearLayout9.setGravity(17);
            this.dayName[i2] = txt(WEEK_NAMES[i2], 9.0f, this.muted, false);
            this.dayDate[i2] = txt("", 12.0f, this.ink, true);
            linearLayout9.addView(this.dayName[i2]);
            linearLayout9.addView(this.dayDate[i2]);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(this.colW, -2);
            layoutParams4.leftMargin = this.gap;
            linearLayout8.addView(linearLayout9, layoutParams4);
            i2++;
            i = -1;
            f2 = 1.0f;
        }
        View view = new View(this);
        view.setBackgroundColor(this.line);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(i, dp(f2));
        layoutParams5.topMargin = dp(10.0f);
        layoutParams5.bottomMargin = dp(8.0f);
        this.card.addView(view, layoutParams5);
        LinearLayout linearLayout10 = new LinearLayout(this);
        linearLayout10.setOrientation(0);
        linearLayout10.setClipChildren(false);
        this.card.addView(linearLayout10, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout linearLayout11 = new LinearLayout(this);
        linearLayout11.setOrientation(1);
        linearLayout10.addView(linearLayout11, new LinearLayout.LayoutParams(this.labelW, -2));
        int i3 = 1;
        while (i3 <= SECTIONS) {
            TextView txt7 = txt(String.valueOf(i3), f, this.faint, false);
            txt7.setGravity(17);
            LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(i, this.rowH);
            layoutParams6.bottomMargin = this.gap + (i3 == 5 ? this.lunchGap : 0);
            linearLayout11.addView(txt7, layoutParams6);
            i3++;
            f = 9.0f;
        }
        for (int i4 = 0; i4 < 7; i4++) {
            LinearLayout linearLayout12 = new LinearLayout(this);
            linearLayout12.setOrientation(1);
            linearLayout12.setClipChildren(false);
            this.cols[i4] = linearLayout12;
            LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(this.colW, -2);
            layoutParams7.leftMargin = this.gap;
            linearLayout10.addView(linearLayout12, layoutParams7);
        }
        TextView txt8 = txt("点课程查看详情\u3000·\u3000白天课程按节次自动跨行", 10.0f, this.faint, false);
        txt8.setGravity(17);
        LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(i, -2);
        layoutParams8.topMargin = dp(12.0f);
        this.card.addView(txt8, layoutParams8);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(i, 0, 1.0f));
        setContentView(linearLayout);
        PageMotion.installBackdrop(this, this.bg);
        PageMotion.fromRight(linearLayout, 620L);
        UiPolish.staggerGroup(linearLayout8);
        this.card.setAlpha(0.0f);
        this.card.setTranslationY(dp(14.0f));
        this.card.animate().alpha(1.0f).translationY(0.0f).setDuration(280L).start();
        updateHeader();
    }

    void m25lambda$build$0$comzuwesmartcourseliteWeekOverviewActivity(View view) {
        finish();
    }

    void m26lambda$build$1$comzuwesmartcourseliteWeekOverviewActivity(View view) {
        shiftWeek(-1);
    }

    void m27lambda$build$2$comzuwesmartcourseliteWeekOverviewActivity(View view) {
        shiftWeek(1);
    }

    private void updateHeader() {
        String str = this.weekNo >= 1 ? "第 " + this.weekNo + " 周" : "假期";
        CharSequence text = this.title.getText();
        if (text == null || !text.toString().contentEquals(str)) {
            TextFx.flip(this.title, str);
        }
        StringBuilder sb = new StringBuilder();
        LocalDate localDate = this.monday;
        DateTimeFormatter dateTimeFormatter = FMT_MD;
        sb.append(localDate.format(dateTimeFormatter));
        sb.append(" – ");
        sb.append(this.monday.plusDays(6L).format(dateTimeFormatter));
        String sb2 = sb.toString();
        boolean z = this.weekNo == weekOfToday();
        TextView textView = this.sub;
        if (z) {
            sb2 = sb2 + " · 本周";
        }
        textView.setText(sb2);
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate plusDays = this.monday.plusDays(i);
            this.dayDate[i].setText(String.format(Locale.CHINA, "%02d", Integer.valueOf(plusDays.getDayOfMonth())));
            boolean equals = plusDays.equals(now);
            this.dayDate[i].setTextColor(equals ? this.green : this.ink);
            this.dayName[i].setTextColor(equals ? this.green : this.muted);
        }
    }

    private int weekOfToday() {
        String string = getSharedPreferences(PREFS, 0).getString("sem_anchor", "");
        if (!string.isEmpty()) {
            try {
                return WeekUtil.weekNumber(LocalDate.parse(string), LocalDate.now());
            } catch (Exception unused) {
            }
        }
        Integer weekFor = WeekCalendar.weekFor(this, LocalDate.now());
        if (weekFor != null) {
            return weekFor.intValue();
        }
        return WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), LocalDate.now());
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.swipeDownX = motionEvent.getX();
            this.swipeDownY = motionEvent.getY();
            this.swipeDownAt = System.currentTimeMillis();
            return super.dispatchTouchEvent(motionEvent);
        }
        if (actionMasked == 1) {
            float x = motionEvent.getX() - this.swipeDownX;
            float max = x / Math.max(1L, System.currentTimeMillis() - this.swipeDownAt);
            boolean z = Math.abs(x) > Math.abs(motionEvent.getY() - this.swipeDownY) * 1.6f;
            boolean z2 = Math.abs(max) > 0.45f;
            boolean z3 = Math.abs(x) > ((float) dp(56.0f));
            if (z && (z2 || z3)) {
                shiftWeek(x < 0.0f ? 1 : -1);
                Haptics.confirm(this.card);
                return true;
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void shiftWeek(int i) {
        this.monday = this.monday.plusWeeks(i);
        this.weekNo += i;
        updateHeader();
        load();
        LinearLayout linearLayout = this.card;
        if (linearLayout != null) {
            linearLayout.setRotationY(0.0f);
            linearLayout.setTranslationX(i > 0 ? dp(36.0f) : -dp(36.0f));
            linearLayout.setAlpha(0.0f);
            linearLayout.animate().translationX(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
        }
    }

    private void load() {
        JSONArray[] jSONArrayArr = new JSONArray[7];
        boolean z = false;
        for (int i = 0; i < 7; i++) {
            JSONArray readDay = readDay(this.monday.plusDays(i));
            jSONArrayArr[i] = readDay;
            if (readDay != null) {
                z = true;
            }
        }
        if (z) {
            for (int i2 = 0; i2 < 7; i2++) {
                renderDay(i2, jSONArrayArr[i2], false);
            }
            flyGrid();
            updateStatus("已显示缓存 · 正在更新…");
        } else {
            for (int i3 = 0; i3 < 7; i3++) {
                renderDay(i3, new JSONArray(), false);
            }
            updateStatus("正在加载…");
        }
        fetch(jSONArrayArr);
    }

    private void fetch(final JSONArray[] jSONArrayArr) {
        LocalDate localDate = this.monday;
        this.finished = 0;
        this.total = 0;
        for (int i = 0; i < 7; i++) {
            final LocalDate plusDays = localDate.plusDays(i);
            long j = getSharedPreferences(PREFS, 0).getLong(DAY_TS + plusDays, 0L);
            if (jSONArrayArr[i] == null || System.currentTimeMillis() - j >= STALE_MS) {
                this.total++;
                final int dayIdx = i;
                this.io.execute(new Runnable() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.4
                    @Override // java.lang.Runnable
                    public final void run() {
                        WeekOverviewActivity.this.m29lambda$fetch$0$comzuwesmartcourseliteWeekOverviewActivity(plusDays, dayIdx, jSONArrayArr);
                    }
                });
            }
        }
        if (this.total == 0) {
            int i2 = 0;
            for (int i3 : this.counts) {
                i2 += i3;
            }
            updateStatus("共 " + i2 + " 门安排 · 已是最新");
        }
    }

    void m29lambda$fetch$0$comzuwesmartcourseliteWeekOverviewActivity(final LocalDate localDate, final int i, final JSONArray[] jSONArrayArr) {
        JSONArray jSONArray = null;
        String message = null;
        try {
            jSONArray = toJson(new SchoolApi(this, this.auth, this.cookie).getTodayCourses(localDate));
        } catch (Exception e) {
            message = e.getMessage();
        }
        final JSONArray fj = jSONArray;
        final String fm = message;
        this.main.post(new Runnable() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.5
            @Override // java.lang.Runnable
            public void run() {
                WeekOverviewActivity.this.m30lambda$fetch$1$comzuwesmartcourseliteWeekOverviewActivity(fj, localDate, i, jSONArrayArr, fm);
            }
        });
    }

    void m30lambda$fetch$1$comzuwesmartcourseliteWeekOverviewActivity(JSONArray jSONArray, LocalDate localDate, int i, JSONArray[] jSONArrayArr, String str) {
        this.finished++;
        if (jSONArray != null) {
            writeDay(localDate, jSONArray);
            renderDay(i, jSONArray, true);
        } else if (jSONArrayArr[i] == null) {
            renderDay(i, new JSONArray(), false);
        }
        if (this.finished >= this.total) {
            int i2 = 0;
            for (int i3 : this.counts) {
                i2 += i3;
            }
            updateStatus(str == null ? "共 " + i2 + " 门安排 · 已更新" : "部分未更新，已保留缓存");
            return;
        }
        updateStatus("更新中 " + this.finished + "/" + this.total + "…");
    }

    private void updateStatus(String str) {
        TextView textView = this.status;
        if (textView != null) {
            textView.setText(str);
        }
    }

    private void flyGrid() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 7; i++) {
            collectCells(i, arrayList);
        }
        playCells(arrayList);
    }

    private int flyColumn(int i, int i2) {
        ArrayList arrayList = new ArrayList();
        collectCells(i, arrayList);
        playCells(arrayList);
        return i2;
    }

    private void collectCells(int i, List<View> list) {
        if (this.cols[i] == null) {
            return;
        }
        for (int i2 = 0; i2 < this.cols[i].getChildCount(); i2++) {
            View childAt = this.cols[i].getChildAt(i2);
            if ("course".equals(childAt.getTag())) {
                list.add(childAt);
            }
        }
    }

    private void playCells(final List<View> list) {
        int size = list.size();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            list.get(i).setAlpha(0.0f);
        }
        LinearLayout linearLayout = this.card;
        if (linearLayout == null) {
            linearLayout = this.cols[0];
        }
        final LinearLayout card = linearLayout;
        card.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.6
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                card.getViewTreeObserver().removeOnPreDrawListener(this);
                int[] base = new int[2];
                card.getLocationInWindow(base);
                int size2 = list.size();
                final int[] iArr = new int[size2];
                final int[] iArr2 = new int[size2];
                for (int i2 = 0; i2 < size2; i2++) {
                    int[] iArr3 = new int[2];
                    ((View) list.get(i2)).getLocationInWindow(iArr3);
                    iArr[i2] = iArr3[0];
                    iArr2[i2] = iArr3[1];
                }
                ArrayList arrayList = new ArrayList();
                for (int i3 = 0; i3 < size2; i3++) {
                    arrayList.add(Integer.valueOf(i3));
                }
                Collections.sort(arrayList, new Comparator<Integer>() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.6.1
                    @Override // java.util.Comparator
                    public int compare(Integer num, Integer num2) {
                        int intValue = num.intValue();
                        int intValue2 = num2.intValue();
                        int[] iArr4 = iArr;
                        int i4 = iArr4[intValue];
                        int i5 = iArr4[intValue2];
                        if (i4 != i5) {
                            return Integer.compare(i4, i5);
                        }
                        int[] iArr5 = iArr2;
                        return Integer.compare(iArr5[intValue], iArr5[intValue2]);
                    }
                });
                for (int i4 = 0; i4 < size2; i4++) {
                    View view = (View) list.get(((Integer) arrayList.get(i4)).intValue());
                    view.setTranslationX((iArr[i4] - base[0]) - WeekOverviewActivity.this.dp(48.0f));
                    view.setRotationY(-24.0f);
                    view.setRotationX(-7.0f);
                    view.setScaleX(0.88f);
                    view.setScaleY(0.88f);
                    view.setCameraDistance(WeekOverviewActivity.this.getResources().getDisplayMetrics().density * 14000.0f);
                    view.animate().alpha(1.0f).translationX(0.0f).rotationY(0.0f).rotationX(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(i4 * 120).setDuration(420L).setInterpolator(Motion.SPRING).start();
                }
                return true;
            }
        });
    }

    private JSONArray readDay(LocalDate localDate) {
        String string = getSharedPreferences(PREFS, 0).getString(DAY_CACHE + localDate, "");
        if (string.isEmpty()) {
            return null;
        }
        try {
            return new JSONArray(string);
        } catch (Exception unused) {
            return null;
        }
    }

    private void writeDay(LocalDate localDate, JSONArray jSONArray) {
        getSharedPreferences(PREFS, 0).edit().putString(DAY_CACHE + localDate, jSONArray.toString()).putLong(DAY_TS + localDate, System.currentTimeMillis()).apply();
    }

    private JSONArray toJson(List<Course> list) {
        JSONArray jSONArray = new JSONArray();
        for (Course course : list) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("courseName", course.name);
                jSONObject.put("teacherName", course.teacher);
                jSONObject.put("classRoom", course.room);
                jSONObject.put("date", course.date);
                jSONObject.put("startSection", course.startSection);
                jSONObject.put("endSection", course.endSection);
                jSONArray.put(jSONObject);
            } catch (Exception unused) {
            }
        }
        return jSONArray;
    }

    private void renderDay(int i, JSONArray jSONArray, boolean z) {
        LinearLayout linearLayout = this.cols[i];
        linearLayout.removeAllViews();
        ArrayList arrayList = new ArrayList();
        if (jSONArray != null) {
            for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i2);
                if (optJSONObject != null) {
                    arrayList.add(new Course(optJSONObject));
                }
            }
        }
        Collections.sort(arrayList, new Comparator() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.7
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return Integer.compare(((Course) obj).startSection, ((Course) obj2).startSection);
            }
        });
        this.counts[i] = arrayList.size();
        boolean[] zArr = new boolean[14];
        int i3 = 1;
        while (i3 <= SECTIONS) {
            if (!zArr[i3]) {
                Course course = null;
                int i4 = 0;
                while (true) {
                    if (i4 >= arrayList.size()) {
                        break;
                    }
                    Course course2 = (Course) arrayList.get(i4);
                    int i5 = course2.startSection <= 0 ? 1 : course2.startSection;
                    int min = Math.min(SECTIONS, course2.endSection < i5 ? i5 : course2.endSection);
                    if (i5 == i3) {
                        course = course2;
                        break;
                    }
                    if (course == null && i5 <= i3 && min >= i3) {
                        course = course2;
                    }
                    i4++;
                }
                if (course == null) {
                    linearLayout.addView(emptyCell(), rowLp(i3, 1));
                } else {
                    int i6 = course.startSection <= 0 ? 1 : course.startSection;
                    int min2 = Math.min(SECTIONS, course.endSection < i6 ? i6 : course.endSection);
                    int max = Math.max(i3, i6);
                    for (int i7 = max; i7 <= min2; i7++) {
                        zArr[i7] = true;
                    }
                    int i8 = (min2 - max) + 1;
                    linearLayout.addView(courseCell(course, i8), rowLp(max, i8));
                    i3 = min2 + 1;
                }
            }
            i3++;
        }
        if (z) {
            flyColumn(i, 0);
        }
    }

    private LinearLayout.LayoutParams rowLp(int i, int i2) {
        int i3 = (i + i2) - 1;
        boolean z = i <= 5 && i3 > 5;
        boolean z2 = i3 == 5;
        int i4 = (this.rowH * i2) + (this.gap * (i2 - 1));
        if (i2 > 1 && z) {
            i4 += this.lunchGap;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, i4);
        layoutParams.bottomMargin = this.gap + (z2 ? this.lunchGap : 0);
        return layoutParams;
    }

    private Course covering(List<Course> list, int i) {
        for (Course course : list) {
            int i2 = course.startSection <= 0 ? 1 : course.startSection;
            int i3 = course.endSection < i2 ? i2 : course.endSection;
            if (i >= i2 && i <= i3) {
                return course;
            }
        }
        return null;
    }

    private View emptyCell() {
        View view = new View(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(UiPolish.withAlpha(this.bgAlt, 102));
        gradientDrawable.setCornerRadius(dp(10.0f));
        view.setBackground(gradientDrawable);
        return view;
    }

    private View courseCell(final Course course, int i) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(17);
        linearLayout.setPadding(dp(3.0f), dp(3.0f), dp(3.0f), dp(3.0f));
        linearLayout.setTag("course");
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(blend(tintFor(course.name), this.cardBg, 0.74f));
        gradientDrawable.setCornerRadius(dp(10.0f));
        linearLayout.setBackground(gradientDrawable);
        TextView txt = txt((course.name == null || course.name.isEmpty()) ? "课程" : course.name, 9.0f, this.ink, true);
        txt.setGravity(17);
        txt.setMaxLines(i < 3 ? i == 2 ? 2 : 1 : 3);
        linearLayout.addView(txt);
        String roomShort = course.roomShort();
        if (!roomShort.isEmpty()) {
            TextView txt2 = txt(roomShort, 7.0f, this.muted, false);
            txt2.setGravity(17);
            txt2.setMaxLines(i >= 2 ? 2 : 1);
            txt2.setLineSpacing(0.0f, 0.92f);
            linearLayout.addView(txt2, top(1.0f));
        }
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.WeekOverviewActivity.8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WeekOverviewActivity.this.m28xbe61c6b1(course, view);
            }
        });
        return linearLayout;
    }

    void m28xbe61c6b1(Course course, View view) {
        showCourse(course);
    }

    private void showCourse(Course course) {
        StringBuilder sb = new StringBuilder("节次\u3000");
        sb.append(course.sections());
        sb.append("\n时间\u3000");
        sb.append(course.timeRange());
        sb.append('\n');
        if (!course.roomShort().isEmpty()) {
            sb.append("地点\u3000");
            sb.append(course.roomShort());
            sb.append('\n');
        }
        if (course.teacher != null && !course.teacher.trim().isEmpty()) {
            sb.append("教师\u3000");
            sb.append(course.teacher.trim());
        }
        new AlertDialog.Builder(this).setTitle(course.name == null ? "课程" : course.name).setMessage(sb.toString().trim()).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
    }

    private int tintFor(String str) {
        int abs = (str == null ? 0 : Math.abs(str.hashCode())) % 4;
        if (abs == 0) {
            return this.green;
        }
        if (abs == 1) {
            return this.blue;
        }
        if (abs == 2) {
            return this.red;
        }
        return blend(this.green, this.blue, 0.5f);
    }

    private int blend(int i, int i2, float f) {
        float f2 = 1.0f - f;
        return Color.rgb((int) ((Color.red(i) * f2) + (Color.red(i2) * f)), (int) ((Color.green(i) * f2) + (Color.green(i2) * f)), (int) ((Color.blue(i) * f2) + (Color.blue(i2) * f)));
    }

    public int dp(float f) {
        return Math.round(f * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams top(float f) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = dp(f);
        return layoutParams;
    }

    private TextView txt(String str, float f, int i, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        if (z) {
            textView.setTypeface(Typeface.DEFAULT, 1);
        }
        return textView;
    }

    private Button ghostBtn(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(11.0f);
        button.setTextColor(this.muted);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(17.0f));
        gradientDrawable.setStroke(dp(1.0f), this.line);
        button.setBackground(gradientDrawable);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        return button;
    }
}
