package com.zuwe.smartcourselite;

import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 智慧思政自己刷新写缓存时调用：名称一致就不覆盖，只在它没有教室/老师时补上。
 */
public final class JwPreserve {

    private JwPreserve() {
    }

    public static void saveAndKeep(MainActivity m, LocalDate date, List<Course> fresh) {
        if (m == null || date == null) return;
        if (fresh == null) {
            m.writeCache(date, fresh);
            return;
        }
        try {
            List<Course> old = JwUi.loadCacheCourses(m, date);
            if (old == null || old.isEmpty()) {
                m.writeCache(date, fresh);
                return;
            }
            List<Course> out = new ArrayList<Course>(fresh.size());
            for (int i = 0; i < fresh.size(); i++) {
                Course f = fresh.get(i);
                if (f == null) continue;
                String room = f.room == null ? "" : f.room.trim();
                String teacher = f.teacher == null ? "" : f.teacher.trim();
                if (isEmpty(room)) {
                    // 智慧思政这次没给教室（多半本来就没有），保住从教务补上的
                    Course hit = find(old, f.name);
                    if (hit != null) {
                        if (!isEmpty(hit.room)) room = hit.room.trim();
                        if (isEmpty(teacher) && hit.teacher != null) {
                            teacher = hit.teacher.trim();
                        }
                    }
                }
                out.add(rebuild(f, room, teacher));
            }
            m.writeCache(date, out);
        } catch (Throwable t) {
            m.writeCache(date, fresh);
        }
    }

    private static Course find(List<Course> list, String name) {
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            if (c != null && similar(name, c.name)) return c;
        }
        return null;
    }

    private static boolean similar(String a, String b) {
        String x = clean(a);
        String y = clean(b);
        if (x.length() == 0 || y.length() == 0) return false;
        if (x.equals(y)) return true;
        return x.startsWith(y) || y.startsWith(x);
    }

    private static String clean(String s) {
        if (s == null) return "";
        String r = s.replace("（加课）", "").replace("（改课）", "");
        r = r.replace("(", "").replace(")", "").replace(" ", "");
        r = r.replace("（", "").replace("）", "");
        return r.trim();
    }

    private static boolean isEmpty(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.length() == 0) return true;
        return t.indexOf("未排") >= 0 || t.indexOf("详见") >= 0 || t.equals("-");
    }

    private static Course rebuild(Course f, String room, String teacher) {
        String r0 = f.room == null ? "" : f.room.trim();
        String t0 = f.teacher == null ? "" : f.teacher.trim();
        if (room.equals(r0) && teacher.equals(t0)) return f;
        try {
            JSONObject o = new JSONObject();
            o.put("courseName", f.name);
            o.put("teacherName", teacher);
            o.put("classRoom", room);
            o.put("date", f.date);
            o.put("startSection", f.startSection);
            o.put("endSection", f.endSection);
            o.put("week", f.week);
            o.put("weekDay", f.weekDay);
            return new Course(o);
        } catch (Throwable t) {
            return f;
        }
    }
}
