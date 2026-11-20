package com.zuwe.smartcourselite;

/* loaded from: classes.dex */
public final class ClassTimeTable {
    private static final String[][] PERIODS = {new String[]{"08:10", "08:50"}, new String[]{"08:55", "09:35"}, new String[]{"09:50", "10:30"}, new String[]{"10:35", "11:15"}, new String[]{"11:20", "12:00"}, new String[]{"13:30", "14:10"}, new String[]{"14:15", "14:55"}, new String[]{"15:05", "15:45"}, new String[]{"15:50", "16:30"}, new String[]{"18:00", "18:40"}, new String[]{"18:45", "19:25"}, new String[]{"19:30", "20:10"}};

    private ClassTimeTable() {
    }

    public static String formatRange(int i, int i2) {
        if (i >= 1 && i2 >= i) {
            String[][] strArr = PERIODS;
            if (i2 <= strArr.length) {
                return strArr[i - 1][0] + "–" + strArr[i2 - 1][1];
            }
        }
        return "上课时间待补充";
    }

    public static int startMinutes(int i) {
        if (i >= 1) {
            String[][] strArr = PERIODS;
            if (i <= strArr.length) {
                return minutesOf(strArr[i - 1][0]);
            }
        }
        return 0;
    }

    public static int endMinutes(int i) {
        if (i < 1) {
            return 0;
        }
        String[][] strArr = PERIODS;
        if (i > strArr.length) {
            return 0;
        }
        return minutesOf(strArr[i - 1][1]);
    }

    public static String clockOf(int i) {
        if (i < 1) {
            return "--:--";
        }
        String[][] strArr = PERIODS;
        return i > strArr.length ? "--:--" : strArr[i - 1][0];
    }

    public static String clockEndOf(int i) {
        if (i < 1) {
            return "--:--";
        }
        String[][] strArr = PERIODS;
        return i > strArr.length ? "--:--" : strArr[i - 1][1];
    }

    public static String clockOfMinutes(int i) {
        int i2 = ((i % 1440) + 1440) % 1440;
        String valueOf = String.valueOf(i2 / 60);
        String valueOf2 = String.valueOf(i2 % 60);
        StringBuilder sb = new StringBuilder();
        if (valueOf.length() < 2) {
            valueOf = "0" + valueOf;
        }
        sb.append(valueOf);
        sb.append(":");
        if (valueOf2.length() < 2) {
            valueOf2 = "0" + valueOf2;
        }
        sb.append(valueOf2);
        return sb.toString();
    }

    private static int minutesOf(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf <= 0) {
            return 0;
        }
        return (Integer.parseInt(str.substring(0, indexOf)) * 60) + Integer.parseInt(str.substring(indexOf + 1));
    }

    public static int periodCount() {
        return PERIODS.length;
    }
}
