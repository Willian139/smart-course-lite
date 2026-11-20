package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class AlarmActivity extends Activity {
    public static final String EXTRA_ID = "alarm_id";
    public static final String EXTRA_TEXT = "alarm_text";
    public static final String EXTRA_TITLE = "alarm_title";
    private static final AtomicReference<AlarmActivity> LIVE_REF = new AtomicReference<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoDismiss = new Runnable() { // from class: com.zuwe.smartcourselite.AlarmActivity.1
        @Override // java.lang.Runnable
        public void run() {
            AlarmActivity.this.finishAlarm();
        }
    };
    private int noteId = ReminderNotifier.LIVE_ID;
    private String alarmTitle = "";
    private String alarmText = "";

    private static String safe(String str) {
        return str == null ? "" : str;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AlarmActivity andSet = LIVE_REF.getAndSet(this);
        if (andSet != null && andSet != this) {
            try {
                andSet.finish();
            } catch (Exception unused) {
            }
        }
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(2621440);
        }
        getWindow().addFlags(4194432);
        Intent intent = getIntent();
        if (intent != null) {
            this.alarmTitle = safe(intent.getStringExtra(EXTRA_TITLE));
            this.alarmText = safe(intent.getStringExtra(EXTRA_TEXT));
            this.noteId = intent.getIntExtra(EXTRA_ID, ReminderNotifier.LIVE_ID);
        }
        if (this.alarmTitle.isEmpty()) {
            this.alarmTitle = "提醒时间到了";
        }
        buildUi();
        ReminderPrefs.pushLog(this, "闹钟页已显示");
        if (isStale()) {
            ReminderPrefs.pushLog(this, "闹钟页判定为已停止的残留，静默退出");
            ReminderNotifier.cancelAll(this);
            AlarmActivity$$ExternalSyntheticBackportWithForwarding0.m(LIVE_REF, this, null);
            finish();
            return;
        }
        AlarmTone.start(this, 120000L);
        this.handler.postDelayed(this.autoDismiss, 120000L);
    }

    private boolean isStale() {
        long stoppedAt = ReminderPrefs.stoppedAt(this);
        long notifiedAt = ReminderPrefs.notifiedAt(this);
        return stoppedAt > 0 && notifiedAt > 0 && stoppedAt >= notifiedAt && System.currentTimeMillis() - stoppedAt < 180000;
    }

    private int dp(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void buildUi() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(-15986931);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(1);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2, 17);
        linearLayout.setPadding(dp(28.0f), 0, dp(28.0f), 0);
        TextView textView = new TextView(this);
        textView.setText("闹 钟");
        textView.setTextSize(12.0f);
        textView.setLetterSpacing(0.42f);
        textView.setTextColor(-8418175);
        textView.setGravity(17);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(-1, -2));
        TextView textView2 = new TextView(this);
        textView2.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        textView2.setTextSize(78.0f);
        textView2.setTypeface(Typeface.create("sans-serif-light", 0));
        textView2.setTextColor(-788749);
        textView2.setGravity(17);
        textView2.setIncludeFontPadding(false);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.topMargin = dp(6.0f);
        linearLayout.addView(textView2, layoutParams2);
        TextView textView3 = new TextView(this);
        textView3.setText(this.alarmTitle);
        textView3.setTextSize(21.0f);
        textView3.setTypeface(Typeface.create("sans-serif-medium", 0));
        textView3.setTextColor(-1);
        textView3.setGravity(17);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp(18.0f);
        linearLayout.addView(textView3, layoutParams3);
        if (!this.alarmText.isEmpty()) {
            TextView textView4 = new TextView(this);
            textView4.setText(this.alarmText);
            textView4.setTextSize(15.0f);
            textView4.setTextColor(-5589588);
            textView4.setGravity(17);
            textView4.setLineSpacing(dp(4.0f), 1.0f);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams4.topMargin = dp(8.0f);
            linearLayout.addView(textView4, layoutParams4);
        }
        TextView textView5 = new TextView(this);
        textView5.setText("响铃 2 分钟后自动停止");
        textView5.setTextSize(11.0f);
        textView5.setTextColor(-10589600);
        textView5.setGravity(17);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams5.topMargin = dp(26.0f);
        linearLayout.addView(textView5, layoutParams5);
        frameLayout.addView(linearLayout, layoutParams);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        FrameLayout.LayoutParams layoutParams6 = new FrameLayout.LayoutParams(-1, -2, 81);
        layoutParams6.setMargins(dp(22.0f), 0, dp(22.0f), dp(40.0f));
        TextView alarmAction = alarmAction("稍后 5 分钟", 0, -12959173, -2695465);
        alarmAction.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AlarmActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Haptics.tick(view);
                AlarmActivity alarmActivity = AlarmActivity.this;
                ReminderScheduler.scheduleSnooze(alarmActivity, 5, alarmActivity.alarmTitle, AlarmActivity.this.alarmText);
                AlarmActivity.this.finishAlarm();
            }
        });
        LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(0, dp(54.0f), 1.0f);
        layoutParams7.rightMargin = dp(10.0f);
        linearLayout2.addView(alarmAction, layoutParams7);
        TextView alarmAction2 = alarmAction("我知道了", -6559564, 0, -15986931);
        alarmAction2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AlarmActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Haptics.confirm(view);
                AlarmActivity.this.finishAlarm();
            }
        });
        linearLayout2.addView(alarmAction2, new LinearLayout.LayoutParams(0, dp(54.0f), 1.0f));
        frameLayout.addView(linearLayout2, layoutParams6);
        setContentView(frameLayout);
    }

    private TextView alarmAction(String str, int i, int i2, int i3) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(15.0f);
        textView.setTypeface(Typeface.create("sans-serif-medium", 0));
        textView.setTextColor(i3);
        textView.setGravity(17);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dp(16.0f));
        gradientDrawable.setColor(i);
        if (i2 != 0) {
            gradientDrawable.setStroke(dp(1.0f), i2);
        }
        textView.setBackground(gradientDrawable);
        return textView;
    }

    public static void dismissLive() {
        AlarmActivity andSet = LIVE_REF.getAndSet(null);
        if (andSet != null) {
            try {
                andSet.finishAlarm();
            } catch (Exception unused) {
            }
        }
    }

    public void finishAlarm() {
        try {
            this.handler.removeCallbacks(this.autoDismiss);
        } catch (Exception unused) {
        }
        AlarmTone.stop();
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (Exception unused2) {
        }
        ReminderPrefs.markStopped(this);
        if (isTaskRoot()) {
            try {
                startActivity(new Intent(this, (Class<?>) MainActivity.class).addFlags(805306368));
            } catch (Exception unused3) {
            }
        }
        AlarmActivity$$ExternalSyntheticBackportWithForwarding0.m(LIVE_REF, this, null);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        AlarmActivity$$ExternalSyntheticBackportWithForwarding0.m(LIVE_REF, this, null);
        try {
            this.handler.removeCallbacks(this.autoDismiss);
        } catch (Exception unused) {
        }
        AlarmTone.stop();
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (AlarmTone.ringing()) {
            return;
        }
        AlarmTone.start(this, 120000L);
    }
}
