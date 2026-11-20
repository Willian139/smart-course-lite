package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class WeekCalendar {
    private static final String KEY_ANCHOR = "week_anchor";
    private static final String KEY_MAP = "week_map";
    private static final String KEY_TS = "week_map_ts";
    private static final Map<String, Integer> MAP = new HashMap();
    private static final String PREFS = "smartcourse_prefs";
    private static final long TTL_MS = 21600000;
    private static LocalDate anchor;
    private static long fetchedAt;
    private static boolean loaded;

    private WeekCalendar() {
    }

    public static synchronized void load(Context context) {
        synchronized (WeekCalendar.class) {
            synchronized (WeekCalendar.class) {
                if (loaded) {
                    return;
                }
                loaded = true;
                SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, 0);
                fetchedAt = sharedPreferences.getLong(KEY_TS, 0L);
                String string = sharedPreferences.getString(KEY_ANCHOR, "");
                if (string != null && !string.isEmpty()) {
                    try {
                        anchor = LocalDate.parse(string);
                    } catch (Exception unused) {
                    }
                }
                fill(sharedPreferences.getString(KEY_MAP, ""));
            }
        }
    }

    private static void fill(String str) {
        MAP.clear();
        if (str == null || str.isEmpty()) {
            return;
        }
        for (String str2 : str.split("\n")) {
            int indexOf = str2.indexOf(61);
            if (indexOf > 0) {
                try {
                    MAP.put(str2.substring(0, indexOf), Integer.valueOf(Integer.parseInt(str2.substring(indexOf + 1).trim())));
                } catch (Exception unused) {
                }
            }
        }
    }

    public static synchronized boolean isStale(Context context) {
        boolean z;
        synchronized (WeekCalendar.class) {
            synchronized (WeekCalendar.class) {
                load(context);
                z = true;
                if (fetchedAt != 0 && !MAP.isEmpty() && System.currentTimeMillis() - fetchedAt <= TTL_MS) {
                    z = false;
                }
            }
            return z;
        }
    }

    public static synchronized LocalDate anchor(Context context) {
        LocalDate localDate;
        synchronized (WeekCalendar.class) {
            synchronized (WeekCalendar.class) {
                load(context);
                localDate = anchor;
            }
            return localDate;
        }
    }

    public static synchronized Integer weekFor(Context context, LocalDate localDate) {
        synchronized (WeekCalendar.class) {
            synchronized (WeekCalendar.class) {
                load(context);
                if (localDate == null) {
                    return null;
                }
                Map<String, Integer> map = MAP;
                Integer num = map.get(localDate.toString());
                if (num != null) {
                    return num;
                }
                if (map.isEmpty()) {
                    return null;
                }
                DayOfWeek dayOfWeek = localDate.getDayOfWeek();
                int i = 0;
                long j = Long.MAX_VALUE;
                LocalDate localDate2 = null;
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    try {
                        LocalDate parse = LocalDate.parse(entry.getKey());
                        if (parse.getDayOfWeek() == dayOfWeek) {
                            long abs = Math.abs(localDate.toEpochDay() - parse.toEpochDay());
                            if (abs < j) {
                                i = entry.getValue().intValue();
                                localDate2 = parse;
                                j = abs;
                            }
                        }
                    } catch (Exception unused) {
                    }
                }
                if (localDate2 == null) {
                    return null;
                }
                return Integer.valueOf((int) (i + ((localDate.toEpochDay() - localDate2.toEpochDay()) / 7)));
            }
        }
    }

    public static synchronized boolean save(Context context, JSONObject jSONObject) {
        synchronized (WeekCalendar.class) {
            synchronized (WeekCalendar.class) {
                if (jSONObject == null) {
                    return false;
                }
                JSONArray optJSONArray = jSONObject.optJSONArray("data");
                if (optJSONArray == null) {
                    return false;
                }
                StringBuilder sb = new StringBuilder();
                LocalDate localDate = null;
                int i = 0;
                for (int i2 = 0; i2 < optJSONArray.length(); i2++) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i2);
                    if (optJSONObject != null) {
                        int optInt = optJSONObject.optInt("week", 0);
                        JSONArray optJSONArray2 = optJSONObject.optJSONArray("teachWeekDetail");
                        if (optInt > 0 && optJSONArray2 != null) {
                            for (int i3 = 0; i3 < optJSONArray2.length(); i3++) {
                                JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i3);
                                String optString = optJSONObject2 == null ? "" : optJSONObject2.optString("date", "");
                                if (optString.length() >= 10) {
                                    String substring = optString.substring(0, 10);
                                    try {
                                        LocalDate parse = LocalDate.parse(substring);
                                        sb.append(substring);
                                        sb.append('=');
                                        sb.append(optInt);
                                        sb.append('\n');
                                        if (localDate == null || parse.isBefore(localDate)) {
                                            localDate = parse;
                                        }
                                        i++;
                                    } catch (Exception unused) {
                                    }
                                }
                            }
                        }
                    }
                }
                if (i == 0) {
                    return false;
                }
                long currentTimeMillis = System.currentTimeMillis();
                context.getSharedPreferences(PREFS, 0).edit().putString(KEY_MAP, sb.toString()).putString(KEY_ANCHOR, localDate.toString()).putLong(KEY_TS, currentTimeMillis).apply();
                fill(sb.toString());
                anchor = localDate;
                fetchedAt = currentTimeMillis;
                return true;
            }
        }
    }
}
