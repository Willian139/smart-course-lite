package cn.zuwe.hygiene;

import android.webkit.CookieManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * 卫生检查 API 客户端。
 *
 * 抓包结论（4.zip / 6.zip）：ihome 的接口用请求头 `token: <32 位十六进制>` 鉴权，
 * 与课表 skl 的 Authorization JWT 是两套系统；同一个 token 可跨天复用，所以登录一次即可长期使用。
 */
public final class HygieneApi {
    public static final String BASE = "https://ihome.zuwe.edu.cn";
    public static final String USER_INFO = "/appsys/sys/user/info";
    public static final String HISTORY_PAGE = "/appsi/mobile/api/history/getHistoryRoomPageBySn";
    public static final String HISTORY_DETAIL = "/appsi/mobile/api/history/getHistoryRoomDetailById/";
    public static final String LOGIN_ROUTE_H5 = BASE + "/wxweb/#/zjweuOAuthLogin";

    private final HygieneSession session;
    private String overrideToken = "";

    public HygieneApi(HygieneSession session) { this.session = session; }

    /** 用一个尚未保存的 token 试调接口（登录校验用）。 */
    public HygieneApi withToken(String token) { overrideToken = token == null ? "" : token.trim(); return this; }

    public JSONObject userInfo() throws IOException { return request("GET", USER_INFO, null, null); }
    public JSONObject historyPage(int page, int limit, String planId, String personSn) throws IOException {
        String query = "?page=" + enc(String.valueOf(page)) + "&limit=" + enc(String.valueOf(limit))
                + "&planId=" + enc(nvl(planId)) + "&personSn=" + enc(nvl(personSn));
        // 4.zip 记录：POST，筛选/分页字段放在 Query，body 为空。
        return request("POST", HISTORY_PAGE + query, "", "application/x-www-form-urlencoded; charset=UTF-8");
    }
    public JSONObject historyDetail(String id) throws IOException {
        return request("GET", HISTORY_DETAIL + pathEnc(id), null, null);
    }

    private JSONObject request(String method, String path, String body, String contentType) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(BASE + path).openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(15000); c.setReadTimeout(25000);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        c.setRequestProperty("Referer", BASE + "/wxweb/");
        String token = overrideToken.isEmpty() ? session.token() : overrideToken;
        if (!token.isEmpty()) c.setRequestProperty("token", token);
        String cookie = CookieManager.getInstance().getCookie(BASE + "/");
        if (cookie != null && !cookie.isEmpty()) c.setRequestProperty("Cookie", cookie);
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            c.setDoOutput(true); c.setRequestProperty("Content-Type", contentType == null ? "application/json;charset=UTF-8" : contentType);
            byte[] data = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = c.getOutputStream()) { out.write(data); }
        }
        try {
            int status = c.getResponseCode();
            String setCookie = c.getHeaderField("Set-Cookie");
            if (setCookie != null) { CookieManager.getInstance().setCookie(BASE + "/", setCookie); CookieManager.getInstance().flush(); }
            InputStream in = status >= 200 && status < 400 ? c.getInputStream() : c.getErrorStream();
            String raw = read(in);
            if (status < 200 || status >= 300) throw new IOException("HTTP " + status + ": " + raw);
            try { return new JSONObject(raw); } catch (Exception e) { throw new IOException("响应不是 JSON：" + raw, e); }
        } finally { c.disconnect(); }
    }
    /** 从 user/info 响应里取账号名（personSn 就是 username）。 */
    public static String usernameOf(JSONObject userInfo) {
        JSONObject data = unwrapData(userInfo);
        JSONObject user = data.optJSONObject("user");
        if (user == null) user = data.optJSONObject("userInfo");
        if (user == null) user = data;
        String[] keys = {"username", "personSn", "userName", "userSn", "personNo"};
        for (String k : keys) { String v = user.optString(k, "").trim(); if (!v.isEmpty()) return v; }
        return "";
    }
    public static JSONObject unwrapData(JSONObject root) {
        JSONObject data = root.optJSONObject("data"); return data == null ? root : data;
    }
    public static JSONObject findPage(JSONObject root) {
        JSONObject data = root.optJSONObject("data");
        JSONObject p = root.optJSONObject("page"); if (p != null) return p;
        if (data != null) { p = data.optJSONObject("page"); if (p != null) return p; }
        return null;
    }
    public static JSONArray findList(JSONObject page) {
        if (page == null) return new JSONArray();
        JSONArray a = page.optJSONArray("list"); if (a != null) return a;
        a = page.optJSONArray("records"); return a == null ? new JSONArray() : a;
    }
    public static void ensureSuccess(JSONObject json) throws IOException {
        int code = json.optInt("code", Integer.MIN_VALUE);
        if (code != Integer.MIN_VALUE && code != 0 && code != 200 && code != 20000)
            throw new IOException(json.optString("msg", json.optString("message", "业务接口失败，code=" + code)));
        if (json.optBoolean("success", true) == false)
            throw new IOException(json.optString("msg", json.optString("message", "业务接口返回失败")));
    }
    private static String read(InputStream in) throws IOException {
        if (in == null) return ""; ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b = new byte[8192]; int n;
        while ((n = in.read(b)) != -1) out.write(b, 0, n); return out.toString("UTF-8");
    }
    private static String enc(String s) throws IOException { return URLEncoder.encode(s, "UTF-8"); }
    private static String pathEnc(String s) throws IOException { return enc(s).replace("+", "%20"); }
    private static String nvl(String s) { return s == null ? "" : s; }
}
