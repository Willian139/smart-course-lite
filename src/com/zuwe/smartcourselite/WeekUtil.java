package com.zuwe.smartcourselite;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

/** 周次计算：中国高校秋季学期 9 月开学、春季学期 2 月下旬开学。 */
public final class WeekUtil {
    private WeekUtil() {}

    public static LocalDate monday(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** 未校准时按校历：秋季 9 月 13 日当周（含）之后的周一、春季 2 月 23 日之后的周一作为第 1 周。 */
    public static LocalDate defaultAnchor(LocalDate now) {
        int m = now.getMonthValue();
        LocalDate start;
        if (m >= 8) {
            start = LocalDate.of(now.getYear(), Month.SEPTEMBER, 13);
        } else if (m == 1) {
            start = LocalDate.of(now.getYear() - 1, Month.SEPTEMBER, 13);
        } else {
            start = LocalDate.of(now.getYear(), Month.FEBRUARY, 23);
        }
        return start.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /** 返回第几周；第 1 周之前返回 0 或负数。 */
    public static int weekNumber(LocalDate anchor, LocalDate date) {
        long days = monday(date).toEpochDay() - monday(anchor).toEpochDay();
        return (int) Math.floor(days / 7.0) + 1;
    }
}
