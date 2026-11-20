package com.zuwe.smartcourselite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;

/* loaded from: classes.dex */
public final class ReminderNotifier {
    private static final String CHANNEL = "smartcourse_alarm_v1";
    private static final String CHANNEL_NEW = "smartcourse_alarm_v3";
    private static final String CHANNEL_PLAIN = "smartcourse_alarm_plain";
    public static final int LIVE_ID = 880001;

    private ReminderNotifier() {
    }

    private static NotificationManager manager(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    public static void ensureChannel(Context context) {
        NotificationManager manager;
        if (Build.VERSION.SDK_INT < 26 || (manager = manager(context)) == null) {
            return;
        }
        if (manager.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL, "到点提醒（闹钟）", 4);
            notificationChannel.setDescription("到点后全屏响铃提醒，直到你处理为止");
            notificationChannel.setLockscreenVisibility(1);
            notificationChannel.setShowBadge(true);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            manager.createNotificationChannel(notificationChannel);
        }
        if (manager.getNotificationChannel(CHANNEL_PLAIN) == null) {
            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_PLAIN, "到点提醒（备用）", 4);
            notificationChannel2.setLockscreenVisibility(1);
            notificationChannel2.setShowBadge(true);
            manager.createNotificationChannel(notificationChannel2);
        }
    }

    private static boolean notificationsAllowed(Context context) {
        if (Build.VERSION.SDK_INT < 33) {
            return true;
        }
        try {
            return context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0;
        } catch (Exception unused) {
            return true;
        }
    }

    public static boolean notifySmart(Context context, String str, String str2, int i) {
        ensureChannel(context);
        if (!notificationsAllowed(context)) {
            ReminderPrefs.pushLog(context, "通知权限未授予：只会响铃，不会出现通知（去系统设置允许通知）");
        }
        NotificationManager manager = manager(context);
        if (manager == null) {
            ReminderPrefs.pushLog(context, "通知失败：系统服务不可用");
            return false;
        }
        if (str2 == null) {
            str2 = "";
        }
        try {
            manager.notify(LIVE_ID, build(context, CHANNEL, str, str2, LIVE_ID, true));
            ReminderPrefs.markNotified(context);
        } catch (Exception e) {
            ReminderPrefs.setNotifyError(context, e.getClass().getSimpleName() + ": " + e.getMessage());
            StringBuilder sb = new StringBuilder("通知异常：");
            sb.append(e.getClass().getSimpleName());
            ReminderPrefs.pushLog(context, sb.toString());
        }
        if (delivered(manager, LIVE_ID)) {
            ReminderPrefs.setNotifyError(context, "");
            ReminderPrefs.pushLog(context, "通知已送达：" + str);
            return true;
        }
        ReminderPrefs.pushLog(context, "通知未生效，改用备用渠道");
        try {
            manager.notify(880008, build(context, CHANNEL_PLAIN, str, str2, LIVE_ID, false));
        } catch (Exception e2) {
            ReminderPrefs.pushLog(context, "备用渠道异常：" + e2.getClass().getSimpleName());
        }
        if (delivered(manager, 880008)) {
            ReminderPrefs.pushLog(context, "备用渠道已送达");
            return true;
        }
        ReminderPrefs.pushLog(context, "备用渠道也未生效：请检查通知权限/免打扰");
        try {
            if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel("smartcourse_reminder_push_v2") != null) {
                manager.notify(880010, build(context, "smartcourse_reminder_push_v2", str, str2, LIVE_ID, false));
                if (delivered(manager, 880010)) {
                    ReminderPrefs.pushLog(context, "旧渠道已送达");
                    return true;
                }
                ReminderPrefs.pushLog(context, "旧渠道也未生效");
            }
        } catch (Exception e3) {
            ReminderPrefs.pushLog(context, "旧渠道异常：" + e3.getClass().getSimpleName());
        }
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_NEW, "提醒（新）", 4);
                notificationChannel.setLockscreenVisibility(1);
                notificationChannel.setShowBadge(true);
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
                manager.createNotificationChannel(notificationChannel);
            }
            manager.notify(880012, build(context, CHANNEL_NEW, str, str2, LIVE_ID, false));
        } catch (Exception e4) {
            ReminderPrefs.pushLog(context, "新渠道异常：" + e4.getClass().getSimpleName());
        }
        if (delivered(manager, 880012)) {
            ReminderPrefs.pushLog(context, "新渠道已送达");
            return true;
        }
        ReminderPrefs.pushLog(context, "新渠道也未生效");
        return false;
    }

    public static void notify(Context context, String str, String str2, int i) {
        notifySmart(context, str, str2, i);
    }

    private static boolean delivered(NotificationManager notificationManager, int i) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        try {
            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
            if (activeNotifications == null) {
                return true;
            }
            for (StatusBarNotification statusBarNotification : activeNotifications) {
                if (statusBarNotification != null && statusBarNotification.getId() == i) {
                    return true;
                }
            }
            return false;
        } catch (Exception unused) {
            return true;
        }
    }

    private static Notification build(Context context, String str, String str2, String str3, int i, boolean z) {
        Notification.Builder builder;
        int i2 = Build.VERSION.SDK_INT >= 23 ? 67108864 : 0;
        Intent intent = new Intent(context, (Class<?>) AlarmActivity.class);
        intent.addFlags(805306368);
        intent.putExtra(AlarmActivity.EXTRA_TITLE, str2);
        intent.putExtra(AlarmActivity.EXTRA_TEXT, str3);
        intent.putExtra(AlarmActivity.EXTRA_ID, i);
        int i3 = i2 | 134217728;
        PendingIntent activity = PendingIntent.getActivity(context, i, intent, i3);
        Intent intent2 = new Intent(context, (Class<?>) MainActivity.class);
        intent2.addFlags(805306368);
        PendingIntent activity2 = PendingIntent.getActivity(context, i + 3, intent2, i3);
        Intent intent3 = new Intent(context, (Class<?>) ReminderReceiver.class);
        intent3.setAction(ReminderReceiver.ACTION_SNOOZE);
        intent3.putExtra(ReminderReceiver.EXTRA_TITLE, str2);
        intent3.putExtra(ReminderReceiver.EXTRA_TEXT, str3);
        intent3.putExtra(ReminderReceiver.EXTRA_ID, i);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, i + 1, intent3, i3);
        Intent intent4 = new Intent(context, (Class<?>) ReminderReceiver.class);
        intent4.setAction(ReminderReceiver.ACTION_STOP);
        intent4.putExtra(ReminderReceiver.EXTRA_ID, i);
        PendingIntent broadcast2 = PendingIntent.getBroadcast(context, i + 2, intent4, i3);
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(context, str);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(str2).setContentText(str3).setStyle(new Notification.BigTextStyle().bigText(str3)).setCategory("alarm").setVisibility(1).setPriority(2).setOngoing(true).setAutoCancel(false).setShowWhen(true).setWhen(System.currentTimeMillis()).setContentIntent(activity2).addAction(new Notification.Action(android.R.drawable.ic_lock_idle_alarm, "稍后 5 分钟", broadcast)).addAction(new Notification.Action(android.R.drawable.ic_menu_close_clear_cancel, "停止", broadcast2));
        if (z) {
            builder.setFullScreenIntent(activity, true);
        }
        return builder.build();
    }

    public static void cancel(Context context, int i) {
        try {
            NotificationManager manager = manager(context);
            if (manager != null) {
                manager.cancel(i);
                manager.cancel(i + 7);
            }
        } catch (Exception unused) {
        }
    }

    public static void cancelAll(Context context) {
        try {
            NotificationManager manager = manager(context);
            if (manager != null) {
                manager.cancelAll();
            }
        } catch (Exception unused) {
        }
    }
}
