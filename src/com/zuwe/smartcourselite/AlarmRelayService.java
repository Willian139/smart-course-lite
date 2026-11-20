package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/* loaded from: classes.dex */
public final class AlarmRelayService extends NotificationListenerService {
    private static final String KEY = "relay";

    public static boolean enabled(Context context) {
        try {
            String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
            if (string != null) {
                return string.contains(context.getPackageName());
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    public static void request(Activity activity) {
        try {
            activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception e) {
            try {
                activity.startActivity(new Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS"));
            } catch (Exception unused) {
                ReminderPrefs.pushLog(activity, "打不开通知使用权设置：" + e.getClass().getSimpleName());
            }
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        try {
            handle(statusBarNotification);
        } catch (Exception e) {
            ReminderPrefs.pushLog(this, "中继异常：" + e.getClass().getSimpleName());
        }
    }

    private void handle(StatusBarNotification statusBarNotification) {
        Bundle bundle;
        if (statusBarNotification == null || statusBarNotification.getNotification() == null || getPackageName().equals(statusBarNotification.getPackageName()) || (bundle = statusBarNotification.getNotification().extras) == null) {
            return;
        }
        String text = text(bundle, "android.title");
        String text2 = text(bundle, "android.text");
        String text3 = text(bundle, "android.bigText");
        String str = text + "\n" + text2 + "\n" + text3;
        if (str.contains("⏰") || str.contains("智慧思政课表")) {
            if (text.isEmpty()) {
                text = "提醒时间到了";
            }
            if (!text3.isEmpty()) {
                text2 = text3;
            }
            if (!ReminderPrefs.firstFireWithin(this, "slot:" + (System.currentTimeMillis() / 60000))) {
                ReminderPrefs.pushLog(this, "本分钟已提醒过，跳过日历通知：" + text);
                return;
            }
            ReminderPrefs.pushLog(this, "日历通知到达，升级为闹钟：" + text);
            ReminderPrefs.markNotified(this);
            ReminderService.start(this);
            ReminderPrefs.pushLog(this, ReminderNotifier.notifySmart(this, text, text2, 0) ? "中继通知已送达" : "中继通知未送达（权限/渠道）");
            AlarmTone.start(this, 120000L);
            try {
                ReminderPrefs.pushLog(this, AlarmOverlay.show(this, text, text2) ? "中继悬浮窗已显示" : "中继悬浮窗未显示（没有悬浮窗权限）");
            } catch (Exception unused) {
            }
            ReminderPrefs.pushLog(this, "中继响铃：" + AlarmTone.describe());
            try {
                Intent intent = new Intent(this, (Class<?>) AlarmActivity.class);
                intent.addFlags(805306368);
                intent.putExtra(AlarmActivity.EXTRA_TITLE, text);
                intent.putExtra(AlarmActivity.EXTRA_TEXT, text2);
                startActivity(intent);
                ReminderPrefs.pushLog(this, "中继已拉起闹钟页");
            } catch (Exception e) {
                ReminderPrefs.pushLog(this, "中继拉起闹钟页失败：" + e.getClass().getSimpleName());
            }
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        if (statusBarNotification != null) {
            try {
                if (getPackageName().equals(statusBarNotification.getPackageName()) && statusBarNotification.getId() == 880001) {
                    ReminderPrefs.pushLog(this, "通知被划掉，停止响铃");
                    ReminderPrefs.markStopped(this);
                    AlarmTone.stop();
                    AlarmActivity.dismissLive();
                    ReminderNotifier.cancelAll(this);
                }
            } catch (Exception e) {
                ReminderPrefs.pushLog(this, "划掉通知后停铃异常：" + e.getClass().getSimpleName());
            }
        }
    }

    private static String text(Bundle bundle, String str) {
        CharSequence charSequence = bundle.getCharSequence(str);
        return charSequence == null ? "" : charSequence.toString();
    }
}
