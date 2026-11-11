package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 教学周历：以学校 /skl/teachweek 接口返回的真实数据为准。
 * 返回形如 {"week":9,"data":[{"week":1,"teachWeekDetail":[{"date":"2026-09-14",...}]}, ...]}，
 * week 为服务端认定的当前周次，data 为每周的日期明细。
 */
public final class WeekCalendar {
    private static final String PREFS = "smartcourse_prefs";
    private static final String KEY_MAP = "week_map";      // 每行 “2026-09-14=1”
    private static final String KEY_ANCHOR = "week_anchor"; // 第 1 周第一天
    private static final String KEY_TS = "week_map_ts";
    private static final long TTL_MS = 6 * 60 * 60 * 1000L;

    private static final Map<String, Integer> MAP = new HashMap<String, Integer>();
    private static LocalDate anchor;
    private static long fetchedAt;
    private static boolean loaded;

    private WeekCalendar() {
    }

    public static synchronized void load(Context context) {
        if (loaded) {
            return;
        }
        loaded = true;
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        fetchedAt = p.getLong(KEY_TS, 0L);
        String a = p.getString(KEY_ANCHOR, "");
        if (a != null && !a.isEmpty()) {
            try {
                anchor = LocalDate.parse(a);
            } catch (Exception ignored) {
            }
        }
        fill(p.getString(KEY_MAP, ""));
    }

    private static void fill(String raw) {
        MAP.clear();
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (String line : raw.split("\n")) {
            int i = line.indexOf('=');
            if (i <= 0) {
                continue;
            }
            try {
                MAP.put(line.substring(0, i), Integer.valueOf(Integer.parseInt(line.substring(i + 1).trim())));
            } catch (Exception ignored) {
            }
        }
    }

    public static synchronized boolean isStale(Context context) {
        load(context);
        return fetchedAt == 0L || MAP.isEmpty() || System.currentTimeMillis() - fetchedAt > TTL_MS;
    }

    public static synchronized LocalDate anchor(Context context) {
        load(context);
        return anchor;
    }

    /** 真实周次：先查日期表，超出表范围时按“同星期”的已知周次外推。 */
    public static synchronized Integer weekFor(Context context, LocalDate date) {
        load(context);
        if (date == null) {
            return null;
        }
        Integer exact = MAP.get(date.toString());
        if (exact != null) {
            return exact;
        }
        if (MAP.isEmpty()) {
            return null;
        }
        DayOfWeek want = date.getDayOfWeek();
        LocalDate best = null;
        int bestWeek = 0;
        long bestDiff = Long.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : MAP.entrySet()) {
            LocalDate d;
            try {
                d = LocalDate.parse(entry.getKey());
            } catch (Exception e) {
                continue;
            }
            if (d.getDayOfWeek() != want) {
                continue;
            }
            long diff = Math.abs(date.toEpochDay() - d.toEpochDay());
            if (diff < bestDiff) {
                bestDiff = diff;
                best = d;
                bestWeek = entry.getValue().intValue();
            }
        }
        if (best == null) {
            return null;
        }
        long weeks = (date.toEpochDay() - best.toEpochDay()) / 7;
        return Integer.valueOf((int) (bestWeek + weeks));
    }

    /** 解析接口返回并落盘；成功返回 true。 */
    public static synchronized boolean save(Context context, JSONObject resp) {
        if (resp == null) {
            return false;
        }
        JSONArray weeks = resp.optJSONArray("data");
        if (weeks == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        LocalDate first = null;
        int count = 0;
        for (int i = 0; i < weeks.length(); i++) {
            JSONObject w = weeks.optJSONObject(i);
            if (w == null) {
                continue;
            }
            int no = w.optInt("week", 0);
            JSONArray days = w.optJSONArray("teachWeekDetail");
            if (no <= 0 || days == null) {
                continue;
            }
            for (int j = 0; j < days.length(); j++) {
                JSONObject day = days.optJSONObject(j);
                String ds = day == null ? "" : day.optString("date", "");
                if (ds.length() < 10) {
                    continue;
                }
                ds = ds.substring(0, 10);
                LocalDate ld;
                try {
                    ld = LocalDate.parse(ds);
                } catch (Exception e) {
                    continue;
                }
                sb.append(ds).append('=').append(no).append('\n');
                if (first == null || ld.isBefore(first)) {
                    first = ld;
                }
                count++;
            }
        }
        if (count == 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_MAP, sb.toString())
                .putString(KEY_ANCHOR, first.toString())
                .putLong(KEY_TS, now)
                .apply();
        fill(sb.toString());
        anchor = first;
        fetchedAt = now;
        return true;
    }
}
