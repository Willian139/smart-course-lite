package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class ReminderPrefs {
    public static final int DEFAULT_LEAD = 10;
    private static final String KEY_COURSE_LEAD = "course_lead";
    private static final String KEY_COURSE_ON = "course_on";
    private static final String KEY_CUSTOM = "custom_list";
    private static final String KEY_DEV_TAPS = "dev_taps";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_ENABLED_DAY = "enabled_day";
    private static final String KEY_EVERY_DAY = "every_day";
    private static final String KEY_FIRED = "fired_";
    private static final String KEY_HIDDEN_MODE = "hidden_mode";
    private static final String KEY_ITEM_LEADS = "item_leads";
    private static final String KEY_SCHEDULE = "schedule_snapshot";
    private static final String KEY_SCHED_AT = "sched_at";
    private static final String KEY_SCHED_COUNT = "sched_count";
    private static final String KEY_SHOWN = "shown_";
    private static final String KEY_TODO_LEAD = "todo_lead";
    private static final String KEY_TODO_ON = "todo_on";
    private static final String KEY_WAKE_AT = "wake_at";
    private static final String KEY_WAKE_TITLE = "wake_title";
    public static final int[] LEADS = {5, 10, 15, 30, 60};
    private static final String PREFS = "smartcourse_reminder";

    private static int clampLead(int i) {
        if (i < 1 || i > 120) {
            return 10;
        }
        return i;
    }

    public static final class Custom {
        public final long at;
        public final String id;
        public final String title;

        public Custom(String str, long j, String str2) {
            this.id = str;
            this.at = j;
            this.title = str2;
        }
    }

    private ReminderPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, 0);
    }

    private static String today() {
        return LocalDate.now().toString();
    }

    public static boolean toggled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setToggled(Context context, boolean z) {
        SharedPreferences.Editor putBoolean = prefs(context).edit().putBoolean(KEY_ENABLED, z);
        if (z) {
            putBoolean.putString(KEY_ENABLED_DAY, today());
        }
        putBoolean.apply();
    }

    public static boolean everyDay(Context context) {
        return prefs(context).getBoolean(KEY_EVERY_DAY, false);
    }

    public static void setEveryDay(Context context, boolean z) {
        prefs(context).edit().putBoolean(KEY_EVERY_DAY, z).apply();
    }

    public static boolean active(Context context) {
        return activeOn(context, LocalDate.now());
    }

    public static boolean activeOn(Context context, LocalDate localDate) {
        if (!toggled(context) || localDate == null) {
            return false;
        }
        if (everyDay(context)) {
            return true;
        }
        return localDate.toString().equals(prefs(context).getString(KEY_ENABLED_DAY, ""));
    }

    public static void setToggledOn(Context context, LocalDate localDate) {
        prefs(context).edit().putBoolean(KEY_ENABLED, true).putString(KEY_ENABLED_DAY, localDate == null ? today() : localDate.toString()).apply();
    }

    public static boolean courseGlobalOn(Context context) {
        return prefs(context).getBoolean(KEY_COURSE_ON, false);
    }

    public static boolean todoGlobalOn(Context context) {
        return prefs(context).getBoolean(KEY_TODO_ON, false);
    }

    private static JSONObject itemLeads(Context context) {
        try {
            return new JSONObject(prefs(context).getString(KEY_ITEM_LEADS, "{}"));
        } catch (Exception unused) {
            return new JSONObject();
        }
    }

    public static int itemLead(Context context, String str) {
        JSONObject itemLeads = itemLeads(context);
        if (itemLeads.has(str)) {
            return clampLead(itemLeads.optInt(str, 10));
        }
        return -1;
    }

    public static void setItemLead(Context context, String str, int i) {
        JSONObject itemLeads = itemLeads(context);
        try {
            itemLeads.put(str, clampLead(i));
        } catch (Exception unused) {
        }
        prefs(context).edit().putString(KEY_ITEM_LEADS, itemLeads.toString()).apply();
    }

    public static void clearItemLead(Context context, String str) {
        JSONObject itemLeads = itemLeads(context);
        itemLeads.remove(str);
        prefs(context).edit().putString(KEY_ITEM_LEADS, itemLeads.toString()).apply();
    }

    public static void clearAll(Context context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).putBoolean(KEY_COURSE_ON, false).putBoolean(KEY_TODO_ON, false).putString(KEY_ENABLED_DAY, "").remove(KEY_ITEM_LEADS).remove(KEY_CUSTOM).remove(KEY_SCHEDULE).remove(KEY_SCHED_COUNT).remove(KEY_SCHED_AT).remove("stop_at").remove("last_notify_at").remove(KEY_WAKE_AT).remove(KEY_WAKE_TITLE).remove("notify_error").remove("sched_error").apply();
    }

    public static boolean hasItemLead(Context context, String str) {
        return itemLeads(context).has(str);
    }

    public static int courseLead(Context context) {
        return clampLead(prefs(context).getInt(KEY_COURSE_LEAD, 10));
    }

    public static int todoLead(Context context) {
        return clampLead(prefs(context).getInt(KEY_TODO_LEAD, 10));
    }

    public static void setCourseLead(Context context, int i) {
        prefs(context).edit().putInt(KEY_COURSE_LEAD, clampLead(i)).apply();
    }

    public static void setTodoLead(Context context, int i) {
        prefs(context).edit().putInt(KEY_TODO_LEAD, clampLead(i)).apply();
    }

    public static void quickCourses(Context context, int i) {
        quickCourses(context, i, LocalDate.now());
    }

    public static void quickCourses(Context context, int i, LocalDate localDate) {
        prefs(context).edit().putBoolean(KEY_ENABLED, true).putBoolean(KEY_COURSE_ON, true).putString(KEY_ENABLED_DAY, localDate == null ? today() : localDate.toString()).putInt(KEY_COURSE_LEAD, clampLead(i)).apply();
    }

    public static void quickTodos(Context context, int i) {
        quickTodos(context, i, LocalDate.now());
    }

    public static void quickTodos(Context context, int i, LocalDate localDate) {
        prefs(context).edit().putBoolean(KEY_ENABLED, true).putBoolean(KEY_TODO_ON, true).putString(KEY_ENABLED_DAY, localDate == null ? today() : localDate.toString()).putInt(KEY_TODO_LEAD, clampLead(i)).apply();
    }

    public static List<Custom> customs(Context context) {
        ArrayList arrayList = new ArrayList();
        try {
            JSONArray jSONArray = new JSONArray(prefs(context).getString(KEY_CUSTOM, "[]"));
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(new Custom(optJSONObject.optString("id", String.valueOf(i)), optJSONObject.optLong("at", 0L), optJSONObject.optString(ReminderReceiver.EXTRA_TITLE, "提醒")));
                }
            }
        } catch (Exception unused) {
        }
        return arrayList;
    }

    public static void addCustom(Context context, long j, String str) {
        List<Custom> customs = customs(context);
        customs.add(new Custom(String.valueOf(System.currentTimeMillis()), j, (str == null || str.trim().isEmpty()) ? "提醒" : str.trim()));
        saveCustoms(context, customs);
    }

    public static void removeCustom(Context context, String str) {
        List<Custom> customs = customs(context);
        ArrayList arrayList = new ArrayList();
        for (Custom custom : customs) {
            if (!custom.id.equals(str)) {
                arrayList.add(custom);
            }
        }
        saveCustoms(context, arrayList);
    }

    private static void saveCustoms(Context context, List<Custom> list) {
        JSONArray jSONArray = new JSONArray();
        for (Custom custom : list) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", custom.id);
                jSONObject.put("at", custom.at);
                jSONObject.put(ReminderReceiver.EXTRA_TITLE, custom.title);
                jSONArray.put(jSONObject);
            } catch (Exception unused) {
            }
        }
        prefs(context).edit().putString(KEY_CUSTOM, jSONArray.toString()).apply();
    }

    public static void saveSchedule(Context context, String str) {
        prefs(context).edit().putString(KEY_SCHEDULE, str).apply();
    }

    public static String schedule(Context context) {
        return prefs(context).getString(KEY_SCHEDULE, "[]");
    }

    public static void setScheduleInfo(Context context, int i, long j) {
        prefs(context).edit().putInt(KEY_SCHED_COUNT, i).putLong(KEY_SCHED_AT, j).apply();
    }

    public static boolean firstFireWithin(Context context, String str) {
        long currentTimeMillis = System.currentTimeMillis();
        String string = prefs(context).getString("fire_" + str, "");
        if (!string.isEmpty()) {
            try {
                if (currentTimeMillis - Long.parseLong(string) < 90000) {
                    return false;
                }
            } catch (Exception unused) {
            }
        }
        prefs(context).edit().putString("fire_" + str, String.valueOf(currentTimeMillis)).apply();
        return true;
    }

    public static void pushLog(Context context, String str) {
        try {
            String format = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            JSONArray jSONArray = new JSONArray(prefs(context).getString("event_log", "[]"));
            JSONArray jSONArray2 = new JSONArray();
            jSONArray2.put(format + "  " + str);
            for (int i = 0; i < jSONArray.length() && i < 23; i++) {
                jSONArray2.put(jSONArray.getString(i));
            }
            prefs(context).edit().putString("event_log", jSONArray2.toString()).apply();
        } catch (Exception unused) {
        }
    }

    public static String logText(Context context) {
        try {
            JSONArray jSONArray = new JSONArray(prefs(context).getString("event_log", "[]"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < jSONArray.length(); i++) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(jSONArray.getString(i));
            }
            return sb.toString();
        } catch (Exception unused) {
            return "";
        }
    }

    public static void setNotifyError(Context context, String str) {
        SharedPreferences.Editor edit = prefs(context).edit();
        if (str == null) {
            str = "";
        }
        edit.putString("notify_error", str).apply();
    }

    public static String notifyError(Context context) {
        return prefs(context).getString("notify_error", "");
    }

    public static void setScheduleError(Context context, String str) {
        SharedPreferences.Editor edit = prefs(context).edit();
        if (str == null) {
            str = "";
        }
        edit.putString("sched_error", str).apply();
    }

    public static String scheduleError(Context context) {
        return prefs(context).getString("sched_error", "");
    }

    public static int scheduleCount(Context context) {
        return prefs(context).getInt(KEY_SCHED_COUNT, -1);
    }

    public static long scheduleNearest(Context context) {
        return prefs(context).getLong(KEY_SCHED_AT, 0L);
    }

    public static long sysAlarmAt(Context context) {
        return prefs(context).getLong("sys_alarm_at", 0L);
    }

    public static void setSysAlarmAt(Context context, long j) {
        prefs(context).edit().putLong("sys_alarm_at", j).apply();
    }

    public static boolean sysClock(Context context) {
        return prefs(context).getBoolean("sys_clock", true);
    }

    public static void setSysClock(Context context, boolean z) {
        prefs(context).edit().putBoolean("sys_clock", z).apply();
    }

    public static String sysNextLogged(Context context) {
        return prefs(context).getString("sys_next_logged", "");
    }

    public static void setSysNextLogged(Context context, String str) {
        SharedPreferences.Editor edit = prefs(context).edit();
        if (str == null) {
            str = "";
        }
        edit.putString("sys_next_logged", str).apply();
    }

    public static boolean calFallback(Context context) {
        return prefs(context).getBoolean("cal_fallback", true);
    }

    public static void setCalFallback(Context context, boolean z) {
        prefs(context).edit().putBoolean("cal_fallback", z).apply();
    }

    public static void markStopped(Context context) {
        prefs(context).edit().putLong("stop_at", System.currentTimeMillis()).apply();
    }

    public static long stoppedAt(Context context) {
        return prefs(context).getLong("stop_at", 0L);
    }

    public static void markNotified(Context context) {
        prefs(context).edit().putLong("last_notify_at", System.currentTimeMillis()).apply();
    }

    public static long notifiedAt(Context context) {
        return prefs(context).getLong("last_notify_at", 0L);
    }

    public static void markWake(Context context, String str) {
        SharedPreferences.Editor putLong = prefs(context).edit().putLong(KEY_WAKE_AT, System.currentTimeMillis());
        if (str == null) {
            str = "";
        }
        putLong.putString(KEY_WAKE_TITLE, str).apply();
    }

    public static long wakeAt(Context context) {
        return prefs(context).getLong(KEY_WAKE_AT, 0L);
    }

    public static String wakeTitle(Context context) {
        return prefs(context).getString(KEY_WAKE_TITLE, "");
    }

    public static boolean hiddenModeUnlocked(Context context) {
        return prefs(context).getBoolean(KEY_HIDDEN_MODE, false);
    }

    public static void setHiddenModeUnlocked(Context context) {
        prefs(context).edit().putBoolean(KEY_HIDDEN_MODE, true).apply();
    }

    public static boolean incrementDevTap(Context context) {
        if (hiddenModeUnlocked(context)) {
            return true;
        }
        int i = prefs(context).getInt(KEY_DEV_TAPS, 0) + 1;
        prefs(context).edit().putInt(KEY_DEV_TAPS, i).apply();
        return i >= 10;
    }

    public static boolean shownOn(Context context, String str, String str2) {
        return str2.equals(prefs(context).getString(KEY_SHOWN + str, ""));
    }

    public static void markShown(Context context, String str, String str2) {
        prefs(context).edit().putString(KEY_SHOWN + str, str2).apply();
    }

    public static boolean customFired(Context context, String str, String str2) {
        return str2.equals(prefs(context).getString(KEY_FIRED + str, ""));
    }

    public static void markCustomFired(Context context, String str, String str2) {
        prefs(context).edit().putString(KEY_FIRED + str, str2).apply();
    }
}
