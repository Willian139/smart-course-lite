package com.zuwe.smartcourselite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class ReminderScheduler {
    private static long lastRunAt;
    private static String lastSignature;
    private static int mergerCount;
    private static final Set<Integer> scheduled = new HashSet();

    private ReminderScheduler() {
    }

    private static void cancelCode(AlarmManager alarmManager, Context context, int i) {
        PendingIntent buildIntent = buildIntent(context, i, null, null);
        if (buildIntent != null) {
            alarmManager.cancel(buildIntent);
        }
        PendingIntent buildIntent2 = buildIntent(context, i ^ 127, null, null);
        if (buildIntent2 != null) {
            alarmManager.cancel(buildIntent2);
        }
    }

    private static void cancelSnapshot(Context context, String str) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null || str == null || str.trim().isEmpty()) {
            return;
        }
        try {
            JSONArray jSONArray = new JSONArray(str);
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    String optString = optJSONObject.optString("key", "");
                    if (!optString.isEmpty()) {
                        int hashCode = optString.hashCode();
                        cancelCode(alarmManager, context, hashCode);
                        scheduled.remove(Integer.valueOf(hashCode));
                    }
                }
            }
        } catch (Exception unused) {
        }
    }

    public static long systemNext(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            AlarmManager.AlarmClockInfo nextAlarmClock = alarmManager == null ? null : alarmManager.getNextAlarmClock();
            if (nextAlarmClock == null) {
                return 0L;
            }
            return nextAlarmClock.getTriggerTime();
        } catch (Exception unused) {
            return 0L;
        }
    }

    public static void cancelAll(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        for (Integer num : scheduled) {
            cancelCode(alarmManager, context, num.intValue());
            cancelCode(alarmManager, context, num.intValue() ^ 23130);
        }
        scheduled.clear();
    }

    /* JADX WARN: Code restructure failed: missing block: B:82:0x015d, code lost:
    
        if (r2 >= r9) goto L63;
     */
    /* JADX WARN: Removed duplicated region for block: B:122:0x0252  */
    /* JADX WARN: Removed duplicated region for block: B:133:0x0275  */
    /* JADX WARN: Removed duplicated region for block: B:168:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:173:0x0272 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void schedule(android.content.Context r24, java.util.List<java.lang.String[]> r25) {
        /*
            Method dump skipped, instructions count: 738
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.ReminderScheduler.schedule(android.content.Context, java.util.List):void");
    }

    static void forgetSignature() {
        lastSignature = "";
    }

    private static void store(Context context, List<String[]> list) {
        JSONArray jSONArray = new JSONArray();
        for (String[] strArr : list) {
            if (strArr != null && strArr.length >= 4) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("at", Long.parseLong(strArr[0]));
                    jSONObject.put("key", strArr[1]);
                    jSONObject.put(ReminderReceiver.EXTRA_TITLE, strArr[2]);
                    jSONObject.put(ReminderReceiver.EXTRA_TEXT, strArr[3]);
                    jSONArray.put(jSONObject);
                } catch (Exception unused) {
                }
            }
        }
        ReminderPrefs.saveSchedule(context, jSONArray.toString());
    }

    public static void reschedule(Context context) {
        ArrayList arrayList = new ArrayList();
        try {
            JSONArray jSONArray = new JSONArray(ReminderPrefs.schedule(context));
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(new String[]{String.valueOf(optJSONObject.optLong("at", 0L)), optJSONObject.optString("key", "k" + i), optJSONObject.optString(ReminderReceiver.EXTRA_TITLE, "提醒"), optJSONObject.optString(ReminderReceiver.EXTRA_TEXT, "")});
                }
            }
        } catch (Exception unused) {
        }
        if (arrayList.isEmpty()) {
            return;
        }
        schedule(context, arrayList);
    }

    public static void scheduleTest(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis() + 5000;
        PendingIntent buildIntent = buildIntent(context, 990001, "测试闹钟 " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), "如果你听到铃声/看到界面，后台闹钟链路是通的");
        if (buildIntent == null) {
            return;
        }
        try {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(currentTimeMillis, buildIntent), buildIntent);
        } catch (Exception unused) {
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x0044, code lost:
    
        r1.setAlarmClock(new android.app.AlarmManager.AlarmClockInfo(r2, r11), r11);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void scheduleSnooze(android.content.Context r9, int r10, java.lang.String r11, java.lang.String r12) {
        /*
            java.lang.String r0 = "稍后提醒："
            java.lang.String r1 = "alarm"
            java.lang.Object r1 = r9.getSystemService(r1)
            android.app.AlarmManager r1 = (android.app.AlarmManager) r1
            if (r1 != 0) goto Ld
            return
        Ld:
            long r2 = java.lang.System.currentTimeMillis()
            r4 = 1
            int r5 = java.lang.Math.max(r4, r10)
            long r5 = (long) r5
            r7 = 60000(0xea60, double:2.9644E-319)
            long r5 = r5 * r7
            long r2 = r2 + r5
            r5 = 0
            if (r11 != 0) goto L22
            r6 = 0
            goto L26
        L22:
            int r6 = r11.hashCode()
        L26:
            r7 = 65535(0xffff, float:9.1834E-41)
            r6 = r6 & r7
            r7 = 5898240(0x5a0000, float:8.265195E-39)
            r6 = r6 | r7
            android.app.PendingIntent r11 = buildIntent(r9, r6, r11, r12)
            if (r11 != 0) goto L34
            return
        L34:
            int r12 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L6f
            r6 = 31
            if (r12 < r6) goto L42
            boolean r12 = r1.canScheduleExactAlarms()     // Catch: java.lang.Exception -> L6f
            if (r12 == 0) goto L41
            goto L42
        L41:
            r4 = 0
        L42:
            if (r4 == 0) goto L4d
            android.app.AlarmManager$AlarmClockInfo r12 = new android.app.AlarmManager$AlarmClockInfo     // Catch: java.lang.Exception -> L6f
            r12.<init>(r2, r11)     // Catch: java.lang.Exception -> L6f
            r1.setAlarmClock(r12, r11)     // Catch: java.lang.Exception -> L6f
            goto L5a
        L4d:
            int r12 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L6f
            r4 = 23
            if (r12 < r4) goto L57
            r1.setExactAndAllowWhileIdle(r5, r2, r11)     // Catch: java.lang.Exception -> L6f
            goto L5a
        L57:
            r1.set(r5, r2, r11)     // Catch: java.lang.Exception -> L6f
        L5a:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L6f
            r11.<init>(r0)     // Catch: java.lang.Exception -> L6f
            r11.append(r10)     // Catch: java.lang.Exception -> L6f
            java.lang.String r10 = " 分钟后"
            r11.append(r10)     // Catch: java.lang.Exception -> L6f
            java.lang.String r10 = r11.toString()     // Catch: java.lang.Exception -> L6f
            com.zuwe.smartcourselite.ReminderPrefs.pushLog(r9, r10)     // Catch: java.lang.Exception -> L6f
            goto L89
        L6f:
            r10 = move-exception
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            java.lang.String r12 = "稍后提醒排程失败："
            r11.<init>(r12)
            java.lang.Class r10 = r10.getClass()
            java.lang.String r10 = r10.getSimpleName()
            r11.append(r10)
            java.lang.String r10 = r11.toString()
            com.zuwe.smartcourselite.ReminderPrefs.pushLog(r9, r10)
        L89:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.ReminderScheduler.scheduleSnooze(android.content.Context, int, java.lang.String, java.lang.String):void");
    }

    static PendingIntent buildIntent(Context context, int i, String str, String str2, long j) {
        Intent intent = new Intent("com.zuwe.smartcourselite.REMIND");
        intent.setClass(context, ReminderReceiver.class);
        intent.setPackage(context.getPackageName());
        if (str != null) {
            intent.putExtra(ReminderReceiver.EXTRA_TITLE, str);
            intent.putExtra(ReminderReceiver.EXTRA_TEXT, str2);
        }
        intent.putExtra("fire_at", j);
        return PendingIntent.getBroadcast(context, i, intent, (Build.VERSION.SDK_INT >= 23 ? 67108864 : 0) | 134217728);
    }

    private static PendingIntent buildIntent(Context context, int i, String str, String str2) {
        Intent intent = new Intent("com.zuwe.smartcourselite.REMIND");
        intent.setClass(context, ReminderReceiver.class);
        intent.setPackage(context.getPackageName());
        if (str != null) {
            intent.putExtra(ReminderReceiver.EXTRA_TITLE, str);
            intent.putExtra(ReminderReceiver.EXTRA_TEXT, str2);
        }
        return PendingIntent.getBroadcast(context, i, intent, (Build.VERSION.SDK_INT >= 23 ? 67108864 : 0) | 134217728);
    }
}
