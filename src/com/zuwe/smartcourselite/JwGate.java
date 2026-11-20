package com.zuwe.smartcourselite;

import java.time.LocalDate;

/** 主界面查看课表的开关：没有官方授权时，只要本地有缓存就允许查看。 */
public final class JwGate {

    private JwGate() {
    }

    public static boolean ok(MainActivity m, LocalDate date) {
        if (m == null || date == null) return false;
        try {
            if (m.ready()) return true;
        } catch (Throwable ignored) {
        }
        try {
            String s = m.getSharedPreferences("smartcourse_prefs", 0)
                    .getString("cache_" + date.toString(), null);
            return s != null && s.trim().length() > 2;
        } catch (Throwable t) {
            return false;
        }
    }
}
