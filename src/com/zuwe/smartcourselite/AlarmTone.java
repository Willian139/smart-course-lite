package com.zuwe.smartcourselite;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.notification.StatusBarNotification;

/* loaded from: classes.dex */
public final class AlarmTone {
    private static Context appCtx;
    private static Runnable autoStop;
    private static Uri lastUri;
    private static MediaPlayer player;
    private static Vibrator vibrator;
    private static Runnable watchdog;
    private static final long[] PATTERN = {0, 800, 400, 800, 400, 800, 1200};
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private AlarmTone() {
    }

    public static void start(Context context) {
        start(context, 180000L);
    }

    public static String describe() {
        Uri uri = lastUri;
        String uri2 = uri == null ? "没有可用铃声" : uri.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(player == null ? "播放器未建立" : "播放器就绪");
        sb.append(" · ");
        sb.append(uri2);
        return sb.toString();
    }

    public static synchronized void start(Context context, long j) {
        synchronized (AlarmTone.class) {
            stop();
            Context applicationContext = context.getApplicationContext();
            try {
                Uri defaultUri = RingtoneManager.getDefaultUri(4);
                if (defaultUri == null) {
                    defaultUri = RingtoneManager.getDefaultUri(1);
                }
                if (defaultUri == null) {
                    defaultUri = RingtoneManager.getDefaultUri(2);
                }
                lastUri = defaultUri;
                if (defaultUri != null) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(applicationContext, defaultUri);
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(4).setContentType(4).build());
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    player = mediaPlayer;
                    try {
                        AudioManager audioManager = (AudioManager) applicationContext.getSystemService("audio");
                        ReminderPrefs.pushLog(applicationContext, "开始响铃：" + defaultUri + " · 闹钟音量 " + (audioManager == null ? -1 : audioManager.getStreamVolume(4)));
                    } catch (Exception unused) {
                    }
                }
            } catch (Exception e) {
                ReminderPrefs.pushLog(applicationContext, "铃声播放失败：" + e.getClass().getSimpleName());
            }
            try {
                Vibrator vibrator2 = (Vibrator) applicationContext.getSystemService("vibrator");
                vibrator = vibrator2;
                if (vibrator2 != null && vibrator2.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createWaveform(PATTERN, 0));
                    } else {
                        vibrator.vibrate(PATTERN, 0);
                    }
                }
            } catch (Exception unused2) {
            }
            appCtx = applicationContext;
            startWatchdog();
            Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.AlarmTone.1
                @Override // java.lang.Runnable
                public void run() {
                    AlarmTone.stop();
                }
            };
            autoStop = runnable;
            if (j > 0) {
                HANDLER.postDelayed(runnable, j);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean liveNotificationVisible() {
        NotificationManager notificationManager;
        try {
            if (appCtx == null || Build.VERSION.SDK_INT < 23 || (notificationManager = (NotificationManager) appCtx.getSystemService("notification")) == null) {
                return true;
            }
            for (StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
                if (statusBarNotification != null && statusBarNotification.getId() == 880001) {
                    return true;
                }
            }
            return false;
        } catch (Exception unused) {
        }
        return true;
    }

    private static void startWatchdog() {
        try {
            Runnable runnable = watchdog;
            if (runnable != null) {
                HANDLER.removeCallbacks(runnable);
                watchdog = null;
            }
            if (liveNotificationVisible()) {
                Runnable runnable2 = new Runnable() { // from class: com.zuwe.smartcourselite.AlarmTone.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (!AlarmTone.liveNotificationVisible()) {
                            ReminderPrefs.pushLog(AlarmTone.appCtx, "通知已不在通知栏，停止响铃");
                            AlarmTone.stop();
                        } else {
                            AlarmTone.HANDLER.postDelayed(this, 2000L);
                        }
                    }
                };
                watchdog = runnable2;
                HANDLER.postDelayed(runnable2, 2000L);
            }
        } catch (Exception unused) {
        }
    }

    public static synchronized void stop() {
        synchronized (AlarmTone.class) {
            try {
                HANDLER.removeCallbacksAndMessages(null);
            } catch (Exception unused) {
            }
            MediaPlayer mediaPlayer = player;
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        player.stop();
                    }
                } catch (Exception unused2) {
                }
                try {
                    player.release();
                } catch (Exception unused3) {
                }
                player = null;
            }
            Vibrator vibrator2 = vibrator;
            if (vibrator2 != null) {
                try {
                    vibrator2.cancel();
                } catch (Exception unused4) {
                }
                vibrator = null;
            }
            autoStop = null;
        }
    }

    public static synchronized boolean ringing() {
        boolean z = false;
        synchronized (AlarmTone.class) {
            z = player != null || vibrator != null;
        }
        return z;
    }
}
