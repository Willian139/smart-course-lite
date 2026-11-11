package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 校园接口客户端。
 *
 * 关键改动：
 *  - 连接/读取超时收紧（5s / 10s），瞬断自动重试一次，避免界面长时间「卡住」；
 *  - 记住上次成功的鉴权方式（Authorization / Cookie），下次直接命中，
 *    不再每次刷新都先打一轮注定失败的请求。
 */
public final class SchoolApi {
    private static final String BASE = "https://skl.zuwe.edu.cn";
    private static final String PREFS = "smartcourse_prefs";
    private static final String KEY_AUTH_MODE = "auth_mode";
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 8000;
    private static final int MAX_ATTEMPTS = 2;

    // 0 = Authorization + Cookie, 1 = 仅 Authorization, 2 = 仅 Cookie
    private static final int MODE_AUTH_COOKIE = 0;
    private static final int MODE_AUTH = 1;
    private static final int MODE_COOKIE = 2;

    private final String authorizationJwt;
    private final String sklToken;
    private final SharedPreferences prefs;
    private volatile int workingAuthMode = MODE_AUTH;

    public SchoolApi(Context context, String authorizationJwt, String sklToken) {
        this.authorizationJwt = authorizationJwt;
        this.sklToken = sklToken;
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.workingAuthMode = prefs.getInt(KEY_AUTH_MODE, MODE_AUTH);
        if (workingAuthMode < 0 || workingAuthMode > 2) {
            workingAuthMode = MODE_AUTH;
        }
    }

    private void rememberMode(int mode) {
        this.workingAuthMode = mode;
        prefs.edit().putInt(KEY_AUTH_MODE, mode).apply();
    }

    private JSONObject get(String path) throws Exception {
        int[] order;
        if (workingAuthMode == MODE_AUTH_COOKIE) {
            order = new int[]{MODE_AUTH_COOKIE, MODE_AUTH, MODE_COOKIE};
        } else if (workingAuthMode == MODE_COOKIE) {
            order = new int[]{MODE_COOKIE, MODE_AUTH_COOKIE, MODE_AUTH};
        } else {
            order = new int[]{MODE_AUTH, MODE_AUTH_COOKIE, MODE_COOKIE};
        }
        ApiException lastAuthError = null;
        for (int mode : order) {
            boolean useAuth = mode != MODE_COOKIE;
            boolean useCookie = mode != MODE_AUTH;
            if (useCookie && (this.sklToken == null || this.sklToken.isEmpty())) {
                continue;
            }
            try {
                JSONObject result = getOnce(path, useAuth, useCookie);
                if (mode != workingAuthMode) {
                    rememberMode(mode);
                }
                return result;
            } catch (ApiException e) {
                if (!e.isAuthFailure()) {
                    throw e;
                }
                lastAuthError = e;
            }
        }
        if (lastAuthError != null) {
            throw lastAuthError;
        }
        throw new ApiException("认证失效：本机没有可用凭证", 401);
    }

    private JSONObject getOnce(String path, boolean useAuth, boolean useCookie) throws Exception {
        IOException lastIo = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                return request(path, useAuth, useCookie);
            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                lastIo = e;
                if (attempt + 1 < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(450);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        }
        throw lastIo == null ? new IOException("网络请求失败") : lastIo;
    }

    private JSONObject request(String path, boolean useAuth, boolean useCookie) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE + path).openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Referer", BASE + "/");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) SmartCourseLite/2.6");
            if (useAuth) {
                connection.setRequestProperty("Authorization", "Bearer " + this.authorizationJwt);
            }
            if (useCookie && this.sklToken != null && !this.sklToken.isEmpty()) {
                connection.setRequestProperty("Cookie", cookieHeader(this.sklToken));
            }
            int responseCode = connection.getResponseCode();
            InputStream stream = (responseCode < 200 || responseCode >= 400)
                    ? connection.getErrorStream() : connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            if (stream != null) {
                byte[] chunk = new byte[4096];
                int read;
                while ((read = stream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
                stream.close();
            }
            String body = buffer.toString(StandardCharsets.UTF_8.name());
            if (responseCode == 401 || responseCode == 403) {
                throw new ApiException("认证失效或无权限（HTTP " + responseCode + "）", responseCode);
            }
            if (responseCode >= 300 && responseCode < 400) {
                // 被重定向到登录页，等同于登录态失效
                throw new ApiException("登录状态已失效，请重新授权", 401);
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new ApiException("服务器 HTTP 错误：" + responseCode, responseCode);
            }
            JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (Exception e) {
                throw new ApiException("服务器返回内容异常，请稍后重试", responseCode);
            }
            if (json.has("code")) {
                int code = json.optInt("code", -1);
                if (code == 401 || code == 403) {
                    throw new ApiException(json.optString("message", "认证失效"), code);
                }
                if (code != 200 && code != 0) {
                    throw new ApiException(json.optString("message", "接口业务错误：" + code), responseCode);
                }
            }
            return json;
        } finally {
            connection.disconnect();
        }
    }

    private static String cookieHeader(String token) throws Exception {
        return "sklToken=" + URLEncoder.encode("Bearer " + token, "UTF-8").replace("+", "%20") + "; version=2.0.6";
    }

    public String probeHomeIcon(boolean useAuth, boolean useCookie) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(BASE + "/skl/home/icon").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(6000);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Referer", BASE + "/");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) SmartCourseLite/2.6");
            if (useAuth) {
                connection.setRequestProperty("Authorization", "Bearer " + this.authorizationJwt);
            }
            if (useCookie && this.sklToken != null && !this.sklToken.isEmpty()) {
                connection.setRequestProperty("Cookie", cookieHeader(this.sklToken));
            }
            int responseCode = connection.getResponseCode();
            InputStream stream = (responseCode < 200 || responseCode >= 400)
                    ? connection.getErrorStream() : connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            if (stream != null) {
                byte[] chunk = new byte[2048];
                int read;
                while ((read = stream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
                stream.close();
            }
            String extra = "";
            try {
                JSONObject json = new JSONObject(buffer.toString(StandardCharsets.UTF_8.name()));
                if (json.has("code")) {
                    extra = " / code=" + json.optInt("code", -1);
                }
            } catch (Exception ignored) {
            }
            return "HTTP " + responseCode + extra;
        } catch (Exception e) {
            return "请求异常：" + e.getClass().getSimpleName();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public List<Course> getTodayCourses(LocalDate date) throws Exception {
        JSONObject data = get("/skl/course?startTime="
                + URLEncoder.encode(date.toString(), "UTF-8") + "&type=day").optJSONObject("data");
        JSONArray list = data == null ? new JSONArray() : data.optJSONArray("list");
        ArrayList<Course> courses = new ArrayList<Course>();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                if (item != null) {
                    courses.add(new Course(item));
                }
            }
        }
        return courses;
    }

    public JSONObject getTeachWeeks(LocalDate date) throws Exception {
        return get("/skl/teachweek?date=" + URLEncoder.encode(date.toString(), "UTF-8"));
    }

    public JSONObject getUserInfo() throws Exception {
        return get("/backstage/auth/infoNew");
    }

    public static final class ApiException extends Exception {
        public final int httpStatus;

        ApiException(String message, int httpStatus) {
            super(message);
            this.httpStatus = httpStatus;
        }

        public boolean isAuthFailure() {
            return this.httpStatus == 401 || this.httpStatus == 403;
        }
    }
}
