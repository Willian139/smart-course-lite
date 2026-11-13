package com.zuwe.smartcourselite;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

/** 智慧思政课表 —— 紧凑顶栏 + 缓存优先 + 抗超时刷新。 */
public final class MainActivity extends Activity {
    private static final String PREFS = "smartcourse_prefs";
    private static final String CACHE_PREFIX = "cache_";
    private static final String CACHE_TIME_PREFIX = "cache_ts_";
    private static final String KEY_AUTO_DAY = "auto_refresh_day";
    private static final String KEY_DARK = "theme_dark";
    private static final String KEY_SEM_ANCHOR = "sem_anchor";
    private static final int PICK_CAPTURE_ZIP = 2081;
    private static final int REQUEST_OAUTH = 2082;
    private static final DateTimeFormatter FMT_TITLE =
            DateTimeFormatter.ofPattern("M月d日", Locale.CHINA);

    private int bg, bgAlt, cardBg, line, ink, muted, faint, green, blue, red, onGreen, accentA, accentB, liveBlue;
    private LinearLayout courseList;
    private LinearLayout dayStrip;
    private boolean quietRender = false;
    private Runnable stateTick;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private float touchDownX = 0f;
    private float touchDownY = 0f;
    private TextView credentialState;
    private TextView dateSub;
    private TextView dateTitle;
    private TextView sectionNote;
    private TextView status;
    private Button refreshBtn;
    private TextView weekLabel;
    private View weekOverviewBtn;
    private View themeBtn;
    private ScrollView courseScroll;
    private ScrollView signInScroll;
    private LinearLayout navCourse;
    private LinearLayout navSignIn;
    private TextView navCourseLabel;
    private TextView navSignInLabel;
    private SvgIcon.IconView navCourseIcon;
    private SvgIcon.IconView navSignInIcon;
    private View navPill;
    private EditText signInCodeInput;
    private EditText signInLatitudeInput;
    private EditText signInLongitudeInput;
    private Button signInSubmitButton;
    private TextView signInStatus;
    private LinearLayout advancedBody;
    private TextView advancedToggle;
    private boolean advancedExpanded;
    private Runnable skeletonStop;
    private LinearLayout screenRoot;
    private FrameLayout bottomNav;
    private FrameLayout navCapsule;
    private LinearLayout headerBlock;
    private int navPillX = -1;
    private boolean navPillPending;
    private LinearLayout imeSubmitBar;
    private Button imeSubmitButton;
    private boolean imeOpen;
    private TextView statRight;
    private TextView statAbsent;
    private TextView statLate;
    private TextView statTotal;
    private LinearLayout signInHistoryList;
    private LinearLayout signInHistoryPanel;
    private List<View> signInHistoryRows;
    private View signInHistoryToggle;
    private SvgIcon.IconView signInHistoryChevron;
    private TextView signInHistoryToggleLabel;
    private boolean signInHistoryExpanded;
    private int signInHistoryTotal;
    private JSONObject signInStats;
    private JSONArray signInHistory;
    private boolean signInLoading;
    private boolean signInSubmitting;
    private int activePage = 0;
    private static final int REQUEST_LOCATION = 2083;
    private final Handler locationHandler = new Handler(Looper.getMainLooper());
    private LocationManager locationManager;
    private LocationListener pendingLocationListener;
    private Runnable locationTimeout;
    private String pendingSignInCode = "";

    private final ExecutorService io = Executors.newFixedThreadPool(2);
    private boolean dark = true;
    private String auth = "";
    private String cookie = "";
    private boolean weekFetching;
    private FrameLayout silentHost;
    private boolean silentAuthRunning;
    private long lastSilentAuthAt;
    private LocalDate viewDate = LocalDate.now();
    private LocalDate openedDate = LocalDate.now();
    private List<Course> lastData;
    private volatile boolean loading;
    private boolean updatePrompted;

    @Override
    protected void onCreate(Bundle bundle) {
        // 覆盖资源表里失效的 AppTheme：无 ActionBar / 标题栏，避免与页面内标题重复
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.openedDate = LocalDate.now();
        this.dark = prefs().getBoolean(KEY_DARK, true);
        applyPalette();
        build();
        loadSaved();
        refreshWeekCalendar(false);
        UpdateManager.cleanupAfterUpdate(this);
        UpdateManager.checkOnLaunch(this);
        requestNotificationPermission();
    }

    /** Android 13+ 需要运行时授予通知权限，否则更新下载的进度/完成通知会被静默丢弃。 */
    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < 33) return;
        try {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override public void run() {
                    try {
                        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 9001);
                    } catch (Exception ignored) {
                    }
                }
            }, 1200L);
        } catch (Exception ignored) {
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, 0);
    }

    private void applyPalette() {
        if (this.dark) {
            this.bg = Color.parseColor("#121311");
            this.bgAlt = Color.parseColor("#232520");
            this.cardBg = Color.parseColor("#1B1D18");
            this.line = Color.parseColor("#2E312A");
            this.ink = Color.parseColor("#ECEDE6");
            this.muted = Color.parseColor("#8E9088");
            this.faint = Color.parseColor("#6E7168");
            this.liveBlue = Color.parseColor("#8FA3B0");
            this.green = Color.parseColor("#8A9A80");
            this.blue = Color.parseColor("#7E939B");
            this.red = Color.parseColor("#B1847B");
            this.accentA = Color.parseColor("#8A9A80");
            this.accentB = Color.parseColor("#8A9A80");
            this.onGreen = Color.parseColor("#121311");
            return;
        }
        this.bg = Color.parseColor("#F5F4EF");
        this.bgAlt = Color.parseColor("#E9E8E1");
        this.cardBg = Color.parseColor("#FFFFFF");
        this.line = Color.parseColor("#E3E2D9");
        this.ink = Color.parseColor("#252722");
        this.muted = Color.parseColor("#92948C");
        this.faint = Color.parseColor("#B3B5AC");
        this.liveBlue = Color.parseColor("#6E8492");
        this.green = Color.parseColor("#687661");
        this.blue = Color.parseColor("#5E737B");
        this.red = Color.parseColor("#936F68");
        this.accentA = Color.parseColor("#687661");
        this.accentB = Color.parseColor("#687661");
        this.onGreen = Color.parseColor("#FFFFFF");
    }

    private void toggleTheme() {
        this.dark = !this.dark;
        prefs().edit().putBoolean(KEY_DARK, this.dark).apply();
        applyPalette();
        build();
        if (this.lastData != null) {
            render(this.lastData);
        } else {
            List<Course> cached = loadCache(this.viewDate);
            if (cached != null) {
                render(cached);
            }
        }
        refreshCredentialLine();
        updateDateHeader();
        setStatus(statusText());
    }

    // ---------------------------------------------------------------- 缓存

    private String cacheKey(LocalDate date) {
        return CACHE_PREFIX + date.toString();
    }

    private void saveCache(LocalDate date, List<Course> list) {
        JSONArray array = new JSONArray();
        if (list != null) {
            for (Course course : list) {
                array.put(courseToJson(course));
            }
        }
        prefs().edit()
                .putString(cacheKey(date), array.toString())
                .putLong(CACHE_TIME_PREFIX + date.toString(), System.currentTimeMillis())
                .apply();
    }

    private JSONObject courseToJson(Course course) {
        JSONObject object = new JSONObject();
        try {
            object.put("courseName", course.name == null ? "" : course.name);
            object.put("teacherName", course.teacher == null ? "" : course.teacher);
            object.put("classRoom", course.room == null ? "" : course.room);
            object.put("date", course.date == null ? "" : course.date);
            object.put("startSection", course.startSection);
            object.put("endSection", course.endSection);
            object.put("week", course.week);
            object.put("weekDay", course.weekDay);
        } catch (Exception ignored) {
        }
        return object;
    }

    private List<Course> loadCache(LocalDate date) {
        String raw = prefs().getString(cacheKey(date), null);
        if (raw == null) {
            return null;
        }
        try {
            JSONArray array = new JSONArray(raw);
            ArrayList<Course> list = new ArrayList<Course>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object != null) {
                    list.add(new Course(object));
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private String cacheAge(LocalDate date) {
        long ts = prefs().getLong(CACHE_TIME_PREFIX + date.toString(), 0L);
        if (ts <= 0) {
            return prefs().getString(KEY_AUTO_DAY, "").equals(date.toString()) ? "今日已同步" : "本机缓存";
        }
        String clock = new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date(ts));
        return "缓存于 " + clock;
    }

    private String statusText() {
        String head = this.viewDate.format(FMT_TITLE);
        if (this.loading) {
            return "正在刷新 " + head + " …";
        }
        if (this.lastData != null) {
            return head + " · " + cacheAge(this.viewDate);
        }
        return head;
    }

    // ---------------------------------------------------------------- 界面

    private void build() {
        getWindow().setStatusBarColor(this.bg);
        getWindow().setNavigationBarColor(this.bg);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        int flags = this.dark ? 0 : (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().getDecorView().setSystemUiVisibility(flags);

        LinearLayout screenRoot = new LinearLayout(this);
        this.screenRoot = screenRoot;
        screenRoot.setOrientation(LinearLayout.VERTICAL);
        screenRoot.setBackgroundColor(this.bg);

        ScrollView scrollView = new ScrollView(this);
        this.courseScroll = scrollView;
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(this.bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20f), dp(18f), dp(20f), dp(104f));
        root.setBackgroundColor(this.bg);
        scrollView.addView(root);
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(this.bg));

        // 即存式大标题区：大标题 + 副题，右侧 icon 胶囊
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(16);
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        TextView title = txt("智慧思政课表", 26, this.ink, true);
        title.setLetterSpacing(-0.02f);
        heading.addView(title);
        heading.addView(txt("课表 · 签到 · 卫生检查", 13, this.muted, false), top(4));
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));

        FrameLayout hygieneBtn = iconChip(SvgIcon.BROOM);
        hygieneBtn.setContentDescription("卫生检查");
        hygieneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] loc = new int[2];
                view.getLocationInWindow(loc);
                Intent intent = new Intent(MainActivity.this, cn.zuwe.hygiene.HygieneHistoryActivity.class);
                intent.putExtra("reveal_x", loc[0] + view.getWidth() / 2);
                intent.putExtra("reveal_y", loc[1] + view.getHeight() / 2);
                PageMotion.captureBackdrop(MainActivity.this);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        LinearLayout.LayoutParams hygieneLp = new LinearLayout.LayoutParams(dp(40f), dp(40f));
        hygieneLp.rightMargin = dp(8f);
        header.addView(hygieneBtn, hygieneLp);

        this.themeBtn = iconChip(this.dark ? SvgIcon.SUN : SvgIcon.MOON);
        this.themeBtn.setContentDescription("切换浅色 / 深色");
        this.themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { toggleTheme(); }
        });
        header.addView(this.themeBtn, new LinearLayout.LayoutParams(dp(40f), dp(40f)));
        root.addView(header);

        this.headerBlock = header;
        root.addView(datePanel(), top(10));

        LinearLayout scheduleHead = new LinearLayout(this);
        scheduleHead.setOrientation(LinearLayout.HORIZONTAL);
        scheduleHead.setGravity(16);
        this.sectionNote = txt("课程安排", 18, this.ink, true);
        scheduleHead.addView(this.sectionNote, new LinearLayout.LayoutParams(0, -2, 1f));
        FrameLayout gridBtn = iconChip(SvgIcon.TIMETABLE);
        gridBtn.setContentDescription("周次总览");
        gridBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { openWeekOverview(); }
        });
        scheduleHead.addView(gridBtn, new LinearLayout.LayoutParams(dp(38f), dp(38f)));
        root.addView(scheduleHead, top(22));

        this.courseList = new LinearLayout(this);
        this.courseList.setOrientation(LinearLayout.VERTICAL);
        root.addView(this.courseList, top(10));

        root.addView(sectionHead("", "账户凭证", "授权返回后自动加密保存到本机"), top(24));
        root.addView(authPanel(), top(10));

        root.addView(txt("凭证仅保存在本机加密存储中。本应用不会伪造登录 code。", 11, this.faint, false), top(12));
        root.addView(txt("v3.5.41 · 自动更新", 11, this.faint, false), top(6));
        root.addView(AboutFooter.build(this, this.ink, this.muted, this.faint, this.line, this.cardBg, this.bgAlt, this.green), top(6));

        this.signInScroll = new ScrollView(this);
        this.signInScroll.setFillViewport(true);
        this.signInScroll.setBackgroundColor(this.bg);
        LinearLayout signInRoot = buildSignInPage();
        this.signInScroll.addView(signInRoot);

        FrameLayout body = new FrameLayout(this);
        body.addView(this.courseScroll, new FrameLayout.LayoutParams(-1, -1));
        body.addView(this.signInScroll, new FrameLayout.LayoutParams(-1, -1));

        this.bottomNav = buildBottomNavigation();
        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(-1, -2);
        navLp.gravity = Gravity.BOTTOM;
        body.addView(this.bottomNav, navLp);

        screenRoot.addView(body, new LinearLayout.LayoutParams(-1, 0, 1f));

        // 键盘弹出时用「提交签到」替换底部导航，避免底栏悬浮在输入法上方。
        this.imeSubmitBar = new LinearLayout(this);
        this.imeSubmitBar.setOrientation(LinearLayout.VERTICAL);
        this.imeSubmitBar.setBackgroundColor(this.cardBg);
        this.imeSubmitBar.setPadding(dp(14f), dp(8f), dp(14f), dp(8f));
        this.imeSubmitBar.setVisibility(View.GONE);
        this.imeSubmitButton = primaryBtn("提交签到");
        this.imeSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { beginSignIn(); }
        });
        this.imeSubmitBar.addView(this.imeSubmitButton, new LinearLayout.LayoutParams(-1, dp(52f)));
        screenRoot.addView(this.imeSubmitBar, new LinearLayout.LayoutParams(-1, -2));

        this.silentHost = new FrameLayout(this);
        screenRoot.addView(this.silentHost, new LinearLayout.LayoutParams(dp(2f), dp(2f)));
        this.courseScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override public void onScrollChanged() {
                updateTopBar(courseScroll.getScrollY());
            }
        });
        this.signInScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override public void onScrollChanged() {
            }
        });
        setContentView(screenRoot);
        screenRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() { syncImeState(); }
        });
        switchPage(this.activePage, false);
        setSignInBusy(this.signInSubmitting);
        if (this.signInStats != null || this.signInHistory != null) {
            renderSignInData();
        }
        updateDateHeader();
    }

    private LinearLayout buildSignInPage() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20f), dp(18f), dp(20f), dp(104f));
        root.setBackgroundColor(this.bg);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(16);
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        TextView attend = txt("课堂签到", 26, this.ink, true);
        attend.setLetterSpacing(-0.02f);
        heading.addView(attend);
        heading.addView(txt("统计与记录 · 近六个月", 13, this.muted, false), top(4));
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));
        Button refresh = ghostBtn("刷新数据");
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { refreshSignInData(); }
        });
        header.addView(refresh);
        root.addView(header);

        root.addView(sectionHead("", "签到统计", "服务端数据"), top(22));
        LinearLayout statsPanel = panel();
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(metricCell("正常签到", "--", true), new LinearLayout.LayoutParams(0, -2, 1f));
        row1.addView(metricCell("缺勤", "--", false), new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(metricCell("迟到", "--", false), new LinearLayout.LayoutParams(0, -2, 1f));
        row2.addView(metricCell("应签到", "--", false), new LinearLayout.LayoutParams(0, -2, 1f));
        this.statRight = (TextView) ((LinearLayout) row1.getChildAt(0)).getChildAt(1);
        this.statAbsent = (TextView) ((LinearLayout) row1.getChildAt(1)).getChildAt(1);
        this.statLate = (TextView) ((LinearLayout) row2.getChildAt(0)).getChildAt(1);
        this.statTotal = (TextView) ((LinearLayout) row2.getChildAt(1)).getChildAt(1);
        statsPanel.addView(row1);
        statsPanel.addView(row2, top(14));
        root.addView(statsPanel, top(10));

        root.addView(sectionHead("", "手动签到", "输入教师提供的签到码"), top(22));
        LinearLayout manual = panel();
        this.signInCodeInput = new EditText(this);
        this.signInCodeInput.setSingleLine(true);
        this.signInCodeInput.setTextSize(16f);
        this.signInCodeInput.setTextColor(this.ink);
        this.signInCodeInput.setHintTextColor(this.faint);
        this.signInCodeInput.setHint("请输入签到码");
        this.signInCodeInput.setPadding(dp(14f), dp(10f), dp(14f), dp(10f));
        this.signInCodeInput.setBackground(inputBackground());
        manual.addView(this.signInCodeInput, new LinearLayout.LayoutParams(-1, dp(52f)));

        // 高级签到：默认收起，点标题行才展开推荐地点与坐标配置。
        LinearLayout advancedHeader = new LinearLayout(this);
        advancedHeader.setOrientation(LinearLayout.HORIZONTAL);
        advancedHeader.setGravity(16);
        advancedHeader.setPadding(0, dp(14f), 0, dp(2f));
        advancedHeader.setMinimumHeight(dp(44f));
        TextView advancedTitle = txt("高级签到 · 推荐位置", 13, this.ink, true);
        this.advancedToggle = txt("展开 ▾", 11, this.muted, false);
        advancedHeader.addView(advancedTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        advancedHeader.addView(this.advancedToggle);
        advancedHeader.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { setAdvancedExpanded(!advancedExpanded); }
        });
        manual.addView(advancedHeader);

        this.advancedBody = new LinearLayout(this);
        this.advancedBody.setOrientation(LinearLayout.VERTICAL);
        this.advancedBody.setVisibility(View.GONE);
        TextView advancedHint = txt("选择地点可一键填入坐标，提交时直接使用该坐标。清空两项则使用设备当前位置。", 10, this.faint, false);
        this.advancedBody.addView(advancedHint, top(6));
        LinearLayout presetRow = new LinearLayout(this);
        presetRow.setOrientation(LinearLayout.HORIZONTAL);
        Button presetBoxue = ghostBtn("博学楼");
        presetBoxue.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { fillRecommendedLocation("30.8361", "120.4422", "博学楼"); }
        });
        Button presetQiushi = ghostBtn("求实楼");
        presetQiushi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { fillRecommendedLocation("30.8359", "120.4404", "求实楼"); }
        });
        presetRow.addView(presetBoxue, new LinearLayout.LayoutParams(0, dp(42f), 1f));
        LinearLayout.LayoutParams qiushiParams = new LinearLayout.LayoutParams(0, dp(42f), 1f);
        qiushiParams.leftMargin = dp(8f);
        presetRow.addView(presetQiushi, qiushiParams);
        this.advancedBody.addView(presetRow, top(9));

        LinearLayout coordinateRow = new LinearLayout(this);
        coordinateRow.setOrientation(LinearLayout.HORIZONTAL);
        this.signInLatitudeInput = new EditText(this);
        this.signInLatitudeInput.setSingleLine(true);
        this.signInLatitudeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        this.signInLatitudeInput.setTextSize(14f);
        this.signInLatitudeInput.setTextColor(this.ink);
        this.signInLatitudeInput.setHintTextColor(this.faint);
        this.signInLatitudeInput.setHint("纬度 Latitude");
        this.signInLatitudeInput.setPadding(dp(12f), dp(8f), dp(12f), dp(8f));
        this.signInLatitudeInput.setBackground(inputBackground());
        this.signInLongitudeInput = new EditText(this);
        this.signInLongitudeInput.setSingleLine(true);
        this.signInLongitudeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        this.signInLongitudeInput.setTextSize(14f);
        this.signInLongitudeInput.setTextColor(this.ink);
        this.signInLongitudeInput.setHintTextColor(this.faint);
        this.signInLongitudeInput.setHint("经度 Longitude");
        this.signInLongitudeInput.setPadding(dp(12f), dp(8f), dp(12f), dp(8f));
        this.signInLongitudeInput.setBackground(inputBackground());
        coordinateRow.addView(this.signInLatitudeInput, new LinearLayout.LayoutParams(0, dp(48f), 1f));
        LinearLayout.LayoutParams longitudeParams = new LinearLayout.LayoutParams(0, dp(48f), 1f);
        longitudeParams.leftMargin = dp(8f);
        coordinateRow.addView(this.signInLongitudeInput, longitudeParams);
        this.advancedBody.addView(coordinateRow, top(9));
        manual.addView(this.advancedBody);

        this.signInSubmitButton = primaryBtn("提交签到");
        this.signInSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { beginSignIn(); }
        });
        manual.addView(this.signInSubmitButton, top(12));
        this.signInStatus = txt("提交时才会请求定位权限；不会使用虚构或固定坐标。", 11, this.faint, false);
        this.signInStatus.setLineSpacing(dp(3f), 1f);
        manual.addView(this.signInStatus, top(10));
        root.addView(manual, top(10));

        root.addView(sectionHead("", "签到记录", "近六个月 · 个人签到明细"), top(22));
        this.signInHistoryList = new LinearLayout(this);
        this.signInHistoryList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout historyPanel = panel();
        this.signInHistoryPanel = historyPanel;
        historyPanel.addView(this.signInHistoryList);
        root.addView(historyPanel, top(10));
        renderSignInData();
        root.addView(AboutFooter.build(this, this.ink, this.muted, this.faint, this.line, this.cardBg, this.bgAlt, this.green), top(16));
        return root;
    }

    private View metricCell(String label, String value, boolean highlight) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        TextView name = txt(label, 11, this.muted, false);
        TextView number = txt(value, 25, highlight ? this.green : this.ink, true);
        cell.addView(name);
        cell.addView(number, top(4));
        return cell;
    }

    private android.graphics.drawable.Drawable inputBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(this.bgAlt);
        drawable.setCornerRadius(dp(12f));
        drawable.setStroke(dp(1f), this.line);
        return drawable;
    }

    private FrameLayout buildBottomNavigation() {
        FrameLayout bar = new FrameLayout(this);
        bar.setPadding(dp(16f), 0, dp(16f), dp(12f));

        FrameLayout capsule = new FrameLayout(this);
        this.navCapsule = capsule;
        GradientDrawable capsuleBg = new GradientDrawable();
        capsuleBg.setColor(this.dark ? 0xE6151714 : 0xD9FFFFFF);
        capsuleBg.setCornerRadius(dp(31f));
        capsuleBg.setStroke(dp(1f), UiPolish.withAlpha(this.line, this.dark ? 0x70 : 0xAA));
        capsule.setBackground(capsuleBg);
        capsule.setElevation(dp(6f));
        FrameLayout.LayoutParams capsuleLp = new FrameLayout.LayoutParams(-1, dp(62f), Gravity.BOTTOM);
        bar.addView(capsule, capsuleLp);

        this.navPill = new View(this);
        GradientDrawable pill = new GradientDrawable();
        // 不透明的高亮块：旧按钮的图标/文字不会透上来变成一层“阴影”
        int pa = this.accentA;
        int pb = this.cardBg;
        pill.setColor(Color.rgb(
                (Color.red(pa) * 3 + Color.red(pb) * 7) / 10,
                (Color.green(pa) * 3 + Color.green(pb) * 7) / 10,
                (Color.blue(pa) * 3 + Color.blue(pb) * 7) / 10));
        pill.setCornerRadius(dp(24f));
        this.navPill.setBackground(pill);
        FrameLayout.LayoutParams pillLp = new FrameLayout.LayoutParams(dp(90f), dp(48f));
        pillLp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        pillLp.leftMargin = dp(6f);
        capsule.addView(this.navPill, pillLp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(6f), dp(7f), dp(6f), dp(7f));
        this.navCourse = navItem(SvgIcon.CALENDAR, "课表", 0);
        this.navSignIn = navItem(SvgIcon.PARSE, "签到", 1);
        row.addView(this.navCourse, new LinearLayout.LayoutParams(0, -1, 1f));
        row.addView(this.navSignIn, new LinearLayout.LayoutParams(0, -1, 1f));
        capsule.addView(row, new FrameLayout.LayoutParams(-1, -1));

        capsule.post(new Runnable() {
            @Override public void run() {
                selectNav(activePage, false);
            }
        });
        return bar;
    }

    private LinearLayout navItem(String iconPath, String label, final int page) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(17);
        SvgIcon.IconView icon = SvgIcon.view(this, iconPath, this.muted, 21);
        TextView text = txt(label, 10, this.muted, false);
        item.addView(icon, new LinearLayout.LayoutParams(dp(21f), dp(21f)));
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(-2, -2);
        textLp.topMargin = dp(2f);
        item.addView(text, textLp);
        item.setTag(new Object[]{ icon, text, iconPath });
        if (page == 0) { this.navCourseIcon = icon; this.navCourseLabel = text; }
        else { this.navSignInIcon = icon; this.navSignInLabel = text; }
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { switchPage(page, true); }
        });
        // 按压下沉 + 松手张力 1.9 的强回弹
        item.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        view.animate().cancel();
                        view.animate().scaleX(0.84f).scaleY(0.84f)
                                .setDuration(112L).setInterpolator(UiPolish.EASE_OUT).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.animate().cancel();
                        view.animate().scaleX(1f).scaleY(1f)
                                .setDuration(560L)
                                .setInterpolator(new OvershootInterpolator(2.6f)).start();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return item;
    }

    private void selectNav(int page, boolean animate) {
        if (this.navCourseIcon != null) {
            this.navCourseIcon.setIconColor(page == 0 ? this.green : this.muted);
            this.navCourseLabel.setTextColor(page == 0 ? this.green : this.muted);
            this.navCourseLabel.setTypeface(Typeface.DEFAULT, page == 0 ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (this.navSignInIcon != null) {
            this.navSignInIcon.setIconColor(page == 1 ? this.green : this.muted);
            this.navSignInLabel.setTextColor(page == 1 ? this.green : this.muted);
            this.navSignInLabel.setTypeface(Typeface.DEFAULT, page == 1 ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (this.navPill == null) return;
        final LinearLayout item = page == 0 ? this.navCourse : this.navSignIn;
        if (item == null) return;
        if (item.getWidth() <= 0) {
            // 首次布局还没量到尺寸，下一帧再试一次（只重试一次，避免空转）
            if (this.navCapsule == null || this.navPillPending) return;
            this.navPillPending = true;
            this.navCapsule.post(new Runnable() {
                @Override public void run() {
                    navPillPending = false;
                    selectNav(activePage, false);
                }
            });
            return;
        }
        View parent = (View) item.getParent();
        int inset = dp(5f);
        int left = item.getLeft() + parent.getLeft() + inset;
        int width = Math.max(dp(40f), item.getWidth() - inset * 2);
        int height = Math.max(dp(40f), item.getHeight());

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.navPill.getLayoutParams();
        lp.width = width;
        lp.height = height;
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        lp.leftMargin = left;
        int from = this.navPillX < 0 ? left : this.navPillX;
        this.navPillX = left;
        this.navPill.setLayoutParams(lp);
        this.navPill.animate().cancel();
        this.navPill.setTranslationX(from - left);
        if (animate) {
            this.navPill.setScaleX(0.82f);
            this.navPill.setScaleY(0.82f);
            this.navPill.animate().translationX(0f).scaleX(1f).scaleY(1f)
                    .setDuration(520L)
                    .setInterpolator(new OvershootInterpolator(2.4f))
                    .start();
        } else {
            this.navPill.setTranslationX(0f);
            this.navPill.setScaleX(1f);
            this.navPill.setScaleY(1f);
        }
    }

    private void updateTopBar(int y) {
        float t = Math.min(1f, y / (float) Math.max(1, dp(70f)));
        if (this.headerBlock != null) {
            this.headerBlock.setAlpha(1f - 0.92f * t);
            this.headerBlock.setTranslationY(-y * 0.28f);
        }
    }

    private void switchPage(int page, boolean load) {
        int from = this.activePage;
        this.activePage = page;
        View out = from == 0 ? this.courseScroll : this.signInScroll;
        View in = page == 0 ? this.courseScroll : this.signInScroll;
        if (out != null && in != null && out != in) {
            animatePageSlide(out, in, page > from ? 1 : -1);
        } else {
            if (this.courseScroll != null) this.courseScroll.setVisibility(page == 0 ? View.VISIBLE : View.GONE);
            if (this.signInScroll != null) this.signInScroll.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        }
        selectNav(page, true);
        applyBottomBarState();
        if (page == 1 && load) refreshSignInData();
    }

    /** 像翻小说一样：旧页向一侧让开并淡出，新页从另一侧推进来。 */
    private void animatePageSlide(final View out, final View in, final int dir) {
        int w = getResources().getDisplayMetrics().widthPixels;
        out.animate().cancel();
        in.animate().cancel();
        // 先把新页复位，避免上一次未走完的动画残留偏移/透明度
        in.setTranslationX(dir * w * 0.18f);
        in.setAlpha(0.35f);
        in.setVisibility(View.VISIBLE);
        in.animate().translationX(0f).alpha(1f)
                .setDuration(320L).setInterpolator(UiPolish.EASE_OUT).start();
        out.setVisibility(View.VISIBLE);
        out.animate().translationX(-dir * w * 0.12f).alpha(0f)
                .setDuration(260L).setInterpolator(UiPolish.EASE_OUT)
                .withEndAction(new Runnable() {
                    @Override public void run() {
                        out.setVisibility(View.GONE);
                        out.setTranslationX(0f);
                        out.setAlpha(1f);
                    }
                }).start();
    }

    private void refreshSignInData() {
        if (this.signInLoading) return;
        if (!ready()) {
            if (this.signInStatus != null) this.signInStatus.setText("请先在课表页完成官方账号授权。");
            return;
        }
        this.signInLoading = true;
        if (this.signInStatus != null) this.signInStatus.setText("正在同步签到统计和历史记录…");
        final LocalDate end = LocalDate.now();
        final LocalDate start = end.minusMonths(6);
        final String currentAuth = this.auth;
        final String currentCookie = this.cookie;
        this.io.execute(new Runnable() {
            @Override public void run() {
                JSONObject stats = null;
                JSONArray history = null;
                Exception failure = null;
                try {
                    SignInApi api = new SignInApi(MainActivity.this, currentAuth, currentCookie);
                    stats = api.getStatistics(start, end);
                    history = api.getHistory(start, end);
                } catch (Exception e) { failure = e; }
                final JSONObject resultStats = stats;
                final JSONArray resultHistory = history;
                final Exception error = failure;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        signInLoading = false;
                        if (error == null) {
                            signInStats = resultStats;
                            signInHistory = resultHistory;
                            renderSignInData();
                            if (signInStatus != null) signInStatus.setText("数据已更新 · " + LocalDate.now());
                        } else {
                            if (signInStatus != null) signInStatus.setText("同步失败：" + safeMessage(error));
                            if (error instanceof SignInApi.ApiException
                                    && ((SignInApi.ApiException) error).isAuthFailure()) {
                                if (signInStatus != null) signInStatus.setText("登录状态已失效，正在自动续期…");
                                trySilentReauth(new Runnable() {
                                    @Override public void run() {
                                        refreshSignInData();
                                    }
                                }, new Runnable() {
                                    @Override public void run() {
                                        if (signInStatus != null) signInStatus.setText("登录状态已失效，请重新授权");
                                        toast("登录状态已失效，请重新授权");
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void renderSignInData() {
        if (this.statRight != null) this.statRight.setText(countText(this.signInStats, "rightCount"));
        if (this.statAbsent != null) this.statAbsent.setText(countText(this.signInStats, "absentCount"));
        if (this.statLate != null) this.statLate.setText(countText(this.signInStats, "lateCount"));
        if (this.statTotal != null) this.statTotal.setText(countText(this.signInStats, "totalCount"));
        if (this.signInHistoryPanel == null) return;
        if (this.signInHistoryRows == null) this.signInHistoryRows = new ArrayList<View>();
        this.signInHistoryRows.clear();
        this.signInHistoryTotal = 0;
        if (this.signInHistory != null) {
            for (int i = 0; i < this.signInHistory.length(); i++) {
                JSONObject item = this.signInHistory.optJSONObject(i);
                if (item == null) continue;
                String date = firstValue(item, "courseDate", "signDate", "checkInDate", "date", "createTime", "checkTime");
                String course = firstValue(item, "courseName", "courseTitle", "className", "name");
                String state = firstValue(item, "statusName", "checkInStatus", "status", "result");
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.addView(txt(course.isEmpty() ? "课堂签到记录" : course, 13, this.ink, true));
                String detail = (date.isEmpty() ? "日期未提供" : date)
                        + (state.isEmpty() ? "" : " · " + state);
                row.addView(txt(detail, 11, this.muted, false), top(5));
                this.signInHistoryRows.add(row);
            }
        }
        this.signInHistoryTotal = this.signInHistoryRows.size();
        installHistoryCard(this.signInHistoryExpanded, false);
    }

    /** 用全新卡片替换旧卡片（重建视图，避免在原视图上直接改高度产生的重绘残缺）。 */
    private void replaceHistoryCard(LinearLayout panel) {
        LinearLayout old = this.signInHistoryPanel;
        if (old != null && old.getParent() instanceof LinearLayout) {
            LinearLayout parent = (LinearLayout) old.getParent();
            int index = parent.indexOfChild(old);
            android.view.ViewGroup.LayoutParams lp = old.getLayoutParams();
            parent.removeView(old);
            parent.addView(panel, index, lp);
        }
        this.signInHistoryPanel = panel;
    }

    private LinearLayout installHistoryCard(final boolean expanded, final boolean animateExpand) {
        final List<View> rows = this.signInHistoryRows == null
                ? new ArrayList<View>() : this.signInHistoryRows;
        // 先把旧列表里的行摘下来，否则重新挂到新列表会抛
        // IllegalStateException: The specified child already has a parent
        if (this.signInHistoryList != null) this.signInHistoryList.removeAllViews();
        final int total = rows.size();
        final int visible = expanded ? total : Math.min(3, total);

        final LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        final int[] lastVisibleIndex = {-1};
        if (total == 0) {
            list.addView(txt("暂无签到记录", 13, this.muted, false));
            list.addView(txt("服务器当前返回空记录；这不代表没有可签到任务。", 11, this.faint, false), top(6));
        } else {
            for (int i = 0; i < visible; i++) {
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
                if (i > 0) rlp.topMargin = dp(16f);
                list.addView(rows.get(i), rlp);
                lastVisibleIndex[0] = list.getChildCount() - 1;
            }
            if (total > 3) list.addView(buildHistoryToggle(total), top(4));
            if (animateExpand && total > 3) {
                for (int k = 3; k < total; k++) {
                    rows.get(k).setAlpha(0f);
                    rows.get(k).setTranslationY(k == 3 ? 0f : dp(14f));
                }
            }
        }

        LinearLayout panel = panel();
        panel.addView(list);
        replaceHistoryCard(panel);
        this.signInHistoryList = list;

        if (animateExpand && total > 3 && lastVisibleIndex[0] >= 0) {
            playHistoryEntrance(panel, list, lastVisibleIndex[0], total);
        }
        return panel;
    }

    /** 与课表页今日课程卡片完全一致：框向下拓展 + 文字上浮浮现。 */
    private void playHistoryEntrance(final LinearLayout panel, final LinearLayout list,
                                     final int lastVisibleIndex, final int total) {
        final List<View> rows = this.signInHistoryRows;
        final int extra = total - 3;
        panel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                panel.getViewTreeObserver().removeOnPreDrawListener(this);
                final int full = panel.getHeight();
                int h0 = panel.getPaddingTop() + panel.getPaddingBottom();
                View anchor = list.getChildAt(lastVisibleIndex);
                if (anchor != null) {
                    h0 += anchor.getBottom();
                    ViewGroup.LayoutParams alp = anchor.getLayoutParams();
                    if (alp instanceof LinearLayout.LayoutParams) {
                        h0 += ((LinearLayout.LayoutParams) alp).bottomMargin;
                    }
                }
                final ViewGroup.LayoutParams lp = panel.getLayoutParams();
                panel.setClipToOutline(false);
                if (full > h0 && h0 > 0 && lp != null) {
                    lp.height = h0;
                    panel.setLayoutParams(lp);
                    ValueAnimator grow = ValueAnimator.ofInt(h0, full);
                    grow.setDuration(460L + (extra - 1) * 120L);
                    grow.setInterpolator(UiPolish.EASE_IN_OUT);
                    grow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override public void onAnimationUpdate(ValueAnimator a) {
                            lp.height = (Integer) a.getAnimatedValue();
                            panel.setLayoutParams(lp);
                        }
                    });
                    grow.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(android.animation.Animator a) {
                            lp.height = -2;
                            panel.setLayoutParams(lp);
                            panel.setClipToOutline(true);
                            panel.invalidate();
                        }
                    });
                    grow.start();
                }
                for (int k = 0; k < extra; k++) {
                    View r = rows.get(3 + k);
                    r.animate().alpha(1f).translationY(0f)
                            .setStartDelay(100L + k * 140L)
                            .setDuration(k == 0 ? 700L : 650L)
                            .setInterpolator(UiPolish.EASE_OUT)
                            .start();
                }
                return true;
            }
        });
    }

    private View buildHistoryToggle(final int total) {
        LinearLayout toggle = new LinearLayout(this);
        toggle.setOrientation(LinearLayout.HORIZONTAL);
        toggle.setGravity(Gravity.CENTER);
        toggle.setPadding(0, dp(8f), 0, dp(6f));
        this.signInHistoryToggleLabel = txt(
                this.signInHistoryExpanded ? "收起签到记录" : "展开全部 " + total + " 条记录",
                12, this.muted, false);
        this.signInHistoryChevron = SvgIcon.view(this, SvgIcon.CHEV_R, this.muted, 13);
        this.signInHistoryChevron.setRotation(this.signInHistoryExpanded ? 270f : 90f);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(13f), dp(13f));
        ilp.leftMargin = dp(6f);
        toggle.addView(this.signInHistoryToggleLabel);
        toggle.addView(this.signInHistoryChevron, ilp);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { toggleHistoryExpanded(); }
        });
        UiPolish.pressable(toggle);
        this.signInHistoryToggle = toggle;
        return toggle;
    }

    private void toggleHistoryExpanded() {
        if (this.signInHistoryExpanded) collapseHistory();
        else {
            this.signInHistoryExpanded = true;
            installHistoryCard(true, true);
        }
    }

    /** 收起：新卡片从当前高度折回到前 3 条的高度。 */
    private void collapseHistory() {
        LinearLayout oldPanel = this.signInHistoryPanel;
        final int from = oldPanel == null ? 0 : oldPanel.getHeight();
        this.signInHistoryExpanded = false;
        final LinearLayout panel = installHistoryCard(false, false);
        final ViewGroup.LayoutParams lp = panel.getLayoutParams();
        if (lp == null || from <= 0) return;
        int width = panel.getWidth() > 0 ? panel.getWidth() : getResources().getDisplayMetrics().widthPixels;
        panel.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int to = panel.getMeasuredHeight();
        if (to <= 0 || to >= from) return;
        panel.setClipToOutline(false);
        lp.height = from;
        panel.setLayoutParams(lp);
        ValueAnimator shrink = ValueAnimator.ofInt(from, to);
        shrink.setDuration(460L);
        shrink.setInterpolator(UiPolish.EASE_IN_OUT);
        shrink.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator a) {
                lp.height = (Integer) a.getAnimatedValue();
                panel.setLayoutParams(lp);
            }
        });
        shrink.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator a) {
                lp.height = -2;
                panel.setLayoutParams(lp);
                panel.setClipToOutline(true);
                panel.invalidate();
            }
        });
        shrink.start();
        if (this.signInHistoryChevron != null) {
            this.signInHistoryChevron.setRotation(270f);
            this.signInHistoryChevron.animate().rotation(90f)
                    .setDuration(320L).setInterpolator(UiPolish.EASE_IN_OUT).start();
        }
    }

    private String countText(JSONObject object, String key) {
        if (object == null || !object.has(key) || object.isNull(key)) return "--";
        return object.optString(key, "--");
    }

    private String firstValue(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.optString(key, "");
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) return value;
        }
        return "";
    }

    private String safeMessage(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private void setAdvancedExpanded(boolean expanded) {
        this.advancedExpanded = expanded;
        if (!expanded) {
            // 收起即回到普通模式：清空隐藏的坐标，提交时重新获取设备位置。
            if (this.signInLatitudeInput != null) this.signInLatitudeInput.setText("");
            if (this.signInLongitudeInput != null) this.signInLongitudeInput.setText("");
            if (this.signInStatus != null) this.signInStatus.setText("已收起高级签到，提交时将获取设备当前位置。");
        }
        if (this.advancedBody != null) UiPolish.accordion(this.advancedBody, expanded);
        updateAdvancedSummary();
    }

    private void updateAdvancedSummary() {
        if (this.advancedToggle == null) return;
        boolean filled = this.signInLatitudeInput != null && this.signInLongitudeInput != null
                && !this.signInLatitudeInput.getText().toString().trim().isEmpty()
                && !this.signInLongitudeInput.getText().toString().trim().isEmpty();
        String marker = this.advancedExpanded ? "收起 ▴" : "展开 ▾";
        if (filled) this.advancedToggle.setText(marker + " · 已用自定义坐标");
        else this.advancedToggle.setText(this.advancedExpanded ? "收起 ▴" : "展开 ▾");
    }

    private void fillRecommendedLocation(String latitude, String longitude, String name) {
        if (this.signInLatitudeInput != null) this.signInLatitudeInput.setText(latitude);
        if (this.signInLongitudeInput != null) this.signInLongitudeInput.setText(longitude);
        updateAdvancedSummary();
        if (this.signInStatus != null) this.signInStatus.setText("已填入" + name + "坐标，提交时直接使用该坐标。");
    }

    /**
     * 读取手填坐标：两项都空返回 null（走设备定位）；只填一项或格式非法抛 IllegalArgumentException。
     */
    private double[] readSelectedCoordinates() {
        if (!this.advancedExpanded) return null;
        String latitudeText = this.signInLatitudeInput == null ? "" : this.signInLatitudeInput.getText().toString().trim();
        String longitudeText = this.signInLongitudeInput == null ? "" : this.signInLongitudeInput.getText().toString().trim();
        if (latitudeText.isEmpty() && longitudeText.isEmpty()) return null;
        if (latitudeText.isEmpty() || longitudeText.isEmpty()) {
            throw new IllegalArgumentException("请同时填写纬度和经度，或清空两项使用设备定位。");
        }
        double selectedLatitude = Double.parseDouble(latitudeText);
        double selectedLongitude = Double.parseDouble(longitudeText);
        if (Double.isNaN(selectedLatitude) || Double.isInfinite(selectedLatitude)
                || Double.isNaN(selectedLongitude) || Double.isInfinite(selectedLongitude)
                || selectedLatitude < -90d || selectedLatitude > 90d
                || selectedLongitude < -180d || selectedLongitude > 180d) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        return new double[]{selectedLatitude, selectedLongitude};
    }

    private void beginSignIn() {
        if (this.signInSubmitting) return;
        String code = this.signInCodeInput == null ? "" : this.signInCodeInput.getText().toString().trim();
        if (code.isEmpty()) {
            toast("请先输入签到码");
            return;
        }
        if (!ready()) return;
        double[] selected;
        try {
            selected = readSelectedCoordinates();
        } catch (IllegalArgumentException invalid) {
            if (this.signInStatus != null) this.signInStatus.setText(invalid.getMessage());
            return;
        }
        this.pendingSignInCode = code;
        setSignInBusy(true);
        // 高级模式：手填/推荐坐标存在时直接提交，不再申请定位权限、不取设备定位。
        if (selected != null) {
            if (this.signInStatus != null) this.signInStatus.setText("正在使用所选坐标提交签到码…");
            submitSignInCode(code, selected[0], selected[1]);
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= 23
                && (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            if (this.signInStatus != null) this.signInStatus.setText("请允许定位权限，以便按平台要求提交真实坐标。");
            return;
        }
        requestCurrentLocationAndSubmit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            boolean granted = false;
            for (int result : grantResults) if (result == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) requestCurrentLocationAndSubmit();
            else {
                setSignInBusy(false);
                if (this.signInStatus != null) this.signInStatus.setText("未获得定位权限，未发送签到请求。");
            }
        }
    }

    private void requestCurrentLocationAndSubmit() {
        try {
            this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (this.locationManager == null) throw new IllegalStateException("设备定位服务不可用");
            Criteria criteria = new Criteria();
            boolean hasFine = android.os.Build.VERSION.SDK_INT < 23
                    || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            criteria.setAccuracy(hasFine ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
            String provider = this.locationManager.getBestProvider(criteria, true);
            if (provider == null || (!hasFine && LocationManager.GPS_PROVIDER.equals(provider))) {
                if (this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) provider = LocationManager.NETWORK_PROVIDER;
                else if (hasFine && this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) provider = LocationManager.GPS_PROVIDER;
                else provider = null;
            }
            if (provider == null) throw new IllegalStateException("请开启系统定位服务后重试");
            if (this.signInStatus != null) this.signInStatus.setText("正在获取当前位置…");
            final String selectedProvider = provider;
            this.pendingLocationListener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    stopLocationRequest();
                    if (location == null) {
                        setSignInBusy(false);
                        if (signInStatus != null) signInStatus.setText("未能获取定位，请重试。");
                        return;
                    }
                    if (location.hasAccuracy() && location.getAccuracy() > 1000f) {
                        setSignInBusy(false);
                        if (signInStatus != null) signInStatus.setText("定位精度不足（" + (int) location.getAccuracy() + " 米），请稍后重试。");
                        return;
                    }
                    double submitLatitude = location.getLatitude();
                    double submitLongitude = location.getLongitude();
                    double[] selected;
                    try {
                        selected = readSelectedCoordinates();
                    } catch (IllegalArgumentException invalid) {
                        setSignInBusy(false);
                        if (signInStatus != null) signInStatus.setText(invalid.getMessage());
                        return;
                    }
                    // 定位回来时如果用户已填好坐标，直接用所选坐标提交（不再做距离校验）。
                    if (selected != null) {
                        submitSignInCode(pendingSignInCode, selected[0], selected[1]);
                        return;
                    }
                    submitSignInCode(pendingSignInCode, submitLatitude, submitLongitude);
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onProviderDisabled(String provider) { }
            };
            this.locationManager.requestSingleUpdate(selectedProvider, this.pendingLocationListener, Looper.getMainLooper());
            this.locationTimeout = new Runnable() {
                @Override public void run() {
                    stopLocationRequest();
                    setSignInBusy(false);
                    if (signInStatus != null) signInStatus.setText("定位超时，尚未发送签到请求。请检查定位服务后重试。");
                }
            };
            this.locationHandler.postDelayed(this.locationTimeout, 15000L);
        } catch (SecurityException e) {
            setSignInBusy(false);
            if (this.signInStatus != null) this.signInStatus.setText("定位权限不足，未发送签到请求。");
        } catch (Exception e) {
            setSignInBusy(false);
            if (this.signInStatus != null) this.signInStatus.setText("定位失败：" + safeMessage(e));
        }
    }

    private void stopLocationRequest() {
        if (this.locationTimeout != null) this.locationHandler.removeCallbacks(this.locationTimeout);
        this.locationTimeout = null;
        if (this.locationManager != null && this.pendingLocationListener != null) {
            try { this.locationManager.removeUpdates(this.pendingLocationListener); }
            catch (SecurityException ignored) { }
        }
        this.pendingLocationListener = null;
    }

    private void setSignInBusy(boolean busy) {
        this.signInSubmitting = busy;
        if (this.signInSubmitButton != null) {
            this.signInSubmitButton.setEnabled(!busy);
            this.signInSubmitButton.setAlpha(busy ? 0.72f : 1f);
        }
        if (this.imeSubmitButton != null) {
            this.imeSubmitButton.setEnabled(!busy);
            this.imeSubmitButton.setAlpha(busy ? 0.72f : 1f);
        }
    }

    /** 根据键盘是否弹出，切换「底部导航」和「提交按钮」。 */
    private void syncImeState() {
        if (this.screenRoot == null) return;
        Rect visible = new Rect();
        this.screenRoot.getWindowVisibleDisplayFrame(visible);
        int screenHeight = this.screenRoot.getRootView().getHeight();
        boolean open = screenHeight - visible.bottom > dp(120f);
        if (open != this.imeOpen) {
            this.imeOpen = open;
            applyBottomBarState();
        }
    }

    private void applyBottomBarState() {
        boolean showSubmit = this.imeOpen && this.activePage == 1;
        if (this.bottomNav != null) this.bottomNav.setVisibility(showSubmit ? View.GONE : View.VISIBLE);
        if (this.imeSubmitBar != null) this.imeSubmitBar.setVisibility(showSubmit ? View.VISIBLE : View.GONE);
    }

    private void submitSignInCode(final String code, final double latitude, final double longitude) {
        if (this.signInStatus != null) this.signInStatus.setText("正在提交签到码…（不会自动重复提交）");
        final String currentAuth = this.auth;
        final String currentCookie = this.cookie;
        this.io.execute(new Runnable() {
            @Override public void run() {
                JSONObject result = null;
                Exception failure = null;
                try {
                    result = new SignInApi(MainActivity.this, currentAuth, currentCookie)
                            .validateCode(code, latitude, longitude);
                } catch (Exception e) { failure = e; }
                final JSONObject response = result;
                final Exception error = failure;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        setSignInBusy(false);
                        if (error != null) {
                            if (signInStatus != null) signInStatus.setText("提交失败：" + safeMessage(error));
                            if (error instanceof SignInApi.ApiException
                                    && ((SignInApi.ApiException) error).isAuthFailure()) {
                                if (signInStatus != null) signInStatus.setText("登录状态已失效，正在自动续期后重试…");
                                trySilentReauth(new Runnable() {
                                    @Override public void run() {
                                        submitSignInCode(code, latitude, longitude);
                                    }
                                }, new Runnable() {
                                    @Override public void run() {
                                        if (signInStatus != null) signInStatus.setText("登录状态已失效，请重新授权后再签到");
                                    }
                                });
                            }
                            return;
                        }
                        int businessCode = response.optInt("code", -1);
                        String message = response.optString("message", "");
                        if (businessCode == 400) {
                            if (signInStatus != null) signInStatus.setText(message.isEmpty() ? "签到码无效。" : message);
                        } else if (businessCode == 200 || businessCode == 0) {
                            UiPolish.successPopup(MainActivity.this, "签到成功！",
                                    "正在刷新签到记录核对…", MainActivity.this.green);
                            if (signInStatus != null) signInStatus.setText("服务器返回成功响应，正在刷新记录核对签到状态…");
                            refreshSignInData();
                        } else {
                            if (signInStatus != null) signInStatus.setText("服务器返回待确认结果："
                                    + (message.isEmpty() ? response.toString() : message));
                        }
                    }
                });
            }
        });
    }

    private View datePanel() {
        LinearLayout panel = panel();
        panel.setPadding(dp(18f), dp(18f), dp(18f), dp(14f));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER_VERTICAL);
        left.setPadding(dp(4f), 0, 0, 0);
        this.dateTitle = new TextView(this);
        this.dateTitle.setGravity(Gravity.LEFT);
        this.dateTitle.setTextSize(30f);
        this.dateTitle.setTextColor(this.ink);
        this.dateTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        this.dateTitle.setLetterSpacing(-0.02f);
        this.dateTitle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { pickDate(); }
        });
        this.dateTitle.setContentDescription("选择日期");
        this.dateSub = txt("", 12, this.muted, false);
        this.dateSub.setGravity(Gravity.LEFT);
        left.addView(this.dateTitle, new LinearLayout.LayoutParams(-2, -2));
        left.addView(this.dateSub, top(3));
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1f));

        FrameLayout calChip = iconChip(SvgIcon.CALENDAR);
        calChip.setContentDescription("打开日历选择日期");
        calChip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { pickDate(); }
        });
        row.addView(calChip, new LinearLayout.LayoutParams(dp(40f), dp(40f)));
        panel.addView(row);

        LinearLayout weekRow = new LinearLayout(this);
        weekRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView weekPrev = chevron("‹", "上一周");
        weekPrev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { jumpTo(viewDate.plusWeeks(-1L)); }
        });
        weekRow.addView(weekPrev, new LinearLayout.LayoutParams(dp(26f), dp(32f)));

        this.weekLabel = txt("", 14, this.ink, true);
        this.weekLabel.setGravity(Gravity.CENTER);
        this.weekLabel.setContentDescription("点击查看周次总览，长按校准第 1 周");
        this.weekLabel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { openWeekOverview(); }
        });
        this.weekLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) { calibrateSemester(); return true; }
        });
        LinearLayout.LayoutParams wlLp = new LinearLayout.LayoutParams(-2, dp(32f));
        wlLp.leftMargin = dp(2f);
        wlLp.rightMargin = dp(2f);
        weekRow.addView(this.weekLabel, wlLp);

        TextView weekNext = chevron("›", "下一周");
        weekNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { jumpTo(viewDate.plusWeeks(1L)); }
        });
        weekRow.addView(weekNext, new LinearLayout.LayoutParams(dp(26f), dp(32f)));

        View spacer = new View(this);
        weekRow.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView overview = txt("周次总览", 12, this.muted, false);
        overview.setGravity(Gravity.CENTER);
        overview.setPadding(dp(8f), 0, dp(2f), 0);
        overview.setContentDescription("周次总览");
        overview.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { openWeekOverview(); }
        });
        UiPolish.pressable(overview);
        this.weekOverviewBtn = overview;
        weekRow.addView(overview, new LinearLayout.LayoutParams(-2, dp(32f)));

        LinearLayout.LayoutParams weekRowLp = new LinearLayout.LayoutParams(-1, -2);
        weekRowLp.topMargin = dp(12f);
        panel.addView(weekRow, weekRowLp);

        this.dayStrip = new LinearLayout(this);
        this.dayStrip.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams stripLp = new LinearLayout.LayoutParams(-1, -2);
        stripLp.topMargin = dp(10f);
        panel.addView(this.dayStrip, stripLp);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        this.status = txt("正在检查本机凭证…", 11, this.faint, false);
        this.status.setLineSpacing(dp(3f), 1f);
        bottom.addView(this.status, new LinearLayout.LayoutParams(0, -2, 1f));
        this.refreshBtn = ghostBtn("刷新");
        this.refreshBtn.setTextSize(12f);
        this.refreshBtn.setMinHeight(dp(32f));
        this.refreshBtn.setPadding(dp(14f), 0, dp(14f), 0);
        this.refreshBtn.setContentDescription("刷新课表");
        this.refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { refresh(true); }
        });
        LinearLayout.LayoutParams refreshLp = new LinearLayout.LayoutParams(-2, dp(32f));
        refreshLp.leftMargin = dp(10f);
        bottom.addView(this.refreshBtn, refreshLp);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(-1, -2);
        bottomLp.topMargin = dp(10f);
        panel.addView(bottom, bottomLp);

        updateDateHeader();
        return panel;
    }

    /** 横向滑动切换前后一天（整页手势，不拦截纵向滚动与点击）。 */
    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.touchDownX = ev.getX();
                this.touchDownY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                float dx = ev.getX() - this.touchDownX;
                float dy = ev.getY() - this.touchDownY;
                if (this.activePage == 0 && Math.abs(dx) > dp(72f) && Math.abs(dx) > Math.abs(dy) * 2f) {
                    jumpTo(this.viewDate.plusDays(dx < 0 ? 1L : -1L));
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private static final String[] WEEKDAY_CN = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
    private static final String[] WEEKDAY_SHORT = {"一", "二", "三", "四", "五", "六", "日"};

    /** 第三层：当前教学周横向七日选择器。 */
    private void renderDayStrip() {
        if (this.dayStrip == null) return;
        this.dayStrip.removeAllViews();
        LocalDate monday = WeekUtil.monday(this.viewDate);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            final LocalDate day = monday.plusDays(i);
            boolean selected = day.equals(this.viewDate);
            boolean isToday = day.equals(today);
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(14f));
            if (selected) {
                bg.setColor(this.green);
            } else if (isToday) {
                bg.setColor(UiPolish.withAlpha(this.green, this.dark ? 0x1F : 0x14));
            } else {
                bg.setColor(Color.TRANSPARENT);
            }
            cell.setBackground(bg);

            TextView week = txt(WEEKDAY_SHORT[i], 11, selected ? this.onGreen : this.muted, false);
            week.setGravity(Gravity.CENTER);
            TextView num = txt(String.valueOf(day.getDayOfMonth()), 15, selected ? this.onGreen : this.ink, true);
            num.setGravity(Gravity.CENTER);

            FrameLayout dotSlot = new FrameLayout(this);
            if (isToday && !selected) {
                View dot = new View(this);
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(this.green);
                dot.setBackground(dotBg);
                dotSlot.addView(dot, new FrameLayout.LayoutParams(dp(4f), dp(4f), Gravity.CENTER));
            }

            cell.addView(week, new LinearLayout.LayoutParams(-1, dp(14f)));
            cell.addView(num, new LinearLayout.LayoutParams(-1, dp(21f)));
            cell.addView(dotSlot, new LinearLayout.LayoutParams(-1, dp(7f)));

            cell.setContentDescription(day.format(FMT_TITLE) + " " + WEEKDAY_CN[i] + (isToday ? " 今天" : ""));
            cell.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) { jumpTo(day); }
            });
            UiPolish.pressable(cell);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54f), 1f);
            if (i > 0) lp.leftMargin = dp(4f);
            this.dayStrip.addView(cell, lp);
        }
    }

    /** 细描边不可见的纯文字箭头按钮。 */
    private TextView chevron(String glyph, String desc) {
        TextView tv = new TextView(this);
        tv.setText(glyph);
        tv.setTextSize(18f);
        tv.setTextColor(this.muted);
        tv.setGravity(Gravity.CENTER);
        tv.setContentDescription(desc);
        tv.setClickable(true);
        UiPolish.pressable(tv);
        return tv;
    }

    private View authPanel() {
        LinearLayout panel = panel();
        this.credentialState = txt("尚未检测到本地授权", 12, this.muted, false);
        panel.addView(this.credentialState);
        Button login = primaryBtn("微信 / 官方账号授权登录");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOAuth(false);
            }
        });
        panel.addView(login, top(12));
        Button importZip = ghostBtn("高级恢复：导入历史抓包 ZIP");
        importZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseCaptureZip();
            }
        });
        panel.addView(importZip, top(8));
        return panel;
    }

    private void jumpTo(LocalDate date) {
        this.viewDate = date;
        updateDateHeader();
        if (!ready()) {
            return;
        }
        List<Course> cached = loadCache(date);
        if (cached != null) {
            render(cached);
            this.lastData = cached;
            setStatus(date.format(FMT_TITLE) + " · " + cacheAge(date));
            return;
        }
        this.lastData = null;
        render(Collections.<Course>emptyList());
        refresh(false);
    }

    private void shiftDate(int delta) {
        LocalDate now = LocalDate.now();
        LocalDate target = this.viewDate.plusDays(delta);
        if (target.isBefore(now.minusDays(200L)) || target.isAfter(now.plusDays(200L))) {
            toast("已超出可浏览范围");
        } else {
            jumpTo(target);
        }
    }

    private void updateDateHeader() {
        if (this.dateTitle == null) return;
        LocalDate now = LocalDate.now();
        this.dateTitle.setText(this.viewDate.getMonthValue() + "月" + this.viewDate.getDayOfMonth() + "日");
        int week = weekOf(this.viewDate);
        String weekday = WEEKDAY_CN[this.viewDate.getDayOfWeek().getValue() - 1];
        this.dateSub.setText(weekday + (week >= 1 ? " · 第" + week + "教学周" : " · 假期"));
        if (this.weekLabel != null) {
            this.weekLabel.setText(week >= 1 ? "第" + week + "周" : "假期");
        }
        renderDayStrip();
        if (this.sectionNote != null) {
            if (this.viewDate.equals(now)) {
                this.sectionNote.setText("今日课程");
            } else if (this.viewDate.equals(now.plusDays(1L))) {
                this.sectionNote.setText("明日课程");
            } else {
                this.sectionNote.setText("课程安排");
            }
        }
    }

    private void styleTab(Button button, boolean active) {
        if (button == null) {
            return;
        }
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(22f));
        if (active) {
            drawable.setColor(UiPolish.withAlpha(this.accentA, this.dark ? 0x33 : 0x22));
            button.setTextColor(this.green);
            button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            drawable.setColor(0);
            button.setTextColor(this.muted);
            button.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }
        button.setBackground(drawable);
    }

    // ---------------------------------------------------------------- 授权

    private void loadSaved() {
        try {
            AuthManager.Credentials credentials = AuthManager.load(this);
            if (credentials != null) {
                this.auth = credentials.authorizationJwt;
                this.cookie = credentials.sklTokenJwt;
                refreshCredentialLine();
                smartStart();
                return;
            }
            if (this.credentialState != null) {
                this.credentialState.setText("尚未登录，请先完成官方授权");
                this.credentialState.setTextColor(this.muted);
            }
            setStatus("等待账号授权");
            render(Collections.<Course>emptyList());
        } catch (Exception e) {
            setStatus("凭证读取失败：" + e.getMessage());
        }
    }

    private void refreshCredentialLine() {
        if (this.credentialState == null) {
            return;
        }
        try {
            AuthManager.Credentials credentials = AuthManager.load(this);
            if (credentials != null) {
                String exp = TokenParser.expiryLabel(credentials.authorizationJwt);
                this.credentialState.setText("已连接 · Authorization 已加密保存"
                        + (credentials.sklTokenJwt.isEmpty() ? "" : " · 兼容凭证已保存")
                        + (exp.isEmpty() ? "" : " · " + exp));
                this.credentialState.setTextColor(this.green);
            }
        } catch (Exception ignored) {
        }
    }

    /** 每天第一次打开（含从后台回到前台）自动同步一次。 */
    private void maybeDailyRefresh() {
        if (this.loading) return;
        String today = LocalDate.now().toString();
        if (today.equals(prefs().getString(KEY_AUTO_DAY, ""))) return;
        try {
            if (AuthManager.load(this) == null) return;
        } catch (Exception e) {
            return;
        }
        autoSyncTodayAndTomorrow(LocalDate.now());
    }

    @Override protected void onPause() {
        if (this.uiHandler != null && this.stateTick != null) {
            this.uiHandler.removeCallbacks(this.stateTick);
        }
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        LocalDate now = LocalDate.now();
        if (!now.equals(this.openedDate)) {
            // 跨天回到前台：如果之前看的是「今天」，自动切到新的今天，而不是停在被天前的日期
            if (this.viewDate.equals(this.openedDate)) {
                this.viewDate = now;
                updateDateHeader();
                if (ready()) {
                    List<Course> cached = loadCache(now);
                    if (cached != null) {
                        render(cached);
                        this.lastData = cached;
                    } else {
                        this.lastData = null;
                        render(Collections.<Course>emptyList());
                    }
                }
            }
            this.openedDate = now;
        }
        maybeDailyRefresh();
        refreshWeekCalendar(false);
        // 从安装器返回时，若包里已经换成新版本，这里顺带清掉安装包与下载状态
        UpdateManager.cleanupAfterUpdate(this);
        // 后台已下好新包（静默下载完成）时，回到前台就提示安装
        if (!this.updatePrompted && UpdateManager.hasDownloaded(this)) {
            this.updatePrompted = true;
            UpdateManager.checkOnLaunch(this);
        }
    }

    private void pickDate() {
        LocalDate base = this.viewDate;
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override public void onDateSet(android.widget.DatePicker picker, int year, int month, int day) {
                jumpTo(LocalDate.of(year, month + 1, day));
            }
        }, base.getYear(), base.getMonthValue() - 1, base.getDayOfMonth());
        dialog.show();
    }

    private LocalDate semesterAnchor() {
        String saved = prefs().getString(KEY_SEM_ANCHOR, "");
        if (!saved.isEmpty()) {
            try { return LocalDate.parse(saved); } catch (Exception ignored) {}
        }
        LocalDate real = WeekCalendar.anchor(this);
        if (real != null) {
            return real;
        }
        return WeekUtil.defaultAnchor(LocalDate.now());
    }

    /** 周次以学校 /skl/teachweek 的真实数据为准，取不到才退回本地推算。 */
    private int weekOf(LocalDate date) {
        String saved = prefs().getString(KEY_SEM_ANCHOR, "");
        if (!saved.isEmpty()) {
            try { return WeekUtil.weekNumber(LocalDate.parse(saved), date); } catch (Exception ignored) {}
        }
        Integer real = WeekCalendar.weekFor(this, date);
        if (real != null) {
            return real.intValue();
        }
        return WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), date);
    }

    /** 用学校 /skl/teachweek 的真实数据校准教学周（缓存 6 小时）。 */
    private void refreshWeekCalendar(boolean force) {
        if (this.auth == null || this.auth.isEmpty()) {
            return;
        }
        if (!force && !WeekCalendar.isStale(this)) {
            updateDateHeader();
            return;
        }
        if (this.weekFetching) {
            return;
        }
        this.weekFetching = true;
        final String authorization = this.auth;
        final String sklToken = this.cookie;
        new Thread(new Runnable() {
            @Override public void run() {
                boolean ok = false;
                try {
                    JSONObject resp = new SchoolApi(MainActivity.this, authorization, sklToken)
                            .getTeachWeeks(LocalDate.now());
                    ok = WeekCalendar.save(MainActivity.this, resp);
                } catch (Exception ignored) {
                } finally {
                    MainActivity.this.weekFetching = false;
                }
                if (ok) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            updateDateHeader();
                        }
                    });
                }
            }
        }).start();
    }

    private void calibrateSemester() {
        LocalDate anchor = semesterAnchor();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override public void onDateSet(android.widget.DatePicker picker, int year, int month, int day) {
                LocalDate picked = LocalDate.of(year, month + 1, day);
                prefs().edit().putString(KEY_SEM_ANCHOR, WeekUtil.monday(picked).toString()).apply();
                updateDateHeader();
                LocalDate mon = WeekUtil.monday(picked);
                toast("第 1 周 = " + (mon.getMonthValue()) + "月" + mon.getDayOfMonth() + "日 起的一周");
            }
        }, anchor.getYear(), anchor.getMonthValue() - 1, anchor.getDayOfMonth()).show();
    }

    private void openWeekOverview() {
        Intent intent = new Intent(this, WeekOverviewActivity.class);
        LocalDate mon = WeekUtil.monday(this.viewDate);
        intent.putExtra("monday", mon.toString());
        intent.putExtra("week", weekOf(this.viewDate));
        UiPolish.containerTransform(this.weekLabel, () -> {
            PageMotion.captureBackdrop(MainActivity.this);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private boolean ready() {
        try {
            AuthManager.Credentials credentials = AuthManager.load(this);
            if (credentials == null) {
                setStatus("尚无有效本地授权，请先登录");
                toast("请先完成官方账号授权");
                return false;
            }
            this.auth = credentials.authorizationJwt;
            this.cookie = credentials.sklTokenJwt;
            return true;
        } catch (Exception e) {
            setStatus("凭证读取失败：" + e.getMessage());
            return false;
        }
    }

    private void startOAuth(boolean reauth) {
        setStatus(reauth ? "正在启动官方重新授权…" : "正在启动官方授权页面…");
        PageMotion.captureBackdrop(this);
        startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                this.lastData = null;
                prefs().edit().remove(KEY_AUTO_DAY).apply();
                loadSaved();
                return;
            }
            setStatus("授权页面已关闭；尚未确认获得新凭证");
            return;
        }
        if (requestCode == PICK_CAPTURE_ZIP && resultCode == RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            setStatus("正在读取历史抓包 ZIP…");
            this.io.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String text = CaptureZipReader.read(MainActivity.this, uri);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                importCaptureText(text);
                            }
                        });
                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setStatus("ZIP 导入失败：" + e.getMessage());
                                toast(e.getMessage());
                            }
                        });
                    }
                }
            });
        }
    }

    private void importCaptureText(String text) {
        try {
            AuthManager.ImportResult result = AuthManager.importCapture(this, text);
            this.auth = result.credentials.authorizationJwt;
            this.cookie = result.credentials.sklTokenJwt;
            this.lastData = null;
            prefs().edit().remove(KEY_AUTO_DAY).apply();
            loadSaved();
            setStatus("历史授权数据已加密保存；正在验证…");
        } catch (Exception e) {
            setStatus("识别/保存失败：" + e.getMessage());
            toast(e.getMessage());
        }
    }

    private void chooseCaptureZip() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/zip", "application/x-zip-compressed", "application/octet-stream"});
        startActivityForResult(intent, PICK_CAPTURE_ZIP);
    }

    // ---------------------------------------------------------------- 同步

    private void smartStart() {
        LocalDate now = LocalDate.now();
        List<Course> cached = loadCache(this.viewDate);
        if (cached != null) {
            // 先把缓存画出来，界面立刻可用，再去后台更新
            render(cached);
            setStatus(this.viewDate.format(FMT_TITLE) + " · " + cacheAge(this.viewDate));
        }
        boolean syncedToday = now.toString().equals(prefs().getString(KEY_AUTO_DAY, ""));
        if (syncedToday && cached != null) {
            return;
        }
        autoSyncTodayAndTomorrow(now);
    }

    /** 今天先出结果马上显示，明天随后后台补，避免首页干等两次请求。 */
    private void autoSyncTodayAndTomorrow(final LocalDate today) {
        if (this.loading) {
            return;
        }
        this.loading = true;
        setBusy(true);
        if (this.lastData == null && loadCache(today) == null) {
            showLoading();
        }
        setStatus("正在同步 " + today.format(FMT_TITLE) + " 与明天…");
        final LocalDate tomorrow = today.plusDays(1L);
        this.io.execute(new Runnable() {
            @Override
            public void run() {
                List<Course> todayList = null;
                Exception todayError = null;
                try {
                    todayList = fetchSync(today);
                } catch (Exception e) {
                    todayError = e;
                }
                final List<Course> first = todayList;
                final Exception firstError = todayError;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (first != null) {
                            saveCache(today, first);
                            if (viewDate.equals(today)) {
                                render(first);
                            }
                            setStatus(today.format(FMT_TITLE) + " · 已更新，正在补明天…");
                        }
                    }
                });
                List<Course> tomorrowList = null;
                Exception tomorrowError = null;
                try {
                    tomorrowList = fetchSync(tomorrow);
                } catch (Exception e) {
                    tomorrowError = e;
                }
                final List<Course> second = tomorrowList;
                final Exception secondError = tomorrowError;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onSyncFinished(today, first, firstError, tomorrow, second, secondError);
                    }
                });
            }
        });
    }

    private List<Course> fetchSync(LocalDate date) throws Exception {
        return new SchoolApi(this, this.auth, this.cookie).getTodayCourses(date);
    }

    private void onSyncFinished(LocalDate date, List<Course> list, Exception error,
                                LocalDate tomorrow, List<Course> tomorrowList, Exception tomorrowError) {
        this.loading = false;
        setBusy(false);
        if (list != null) {
            saveCache(date, list);
        }
        if (tomorrowList != null) {
            saveCache(tomorrow, tomorrowList);
        }
        if (list != null || tomorrowList != null) {
            prefs().edit().putString(KEY_AUTO_DAY, date.toString()).apply();
        }
        if (list != null) {
            if (this.viewDate.equals(date)) {
                render(list);
            }
            setStatus(date.format(FMT_TITLE) + " · 刚刚更新");
            return;
        }
        if (this.viewDate.equals(date) && loadCache(date) != null) {
            render(loadCache(date));
            setStatus(date.format(FMT_TITLE) + " · 更新失败，显示本机缓存");
            return;
        }
        Exception report = error != null ? error : tomorrowError;
        if (report != null) {
            handleApiError(report, true);
        }
    }

    private void refresh(boolean force) {
        if (this.loading) {
            toast("正在刷新，请稍候…");
            return;
        }
        if (!ready()) {
            return;
        }
        List<Course> cached = loadCache(this.viewDate);
        if (!force && cached != null) {
            render(cached);
            setStatus(this.viewDate.format(FMT_TITLE) + " · " + cacheAge(this.viewDate));
            return;
        }
        if (cached != null) {
            // 刷新时保留现有内容，只在状态栏提示，避免整页变「正在加载」
            render(cached);
        }
        startFetch(this.viewDate);
    }

    private void startFetch(final LocalDate date) {
        this.loading = true;
        setBusy(true);
        List<Course> cached = loadCache(date);
        if (cached == null || cached.isEmpty()) {
            showLoading();
        }
        setStatus("正在获取 " + date.format(FMT_TITLE) + " 的课表…");
        this.io.execute(new Runnable() {
            @Override
            public void run() {
                List<Course> list = null;
                Exception error = null;
                try {
                    list = fetchSync(date);
                } catch (Exception e) {
                    error = e;
                }
                final List<Course> result = list;
                final Exception failure = error;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onFetchFinished(date, result, failure);
                    }
                });
            }
        });
    }

    private void onFetchFinished(LocalDate date, List<Course> list, Exception error) {
        this.loading = false;
        setBusy(false);
        if (list != null) {
            saveCache(date, list);
            if (this.viewDate.equals(date)) {
                render(list);
                setStatus(date.format(FMT_TITLE) + " · 刚刚更新 · " + list.size() + " 门课程");
            }
            return;
        }
        if (error != null) {
            handleApiError(error, true);
        }
    }

    private void setBusy(boolean busy) {
        if (this.refreshBtn != null) {
            this.refreshBtn.setEnabled(!busy);
            this.refreshBtn.setText(busy ? "正在刷新…" : "刷新课表");
            this.refreshBtn.setAlpha(busy ? 0.6f : 1f);
        }
    }

    private void showLoading() {
        if (this.courseList == null) {
            return;
        }
        // 骨架屏 + Shimmer 微光扫过，接口返回后 render() 会替换为真实内容
        this.skeletonStop = UiPolish.skeleton(this.courseList, 3,
                UiPolish.withAlpha(this.cardBg, this.dark ? 0xCC : 0xD9),
                this.dark ? 0x30FFFFFF : 0x50FFFFFF,
                UiPolish.withAlpha(this.line, 0x77));
    }

    private void render(List<Course> list) {
        this.lastData = list;
        if (this.courseList == null) return;
        if (this.skeletonStop != null) { this.skeletonStop.run(); this.skeletonStop = null; }
        this.courseList.removeAllViews();
        scheduleStateTick();
        String day = this.viewDate.format(FMT_TITLE);
        if (list == null || list.isEmpty()) {
            LinearLayout empty = new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20f), dp(40f), dp(20f), dp(44f));
            empty.addView(txt("这一天没有课", 15, this.ink, true));
            empty.addView(txt("左右滑动可以切换前后一天", 12, this.faint, false), top(8));
            this.courseList.addView(empty);
            if (!this.quietRender) UiPolish.flyIn(empty, 0);
            setStatus(day + " · 0 门课程");
            return;
        }

        List<Course> sorted = new ArrayList<Course>(list);
        Collections.sort(sorted, new java.util.Comparator<Course>() {
            @Override public int compare(Course a, Course b) { return Integer.compare(a.startSection, b.startSection); }
        });

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, dp(6f), 0, dp(6f));
        UiPolish.glass(card, this.cardBg, this.line, 24f);
        this.courseList.addView(card);

        int nowMin = nowMinutes();
        boolean isToday = this.viewDate.equals(LocalDate.now());
        List<View> animRows = new ArrayList<View>();
        for (int i = 0; i < sorted.size(); i++) {
            Course c = sorted.get(i);
            if (i > 0) card.addView(separatorRow());
            View row = timelineRow(c, nowMin, isToday);
            card.addView(row);
            animRows.add(row);
        }
        if (!this.quietRender) playCardEntrance(card, animRows);
        setStatus(day + " · " + sorted.size() + " 门课程");
    }

    /**
     * 卡片（白框）跟着第一条课程一起从左侧滑入，随后卡片逐个长高、课程逐条淡入。
     * 单条 1500ms，错开 500ms。
     */
    private void playCardEntrance(final View card, final List<View> rows) {
        final int n = rows.size();
        if (n == 0) return;
        for (int i = 0; i < n; i++) {
            View r = rows.get(i);
            r.setAlpha(0f);
            r.setTranslationY(i == 0 ? 0f : dp(14f));
        }
        card.setAlpha(0f);
        card.setTranslationX(-dp(110f));
        card.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                card.getViewTreeObserver().removeOnPreDrawListener(this);
                final int full = card.getHeight();
                int h0 = rows.get(0).getHeight() + card.getPaddingTop() + card.getPaddingBottom();
                final ViewGroup.LayoutParams lp = card.getLayoutParams();
                if (full > h0 && h0 > 0) {
                    lp.height = h0;
                    card.setLayoutParams(lp);
                    ValueAnimator grow = ValueAnimator.ofInt(h0, full);
                    grow.setDuration(800L + (n - 1) * 500L);
                    grow.setInterpolator(UiPolish.EASE_IN_OUT);
                    grow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override public void onAnimationUpdate(ValueAnimator a) {
                            lp.height = (Integer) a.getAnimatedValue();
                            card.setLayoutParams(lp);
                        }
                    });
                    grow.start();
                }
                card.animate().alpha(1f).translationX(0f)
                        .setDuration(1800L)
                        .setInterpolator(UiPolish.BOUNCE)
                        .withEndAction(new Runnable() {
                            @Override public void run() {
                                lp.height = -2;
                                card.setLayoutParams(lp);
                                card.setTranslationX(0f);
                            }
                        })
                        .start();
                for (int i = 0; i < n; i++) {
                    View r = rows.get(i);
                    if (i == 0) {
                        // 第一条跟着卡片慢慢显影，别抢在框还没到位时先亮出来
                        r.animate().alpha(1f).translationY(0f)
                                .setStartDelay(300L)
                                .setDuration(1700L)
                                .setInterpolator(UiPolish.EASE_IN_OUT)
                                .start();
                    } else {
                        r.animate().alpha(1f).translationY(0f)
                                .setStartDelay(i * 500L)
                                .setDuration(1500L)
                                .setInterpolator(UiPolish.EASE_OUT)
                                .start();
                    }
                }
                return true;
            }
        });
    }

    private void scheduleStateTick() {
        if (this.uiHandler == null) return;
        if (this.stateTick == null) {
            this.stateTick = new Runnable() {
                @Override public void run() {
                    if (lastData != null && courseList != null && viewDate.equals(LocalDate.now())) {
                        quietRender = true;
                        render(lastData);
                        quietRender = false;
                    }
                }
            };
        }
        this.uiHandler.removeCallbacks(this.stateTick);
        this.uiHandler.postDelayed(this.stateTick, 60000L);
    }

    private int nowMinutes() {
        LocalTime t = LocalTime.now();
        return t.getHour() * 60 + t.getMinute();
    }

    private int startMinutes(Course c) {
        return c.startSection >= 1 ? ClassTimeTable.startMinutes(c.startSection) : 24 * 60;
    }

    private int endMinutes(Course c) {
        if (c.startSection < 1) return 24 * 60;
        int end = c.endSection >= c.startSection ? c.endSection : c.startSection;
        return ClassTimeTable.endMinutes(end);
    }

    private boolean nowWithinDay(List<Course> list, int now) {
        if (list.isEmpty()) return false;
        int first = Integer.MAX_VALUE, last = -1;
        for (Course c : list) {
            first = Math.min(first, startMinutes(c));
            last = Math.max(last, endMinutes(c));
        }
        return now >= first && now <= last;
    }

    /** 时间轴单节课程：左时间列 + 短轨道线 + 课程信息；只有显示今天是才区分进行中与已结束。 */
    private View timelineRow(Course course, int nowMin, boolean isToday) {
        int start = startMinutes(course);
        int end = endMinutes(course);
        int state = !isToday ? 0 : (nowMin < start ? 0 : (nowMin <= end ? 1 : 2));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        int padV = dp(14f);
        row.setPadding(dp(14f), padV, dp(16f), padV);

        FrameLayout accentWrap = new FrameLayout(this);
        if (state == 1) {
            View bar = new View(this);
            GradientDrawable barBg = new GradientDrawable();
            barBg.setCornerRadius(dp(2f));
            barBg.setColor(this.liveBlue);
            bar.setBackground(barBg);
            FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(dp(3f), -1);
            barLp.topMargin = dp(2f);
            barLp.bottomMargin = dp(2f);
            accentWrap.addView(bar, barLp);
        }
        row.addView(accentWrap, new LinearLayout.LayoutParams(dp(3f), -1));

        LinearLayout timeCol = new LinearLayout(this);
        timeCol.setOrientation(LinearLayout.VERTICAL);
        timeCol.setGravity(Gravity.TOP | Gravity.RIGHT);
        // 开始时间大而粗放在顶部，结束时间小而轻沉到底部，两者右边缘对齐、共享同一列
        int timeColor = state == 1 ? this.liveBlue : this.ink;
        TextView st = txt(ClassTimeTable.clockOf(course.startSection), 16, timeColor, true);
        st.setGravity(Gravity.RIGHT);
        st.setIncludeFontPadding(false);
        TextView et = txt(ClassTimeTable.clockEndOf(course.endSection), 11, this.muted, false);
        et.setGravity(Gravity.RIGHT);
        et.setIncludeFontPadding(false);
        timeCol.setGravity(Gravity.TOP | Gravity.RIGHT);
        timeCol.addView(st, new LinearLayout.LayoutParams(-1, -2));
        View gap = new View(this);
        timeCol.addView(gap, new LinearLayout.LayoutParams(-1, 0, 1f));
        timeCol.addView(et, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(dp(56f), -1);
        tcLp.leftMargin = dp(11f);
        row.addView(timeCol, tcLp);

        LinearLayout rail = new LinearLayout(this);
        rail.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        View line = new View(this);
        GradientDrawable lineBg = new GradientDrawable();
        lineBg.setCornerRadius(dp(2f));
        lineBg.setColor(state == 1 ? UiPolish.withAlpha(this.liveBlue, 0x99)
                : UiPolish.withAlpha(this.ink, this.dark ? 0x2A : 0x1C));
        line.setBackground(lineBg);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(dp(3f), dp(30f));
        lineLp.topMargin = dp(4f);
        rail.addView(line, lineLp);
        LinearLayout.LayoutParams railLp = new LinearLayout.LayoutParams(dp(18f), -2);
        railLp.leftMargin = dp(8f);
        railLp.rightMargin = dp(10f);
        row.addView(rail, railLp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = txt(course.name, 15, this.ink, true);
        nameRow.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
        if (state == 1) {
            TextView chip = txt("正在上课", 10, this.onGreen, true);
            chip.setPadding(dp(8f), dp(3f), dp(8f), dp(3f));
            GradientDrawable chipBg = new GradientDrawable();
            chipBg.setColor(this.liveBlue);
            chipBg.setCornerRadius(dp(9f));
            chip.setBackground(chipBg);
            nameRow.addView(chip);
        }
        body.addView(nameRow);

        StringBuilder meta = new StringBuilder();
        if (course.startSection >= 1) {
            meta.append("第").append(course.startSection).append("–").append(course.endSection).append("节");
        }
        if (!course.roomShort().isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(course.roomShort());
        }
        if (meta.length() > 0) body.addView(txt(meta.toString(), 12, this.muted, false), top(6));
        if (course.teacher != null && !course.teacher.isEmpty()) {
            body.addView(txt(course.teacher, 11, this.faint, false), top(4));
        }
        if (state == 1) {
            final float fraction = Math.max(0f, Math.min(1f, (nowMin - start) / (float) Math.max(1, end - start)));
            FrameLayout track = new FrameLayout(this);
            GradientDrawable tb = new GradientDrawable();
            tb.setColor(UiPolish.withAlpha(this.liveBlue, this.dark ? 0x40 : 0x2A));
            tb.setCornerRadius(dp(2f));
            track.setBackground(tb);
            View fill = new View(this);
            GradientDrawable fb = new GradientDrawable();
            fb.setColor(this.liveBlue);
            fb.setCornerRadius(dp(2f));
            fill.setBackground(fb);
            track.addView(fill, new FrameLayout.LayoutParams(0, -1));
            track.post(new Runnable() {
                @Override public void run() {
                    ViewGroup.LayoutParams fp = fill.getLayoutParams();
                    fp.width = Math.max(dp(2f), (int) (track.getWidth() * fraction));
                    fill.setLayoutParams(fp);
                }
            });
            LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(-1, dp(4f));
            trackLp.topMargin = dp(10f);
            body.addView(track, trackLp);
        }
        row.addView(body, new LinearLayout.LayoutParams(0, -2, 1f));

        if (state == 2) row.setAlpha(0.5f);
        UiPolish.pressable(row);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { showCourseDetail(course); }
        });
        return row;
    }

    /** 课程详情：沿用原有信息字段，仅做展示。 */
    private void showCourseDetail(Course course) {
        StringBuilder sb = new StringBuilder();
        if (course.startSection >= 1) {
            sb.append(course.sections());
            sb.append("\n").append(ClassTimeTable.formatRange(course.startSection,
                    Math.max(course.startSection, course.endSection)));
        }
        if (!course.roomShort().isEmpty()) {
            sb.append("\n地点：").append(course.roomShort());
        }
        if (course.teacher != null && !course.teacher.isEmpty()) {
            sb.append("\n教师：").append(course.teacher);
        }
        AlertDialog dlg = new AlertDialog.Builder(this).setTitle(course.name)
                .setMessage(sb.toString().trim())
                .setPositiveButton("关闭", null)
                .create();
        dlg.show();
    }

    private View separatorRow() {
        LinearLayout wrap = new LinearLayout(this);
        View line = new View(this);
        line.setBackgroundColor(UiPolish.withAlpha(this.ink, this.dark ? 0x14 : 0x0E));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1f));
        lp.leftMargin = dp(14f);
        lp.rightMargin = dp(16f);
        wrap.setPadding(0, dp(5f), 0, dp(5f));
        wrap.addView(line, lp);
        return wrap;
    }

    private View courseCard(Course course) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18f), dp(16f), dp(18f), dp(16f));
        UiPolish.glass(card, this.cardBg, this.line, 20f);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(16);
        TextView name = txt(course.name, 15, this.ink, true);
        topRow.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView timePill = txt(course.sections(), 11, this.green, true);
        timePill.setPadding(dp(10f), dp(4f), dp(10f), dp(4f));
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(UiPolish.withAlpha(this.accentA, this.dark ? 0x2A : 0x1C));
        pillBg.setCornerRadius(dp(12f));
        timePill.setBackground(pillBg);
        topRow.addView(timePill);
        card.addView(topRow);

        card.addView(txt(course.timeRange(), 12, this.muted, false), top(8));
        StringBuilder meta = new StringBuilder();
        if (course.teacher != null && !course.teacher.isEmpty()) meta.append(course.teacher);
        if (!course.roomShort().isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(course.roomShort());
        }
        if (meta.length() > 0) card.addView(txt(meta.toString(), 11, this.faint, false), top(6));
        UiPolish.pressable(card);
        return card;
    }

    /** 静默续期（复用 CAS 会话），成功回调 onSuccess，失败回调 onFailure。 */
    private void trySilentReauth(final Runnable onSuccess, final Runnable onFailure) {
        long now = System.currentTimeMillis();
        if (this.silentAuthRunning || now - this.lastSilentAuthAt < 25000L) {
            if (onFailure != null) onFailure.run();
            return;
        }
        this.silentAuthRunning = true;
        this.lastSilentAuthAt = now;
        setStatus("登录状态已失效，正在自动续期…");
        new SilentAuth(this, this.silentHost, new SilentAuth.Callback() {
            @Override public void onResult(boolean ok) {
                silentAuthRunning = false;
                if (ok && ready()) {
                    refreshCredentialLine();
                    setStatus("登录状态已自动续期");
                    if (onSuccess != null) onSuccess.run();
                    return;
                }
                if (onFailure != null) onFailure.run();
            }
        }).start(9000L);
    }

    private void showReauthPrompt(boolean offerReauth) {
        setStatus("服务器拒绝当前凭证，需重新授权");
        if (this.courseList != null) {
            this.courseList.removeAllViews();
            LinearLayout panel = panel();
            panel.addView(txt("凭证已失效", 14, this.ink, true));
            panel.addView(txt("自动续期没有成功，请重新完成官方授权后刷新。", 12, this.faint, false), top(6));
            this.courseList.addView(panel);
        }
        if (offerReauth) {
            new AlertDialog.Builder(this)
                    .setTitle("需要重新授权")
                    .setMessage("当前登录凭证已失效或无权限。现在打开智慧思政官方登录页面重新授权？")
                    .setNegativeButton("稍后", (DialogInterface.OnClickListener) null)
                    .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startOAuth(true);
                        }
                    })
                    .show();
        }
    }

    private void handleApiError(Exception exception, boolean offerReauth) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        int httpStatus = -1;
        if (exception instanceof SchoolApi.ApiException) {
            httpStatus = ((SchoolApi.ApiException) exception).httpStatus;
        }
        if (httpStatus == 401 || httpStatus == 403) {
            AuthManager.markUnauthorized(this);
            setStatus("服务器拒绝当前凭证，正在尝试自动续期…");
            final boolean askable = offerReauth;
            trySilentReauth(new Runnable() {
                @Override public void run() {
                    lastData = null;
                    refresh(true);
                }
            }, new Runnable() {
                @Override public void run() {
                    showReauthPrompt(askable);
                }
            });
            return;
        }

        boolean transientError = isTransient(exception);
        List<Course> cached = loadCache(this.viewDate);
        if (cached != null) {
            render(cached);
            setStatus((transientError ? "网络超时，显示 " : "刷新失败，显示 ") + cacheAge(this.viewDate));
            return;
        }
        setStatus((transientError ? "网络超时：" : "请求失败：") + message);
        if (this.courseList != null) {
            this.courseList.removeAllViews();
            LinearLayout panel = panel();
            panel.addView(txt(transientError ? "网络超时" : "加载失败", 14, this.ink, true));
            panel.addView(txt(message, 12, this.faint, false), top(6));
            Button retry = ghostBtn("重试");
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refresh(true);
                }
            });
            panel.addView(retry, top(12));
            this.courseList.addView(panel);
        }
    }

    private static boolean isTransient(Exception exception) {
        if (exception instanceof java.net.SocketTimeoutException
                || exception instanceof java.net.UnknownHostException
                || exception instanceof java.net.ConnectException) {
            return true;
        }
        if (exception instanceof java.io.IOException) {
            String name = exception.getClass().getSimpleName();
            return name.contains("Timeout") || name.contains("Connect") || name.contains("Network");
        }
        return false;
    }

    // ---------------------------------------------------------------- 小组件

    private void setStatus(String text) {
        if (this.status != null) {
            this.status.setText(text);
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private TextView eyebrow(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(9f);
        view.setTextColor(this.faint);
        view.setLetterSpacing(0.18f);
        view.setAllCaps(true);
        return view;
    }

    private View hairline() {
        View view = new View(this);
        view.setBackgroundColor(this.line);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1f)));
        return view;
    }

    private View sectionHead(String tag, String title, String note) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(80);
        row.addView(txt(title, 16, this.ink, true));
        TextView noteView = txt(note, 11, this.faint, false);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(0, -2, 1f);
        noteParams.leftMargin = dp(10f);
        noteView.setGravity(8388613);
        row.addView(noteView, noteParams);
        layout.addView(row, top(6));
        return layout;
    }

    private LinearLayout panel() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16f), dp(16f), dp(16f), dp(16f));
        UiPolish.glass(layout, this.cardBg, this.line, 24f);
        return layout;
    }

    private TextView txt(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button primaryBtn(String text) {
        Button button = baseBtn(text);
        button.setTextColor(this.onGreen);
        button.setTextSize(15f);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setMinHeight(dp(52f));
        button.setMinimumHeight(dp(52f));
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{ this.accentA, this.accentB });
        drawable.setCornerRadius(dp(16f));
        button.setBackground(drawable);
        UiPolish.pressable(button);
        return button;
    }

    /** 即存式 icon 胶囊：bgAlt 圆角方块 + 居中单色矢量图标（不用 Button，避免默认内边距把图标挤歪）。 */
    private FrameLayout iconChip(String iconPath) {
        FrameLayout chip = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(this.bgAlt);
        bg.setCornerRadius(dp(20f));
        chip.setBackground(new RippleDrawable(ColorStateList.valueOf(UiPolish.withAlpha(this.ink, 0x1A)), bg, null));
        SvgIcon.IconView icon = SvgIcon.view(this, iconPath, this.muted, 22);
        chip.addView(icon, new FrameLayout.LayoutParams(dp(22f), dp(22f), Gravity.CENTER));
        chip.setClickable(true);
        UiPolish.pressable(chip);
        return chip;
    }

    private Button ghostBtn(String text) {
        Button button = baseBtn(text);
        button.setTextColor(this.ink);
        button.setTextSize(12f);
        button.setMinHeight(dp(44f));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(this.bgAlt);
        drawable.setCornerRadius(dp(14f));
        button.setBackground(drawable);
        UiPolish.pressable(button);
        return button;
    }

    private Button baseBtn(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(13f);
        button.setMinHeight(dp(46f));
        button.setPadding(dp(16f), 0, dp(16f), 0);
        button.setStateListAnimator(null);
        return button;
    }

    private Button tabBtn(String text) {
        Button button = baseBtn(text);
        button.setTextSize(13f);
        button.setMinHeight(dp(44f));
        button.setTextColor(this.muted);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(this.bgAlt);
        drawable.setCornerRadius(dp(22f));
        button.setBackground(drawable);
        UiPolish.pressable(button);
        return button;
    }

    private Button arrowBtn(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18f);
        button.setAllCaps(false);
        button.setTextColor(this.ink);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(dp(40f));
        button.setMinimumWidth(dp(40f));
        button.setMinHeight(dp(40f));
        button.setMinimumHeight(dp(40f));
        button.setStateListAnimator(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(this.bgAlt);
        drawable.setCornerRadius(dp(20f));
        button.setBackground(drawable);
        return button;
    }

    private Button roundBtn(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15f);
        button.setAllCaps(false);
        button.setTextColor(this.ink);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(dp(38f));
        button.setMinimumWidth(dp(38f));
        button.setMinHeight(dp(38f));
        button.setMinimumHeight(dp(38f));
        button.setStateListAnimator(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(this.bgAlt);
        drawable.setCornerRadius(dp(20f));
        button.setBackground(drawable);
        return button;
    }

    private LinearLayout.LayoutParams top(int dp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(dp);
        return params;
    }

    private int dp(float value) {
        return (int) ((value * getResources().getDisplayMetrics().density) + 0.5f);
    }

    @Override
    protected void onDestroy() {
        stopLocationRequest();
        super.onDestroy();
        this.io.shutdownNow();
    }
}
