package com.zuwe.smartcourselite;

import org.json.JSONObject;

/* loaded from: classes.dex */
public final class Course {
    public final String date;
    public final int endSection;
    public final String name;
    public final String room;
    public final int startSection;
    public final String teacher;
    public final int week;
    public final int weekDay;

    public Course(JSONObject jSONObject) {
        this.name = jSONObject.optString("courseName", "未命名课程");
        this.teacher = jSONObject.optString("teacherName", "");
        this.room = jSONObject.optString("classRoom", "");
        this.date = jSONObject.optString("date", "");
        this.startSection = jSONObject.optInt("startSection", 0);
        this.endSection = jSONObject.optInt("endSection", 0);
        this.week = jSONObject.optInt("week", 0);
        this.weekDay = jSONObject.optInt("weekDay", 0);
    }

    public String roomShort() {
        String str = this.room;
        String trim = str == null ? "" : str.trim();
        if (trim.isEmpty()) {
            return "";
        }
        int indexOf = trim.indexOf("校区");
        if (indexOf >= 0) {
            String trim2 = trim.substring(indexOf + 2).trim();
            if (!trim2.isEmpty()) {
                trim = trim2;
            }
        }
        int indexOf2 = trim.indexOf(27004);
        if (indexOf2 > 0 && indexOf2 < trim.length() - 1) {
            String trim3 = trim.substring(indexOf2 + 1).trim();
            if (!trim3.isEmpty()) {
                trim = trim3;
            }
        }
        return trim.replaceAll("^(\\D+?)(\\d+.*)$", "$1 $2").trim();
    }

    public String sections() {
        if (this.startSection == 0) {
            return "节次未提供";
        }
        return this.startSection + "–" + this.endSection + " 节";
    }

    public String timeRange() {
        return ClassTimeTable.formatRange(this.startSection, this.endSection);
    }
}
