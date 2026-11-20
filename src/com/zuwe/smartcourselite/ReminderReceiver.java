package com.zuwe.smartcourselite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/* loaded from: classes.dex */
public final class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_SNOOZE = "com.zuwe.smartcourselite.action.SNOOZE";
    public static final String ACTION_STOP = "com.zuwe.smartcourselite.action.STOP";
    public static final String EXTRA_ID = "note_id";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_TITLE = "title";

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        boolean z;
        String str;
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_STOP.equals(action)) {
            ReminderPrefs.markStopped(context);
            AlarmTone.stop();
            AlarmActivity.dismissLive();
            ReminderNotifier.cancelAll(context);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.ReminderReceiver.1
                @Override // java.lang.Runnable
                public void run() {
                    ReminderNotifier.cancelAll(context);
                }
            }, 400L);
            ReminderPrefs.pushLog(context, "提醒已停止");
            return;
        }
        if (ACTION_SNOOZE.equals(action)) {
            AlarmTone.stop();
            ReminderNotifier.cancelAll(context);
            String stringExtra = intent.getStringExtra(EXTRA_TITLE);
            String stringExtra2 = intent.getStringExtra(EXTRA_TEXT);
            if (stringExtra == null) {
                stringExtra = "稍后提醒";
            }
            ReminderScheduler.scheduleSnooze(context, 5, stringExtra, stringExtra2 != null ? stringExtra2 : "");
            return;
        }
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            ReminderScheduler.reschedule(context);
            return;
        }
        String stringExtra3 = intent.getStringExtra(EXTRA_TITLE);
        String stringExtra4 = intent.getStringExtra(EXTRA_TEXT);
        if (stringExtra3 == null) {
            return;
        }
        if (!ReminderPrefs.firstFireWithin(context, "slot:" + (System.currentTimeMillis() / 60000))) {
            ReminderPrefs.pushLog(context, "重复触发已忽略：" + stringExtra3);
            return;
        }
        long stoppedAt = ReminderPrefs.stoppedAt(context);
        long notifiedAt = ReminderPrefs.notifiedAt(context);
        if (stoppedAt > 0 && stoppedAt >= notifiedAt && System.currentTimeMillis() - stoppedAt < 45000) {
            ReminderPrefs.pushLog(context, "已停止过，忽略补投：" + stringExtra3);
            return;
        }
        long currentTimeMillis = System.currentTimeMillis() - notifiedAt;
        if (currentTimeMillis >= 0 && currentTimeMillis < 45000 && AlarmTone.ringing()) {
            ReminderPrefs.pushLog(context, "同一时段已在提醒，合并：" + stringExtra3);
            return;
        }
        ReminderPrefs.markNotified(context);
        ReminderPrefs.markWake(context, stringExtra3);
        int currentTimeMillis2 = (int) (System.currentTimeMillis() & 65535);
        ReminderPrefs.pushLog(context, "闹钟触发：" + stringExtra3);
        try {
            long longExtra = intent.getLongExtra("fire_at", 0L);
            if (longExtra > 0) {
                DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("HH:mm:ss");
                String format = LocalTime.now().format(ofPattern);
                String format2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(longExtra), ZoneId.systemDefault()).format(ofPattern);
                long currentTimeMillis3 = (System.currentTimeMillis() - longExtra) / 1000;
                StringBuilder sb = new StringBuilder("触发 ");
                sb.append(format);
                sb.append(" · 预定 ");
                sb.append(format2);
                if (currentTimeMillis3 > 30) {
                    str = " · 延迟 " + currentTimeMillis3 + " 秒（系统冻结导致，去系统设置关掉电池优化）";
                } else {
                    str = "";
                }
                sb.append(str);
                ReminderPrefs.pushLog(context, sb.toString());
            }
            z = ReminderNotifier.notifySmart(context, stringExtra3, stringExtra4 == null ? "" : stringExtra4, currentTimeMillis2);
        } catch (Exception e) {
            ReminderPrefs.pushLog(context, "通知阶段异常：" + e.getClass().getSimpleName());
            z = false;
        }
        try {
            ReminderPrefs.pushLog(context, "开始响铃");
            AlarmTone.start(context, 180000L);
            StringBuilder sb2 = new StringBuilder("已响铃：");
            sb2.append(AlarmTone.describe());
            sb2.append(z ? "" : "（通知未送达）");
            ReminderPrefs.pushLog(context, sb2.toString());
        } catch (Exception e2) {
            ReminderPrefs.pushLog(context, "响铃异常：" + e2.getClass().getSimpleName());
        }
        try {
            ReminderPrefs.pushLog(context, AlarmOverlay.show(context, stringExtra3, stringExtra4 == null ? "" : stringExtra4) ? "悬浮窗闹钟已显示" : "悬浮窗闹钟未显示（没有「显示在其他应用上层」权限）");
        } catch (Exception e3) {
            ReminderPrefs.pushLog(context, "悬浮窗闹钟异常：" + e3.getClass().getSimpleName());
        }
        try {
            Intent intent2 = new Intent(context, (Class<?>) AlarmActivity.class);
            intent2.addFlags(805306368);
            intent2.putExtra(AlarmActivity.EXTRA_TITLE, stringExtra3);
            intent2.putExtra(AlarmActivity.EXTRA_TEXT, stringExtra4 != null ? stringExtra4 : "");
            intent2.putExtra(AlarmActivity.EXTRA_ID, currentTimeMillis2);
            context.startActivity(intent2);
            ReminderPrefs.pushLog(context, "闹钟页已拉起");
        } catch (Exception e4) {
            ReminderPrefs.pushLog(context, "闹钟界面启动失败：" + e4.getClass().getSimpleName());
        }
    }
}
