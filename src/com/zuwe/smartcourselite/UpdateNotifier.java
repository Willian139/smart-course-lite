package com.zuwe.smartcourselite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import java.io.File;

/* loaded from: classes.dex */
final class UpdateNotifier {
    static final String CHANNEL = "smartcourse_update";
    private static final int ICON = 17301633;
    static final int ID_DONE = 4002;
    static final int ID_PROGRESS = 4001;

    private UpdateNotifier() {
    }

    static void ensureChannel(Context context) {
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT < 26 || (notificationManager = (NotificationManager) context.getSystemService("notification")) == null || notificationManager.getNotificationChannel(CHANNEL) != null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL, "应用更新", 2);
        notificationChannel.setDescription("后台下载与安装提示");
        notificationChannel.setShowBadge(false);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    static Notification progress(Context context, int i, String str) {
        String str2;
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(CHANNEL);
        }
        builder.setSmallIcon(17301633);
        builder.setLargeIcon(largeIcon(context));
        builder.setContentTitle("正在下载 v" + str);
        if (i < 0) {
            str2 = "准备中…";
        } else {
            str2 = i + "%";
        }
        builder.setContentText(str2);
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        if (i > 0.0d) {
            builder.setProgress(100, i, false);
        } else {
            builder.setProgress(0, 0, true);
        }
        builder.setPriority(-1);
        return builder.build();
    }

    static Notification done(Context context, String str, String str2) {
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(CHANNEL);
        }
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        builder.setLargeIcon(largeIcon(context));
        builder.setContentTitle("v" + str + " 已下载完成");
        builder.setContentText("点击安装更新");
        String str3 = "";
        String trim = str2 == null ? "" : str2.trim();
        if (trim.length() > 160) {
            trim = trim.substring(0, 160) + "…";
        }
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        StringBuilder sb = new StringBuilder("v");
        sb.append(str);
        sb.append(" 已下载完成，点击安装更新。");
        if (!trim.isEmpty()) {
            str3 = "\n" + trim;
        }
        sb.append(str3);
        builder.setStyle(bigTextStyle.bigText(sb.toString()));
        builder.setAutoCancel(true);
        builder.setShowWhen(true);
        Intent intent = new Intent(context, (Class<?>) MainActivity.class);
        intent.setFlags(805306368);
        builder.setContentIntent(PendingIntent.getActivity(context, 1001, intent, pendingFlags()));
        return builder.build();
    }

    static Notification failed(Context context, String str, String str2) {
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(CHANNEL);
        }
        builder.setSmallIcon(android.R.drawable.stat_notify_error);
        builder.setContentTitle("v" + str + " 下载失败");
        builder.setContentText("点击打开应用重试（" + str2 + "）");
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, (Class<?>) MainActivity.class);
        intent.setFlags(805306368);
        builder.setContentIntent(PendingIntent.getActivity(context, 1003, intent, pendingFlags()));
        return builder.build();
    }

    static PendingIntent installIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri exposeForInstall = UpdateManager.exposeForInstall(context, file);
        if (exposeForInstall == null) {
            exposeForInstall = Uri.fromFile(file);
        }
        intent.setDataAndType(exposeForInstall, "application/vnd.android.package-archive");
        intent.setFlags(268435457);
        return PendingIntent.getActivity(context, 1002, intent, pendingFlags());
    }

    private static int pendingFlags() {
        return Build.VERSION.SDK_INT >= 23 ? 201326592 : 134217728;
    }

    private static Bitmap largeIcon(Context context) {
        try {
            Drawable applicationIcon = context.getPackageManager().getApplicationIcon(context.getPackageName());
            if (applicationIcon instanceof BitmapDrawable) {
                return ((BitmapDrawable) applicationIcon).getBitmap();
            }
            int max = Math.max(1, (int) (context.getResources().getDisplayMetrics().density * 48.0f));
            Bitmap createBitmap = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            applicationIcon.setBounds(0, 0, max, max);
            applicationIcon.draw(canvas);
            return createBitmap;
        } catch (Exception unused) {
            return null;
        }
    }

    static void show(Context context, Notification notification, int i) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        try {
            notificationManager.notify(i, notification);
        } catch (Exception unused) {
        }
    }

    static void cancel(Context context, int i) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        try {
            notificationManager.cancel(i);
        } catch (Exception unused) {
        }
    }
}
