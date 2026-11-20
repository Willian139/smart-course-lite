package com.zuwe.smartcourselite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.Toast;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class ReminderService extends Service {
    private static final String CHANNEL = "smartcourse_keepalive";
    private static final int ID_ONGOING = 4100;
    private final Runnable fire = new Runnable() { // from class: com.zuwe.smartcourselite.ReminderService.1
        @Override // java.lang.Runnable
        public void run() {
            if (ReminderService.this.nextAt <= 0 || System.currentTimeMillis() - ReminderService.this.nextAt <= 15000) {
                try {
                    String str = ReminderService.this.nextKey;
                    if (str != null && ReminderPrefs.firstFireWithin(ReminderService.this, str)) {
                        ReminderService reminderService = ReminderService.this;
                        ReminderNotifier.notify(reminderService, reminderService.nextTitle, ReminderService.this.nextText, Math.abs((ReminderService.this.nextKey == null ? "" : ReminderService.this.nextKey).hashCode()));
                        ReminderPrefs.pushLog(ReminderService.this, "服务触发提醒：" + ReminderService.this.nextTitle);
                        ReminderService reminderService2 = ReminderService.this;
                        ReminderPrefs.markWake(reminderService2, reminderService2.nextTitle);
                    }
                } catch (Exception e) {
                    ReminderPrefs.pushLog(ReminderService.this, "服务触发失败：" + e.getClass().getSimpleName());
                }
                ReminderService.this.armNext();
                return;
            }
            ReminderPrefs.pushLog(ReminderService.this, "跳过过期提醒");
            ReminderService.this.nextAt = 0L;
            ReminderService.this.armNext();
        }
    };
    private Handler handler;
    private long loggedNextAt;
    private boolean loggedStart;
    private long nextAt;
    private String nextKey;
    private String nextText;
    private String nextTitle;
    private PowerManager.WakeLock wakeLock;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void toast(String str) {
        try {
            Toast.makeText(this, str, 0).show();
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            startForeground(ID_ONGOING, buildOngoing());
            if (!this.loggedStart) {
                this.loggedStart = true;
                toast("提醒服务已启动");
                ReminderPrefs.pushLog(this, "前台服务已启动");
            }
        } catch (Exception e) {
            ReminderPrefs.pushLog(this, "前台服务启动失败：" + e.getClass().getSimpleName() + " " + e.getMessage());
            if (!this.loggedStart) {
                this.loggedStart = true;
                toast("提醒服务启动失败：" + e.getClass().getSimpleName());
            }
        }
        acquireWakeLock();
        armNext();
        return 1;
    }

    private void acquireWakeLock() {
        PowerManager powerManager;
        try {
            if (this.wakeLock == null && (powerManager = (PowerManager) getSystemService("power")) != null) {
                PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(1, "smartcourse:reminder");
                this.wakeLock = newWakeLock;
                newWakeLock.setReferenceCounted(false);
            }
            PowerManager.WakeLock wakeLock = this.wakeLock;
            if (wakeLock != null && !wakeLock.isHeld()) {
                this.wakeLock.acquire(43200000L);
            }
        } catch (Exception unused) {
        }
    }

    public void armNext() {
        long j;
        this.handler.removeCallbacks(this.fire);
        long currentTimeMillis = System.currentTimeMillis();
        this.nextKey = null;
        this.nextTitle = null;
        this.nextText = null;
        try {
            JSONArray jSONArray = new JSONArray(ReminderPrefs.schedule(this));
            j = 0;
            for (int i = 0; i < jSONArray.length(); i++) {
                try {
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        long optLong = optJSONObject.optLong("at", 0L);
                        if (optLong > currentTimeMillis && (j == 0 || optLong < j)) {
                            try {
                                this.nextKey = optJSONObject.optString("key");
                                this.nextTitle = optJSONObject.optString(ReminderReceiver.EXTRA_TITLE);
                                this.nextText = optJSONObject.optString(ReminderReceiver.EXTRA_TEXT);
                            } catch (Exception unused) {
                            }
                            j = optLong;
                        }
                    }
                } catch (Exception unused2) {
                }
            }
        } catch (Exception unused3) {
            j = 0;
        }
        if (j == 0) {
            this.nextAt = 0L;
            return;
        }
        this.nextAt = j;
        this.handler.postDelayed(this.fire, Math.max(500L, j - currentTimeMillis));
        if (j != this.loggedNextAt) {
            this.loggedNextAt = j;
            ReminderPrefs.pushLog(this, "下一次提醒 " + LocalDateTime.ofInstant(Instant.ofEpochMilli(j), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M月d日 HH:mm")));
        }
    }

    private Notification buildOngoing() {
        Notification.Builder builder;
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        if (Build.VERSION.SDK_INT >= 26 && notificationManager != null) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL, "提醒服务", 1);
            notificationChannel.setShowBadge(false);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("上课提醒已开启").setContentText("到点会在这里提醒你").setOngoing(true).setPriority(-2);
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.setFlags(268435456);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, (Build.VERSION.SDK_INT >= 23 ? 67108864 : 0) | 134217728));
        return builder.build();
    }

    @Override // android.app.Service
    public void onDestroy() {
        try {
            this.handler.removeCallbacks(this.fire);
        } catch (Exception unused) {
        }
        try {
            PowerManager.WakeLock wakeLock = this.wakeLock;
            if (wakeLock != null && wakeLock.isHeld()) {
                this.wakeLock.release();
            }
        } catch (Exception unused2) {
        }
        ReminderPrefs.pushLog(this, "前台服务已停止");
        stopForeground(true);
        super.onDestroy();
    }

    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, (Class<?>) ReminderService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            ReminderPrefs.pushLog(context, "启动服务失败：" + e.getClass().getSimpleName());
        }
    }

    public static void stop(Context context) {
        try {
            context.stopService(new Intent(context, (Class<?>) ReminderService.class));
        } catch (Exception unused) {
        }
    }
}
