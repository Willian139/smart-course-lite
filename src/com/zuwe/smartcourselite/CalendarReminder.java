package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/* loaded from: classes.dex */
public final class CalendarReminder {
    private static final int CODE = 9002;
    private static final String KEY_IDS = "event_ids";
    private static final String KEY_INFO = "last_sync_info";
    private static final String KEY_SIG = "sync_sig";
    private static final String KEY_STATE = "last_state";
    private static final String KEY_SYNC_TIME = "sync_at";
    private static final String KEY_TEST_IDS = "test_ids";
    private static final int MAX_EVENTS = 120;
    private static final String PREFS = "smartcourse_calendar";

    public static int requestCode() {
        return CODE;
    }

    private CalendarReminder() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, 0);
    }

    public static boolean hasPermission(Context context) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                return true;
            }
            if (context.checkSelfPermission("android.permission.WRITE_CALENDAR") == 0) {
                return context.checkSelfPermission("android.permission.READ_CALENDAR") == 0;
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    public static void requestPermission(Activity activity) {
        try {
            activity.requestPermissions(new String[]{"android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR"}, CODE);
        } catch (Exception e) {
            ReminderPrefs.pushLog(activity, "申请日历权限失败：" + e.getClass().getSimpleName());
        }
    }

    public static void sync(Context context, List<String[]> list) {
        String str;
        long j;
        Context context2 = context;
        if (!hasPermission(context)) {
            logOnce(context2, "日历权限未开启，系统日历兜底未启用");
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        List arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            String[] strArr = list.get(i);
            try {
                if (Long.parseLong(strArr[0]) > currentTimeMillis - 300000) {
                    arrayList.add(strArr);
                }
            } catch (Exception unused) {
            }
        }
        Collections.sort(arrayList, new Comparator<String[]>() { // from class: com.zuwe.smartcourselite.CalendarReminder.1
            @Override // java.util.Comparator
            public int compare(String[] strArr2, String[] strArr3) {
                return Long.valueOf(strArr2[0]).compareTo(Long.valueOf(strArr3[0]));
            }
        });
        if (arrayList.size() > MAX_EVENTS) {
            arrayList = arrayList.subList(0, MAX_EVENTS);
        }
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (true) {
            str = "x:";
            if (i2 >= arrayList.size()) {
                break;
            }
            String str2 = ((String[]) arrayList.get(i2))[1];
            if (str2.startsWith("x:")) {
                i3++;
            } else if (str2.startsWith("c:") || str2.startsWith("cal:")) {
                i4++;
            } else if (str2.startsWith("t:")) {
                i5++;
            }
            i2++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i6 = 0; i6 < arrayList.size(); i6++) {
            sb.append(((String[]) arrayList.get(i6))[0]);
            sb.append('|');
            sb.append(((String[]) arrayList.get(i6))[1]);
            sb.append(';');
        }
        String sb2 = sb.toString();
        String str3 = "";
        if (arrayList.isEmpty()) {
            clearEvents(context);
            prefs(context).edit().putString(KEY_SIG, "").apply();
            logOnce(context2, "日历：当前没有需要写入的提醒");
            return;
        }
        try {
            clearEvents(context);
            clearTestEvents(context);
            long ensureCalendar = ensureCalendar(context);
            if (ensureCalendar < 0) {
                logOnce(context2, "日历：没有可写入的日历，也没能新建");
                return;
            }
            ContentResolver contentResolver = context.getContentResolver();
            int bestReminderMethod = bestReminderMethod(context2, ensureCalendar);
            StringBuilder sb3 = new StringBuilder();
            int i7 = i3;
            int i8 = i4;
            int i9 = i5;
            int i10 = 0;
            int i11 = 0;
            while (i10 < arrayList.size()) {
                try {
                    String[] strArr2 = (String[]) arrayList.get(i10);
                    long parseLong = Long.parseLong(strArr2[0]);
                    String str4 = str;
                    ContentValues contentValues = new ContentValues();
                    List list2 = arrayList;
                    contentValues.put("calendar_id", Long.valueOf(ensureCalendar));
                    String str5 = strArr2[2];
                    if (str5 == null) {
                        str5 = "提醒";
                    }
                    if (str5.startsWith("⏰")) {
                        j = currentTimeMillis;
                    } else {
                        StringBuilder sb4 = new StringBuilder();
                        j = currentTimeMillis;
                        sb4.append("⏰ ");
                        sb4.append(str5);
                        str5 = sb4.toString();
                    }
                    contentValues.put(ReminderReceiver.EXTRA_TITLE, str5);
                    StringBuilder sb5 = new StringBuilder();
                    String str6 = strArr2[3];
                    if (str6 == null) {
                        str6 = "";
                    }
                    sb5.append(str6);
                    sb5.append("\n—— 来自智慧思政课表，点开 App 看今天安排");
                    contentValues.put("description", sb5.toString());
                    contentValues.put("eventLocation", "");
                    contentValues.put("eventTimezone", TimeZone.getDefault().getID());
                    contentValues.put("dtstart", Long.valueOf(parseLong));
                    contentValues.put("dtend", Long.valueOf(parseLong + 60000));
                    contentValues.put("hasAlarm", (Integer) 1);
                    Uri insert = contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);
                    if (insert != null) {
                        long parseId = ContentUris.parseId(insert);
                        ContentValues contentValues2 = new ContentValues();
                        contentValues2.put("event_id", Long.valueOf(parseId));
                        contentValues2.put("minutes", (Integer) 0);
                        contentValues2.put("method", Integer.valueOf(bestReminderMethod));
                        try {
                            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, contentValues2);
                        } catch (Exception unused2) {
                        }
                        if (sb3.length() > 0) {
                            sb3.append(',');
                        }
                        sb3.append(parseId);
                        i11++;
                    }
                    i10++;
                    str = str4;
                    arrayList = list2;
                    currentTimeMillis = j;
                } catch (Exception e) {
                    e = e;
                    context2 = context;
                    logOnce(context2, "日历写入失败：" + e.getClass().getSimpleName() + " " + e.getMessage());
                    return;
                }
            }
            List list3 = arrayList;
            long j2 = currentTimeMillis;
            String str7 = str;
            prefs(context).edit().putString(KEY_IDS, sb3.toString()).putString(KEY_SIG, sb2).putLong(KEY_SYNC_TIME, System.currentTimeMillis()).apply();
            String readBack = readBack(context, ensureCalendar, j2);
            StringBuilder sb6 = new StringBuilder();
            int i12 = 0;
            while (i12 < list3.size() && sb6.length() < 40) {
                List list4 = list3;
                String str8 = str7;
                if (((String[]) list4.get(i12))[1].startsWith(str8)) {
                    String format = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(((String[]) list4.get(i12))[0])), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M日HH:mm"));
                    if (sb6.length() > 0) {
                        sb6.append("、");
                    }
                    sb6.append(format);
                }
                i12++;
                list3 = list4;
                str7 = str8;
            }
            List list5 = list3;
            StringBuilder sb7 = new StringBuilder();
            for (int i13 = 0; i13 < list5.size() && sb7.length() < 34; i13++) {
                long parseLong2 = Long.parseLong(((String[]) list5.get(i13))[0]);
                if (sb7.length() > 0) {
                    sb7.append(' ');
                }
                sb7.append(LocalDateTime.ofInstant(Instant.ofEpochMilli(parseLong2), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d日HH:mm")));
            }
            StringBuilder sb8 = new StringBuilder();
            sb8.append("日历同步：写入 ");
            sb8.append(i11);
            sb8.append(" 条（课程 ");
            sb8.append(i8);
            sb8.append(" / 待办 ");
            sb8.append(i9);
            sb8.append(" / 自定义 ");
            sb8.append(i7);
            if (sb6.length() != 0) {
                str3 = "：" + ((Object) sb6);
            }
            sb8.append(str3);
            sb8.append(" · ");
            sb8.append(bestReminderMethod == 4 ? "闹钟提醒" : "通知提醒");
            sb8.append("）");
            String sb9 = sb8.toString();
            prefs(context).edit().putString(KEY_INFO, sb9 + " @ " + new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(new Date())).apply();
            ReminderPrefs.pushLog(context, sb9);
            ReminderPrefs.pushLog(context, "日历回读：" + readBack);
            if (i11 < list5.size()) {
                ReminderPrefs.pushLog(context, "日历：有 " + (list5.size() - i11) + " 条没能写进去");
            }
        } catch (Exception e2) {
            // 日历同步失败不影响主流程
            ReminderPrefs.pushLog(context, "日历同步失败：" + e2.getClass().getSimpleName());
        }
    }

    private static String readBack(Context context, long j, long j2) {
        try {
            Cursor query = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, new String[]{"dtstart", ReminderReceiver.EXTRA_TITLE}, "calendar_id=" + j + " AND dtstart>=" + (j2 - 3600000) + " AND dtstart<=" + (j2 + 691200000), null, "dtstart ASC");
            if (query == null) {
                return "查询失败";
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (query.moveToNext() && i < 40) {
                i++;
                if (sb.length() < 60) {
                    long j3 = query.getLong(0);
                    String string = query.getString(1);
                    if (sb.length() > 0) {
                        sb.append(" / ");
                    }
                    sb.append(LocalDateTime.ofInstant(Instant.ofEpochMilli(j3), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d日HH:mm")));
                    if (string != null && !string.isEmpty()) {
                        sb.append(' ');
                        if (string.length() > 8) {
                            string = string.substring(0, 8);
                        }
                        sb.append(string);
                    }
                }
            }
            query.close();
            if (i == 0) {
                return "0 条（日历里没有我们的事件）";
            }
            return i + " 条：" + ((Object) sb);
        } catch (Exception e) {
            return "回读异常：" + e.getClass().getSimpleName();
        }
    }

    private static int countSaved(Context context) {
        String string = prefs(context).getString(KEY_IDS, "");
        if (string.trim().isEmpty()) {
            return 0;
        }
        return string.split(",").length;
    }

    public static void clearEvents(Context context) {
        String string = prefs(context).getString(KEY_IDS, "");
        prefs(context).edit().putString(KEY_IDS, "").apply();
        if (string.trim().isEmpty()) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        for (String str : string.split(",")) {
            try {
                contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(str.trim())), null, null);
            } catch (Exception unused) {
            }
        }
    }

    private static int bestReminderMethod(Context context, long j) {
        try {
            Cursor query = context.getContentResolver().query(ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, j), new String[]{"allowedReminders"}, null, null, null);
            if (query != null && query.moveToFirst()) {
                String string = query.getString(0);
                query.close();
                if (string != null && string.contains(String.valueOf(4))) {
                    return 4;
                }
            } else if (query != null) {
                query.close();
            }
        } catch (Exception unused) {
        }
        return 1;
    }

    /* JADX WARN: Removed duplicated region for block: B:37:0x009b  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x009f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static long ensureCalendar(android.content.Context r22) {
        /*
            Method dump skipped, instructions count: 307
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.CalendarReminder.ensureCalendar(android.content.Context):long");
    }

    private static void logOnce(Context context, String str) {
        try {
            if (prefs(context).getString(KEY_STATE, "").equals(str)) {
                return;
            }
            prefs(context).edit().putString(KEY_STATE, str).apply();
            ReminderPrefs.pushLog(context, str);
        } catch (Exception unused) {
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:26:0x00d1, code lost:
    
        if (r16.moveToFirst() != false) goto L28;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean writeTest(android.content.Context r17, long r18) {
        /*
            Method dump skipped, instructions count: 359
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.CalendarReminder.writeTest(android.content.Context, long):boolean");
    }

    private static void clearTestEvents(Context context) {
        String string = prefs(context).getString(KEY_TEST_IDS, "");
        prefs(context).edit().putString(KEY_TEST_IDS, "").apply();
        if (string.trim().isEmpty()) {
            return;
        }
        for (String str : string.split(",")) {
            try {
                context.getContentResolver().delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(str.trim())), null, null);
            } catch (Exception unused) {
            }
        }
    }

    private static void logCalendarOnce(Context context, long j) {
        try {
            Cursor query = context.getContentResolver().query(ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, j), new String[]{"calendar_displayName", "account_name", "account_type", "calendar_access_level"}, null, null, null);
            if (query != null && query.moveToFirst()) {
                String str = "日历：使用「" + query.getString(0) + "」(" + query.getString(1) + "/" + query.getString(2) + " · 级别 " + query.getInt(3) + ")";
                query.close();
                if (!prefs(context).getString("cal_choice", "").equals(str)) {
                    prefs(context).edit().putString("cal_choice", str).apply();
                    ReminderPrefs.pushLog(context, str);
                }
            } else if (query != null) {
                query.close();
            }
        } catch (Exception unused) {
        }
    }

    public static void reset(Context context) {
        try {
            clearEvents(context);
            clearTestEvents(context);
            prefs(context).edit().remove(KEY_SIG).remove(KEY_SYNC_TIME).remove(KEY_IDS).remove(KEY_TEST_IDS).remove(KEY_INFO).apply();
            ReminderPrefs.pushLog(context, "日历已重置，准备重建");
        } catch (Exception e) {
            ReminderPrefs.pushLog(context, "日历重置失败：" + e.getClass().getSimpleName());
        }
    }

    public static String summary(Context context) {
        if (!hasPermission(context)) {
            return "日历权限未开启";
        }
        String string = prefs(context).getString(KEY_INFO, "");
        if (!string.isEmpty()) {
            return string;
        }
        return "已写入日历 " + countSaved(context) + " 条";
    }

    public static void setMainContextState(Context context, String str) {
        logOnce(context, str);
    }
}
