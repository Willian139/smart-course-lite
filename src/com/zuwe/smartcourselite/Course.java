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

    /** 把「南浔校区博学楼博学北106」这类地点压缩成「博学北 106」这种具体教室。 */
    public String roomShort() {
        String s = this.room == null ? "" : this.room.trim();
        if (s.isEmpty()) return "";
        int campus = s.indexOf("校区");
        if (campus >= 0) {
            String rest = s.substring(campus + 2).trim();
            if (!rest.isEmpty()) s = rest;
        }
        int building = s.indexOf('楼');
        if (building > 0 && building < s.length() - 1) {
            String rest = s.substring(building + 1).trim();
            if (!rest.isEmpty()) s = rest;
        }
        s = s.replaceAll("^(\\D+?)(\\d+.*)$", "$1 $2").trim();
        return s;
    }

    public String sections() {
        return this.startSection == 0 ? "节次未提供" : this.startSection + "–" + this.endSection + " 节";
    }

    public String timeRange() {
        return ClassTimeTable.formatRange(this.startSection, this.endSection);
    }
}
