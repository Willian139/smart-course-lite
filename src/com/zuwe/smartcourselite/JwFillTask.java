package com.zuwe.smartcourselite;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/** 后台把教务课表合并进智慧思政缓存，避免整学期写缓存把主线程卡住。 */
public final class JwFillTask extends Thread {

    private final JwUi ui;
    private final MainActivity main;
    private final JwApi.Result result;

    public JwFillTask(JwUi ui, MainActivity main, JwApi.Result result) {
        this.ui = ui;
        this.main = main;
        this.result = result;
        setName("JwFillTask");
    }

    @Override
    public void run() {
        try {
            try {
                JSONObject t = new JSONObject();
                t.put("courseName", "自检课");
                t.put("classRoom", "A101");
                t.put("startSection", 3);
                Course tc = new Course(t);
                String k0 = result == null || result.items.isEmpty() ? "none"
                        : (result.items.get(0).name + "/" + result.items.get(0).room + "/"
                        + result.items.get(0).start);
                JwLog.w(main, "fill", "selftest name=" + tc.name + " room=" + tc.room
                        + " start=" + tc.startSection + " kb0=" + k0);
            } catch (Throwable t) {
                JwLog.w(main, "fill", "selftest error " + t);
            }
            LocalDate today = LocalDate.now();
            List<Course> todayList = null;
            int total = 0;
            for (int w = 0; w < result.weeks.size(); w++) {
                JwApi.WeekRange wk = result.weeks.get(w);
                if (wk != null && wk.start != null) total += 7;
            }
            int done = 0;
            int days = 0;
            int skipped = 0;
            int freshly = 0;
            int empty = 0;
            int added = 0;
            int filled = 0;
            ui.postProgress(0, total);
            for (int w = 0; w < result.weeks.size(); w++) {
                JwApi.WeekRange wk = result.weeks.get(w);
                if (wk == null || wk.start == null) continue;
                for (int d = 1; d <= 7; d++) {
                    done++;
                    LocalDate date = wk.start.plusDays(d - 1);
                    List<Course> old = JwUi.loadCacheCourses(main, date);
                    int size = JwUi.cacheSize(main, date);
                    if (old.isEmpty() && size > 0) {
                        // 有内容却解析不出来：宁可不动，避免覆盖用户原有数据
                        JwLog.w(main, "fill", "skip-unparsed " + date + " size=" + size);
                        skipped++;
                    } else if (old.isEmpty()) {
                        // 没有缓存或缓存是空的：按教务课表补上这一天的课
                        List<Course> now = coursesFor(w, d, date);
                        if (now.isEmpty()) {
                            empty++;
                        } else {
                            main.saveCache(date, now);
                            freshly++;
                            if (date.equals(today)) todayList = now;
                        }
                    } else {
                        List<Course> now = coursesFor(w, d, date);
                        JwUi.MergeStat st = JwUi.mergeCourses(old, now);
                        main.saveCache(date, st.list);
                        days++;
                        added += st.added;
                        filled += st.filled;
                        if (date.equals(today)) todayList = st.list;
                        if (isThisWeek(date, today)) {
                            JwLog.w(main, "fill", "day=" + date + " old=" + brief(old)
                                    + " now=" + brief(now) + " fill=" + st.filled);
                        }
                    }
                    if (done % 7 == 0 || done == total) ui.postProgress(done, total);
                }
            }
            String msg = "已整理整学期 " + (days + freshly) + " 天"
                    + (days > 0 ? "（其中 " + days + " 天有智慧思政数据" : "")
                    + (filled > 0 ? "，用教务补全 " + filled + " 处空缺教室" : "")
                    + (freshly > 0 ? "；" + freshly + " 天原本没有数据，已按教务课表补上" : "")
                    + (empty > 0 ? "；" + empty + " 天两边的课都没有" : "")
                    + (skipped > 0 ? "；" + skipped + " 天缓存读不出来，未改动" : "");
            JwLog.w(main, "fill", "ok days=" + days + " fresh=" + freshly + " empty=" + empty
                    + " skip=" + skipped + " add=" + added + " fill=" + filled);
            ui.postFill(msg, todayList, true);
        } catch (Throwable t) {
            JwLog.w(main, "fill", "error " + t);
            ui.postFill("填充失败：" + t, null, false);
        }
    }

    private static boolean isThisWeek(LocalDate d, LocalDate today) {
        LocalDate mon = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return !d.isBefore(mon) && !d.isAfter(mon.plusDays(6));
    }

    private static String brief(List<Course> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("; ");
            Course c = list.get(i);
            sb.append(c.name).append("|").append(c.room).append("|").append(c.startSection);
        }
        return sb.append("]").toString();
    }

    private List<Course> coursesFor(int w, int weekDay, LocalDate date) {
        List<Course> out = new ArrayList<Course>();
        if (w < 0 || w >= result.weeks.size()) return out;
        JwApi.WeekRange wk = result.weeks.get(w);
        for (int i = 0; i < result.items.size(); i++) {
            JwApi.KbItem it = result.items.get(i);
            if (it.weekDay != weekDay) continue;
            if (!it.inWeek(wk.week)) continue;
            try {
                JSONObject o = new JSONObject();
                o.put("courseName", it.name);
                o.put("teacherName", it.teacher);
                o.put("classRoom", it.room);
                o.put("date", date.toString());
                o.put("startSection", it.start);
                o.put("endSection", it.end);
                o.put("week", wk.week);
                o.put("weekDay", weekDay);
                out.add(new Course(o));
            } catch (Throwable ignored) {
            }
        }
        return out;
    }
}
