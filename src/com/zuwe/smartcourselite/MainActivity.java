package com.zuwe.smartcourselite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.zuwe.hygiene.HygieneHistoryActivity;
import com.zuwe.smartcourselite.AuthManager;
import com.zuwe.smartcourselite.ReminderPrefs;
import com.zuwe.smartcourselite.SchoolApi;
import com.zuwe.smartcourselite.SignInApi;
import com.zuwe.smartcourselite.SilentAuth;
import com.zuwe.smartcourselite.SvgIcon;
import com.zuwe.smartcourselite.TextFx;
import com.zuwe.smartcourselite.TodoStore;
import com.zuwe.smartcourselite.WheelPicker;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class MainActivity extends Activity {
    private static final String CACHE_PREFIX = "cache_";
    private static final String CACHE_TIME_PREFIX = "cache_ts_";
    private static final String KEY_AUTO_DAY = "auto_refresh_day";
    private static final String KEY_DARK = "theme_dark";
    private static final String KEY_SEM_ANCHOR = "sem_anchor";
    private static final int PICK_CAPTURE_ZIP = 2081;
    private static final String PREFS = "smartcourse_prefs";
    private static final int REQUEST_LOCATION = 2083;
    private static final int REQUEST_OAUTH = 2082;
    private static final int W_SEMIBOLD = 2;
    private int accentA;
    private int accentB;
    private LinearLayout advancedBody;
    private boolean advancedExpanded;
    private TextView advancedToggle;
    private int bg;
    private int bgAlt;
    private int blue;
    private FrameLayout bottomNav;
    private int cardBg;
    private LinearLayout courseList;
    private ScrollView courseScroll;
    private View credentialDot;
    private TextView credentialState;
    private TextView dateSub;
    private TextView dateTitle;
    private LinearLayout dayStrip;
    private FrameLayout detailOverlay;
    private int faint;
    private int green;
    private int greenDark;
    private LinearLayout headerBlock;
    private View homeTitle;
    private boolean imeOpen;
    private LinearLayout imeSubmitBar;
    private Button imeSubmitButton;
    private int ink;
    private List inlineCustoms;
    private List inlineTodos;
    private List<Course> lastData;
    private long lastNotifyAskAt;
    private long lastSilentAuthAt;
    private int line;
    private int liveBlue;
    private volatile boolean loading;
    private LocationManager locationManager;
    private Runnable locationTimeout;
    private int muted;
    private FrameLayout navCapsule;
    private LinearLayout navCourse;
    private SvgIcon.IconView navCourseIcon;
    private TextView navCourseLabel;
    private View navPill;
    private boolean navPillPending;
    private LinearLayout navSignIn;
    private SvgIcon.IconView navSignInIcon;
    private TextView navSignInLabel;
    private int onGreen;
    private LocationListener pendingLocationListener;
    private TodoStore.Item pendingTodoReveal;
    private View pendingTodoRow;
    private TextView pickerCount;
    private int red;
    private Button refreshBtn;
    private boolean revealBellsOnRender;
    private View ringingBar;
    private LinearLayout screenRoot;
    private TextView sectionNote;
    private View selectedDayChip;
    private EditText signInCodeInput;
    private int signInDataState;
    private JSONArray signInHistory;
    private SvgIcon.IconView signInHistoryChevron;
    private boolean signInHistoryExpanded;
    private LinearLayout signInHistoryList;
    private LinearLayout signInHistoryPanel;
    private List<View> signInHistoryRows;
    private View signInHistoryToggle;
    private TextView signInHistoryToggleLabel;
    private int signInHistoryTotal;
    private EditText signInLatitudeInput;
    private boolean signInLoading;
    private EditText signInLongitudeInput;
    private ScrollView signInScroll;
    private JSONObject signInStats;
    private TextView signInStatus;
    private Button signInSubmitButton;
    private boolean signInSubmitting;
    private View signInTitle;
    private boolean silentAuthRunning;
    private FrameLayout silentHost;
    private Runnable skeletonStop;
    private TextView statAbsent;
    private TextView statLate;
    private TextView statRight;
    private TextView statTotal;
    private Runnable stateTick;
    private TextView status;
    private int success;
    private View themeBtn;
    private View todoEmpty;
    private TextView todoMeta;
    private TextView todoTitle;
    private FrameLayout todoTrack;
    private View todoTrackFill;
    private boolean updatePrompted;
    private int warning;
    private boolean weekFetching;
    private TextView weekLabel;
    private View weekOverviewBtn;
    private static final DateTimeFormatter FMT_TITLE = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA);
    private static final int W_REGULAR = 0;
    private static final Typeface TF_MEDIUM = Typeface.create("sans-serif-medium", W_REGULAR);
    private static final int W_MEDIUM = 1;
    private static final Typeface TF_SEMIBOLD = Typeface.create("sans-serif", W_MEDIUM);
    private static final String[] WEEKDAY_CN = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
    private static final String[] WEEKDAY_SHORT = {"一", "二", "三", "四", "五", "六", "日"};
    private boolean quietRender = false;
    private boolean pendingDayReveal = false;
    private final LinkedHashMap<String, View> reminderBells = new LinkedHashMap<>();
    private final List<String> pickerKeys = new ArrayList();
    private final List<CheckBox> pickerBoxes = new ArrayList();
    private final List<String> pendingRevealKeys = new ArrayList();
    private final Set<String> revealExclude = new HashSet();
    private boolean todoToggling = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private float touchDownX = 0.0f;
    private boolean pageDragging = false;
    private boolean pullDragging = false;
    private float touchDownY = 0.0f;
    private float lastDragX = 0.0f;
    private long lastDragT = 0;
    private float dragVx = 0.0f;
    private int navPillX = -1;
    private int activePage = W_REGULAR;
    private final Handler locationHandler = new Handler(Looper.getMainLooper());
    private String pendingSignInCode = "";
    private final ExecutorService io = Executors.newFixedThreadPool(W_SEMIBOLD);
    private boolean dark = true;
    private String auth = "";
    private String cookie = "";
    private LocalDate viewDate = LocalDate.now();
    private LocalDate openedDate = LocalDate.now();

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(bundle);
        requestWindowFeature(W_MEDIUM);
        this.openedDate = LocalDate.now();
        this.dark = prefs().getBoolean(KEY_DARK, true);
        applyPalette();
        build();
        loadSaved();
        refreshWeekCalendar(false);
        UpdateManager.cleanupAfterUpdate(this);
        UpdateManager.checkOnLaunch(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ensureReminderPermissions() {
        NotificationManager notificationManager;
        AlarmManager alarmManager;
        try {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 9001);
            }
        } catch (Exception unused) {
        }
        try {
            if (Build.VERSION.SDK_INT >= 31 && (alarmManager = (AlarmManager) getSystemService("alarm")) != null && !alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM", Uri.parse("package:" + getPackageName())));
            }
        } catch (Exception unused2) {
        }
        try {
            if (Build.VERSION.SDK_INT < 34 || (notificationManager = (NotificationManager) getSystemService("notification")) == null || notificationManager.canUseFullScreenIntent()) {
                return;
            }
            startActivity(new Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT", Uri.parse("package:" + getPackageName())));
        } catch (Exception unused3) {
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }
        try {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0) {
                return;
            }
            getWindow().getDecorView().postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        MainActivity.this.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 9001);
                    } catch (Exception unused) {
                    }
                }
            }, 1200L);
        } catch (Exception unused) {
        }
    }

    public SharedPreferences prefs() {
        return getSharedPreferences(PREFS, W_REGULAR);
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
            this.greenDark = Color.parseColor("#B5C3AC");
            this.blue = Color.parseColor("#9DB8CC");
            this.success = Color.parseColor("#8BA88A");
            this.warning = Color.parseColor("#D0A96A");
            this.red = Color.parseColor("#CD8B84");
            this.accentA = Color.parseColor("#95A48C");
            this.accentB = Color.parseColor("#95A48C");
            this.liveBlue = Color.parseColor("#9DB8CC");
            this.onGreen = Color.parseColor("#1A1C17");
            applyWindowBackground();
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
        this.greenDark = Color.parseColor("#515E4D");
        this.blue = Color.parseColor("#91AEC7");
        this.success = Color.parseColor("#71876D");
        this.warning = Color.parseColor("#C59A58");
        this.red = Color.parseColor("#BA716B");
        this.accentA = Color.parseColor("#6F7C69");
        this.accentB = Color.parseColor("#6F7C69");
        this.liveBlue = Color.parseColor("#91AEC7");
        this.onGreen = Color.parseColor("#FFFFFF");
        applyWindowBackground();
    }

    private void applyWindowBackground() {
        try {
            getWindow().setBackgroundDrawable(new ColorDrawable(this.bg));
        } catch (Exception unused) {
        }
    }

    public void toggleTheme() {
        this.dark = !this.dark;
        prefs().edit().putBoolean(KEY_DARK, this.dark).apply();
        applyPalette();
        build();
        List<Course> list = this.lastData;
        if (list != null) {
            render(list);
        } else {
            List<Course> loadCache = loadCache(this.viewDate);
            if (loadCache != null) {
                render(loadCache);
            }
        }
        refreshCredentialLine();
        updateDateHeader();
        setStatus(statusText());
    }

    private String cacheKey(LocalDate localDate) {
        return CACHE_PREFIX + localDate.toString();
    }

    public void saveCache(LocalDate localDate, List<Course> list) {
        JwPreserve.saveAndKeep(this, localDate, list);
    }

    void writeCache(LocalDate localDate, List<Course> list) {
        JSONArray jSONArray = new JSONArray();
        if (list != null) {
            Iterator<Course> it = list.iterator();
            while (it.hasNext()) {
                jSONArray.put(courseToJson(it.next()));
            }
        }
        prefs().edit().putString(cacheKey(localDate), jSONArray.toString()).putLong(CACHE_TIME_PREFIX + localDate.toString(), System.currentTimeMillis()).apply();
    }

    private JSONObject courseToJson(Course course) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("courseName", course.name == null ? "" : course.name);
            jSONObject.put("teacherName", course.teacher == null ? "" : course.teacher);
            jSONObject.put("classRoom", course.room == null ? "" : course.room);
            jSONObject.put("date", course.date != null ? course.date : "");
            jSONObject.put("startSection", course.startSection);
            jSONObject.put("endSection", course.endSection);
            jSONObject.put("week", course.week);
            jSONObject.put("weekDay", course.weekDay);
        } catch (Exception unused) {
        }
        return jSONObject;
    }

    private List<Course> loadCache(LocalDate localDate) {
        String string = prefs().getString(cacheKey(localDate), null);
        if (string == null) {
            return null;
        }
        try {
            JSONArray jSONArray = new JSONArray(string);
            ArrayList arrayList = new ArrayList();
            for (int i = W_REGULAR; i < jSONArray.length(); i += W_MEDIUM) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(new Course(optJSONObject));
                }
            }
            return arrayList;
        } catch (Exception unused) {
            return null;
        }
    }

    private String cacheAge(LocalDate localDate) {
        long j = prefs().getLong(CACHE_TIME_PREFIX + localDate.toString(), 0L);
        if (j <= 0) {
            return prefs().getString(KEY_AUTO_DAY, "").equals(localDate.toString()) ? "今日已同步" : "本机缓存";
        }
        return "缓存于 " + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date(j));
    }

    private String statusText() {
        String format = this.viewDate.format(FMT_TITLE);
        if (this.loading) {
            return "正在刷新 " + format + " …";
        }
        if (this.lastData == null) {
            return format;
        }
        return format + " · " + cacheAge(this.viewDate);
    }

    private void build() {
        getWindow().setStatusBarColor(this.bg);
        getWindow().setNavigationBarColor(this.bg);
        getWindow().setSoftInputMode(16);
        getWindow().getDecorView().setSystemUiVisibility(this.dark ? W_REGULAR : 8208);
        LinearLayout linearLayout = new LinearLayout(this);
        this.screenRoot = linearLayout;
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setBackgroundColor(this.bg);
        ScrollView scrollView = new ScrollView(this);
        this.courseScroll = scrollView;
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(this.bg);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(W_SEMIBOLD);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_MEDIUM);
        linearLayout2.setPadding(dp(24.0f), dp(20.0f), dp(24.0f), dp(116.0f));
        linearLayout2.setBackgroundColor(this.bg);
        scrollView.addView(linearLayout2);
        getWindow().setBackgroundDrawable(new ColorDrawable(this.bg));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_REGULAR);
        linearLayout3.setGravity(16);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(W_MEDIUM);
        View title = TextFx.title(this, "智慧思政课表", 30.0f, this.ink, TF_SEMIBOLD, 62L, 660L);
        this.homeTitle = title;
        linearLayout4.addView(title);
        playTitleFx(title, 180L);
        linearLayout4.addView(label("课表 · 签到 · 卫生检查", 14.0f, this.muted, W_REGULAR), top(5));
        linearLayout3.addView(linearLayout4, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        FrameLayout circleChip = circleChip(SvgIcon.BROOM, "卫生检查");
        circleChip.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int[] iArr = new int[MainActivity.W_SEMIBOLD];
                view.getLocationInWindow(iArr);
                Intent intent = new Intent(MainActivity.this, (Class<?>) HygieneHistoryActivity.class);
                intent.putExtra("reveal_x", iArr[MainActivity.W_REGULAR] + (view.getWidth() / MainActivity.W_SEMIBOLD));
                intent.putExtra("reveal_y", iArr[MainActivity.W_MEDIUM] + (view.getHeight() / MainActivity.W_SEMIBOLD));
                PageMotion.captureBackdrop(MainActivity.this);
                MainActivity.this.startActivity(intent);
                MainActivity.this.overridePendingTransition(MainActivity.W_REGULAR, MainActivity.W_REGULAR);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(48.0f), dp(48.0f));
        layoutParams.rightMargin = dp(10.0f);
        linearLayout3.addView(circleChip, layoutParams);
        FrameLayout circleChip2 = circleChip(this.dark ? SvgIcon.SUN : SvgIcon.MOON, "切换浅色 / 深色");
        this.themeBtn = circleChip2;
        circleChip2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.toggleTheme();
            }
        });
        linearLayout3.addView(this.themeBtn, new LinearLayout.LayoutParams(dp(48.0f), dp(48.0f)));
        linearLayout2.addView(linearLayout3);
        this.headerBlock = linearLayout3;
        linearLayout2.addView(datePanel(), top(20));
        TextView label = label("今日课程", 21.0f, this.ink, W_SEMIBOLD);
        this.sectionNote = label;
        linearLayout2.addView(label, top(28));
        LinearLayout linearLayout5 = new LinearLayout(this);
        this.courseList = linearLayout5;
        linearLayout5.setOrientation(W_MEDIUM);
        linearLayout2.addView(this.courseList, top(12));
        linearLayout2.addView(todoSectionHead(), top(26));
        linearLayout2.addView(sectionHead("", "账户凭证", "授权返回后自动加密保存到本机"), top(28));
        linearLayout2.addView(authPanel(), top(12));
        linearLayout2.addView(AboutFooter.build(this, this.ink, this.muted, this.faint, this.line, this.cardBg, this.bgAlt, this.green), top(16));
        ScrollView scrollView2 = new ScrollView(this);
        this.signInScroll = scrollView2;
        scrollView2.setFillViewport(true);
        this.signInScroll.setBackgroundColor(this.bg);
        this.signInScroll.setVerticalScrollBarEnabled(false);
        this.signInScroll.setHorizontalScrollBarEnabled(false);
        this.signInScroll.setOverScrollMode(W_SEMIBOLD);
        this.signInScroll.addView(buildSignInPage());
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(this.courseScroll, new FrameLayout.LayoutParams(-1, -1));
        frameLayout.addView(this.signInScroll, new FrameLayout.LayoutParams(-1, -1));
        this.bottomNav = buildBottomNavigation();
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -2);
        layoutParams2.gravity = 80;
        frameLayout.addView(this.bottomNav, layoutParams2);
        linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(-1, W_REGULAR, 1.0f));
        LinearLayout linearLayout6 = new LinearLayout(this);
        this.imeSubmitBar = linearLayout6;
        linearLayout6.setOrientation(W_MEDIUM);
        this.imeSubmitBar.setBackgroundColor(this.cardBg);
        this.imeSubmitBar.setPadding(dp(14.0f), dp(8.0f), dp(14.0f), dp(8.0f));
        this.imeSubmitBar.setVisibility(8);
        Button primaryBtn = primaryBtn("提交签到");
        this.imeSubmitButton = primaryBtn;
        primaryBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.beginSignIn();
            }
        });
        this.imeSubmitBar.addView(this.imeSubmitButton, new LinearLayout.LayoutParams(-1, dp(60.0f)));
        linearLayout.addView(this.imeSubmitBar, new LinearLayout.LayoutParams(-1, -2));
        FrameLayout frameLayout2 = new FrameLayout(this);
        this.silentHost = frameLayout2;
        linearLayout.addView(frameLayout2, new LinearLayout.LayoutParams(dp(2.0f), dp(2.0f)));
        this.courseScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { // from class: com.zuwe.smartcourselite.MainActivity.5
            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.updateTopBar(mainActivity.courseScroll.getScrollY());
            }
        });
        this.signInScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { // from class: com.zuwe.smartcourselite.MainActivity.6
            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
            }
        });
        setContentView(linearLayout);
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.zuwe.smartcourselite.MainActivity.7
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                MainActivity.this.syncImeState();
            }
        });
        switchPage(this.activePage, false);
        setSignInBusy(this.signInSubmitting);
        if (this.signInStats != null || this.signInHistory != null) {
            renderSignInData();
        }
        updateDateHeader();
    }

    private LinearLayout buildSignInPage() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(24.0f), dp(20.0f), dp(24.0f), dp(116.0f));
        linearLayout.setBackgroundColor(this.bg);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_MEDIUM);
        View title = TextFx.title(this, "课堂签到", 29.0f, this.ink, TF_SEMIBOLD, 62L, 660L);
        this.signInTitle = title;
        linearLayout3.addView(title);
        playTitleFx(title, 180L);
        linearLayout3.addView(label("统计与记录 · 近六个月", 14.0f, this.muted, W_REGULAR), top(5));
        linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        Button ghostBtn = ghostBtn("刷新数据");
        ghostBtn.setMinHeight(dp(52.0f));
        ghostBtn.setMinimumHeight(dp(52.0f));
        ghostBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.refreshSignInData();
            }
        });
        linearLayout2.addView(ghostBtn);
        linearLayout.addView(linearLayout2);
        linearLayout.addView(sectionHead("", "签到统计", "服务端数据"), top(28));
        LinearLayout panel = panel();
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(W_REGULAR);
        linearLayout4.addView(metricCell("正常签到", "--", true), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        linearLayout4.addView(metricCell("缺勤", "--", false), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(W_REGULAR);
        linearLayout5.addView(metricCell("迟到", "--", false), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        linearLayout5.addView(metricCell("应签到", "--", false), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        this.statRight = (TextView) ((LinearLayout) linearLayout4.getChildAt(W_REGULAR)).getChildAt(W_MEDIUM);
        this.statAbsent = (TextView) ((LinearLayout) linearLayout4.getChildAt(W_MEDIUM)).getChildAt(W_MEDIUM);
        this.statLate = (TextView) ((LinearLayout) linearLayout5.getChildAt(W_REGULAR)).getChildAt(W_MEDIUM);
        this.statTotal = (TextView) ((LinearLayout) linearLayout5.getChildAt(W_MEDIUM)).getChildAt(W_MEDIUM);
        panel.addView(linearLayout4);
        panel.addView(linearLayout5, top(20));
        linearLayout.addView(panel, top(12));
        linearLayout.addView(sectionHead("", "手动签到", "输入教师提供的签到码"), top(28));
        LinearLayout panel2 = panel();
        EditText editText = new EditText(this);
        this.signInCodeInput = editText;
        editText.setSingleLine(true);
        this.signInCodeInput.setTextSize(15.0f);
        this.signInCodeInput.setTextColor(this.ink);
        this.signInCodeInput.setHintTextColor(this.faint);
        this.signInCodeInput.setHint("请输入签到码");
        this.signInCodeInput.setPadding(dp(16.0f), W_REGULAR, dp(16.0f), W_REGULAR);
        this.signInCodeInput.setBackground(inputBackground());
        panel2.addView(this.signInCodeInput, new LinearLayout.LayoutParams(-1, dp(64.0f)));
        LinearLayout linearLayout6 = new LinearLayout(this);
        linearLayout6.setOrientation(W_REGULAR);
        linearLayout6.setGravity(16);
        linearLayout6.setPadding(W_REGULAR, dp(14.0f), W_REGULAR, dp(2.0f));
        linearLayout6.setMinimumHeight(dp(44.0f));
        TextView label = label("高级签到 · 推荐位置", 14.0f, this.ink, W_MEDIUM);
        this.advancedToggle = label("展开 ▾", 12.0f, this.muted, W_REGULAR);
        linearLayout6.addView(label, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        linearLayout6.addView(this.advancedToggle);
        linearLayout6.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.9
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.setAdvancedExpanded(!MainActivity.this.advancedExpanded);
            }
        });
        panel2.addView(linearLayout6);
        linearLayout6.setVisibility(ReminderPrefs.hiddenModeUnlocked(this) ? W_REGULAR : 8);
        LinearLayout linearLayout7 = new LinearLayout(this);
        this.advancedBody = linearLayout7;
        linearLayout7.setOrientation(W_MEDIUM);
        this.advancedBody.setVisibility(8);
        this.advancedBody.addView(label("选择地点可一键填入坐标，提交时直接使用该坐标。清空两项则使用设备当前位置。", 12.0f, this.faint, W_REGULAR), top(6));
        LinearLayout linearLayout8 = new LinearLayout(this);
        linearLayout8.setOrientation(W_REGULAR);
        Button ghostBtn2 = ghostBtn("博学楼");
        ghostBtn2.setMinHeight(dp(42.0f));
        ghostBtn2.setMinimumHeight(dp(42.0f));
        ghostBtn2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.10
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.fillRecommendedLocation("30.8361", "120.4422", "博学楼");
            }
        });
        Button ghostBtn3 = ghostBtn("求实楼");
        ghostBtn3.setMinHeight(dp(42.0f));
        ghostBtn3.setMinimumHeight(dp(42.0f));
        ghostBtn3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.11
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.fillRecommendedLocation("30.8359", "120.4404", "求实楼");
            }
        });
        linearLayout8.addView(ghostBtn2, new LinearLayout.LayoutParams(W_REGULAR, dp(42.0f), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(W_REGULAR, dp(42.0f), 1.0f);
        layoutParams.leftMargin = dp(8.0f);
        linearLayout8.addView(ghostBtn3, layoutParams);
        this.advancedBody.addView(linearLayout8, top(9));
        LinearLayout linearLayout9 = new LinearLayout(this);
        linearLayout9.setOrientation(W_REGULAR);
        EditText editText2 = new EditText(this);
        this.signInLatitudeInput = editText2;
        editText2.setSingleLine(true);
        this.signInLatitudeInput.setInputType(12290);
        this.signInLatitudeInput.setTextSize(14.0f);
        this.signInLatitudeInput.setTextColor(this.ink);
        this.signInLatitudeInput.setHintTextColor(this.faint);
        this.signInLatitudeInput.setHint("纬度 Latitude");
        this.signInLatitudeInput.setPadding(dp(12.0f), dp(8.0f), dp(12.0f), dp(8.0f));
        this.signInLatitudeInput.setBackground(inputBackground());
        EditText editText3 = new EditText(this);
        this.signInLongitudeInput = editText3;
        editText3.setSingleLine(true);
        this.signInLongitudeInput.setInputType(12290);
        this.signInLongitudeInput.setTextSize(14.0f);
        this.signInLongitudeInput.setTextColor(this.ink);
        this.signInLongitudeInput.setHintTextColor(this.faint);
        this.signInLongitudeInput.setHint("经度 Longitude");
        this.signInLongitudeInput.setPadding(dp(12.0f), dp(8.0f), dp(12.0f), dp(8.0f));
        this.signInLongitudeInput.setBackground(inputBackground());
        linearLayout9.addView(this.signInLatitudeInput, new LinearLayout.LayoutParams(W_REGULAR, dp(48.0f), 1.0f));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(W_REGULAR, dp(48.0f), 1.0f);
        layoutParams2.leftMargin = dp(8.0f);
        linearLayout9.addView(this.signInLongitudeInput, layoutParams2);
        this.advancedBody.addView(linearLayout9, top(9));
        panel2.addView(this.advancedBody);
        Button primaryBtn = primaryBtn("提交签到");
        this.signInSubmitButton = primaryBtn;
        primaryBtn.setMinHeight(dp(64.0f));
        this.signInSubmitButton.setMinimumHeight(dp(64.0f));
        this.signInSubmitButton.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.12
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.beginSignIn();
            }
        });
        panel2.addView(this.signInSubmitButton, top(14));
        TextView label2 = label("提交时才会请求定位权限；不会使用虚构或固定坐标。", 12.0f, this.faint, W_REGULAR);
        this.signInStatus = label2;
        label2.setLineSpacing(dp(3.0f), 1.0f);
        panel2.addView(this.signInStatus, top(12));
        linearLayout.addView(panel2, top(12));
        linearLayout.addView(sectionHead("", "签到记录", "近六个月 · 个人签到明细"), top(28));
        LinearLayout linearLayout10 = new LinearLayout(this);
        this.signInHistoryList = linearLayout10;
        linearLayout10.setOrientation(W_MEDIUM);
        LinearLayout panel3 = panel();
        this.signInHistoryPanel = panel3;
        panel3.addView(this.signInHistoryList);
        linearLayout.addView(panel3, top(12));
        renderSignInData();
        linearLayout.addView(AboutFooter.build(this, this.ink, this.muted, this.faint, this.line, this.cardBg, this.bgAlt, this.green), top(16));
        return linearLayout;
    }

    private View metricCell(String str, String str2, boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        TextView label = label(str, 14.0f, this.muted, W_REGULAR);
        TextView label2 = label(str2, 30.0f, z ? this.green : this.ink, W_SEMIBOLD);
        linearLayout.addView(label);
        linearLayout.addView(label2, top(6));
        return linearLayout;
    }

    private Drawable inputBackground() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(16.0f));
        gradientDrawable.setStroke(dp(1.0f), this.line);
        return gradientDrawable;
    }

    private FrameLayout buildBottomNavigation() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setPadding(dp(16.0f), W_REGULAR, dp(16.0f), dp(14.0f));
        FrameLayout frameLayout2 = new FrameLayout(this);
        this.navCapsule = frameLayout2;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.cardBg);
        gradientDrawable.setCornerRadius(dp(28.0f));
        gradientDrawable.setStroke(dp(1.0f), this.line);
        frameLayout2.setBackground(gradientDrawable);
        frameLayout2.setElevation(0.0f);
        frameLayout.addView(frameLayout2, new FrameLayout.LayoutParams(-1, dp(64.0f), 80));
        this.navPill = new View(this);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setColor(UiPolish.withAlpha(this.green, this.dark ? 48 : 28));
        gradientDrawable2.setCornerRadius(dp(20.0f));
        this.navPill.setBackground(gradientDrawable2);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp(0.0f), dp(0.0f));
        layoutParams.gravity = 8388627;
        layoutParams.leftMargin = dp(6.0f);
        frameLayout2.addView(this.navPill, layoutParams);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setPadding(dp(6.0f), dp(6.0f), dp(6.0f), dp(6.0f));
        this.navCourse = navItem(SvgIcon.CALENDAR, "课表", W_REGULAR);
        this.navSignIn = navItem(SvgIcon.PARSE, "签到", W_MEDIUM);
        linearLayout.addView(this.navCourse, new LinearLayout.LayoutParams(W_REGULAR, -1, 1.0f));
        linearLayout.addView(this.navSignIn, new LinearLayout.LayoutParams(W_REGULAR, -1, 1.0f));
        frameLayout2.addView(linearLayout, new FrameLayout.LayoutParams(-1, -1));
        frameLayout2.post(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.13
            @Override // java.lang.Runnable
            public void run() {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.selectNav(mainActivity.activePage, false);
            }
        });
        return frameLayout;
    }

    private LinearLayout navItem(String str, String str2, final int i) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setGravity(17);
        SvgIcon.IconView view = SvgIcon.view(this, str, this.muted, 23);
        TextView label = label(str2, 12.0f, this.muted, W_REGULAR);
        linearLayout.addView(view, new LinearLayout.LayoutParams(dp(23.0f), dp(23.0f)));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = dp(3.0f);
        linearLayout.addView(label, layoutParams);
        linearLayout.setTag(new Object[]{view, label, str});
        if (i == 0) {
            this.navCourseIcon = view;
            this.navCourseLabel = label;
        } else {
            this.navSignInIcon = view;
            this.navSignInLabel = label;
        }
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.14
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.switchPage(i, true);
            }
        });
        linearLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.MainActivity.15
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    Haptics.tick(view2);
                    view2.animate().cancel();
                    view2.animate().scaleX(0.94f).scaleY(0.94f).setDuration(90L).setInterpolator(Motion.ENTER).start();
                    return false;
                }
                if (actionMasked != MainActivity.W_MEDIUM && actionMasked != 3) {
                    return false;
                }
                view2.animate().cancel();
                view2.animate().scaleX(1.0f).scaleY(1.0f).setDuration(180L).setInterpolator(Motion.ENTER).start();
                return false;
            }
        });
        return linearLayout;
    }

    public void selectNav(int i, boolean z) {
        SvgIcon.IconView iconView = this.navCourseIcon;
        if (iconView != null) {
            iconView.setIconColor(i == 0 ? this.green : this.muted);
            this.navCourseLabel.setTextColor(i == 0 ? this.green : this.muted);
            this.navCourseLabel.setTypeface(i == 0 ? TF_MEDIUM : Typeface.DEFAULT, W_REGULAR);
        }
        SvgIcon.IconView iconView2 = this.navSignInIcon;
        if (iconView2 != null) {
            iconView2.setIconColor(i == W_MEDIUM ? this.green : this.muted);
            this.navSignInLabel.setTextColor(i == W_MEDIUM ? this.green : this.muted);
            this.navSignInLabel.setTypeface(i == W_MEDIUM ? TF_MEDIUM : Typeface.DEFAULT, W_REGULAR);
        }
        if (this.navPill == null) {
            return;
        }
        LinearLayout linearLayout = i == 0 ? this.navCourse : this.navSignIn;
        if (linearLayout == null) {
            return;
        }
        if (linearLayout.getWidth() <= 0) {
            FrameLayout frameLayout = this.navCapsule;
            if (frameLayout == null || this.navPillPending) {
                return;
            }
            this.navPillPending = true;
            frameLayout.post(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.16
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.navPillPending = false;
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.selectNav(mainActivity.activePage, false);
                }
            });
            return;
        }
        View view = (View) linearLayout.getParent();
        int dp = dp(5.0f);
        int left = linearLayout.getLeft() + view.getLeft() + dp;
        int max = Math.max(dp(40.0f), linearLayout.getWidth() - (dp * W_SEMIBOLD));
        int max2 = Math.max(dp(40.0f), linearLayout.getHeight());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.navPill.getLayoutParams();
        layoutParams.width = max;
        layoutParams.height = max2;
        layoutParams.gravity = 8388627;
        layoutParams.leftMargin = left;
        int i2 = this.navPillX;
        if (i2 < 0) {
            i2 = left;
        }
        this.navPillX = left;
        this.navPill.setLayoutParams(layoutParams);
        this.navPill.animate().cancel();
        this.navPill.setTranslationX(i2 - left);
        if (z) {
            this.navPill.animate().translationX(0.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
            SvgIcon.IconView iconView3 = i == 0 ? this.navCourseIcon : this.navSignInIcon;
            if (iconView3 != null) {
                iconView3.setRotationY(-78.0f);
                iconView3.animate().rotationY(0.0f).setDuration(420L).setStartDelay(20L).setInterpolator(Motion.SPRING).start();
                iconView3.setScaleX(0.78f);
                iconView3.setScaleY(0.78f);
                iconView3.animate().scaleX(1.0f).scaleY(1.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
                return;
            }
            return;
        }
        this.navPill.setTranslationX(0.0f);
    }

    public void updateTopBar(int i) {
        float min = Math.min(1.0f, i / Math.max(W_MEDIUM, dp(70.0f)));
        LinearLayout linearLayout = this.headerBlock;
        if (linearLayout != null) {
            linearLayout.setAlpha(1.0f - (0.92f * min));
            linearLayout.setPivotY(0.0f);
            float f = 1.0f - (min * 0.07f);
            linearLayout.setScaleX(f);
            linearLayout.setScaleY(f);
            this.headerBlock.setTranslationY((-i) * 0.28f);
        }
    }

    public void switchPage(int i, boolean z) {
        int i2 = this.activePage;
        this.activePage = i;
        ScrollView scrollView = i2 == 0 ? this.courseScroll : this.signInScroll;
        ScrollView scrollView2 = i == 0 ? this.courseScroll : this.signInScroll;
        if (scrollView != null && scrollView2 != null && scrollView != scrollView2) {
            animatePageSlide(scrollView, scrollView2, i > i2 ? W_MEDIUM : -1);
        } else {
            ScrollView scrollView3 = this.courseScroll;
            if (scrollView3 != null) {
                scrollView3.setVisibility(i == 0 ? W_REGULAR : 8);
            }
            ScrollView scrollView4 = this.signInScroll;
            if (scrollView4 != null) {
                scrollView4.setVisibility(i == W_MEDIUM ? W_REGULAR : 8);
            }
        }
        selectNav(i, true);
        applyBottomBarState();
        if (i == W_MEDIUM) {
            playTitleFx(this.signInTitle, 60L);
        } else {
            playTitleFx(this.homeTitle, 80L);
        }
        if (i == W_MEDIUM && z) {
            refreshSignInData();
        }
    }

    private void animatePageSlide(final View view, final View view2, int i) {
        float f = getResources().getDisplayMetrics().density;
        float f2 = getResources().getDisplayMetrics().widthPixels;
        view.animate().cancel();
        view2.animate().cancel();
        view2.setVisibility(W_REGULAR);
        view2.setAlpha(0.0f);
        view2.setTranslationX(i * f2 * 0.3f);
        view2.setScaleX(0.94f);
        view2.setScaleY(0.94f);
        view2.setRotationY(0.0f);
        view2.animate().alpha(1.0f).translationX(0.0f).scaleX(1.0f).scaleY(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
        view.animate().alpha(0.0f).translationX((-i) * f2 * 0.18f).scaleX(0.92f).scaleY(0.92f).setDuration(420L).setInterpolator(Motion.STANDARD).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.17
            @Override // java.lang.Runnable
            public void run() {
                view.setVisibility(8);
                view.setTranslationX(0.0f);
                view.setRotationY(0.0f);
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
                view.setAlpha(1.0f);
                view2.setRotationY(0.0f);
                view2.setScaleX(1.0f);
                view2.setScaleY(1.0f);
                view2.setPivotX(0.0f);
            }
        }).start();
    }

    public void refreshSignInData() {
        if (this.signInLoading) {
            return;
        }
        if (!ready()) {
            TextView textView = this.signInStatus;
            if (textView != null) {
                textView.setText("请先在课表页完成官方账号授权。");
                return;
            }
            return;
        }
        this.signInLoading = true;
        this.signInDataState = W_MEDIUM;
        TextView textView2 = this.signInStatus;
        if (textView2 != null) {
            textView2.setText("正在同步签到统计和历史记录…");
        }
        renderSignInData();
        LocalDate now = LocalDate.now();
        this.io.execute(new RunnableC0006AnonymousClass18(this, this.auth, this.cookie, now.minusMonths(6L), now));
    }

    /* renamed from: com.zuwe.smartcourselite.MainActivity$AnonymousClass18, reason: case insensitive filesystem */
    class RunnableC0006AnonymousClass18 implements Runnable {
        final MainActivity this$0;
        final String val$currentAuth;
        final String val$currentCookie;
        final LocalDate val$end;
        final LocalDate val$start;

        RunnableC0006AnonymousClass18(MainActivity mainActivity, String str, String str2, LocalDate localDate, LocalDate localDate2) {
            this.val$currentAuth = str;
            this.val$currentCookie = str2;
            this.val$start = localDate;
            this.val$end = localDate2;
            this.this$0 = mainActivity;
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r1v1 */
        /* JADX WARN: Type inference failed for: r1v2, types: [org.json.JSONArray] */
        /* JADX WARN: Type inference failed for: r1v6 */
        /* JADX WARN: Type inference failed for: r1v7, types: [org.json.JSONArray] */
        @Override // java.lang.Runnable
        public void run() {
            JSONObject stats;
            JSONArray history;
            Exception err;
            try {
                SignInApi signInApi = new SignInApi(this.this$0, this.val$currentAuth, this.val$currentCookie);
                stats = signInApi.getStatistics(this.val$start, this.val$end);
                try {
                    history = signInApi.getHistory(this.val$start, this.val$end);
                    err = null;
                } catch (Exception e) {
                    history = null;
                    err = e;
                }
            } catch (Exception e2) {
                stats = null;
                history = null;
                err = e2;
            }
            final JSONObject fStats = stats;
            final JSONArray fHistory = history;
            final Exception fErr = err;
            this.this$0.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass18.1
                @Override // java.lang.Runnable
                public void run() {
                    RunnableC0006AnonymousClass18.this.this$0.signInLoading = false;
                    if (fErr == null) {
                        RunnableC0006AnonymousClass18.this.this$0.signInDataState = MainActivity.W_SEMIBOLD;
                        RunnableC0006AnonymousClass18.this.this$0.signInStats = fStats;
                        RunnableC0006AnonymousClass18.this.this$0.signInHistory = fHistory;
                        RunnableC0006AnonymousClass18.this.this$0.renderSignInData();
                        if (RunnableC0006AnonymousClass18.this.this$0.signInStatus != null) {
                            RunnableC0006AnonymousClass18.this.this$0.signInStatus.setText("数据已更新 · " + LocalDate.now());
                            return;
                        }
                        return;
                    }
                    RunnableC0006AnonymousClass18.this.this$0.signInDataState = 3;
                    if (RunnableC0006AnonymousClass18.this.this$0.signInStatus != null) {
                        RunnableC0006AnonymousClass18.this.this$0.signInStatus.setText("同步失败：" + RunnableC0006AnonymousClass18.this.this$0.safeMessage(fErr));
                    }
                    Exception exc3 = fErr;
                    if ((exc3 instanceof SignInApi.ApiException) && ((SignInApi.ApiException) exc3).isAuthFailure()) {
                        if (RunnableC0006AnonymousClass18.this.this$0.signInStatus != null) {
                            RunnableC0006AnonymousClass18.this.this$0.signInStatus.setText("登录状态已失效，正在自动续期…");
                        }
                        RunnableC0006AnonymousClass18.this.this$0.trySilentReauth(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass18.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                RunnableC0006AnonymousClass18.this.this$0.refreshSignInData();
                            }
                        }, new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass18.1.2
                            @Override // java.lang.Runnable
                            public void run() {
                                if (RunnableC0006AnonymousClass18.this.this$0.signInStatus != null) {
                                    RunnableC0006AnonymousClass18.this.this$0.signInStatus.setText("登录状态已失效，请重新授权");
                                }
                                RunnableC0006AnonymousClass18.this.this$0.toast("登录状态已失效，请重新授权");
                            }
                        });
                    }
                }
            });
        }
    }

    public void renderSignInData() {
        setStat(this.statRight, statText(this.signInStats, "rightCount"));
        setStat(this.statAbsent, statText(this.signInStats, "absentCount"));
        setStat(this.statLate, statText(this.signInStats, "lateCount"));
        setStat(this.statTotal, statText(this.signInStats, "totalCount"));
        if (this.signInHistoryPanel == null) {
            return;
        }
        if (this.signInHistoryRows == null) {
            this.signInHistoryRows = new ArrayList();
        }
        this.signInHistoryRows.clear();
        this.signInHistoryTotal = W_REGULAR;
        if (this.signInHistory != null) {
            for (int i = W_REGULAR; i < this.signInHistory.length(); i += W_MEDIUM) {
                JSONObject optJSONObject = this.signInHistory.optJSONObject(i);
                if (optJSONObject != null) {
                    this.signInHistoryRows.add(buildHistoryRow(optJSONObject));
                }
            }
        }
        this.signInHistoryTotal = this.signInHistoryRows.size();
        installHistoryCard(this.signInHistoryExpanded, false);
    }

    private View buildHistoryRow(JSONObject jSONObject) {
        String firstValue = firstValue(jSONObject, "courseDate", "signDate", "checkInDate", "date", "createTime", "checkTime");
        String firstValue2 = firstValue(jSONObject, "courseName", "courseTitle", "className", "name");
        String firstValue3 = firstValue(jSONObject, "statusName", "checkInStatus", "status", "result");
        String firstValue4 = firstValue(jSONObject, "location", "address", "place", "room");
        String prettyDate = prettyDate(firstValue);
        if (firstValue2.isEmpty()) {
            firstValue2 = "课堂签到";
        }
        boolean isEmpty = !firstValue3.isEmpty();
        for (int i = W_REGULAR; i < firstValue3.length(); i += W_MEDIUM) {
            char charAt = firstValue3.charAt(i);
            if (charAt < '0' || charAt > '9') {
                isEmpty = false;
                break;
            }
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        linearLayout2.addView(label(firstValue2, 12.0f, this.ink, W_REGULAR));
        if (!prettyDate.isEmpty()) {
            View label = label(prettyDate, 10.0f, this.faint, W_REGULAR);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.leftMargin = dp(10.0f);
            linearLayout2.addView(label, layoutParams);
        }
        if (!firstValue3.isEmpty() && !isEmpty) {
            TextView label2 = label(firstValue3, 9.0f, this.faint, W_REGULAR);
            label2.setPadding(dp(6.0f), dp(2.0f), dp(6.0f), dp(2.0f));
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(this.bgAlt);
            gradientDrawable.setCornerRadius(dp(9.0f));
            label2.setBackground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.leftMargin = dp(9.0f);
            linearLayout2.addView(label2, layoutParams2);
        }
        linearLayout.addView(linearLayout2);
        if (!firstValue4.isEmpty()) {
            linearLayout.addView(label(firstValue4, 10.0f, this.faint, W_REGULAR), top(4));
        }
        linearLayout.setTag("");
        return linearLayout;
    }

    private void replaceHistoryCard(LinearLayout linearLayout) {
        LinearLayout linearLayout2 = this.signInHistoryPanel;
        if (linearLayout2 != null && (linearLayout2.getParent() instanceof LinearLayout)) {
            LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getParent();
            int indexOfChild = linearLayout3.indexOfChild(linearLayout2);
            ViewGroup.LayoutParams layoutParams = linearLayout2.getLayoutParams();
            linearLayout3.removeView(linearLayout2);
            linearLayout3.addView(linearLayout, indexOfChild, layoutParams);
        }
        this.signInHistoryPanel = linearLayout;
    }

    private LinearLayout installHistoryCard(boolean z, boolean z2) {
        int i;
        List list = this.signInHistoryRows;
        if (list == null) {
            list = new ArrayList();
        }
        LinearLayout linearLayout = this.signInHistoryList;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
        }
        int size = list.size();
        int min = z ? size : Math.min(3, size);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_MEDIUM);
        int[] iArr = {-1};
        if (size == 0) {
            TextView label = label("暂无签到记录", 13.0f, this.muted, W_REGULAR);
            TextView label2 = label("服务器当前返回空记录；这不代表没有可签到任务。", 12.0f, this.faint, W_REGULAR);
            label.setAlpha(0.0f);
            label.setTranslationY(dp(8.0f));
            label2.setAlpha(0.0f);
            label2.setTranslationY(dp(8.0f));
            linearLayout2.addView(label);
            linearLayout2.addView(label2, top(6));
            label.animate().alpha(1.0f).translationY(0.0f).setDuration(280L).setInterpolator(Motion.ENTER).start();
            label2.animate().alpha(1.0f).translationY(0.0f).setStartDelay(60L).setDuration(280L).setInterpolator(Motion.ENTER).start();
        } else {
            for (int i2 = W_REGULAR; i2 < min; i2 += W_MEDIUM) {
                View view = (View) list.get(i2);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                if (linearLayout2.getChildCount() > 0) {
                    layoutParams.topMargin = dp(15.0f);
                }
                linearLayout2.addView(view, layoutParams);
                iArr[W_REGULAR] = linearLayout2.getChildCount() - W_MEDIUM;
            }
            if (size > 3) {
                linearLayout2.addView(buildHistoryToggle(size), top(4));
            }
            if (z2 && size > 3) {
                int i3 = 3;
                while (i3 < size) {
                    ((View) list.get(i3)).setAlpha(0.0f);
                    ((View) list.get(i3)).setTranslationY(i3 != 3 ? dp(14.0f) : 0.0f);
                    i3 += W_MEDIUM;
                }
            }
        }
        LinearLayout panel = panel();
        panel.addView(linearLayout2);
        replaceHistoryCard(panel);
        this.signInHistoryList = linearLayout2;
        if (z2 && size > 3 && (i = iArr[W_REGULAR]) >= 0) {
            playHistoryEntrance(panel, linearLayout2, i, size);
        }
        return panel;
    }

    private View buildHistoryGroupHeader(String str) {
        String prettyDate;
        String trim = "";
        if (str == null) {
            trim = "";
        } else {
            try {
                trim = str.trim();
            } catch (Exception unused) {
                prettyDate = prettyDate(str);
            }
        }
        if (trim.length() >= 10 && trim.charAt(4) == '-' && trim.charAt(7) == '-') {
            LocalDate parse = LocalDate.parse(trim.substring(W_REGULAR, 10));
            prettyDate = parse.getMonthValue() + "月" + parse.getDayOfMonth() + "日 · " + WEEKDAY_CN[parse.getDayOfWeek().getValue() - 1];
        } else {
            prettyDate = prettyDate(trim);
        }
        if (prettyDate.isEmpty()) {
            prettyDate = "签到记录";
        }
        TextView label = label(prettyDate, 12.0f, this.faint, W_REGULAR);
        label.setLetterSpacing(0.04f);
        return label;
    }

    private void playHistoryEntrance(final LinearLayout linearLayout, final LinearLayout linearLayout2, final int i, int i2) {
        final List<View> list = this.signInHistoryRows;
        final int i3 = i2 - 3;
        linearLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.MainActivity.18
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                linearLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                int height = linearLayout.getHeight();
                int paddingTop = linearLayout.getPaddingTop() + linearLayout.getPaddingBottom();
                View childAt = linearLayout2.getChildAt(i);
                if (childAt != null) {
                    paddingTop += childAt.getBottom();
                    ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                    if (layoutParams instanceof LinearLayout.LayoutParams) {
                        paddingTop += ((LinearLayout.LayoutParams) layoutParams).bottomMargin;
                    }
                }
                final ViewGroup.LayoutParams layoutParams2 = linearLayout.getLayoutParams();
                LinearLayout linearLayout3 = linearLayout;
                int i4 = MainActivity.W_REGULAR;
                linearLayout3.setClipToOutline(false);
                if (height > paddingTop && paddingTop > 0 && layoutParams2 != null) {
                    layoutParams2.height = paddingTop;
                    linearLayout.setLayoutParams(layoutParams2);
                    ValueAnimator ofInt = ValueAnimator.ofInt(paddingTop, height);
                    ofInt.setDuration(((i3 - MainActivity.W_MEDIUM) * 120) + 460);
                    ofInt.setInterpolator(Motion.STANDARD);
                    final LinearLayout linearLayout4 = linearLayout;
                    ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.18.1
                        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            layoutParams2.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                            linearLayout4.setLayoutParams(layoutParams2);
                        }
                    });
                    final LinearLayout linearLayout5 = linearLayout;
                    ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.MainActivity.18.2
                        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(Animator animator) {
                            layoutParams2.height = -2;
                            linearLayout5.setLayoutParams(layoutParams2);
                            linearLayout5.setClipToOutline(true);
                            linearLayout5.invalidate();
                        }
                    });
                    ofInt.start();
                }
                while (i4 < i3) {
                    ((View) list.get(i4 + 3)).animate().alpha(1.0f).translationY(0.0f).setStartDelay((i4 * 140) + 100).setDuration(i4 == 0 ? 700L : 650L).setInterpolator(Motion.ENTER).start();
                    i4 += MainActivity.W_MEDIUM;
                }
                return true;
            }
        });
    }

    private View buildHistoryToggle(int i) {
        String str;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(17);
        linearLayout.setPadding(W_REGULAR, dp(8.0f), W_REGULAR, dp(6.0f));
        if (this.signInHistoryExpanded) {
            str = "收起签到记录";
        } else {
            str = "展开全部 " + i + " 条记录";
        }
        this.signInHistoryToggleLabel = label(str, 12.0f, this.muted, W_REGULAR);
        SvgIcon.IconView view = SvgIcon.view(this, SvgIcon.CHEV_R, this.muted, 13);
        this.signInHistoryChevron = view;
        view.setRotation(this.signInHistoryExpanded ? 270.0f : 90.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(13.0f), dp(13.0f));
        layoutParams.leftMargin = dp(6.0f);
        linearLayout.addView(this.signInHistoryToggleLabel);
        linearLayout.addView(this.signInHistoryChevron, layoutParams);
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.19
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.toggleHistoryExpanded();
            }
        });
        UiPolish.pressable(linearLayout);
        this.signInHistoryToggle = linearLayout;
        return linearLayout;
    }

    public void toggleHistoryExpanded() {
        if (this.signInHistoryExpanded) {
            collapseHistory();
        } else {
            this.signInHistoryExpanded = true;
            installHistoryCard(true, true);
        }
    }

    private void collapseHistory() {
        LinearLayout linearLayout = this.signInHistoryPanel;
        int height = linearLayout == null ? W_REGULAR : linearLayout.getHeight();
        this.signInHistoryExpanded = false;
        final LinearLayout installHistoryCard = installHistoryCard(false, false);
        final ViewGroup.LayoutParams layoutParams = installHistoryCard.getLayoutParams();
        if (layoutParams == null || height <= 0) {
            return;
        }
        installHistoryCard.measure(View.MeasureSpec.makeMeasureSpec(installHistoryCard.getWidth() > 0 ? installHistoryCard.getWidth() : getResources().getDisplayMetrics().widthPixels, 1073741824), View.MeasureSpec.makeMeasureSpec(W_REGULAR, W_REGULAR));
        int measuredHeight = installHistoryCard.getMeasuredHeight();
        if (measuredHeight <= 0 || measuredHeight >= height) {
            return;
        }
        installHistoryCard.setClipToOutline(false);
        layoutParams.height = height;
        installHistoryCard.setLayoutParams(layoutParams);
        ValueAnimator ofInt = ValueAnimator.ofInt(height, measuredHeight);
        ofInt.setDuration(420L);
        ofInt.setInterpolator(Motion.STANDARD);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.20
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                layoutParams.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                installHistoryCard.setLayoutParams(layoutParams);
            }
        });
        ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.MainActivity.21
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                layoutParams.height = -2;
                installHistoryCard.setLayoutParams(layoutParams);
                installHistoryCard.setClipToOutline(true);
                installHistoryCard.invalidate();
            }
        });
        ofInt.start();
        SvgIcon.IconView iconView = this.signInHistoryChevron;
        if (iconView != null) {
            iconView.setRotation(270.0f);
            this.signInHistoryChevron.animate().rotation(90.0f).setDuration(280L).setInterpolator(Motion.STANDARD).start();
        }
    }

    private void setStat(TextView textView, String str) {
        if (textView == null) {
            return;
        }
        try {
            TextFx.countUp(textView, Integer.parseInt(str.trim()), 820L);
        } catch (Exception unused) {
            textView.setText(str);
        }
    }

    private String statText(JSONObject jSONObject, String str) {
        return this.signInLoading ? "…" : this.signInDataState != W_SEMIBOLD ? "--" : jSONObject == null ? "0" : jSONObject.optString(str, "0");
    }

    private static String prettyDate(String str) {
        boolean z;
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        if (trim.isEmpty() || "null".equalsIgnoreCase(trim)) {
            return "";
        }
        for (int i = W_REGULAR; i < trim.length(); i += W_MEDIUM) {
            char charAt = trim.charAt(i);
            if (charAt < '0' || charAt > '9') {
                z = false;
                break;
            }
        }
        z = true;
        if (z) {
            if (trim.length() == 8) {
                return LocalDate.parse(trim, DateTimeFormatter.ofPattern("yyyyMMdd")).format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA));
            }
            long parseLong = Long.parseLong(trim);
            if (trim.length() < 12) {
                if (trim.length() == 10) {
                    parseLong *= 1000;
                }
                return "";
            }
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(parseLong), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA));
        }
        String replace = trim.replace('T', ' ');
        if (replace.length() < 16) {
            return trim;
        }
        try {
            return LocalDateTime.parse(replace.substring(W_REGULAR, 16).replace(' ', 'T')).format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA));
        } catch (Exception unused) {
            return trim;
        }
    }

    private String firstValue(JSONObject jSONObject, String... strArr) {
        int length = strArr.length;
        for (int i = W_REGULAR; i < length; i += W_MEDIUM) {
            String optString = jSONObject.optString(strArr[i], "");
            if (!optString.isEmpty() && !"null".equalsIgnoreCase(optString)) {
                return optString;
            }
        }
        return "";
    }

    public String safeMessage(Exception exc) {
        return exc.getMessage() == null ? exc.getClass().getSimpleName() : exc.getMessage();
    }

    public void setAdvancedExpanded(boolean z) {
        this.advancedExpanded = z;
        if (!z) {
            EditText editText = this.signInLatitudeInput;
            if (editText != null) {
                editText.setText("");
            }
            EditText editText2 = this.signInLongitudeInput;
            if (editText2 != null) {
                editText2.setText("");
            }
            TextView textView = this.signInStatus;
            if (textView != null) {
                textView.setText("已收起高级签到，提交时将获取设备当前位置。");
            }
        }
        LinearLayout linearLayout = this.advancedBody;
        if (linearLayout != null) {
            UiPolish.accordion(linearLayout, z);
        }
        updateAdvancedSummary();
    }

    private void updateAdvancedSummary() {
        if (this.advancedToggle == null) {
            return;
        }
        EditText editText = this.signInLatitudeInput;
        boolean z = (editText == null || this.signInLongitudeInput == null || editText.getText().toString().trim().isEmpty() || this.signInLongitudeInput.getText().toString().trim().isEmpty()) ? false : true;
        boolean z2 = this.advancedExpanded;
        String str = z2 ? "收起 ▴" : "展开 ▾";
        if (z) {
            this.advancedToggle.setText(str.concat(" · 已用自定义坐标"));
        } else {
            this.advancedToggle.setText(z2 ? "收起 ▴" : "展开 ▾");
        }
    }

    public void fillRecommendedLocation(String str, String str2, String str3) {
        EditText editText = this.signInLatitudeInput;
        if (editText != null) {
            editText.setText(str);
        }
        EditText editText2 = this.signInLongitudeInput;
        if (editText2 != null) {
            editText2.setText(str2);
        }
        updateAdvancedSummary();
        TextView textView = this.signInStatus;
        if (textView != null) {
            textView.setText("已填入" + str3 + "坐标，提交时直接使用该坐标。");
        }
    }

    public double[] readSelectedCoordinates() {
        if (!this.advancedExpanded) {
            return null;
        }
        EditText editText = this.signInLatitudeInput;
        String trim = editText == null ? "" : editText.getText().toString().trim();
        EditText editText2 = this.signInLongitudeInput;
        String trim2 = editText2 != null ? editText2.getText().toString().trim() : "";
        if (trim.isEmpty() && trim2.isEmpty()) {
            return null;
        }
        if (trim.isEmpty() || trim2.isEmpty()) {
            throw new IllegalArgumentException("请同时填写纬度和经度，或清空两项使用设备定位。");
        }
        double parseDouble = Double.parseDouble(trim);
        double parseDouble2 = Double.parseDouble(trim2);
        if (Double.isNaN(parseDouble) || Double.isInfinite(parseDouble) || Double.isNaN(parseDouble2) || Double.isInfinite(parseDouble2) || parseDouble < -90.0d || parseDouble > 90.0d || parseDouble2 < -180.0d || parseDouble2 > 180.0d) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        return new double[]{parseDouble, parseDouble2};
    }

    public void beginSignIn() {
        if (this.signInSubmitting) {
            return;
        }
        EditText editText = this.signInCodeInput;
        String trim = editText == null ? "" : editText.getText().toString().trim();
        if (trim.isEmpty()) {
            toast("请先输入签到码");
            return;
        }
        if (ready()) {
            try {
                double[] readSelectedCoordinates = readSelectedCoordinates();
                this.pendingSignInCode = trim;
                setSignInBusy(true);
                if (readSelectedCoordinates != null) {
                    TextView textView = this.signInStatus;
                    if (textView != null) {
                        textView.setText("正在使用所选坐标提交签到码…");
                    }
                    submitSignInCode(trim, readSelectedCoordinates[W_REGULAR], readSelectedCoordinates[W_MEDIUM]);
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0 && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
                    requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, REQUEST_LOCATION);
                    TextView textView2 = this.signInStatus;
                    if (textView2 != null) {
                        textView2.setText("请允许定位权限，以便按平台要求提交真实坐标。");
                        return;
                    }
                    return;
                }
                requestCurrentLocationAndSubmit();
            } catch (IllegalArgumentException e) {
                TextView textView3 = this.signInStatus;
                if (textView3 != null) {
                    textView3.setText(e.getMessage());
                }
            }
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == CalendarReminder.requestCode()) {
            if (CalendarReminder.hasPermission(this)) {
                scheduleRemindersSafe();
                Toast.makeText(this, "日历权限已开启，" + CalendarReminder.summary(this), W_MEDIUM).show();
                return;
            }
            Toast.makeText(this, "没有拿到日历权限，可以用的时候再给", W_MEDIUM).show();
            return;
        }
        if (i == REQUEST_LOCATION) {
            int length = iArr.length;
            boolean z = false;
            for (int i2 = W_REGULAR; i2 < length; i2 += W_MEDIUM) {
                if (iArr[i2] == 0) {
                    z = true;
                }
            }
            if (z) {
                requestCurrentLocationAndSubmit();
                return;
            }
            setSignInBusy(false);
            TextView textView = this.signInStatus;
            if (textView != null) {
                textView.setText("未获得定位权限，未发送签到请求。");
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x005d A[Catch: Exception -> 0x0086, SecurityException -> 0x00a4, TryCatch #2 {SecurityException -> 0x00a4, Exception -> 0x0086, blocks: (B:3:0x0003, B:5:0x000f, B:7:0x001b, B:13:0x002c, B:17:0x003b, B:22:0x005d, B:24:0x0061, B:25:0x0066, B:28:0x0076, B:29:0x007d, B:30:0x0044, B:35:0x004f, B:41:0x007e, B:42:0x0085), top: B:2:0x0003 }] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0076 A[Catch: Exception -> 0x0086, SecurityException -> 0x00a4, TryCatch #2 {SecurityException -> 0x00a4, Exception -> 0x0086, blocks: (B:3:0x0003, B:5:0x000f, B:7:0x001b, B:13:0x002c, B:17:0x003b, B:22:0x005d, B:24:0x0061, B:25:0x0066, B:28:0x0076, B:29:0x007d, B:30:0x0044, B:35:0x004f, B:41:0x007e, B:42:0x0085), top: B:2:0x0003 }] */
    /* JADX WARN: Removed duplicated region for block: B:32:0x004c  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x004d  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void requestCurrentLocationAndSubmit() {
        /*
            r8 = this;
            java.lang.String r0 = "network"
            r1 = 0
            java.lang.String r2 = "location"
            java.lang.Object r2 = r8.getSystemService(r2)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            android.location.LocationManager r2 = (android.location.LocationManager) r2     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r8.locationManager = r2     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r2 == 0) goto L7e
            android.location.Criteria r2 = new android.location.Criteria     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r2.<init>()     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            int r3 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r4 = 23
            r5 = 1
            if (r3 < r4) goto L26
            java.lang.String r3 = "android.permission.ACCESS_FINE_LOCATION"
            int r3 = r8.checkSelfPermission(r3)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r3 != 0) goto L24
            goto L26
        L24:
            r3 = 0
            goto L27
        L26:
            r3 = 1
        L27:
            if (r3 == 0) goto L2b
            r4 = 1
            goto L2c
        L2b:
            r4 = 2
        L2c:
            r2.setAccuracy(r4)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            android.location.LocationManager r4 = r8.locationManager     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            java.lang.String r2 = r4.getBestProvider(r2, r5)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            java.lang.String r4 = "gps"
            if (r2 == 0) goto L44
            if (r3 != 0) goto L42
            boolean r5 = r4.equals(r2)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r5 == 0) goto L42
            goto L44
        L42:
            r3 = r2
            goto L5b
        L44:
            android.location.LocationManager r2 = r8.locationManager     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            boolean r2 = r2.isProviderEnabled(r0)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r2 == 0) goto L4d
            goto L5a
        L4d:
            if (r3 == 0) goto L59
            android.location.LocationManager r0 = r8.locationManager     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            boolean r0 = r0.isProviderEnabled(r4)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r0 == 0) goto L59
            r3 = r4
            goto L5b
        L59:
            r0 = 0
        L5a:
            r3 = r0
        L5b:
            if (r3 == 0) goto L76
            android.widget.TextView r0 = r8.signInStatus     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            if (r0 == 0) goto L66
            java.lang.String r2 = "正在获取当前位置…"
            r0.setText(r2)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
        L66:
            com.zuwe.smartcourselite.MainActivity$22 r7 = new com.zuwe.smartcourselite.MainActivity$22     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r7.<init>()     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r8.pendingLocationListener = r7     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            android.location.LocationManager r2 = r8.locationManager     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            r4 = 0
            r6 = 0
            r2.requestLocationUpdates(r3, r4, r6, r7)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            goto Lb1
        L76:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            java.lang.String r2 = "请开启系统定位服务后重试"
            r0.<init>(r2)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            throw r0     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
        L7e:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            java.lang.String r2 = "设备定位服务不可用"
            r0.<init>(r2)     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
            throw r0     // Catch: java.lang.Exception -> L86 java.lang.SecurityException -> La4
        L86:
            r0 = move-exception
            r8.setSignInBusy(r1)
            android.widget.TextView r1 = r8.signInStatus
            if (r1 == 0) goto Lb1
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "定位失败："
            r2.<init>(r3)
            java.lang.String r0 = r0.getMessage()
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r1.setText(r0)
            goto Lb1
        La4:
            r8.setSignInBusy(r1)
            android.widget.TextView r0 = r8.signInStatus
            if (r0 == 0) goto Lb1
            java.lang.String r1 = "没有定位权限，请授予后再试。"
            r0.setText(r1)
        Lb1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.MainActivity.requestCurrentLocationAndSubmit():void");
    }

    public void stopLocationRequest() {
        LocationListener locationListener;
        Runnable runnable = this.locationTimeout;
        if (runnable != null) {
            this.locationHandler.removeCallbacks(runnable);
        }
        this.locationTimeout = null;
        LocationManager locationManager = this.locationManager;
        if (locationManager != null && (locationListener = this.pendingLocationListener) != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException unused) {
            }
        }
        this.pendingLocationListener = null;
    }

    public void setSignInBusy(boolean z) {
        this.signInSubmitting = z;
        Button button = this.signInSubmitButton;
        if (button != null) {
            button.setEnabled(!z);
            this.signInSubmitButton.setAlpha(z ? 0.72f : 1.0f);
        }
        Button button2 = this.imeSubmitButton;
        if (button2 != null) {
            button2.setEnabled(!z);
            this.imeSubmitButton.setAlpha(z ? 0.72f : 1.0f);
        }
    }

    public void syncImeState() {
        if (this.screenRoot == null) {
            return;
        }
        Rect rect = new Rect();
        this.screenRoot.getWindowVisibleDisplayFrame(rect);
        boolean z = this.screenRoot.getRootView().getHeight() - rect.bottom > dp(120.0f);
        if (z != this.imeOpen) {
            this.imeOpen = z;
            applyBottomBarState();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:4:0x0008, code lost:
    
        if (r5.activePage == com.zuwe.smartcourselite.MainActivity.W_MEDIUM) goto L8;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void applyBottomBarState() {
        /*
            r5 = this;
            boolean r0 = r5.imeOpen
            r1 = 0
            if (r0 == 0) goto Lb
            int r0 = r5.activePage
            r2 = 1
            if (r0 != r2) goto Lb
            goto Lc
        Lb:
            r2 = 0
        Lc:
            android.widget.FrameLayout r0 = r5.bottomNav
            r3 = 8
            if (r0 == 0) goto L1b
            if (r2 == 0) goto L17
            r4 = 8
            goto L18
        L17:
            r4 = 0
        L18:
            r0.setVisibility(r4)
        L1b:
            android.widget.LinearLayout r0 = r5.imeSubmitBar
            if (r0 == 0) goto L27
            if (r2 == 0) goto L22
            goto L24
        L22:
            r1 = 8
        L24:
            r0.setVisibility(r1)
        L27:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.MainActivity.applyBottomBarState():void");
    }

    public void submitSignInCode(String str, double d, double d2) {
        TextView textView = this.signInStatus;
        if (textView != null) {
            textView.setText("正在提交签到码…（不会自动重复提交）");
        }
        this.io.execute(new RunnableC0007AnonymousClass25(this, this.auth, this.cookie, str, d, d2));
    }

    /* renamed from: com.zuwe.smartcourselite.MainActivity$AnonymousClass25, reason: case insensitive filesystem */
    class RunnableC0007AnonymousClass25 implements Runnable {
        final MainActivity this$0;
        final String val$code;
        final String val$currentAuth;
        final String val$currentCookie;
        final double val$latitude;
        final double val$longitude;

        RunnableC0007AnonymousClass25(MainActivity mainActivity, String str, String str2, String str3, double d, double d2) {
            this.val$currentAuth = str;
            this.val$currentCookie = str2;
            this.val$code = str3;
            this.val$latitude = d;
            this.val$longitude = d2;
            this.this$0 = mainActivity;
        }

        @Override // java.lang.Runnable
        public void run() {
            JSONObject jSONObject = null;
            Exception exc = null;
            try {
                exc = null;
                jSONObject = new SignInApi(this.this$0, this.val$currentAuth, this.val$currentCookie).validateCode(this.val$code, this.val$latitude, this.val$longitude);
            } catch (Exception e) {
                jSONObject = null;
                exc = e;
            }
            final JSONObject fj = jSONObject;
            final Exception fe = exc;
            MainActivity mainActivity = this.this$0;
            final String str = this.val$code;
            final double d = this.val$latitude;
            final double d2 = this.val$longitude;
            mainActivity.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass25.1
                @Override // java.lang.Runnable
                public void run() {
                    RunnableC0007AnonymousClass25.this.this$0.setSignInBusy(false);
                    if (fe != null) {
                        if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                            RunnableC0007AnonymousClass25.this.this$0.signInStatus.setText("提交失败：" + RunnableC0007AnonymousClass25.this.this$0.safeMessage(fe));
                        }
                        Exception exc2 = fe;
                        if ((exc2 instanceof SignInApi.ApiException) && ((SignInApi.ApiException) exc2).isAuthFailure()) {
                            if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                                RunnableC0007AnonymousClass25.this.this$0.signInStatus.setText("登录状态已失效，正在自动续期后重试…");
                            }
                            MainActivity mainActivity2 = RunnableC0007AnonymousClass25.this.this$0;
                            final String str2 = str;
                            final double d3 = d;
                            final double d4 = d2;
                            mainActivity2.trySilentReauth(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass25.1.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    RunnableC0007AnonymousClass25.this.this$0.submitSignInCode(str2, d3, d4);
                                }
                            }, new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.AnonymousClass25.1.2
                                @Override // java.lang.Runnable
                                public void run() {
                                    if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                                        RunnableC0007AnonymousClass25.this.this$0.signInStatus.setText("登录状态已失效，请重新授权后再签到");
                                    }
                                }
                            });
                            return;
                        }
                        return;
                    }
                    int optInt = fj.optInt("code", -1);
                    String optString = fj.optString("message", "");
                    if (optInt == 400) {
                        if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                            TextView textView = RunnableC0007AnonymousClass25.this.this$0.signInStatus;
                            if (optString.isEmpty()) {
                                optString = "签到码无效。";
                            }
                            textView.setText(optString);
                            return;
                        }
                        return;
                    }
                    if (optInt == 200 || optInt == 0) {
                        UiPolish.successPopup(RunnableC0007AnonymousClass25.this.this$0, "签到成功！", "正在刷新签到记录核对…", RunnableC0007AnonymousClass25.this.this$0.green);
                        if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                            RunnableC0007AnonymousClass25.this.this$0.signInStatus.setText("服务器返回成功响应，正在刷新记录核对签到状态…");
                        }
                        RunnableC0007AnonymousClass25.this.this$0.refreshSignInData();
                        return;
                    }
                    if (RunnableC0007AnonymousClass25.this.this$0.signInStatus != null) {
                        TextView textView2 = RunnableC0007AnonymousClass25.this.this$0.signInStatus;
                        StringBuilder sb = new StringBuilder("服务器返回待确认结果：");
                        if (optString.isEmpty()) {
                            optString = fj.toString();
                        }
                        sb.append(optString);
                        textView2.setText(sb.toString());
                    }
                }
            });
        }
    }

    private View datePanel() {
        LinearLayout bigPanel = bigPanel();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_MEDIUM);
        linearLayout2.setGravity(16);
        TextView label = label("", 34.0f, this.ink, W_SEMIBOLD);
        this.dateTitle = label;
        label.setLetterSpacing(-0.02f);
        this.dateTitle.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.23
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.pickDate();
            }
        });
        this.dateTitle.setContentDescription("选择日期");
        this.dateSub = label("", 14.0f, this.muted, W_REGULAR);
        linearLayout2.addView(this.dateTitle, new LinearLayout.LayoutParams(-2, -2));
        linearLayout2.addView(this.dateSub, top(4));
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        FrameLayout circleChip = circleChip(SvgIcon.CALENDAR, "打开日历选择日期");
        circleChip.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.24
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.pickDate();
            }
        });
        FrameLayout circleChip2 = circleChip(SvgIcon.BELL, "上课提醒设置");
        circleChip2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.25
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showReminderSettings();
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(44.0f), dp(44.0f));
        layoutParams.rightMargin = dp(6.0f);
        linearLayout.addView(circleChip2, layoutParams);
        linearLayout.addView(circleChip, new LinearLayout.LayoutParams(dp(44.0f), dp(44.0f)));
        bigPanel.addView(linearLayout);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setGravity(16);
        linearLayout3.addView(new View(this), new LinearLayout.LayoutParams(W_REGULAR, W_MEDIUM, 1.0f));
        TextView chevron = chevron("‹", "上一周");
        chevron.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.26
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.jumpTo(mainActivity.viewDate.plusWeeks(-1L));
            }
        });
        linearLayout3.addView(chevron, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
        TextView label2 = label("", 14.0f, this.ink, W_MEDIUM);
        this.weekLabel = label2;
        label2.setGravity(17);
        this.weekLabel.setMinWidth(dp(92.0f));
        this.weekLabel.setContentDescription("点击查看周次总览，长按校准第 1 周");
        this.weekLabel.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.27
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.openWeekOverview();
            }
        });
        this.weekLabel.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.28
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                MainActivity.this.calibrateSemester();
                return true;
            }
        });
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, dp(32.0f));
        layoutParams2.leftMargin = dp(2.0f);
        layoutParams2.rightMargin = dp(2.0f);
        linearLayout3.addView(this.weekLabel, layoutParams2);
        TextView chevron2 = chevron("›", "下一周");
        chevron2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.29
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.jumpTo(mainActivity.viewDate.plusWeeks(1L));
            }
        });
        linearLayout3.addView(chevron2, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
        linearLayout3.addView(new View(this), new LinearLayout.LayoutParams(W_REGULAR, W_MEDIUM, 1.0f));
        TextView label3 = label("周次总览", 13.0f, this.muted, W_REGULAR);
        label3.setGravity(17);
        label3.setPadding(dp(8.0f), W_REGULAR, dp(2.0f), W_REGULAR);
        label3.setContentDescription("周次总览");
        label3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.30
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.openWeekOverview();
            }
        });
        UiPolish.pressable(label3);
        this.weekOverviewBtn = label3;
        linearLayout3.addView(label3, new LinearLayout.LayoutParams(-2, dp(32.0f)));
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp(16.0f);
        bigPanel.addView(linearLayout3, layoutParams3);
        LinearLayout linearLayout4 = new LinearLayout(this);
        this.dayStrip = linearLayout4;
        linearLayout4.setOrientation(W_REGULAR);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.topMargin = dp(16.0f);
        bigPanel.addView(this.dayStrip, layoutParams4);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setGravity(16);
        TextView label4 = label("正在检查本机凭证…", 12.0f, this.faint, W_REGULAR);
        this.status = label4;
        label4.setLineSpacing(dp(3.0f), 1.0f);
        linearLayout5.addView(this.status, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        Button ghostBtn = ghostBtn("刷新课表");
        this.refreshBtn = ghostBtn;
        ghostBtn.setTextSize(12.0f);
        this.refreshBtn.setMinHeight(dp(36.0f));
        this.refreshBtn.setMinimumHeight(dp(36.0f));
        this.refreshBtn.setPadding(dp(14.0f), W_REGULAR, dp(14.0f), W_REGULAR);
        this.refreshBtn.setContentDescription("刷新课表");
        this.refreshBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.31
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.refresh(true);
            }
        });
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams5.leftMargin = dp(10.0f);
        linearLayout5.addView(this.refreshBtn, layoutParams5);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams6.topMargin = dp(14.0f);
        bigPanel.addView(linearLayout5, layoutParams6);
        updateDateHeader();
        return bigPanel;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        ScrollView scrollView;
        ScrollView scrollView2;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.touchDownX = motionEvent.getX();
            this.touchDownY = motionEvent.getY();
            this.lastDragX = motionEvent.getX();
            this.lastDragT = motionEvent.getEventTime();
            this.dragVx = 0.0f;
            this.pageDragging = false;
        } else if (actionMasked == W_SEMIBOLD) {
            float x = motionEvent.getX() - this.touchDownX;
            float y = motionEvent.getY() - this.touchDownY;
            if (this.activePage == 0 && this.courseScroll != null && Math.abs(x) > dp(10.0f) && Math.abs(x) > Math.abs(y) * 1.6f) {
                if (!this.pageDragging && (scrollView2 = this.courseScroll) != null) {
                    Haptics.tick(scrollView2);
                }
                this.pageDragging = true;
                float x2 = motionEvent.getX();
                long eventTime = motionEvent.getEventTime();
                float f = eventTime - this.lastDragT;
                if (f > 0.0f) {
                    this.dragVx = (x2 - this.lastDragX) / f;
                }
                this.lastDragX = x2;
                this.lastDragT = eventTime;
                float dp = dp(150.0f);
                double d = dp;
                float tanh = (float) (d * Math.tanh(x / d));
                this.courseScroll.setTranslationX(0.42f * tanh);
                this.courseScroll.setAlpha(1.0f - Math.min(0.28f, Math.abs(tanh) / (dp * 2.8f)));
            } else if (this.activePage == 0 && (scrollView = this.courseScroll) != null && scrollView.getScrollY() == 0 && y > dp(14.0f) && y > Math.abs(x) * 1.4f) {
                this.pullDragging = true;
                float dp2 = dp(220.0f);
                double d2 = dp2;
                float tanh2 = (float) (d2 * Math.tanh(y / d2));
                this.courseScroll.setPivotY(0.0f);
                this.courseScroll.setTranslationY(0.55f * tanh2);
                this.courseScroll.setScaleY(Math.min(0.06f, (tanh2 / dp2) * 0.06f) + 1.0f);
            }
        } else if (actionMasked == W_MEDIUM || actionMasked == 3) {
            float x3 = motionEvent.getX() - this.touchDownX;
            float y2 = motionEvent.getY() - this.touchDownY;
            if (this.pullDragging) {
                this.pullDragging = false;
                boolean z = y2 > ((float) dp(90.0f));
                ScrollView scrollView3 = this.courseScroll;
                if (scrollView3 != null) {
                    scrollView3.animate().cancel();
                    this.courseScroll.animate().translationY(0.0f).scaleY(1.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
                }
                if (z) {
                    refresh(true);
                }
            } else {
                if (this.pageDragging) {
                    this.pageDragging = false;
                    boolean z2 = Math.abs(this.dragVx) > 0.45f;
                    boolean z3 = Math.abs(x3) > ((float) dp(24.0f));
                    boolean z4 = x3 < 0.0f;
                    if (this.activePage == 0 && (z2 || z3)) {
                        if (z2) {
                            z4 = this.dragVx < 0.0f;
                        }
                        ScrollView scrollView4 = this.courseScroll;
                        if (scrollView4 != null) {
                            Haptics.confirm(scrollView4);
                        }
                        jumpTo(this.viewDate.plusDays(z4 ? 1L : -1L));
                        if (this.courseScroll != null) {
                            float dp3 = dp(30.0f) + Math.min(dp(34.0f), Math.abs(this.dragVx) * dp(56.0f));
                            ScrollView scrollView5 = this.courseScroll;
                            if (!z4) {
                                dp3 = -dp3;
                            }
                            scrollView5.setTranslationX(dp3);
                        }
                    }
                    ScrollView scrollView6 = this.courseScroll;
                    if (scrollView6 != null) {
                        scrollView6.animate().cancel();
                        this.courseScroll.animate().translationX(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
                    }
                } else if (this.activePage == 0 && Math.abs(x3) > dp(72.0f) && Math.abs(x3) > Math.abs(y2) * 2.0f) {
                    ScrollView scrollView7 = this.courseScroll;
                    if (scrollView7 != null) {
                        Haptics.confirm(scrollView7);
                    }
                    jumpTo(this.viewDate.plusDays(x3 >= 0.0f ? -1L : 1L));
                }
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void renderDayStrip() {
        LocalDate localDate;
        LinearLayout linearLayout = this.dayStrip;
        if (linearLayout == null) {
            return;
        }
        linearLayout.removeAllViews();
        LocalDate monday = WeekUtil.monday(this.viewDate);
        LocalDate now = LocalDate.now();
        int i = W_REGULAR;
        int i2 = W_REGULAR;
        while (i2 < 7) {
            final LocalDate plusDays = monday.plusDays(i2);
            boolean equals = plusDays.equals(this.viewDate);
            boolean equals2 = plusDays.equals(now);
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(W_MEDIUM);
            linearLayout2.setGravity(17);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(dp(16.0f));
            if (equals) {
                gradientDrawable.setColor(this.green);
            } else if (equals2) {
                gradientDrawable.setColor(UiPolish.withAlpha(this.green, this.dark ? 31 : 20));
            } else {
                gradientDrawable.setColor(i);
            }
            linearLayout2.setBackground(gradientDrawable);
            TextView label = label(WEEKDAY_SHORT[i2], 12.0f, equals ? this.onGreen : this.muted, i);
            label.setGravity(17);
            TextView label2 = label(String.valueOf(plusDays.getDayOfMonth()), 17.0f, equals ? this.onGreen : this.ink, W_MEDIUM);
            label2.setGravity(17);
            FrameLayout frameLayout = new FrameLayout(this);
            if (!equals2 || equals) {
                localDate = monday;
            } else {
                View view = new View(this);
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setShape(W_MEDIUM);
                gradientDrawable2.setColor(this.green);
                view.setBackground(gradientDrawable2);
                localDate = monday;
                frameLayout.addView(view, new FrameLayout.LayoutParams(dp(4.0f), dp(4.0f), 17));
            }
            linearLayout2.addView(label, new LinearLayout.LayoutParams(-1, dp(14.0f)));
            linearLayout2.addView(label2, new LinearLayout.LayoutParams(-1, dp(21.0f)));
            linearLayout2.addView(frameLayout, new LinearLayout.LayoutParams(-1, dp(7.0f)));
            StringBuilder sb = new StringBuilder();
            sb.append(plusDays.format(FMT_TITLE));
            sb.append(" ");
            sb.append(WEEKDAY_CN[i2]);
            sb.append(equals2 ? " 今天" : "");
            linearLayout2.setContentDescription(sb.toString());
            linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.32
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    MainActivity.this.jumpTo(plusDays);
                }
            });
            UiPolish.pressable(linearLayout2);
            if (equals) {
                this.selectedDayChip = linearLayout2;
            }
            if (equals) {
                linearLayout2.setScaleX(0.94f);
                linearLayout2.setScaleY(0.94f);
                linearLayout2.animate().scaleX(1.0f).scaleY(1.0f).setDuration(90L).setInterpolator(Motion.STANDARD).start();
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(W_REGULAR, dp(60.0f), 1.0f);
            if (i2 > 0) {
                layoutParams.leftMargin = dp(4.0f);
            }
            this.dayStrip.addView(linearLayout2, layoutParams);
            if (equals) {
                linearLayout2.setScaleX(0.9f);
                linearLayout2.setScaleY(0.9f);
                linearLayout2.setAlpha(0.55f);
                linearLayout2.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(180L).setInterpolator(Motion.ENTER).start();
            }
            i2 += W_MEDIUM;
            monday = localDate;
            i = W_REGULAR;
        }
    }

    private TextView chevron(String str, String str2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(18.0f);
        textView.setTextColor(this.muted);
        textView.setGravity(17);
        textView.setMinWidth(dp(36.0f));
        textView.setMinHeight(dp(36.0f));
        textView.setMinimumWidth(dp(36.0f));
        textView.setMinimumHeight(dp(36.0f));
        textView.setContentDescription(str2);
        textView.setClickable(true);
        UiPolish.pressable(textView);
        return textView;
    }

    private View authPanel() {
        LinearLayout bigPanel = bigPanel();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        View view = new View(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(W_MEDIUM);
        gradientDrawable.setColor(this.muted);
        view.setBackground(gradientDrawable);
        linearLayout.addView(view, new LinearLayout.LayoutParams(dp(8.0f), dp(8.0f)));
        this.credentialDot = view;
        this.credentialState = label("尚未检测到本地授权", 13.0f, this.muted, W_REGULAR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = dp(8.0f);
        linearLayout.addView(this.credentialState, layoutParams);
        bigPanel.addView(linearLayout);
        Button primaryBtn = primaryBtn("微信 / 官方账号授权登录");
        primaryBtn.setMinHeight(dp(60.0f));
        primaryBtn.setMinimumHeight(dp(60.0f));
        primaryBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.33
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.startOAuth(false);
            }
        });
        bigPanel.addView(primaryBtn, top(16));
        Button jwEntry = ghostBtn("教务系统课表 · 网页登录后一键填充");
        jwEntry.setOnClickListener(new JwClickListener());
        bigPanel.addView(jwEntry, top(10));
        Button ghostBtn = ghostBtn("高级恢复：导入历史抓包 ZIP");
        ghostBtn.setMinHeight(dp(52.0f));
        ghostBtn.setMinimumHeight(dp(52.0f));
        ghostBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.34
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.chooseCaptureZip();
            }
        });
        bigPanel.addView(ghostBtn, top(10));
        TextView label = label("凭证仅保存在本机加密存储中，本应用不会伪造登录 code。", 12.0f, this.faint, W_REGULAR);
        label.setLineSpacing(dp(3.0f), 1.0f);
        bigPanel.addView(label, top(14));
        return bigPanel;
    }

    public void jumpTo(LocalDate localDate) {
        if (!localDate.equals(this.viewDate)) {
            this.pendingDayReveal = true;
        }
        this.viewDate = localDate;
        updateDateHeader();
        if (JwGate.ok(this, localDate)) {
            List<Course> loadCache = loadCache(localDate);
            if (loadCache != null) {
                render(loadCache);
                this.lastData = loadCache;
                setStatus(localDate.format(FMT_TITLE) + " · " + cacheAge(localDate));
                return;
            }
            this.lastData = null;
            render(Collections.EMPTY_LIST);
            refresh(false);
        }
    }

    private void shiftDate(int i) {
        LocalDate now = LocalDate.now();
        LocalDate plusDays = this.viewDate.plusDays(i);
        if (plusDays.isBefore(now.minusDays(200L)) || plusDays.isAfter(now.plusDays(200L))) {
            toast("已超出可浏览范围");
        } else {
            jumpTo(plusDays);
        }
    }

    public void updateDateHeader() {
        String str;
        String str2;
        String str3;
        if (this.dateTitle == null) {
            return;
        }
        LocalDate now = LocalDate.now();
        String str4 = this.viewDate.getMonthValue() + "月" + this.viewDate.getDayOfMonth() + "日";
        if (this.dateTitle.getText() == null || this.dateTitle.getText().length() == 0) {
            this.dateTitle.setText(str4);
        } else {
            TextFx.flip(this.dateTitle, str4);
        }
        int weekOf = weekOf(this.viewDate);
        String str5 = WEEKDAY_CN[this.viewDate.getDayOfWeek().getValue() - W_MEDIUM];
        TextView textView = this.dateSub;
        StringBuilder sb = new StringBuilder();
        sb.append(str5);
        if (weekOf >= W_MEDIUM) {
            str = " · 第" + weekOf + "教学周";
        } else {
            str = " · 假期";
        }
        sb.append(str);
        textView.setText(sb.toString());
        TextView textView2 = this.weekLabel;
        if (textView2 != null) {
            if (weekOf >= W_MEDIUM) {
                str3 = "第" + weekOf + "周";
            } else {
                str3 = "假期";
            }
            textView2.setText(str3);
        }
        renderDayStrip();
        TextView textView3 = this.todoTitle;
        if (textView3 != null) {
            if (this.viewDate.equals(now)) {
                str2 = "今日待办";
            } else {
                str2 = "待办 · " + this.viewDate.getMonthValue() + "月" + this.viewDate.getDayOfMonth() + "日";
            }
            textView3.setText(str2);
        }
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

    private void styleTab(Button button, boolean z) {
        if (button == null) {
            return;
        }
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(22.0f));
        if (z) {
            gradientDrawable.setColor(UiPolish.withAlpha(this.accentA, this.dark ? 51 : 34));
            button.setTextColor(this.green);
            button.setTypeface(Typeface.DEFAULT, W_MEDIUM);
        } else {
            gradientDrawable.setColor(W_REGULAR);
            button.setTextColor(this.muted);
            button.setTypeface(Typeface.DEFAULT, W_REGULAR);
        }
        button.setBackground(gradientDrawable);
    }

    private void loadSaved() {
        try {
            AuthManager.Credentials load = AuthManager.load(this);
            if (load != null) {
                this.auth = load.authorizationJwt;
                this.cookie = load.sklTokenJwt;
                refreshCredentialLine();
                smartStart();
                return;
            }
            if (this.credentialState != null) {
                setCredentialState("尚未登录，请先完成官方授权", this.muted);
            }
            setStatus("等待账号授权");
            render(Collections.EMPTY_LIST);
        } catch (Exception e) {
            setStatus("凭证读取失败：" + e.getMessage());
        }
    }

    public void refreshCredentialLine() {
        if (this.credentialState == null) {
            return;
        }
        try {
            AuthManager.Credentials load = AuthManager.load(this);
            if (load != null) {
                String expiryLabel = TokenParser.expiryLabel(load.authorizationJwt);
                StringBuilder sb = new StringBuilder("已连接 · Authorization 已加密保存");
                String str = "";
                sb.append(load.sklTokenJwt.isEmpty() ? "" : " · 兼容凭证已保存");
                if (!expiryLabel.isEmpty()) {
                    str = " · " + expiryLabel;
                }
                sb.append(str);
                setCredentialState(sb.toString(), this.green);
            }
        } catch (Exception unused) {
        }
    }

    private void setCredentialState(String str, int i) {
        TextView textView = this.credentialState;
        if (textView != null) {
            textView.setText(str);
            this.credentialState.setTextColor(i);
        }
        View view = this.credentialDot;
        if (view != null) {
            ((GradientDrawable) view.getBackground()).setColor(i);
        }
    }

    private void showRingingBar() {
        try {
            if (AlarmTone.ringing() && this.ringingBar == null) {
                TextView textView = new TextView(this);
                textView.setText("闹钟正在响 · 点此停止");
                textView.setTextSize(13.0f);
                textView.setTypeface(TF_MEDIUM);
                textView.setTextColor(-15986931);
                textView.setGravity(17);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(-6559564);
                gradientDrawable.setCornerRadius(dp(14.0f));
                textView.setBackground(gradientDrawable);
                int dp = dp(12.0f);
                textView.setPadding(dp, dp(10.0f), dp, dp(10.0f));
                textView.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.35
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        Haptics.tick(view);
                        AlarmTone.stop();
                        ReminderNotifier.cancelAll(MainActivity.this);
                        AlarmActivity.dismissLive();
                        MainActivity.this.removeRingingBar();
                    }
                });
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
                layoutParams.gravity = 49;
                layoutParams.topMargin = dp(46.0f);
                ((ViewGroup) getWindow().getDecorView()).addView(textView, layoutParams);
                this.ringingBar = textView;
                textView.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.36
                    @Override // java.lang.Runnable
                    public void run() {
                        MainActivity.this.removeRingingBar();
                    }
                }, 61000L);
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeRingingBar() {
        try {
            View view = this.ringingBar;
            if (view == null) {
                return;
            }
            this.ringingBar = null;
            ((ViewGroup) view.getParent()).removeView(view);
        } catch (Exception unused) {
        }
    }

    private void ensureNotifyPermissionOnResume() {
        if (Build.VERSION.SDK_INT >= 33 && ReminderPrefs.toggled(this) && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.lastNotifyAskAt < 60000) {
                return;
            }
            this.lastNotifyAskAt = currentTimeMillis;
            ReminderPrefs.pushLog(this, "通知权限未授予，正在申请");
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 9001);
        }
    }

    private void maybeDailyRefresh() {
        if (this.loading || LocalDate.now().toString().equals(prefs().getString(KEY_AUTO_DAY, ""))) {
            return;
        }
        try {
            if (AuthManager.load(this) == null) {
                return;
            }
            autoSyncTodayAndTomorrow(LocalDate.now());
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        Runnable runnable;
        Handler handler = this.uiHandler;
        if (handler != null && (runnable = this.stateTick) != null) {
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        LocalDate now = LocalDate.now();
        if (!now.equals(this.openedDate)) {
            if (this.viewDate.equals(this.openedDate)) {
                this.viewDate = now;
                updateDateHeader();
                if (ready()) {
                    List<Course> loadCache = loadCache(now);
                    if (loadCache != null) {
                        render(loadCache);
                        this.lastData = loadCache;
                    } else {
                        this.lastData = null;
                        render(Collections.EMPTY_LIST);
                    }
                }
            }
            this.openedDate = now;
        }
        maybeDailyRefresh();
        ensureNotifyPermissionOnResume();
        maybeRemind();
        scheduleRemindersSafe();
        showRingingBar();
        refreshWeekCalendar(false);
        UpdateManager.cleanupAfterUpdate(this);
        if (this.updatePrompted || !UpdateManager.hasDownloaded(this)) {
            return;
        }
        this.updatePrompted = true;
        UpdateManager.checkOnLaunch(this);
    }

    public void pickDate() {
        LocalDate localDate = this.viewDate;
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() { // from class: com.zuwe.smartcourselite.MainActivity.37
            @Override // android.app.DatePickerDialog.OnDateSetListener
            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                MainActivity.this.jumpTo(LocalDate.of(i, i2 + MainActivity.W_MEDIUM, i3));
            }
        }, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth()).show();
    }

    private LocalDate semesterAnchor() {
        String string = prefs().getString(KEY_SEM_ANCHOR, "");
        if (!string.isEmpty()) {
            try {
                return LocalDate.parse(string);
            } catch (Exception unused) {
            }
        }
        LocalDate anchor = WeekCalendar.anchor(this);
        return anchor != null ? anchor : WeekUtil.defaultAnchor(LocalDate.now());
    }

    private int weekOf(LocalDate localDate) {
        String string = prefs().getString(KEY_SEM_ANCHOR, "");
        if (!string.isEmpty()) {
            try {
                return WeekUtil.weekNumber(LocalDate.parse(string), localDate);
            } catch (Exception unused) {
            }
        }
        Integer weekFor = WeekCalendar.weekFor(this, localDate);
        if (weekFor != null) {
            return weekFor.intValue();
        }
        return WeekUtil.weekNumber(WeekUtil.defaultAnchor(LocalDate.now()), localDate);
    }

    private void refreshWeekCalendar(boolean z) {
        String str = this.auth;
        if (str == null || str.isEmpty()) {
            return;
        }
        if (!z && !WeekCalendar.isStale(this)) {
            updateDateHeader();
        } else {
            if (this.weekFetching) {
                return;
            }
            this.weekFetching = true;
            final String str2 = this.auth;
            final String str3 = this.cookie;
            new Thread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.38
                @Override // java.lang.Runnable
                public void run() {
                    boolean z2 = false;
                    try {
                        MainActivity mainActivity = MainActivity.this;
                        boolean save = WeekCalendar.save(mainActivity, new SchoolApi(mainActivity, str2, str3).getTeachWeeks(LocalDate.now()));
                        MainActivity.this.weekFetching = false;
                        z2 = save;
                    } catch (Exception unused) {
                        MainActivity.this.weekFetching = false;
                    } catch (Throwable th) {
                        MainActivity.this.weekFetching = false;
                        throw th;
                    }
                    if (z2) {
                        MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.38.1
                            @Override // java.lang.Runnable
                            public void run() {
                                MainActivity.this.updateDateHeader();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public void calibrateSemester() {
        LocalDate semesterAnchor = semesterAnchor();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() { // from class: com.zuwe.smartcourselite.MainActivity.39
            @Override // android.app.DatePickerDialog.OnDateSetListener
            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                LocalDate of = LocalDate.of(i, i2 + MainActivity.W_MEDIUM, i3);
                MainActivity.this.prefs().edit().putString(MainActivity.KEY_SEM_ANCHOR, WeekUtil.monday(of).toString()).apply();
                MainActivity.this.updateDateHeader();
                LocalDate monday = WeekUtil.monday(of);
                MainActivity.this.toast("第 1 周 = " + monday.getMonthValue() + "月" + monday.getDayOfMonth() + "日 起的一周");
            }
        }, semesterAnchor.getYear(), semesterAnchor.getMonthValue() - 1, semesterAnchor.getDayOfMonth()).show();
    }

    public void openWeekOverview() {
        final Intent intent = new Intent(this, (Class<?>) WeekOverviewActivity.class);
        intent.putExtra("monday", WeekUtil.monday(this.viewDate).toString());
        intent.putExtra("week", weekOf(this.viewDate));
        UiPolish.containerTransform(this.weekLabel, new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.40
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m23lambda$openWeekOverview$0$comzuwesmartcourseliteMainActivity(intent);
            }
        });
    }

    void m23lambda$openWeekOverview$0$comzuwesmartcourseliteMainActivity(Intent intent) {
        PageMotion.captureBackdrop(this);
        startActivity(intent);
        overridePendingTransition(W_REGULAR, W_REGULAR);
    }

    public boolean ready() {
        try {
            AuthManager.Credentials load = AuthManager.load(this);
            if (load == null) {
                setStatus("尚无有效本地授权，请先登录");
                toast("请先完成官方账号授权");
                return false;
            }
            this.auth = load.authorizationJwt;
            this.cookie = load.sklTokenJwt;
            return true;
        } catch (Exception e) {
            setStatus("凭证读取失败：" + e.getMessage());
            return false;
        }
    }

    public void startOAuth(boolean z) {
        setStatus(z ? "正在启动官方重新授权…" : "正在启动官方授权页面…");
        PageMotion.captureBackdrop(this);
        startActivityForResult(new Intent(this, (Class<?>) OAuthActivity.class), REQUEST_OAUTH);
        overridePendingTransition(W_REGULAR, W_REGULAR);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQUEST_OAUTH) {
            if (i2 == -1) {
                this.lastData = null;
                prefs().edit().remove(KEY_AUTO_DAY).apply();
                loadSaved();
                return;
            }
            setStatus("授权页面已关闭；尚未确认获得新凭证");
            return;
        }
        if (i != PICK_CAPTURE_ZIP || i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        final Uri data = intent.getData();
        setStatus("正在读取历史抓包 ZIP…");
        this.io.execute(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.41
            @Override // java.lang.Runnable
            public void run() {
                try {
                    final String read = CaptureZipReader.read(MainActivity.this, data);
                    MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.41.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.importCaptureText(read);
                        }
                    });
                } catch (Exception e) {
                    MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.41.2
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.setStatus("ZIP 导入失败：" + e.getMessage());
                            MainActivity.this.toast(e.getMessage());
                        }
                    });
                }
            }
        });
    }

    public void importCaptureText(String str) {
        try {
            AuthManager.ImportResult importCapture = AuthManager.importCapture(this, str);
            this.auth = importCapture.credentials.authorizationJwt;
            this.cookie = importCapture.credentials.sklTokenJwt;
            this.lastData = null;
            prefs().edit().remove(KEY_AUTO_DAY).apply();
            loadSaved();
            setStatus("历史授权数据已加密保存；正在验证…");
        } catch (Exception e) {
            setStatus("识别/保存失败：" + e.getMessage());
            toast(e.getMessage());
        }
    }

    public void chooseCaptureZip() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("application/zip");
        intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"application/zip", "application/x-zip-compressed", "application/octet-stream"});
        startActivityForResult(intent, PICK_CAPTURE_ZIP);
    }

    private void smartStart() {
        LocalDate now = LocalDate.now();
        List<Course> loadCache = loadCache(this.viewDate);
        if (loadCache != null) {
            render(loadCache);
            setStatus(this.viewDate.format(FMT_TITLE) + " · " + cacheAge(this.viewDate));
        }
        if (!now.toString().equals(prefs().getString(KEY_AUTO_DAY, "")) || loadCache == null) {
            autoSyncTodayAndTomorrow(now);
        }
    }

    private void autoSyncTodayAndTomorrow(final LocalDate localDate) {
        if (this.loading) {
            return;
        }
        this.loading = true;
        setBusy(true);
        if (this.lastData == null && loadCache(localDate) == null) {
            showLoading();
        }
        setStatus("正在同步 " + localDate.format(FMT_TITLE) + " 与明天…");
        final LocalDate plusDays = localDate.plusDays(1L);
        this.io.execute(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.42
            @Override // java.lang.Runnable
            public void run() {
                List<Course> list = null;
                Exception exc = null;
                List<Course> list2 = null;
                Exception exc2 = null;
                try {
                    exc = null;
                    list = MainActivity.this.fetchSync(localDate);
                } catch (Exception e) {
                    list = null;
                    exc = e;
                }
                final List<Course> fList = list;
                final Exception fe1 = exc;
                final LocalDate localDate2 = localDate;
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.42.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (fList != null) {
                            MainActivity.this.saveCache(localDate2, fList);
                            if (MainActivity.this.viewDate.equals(localDate2)) {
                                MainActivity.this.render(fList);
                            }
                            MainActivity.this.setStatus(localDate2.format(MainActivity.FMT_TITLE) + " · 已更新，正在补明天…");
                        }
                    }
                });
                try {
                    exc2 = null;
                    list2 = MainActivity.this.fetchSync(plusDays);
                } catch (Exception e2) {
                    list2 = null;
                    exc2 = e2;
                }
                final List<Course> fList2 = list2;
                final Exception fe2 = exc2;
                final LocalDate localDate3 = localDate;
                final LocalDate localDate4 = plusDays;
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.42.2
                    @Override // java.lang.Runnable
                    public void run() {
                        MainActivity.this.onSyncFinished(localDate3, fList, fe1, localDate4, fList2, fe2);
                    }
                });
            }
        });
    }

    public List<Course> fetchSync(LocalDate localDate) throws Exception {
        return new SchoolApi(this, this.auth, this.cookie).getTodayCourses(localDate);
    }

    public void onSyncFinished(LocalDate localDate, List<Course> list, Exception exc, LocalDate localDate2, List<Course> list2, Exception exc2) {
        this.loading = false;
        setBusy(false);
        if (list != null) {
            saveCache(localDate, list);
        }
        if (list2 != null) {
            saveCache(localDate2, list2);
        }
        if (list != null || list2 != null) {
            prefs().edit().putString(KEY_AUTO_DAY, localDate.toString()).apply();
        }
        if (list != null) {
            if (this.viewDate.equals(localDate)) {
                render(list);
            }
            setStatus(localDate.format(FMT_TITLE) + " · 刚刚更新");
            return;
        }
        if (!this.viewDate.equals(localDate) || loadCache(localDate) == null) {
            if (exc == null) {
                exc = exc2;
            }
            if (exc != null) {
                handleApiError(exc, true);
                return;
            }
            return;
        }
        render(loadCache(localDate));
        setStatus(localDate.format(FMT_TITLE) + " · 更新失败，显示本机缓存");
    }

    public void refresh(boolean z) {
        if (this.loading) {
            toast("正在刷新，请稍候…");
            return;
        }
        if (ready()) {
            List<Course> loadCache = loadCache(this.viewDate);
            if (!z && loadCache != null) {
                render(loadCache);
                setStatus(this.viewDate.format(FMT_TITLE) + " · " + cacheAge(this.viewDate));
                return;
            }
            if (loadCache != null) {
                render(loadCache);
            }
            startFetch(this.viewDate);
        }
    }

    private void startFetch(final LocalDate localDate) {
        this.loading = true;
        setBusy(true);
        List<Course> loadCache = loadCache(localDate);
        if (loadCache == null || loadCache.isEmpty()) {
            showLoading();
        }
        setStatus("正在获取 " + localDate.format(FMT_TITLE) + " 的课表…");
        this.io.execute(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.43
            @Override // java.lang.Runnable
            public void run() {
                List<Course> list = null;
                Exception e1 = null;
                try {
                    list = MainActivity.this.fetchSync(localDate);
                } catch (Exception ex1) {
                    e1 = ex1;
                }
                final List<Course> flist = list;
                final Exception ferr = e1;
                final LocalDate localDate2 = localDate;
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.43.1
                    @Override // java.lang.Runnable
                    public void run() {
                        MainActivity.this.onFetchFinished(localDate2, flist, ferr);
                    }
                });
            }
        });
    }

    public void onFetchFinished(LocalDate localDate, List<Course> list, Exception exc) {
        this.loading = false;
        setBusy(false);
        if (list == null) {
            if (exc != null) {
                handleApiError(exc, true);
                return;
            }
            return;
        }
        saveCache(localDate, list);
        if (this.viewDate.equals(localDate)) {
            render(list);
            setStatus(localDate.format(FMT_TITLE) + " · 刚刚更新 · " + list.size() + " 门课程");
        }
    }

    private void setBusy(boolean z) {
        Button button = this.refreshBtn;
        if (button != null) {
            button.setEnabled(!z);
            this.refreshBtn.setText(z ? "正在刷新…" : "刷新课表");
            this.refreshBtn.setAlpha(z ? 0.6f : 1.0f);
            if (z) {
                this.refreshBtn.setRotation(0.0f);
                this.refreshBtn.animate().rotation(360.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
            }
        }
    }

    private void showLoading() {
        LinearLayout linearLayout = this.courseList;
        if (linearLayout == null) {
            return;
        }
        this.skeletonStop = UiPolish.skeleton(linearLayout, 3, UiPolish.withAlpha(this.cardBg, this.dark ? 204 : 217), this.dark ? 822083583 : 1358954495, UiPolish.withAlpha(this.line, 119));
    }

    public void render(List<Course> list) {
        boolean z;
        View view;
        List<ReminderPrefs.Custom> list2;
        ArrayList arrayList;
        Course course;
        this.lastData = list;
        if (this.courseList == null) {
            return;
        }
        Runnable runnable = this.skeletonStop;
        if (runnable != null) {
            runnable.run();
            this.skeletonStop = null;
        }
        this.courseList.removeAllViews();
        this.reminderBells.clear();
        this.courseList.animate().cancel();
        this.courseList.setAlpha(0.0f);
        this.courseList.animate().alpha(1.0f).setDuration(280L).setInterpolator(Motion.ENTER).start();
        scheduleStateTick();
        String format = this.viewDate.format(FMT_TITLE);
        List load = TodoStore.load(this, this.viewDate);
        updateTodoHead(load);
        List<ReminderPrefs.Custom> customs = ReminderPrefs.customs(this);
        int i = W_REGULAR;
        while (true) {
            if (i >= customs.size()) {
                z = false;
                break;
            } else {
                if (Instant.ofEpochMilli(customs.get(i).at).atZone(ZoneId.systemDefault()).toLocalDate().equals(this.viewDate)) {
                    z = true;
                    break;
                }
                i += W_MEDIUM;
            }
        }
        if ((list == null || list.isEmpty()) && load.isEmpty() && !z) {
            renderEmptyState();
            setStatus(format + " · 0 门课程");
            return;
        }
        ArrayList arrayList2 = new ArrayList(list);
        Collections.sort(arrayList2, new Comparator<Course>() { // from class: com.zuwe.smartcourselite.MainActivity.44
            @Override // java.util.Comparator
            public int compare(Course course2, Course course3) {
                return Integer.compare(course2.startSection, course3.startSection);
            }
        });
        int nowMinutes = nowMinutes();
        boolean equals = this.viewDate.equals(LocalDate.now());
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList(arrayList2);
        Collections.sort(arrayList4, new Comparator() { // from class: com.zuwe.smartcourselite.MainActivity.45
            @Override // java.util.Comparator
            public int compare(Object obj, Object obj2) {
                return Integer.compare(MainActivity.this.startMinutes((Course) obj), MainActivity.this.startMinutes((Course) obj2));
            }
        });
        IdentityHashMap identityHashMap = new IdentityHashMap();
        ArrayList arrayList5 = new ArrayList();
        int i2 = W_REGULAR;
        while (i2 < load.size()) {
            TodoStore.Item item = (TodoStore.Item) load.get(i2);
            int minutesOf = TodoStore.minutesOf(item.time);
            int i3 = W_REGULAR;
            Course course2 = null;
            while (i3 < arrayList4.size()) {
                Course course3 = (Course) arrayList4.get(i3);
                List list3 = load;
                if (minutesOf >= startMinutes(course3) && minutesOf <= endMinutes(course3)) {
                    course2 = course3;
                }
                i3 += W_MEDIUM;
                load = list3;
            }
            List list4 = load;
            if (course2 == null) {
                arrayList5.add(item);
            } else {
                ArrayList arrayList6 = (ArrayList) identityHashMap.get(course2);
                if (arrayList6 == null) {
                    arrayList6 = new ArrayList();
                    identityHashMap.put(course2, arrayList6);
                }
                arrayList6.add(item);
            }
            i2 += W_MEDIUM;
            load = list4;
        }
        List list5 = load;
        ArrayList arrayList7 = new ArrayList();
        arrayList7.addAll(arrayList4);
        arrayList7.addAll(arrayList5);
        IdentityHashMap identityHashMap2 = new IdentityHashMap();
        List<ReminderPrefs.Custom> customs2 = ReminderPrefs.customs(this);
        int i4 = W_REGULAR;
        while (i4 < customs2.size()) {
            ReminderPrefs.Custom custom = customs2.get(i4);
            if (Instant.ofEpochMilli(custom.at).atZone(ZoneId.systemDefault()).toLocalDate().equals(this.viewDate)) {
                int entryMinutes = entryMinutes(custom);
                int i5 = W_REGULAR;
                while (true) {
                    if (i5 >= arrayList4.size()) {
                        list2 = customs2;
                        arrayList = arrayList2;
                        course = null;
                        break;
                    }
                    course = (Course) arrayList4.get(i5);
                    list2 = customs2;
                    int startMinutes = startMinutes(course);
                    arrayList = arrayList2;
                    int endMinutes = endMinutes(course);
                    if (startMinutes > 0 && entryMinutes >= startMinutes && entryMinutes <= endMinutes) {
                        break;
                    }
                    i5 += W_MEDIUM;
                    customs2 = list2;
                    arrayList2 = arrayList;
                }
                if (course != null) {
                    List list6 = (List) identityHashMap2.get(course);
                    if (list6 == null) {
                        list6 = new ArrayList();
                        identityHashMap2.put(course, list6);
                    }
                    list6.add(custom);
                }
            } else {
                list2 = customs2;
                arrayList = arrayList2;
            }
            i4 += W_MEDIUM;
            customs2 = list2;
            arrayList2 = arrayList;
        }
        ArrayList arrayList8 = arrayList2;
        List<ReminderPrefs.Custom> customs3 = ReminderPrefs.customs(this);
        for (int i6 = W_REGULAR; i6 < customs3.size(); i6 += W_MEDIUM) {
            ReminderPrefs.Custom custom2 = customs3.get(i6);
            if (Instant.ofEpochMilli(custom2.at).atZone(ZoneId.systemDefault()).toLocalDate().equals(this.viewDate)) {
                arrayList7.add(custom2);
            }
        }
        for (int size = arrayList7.size() - W_MEDIUM; size >= 0; size--) {
            Object obj = arrayList7.get(size);
            if (obj instanceof ReminderPrefs.Custom) {
                int entryMinutes2 = entryMinutes(obj);
                int i7 = W_REGULAR;
                while (true) {
                    if (i7 < arrayList4.size()) {
                        Course course4 = (Course) arrayList4.get(i7);
                        int startMinutes2 = startMinutes(course4);
                        int endMinutes2 = endMinutes(course4);
                        if (startMinutes2 > 0 && entryMinutes2 >= startMinutes2 && entryMinutes2 <= endMinutes2 && identityHashMap2.get(course4) != null) {
                            arrayList7.remove(size);
                            break;
                        }
                        i7 += W_MEDIUM;
                    }
                }
            }
        }
        Collections.sort(arrayList7, new Comparator() { // from class: com.zuwe.smartcourselite.MainActivity.46
            @Override // java.util.Comparator
            public int compare(Object obj2, Object obj3) {
                return Integer.compare(MainActivity.this.entryMinutes(obj2), MainActivity.this.entryMinutes(obj3));
            }
        });
        for (int i8 = W_REGULAR; i8 < arrayList7.size(); i8 += W_MEDIUM) {
            Object obj2 = arrayList7.get(i8);
            if (i8 > 0) {
                this.courseList.addView(separatorRow());
            }
            if (obj2 instanceof Course) {
                Course course5 = (Course) obj2;
                this.inlineTodos = (List) identityHashMap.get(course5);
                this.inlineCustoms = (List) identityHashMap2.get(course5);
                view = timelineRow(course5, nowMinutes, equals);
                this.courseList.addView(view);
            } else if (obj2 instanceof ReminderPrefs.Custom) {
                view = customRow((ReminderPrefs.Custom) obj2);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                layoutParams.topMargin = dp(4.0f);
                layoutParams.bottomMargin = dp(4.0f);
                this.courseList.addView(view, layoutParams);
            } else {
                TodoStore.Item item2 = (TodoStore.Item) obj2;
                View view2 = todoRow(item2, false);
                if (isPendingReveal(item2)) {
                    view2.setAlpha(0.0f);
                    this.pendingTodoRow = view2;
                }
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
                layoutParams2.topMargin = dp(4.0f);
                layoutParams2.bottomMargin = dp(4.0f);
                this.courseList.addView(view2, layoutParams2);
                view = view2;
            }
            arrayList3.add(view);
        }
        View view3 = this.pendingTodoRow;
        if (view3 != null) {
            arrayList3.remove(view3);
        }
        if (this.pendingDayReveal && !this.quietRender) {
            for (int i9 = W_REGULAR; i9 < arrayList3.size(); i9 += W_MEDIUM) {
                View view4 = (View) arrayList3.get(i9);
                view4.setAlpha(1.0f);
                view4.setTranslationY(0.0f);
                view4.setRotationX(0.0f);
                view4.setScaleX(1.0f);
                view4.setScaleY(1.0f);
            }
        } else if (!this.quietRender) {
            playCourseEntrance(arrayList3);
        }
        playPendingTodoReveal();
        revealScheduleFromChip();
        StringBuilder sb = new StringBuilder();
        sb.append(format);
        sb.append(" · ");
        sb.append(arrayList8.size());
        sb.append(" 门课程");
        sb.append(list5.isEmpty() ? "" : " · " + list5.size() + " 项待办");
        setStatus(sb.toString());
        maybeRemind();
        if (!this.revealBellsOnRender || this.reminderBells.isEmpty()) {
            return;
        }
        this.revealBellsOnRender = false;
        playBellReveal();
    }

    private boolean isPendingReveal(TodoStore.Item item) {
        TodoStore.Item item2 = this.pendingTodoReveal;
        return item2 != null && item2.time.equals(item.time) && this.pendingTodoReveal.text.equals(item.text);
    }

    private void playPendingTodoReveal() {
        View view = this.pendingTodoRow;
        this.pendingTodoRow = null;
        this.pendingTodoReveal = null;
        if (view == null) {
            return;
        }
        view.post(new AnonymousClass47(view));
    }

    /* renamed from: com.zuwe.smartcourselite.MainActivity$47, reason: invalid class name */
    class AnonymousClass47 implements Runnable {
        final /* synthetic */ View val$row;

        AnonymousClass47(View view) {
            this.val$row = view;
        }

        @Override // java.lang.Runnable
        public void run() {
            final int width = this.val$row.getWidth() > 0 ? this.val$row.getWidth() : MainActivity.this.getResources().getDisplayMetrics().widthPixels;
            this.val$row.setAlpha(0.0f);
            int[] iArr = new int[MainActivity.W_SEMIBOLD];
            this.val$row.getLocationInWindow(iArr);
            int[] iArr2 = new int[MainActivity.W_SEMIBOLD];
            MainActivity.this.courseScroll.getLocationInWindow(iArr2);
            int height = (iArr[MainActivity.W_MEDIUM] + (this.val$row.getHeight() / MainActivity.W_SEMIBOLD)) - (iArr2[MainActivity.W_MEDIUM] + (MainActivity.this.courseScroll.getHeight() / MainActivity.W_SEMIBOLD));
            final Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.47.1
                @Override // java.lang.Runnable
                public void run() {
                    AnonymousClass47.this.val$row.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.47.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.slideTodoIn(AnonymousClass47.this.val$row, width);
                        }
                    }, 400L);
                }
            };
            if (Math.abs(height) <= MainActivity.this.dp(6.0f)) {
                runnable.run();
                return;
            }
            ValueAnimator ofInt = ValueAnimator.ofInt(MainActivity.this.courseScroll.getScrollY(), MainActivity.this.courseScroll.getScrollY() + height);
            ofInt.setDuration(520L);
            ofInt.setInterpolator(Motion.ENTER);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.47.2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MainActivity.this.courseScroll.scrollTo(MainActivity.W_REGULAR, ((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            });
            ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.MainActivity.47.3
                boolean done = false;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (this.done) {
                        return;
                    }
                    this.done = true;
                    runnable.run();
                }
            });
            ofInt.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void slideTodoIn(final View view, int i) {
        final float dp = i + dp(28.0f);
        view.setTranslationX(-dp);
        view.setAlpha(0.0f);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(780L);
        ofFloat.setInterpolator(Motion.ENTER);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.48
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                view.setTranslationX((-dp) * (1.0f - floatValue));
                view.setAlpha(Math.min(1.0f, floatValue * 2.4f));
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.MainActivity.49
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setTranslationX(0.0f);
                view.setAlpha(1.0f);
                MainActivity mainActivity = MainActivity.this;
                TodoFx.notice(mainActivity, "已加入一条待办 🥳", mainActivity.ink, MainActivity.this.cardBg, MainActivity.this.line);
            }
        });
        ofFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int entryMinutes(Object obj) {
        if (obj instanceof Course) {
            return startMinutes((Course) obj);
        }
        if (obj instanceof ReminderPrefs.Custom) {
            LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(((ReminderPrefs.Custom) obj).at), ZoneId.systemDefault());
            return (ofInstant.getHour() * 60) + ofInstant.getMinute();
        }
        int minutesOf = TodoStore.minutesOf(((TodoStore.Item) obj).time);
        if (minutesOf < 0) {
            return 1440;
        }
        return minutesOf;
    }

    private View todoSectionHead() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        TextView label = label("今日待办", 21.0f, this.ink, W_SEMIBOLD);
        this.todoTitle = label;
        linearLayout2.addView(label, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(SvgIcon.view(this, SvgIcon.PLUS, this.green, this.dark ? 21 : 20), new FrameLayout.LayoutParams(dp(21.0f), dp(21.0f), 17));
        frameLayout.setContentDescription("添加待办");
        frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.50
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showAddTodoDialog();
            }
        });
        Haptics.attach(frameLayout);
        linearLayout2.addView(frameLayout, new LinearLayout.LayoutParams(dp(32.0f), dp(32.0f)));
        linearLayout.addView(linearLayout2);
        this.todoTrack = new FrameLayout(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(1.0f));
        gradientDrawable.setColor(UiPolish.withAlpha(this.ink, this.dark ? 26 : 16));
        this.todoTrack.setBackground(gradientDrawable);
        this.todoTrackFill = new View(this);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setCornerRadius(dp(1.0f));
        gradientDrawable2.setColor(this.green);
        this.todoTrackFill.setBackground(gradientDrawable2);
        this.todoTrack.addView(this.todoTrackFill, new FrameLayout.LayoutParams(W_REGULAR, -1));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(2.0f));
        layoutParams.topMargin = dp(8.0f);
        linearLayout.addView(this.todoTrack, layoutParams);
        TextView txt = txt("今天还没有待办", 11, this.faint, false);
        this.todoMeta = txt;
        linearLayout.addView(txt, top(7));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_REGULAR);
        linearLayout3.setGravity(16);
        linearLayout3.setPadding(dp(14.0f), dp(13.0f), dp(14.0f), dp(13.0f));
        GradientDrawable gradientDrawable3 = new GradientDrawable();
        gradientDrawable3.setCornerRadius(dp(12.0f));
        gradientDrawable3.setColor(W_REGULAR);
        gradientDrawable3.setStroke(dp(1.0f), UiPolish.withAlpha(this.ink, this.dark ? 44 : 30), dp(4.0f), dp(4.0f));
        linearLayout3.setBackground(gradientDrawable3);
        linearLayout3.addView(SvgIcon.view(this, SvgIcon.PLUS, this.faint, 14), new LinearLayout.LayoutParams(dp(14.0f), dp(14.0f)));
        TextView txt2 = txt("添加今天要做的事…", 13, this.faint, false);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f);
        layoutParams2.leftMargin = dp(8.0f);
        linearLayout3.addView(txt2, layoutParams2);
        linearLayout3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.51
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showAddTodoDialog();
            }
        });
        Haptics.attach(linearLayout3);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp(10.0f);
        linearLayout.addView(linearLayout3, layoutParams3);
        this.todoEmpty = linearLayout3;
        return linearLayout;
    }

    private void updateTodoHead(List list) {
        FrameLayout frameLayout;
        int i = W_REGULAR;
        final int size = list == null ? W_REGULAR : list.size();
        int i2 = W_REGULAR;
        for (int i3 = W_REGULAR; i3 < size; i3 += W_MEDIUM) {
            if (((TodoStore.Item) list.get(i3)).done) {
                i2 += W_MEDIUM;
            }
        }
        TextView textView = this.todoMeta;
        if (textView != null) {
            textView.setText(size == 0 ? "今天还没有待办" : size + " 项 · 已完成 " + i2);
        }
        FrameLayout frameLayout2 = this.todoTrack;
        if (frameLayout2 != null) {
            frameLayout2.setVisibility(size == 0 ? 8 : W_REGULAR);
        }
        if (this.todoTrackFill != null && (frameLayout = this.todoTrack) != null) {
            final int doneCount = i2;
            frameLayout.post(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.52
                @Override // java.lang.Runnable
                public void run() {
                    float width = MainActivity.this.todoTrack.getWidth();
                    int i4 = size;
                    int i5 = (int) (width * (i4 == 0 ? 0.0f : doneCount / i4));
                    ViewGroup.LayoutParams layoutParams = MainActivity.this.todoTrackFill.getLayoutParams();
                    layoutParams.width = Math.max(MainActivity.W_REGULAR, i5);
                    MainActivity.this.todoTrackFill.setLayoutParams(layoutParams);
                }
            });
        }
        View view = this.todoEmpty;
        if (view != null) {
            if (size != 0) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private int todoAccent(TodoStore.Item item) {
        int[] iArr = {-1007556, -2074027, -11887648, -12603782, -6589728, -2065496};
        int[] iArr2 = {-544180, -37016, -11032080, -11544678, -5206793, -1011778};
        if (this.dark) {
            iArr = iArr2;
        }
        return iArr[((item.time + "|" + item.text).hashCode() & Integer.MAX_VALUE) % iArr.length];
    }

    private View customInlineRow(ReminderPrefs.Custom custom) {
        LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(custom.at), ZoneId.systemDefault());
        String clockOfMinutes = ClassTimeTable.clockOfMinutes((ofInstant.getHour() * 60) + ofInstant.getMinute());
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(14.0f), dp(6.0f), dp(16.0f), dp(6.0f));
        View view = new View(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(1.0f));
        gradientDrawable.setColor(UiPolish.withAlpha(this.green, 112));
        view.setBackground(gradientDrawable);
        linearLayout.addView(view, new LinearLayout.LayoutParams(dp(2.0f), -1));
        TextView label = label(clockOfMinutes, 13.0f, this.muted, W_REGULAR);
        label.setTypeface(Typeface.MONOSPACE);
        label.setGravity(5);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(44.0f), -2);
        layoutParams.leftMargin = dp(24.0f);
        linearLayout.addView(label, layoutParams);
        TextView label2 = label(custom.title, 13.5f, this.muted, W_REGULAR);
        label2.setMaxLines(W_SEMIBOLD);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f);
        layoutParams2.leftMargin = dp(12.0f);
        linearLayout.addView(label2, layoutParams2);
        linearLayout.addView(SvgIcon.view(this, SvgIcon.BELL, this.faint, 13), new LinearLayout.LayoutParams(dp(13.0f), dp(13.0f)));
        return linearLayout;
    }

    private View customRow(ReminderPrefs.Custom custom) {
        LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(custom.at), ZoneId.systemDefault());
        String clockOfMinutes = ClassTimeTable.clockOfMinutes((ofInstant.getHour() * 60) + ofInstant.getMinute());
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(14.0f), dp(10.0f), dp(16.0f), dp(10.0f));
        View view = new View(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(1.0f));
        gradientDrawable.setColor(UiPolish.withAlpha(this.green, 176));
        view.setBackground(gradientDrawable);
        linearLayout.addView(view, new LinearLayout.LayoutParams(dp(2.0f), -1));
        TextView label = label(clockOfMinutes, 16.0f, this.ink, W_SEMIBOLD);
        label.setTypeface(Typeface.MONOSPACE, W_MEDIUM);
        label.setGravity(5);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(56.0f), -2);
        layoutParams.leftMargin = dp(12.0f);
        linearLayout.addView(label, layoutParams);
        TextView label2 = label(custom.title, 15.0f, this.ink, W_MEDIUM);
        label2.setMaxLines(W_SEMIBOLD);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f);
        layoutParams2.leftMargin = dp(12.0f);
        linearLayout.addView(label2, layoutParams2);
        linearLayout.addView(SvgIcon.view(this, SvgIcon.BELL, this.green, 15), new LinearLayout.LayoutParams(dp(15.0f), dp(15.0f)));
        return linearLayout;
    }

    private View todoRow(final TodoStore.Item item, boolean z) {
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        linearLayout.setMinimumHeight(dp(44.0f));
        linearLayout.setPadding(dp(14.0f), dp(8.0f), dp(16.0f), dp(8.0f));
        View view = new View(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(1.0f));
        gradientDrawable.setColor(UiPolish.withAlpha(this.ink, this.dark ? 96 : 64));
        view.setBackground(gradientDrawable);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(2.0f), -1);
        layoutParams.leftMargin = W_REGULAR;
        linearLayout.addView(view, layoutParams);
        boolean z2 = !z;
        TextView txt = txt(item.time, z2 ? 16 : 13, item.done ? this.faint : z2 ? this.ink : this.muted, z2);
        if (!z2) {
            txt.setTypeface(Typeface.MONOSPACE);
        }
        txt.setGravity(5);
        txt.setIncludeFontPadding(false);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(z2 ? 56.0f : 44.0f), -2);
        layoutParams2.leftMargin = dp(z2 ? 12.0f : 24.0f);
        linearLayout.addView(txt, layoutParams2);
        FrameLayout frameLayout = new FrameLayout(this);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setShape(W_MEDIUM);
        gradientDrawable2.setColor(item.done ? this.green : todoAccent(item));
        frameLayout.setBackground(gradientDrawable2);
        if (item.done) {
            frameLayout.addView(SvgIcon.view(this, SvgIcon.CHECK, this.onGreen, 8), new FrameLayout.LayoutParams(dp(10.0f), dp(10.0f), 17));
        }
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(dp(16.0f), dp(16.0f));
        layoutParams3.leftMargin = dp(10.0f);
        linearLayout.addView(frameLayout, layoutParams3);
        TextView label = label(item.text, 15.0f, item.done ? this.faint : this.ink, W_MEDIUM);
        label.setMaxLines(3);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f);
        layoutParams4.leftMargin = dp(10.0f);
        linearLayout.addView(label, layoutParams4);
        if (todoReminderOn(item)) {
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(dp(13.0f), dp(13.0f));
            layoutParams5.leftMargin = dp(6.0f);
            layoutParams5.gravity = 16;
            linearLayout.addView(reminderBell("t:" + item.time + "|" + item.text), layoutParams5);
        }
        final boolean[] zArr = {false};
        final float[] fArr = {W_REGULAR};
        final Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.53
            @Override // java.lang.Runnable
            public void run() {
                zArr[MainActivity.W_REGULAR] = true;
                Haptics.confirm(linearLayout);
                MainActivity.this.confirmDeleteTodo(item);
            }
        };
        linearLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.MainActivity.54
            /* JADX WARN: Code restructure failed: missing block: B:8:0x000e, code lost:
            
                if (r0 != 3) goto L16;
             */
            @Override // android.view.View.OnTouchListener
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct add '--show-bad-code' argument
            */
            public boolean onTouch(android.view.View r5, android.view.MotionEvent r6) {
                /*
                    r4 = this;
                    int r0 = r6.getActionMasked()
                    r1 = 0
                    if (r0 == 0) goto L54
                    r2 = 1
                    if (r0 == r2) goto L31
                    r2 = 2
                    if (r0 == r2) goto L11
                    r6 = 3
                    if (r0 == r6) goto L31
                    goto L7f
                L11:
                    float r6 = r6.getY()
                    float[] r0 = r3
                    r0 = r0[r1]
                    float r6 = r6 - r0
                    float r6 = java.lang.Math.abs(r6)
                    com.zuwe.smartcourselite.MainActivity r0 = com.zuwe.smartcourselite.MainActivity.this
                    r2 = 1094713344(0x41400000, float:12.0)
                    int r0 = r0.dp(r2)
                    float r0 = (float) r0
                    int r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
                    if (r6 <= 0) goto L7f
                    java.lang.Runnable r6 = r4
                    r5.removeCallbacks(r6)
                    goto L7f
                L31:
                    java.lang.Runnable r6 = r4
                    r5.removeCallbacks(r6)
                    android.view.ViewPropertyAnimator r5 = r5.animate()
                    r6 = 1065353216(0x3f800000, float:1.0)
                    android.view.ViewPropertyAnimator r5 = r5.scaleX(r6)
                    android.view.ViewPropertyAnimator r5 = r5.scaleY(r6)
                    r2 = 180(0xb4, double:8.9E-322)
                    android.view.ViewPropertyAnimator r5 = r5.setDuration(r2)
                    android.view.animation.PathInterpolator r6 = com.zuwe.smartcourselite.Motion.SPRING
                    android.view.ViewPropertyAnimator r5 = r5.setInterpolator(r6)
                    r5.start()
                    goto L7f
                L54:
                    boolean[] r0 = r2
                    r0[r1] = r1
                    float[] r0 = r3
                    float r6 = r6.getY()
                    r0[r1] = r6
                    android.view.ViewPropertyAnimator r6 = r5.animate()
                    r0 = 1065101558(0x3f7c28f6, float:0.985)
                    android.view.ViewPropertyAnimator r6 = r6.scaleX(r0)
                    android.view.ViewPropertyAnimator r6 = r6.scaleY(r0)
                    r2 = 90
                    android.view.ViewPropertyAnimator r6 = r6.setDuration(r2)
                    r6.start()
                    java.lang.Runnable r6 = r4
                    r2 = 600(0x258, double:2.964E-321)
                    r5.postDelayed(r6, r2)
                L7f:
                    return r1
                */
                throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.MainActivity.AnonymousClass54.onTouch(android.view.View, android.view.MotionEvent):boolean");
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.55
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                boolean[] zArr2 = zArr;
                if (!zArr2[MainActivity.W_REGULAR]) {
                    MainActivity.this.toggleTodo(item);
                } else {
                    zArr2[MainActivity.W_REGULAR] = false;
                }
            }
        });
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleTodo(TodoStore.Item item) {
        List<TodoStore.Item> load = TodoStore.load(this, this.viewDate);
        ArrayList arrayList = new ArrayList();
        boolean z = false;
        for (int i = W_REGULAR; i < load.size(); i += W_MEDIUM) {
            TodoStore.Item item2 = load.get(i);
            if (!z && item2.time.equals(item.time) && item2.text.equals(item.text)) {
                arrayList.add(new TodoStore.Item(item2.time, item2.text, !item2.done));
                z = true;
            } else {
                arrayList.add(item2);
            }
        }
        if (z) {
            TodoStore.save(this, this.viewDate, arrayList);
            boolean isEmpty = !arrayList.isEmpty();
            for (int i2 = W_REGULAR; i2 < arrayList.size(); i2 += W_MEDIUM) {
                if (!((TodoStore.Item) arrayList.get(i2)).done) {
                    isEmpty = false;
                }
            }
            boolean z2 = false;
            for (int i3 = W_REGULAR; i3 < arrayList.size(); i3 += W_MEDIUM) {
                if (((TodoStore.Item) arrayList.get(i3)).time.equals(item.time) && ((TodoStore.Item) arrayList.get(i3)).text.equals(item.text)) {
                    z2 = ((TodoStore.Item) arrayList.get(i3)).done;
                }
            }
            if (z2) {
                TodoFx.buzz(this.courseScroll);
            } else {
                Haptics.confirm(this.courseScroll);
            }
            render(this.lastData);
            if (isEmpty && z2) {
                TodoFx.allDone(this, this.ink, this.muted, this.cardBg, this.line);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void confirmDeleteTodo(final TodoStore.Item item) {
        new AlertDialog.Builder(this).setTitle("删除待办").setMessage(item.time + "  " + item.text).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("删除", new DialogInterface.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.56
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity mainActivity = MainActivity.this;
                List<TodoStore.Item> load = TodoStore.load(mainActivity, mainActivity.viewDate);
                ArrayList arrayList = new ArrayList();
                boolean z = false;
                for (int i2 = MainActivity.W_REGULAR; i2 < load.size(); i2 += MainActivity.W_MEDIUM) {
                    TodoStore.Item item2 = load.get(i2);
                    if (!z && item2.time.equals(item.time) && item2.text.equals(item.text)) {
                        z = true;
                    } else {
                        arrayList.add(item2);
                    }
                }
                MainActivity mainActivity2 = MainActivity.this;
                TodoStore.save(mainActivity2, mainActivity2.viewDate, arrayList);
                MainActivity mainActivity3 = MainActivity.this;
                mainActivity3.render(mainActivity3.lastData);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAddTodoDialog() {
        int parseColor;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(W_MEDIUM);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(18.0f), dp(8.0f), dp(18.0f), dp(18.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        if (this.dark) {
            parseColor = Color.parseColor("#1B1D18");
        } else {
            parseColor = Color.parseColor("#F8F7F3");
        }
        gradientDrawable.setColor(parseColor);
        float dp = dp(26.0f);
        gradientDrawable.setCornerRadii(new float[]{dp, dp, dp, dp, W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR});
        linearLayout.setBackground(gradientDrawable);
        View view = new View(this);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setCornerRadius(dp(2.0f));
        gradientDrawable2.setColor(UiPolish.withAlpha(this.ink, 46));
        view.setBackground(gradientDrawable2);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(view, new FrameLayout.LayoutParams(dp(36.0f), dp(4.0f), 17));
        linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(-1, dp(16.0f)));
        LocalTime now = LocalTime.now();
        final int hour = (now.getHour() * 60) + now.getMinute();
        FrameLayout frameLayout2 = new FrameLayout(this);
        TextView txt = txt("✕", 22, this.muted, false);
        txt.setGravity(19);
        txt.setPadding(dp(6.0f), dp(6.0f), dp(10.0f), dp(6.0f));
        txt.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.57
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                dialog.dismiss();
            }
        });
        frameLayout2.addView(txt, new FrameLayout.LayoutParams(-2, -2, 19));
        TextView txt2 = txt("添加待办", 17, this.ink, true);
        txt2.setGravity(17);
        frameLayout2.addView(txt2, new FrameLayout.LayoutParams(-2, -2, 17));
        TextView txt3 = txt("✓", 26, this.green, false);
        txt3.setGravity(21);
        txt3.setPadding(dp(10.0f), dp(4.0f), dp(6.0f), dp(4.0f));
        frameLayout2.addView(txt3, new FrameLayout.LayoutParams(-2, -2, 21));
        Haptics.attach(txt);
        Haptics.attach(txt3);
        linearLayout.addView(frameLayout2, new LinearLayout.LayoutParams(-1, dp(40.0f)));
        final TextView txt4 = txt("", 13, this.muted, false);
        txt4.setGravity(17);
        linearLayout.addView(txt4, top(4));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(17);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_MEDIUM);
        TextView txt5 = txt("时", 11, this.faint, false);
        txt5.setGravity(17);
        linearLayout3.addView(txt5);
        final WheelPicker wheelPicker = new WheelPicker(this, 24, now.getHour(), this.ink, this.faint, 52, 172);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(52.0f), dp(172.0f));
        layoutParams.topMargin = dp(2.0f);
        linearLayout3.addView(wheelPicker, layoutParams);
        linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(W_MEDIUM);
        TextView txt6 = txt("分", 11, this.faint, false);
        txt6.setGravity(17);
        linearLayout4.addView(txt6);
        final WheelPicker wheelPicker2 = new WheelPicker(this, 60, W_REGULAR, this.ink, this.faint, 62, 172);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(62.0f), dp(172.0f));
        layoutParams2.topMargin = dp(2.0f);
        linearLayout4.addView(wheelPicker2, layoutParams2);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.leftMargin = dp(26.0f);
        linearLayout2.addView(linearLayout4, layoutParams3);
        linearLayout.addView(linearLayout2, top(W_SEMIBOLD));
        final Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.58
            @Override // java.lang.Runnable
            public void run() {
                String str;
                int value = ((wheelPicker.value() * 60) + wheelPicker2.value()) - hour;
                if (value == 0) {
                    str = "就是现在";
                } else if (value >= 0) {
                    int i = value / 60;
                    int i2 = value % 60;
                    if (i > 0 && i2 > 0) {
                        str = i + " 小时 " + i2 + " 分钟后";
                    } else if (i > 0) {
                        str = i + " 小时后";
                    } else {
                        str = i2 + " 分钟后";
                    }
                } else if (value == -1) {
                    str = "已过去 1 分钟";
                } else {
                    str = "已过去 " + (-value) + " 分钟";
                }
                txt4.setText(str);
            }
        };
        wheelPicker.setOnChange(new WheelPicker.OnChange() { // from class: com.zuwe.smartcourselite.MainActivity.59
            @Override // com.zuwe.smartcourselite.WheelPicker.OnChange
            public void onChanged(int i) {
                runnable.run();
            }
        });
        wheelPicker2.setOnChange(new WheelPicker.OnChange() { // from class: com.zuwe.smartcourselite.MainActivity.60
            @Override // com.zuwe.smartcourselite.WheelPicker.OnChange
            public void onChanged(int i) {
                runnable.run();
            }
        });
        runnable.run();
        final EditText editText = new EditText(this);
        editText.setSingleLine(false);
        editText.setMaxLines(3);
        editText.setTextSize(16.0f);
        editText.setTextColor(this.ink);
        editText.setHintTextColor(this.faint);
        editText.setHint("要做什么");
        editText.setBackground(inputBackground());
        editText.setPadding(dp(14.0f), dp(12.0f), dp(14.0f), dp(12.0f));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, dp(52.0f));
        layoutParams4.topMargin = dp(16.0f);
        linearLayout.addView(editText, layoutParams4);
        txt3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.61
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                String trim = editText.getText().toString().trim();
                if (trim.isEmpty()) {
                    Toast.makeText(MainActivity.this, "写点要做什么", MainActivity.W_REGULAR).show();
                    return;
                }
                String format = String.format(Locale.US, "%02d:%02d", Integer.valueOf(wheelPicker.value()), Integer.valueOf(wheelPicker2.value()));
                MainActivity mainActivity = MainActivity.this;
                ArrayList arrayList = new ArrayList(TodoStore.load(mainActivity, mainActivity.viewDate));
                TodoStore.Item item = new TodoStore.Item(format, trim, false);
                arrayList.add(item);
                MainActivity mainActivity2 = MainActivity.this;
                TodoStore.save(mainActivity2, mainActivity2.viewDate, arrayList);
                Haptics.confirm(view2);
                TodoFx.buzz(view2);
                MainActivity.this.pendingTodoReveal = item;
                dialog.dismiss();
                MainActivity mainActivity3 = MainActivity.this;
                mainActivity3.render(mainActivity3.lastData);
            }
        });
        dialog.setContentView(linearLayout);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(W_REGULAR));
            window.setLayout(-1, -2);
            window.setGravity(80);
            window.setSoftInputMode(16);
        }
        linearLayout.setTranslationY(dp(60.0f));
        linearLayout.setAlpha(0.0f);
        linearLayout.animate().translationY(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0513, code lost:
    
        if (r2.isIgnoringBatteryOptimizations(getPackageName()) != false) goto L47;
     */
    /* JADX WARN: Removed duplicated region for block: B:101:0x05b5  */
    /* JADX WARN: Removed duplicated region for block: B:103:0x058a  */
    /* JADX WARN: Removed duplicated region for block: B:104:0x0526  */
    /* JADX WARN: Removed duplicated region for block: B:105:0x051f  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x051c  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0523  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x0537  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x057c  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x05a7  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x05f4  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x05f7  */
    /* JADX WARN: Removed duplicated region for block: B:79:0x067d  */
    /* JADX WARN: Removed duplicated region for block: B:81:0x0685  */
    /* JADX WARN: Removed duplicated region for block: B:85:0x06fd  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x0765  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0688 A[Catch: Exception -> 0x06e5, TryCatch #2 {Exception -> 0x06e5, blocks: (B:77:0x0673, B:82:0x06d5, B:93:0x0688, B:95:0x06b9, B:97:0x067f), top: B:76:0x0673 }] */
    /* JADX WARN: Removed duplicated region for block: B:97:0x067f A[Catch: Exception -> 0x06e5, TryCatch #2 {Exception -> 0x06e5, blocks: (B:77:0x0673, B:82:0x06d5, B:93:0x0688, B:95:0x06b9, B:97:0x067f), top: B:76:0x0673 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void showReminderSettings() {
        /*
            Method dump skipped, instructions count: 1955
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.MainActivity.showReminderSettings():void");
    }

    private void attachSheetDrag(final View view, final ScrollView scrollView, final Dialog dialog) {
        final float dp = dp(44.0f);
        scrollView.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.MainActivity.79
            private float downY;
            private boolean dragging;
            private boolean startedFromGrab;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    this.downY = motionEvent.getRawY();
                    this.dragging = false;
                    this.startedFromGrab = motionEvent.getY() < dp;
                    return false;
                }
                if (actionMasked != MainActivity.W_MEDIUM) {
                    if (actionMasked == MainActivity.W_SEMIBOLD) {
                        if (!this.startedFromGrab) {
                            return false;
                        }
                        float rawY = motionEvent.getRawY() - this.downY;
                        if (!this.dragging) {
                            if (rawY <= MainActivity.this.dp(12.0f) || scrollView.getScrollY() != 0) {
                                return false;
                            }
                            this.dragging = true;
                        }
                        view.setTranslationY(Math.max(0.0f, rawY * 0.92f));
                        return true;
                    }
                    if (actionMasked != 3) {
                        return false;
                    }
                }
                if (!this.startedFromGrab || !this.dragging) {
                    return false;
                }
                float translationY = view.getTranslationY();
                this.dragging = false;
                this.startedFromGrab = false;
                if (translationY > MainActivity.this.dp(96.0f)) {
                    dialog.dismiss();
                } else {
                    view.animate().translationY(0.0f).setDuration(180L).setInterpolator(Motion.ENTER).start();
                }
                return true;
            }
        });
    }

    private TextView panelSection(String str) {
        TextView txt = txt(str, 12, this.muted, true);
        txt.setLetterSpacing(0.06f);
        return txt;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void paintLeadChips(List<TextView> list, int i, MainActivity mainActivity) {
        int i2;
        for (int i3 = W_REGULAR; i3 < list.size(); i3 += W_MEDIUM) {
            TextView textView = list.get(i3);
            boolean z = ReminderPrefs.LEADS[i3] == i;
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(mainActivity.dp(10.0f));
            if (z) {
                i2 = UiPolish.withAlpha(mainActivity.green, 42);
            } else {
                i2 = mainActivity.bgAlt;
            }
            gradientDrawable.setColor(i2);
            textView.setBackground(gradientDrawable);
            textView.setTextColor(z ? mainActivity.green : mainActivity.muted);
        }
    }

    private void showReminder(String str, String str2, String str3) {
        if (isFinishing()) {
            return;
        }
        ReminderAlert.show(this, str, str2, str3, this.ink, this.muted, this.cardBg, this.line, this.green);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleRemindersSafe() {
        try {
            scheduleReminders();
        } catch (Exception e) {
            ReminderPrefs.pushLog(this, "排程异常：" + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private void scheduleReminders() {
        LocalDate now = LocalDate.now();
        boolean activeOn = ReminderPrefs.activeOn(this, now);
        int i = W_MEDIUM;
        boolean z = activeOn && this.viewDate.equals(now);
        long epochMilli = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        ArrayList arrayList = new ArrayList();
        if (z && this.lastData != null) {
            int i2 = W_REGULAR;
            while (i2 < this.lastData.size()) {
                Course course = this.lastData.get(i2);
                if (course.startSection >= i) {
                    int itemLead = ReminderPrefs.itemLead(this, "c:" + course.name + ":" + course.startSection);
                    if (itemLead < 0) {
                        if (ReminderPrefs.courseGlobalOn(this)) {
                            itemLead = ReminderPrefs.courseLead(this);
                        }
                    }
                    arrayList.add(new String[]{String.valueOf(((ClassTimeTable.startMinutes(course.startSection) - itemLead) * 60000) + epochMilli), "c:" + course.name + ":" + course.startSection, "上课提醒 · " + course.name, itemLead + " 分钟后上课 · " + course.roomShort()});
                }
                i2 += W_MEDIUM;
                i = W_MEDIUM;
            }
        }
        ArrayList arrayList2 = new ArrayList();
        if (z) {
            arrayList2.addAll(TodoStore.load(this, now));
        }
        for (int i3 = W_REGULAR; i3 < arrayList2.size(); i3 += W_MEDIUM) {
            TodoStore.Item item = (TodoStore.Item) arrayList2.get(i3);
            if (!item.done) {
                int itemLead2 = ReminderPrefs.itemLead(this, "t:" + item.time + "|" + item.text);
                if (itemLead2 < 0) {
                    if (ReminderPrefs.todoGlobalOn(this)) {
                        itemLead2 = ReminderPrefs.todoLead(this);
                    }
                }
                if (TodoStore.minutesOf(item.time) >= 0) {
                    arrayList.add(new String[]{String.valueOf(((TodoStore.minutesOf(item.time) - itemLead2) * 60000) + epochMilli), "t:" + item.time + "|" + item.text, "待办提醒 · " + item.text, itemLead2 + " 分钟后 · " + item.time});
                }
            }
        }
        if (ReminderPrefs.toggled(this)) {
            List<ReminderPrefs.Custom> customs = ReminderPrefs.customs(this);
            for (int i4 = W_REGULAR; i4 < customs.size(); i4 += W_MEDIUM) {
                ReminderPrefs.Custom custom = customs.get(i4);
                arrayList.add(new String[]{String.valueOf(custom.at), "x:" + custom.id, "自定义提醒 · " + custom.title, "时间到了"});
            }
        }
        long currentTimeMillis = System.currentTimeMillis();
        long j = 0;
        int i5 = W_REGULAR;
        for (int i6 = W_REGULAR; i6 < arrayList.size(); i6 += W_MEDIUM) {
            try {
                try {
                    long parseLong = Long.parseLong(((String[]) arrayList.get(i6))[W_REGULAR]);
                    if (parseLong > currentTimeMillis + 1000) {
                        i5 += W_MEDIUM;
                        if (j == 0 || parseLong < j) {
                            j = parseLong;
                        }
                    }
                } catch (Exception unused) {
                }
            } catch (Exception unused2) {
            }
        }
        ReminderPrefs.setScheduleInfo(this, i5, j);
        ReminderScheduler.schedule(this, arrayList);
        int i7 = W_REGULAR;
        int i8 = W_REGULAR;
        int i9 = W_REGULAR;
        for (int i10 = W_REGULAR; i10 < arrayList.size(); i10 += W_MEDIUM) {
            String str = ((String[]) arrayList.get(i10))[W_MEDIUM];
            if (str.startsWith("c:")) {
                i7 += W_MEDIUM;
            } else if (str.startsWith("t:")) {
                i8 += W_MEDIUM;
            } else if (str.startsWith("x:")) {
                i9 += W_MEDIUM;
            }
        }
        ReminderPrefs.pushLog(this, "排程候选：课程 " + i7 + " / 待办 " + i8 + " / 自定义 " + i9);
        if (j > 0) {
            ReminderPrefs.pushLog(this, "最近一条提醒 " + LocalDateTime.ofInstant(Instant.ofEpochMilli(j), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M月d日 HH:mm:ss")));
        }
        ArrayList arrayList3 = new ArrayList(arrayList);
        arrayList3.addAll(weekCalItems(7));
        if (ReminderPrefs.calFallback(this)) {
            CalendarReminder.sync(this, arrayList3);
        } else {
            CalendarReminder.sync(this, new ArrayList());
        }
        if (ReminderPrefs.toggled(this) && (ReminderPrefs.courseGlobalOn(this) || ReminderPrefs.todoGlobalOn(this) || !ReminderPrefs.customs(this).isEmpty())) {
            ReminderService.start(this);
        } else {
            ReminderService.stop(this);
        }
    }

    private List<String[]> weekCalItems(int i) {
        ArrayList arrayList = new ArrayList();
        LocalDate now = LocalDate.now();
        boolean courseGlobalOn = ReminderPrefs.courseGlobalOn(this);
        int i2 = W_MEDIUM;
        int i3 = i;
        int i4 = W_MEDIUM;
        while (i4 < i3) {
            LocalDate plusDays = now.plusDays(i4);
            List<Course> loadCache = loadCache(plusDays);
            if (loadCache != null) {
                long epochMilli = plusDays.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                int i5 = W_REGULAR;
                while (i5 < loadCache.size()) {
                    Course course = loadCache.get(i5);
                    if (course.startSection >= i2) {
                        int itemLead = ReminderPrefs.itemLead(this, "c:" + course.name + ":" + course.startSection);
                        if (itemLead < 0) {
                            if (courseGlobalOn) {
                                itemLead = ReminderPrefs.courseLead(this);
                            }
                        }
                        arrayList.add(new String[]{String.valueOf(((ClassTimeTable.startMinutes(course.startSection) - itemLead) * 60000) + epochMilli), "cal:" + plusDays + ":c:" + course.name + ":" + course.startSection, "上课提醒 · " + course.name, itemLead + " 分钟后上课 · " + course.roomShort()});
                    }
                    i5 += W_MEDIUM;
                    i2 = W_MEDIUM;
                }
            }
            i4 += W_MEDIUM;
            i3 = i;
            i2 = W_MEDIUM;
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void maybeRemind() {
        if (this.viewDate.equals(LocalDate.now())) {
            int nowMinutes = nowMinutes();
            String localDate = this.viewDate.toString();
            if (ReminderPrefs.toggled(this)) {
                List<ReminderPrefs.Custom> customs = ReminderPrefs.customs(this);
                long currentTimeMillis = System.currentTimeMillis();
                for (int i = W_REGULAR; i < customs.size(); i += W_MEDIUM) {
                    ReminderPrefs.Custom custom = customs.get(i);
                    if (custom.at <= currentTimeMillis && currentTimeMillis - custom.at <= 60000 && !ReminderPrefs.customFired(this, custom.id, localDate)) {
                        ReminderPrefs.markCustomFired(this, custom.id, localDate);
                        LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(custom.at), ZoneId.systemDefault());
                        showReminder("自定义提醒", custom.title, ClassTimeTable.clockOfMinutes((ofInstant.getHour() * 60) + ofInstant.getMinute()) + " · 你设置的提醒时间到了");
                        return;
                    }
                }
                if (ReminderPrefs.activeOn(this, LocalDate.now())) {
                    int courseLead = ReminderPrefs.courseLead(this);
                    List<Course> list = this.lastData;
                    if (list != null && !list.isEmpty()) {
                        Course course = null;
                        int i2 = W_REGULAR;
                        for (int i3 = W_REGULAR; i3 < this.lastData.size(); i3 += W_MEDIUM) {
                            Course course2 = this.lastData.get(i3);
                            if (course2.startSection >= W_MEDIUM) {
                                int startMinutes = ClassTimeTable.startMinutes(course2.startSection);
                                int itemLead = ReminderPrefs.itemLead(this, "c:" + course2.name + ":" + course2.startSection);
                                if (itemLead < 0) {
                                    if (ReminderPrefs.courseGlobalOn(this)) {
                                        itemLead = courseLead;
                                    }
                                }
                                if (((startMinutes > 0 && startMinutes <= nowMinutes && nowMinutes - startMinutes <= 10) || (startMinutes > nowMinutes && startMinutes - nowMinutes <= itemLead)) && (course == null || startMinutes < i2)) {
                                    course = course2;
                                    i2 = startMinutes;
                                }
                            }
                        }
                        if (course != null) {
                            String str = localDate + "|c|" + course.name + "|" + course.startSection;
                            if (!ReminderPrefs.shownOn(this, str, localDate)) {
                                ReminderPrefs.markShown(this, str, localDate);
                                StringBuilder sb = new StringBuilder();
                                if (i2 <= nowMinutes) {
                                    sb.append("已开始 ");
                                    sb.append(nowMinutes - i2);
                                    sb.append(" 分钟 · ");
                                } else {
                                    sb.append(i2 - nowMinutes);
                                    sb.append(" 分钟后上课 · ");
                                }
                                sb.append(course.name);
                                if (!course.roomShort().isEmpty()) {
                                    sb.append(" · ");
                                    sb.append(course.roomShort());
                                }
                                showReminder("上课提醒", course.name, sb.toString());
                                return;
                            }
                        }
                    }
                    int i4 = ReminderPrefs.todoLead(this);
                    List<TodoStore.Item> load = TodoStore.load(this, this.viewDate);
                    for (int i5 = W_REGULAR; i5 < load.size(); i5 += W_MEDIUM) {
                        TodoStore.Item item = load.get(i5);
                        if (!item.done) {
                            int minutesOf = TodoStore.minutesOf(item.time);
                            int itemLead2 = ReminderPrefs.itemLead(this, "t:" + item.time + "|" + item.text);
                            if (itemLead2 < 0) {
                                if (ReminderPrefs.todoGlobalOn(this)) {
                                    itemLead2 = i4;
                                } else {
                                    continue;
                                }
                            }
                            if ((minutesOf >= 0 && minutesOf <= nowMinutes && nowMinutes - minutesOf <= 10) || (minutesOf >= 0 && minutesOf > nowMinutes && minutesOf - nowMinutes <= itemLead2)) {
                                String str2 = localDate + "|t|" + item.time + "|" + item.text;
                                if (!ReminderPrefs.shownOn(this, str2, localDate)) {
                                    ReminderPrefs.markShown(this, str2, localDate);
                                    showReminder("待办提醒", item.text, (minutesOf <= nowMinutes ? "已到时间 · " : (minutesOf - nowMinutes) + " 分钟后 · ") + "预约时间 " + item.time);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLeadWheel(final int[] iArr, final List<TextView> list, final int i) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(W_MEDIUM);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(18.0f), dp(16.0f), dp(18.0f), dp(20.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.dark ? Color.parseColor("#1B1D18") : Color.parseColor("#F8F7F3"));
        float dp = dp(24.0f);
        gradientDrawable.setCornerRadii(new float[]{dp, dp, dp, dp, W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR});
        linearLayout.setBackground(gradientDrawable);
        TextView label = label(i == W_MEDIUM ? "今日课程提醒" : i == W_SEMIBOLD ? "今日待办提醒" : i == 3 ? "选择提前时间" : "提前提醒", 17.0f, this.ink, W_SEMIBOLD);
        label.setGravity(17);
        linearLayout.addView(label);
        final TextView txt = txt(leadHintText(i, iArr[W_REGULAR]), 12, this.muted, false);
        txt.setGravity(17);
        linearLayout.addView(txt, top(4));
        final WheelPicker wheelPicker = new WheelPicker(this, 120, iArr[W_REGULAR] - 1, this.ink, this.faint, 120, 172);
        wheelPicker.setOneBased(true);
        linearLayout.addView(wheelPicker, new LinearLayout.LayoutParams(dp(120.0f), dp(172.0f)));
        ((LinearLayout.LayoutParams) wheelPicker.getLayoutParams()).gravity = 17;
        wheelPicker.setOnChange(new WheelPicker.OnChange() { // from class: com.zuwe.smartcourselite.MainActivity.80
            @Override // com.zuwe.smartcourselite.WheelPicker.OnChange
            public void onChanged(int i2) {
                txt.setText(MainActivity.this.leadHintText(i, i2 + MainActivity.W_MEDIUM));
            }
        });
        Button primaryBtn = primaryBtn("保存");
        primaryBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.81
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                String str;
                int value = wheelPicker.value() + MainActivity.W_MEDIUM;
                int[] iArr2 = iArr;
                int i2 = MainActivity.W_REGULAR;
                iArr2[MainActivity.W_REGULAR] = value;
                MainActivity.this.revealExclude.clear();
                MainActivity.this.revealExclude.addAll(MainActivity.this.snapshotBellKeys());
                int i3 = i;
                if (i3 == MainActivity.W_MEDIUM) {
                    MainActivity mainActivity = MainActivity.this;
                    ReminderPrefs.quickCourses(mainActivity, value, mainActivity.viewDate);
                    Toast.makeText(MainActivity.this, "今日课程将提前 " + value + " 分钟提醒", MainActivity.W_REGULAR).show();
                } else if (i3 == MainActivity.W_SEMIBOLD) {
                    MainActivity mainActivity2 = MainActivity.this;
                    ReminderPrefs.quickTodos(mainActivity2, value, mainActivity2.viewDate);
                    Toast.makeText(MainActivity.this, "今日待办将提前 " + value + " 分钟提醒", MainActivity.W_REGULAR).show();
                } else if (i3 == 3) {
                    for (int i4 = MainActivity.W_REGULAR; i4 < MainActivity.this.pickerKeys.size(); i4 += MainActivity.W_MEDIUM) {
                        String str2 = (String) MainActivity.this.pickerKeys.get(i4);
                        if (((CheckBox) MainActivity.this.pickerBoxes.get(i4)).isChecked()) {
                            ReminderPrefs.setItemLead(MainActivity.this, str2, value);
                            MainActivity.this.pendingRevealKeys.add(str2);
                        } else {
                            ReminderPrefs.clearItemLead(MainActivity.this, str2);
                        }
                    }
                    MainActivity mainActivity3 = MainActivity.this;
                    ReminderPrefs.setToggledOn(mainActivity3, mainActivity3.viewDate);
                } else {
                    ReminderPrefs.setCourseLead(MainActivity.this, value);
                    ReminderPrefs.setTodoLead(MainActivity.this, value);
                }
                MainActivity.paintLeadChips(list, value, MainActivity.this);
                MainActivity.this.scheduleRemindersSafe();
                Haptics.confirm(view);
                if (i == 3) {
                    int i5 = MainActivity.W_REGULAR;
                    while (i2 < MainActivity.this.pickerBoxes.size()) {
                        if (((CheckBox) MainActivity.this.pickerBoxes.get(i2)).isChecked()) {
                            i5 += MainActivity.W_MEDIUM;
                        }
                        i2 += MainActivity.W_MEDIUM;
                    }
                    i2 = i5;
                }
                int i6 = i;
                if (i6 == 3) {
                    str = "已为 " + i2 + " 项设置提醒 · 提前 " + value + " 分钟 ✌️";
                } else if (i6 == MainActivity.W_MEDIUM) {
                    str = "今日课程 · 提前 " + value + " 分钟提醒 ✌️";
                } else if (i6 == MainActivity.W_SEMIBOLD) {
                    str = "今日待办 · 提前 " + value + " 分钟提醒 ✌️";
                } else {
                    str = "提前 " + value + " 分钟提醒已开启 ✌️";
                }
                dialog.dismiss();
                MainActivity mainActivity4 = MainActivity.this;
                TodoFx.notice(mainActivity4, str, mainActivity4.ink, MainActivity.this.cardBg, MainActivity.this.line);
                MainActivity.this.revealBellsOnRender = true;
                MainActivity mainActivity5 = MainActivity.this;
                mainActivity5.render(mainActivity5.lastData);
            }
        });
        linearLayout.addView(primaryBtn, top(14));
        dialog.setContentView(linearLayout);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(W_REGULAR));
            window.setLayout(-1, -2);
            window.setGravity(80);
        }
        linearLayout.setTranslationY(dp(60.0f));
        linearLayout.setAlpha(0.0f);
        linearLayout.animate().translationY(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showReminderPicker() {
        int minutesOf;
        this.pickerKeys.clear();
        this.pickerBoxes.clear();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(W_MEDIUM);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(18.0f), dp(14.0f), dp(18.0f), dp(18.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.dark ? Color.parseColor("#1B1D18") : Color.parseColor("#F8F7F3"));
        float dp = dp(24.0f);
        int i = 3;
        gradientDrawable.setCornerRadii(new float[]{dp, dp, dp, dp, W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR});
        linearLayout.setBackground(gradientDrawable);
        View label = label("选择要提醒的课程 / 待办", 17.0f, this.ink, W_SEMIBOLD);
        View txt = txt(this.viewDate.format(FMT_TITLE) + " · 勾选后下一步选择提前时间", 12, this.muted, false);
        linearLayout.addView(label);
        linearLayout.addView(txt, top(4));
        TextView txt2 = txt("已选 0 项", 12, this.green, true);
        this.pickerCount = txt2;
        linearLayout.addView(txt2, top(4));
        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_MEDIUM);
        scrollView.addView(linearLayout2);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(238.0f));
        layoutParams.topMargin = dp(10.0f);
        linearLayout.addView(scrollView, layoutParams);
        ArrayList arrayList = new ArrayList();
        int i2 = 100000;
        if (this.lastData != null) {
            int i3 = W_REGULAR;
            while (i3 < this.lastData.size()) {
                Course course = this.lastData.get(i3);
                int startMinutes = ClassTimeTable.startMinutes(course.startSection);
                if (isFutureOnViewDate(startMinutes)) {
                    arrayList.add(new String[]{String.format(Locale.US, "%05d", Integer.valueOf(startMinutes % i2)), "c:" + course.name + ":" + course.startSection, ClassTimeTable.clockOf(course.startSection) + "  " + course.name});
                }
                i3 += W_MEDIUM;
                i2 = 100000;
            }
        }
        List<TodoStore.Item> load = TodoStore.load(this, this.viewDate);
        int i4 = W_REGULAR;
        while (i4 < load.size()) {
            TodoStore.Item item = load.get(i4);
            if (!item.done && ((minutesOf = TodoStore.minutesOf(item.time)) < 0 || isFutureOnViewDate(minutesOf))) {
                String[] strArr = new String[i];
                Locale locale = Locale.US;
                Object[] objArr = new Object[W_MEDIUM];
                if (minutesOf < 0) {
                    minutesOf = 100000;
                }
                objArr[W_REGULAR] = Integer.valueOf(minutesOf);
                strArr[W_REGULAR] = String.format(locale, "%05d", objArr);
                strArr[W_MEDIUM] = "t:" + item.time + "|" + item.text;
                strArr[W_SEMIBOLD] = item.time + "  " + item.text;
                arrayList.add(strArr);
            }
            i4 += W_MEDIUM;
            i = 3;
        }
        Collections.sort(arrayList, new Comparator<String[]>() { // from class: com.zuwe.smartcourselite.MainActivity.82
            @Override // java.util.Comparator
            public int compare(String[] strArr2, String[] strArr3) {
                return strArr2[MainActivity.W_REGULAR].compareTo(strArr3[MainActivity.W_REGULAR]);
            }
        });
        for (int i5 = W_REGULAR; i5 < arrayList.size(); i5 += W_MEDIUM) {
            linearLayout2.addView(pickerRow(((String[]) arrayList.get(i5))[W_MEDIUM], ((String[]) arrayList.get(i5))[W_SEMIBOLD]));
        }
        if (linearLayout2.getChildCount() == 0) {
            linearLayout2.addView(txt("这一天没有可提醒的课程或待办", 12, this.faint, false));
        }
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_REGULAR);
        linearLayout3.setGravity(17);
        Button ghostBtn = ghostBtn("取消");
        ghostBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.83
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button primaryBtn = primaryBtn("下一步");
        primaryBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.84
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean z;
                List list = MainActivity.this.pickerBoxes;
                int i6 = MainActivity.W_REGULAR;
                while (true) {
                    if (i6 >= list.size()) {
                        z = false;
                        break;
                    } else {
                        if (((CheckBox) list.get(i6)).isChecked()) {
                            z = true;
                            break;
                        }
                        i6 += MainActivity.W_MEDIUM;
                    }
                }
                if (!z) {
                    Toast.makeText(MainActivity.this, "至少选一条", MainActivity.W_REGULAR).show();
                    return;
                }
                Haptics.tick(view);
                dialog.dismiss();
                MainActivity.this.showLeadWheel(new int[]{ReminderPrefs.courseLead(MainActivity.this)}, new ArrayList(), 3);
            }
        });
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(W_REGULAR, dp(44.0f), 1.0f);
        layoutParams2.rightMargin = dp(6.0f);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(W_REGULAR, dp(44.0f), 1.0f);
        layoutParams3.leftMargin = dp(6.0f);
        linearLayout3.addView(ghostBtn, layoutParams2);
        linearLayout3.addView(primaryBtn, layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout3, layoutParams4);
        dialog.setContentView(linearLayout);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(W_REGULAR));
            window.setLayout(-1, -2);
            window.setGravity(80);
        }
        linearLayout.setTranslationY(dp(60.0f));
        linearLayout.setAlpha(0.0f);
        linearLayout.animate().translationY(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePickerCount() {
        if (this.pickerCount == null) {
            return;
        }
        int i = W_REGULAR;
        for (int i2 = W_REGULAR; i2 < this.pickerBoxes.size(); i2 += W_MEDIUM) {
            if (this.pickerBoxes.get(i2).isChecked()) {
                i += W_MEDIUM;
            }
        }
        this.pickerCount.setText("已选 " + i + " 项");
    }

    private View pickerRow(String str, String str2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(16);
        linearLayout.setPadding(W_REGULAR, dp(7.0f), W_REGULAR, dp(7.0f));
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(ReminderPrefs.hasItemLead(this, str));
        checkBox.setButtonTintList(ColorStateList.valueOf(this.green));
        checkBox.setClickable(false);
        checkBox.setFocusable(false);
        linearLayout.addView(checkBox, new LinearLayout.LayoutParams(dp(30.0f), dp(30.0f)));
        TextView label = label(str2, 13.0f, this.ink, W_REGULAR);
        label.setMaxLines(W_MEDIUM);
        linearLayout.addView(label, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.85
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                checkBox.setChecked(!checkBox.isChecked());
                Haptics.tick(checkBox);
                MainActivity.this.updatePickerCount();
            }
        });
        this.pickerKeys.add(str);
        this.pickerBoxes.add(checkBox);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String leadHintText(int i, int i2) {
        if (i == W_MEDIUM) {
            return "今日所有课程开始前 " + i2 + " 分钟提醒";
        }
        if (i == W_SEMIBOLD) {
            return "今日所有待办时间前 " + i2 + " 分钟提醒";
        }
        return "课程 / 待办开始前 " + i2 + " 分钟提醒";
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r3v3, types: [java.time.ZonedDateTime] */
    public long reminderAt(int i, int i2) {
        LocalDate localDate = this.viewDate;
        if (localDate == null) {
            localDate = LocalDate.now();
        }
        LocalDateTime atTime = localDate.atTime(i, i2);
        if (localDate.equals(LocalDate.now()) && !atTime.isAfter(LocalDateTime.now())) {
            atTime = atTime.plusDays(1L);
        }
        return atTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String reminderWhen(long j, int i, int i2) {
        LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(j), ZoneId.systemDefault());
        String format = String.format(Locale.US, "%02d:%02d", Integer.valueOf(i), Integer.valueOf(i2));
        long minutes = Duration.between(LocalDateTime.now(), ofInstant).toMinutes();
        if (minutes <= 0) {
            return format;
        }
        if (minutes >= 60) {
            return format + " · " + (minutes / 60) + " 小时 " + (minutes % 60) + " 分钟后";
        }
        return format + " · " + minutes + " 分钟后";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCustomReminderSheet() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(W_MEDIUM);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(18.0f), dp(12.0f), dp(18.0f), dp(20.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.dark ? Color.parseColor("#1B1D18") : Color.parseColor("#F8F7F3"));
        float dp = dp(26.0f);
        gradientDrawable.setCornerRadii(new float[]{dp, dp, dp, dp, W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR});
        linearLayout.setBackground(gradientDrawable);
        final WheelPicker wheelPicker = new WheelPicker(this, 24, LocalTime.now().getHour(), this.ink, this.faint, 52, 172);
        final WheelPicker wheelPicker2 = new WheelPicker(this, 60, W_REGULAR, this.ink, this.faint, 62, 172);
        final EditText editText = new EditText(this);
        final TextView txt = txt("", 12, this.muted, false);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        TextView label = label("取消", 13.0f, this.muted, W_REGULAR);
        label.setPadding(dp(4.0f), dp(6.0f), dp(10.0f), dp(6.0f));
        label.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.86
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        TextView label2 = label("添加提醒", 17.0f, this.ink, W_SEMIBOLD);
        label2.setGravity(17);
        TextView label3 = label("保存", 13.0f, this.green, W_MEDIUM);
        label3.setPadding(dp(10.0f), dp(6.0f), dp(4.0f), dp(6.0f));
        label3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.87
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                int value = wheelPicker.value();
                int value2 = wheelPicker2.value();
                long reminderAt = MainActivity.this.reminderAt(value, value2);
                MainActivity.this.ensureReminderPermissions();
                MainActivity mainActivity = MainActivity.this;
                StringBuilder sb = new StringBuilder("已保存自定义提醒 ");
                int i = (value * 60) + value2;
                sb.append(ClassTimeTable.clockOfMinutes(i));
                sb.append("，开始同步日历");
                ReminderPrefs.pushLog(mainActivity, sb.toString());
                ReminderPrefs.addCustom(MainActivity.this, reminderAt, editText.getText().toString().trim().isEmpty() ? "提醒" : editText.getText().toString().trim());
                Haptics.confirm(view);
                dialog.dismiss();
                MainActivity mainActivity2 = MainActivity.this;
                mainActivity2.render(mainActivity2.lastData);
                MainActivity.this.scheduleRemindersSafe();
                String clockOfMinutes = ClassTimeTable.clockOfMinutes(i);
                ReminderAlert.confirm(MainActivity.this, "创建成功 · " + clockOfMinutes, "已加入时间轴，到点会提醒你", MainActivity.this.ink, MainActivity.this.muted, MainActivity.this.cardBg, MainActivity.this.line, MainActivity.this.green);
            }
        });
        linearLayout2.addView(label, new LinearLayout.LayoutParams(-2, -2));
        linearLayout2.addView(label2, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        linearLayout2.addView(label3, new LinearLayout.LayoutParams(-2, -2));
        linearLayout.addView(linearLayout2);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(W_REGULAR);
        linearLayout3.setGravity(17);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(52.0f), dp(172.0f));
        layoutParams.gravity = 16;
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(62.0f), dp(172.0f));
        layoutParams2.gravity = 16;
        linearLayout3.addView(wheelPicker, layoutParams);
        linearLayout3.addView(wheelPicker2, layoutParams2);
        linearLayout.addView(linearLayout3, top(W_SEMIBOLD));
        editText.setSingleLine(true);
        editText.setTextSize(15.0f);
        editText.setTextColor(this.ink);
        editText.setHintTextColor(this.faint);
        editText.setHint("提醒内容（可选）");
        editText.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
        editText.setBackground(inputBackground());
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, dp(52.0f));
        layoutParams3.topMargin = dp(10.0f);
        linearLayout.addView(editText, layoutParams3);
        WheelPicker.OnChange onChange = new WheelPicker.OnChange() { // from class: com.zuwe.smartcourselite.MainActivity.88
            @Override // com.zuwe.smartcourselite.WheelPicker.OnChange
            public void onChanged(int i) {
                int value = wheelPicker.value();
                int value2 = wheelPicker2.value();
                TextView textView = txt;
                MainActivity mainActivity = MainActivity.this;
                textView.setText(mainActivity.reminderWhen(mainActivity.reminderAt(value, value2), value, value2));
            }
        };
        wheelPicker.setOnChange(onChange);
        wheelPicker2.setOnChange(onChange);
        dialog.setContentView(linearLayout);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(W_REGULAR));
            window.setLayout(-1, -2);
            window.setGravity(80);
        }
        linearLayout.setTranslationY(dp(60.0f));
        linearLayout.setAlpha(0.0f);
        linearLayout.animate().translationY(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
    }

    private boolean courseReminderOn(Course course) {
        if (course == null) {
            return false;
        }
        int itemLead = ReminderPrefs.itemLead(this, "c:" + course.name + ":" + course.startSection);
        if (itemLead < 0) {
            if (!ReminderPrefs.activeOn(this, this.viewDate) || !ReminderPrefs.courseGlobalOn(this)) {
                return false;
            }
            itemLead = ReminderPrefs.courseLead(this);
        }
        int startMinutes = ClassTimeTable.startMinutes(course.startSection);
        return startMinutes > 0 && startMinutes - itemLead > 0 && isFutureOnViewDate(startMinutes);
    }

    private boolean isFutureOnViewDate(int i) {
        return !this.viewDate.equals(LocalDate.now()) || i > nowMinutes();
    }

    private boolean todoReminderOn(TodoStore.Item item) {
        if (item == null || item.done) {
            return false;
        }
        int itemLead = ReminderPrefs.itemLead(this, "t:" + item.time + "|" + item.text);
        if (itemLead < 0) {
            if (!ReminderPrefs.activeOn(this, this.viewDate) || !ReminderPrefs.todoGlobalOn(this)) {
                return false;
            }
            itemLead = ReminderPrefs.todoLead(this);
        }
        int minutesOf = TodoStore.minutesOf(item.time);
        return minutesOf >= 0 && minutesOf - itemLead > 0 && isFutureOnViewDate(minutesOf);
    }

    private View reminderBell(String str) {
        SvgIcon.IconView view = SvgIcon.view(this, SvgIcon.BELL, this.green, 13);
        this.reminderBells.put(str, view);
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Set<String> snapshotBellKeys() {
        HashSet hashSet = new HashSet();
        List<Course> list = this.lastData;
        if (list != null) {
            for (int i = W_REGULAR; i < this.lastData.size(); i += W_MEDIUM) {
                Course course = this.lastData.get(i);
                if (courseReminderOn(course)) {
                    hashSet.add("c:" + course.name + ":" + course.startSection);
                }
            }
        }
        List<TodoStore.Item> load = TodoStore.load(this, this.viewDate);
        for (int i2 = W_REGULAR; i2 < load.size(); i2 += W_MEDIUM) {
            if (todoReminderOn(load.get(i2))) {
                hashSet.add("t:" + load.get(i2).time + "|" + load.get(i2).text);
            }
        }
        return hashSet;
    }

    private void playBellReveal() {
        if (this.reminderBells.isEmpty()) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, View> entry : this.reminderBells.entrySet()) {
            if (!this.revealExclude.contains(entry.getKey()) && (this.pendingRevealKeys.isEmpty() || this.pendingRevealKeys.contains(entry.getKey()))) {
                arrayList.add(entry.getValue());
            }
        }
        this.pendingRevealKeys.clear();
        this.revealExclude.clear();
        if (arrayList.isEmpty()) {
            return;
        }
        for (int i = W_REGULAR; i < arrayList.size(); i += W_MEDIUM) {
            final View view = (View) arrayList.get(i);
            view.setAlpha(0.0f);
            view.setScaleX(0.2f);
            view.setScaleY(0.2f);
            view.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.89
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.scrollBellToCenter(view);
                    view.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(560L).setInterpolator(Motion.SPRING).start();
                    Haptics.tick(view);
                }
            }, (i * 460) + 260);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scrollBellToCenter(View view) {
        if (this.courseScroll == null || view == null) {
            return;
        }
        int[] iArr = new int[W_SEMIBOLD];
        view.getLocationInWindow(iArr);
        int[] iArr2 = new int[W_SEMIBOLD];
        this.courseScroll.getLocationInWindow(iArr2);
        int height = (iArr[W_MEDIUM] + (view.getHeight() / W_SEMIBOLD)) - (iArr2[W_MEDIUM] + (this.courseScroll.getHeight() / W_SEMIBOLD));
        if (Math.abs(height) > dp(40.0f)) {
            int scrollY = this.courseScroll.getScrollY();
            ValueAnimator ofInt = ValueAnimator.ofInt(scrollY, height + scrollY);
            ofInt.setDuration(680L);
            ofInt.setInterpolator(Motion.STANDARD);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.90
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MainActivity.this.courseScroll.scrollTo(MainActivity.W_REGULAR, ((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            });
            ofInt.start();
        }
    }

    private void renderEmptyState() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setGravity(17);
        linearLayout.setPadding(dp(20.0f), dp(34.0f), dp(20.0f), dp(40.0f));
        if (this.viewDate.equals(LocalDate.now())) {
            linearLayout.addView(new MascotView(this, this.blue), new LinearLayout.LayoutParams(dp(88.0f), dp(88.0f)));
            TextView label = label("今天，留一点时间给自己。", 17.0f, this.ink, W_MEDIUM);
            label.setGravity(17);
            linearLayout.addView(label, top(14));
            TextView label2 = label("今日暂无课程安排。", 13.0f, this.faint, W_REGULAR);
            label2.setGravity(17);
            linearLayout.addView(label2, top(6));
        } else {
            TextView label3 = label("这一天没有课", 15.0f, this.ink, W_SEMIBOLD);
            label3.setGravity(17);
            label3.setAlpha(0.0f);
            label3.setTranslationY(dp(8.0f));
            linearLayout.addView(label3);
            TextView label4 = label("左右滑动可以切换前后一天", 13.0f, this.faint, W_REGULAR);
            label4.setGravity(17);
            label4.setAlpha(0.0f);
            label4.setTranslationY(dp(8.0f));
            linearLayout.addView(label4, top(8));
            label3.animate().alpha(1.0f).translationY(0.0f).setDuration(280L).setInterpolator(Motion.ENTER).start();
            label4.animate().alpha(1.0f).translationY(0.0f).setStartDelay(60L).setDuration(280L).setInterpolator(Motion.ENTER).start();
        }
        this.courseList.addView(linearLayout);
        if (this.quietRender) {
            return;
        }
        UiPolish.containerExpand(linearLayout);
        revealScheduleFromChip();
    }

    private void revealScheduleFromChip() {
        if (this.pendingDayReveal) {
            this.pendingDayReveal = false;
            View view = this.selectedDayChip;
            LinearLayout linearLayout = this.courseList;
            if (view == null || linearLayout == null || linearLayout.getWidth() <= 0 || linearLayout.getHeight() <= 0) {
                return;
            }
            int[] iArr = new int[W_SEMIBOLD];
            int[] iArr2 = new int[W_SEMIBOLD];
            view.getLocationInWindow(iArr);
            linearLayout.getLocationInWindow(iArr2);
            linearLayout.setPivotX(Math.max(0.0f, Math.min(linearLayout.getWidth(), (iArr[W_REGULAR] - iArr2[W_REGULAR]) + (view.getWidth() / 2.0f))));
            linearLayout.setPivotY(0.0f);
            linearLayout.setTranslationY(-Math.min(dp(46.0f), linearLayout.getHeight() * 0.35f));
            linearLayout.setScaleX(0.94f);
            linearLayout.setScaleY(0.94f);
            linearLayout.setAlpha(0.12f);
            linearLayout.animate().translationY(0.0f).scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(720L).setInterpolator(Motion.SPRING).start();
        }
    }

    private void playCourseEntrance(List<View> list) {
        int size = list.size();
        if (size == 0) {
            return;
        }
        float f = getResources().getDisplayMetrics().density;
        for (int i = W_REGULAR; i < size; i += W_MEDIUM) {
            View view = list.get(i);
            view.setAlpha(0.0f);
            view.setTranslationY(26.0f * f);
            view.setRotationX(-13.0f);
            view.setScaleX(0.94f);
            view.setScaleY(0.94f);
            view.setCameraDistance(18000.0f * f);
            view.setPivotY(0.0f);
            view.animate().alpha(1.0f).translationY(0.0f).rotationX(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(Math.min(i, 8) * 70).setDuration(420L).setInterpolator(Motion.SPRING).start();
        }
    }

    private void scheduleStateTick() {
        if (this.uiHandler == null) {
            return;
        }
        if (this.stateTick == null) {
            this.stateTick = new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.91
                @Override // java.lang.Runnable
                public void run() {
                    if (MainActivity.this.lastData == null || MainActivity.this.courseList == null || !MainActivity.this.viewDate.equals(LocalDate.now())) {
                        return;
                    }
                    MainActivity.this.quietRender = true;
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.render(mainActivity.lastData);
                    MainActivity.this.quietRender = false;
                }
            };
        }
        this.uiHandler.removeCallbacks(this.stateTick);
        this.uiHandler.postDelayed(this.stateTick, 60000L);
    }

    private int nowMinutes() {
        LocalTime now = LocalTime.now();
        return (now.getHour() * 60) + now.getMinute();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int startMinutes(Course course) {
        if (course.startSection >= W_MEDIUM) {
            return ClassTimeTable.startMinutes(course.startSection);
        }
        return 1440;
    }

    private int endMinutes(Course course) {
        if (course.startSection < W_MEDIUM) {
            return 1440;
        }
        return ClassTimeTable.endMinutes(course.endSection >= course.startSection ? course.endSection : course.startSection);
    }

    private boolean nowWithinDay(List<Course> list, int i) {
        if (list.isEmpty()) {
            return false;
        }
        int i2 = Integer.MAX_VALUE;
        int i3 = -1;
        for (Course course : list) {
            i2 = Math.min(i2, startMinutes(course));
            i3 = Math.max(i3, endMinutes(course));
        }
        return i >= i2 && i <= i3;
    }

    private View timelineRow(final Course course, int i, boolean z) {
        int withAlpha;
        char c = (!z || i < startMinutes(course)) ? (char) 0 : i <= endMinutes(course) ? (char) 1 : (char) 2;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_REGULAR);
        linearLayout.setGravity(48);
        int dp = dp(14.0f);
        linearLayout.setPadding(dp(14.0f), dp, dp(16.0f), dp);
        FrameLayout frameLayout = new FrameLayout(this);
        if (c == W_MEDIUM) {
            View view = new View(this);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(dp(2.0f));
            gradientDrawable.setColor(this.green);
            view.setBackground(gradientDrawable);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp(3.0f), -1);
            layoutParams.topMargin = dp(2.0f);
            layoutParams.bottomMargin = dp(2.0f);
            frameLayout.addView(view, layoutParams);
        }
        linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(dp(3.0f), -1));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_MEDIUM);
        linearLayout2.setGravity(53);
        TextView txt = txt(ClassTimeTable.clockOf(course.startSection), 16, c == W_MEDIUM ? this.green : this.ink, true);
        txt.setGravity(5);
        txt.setIncludeFontPadding(false);
        TextView txt2 = txt(ClassTimeTable.clockEndOf(course.endSection), 11, this.muted, false);
        txt2.setGravity(5);
        txt2.setIncludeFontPadding(false);
        linearLayout2.setGravity(53);
        linearLayout2.addView(txt, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(dp(56.0f), -1);
        layoutParams2.leftMargin = dp(11.0f);
        linearLayout.addView(linearLayout2, layoutParams2);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setGravity(49);
        View view2 = new View(this);
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setCornerRadius(dp(2.0f));
        if (c == W_MEDIUM) {
            withAlpha = UiPolish.withAlpha(this.green, 153);
        } else {
            withAlpha = UiPolish.withAlpha(this.ink, this.dark ? 42 : 28);
        }
        gradientDrawable2.setColor(withAlpha);
        view2.setBackground(gradientDrawable2);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(dp(3.0f), dp(30.0f));
        layoutParams3.topMargin = dp(4.0f);
        linearLayout3.addView(view2, layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(dp(18.0f), -2);
        layoutParams4.leftMargin = dp(8.0f);
        layoutParams4.rightMargin = dp(10.0f);
        linearLayout.addView(linearLayout3, layoutParams4);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(W_MEDIUM);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setGravity(16);
        linearLayout5.addView(label(course.name, 15.0f, this.ink, W_MEDIUM), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        if (courseReminderOn(course)) {
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(dp(13.0f), dp(13.0f));
            layoutParams5.leftMargin = dp(6.0f);
            layoutParams5.gravity = 16;
            linearLayout5.addView(reminderBell("c:" + course.name + ":" + course.startSection), layoutParams5);
        }
        if (c == W_MEDIUM) {
            TextView txt3 = txt("正在上课", 10, this.onGreen, true);
            txt3.setPadding(dp(8.0f), dp(3.0f), dp(8.0f), dp(3.0f));
            GradientDrawable gradientDrawable3 = new GradientDrawable();
            gradientDrawable3.setColor(this.green);
            gradientDrawable3.setCornerRadius(dp(9.0f));
            txt3.setBackground(gradientDrawable3);
            linearLayout5.addView(txt3);
            txt3.setPivotX(dp(12.0f));
            txt3.setPivotY(dp(9.0f));
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(txt3, PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.08f), PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.08f), PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.82f));
            ofPropertyValuesHolder.setDuration(1500L);
            ofPropertyValuesHolder.setRepeatCount(-1);
            ofPropertyValuesHolder.setRepeatMode(W_SEMIBOLD);
            ofPropertyValuesHolder.setInterpolator(new AccelerateDecelerateInterpolator());
            ofPropertyValuesHolder.start();
        }
        linearLayout4.addView(linearLayout5);
        StringBuilder sb = new StringBuilder();
        if (course.startSection >= W_MEDIUM) {
            sb.append("第");
            sb.append(course.startSection);
            sb.append("–");
            sb.append(course.endSection);
            sb.append("节");
        }
        if (!course.roomShort().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(course.roomShort());
        }
        if (sb.length() > 0) {
            linearLayout4.addView(txt(sb.toString(), 12, this.muted, false), top(6));
        }
        if (course.teacher != null && !course.teacher.isEmpty()) {
            linearLayout4.addView(txt(course.teacher, 11, this.faint, false), top(4));
        }
        if (c == W_MEDIUM) {
            final float max = Math.max(0.0f, Math.min(1.0f, (nowMinutes() - startMinutes(course)) / Math.max(W_MEDIUM, endMinutes(course) - startMinutes(course))));
            final FrameLayout frameLayout2 = new FrameLayout(this);
            GradientDrawable gradientDrawable4 = new GradientDrawable();
            gradientDrawable4.setColor(UiPolish.withAlpha(this.green, this.dark ? 64 : 42));
            gradientDrawable4.setCornerRadius(dp(2.0f));
            frameLayout2.setBackground(gradientDrawable4);
            final View view3 = new View(this);
            GradientDrawable gradientDrawable5 = new GradientDrawable();
            gradientDrawable5.setColor(this.green);
            gradientDrawable5.setCornerRadius(dp(2.0f));
            view3.setBackground(gradientDrawable5);
            frameLayout2.addView(view3, new FrameLayout.LayoutParams(W_REGULAR, -1));
            frameLayout2.post(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.92
                @Override // java.lang.Runnable
                public void run() {
                    int max2 = Math.max(MainActivity.this.dp(2.0f), (int) (frameLayout2.getWidth() * max));
                    ViewGroup.LayoutParams layoutParams6 = view3.getLayoutParams();
                    layoutParams6.width = MainActivity.this.dp(2.0f);
                    view3.setLayoutParams(layoutParams6);
                    ValueAnimator ofInt = ValueAnimator.ofInt(MainActivity.this.dp(2.0f), max2);
                    ofInt.setDuration(420L);
                    ofInt.setStartDelay(180L);
                    ofInt.setInterpolator(Motion.ENTER);
                    ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.92.1
                        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            ViewGroup.LayoutParams layoutParams7 = view3.getLayoutParams();
                            layoutParams7.width = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                            view3.setLayoutParams(layoutParams7);
                        }
                    });
                    ofInt.start();
                }
            });
            LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, dp(4.0f));
            layoutParams6.topMargin = dp(10.0f);
            linearLayout4.addView(frameLayout2, layoutParams6);
        }
        linearLayout.addView(linearLayout4, new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        if (c == W_SEMIBOLD) {
            linearLayout.setAlpha(0.5f);
        }
        linearLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.MainActivity.93
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view4, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    Haptics.tick(view4);
                    view4.animate().cancel();
                    view4.animate().scaleX(0.976f).scaleY(0.976f).translationZ(MainActivity.this.dp(8.0f)).setDuration(90L).setInterpolator(Motion.STANDARD).start();
                    return false;
                }
                if (actionMasked != MainActivity.W_MEDIUM && actionMasked != 3) {
                    return false;
                }
                view4.animate().cancel();
                view4.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
                return false;
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.94
            @Override // android.view.View.OnClickListener
            public void onClick(View view4) {
                MainActivity.this.showCourseDetail(course, view4);
            }
        });
        LinearLayout linearLayout6 = new LinearLayout(this);
        linearLayout6.setOrientation(W_MEDIUM);
        linearLayout6.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
        if (this.inlineTodos != null) {
            for (int i2 = W_REGULAR; i2 < this.inlineTodos.size(); i2 += W_MEDIUM) {
                TodoStore.Item item = (TodoStore.Item) this.inlineTodos.get(i2);
                View view4 = todoRow(item, true);
                if (isPendingReveal(item)) {
                    view4.setAlpha(0.0f);
                    this.pendingTodoRow = view4;
                }
                linearLayout6.addView(view4);
            }
            this.inlineTodos = null;
        }
        if (this.inlineCustoms != null) {
            for (int i3 = W_REGULAR; i3 < this.inlineCustoms.size(); i3 += W_MEDIUM) {
                linearLayout6.addView(customInlineRow((ReminderPrefs.Custom) this.inlineCustoms.get(i3)));
            }
            this.inlineCustoms = null;
        }
        LinearLayout linearLayout7 = new LinearLayout(this);
        linearLayout7.setOrientation(W_REGULAR);
        linearLayout7.setGravity(16);
        linearLayout7.setPadding(dp(14.0f), W_REGULAR, dp(16.0f), dp(10.0f));
        linearLayout7.addView(new View(this), new LinearLayout.LayoutParams(dp(14.0f), W_MEDIUM));
        txt2.setGravity(5);
        linearLayout7.addView(txt2, new LinearLayout.LayoutParams(dp(56.0f), -2));
        linearLayout6.addView(linearLayout7);
        return linearLayout6;
    }

    public void showCourseDetail(Course course) {
        showCourseDetail(course, null);
    }

    public void showCourseDetail(Course course, final View view) {
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setClickable(true);
        final View view2 = new View(this);
        view2.setBackgroundColor(Color.argb(255, W_REGULAR, W_REGULAR, W_REGULAR));
        view2.setAlpha(0.0f);
        frameLayout.addView(view2, new FrameLayout.LayoutParams(-1, -1));
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.addView(label(course.name, 16.0f, this.ink, W_SEMIBOLD));
        if (course.startSection >= W_MEDIUM) {
            TextView label = label(ClassTimeTable.formatRange(course.startSection, Math.max(course.startSection, course.endSection)), 12.0f, this.muted, W_REGULAR);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = dp(7.0f);
            linearLayout.addView(label, layoutParams);
        }
        if (!course.roomShort().isEmpty()) {
            TextView label2 = label("地点 · " + course.roomShort(), 12.0f, this.muted, W_REGULAR);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = dp(6.0f);
            linearLayout.addView(label2, layoutParams2);
        }
        if (course.teacher != null && !course.teacher.isEmpty()) {
            TextView label3 = label("教师 · " + course.teacher, 12.0f, this.faint, W_REGULAR);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.topMargin = dp(6.0f);
            linearLayout.addView(label3, layoutParams3);
        }
        TextView label4 = label("关闭", 12.0f, this.green, W_MEDIUM);
        label4.setPadding(W_REGULAR, dp(10.0f), W_REGULAR, W_REGULAR);
        linearLayout.addView(label4);
        final FrameLayout frameLayout2 = new FrameLayout(this);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.cardBg);
        gradientDrawable.setCornerRadius(dp(22.0f));
        gradientDrawable.setStroke(Math.max(W_MEDIUM, dp(1.0f)), UiPolish.withAlpha(this.line, 200));
        frameLayout2.setBackground(gradientDrawable);
        frameLayout2.setElevation(dp(16.0f));
        linearLayout.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(14.0f));
        frameLayout2.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(dp(280.0f), -2);
        layoutParams4.gravity = 51;
        frameLayout.addView(frameLayout2, layoutParams4);
        viewGroup.addView(frameLayout, new ViewGroup.LayoutParams(-1, -1));
        this.detailOverlay = frameLayout;
        frameLayout.post(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.95
            @Override // java.lang.Runnable
            public void run() {
                float f;
                float f2;
                final float f3;
                final float f4;
                int width = frameLayout.getWidth();
                int height = frameLayout.getHeight();
                int min = Math.min(width - MainActivity.this.dp(48.0f), MainActivity.this.dp(460.0f));
                if (min <= 0) {
                    min = width;
                }
                frameLayout2.measure(View.MeasureSpec.makeMeasureSpec(min, 1073741824), View.MeasureSpec.makeMeasureSpec(MainActivity.W_REGULAR, MainActivity.W_REGULAR));
                int measuredHeight = frameLayout2.getMeasuredHeight();
                FrameLayout.LayoutParams layoutParams5 = (FrameLayout.LayoutParams) frameLayout2.getLayoutParams();
                layoutParams5.width = min;
                layoutParams5.height = measuredHeight;
                frameLayout2.setLayoutParams(layoutParams5);
                int i = (width - min) / MainActivity.W_SEMIBOLD;
                int max = Math.max(MainActivity.this.dp(56.0f), (height - measuredHeight) / MainActivity.W_SEMIBOLD);
                float f5 = i;
                frameLayout2.setX(f5);
                float f6 = max;
                frameLayout2.setY(f6);
                final float translationX = frameLayout2.getTranslationX();
                final float translationY = frameLayout2.getTranslationY();
                frameLayout2.setTag(new float[]{translationX, translationY});
                float f7 = min;
                float f8 = f7 * 0.5f;
                frameLayout2.setPivotX(f8);
                float f9 = measuredHeight;
                float f10 = f9 * 0.5f;
                frameLayout2.setPivotY(f10);
                float f11 = f5 + f8;
                float f12 = f6 + f10;
                View view3 = view;
                if (view3 == null || view3.getWidth() <= 0 || view.getHeight() <= 0) {
                    f = f11;
                    f2 = f12;
                    f3 = 0.92f;
                    f4 = 0.92f;
                } else {
                    int[] loc = new int[MainActivity.W_SEMIBOLD];
                    view.getLocationInWindow(loc);
                    float max2 = Math.max(0.12f, view.getWidth() / f7);
                    f = loc[MainActivity.W_REGULAR] + (view.getWidth() * 0.5f);
                    f2 = loc[MainActivity.W_MEDIUM] + (view.getHeight() * 0.5f);
                    f3 = max2;
                    f4 = Math.max(0.08f, view.getHeight() / f9);
                }
                final float f13 = f - f11;
                final float f14 = f2 - f12;
                frameLayout2.setScaleX(f3);
                frameLayout2.setScaleY(f4);
                frameLayout2.setTranslationX(translationX + f13);
                frameLayout2.setTranslationY(translationY + f14);
                frameLayout2.setAlpha(0.9f);
                linearLayout.setAlpha(0.0f);
                ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
                ofFloat.setDuration(420L);
                ofFloat.setInterpolator(Motion.ENTER);
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.95.1
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        FrameLayout frameLayout3 = frameLayout2;
                        float f15 = f3;
                        frameLayout3.setScaleX(f15 + ((1.0f - f15) * floatValue));
                        FrameLayout frameLayout4 = frameLayout2;
                        float f16 = f4;
                        frameLayout4.setScaleY(f16 + ((1.0f - f16) * floatValue));
                        float f17 = 1.0f - floatValue;
                        frameLayout2.setTranslationX(translationX + (f13 * f17));
                        frameLayout2.setTranslationY(translationY + (f14 * f17));
                        frameLayout2.setAlpha((floatValue * 0.1f) + 0.9f);
                    }
                });
                ofFloat.start();
                linearLayout.animate().alpha(1.0f).setStartDelay(90L).setDuration(180L).setInterpolator(Motion.STANDARD).start();
                view2.animate().alpha(0.55f).setDuration(180L).setInterpolator(Motion.STANDARD).start();
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.96
            @Override // android.view.View.OnClickListener
            public void onClick(View view3) {
                MainActivity.this.closeDetailSheet(frameLayout, frameLayout2, linearLayout, view2);
            }
        };
        view2.setOnClickListener(onClickListener);
        label4.setOnClickListener(onClickListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeDetailSheet(final FrameLayout frameLayout, final FrameLayout frameLayout2, View view, View view2) {
        if (frameLayout == null || frameLayout.getParent() == null) {
            return;
        }
        view.animate().cancel();
        view.animate().alpha(0.0f).setDuration(90L).setInterpolator(Motion.STANDARD).start();
        final float scaleX = frameLayout2.getScaleX();
        final float scaleY = frameLayout2.getScaleY();
        final float translationX = frameLayout2.getTranslationX();
        final float translationY = frameLayout2.getTranslationY();
        Object tag = frameLayout2.getTag();
        boolean z = tag instanceof float[];
        final float f = z ? ((float[]) tag)[W_REGULAR] : 0.0f;
        final float f2 = z ? ((float[]) tag)[W_MEDIUM] : 0.0f;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setDuration(280L);
        ofFloat.setInterpolator(Motion.STANDARD);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MainActivity.97
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                frameLayout2.setScaleX(((scaleX - 1.0f) * floatValue) + 1.0f);
                frameLayout2.setScaleY(((scaleY - 1.0f) * floatValue) + 1.0f);
                FrameLayout frameLayout3 = frameLayout2;
                float f3 = f;
                frameLayout3.setTranslationX(f3 + ((translationX - f3) * floatValue));
                FrameLayout frameLayout4 = frameLayout2;
                float f4 = f2;
                frameLayout4.setTranslationY(f4 + ((translationY - f4) * floatValue));
                frameLayout2.setAlpha((floatValue * 0.1f) + 0.9f);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.MainActivity.98
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                ViewParent parent = frameLayout.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(frameLayout);
                }
            }
        });
        ofFloat.start();
        view2.animate().alpha(0.0f).setDuration(180L).setInterpolator(Motion.STANDARD).start();
    }

    private View separatorRow() {
        LinearLayout linearLayout = new LinearLayout(this);
        View view = new View(this);
        view.setBackgroundColor(UiPolish.withAlpha(this.ink, this.dark ? 20 : 14));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(1.0f));
        layoutParams.leftMargin = dp(14.0f);
        layoutParams.rightMargin = dp(16.0f);
        linearLayout.setPadding(W_REGULAR, dp(5.0f), W_REGULAR, dp(5.0f));
        linearLayout.addView(view, layoutParams);
        return linearLayout;
    }

    private View courseCard(Course course) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(18.0f), dp(16.0f), dp(18.0f), dp(16.0f));
        UiPolish.glass(linearLayout, this.cardBg, this.line, 20.0f);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        linearLayout2.addView(txt(course.name, 15, this.ink, true), new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f));
        TextView txt = txt(course.sections(), 11, this.green, true);
        txt.setPadding(dp(10.0f), dp(4.0f), dp(10.0f), dp(4.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(UiPolish.withAlpha(this.accentA, this.dark ? 42 : 28));
        gradientDrawable.setCornerRadius(dp(12.0f));
        txt.setBackground(gradientDrawable);
        linearLayout2.addView(txt);
        linearLayout.addView(linearLayout2);
        linearLayout.addView(txt(course.timeRange(), 12, this.muted, false), top(8));
        StringBuilder sb = new StringBuilder();
        if (course.teacher != null && !course.teacher.isEmpty()) {
            sb.append(course.teacher);
        }
        if (!course.roomShort().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(course.roomShort());
        }
        if (sb.length() > 0) {
            linearLayout.addView(txt(sb.toString(), 11, this.faint, false), top(6));
        }
        UiPolish.pressable(linearLayout);
        return linearLayout;
    }

    public void trySilentReauth(final Runnable runnable, final Runnable runnable2) {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.silentAuthRunning || currentTimeMillis - this.lastSilentAuthAt < 25000) {
            if (runnable2 != null) {
                runnable2.run();
            }
        } else {
            this.silentAuthRunning = true;
            this.lastSilentAuthAt = currentTimeMillis;
            setStatus("登录状态已失效，正在自动续期…");
            new SilentAuth(this, this.silentHost, new SilentAuth.Callback() { // from class: com.zuwe.smartcourselite.MainActivity.99
                @Override // com.zuwe.smartcourselite.SilentAuth.Callback
                public void onResult(boolean z) {
                    MainActivity.this.silentAuthRunning = false;
                    if (z && MainActivity.this.ready()) {
                        MainActivity.this.refreshCredentialLine();
                        MainActivity.this.setStatus("登录状态已自动续期");
                        Runnable runnable3 = runnable;
                        if (runnable3 != null) {
                            runnable3.run();
                            return;
                        }
                        return;
                    }
                    Runnable runnable4 = runnable2;
                    if (runnable4 != null) {
                        runnable4.run();
                    }
                }
            }).start(9000L);
        }
    }

    public void showReauthPrompt(boolean z) {
        setStatus("服务器拒绝当前凭证，需重新授权");
        LinearLayout linearLayout = this.courseList;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            LinearLayout panel = panel();
            panel.addView(txt("凭证已失效", 14, this.ink, true));
            panel.addView(txt("自动续期没有成功，请重新完成官方授权后刷新。", 12, this.faint, false), top(6));
            this.courseList.addView(panel);
        }
        if (z) {
            new AlertDialog.Builder(this).setTitle("需要重新授权").setMessage("当前登录凭证已失效或无权限。现在打开智慧思政官方登录页面重新授权？").setNegativeButton("稍后", (DialogInterface.OnClickListener) null).setPositiveButton("重新授权", new DialogInterface.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.100
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.startOAuth(true);
                }
            }).show();
        }
    }

    private void handleApiError(Exception exc, final boolean z) {
        String simpleName = exc.getMessage() == null ? exc.getClass().getSimpleName() : exc.getMessage();
        int i = exc instanceof SchoolApi.ApiException ? ((SchoolApi.ApiException) exc).httpStatus : -1;
        if (i == 401 || i == 403) {
            AuthManager.markUnauthorized(this);
            setStatus("服务器拒绝当前凭证，正在尝试自动续期…");
            trySilentReauth(new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.101
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.lastData = null;
                    MainActivity.this.refresh(true);
                }
            }, new Runnable() { // from class: com.zuwe.smartcourselite.MainActivity.102
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.showReauthPrompt(z);
                }
            });
            return;
        }
        boolean isTransient = isTransient(exc);
        List<Course> loadCache = loadCache(this.viewDate);
        if (loadCache != null) {
            render(loadCache);
            StringBuilder sb = new StringBuilder();
            sb.append(isTransient ? "网络超时，显示 " : "刷新失败，显示 ");
            sb.append(cacheAge(this.viewDate));
            setStatus(sb.toString());
            return;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(isTransient ? "网络超时：" : "请求失败：");
        sb2.append(simpleName);
        setStatus(sb2.toString());
        LinearLayout linearLayout = this.courseList;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            LinearLayout panel = panel();
            panel.addView(txt(isTransient ? "网络超时" : "加载失败", 14, this.ink, true));
            panel.addView(txt(simpleName, 12, this.faint, false), top(6));
            Button ghostBtn = ghostBtn("重试");
            ghostBtn.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.MainActivity.103
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MainActivity.this.refresh(true);
                }
            });
            panel.addView(ghostBtn, top(12));
            this.courseList.addView(panel);
        }
    }

    private static boolean isTransient(Exception exc) {
        if ((exc instanceof SocketTimeoutException) || (exc instanceof UnknownHostException) || (exc instanceof ConnectException)) {
            return true;
        }
        if (!(exc instanceof IOException)) {
            return false;
        }
        String simpleName = exc.getClass().getSimpleName();
        return simpleName.contains("Timeout") || simpleName.contains("Connect") || simpleName.contains("Network");
    }

    public void setStatus(String str) {
        TextView textView = this.status;
        if (textView == null) {
            return;
        }
        CharSequence text = textView.getText();
        if (text != null) {
            if (text.toString().contentEquals(str == null ? "" : str)) {
                return;
            }
        }
        textView.animate().cancel();
        textView.setAlpha(0.25f);
        textView.setText(str);
        textView.animate().alpha(1.0f).setDuration(180L).setInterpolator(Motion.ENTER).start();
    }

    public void toast(String str) {
        Toast.makeText(this, str, W_REGULAR).show();
    }

    private TextView eyebrow(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(9.0f);
        textView.setTextColor(this.faint);
        textView.setLetterSpacing(0.18f);
        textView.setAllCaps(true);
        return textView;
    }

    private View hairline() {
        View view = new View(this);
        view.setBackgroundColor(this.line);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1.0f)));
        return view;
    }

    private View sectionHead(String str, String str2, String str3) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(W_REGULAR);
        linearLayout2.setGravity(16);
        linearLayout2.addView(label(str2, 21.0f, this.ink, W_SEMIBOLD));
        TextView label = label(str3, 13.0f, this.faint, W_REGULAR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(W_REGULAR, -2, 1.0f);
        layoutParams.leftMargin = dp(10.0f);
        label.setGravity(5);
        linearLayout2.addView(label, layoutParams);
        linearLayout.addView(linearLayout2);
        return linearLayout;
    }

    private LinearLayout panel() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        UiPolish.glass(linearLayout, this.cardBg, this.line, 20.0f);
        return linearLayout;
    }

    private LinearLayout bigPanel() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(W_MEDIUM);
        linearLayout.setPadding(dp(24.0f), dp(24.0f), dp(24.0f), dp(24.0f));
        UiPolish.glass(linearLayout, this.cardBg, this.line, 26.0f);
        return linearLayout;
    }

    private TextView txt(String str, int i, int i2, boolean z) {
        return label(str, i, i2, z ? W_SEMIBOLD : W_REGULAR);
    }

    private void playTitleFx(View view, long j) {
        if (view instanceof TextFx.StaggerText) {
            ((TextFx.StaggerText) view).play(j);
        }
    }

    private TextView label(String str, float f, int i, int i2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        if (i2 == W_MEDIUM) {
            textView.setTypeface(TF_MEDIUM, W_REGULAR);
            return textView;
        }
        if (i2 == W_SEMIBOLD) {
            textView.setTypeface(TF_SEMIBOLD, W_REGULAR);
            return textView;
        }
        textView.setTypeface(Typeface.DEFAULT, W_REGULAR);
        return textView;
    }

    private Button primaryBtn(String str) {
        Button baseBtn = baseBtn(str);
        baseBtn.setTextColor(this.onGreen);
        baseBtn.setTextSize(15.0f);
        baseBtn.setTypeface(TF_SEMIBOLD, W_REGULAR);
        baseBtn.setMinHeight(dp(56.0f));
        baseBtn.setMinimumHeight(dp(56.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.green);
        gradientDrawable.setCornerRadius(dp(16.0f));
        baseBtn.setBackground(gradientDrawable);
        UiPolish.pressable(baseBtn);
        return baseBtn;
    }

    private FrameLayout circleChip(String str, String str2) {
        FrameLayout frameLayout = new FrameLayout(this);
        Haptics.attach(frameLayout);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(W_MEDIUM);
        gradientDrawable.setColor(this.bgAlt);
        frameLayout.setBackground(new RippleDrawable(ColorStateList.valueOf(UiPolish.withAlpha(this.ink, 24)), gradientDrawable, null));
        frameLayout.addView(SvgIcon.view(this, str, this.muted, 22), new FrameLayout.LayoutParams(dp(22.0f), dp(22.0f), 17));
        frameLayout.setContentDescription(str2);
        frameLayout.setClickable(true);
        UiPolish.pressable(frameLayout);
        return frameLayout;
    }

    private FrameLayout iconChip(String str) {
        FrameLayout frameLayout = new FrameLayout(this);
        Haptics.attach(frameLayout);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(20.0f));
        frameLayout.setBackground(new RippleDrawable(ColorStateList.valueOf(UiPolish.withAlpha(this.ink, 26)), gradientDrawable, null));
        frameLayout.addView(SvgIcon.view(this, str, this.muted, 22), new FrameLayout.LayoutParams(dp(22.0f), dp(22.0f), 17));
        frameLayout.setClickable(true);
        UiPolish.pressable(frameLayout);
        return frameLayout;
    }

    private Button ghostBtn(String str) {
        Button baseBtn = baseBtn(str);
        baseBtn.setTextColor(this.ink);
        baseBtn.setTextSize(13.0f);
        baseBtn.setMinHeight(dp(48.0f));
        baseBtn.setMinimumHeight(dp(48.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(12.0f));
        gradientDrawable.setStroke(dp(1.0f), this.line);
        baseBtn.setBackground(gradientDrawable);
        UiPolish.pressable(baseBtn);
        return baseBtn;
    }

    private Button baseBtn(String str) {
        Button button = new Button(this);
        Haptics.attach(button);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(13.0f);
        button.setMinHeight(dp(46.0f));
        button.setPadding(dp(16.0f), W_REGULAR, dp(16.0f), W_REGULAR);
        button.setStateListAnimator(null);
        return button;
    }

    private Button tabBtn(String str) {
        Button baseBtn = baseBtn(str);
        baseBtn.setTextSize(13.0f);
        baseBtn.setMinHeight(dp(44.0f));
        baseBtn.setTextColor(this.muted);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(22.0f));
        baseBtn.setBackground(gradientDrawable);
        UiPolish.pressable(baseBtn);
        return baseBtn;
    }

    private Button arrowBtn(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(18.0f);
        button.setAllCaps(false);
        button.setTextColor(this.ink);
        button.setPadding(W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR);
        button.setMinWidth(dp(40.0f));
        button.setMinimumWidth(dp(40.0f));
        button.setMinHeight(dp(40.0f));
        button.setMinimumHeight(dp(40.0f));
        button.setStateListAnimator(null);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(20.0f));
        button.setBackground(gradientDrawable);
        return button;
    }

    private Button roundBtn(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(15.0f);
        button.setAllCaps(false);
        button.setTextColor(this.ink);
        button.setPadding(W_REGULAR, W_REGULAR, W_REGULAR, W_REGULAR);
        button.setMinWidth(dp(38.0f));
        button.setMinimumWidth(dp(38.0f));
        button.setMinHeight(dp(38.0f));
        button.setMinimumHeight(dp(38.0f));
        button.setStateListAnimator(null);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.bgAlt);
        gradientDrawable.setCornerRadius(dp(20.0f));
        button.setBackground(gradientDrawable);
        return button;
    }

    private LinearLayout.LayoutParams top(int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = dp(i);
        return layoutParams;
    }

    public int dp(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        stopLocationRequest();
        super.onDestroy();
        this.io.shutdownNow();
    }
}
