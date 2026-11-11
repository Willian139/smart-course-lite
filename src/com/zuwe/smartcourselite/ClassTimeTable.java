package com.zuwe.smartcourselite;

/* loaded from: classes.dex */
public final class ClassTimeTable {
    private static final String[][] PERIODS = {new String[]{"08:10", "08:50"}, new String[]{"08:55", "09:35"}, new String[]{"09:50", "10:30"}, new String[]{"10:35", "11:15"}, new String[]{"11:20", "12:00"}, new String[]{"13:30", "14:10"}, new String[]{"14:15", "14:55"}, new String[]{"15:05", "15:45"}, new String[]{"15:50", "16:30"}, new String[]{"18:00", "18:40"}, new String[]{"18:45", "19:25"}, new String[]{"19:30", "20:10"}};

    private ClassTimeTable() {
    }

    public static String formatRange(int i, int i2) {
        if (i >= 1 && i2 >= i && i2 <= PERIODS.length) {
            return PERIODS[i - 1][0] + "–" + PERIODS[i2 - 1][1];
        }
        return "上课时间待补充";
    }

    public static int startMinutes(int section) {
        if (section < 1 || section > PERIODS.length) return 0;
        return minutesOf(PERIODS[section - 1][0]);
    }

    public static int endMinutes(int section) {
        if (section < 1 || section > PERIODS.length) return 0;
        return minutesOf(PERIODS[section - 1][1]);
    }

    public static String clockOf(int section) {
        if (section < 1 || section > PERIODS.length) return "--:--";
        return PERIODS[section - 1][0];
    }

    public static String clockEndOf(int section) {
        if (section < 1 || section > PERIODS.length) return "--:--";
        return PERIODS[section - 1][1];
    }

    public static String clockOfMinutes(int minutes) {
        int m = ((minutes % 1440) + 1440) % 1440;
        String h = String.valueOf(m / 60);
        String mi = String.valueOf(m % 60);
        return (h.length() < 2 ? "0" + h : h) + ":" + (mi.length() < 2 ? "0" + mi : mi);
    }

    private static int minutesOf(String hhmm) {
        int i = hhmm.indexOf(':');
        if (i <= 0) return 0;
        return Integer.parseInt(hhmm.substring(0, i)) * 60 + Integer.parseInt(hhmm.substring(i + 1));
    }

    public static int periodCount() {
        return PERIODS.length;
    }
}
