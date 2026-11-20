package com.zuwe.smartcourselite;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class SystemAlarm {
    private SystemAlarm() {
    }

    public static void armNearest(Context context) {
        long j;
        if (ReminderPrefs.sysClock(context)) {
            long currentTimeMillis = System.currentTimeMillis();
            long sysAlarmAt = ReminderPrefs.sysAlarmAt(context);
            String str = null;
            try {
                JSONArray jSONArray = new JSONArray(ReminderPrefs.schedule(context));
                j = 0;
                for (int i = 0; i < jSONArray.length(); i++) {
                    try {
                        JSONObject optJSONObject = jSONArray.optJSONObject(i);
                        if (optJSONObject != null) {
                            long optLong = optJSONObject.optLong("at", 0L);
                            if (optLong > currentTimeMillis + 30000 && (j == 0 || optLong < j)) {
                                try {
                                    String optString = optJSONObject.optString(ReminderReceiver.EXTRA_TITLE, "提醒");
                                    String optString2 = optJSONObject.optString(ReminderReceiver.EXTRA_TEXT, "");
                                    if (!optString2.isEmpty()) {
                                        optString = optString + " · " + optString2;
                                    }
                                    str = optString;
                                    j = optLong;
                                } catch (Exception unused) {
                                    j = optLong;
                                }
                            }
                        }
                    } catch (Exception unused2) {
                    }
                }
            } catch (Exception unused3) {
                j = 0;
            }
            if (j == 0) {
                ReminderPrefs.pushLog(context, "系统时钟：没有可排的提醒");
                return;
            }
            if (sysAlarmAt <= currentTimeMillis || sysAlarmAt > 60000 + j) {
                if (armAt(context, j, str)) {
                    ReminderPrefs.setSysAlarmAt(context, j);
                } else {
                    ReminderPrefs.setSysClock(context, false);
                }
            }
        }
    }

    public static boolean armAt(Context context, long j, String str) {
        LocalDateTime ofInstant = LocalDateTime.ofInstant(Instant.ofEpochMilli(j), ZoneId.systemDefault());
        String format = ofInstant.format(DateTimeFormatter.ofPattern("M月d日 HH:mm"));
        try {
            context.startActivity(buildSetAlarm(ofInstant.getHour(), ofInstant.getMinute(), str, null));
            ReminderPrefs.pushLog(context, "系统时钟已写入 " + format + " 闹钟");
            return true;
        } catch (Exception e) {
            ReminderPrefs.pushLog(context, "通用写入失败：" + e.getClass().getSimpleName());
            String[] strArr = {"com.android.deskclock", "com.google.android.deskclock", "com.miui.clock", "com.coloros.alarmclock", "com.oplus.alarmclock", "com.huawei.deskclock", "com.hihonor.deskclock", "com.vivo.clock", "com.bbk.clock", "com.sec.android.app.clockpackage", "com.android.alarmclock"};
            for (int i = 0; i < 11; i++) {
                String str2 = strArr[i];
                try {
                    context.startActivity(buildSetAlarm(ofInstant.getHour(), ofInstant.getMinute(), str, str2));
                    ReminderPrefs.pushLog(context, "系统时钟已写入 " + format + "（" + str2 + "）");
                    return true;
                } catch (Exception unused) {
                }
            }
            ReminderPrefs.pushLog(context, "系统时钟：所有写入方式都失败");
            return false;
        }
    }

    private static Intent buildSetAlarm(int i, int i2, String str, String str2) {
        Intent intent = new Intent("android.intent.action.SET_ALARM");
        intent.putExtra("android.intent.extra.alarm.HOUR", i);
        intent.putExtra("android.intent.extra.alarm.MINUTES", i2);
        intent.putExtra("android.intent.extra.alarm.MESSAGE", str);
        intent.putExtra("android.intent.extra.alarm.SKIP_UI", true);
        intent.addFlags(268435456);
        if (str2 != null) {
            intent.setPackage(str2);
        }
        return intent;
    }

    public static void logSystemNext(Context context) {
        String str;
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            AlarmManager.AlarmClockInfo nextAlarmClock = alarmManager == null ? null : alarmManager.getNextAlarmClock();
            if (nextAlarmClock == null) {
                str = "系统闹钟表：空";
            } else {
                str = "系统闹钟表：" + LocalDateTime.ofInstant(Instant.ofEpochMilli(nextAlarmClock.getTriggerTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M月d日 HH:mm"));
            }
        } catch (Exception e) {
            str = "系统闹钟表读取失败：" + e.getClass().getSimpleName();
        }
        if (str.equals(ReminderPrefs.sysNextLogged(context))) {
            return;
        }
        ReminderPrefs.setSysNextLogged(context, str);
        ReminderPrefs.pushLog(context, str);
    }
}
