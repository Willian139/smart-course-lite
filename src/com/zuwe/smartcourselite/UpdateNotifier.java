package com.zuwe.smartcourselite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import java.io.File;

/** 更新下载用的通知栏封装（前台服务进度 + 完成提示 + 点击安装）。只用系统 Notification。 */
final class UpdateNotifier {

    static final String CHANNEL = "smartcourse_update";
    static final int ID_PROGRESS = 4001;
    static final int ID_DONE = 4002;
    private static final int ICON = android.R.drawable.stat_sys_download;

    private UpdateNotifier() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (manager.getNotificationChannel(CHANNEL) != null) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL, "应用更新", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("后台下载与安装提示");
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);
    }

    @SuppressWarnings("deprecation")
    static Notification progress(Context context, int percent, String version) {
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) builder.setChannelId(CHANNEL);
        builder.setSmallIcon(ICON);
        builder.setLargeIcon(largeIcon(context));
        builder.setContentTitle("正在下载 v" + version);
        builder.setContentText(percent < 0 ? "准备中…" : percent + "%");
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        if (percent > 0.0) builder.setProgress(100, percent, false);
        else builder.setProgress(0, 0, true);
        if (Build.VERSION.SDK_INT >= 16) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        return builder.build();
    }

    @SuppressWarnings("deprecation")
    static Notification done(Context context, String version, String notes) {
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) builder.setChannelId(CHANNEL);
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        builder.setLargeIcon(largeIcon(context));
        builder.setContentTitle("v" + version + " 已下载完成");
        builder.setContentText("点击安装更新");
        if (Build.VERSION.SDK_INT >= 16) {
            String extra = notes == null ? "" : notes.trim();
            if (extra.length() > 160) extra = extra.substring(0, 160) + "…";
            builder.setStyle(new Notification.BigTextStyle()
                    .bigText("v" + version + " 已下载完成，点击安装更新。" + (extra.isEmpty() ? "" : "\n" + extra)));
        }
        builder.setAutoCancel(true);
        builder.setShowWhen(true);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder.setContentIntent(PendingIntent.getActivity(context, 1001, intent, pendingFlags()));
        return builder.build();
    }

    @SuppressWarnings("deprecation")
    static Notification failed(Context context, String version, String reason) {
        ensureChannel(context);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= 26) builder.setChannelId(CHANNEL);
        builder.setSmallIcon(android.R.drawable.stat_notify_error);
        builder.setContentTitle("v" + version + " 下载失败");
        builder.setContentText("点击打开应用重试（" + reason + "）");
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder.setContentIntent(PendingIntent.getActivity(context, 1003, intent, pendingFlags()));
        return builder.build();
    }

    static PendingIntent installIntent(Context context, File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return PendingIntent.getActivity(context, 1002, intent, pendingFlags());
    }

    private static int pendingFlags() {
        if (Build.VERSION.SDK_INT >= 23) {
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    private static Bitmap largeIcon(Context context) {
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(context.getPackageName());
            if (icon instanceof BitmapDrawable) return ((BitmapDrawable) icon).getBitmap();
            float density = context.getResources().getDisplayMetrics().density;
            int size = Math.max(1, (int) (48 * density));
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            icon.setBounds(0, 0, size, size);
            icon.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    static void show(Context context, Notification notification, int id) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        try {
            manager.notify(id, notification);
        } catch (Exception ignored) {
        }
    }

    static void cancel(Context context, int id) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        try {
            manager.cancel(id);
        } catch (Exception ignored) {
        }
    }
}
