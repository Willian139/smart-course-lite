package com.zuwe.smartcourselite;

import android.content.Context;

/** 后台线程：登录教务系统并拉取课表。 */
public final class JwWorker extends Thread {

    private final JwUi ui;
    private final JwApi api;
    private final String user;
    private final String pass;
    private final String captcha;
    private final String xnm;
    private final String xqm;
    private final String cookie;

    public JwWorker(JwUi ui, JwApi api, String user, String pass, String captcha,
                    String xnm, String xqm) {
        this.ui = ui;
        this.api = api;
        this.user = user;
        this.pass = pass;
        this.captcha = captcha;
        this.xnm = xnm;
        this.xqm = xqm;
        this.cookie = null;
        setName("jw-worker");
        setPriority(MIN_PRIORITY);
    }

    /** 用网页登录拿到的 Cookie 直接取课表（不碰密码和验证码）。 */
    public JwWorker(JwUi ui, JwApi api, String cookie, String xnm, String xqm) {
        this.ui = ui;
        this.api = api;
        this.user = "";
        this.pass = "";
        this.captcha = "";
        this.cookie = cookie;
        this.xnm = xnm;
        this.xqm = xqm;
        setName("jw-worker");
        setPriority(MIN_PRIORITY);
    }

    @Override
    public void run() {
        Context ctx = ui.activity();
        try {
            if (cookie != null) {
                api.setCookieHeader(cookie);
                if (!api.checkSession()) {
                    throw new Exception("网页里还没登录到首页，请登录成功后再点");
                }
                JwLog.w(ctx, "login", "webview-cookie ok " + api.debugInfo());
            } else {
                api.login(user, pass, captcha);
                JwLog.w(ctx, "login", "ok " + api.debugInfo());
            }
            JwApi.Result r = api.fetch(xnm, xqm);
            JwLog.w(ctx, "fetch", "source=" + r.source + " items=" + r.items.size()
                    + " weeks=" + r.weeks.size() + " student=" + r.student);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < r.items.size(); i++) {
                JwApi.KbItem it = r.items.get(i);
                sb.append("周").append(it.weekDay)
                        .append(' ').append(it.start).append('-').append(it.end)
                        .append(' ').append(it.name)
                        .append(' ').append(it.room)
                        .append(" weeks=");
                for (int j = 0; j < it.weeks.length; j++) {
                    sb.append(it.weeks[j]).append(',');
                }
                sb.append('\n');
            }
            JwLog.w(ctx, "items", sb.toString());
            ui.postOk(r);
        } catch (Throwable t) {
            String msg = t.getMessage();
            JwLog.w(ctx, "error", t.toString() + " | " + api.debugInfo());
            JwLog.w(ctx, "body", api.lastBodyHead());
            ui.postErr(friendly(t, msg));
        }
    }

    /** 别把 Java 原始异常直接甩给用户。 */
    private static String friendly(Throwable t, String msg) {
        if (msg == null || msg.length() == 0) {
            return "取课表失败：" + t.getClass().getSimpleName();
        }
        if (msg.indexOf("Illegal embedded") >= 0 || msg.indexOf("For input string") >= 0
                || msg.indexOf("NumberFormat") >= 0) {
            return "登录失败：服务器返回的公钥/数据格式异常，已按明文方式重试仍不行，请稍后再试";
        }
        if (msg.indexOf("Exception") >= 0 && msg.indexOf("：") < 0) {
            return "取课表失败：" + t.getClass().getSimpleName();
        }
        return msg;
    }
}
