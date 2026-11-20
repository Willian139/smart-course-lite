package com.zuwe.smartcourselite;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

/* loaded from: classes.dex */
public final class WeekUtil {
    private WeekUtil() {
    }

    public static LocalDate monday(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate defaultAnchor(LocalDate localDate) {
        LocalDate of;
        int monthValue = localDate.getMonthValue();
        if (monthValue >= 8) {
            of = LocalDate.of(localDate.getYear(), Month.SEPTEMBER, 13);
        } else if (monthValue == 1) {
            of = LocalDate.of(localDate.getYear() - 1, Month.SEPTEMBER, 13);
        } else {
            of = LocalDate.of(localDate.getYear(), Month.FEBRUARY, 23);
        }
        return of.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    public static int weekNumber(LocalDate localDate, LocalDate localDate2) {
        return ((int) Math.floor((monday(localDate2).toEpochDay() - monday(localDate).toEpochDay()) / 7.0d)) + 1;
    }
}
