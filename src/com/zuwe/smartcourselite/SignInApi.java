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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 课堂签到接口。与课表 API 分离，避免签到接口的业务 code 规则影响课表请求。
 * 当前已确认：统计和个人明细直接返回 JSON 数组；valid-code 在 HTTP 200 下
 * 仍可能返回业务 code=400，因此此处保留原始业务响应，由 UI 判断。
 */
public final class SignInApi {
    private static final String BASE = "https://skl.zuwe.edu.cn";
    private static final String PREFS = "smartcourse_prefs";
    private static final String KEY_AUTH_MODE = "auth_mode";
    private static final int MODE_AUTH_COOKIE = 0;
    private static final int MODE_AUTH = 1;
    private static final int MODE_COOKIE = 2;

    private final String authorization;
    private final String sklToken;
    private final SharedPreferences prefs;
    private volatile int workingMode;

    public SignInApi(Context context, String authorization, String sklToken) {
        this.authorization = authorization == null ? "" : authorization;
        this.sklToken = sklToken == null ? "" : sklToken;
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int saved = prefs.getInt(KEY_AUTH_MODE, MODE_AUTH);
        this.workingMode = saved >= 0 && saved <= 2 ? saved : MODE_AUTH;
    }

    public JSONObject getStatistics(LocalDate start, LocalDate end) throws Exception {
        JSONArray array = getArray("/skl/stat/stu/user?startDate=" + enc(start.toString())
                + "&endDate=" + enc(end.toString()));
        return array.length() > 0 ? array.optJSONObject(0) : null;
    }

    public JSONArray getHistory(LocalDate start, LocalDate end) throws Exception {
        return getArray("/skl/check-in-student-detail/my?startDate=" + enc(start.toString())
                + "&endDate=" + enc(end.toString()));
    }

    /**
     * 发起平台原生签到码请求。此方法不把业务 code=400 当作 HTTP 异常，
     * 也不自动重试，避免重复提交；返回值必须由调用方检查业务 code。
     */
    public JSONObject validateCode(String code, double latitude, double longitude) throws Exception {
        String path = "/skl/checkIn/valid-code?code=" + enc(code)
                + "&latitude=" + enc(Double.toString(latitude))
                + "&longitude=" + enc(Double.toString(longitude));
        String body = getRaw(path, false);
        return new JSONObject(body);
    }

    private JSONArray getArray(String path) throws Exception {
        String body = getRaw(path, true);
        String trimmed = body == null ? "" : body.trim();
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }
        if (trimmed.startsWith("{")) {
            JSONObject object = new JSONObject(trimmed);
            int businessCode = object.optInt("code", -1);
            if (businessCode == 401 || businessCode == 403) {
                throw new ApiException(object.optString("message", "登录凭证失效"), businessCode);
            }
            if (businessCode != -1 && businessCode != 200 && businessCode != 0) {
                throw new ApiException(object.optString("message", "签到数据接口业务错误：" + businessCode), businessCode);
            }
            JSONArray data = object.optJSONArray("data");
            if (data != null) return data;
            JSONObject nested = object.optJSONObject("data");
            if (nested != null) {
                JSONArray list = nested.optJSONArray("list");
                if (list != null) return list;
            }
            return new JSONArray();
        }
        throw new IOException("签到接口返回了非 JSON 内容");
    }

    private String getRaw(String path, boolean retryIo) throws Exception {
        int[] order;
        if (workingMode == MODE_AUTH_COOKIE) {
            order = new int[]{MODE_AUTH_COOKIE, MODE_AUTH, MODE_COOKIE};
        } else if (workingMode == MODE_COOKIE) {
            order = new int[]{MODE_COOKIE, MODE_AUTH_COOKIE, MODE_AUTH};
        } else {
            order = new int[]{MODE_AUTH, MODE_AUTH_COOKIE, MODE_COOKIE};
        }
        ApiException lastAuth = null;
        for (int mode : order) {
            boolean useAuth = mode != MODE_COOKIE;
            boolean useCookie = mode != MODE_AUTH;
            if (useAuth && authorization.isEmpty()) continue;
            if (useCookie && sklToken.isEmpty()) continue;
            try {
                String body = request(path, useAuth, useCookie, retryIo);
                if (mode != workingMode) {
                    workingMode = mode;
                    prefs.edit().putInt(KEY_AUTH_MODE, mode).apply();
                }
                return body;
            } catch (ApiException e) {
                if (!e.isAuthFailure()) throw e;
                lastAuth = e;
            }
        }
        if (lastAuth != null) throw lastAuth;
        throw new ApiException("没有可用的登录凭证", 401);
    }

    private String request(String path, boolean useAuth, boolean useCookie, boolean retryIo) throws Exception {
        int attempts = retryIo ? 2 : 1;
        IOException lastIo = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(BASE + path).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(8000);
                connection.setInstanceFollowRedirects(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "application/json, text/plain, */*");
                connection.setRequestProperty("Referer", BASE + "/");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) SmartCourseLite/2.6.1");
                if (useAuth) connection.setRequestProperty("Authorization", "Bearer " + authorization);
                if (useCookie) {
                    connection.setRequestProperty("Cookie", cookieHeader(sklToken));
                } else {
                    // 抓包中的 Authorization-only 请求仍携带 version=2.0.6。
                    connection.setRequestProperty("Cookie", "version=2.0.6");
                }
                int status = connection.getResponseCode();
                InputStream stream = (status < 200 || status >= 400)
                        ? connection.getErrorStream() : connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                if (stream != null) {
                    byte[] chunk = new byte[4096];
                    int count;
                    while ((count = stream.read(chunk)) != -1) buffer.write(chunk, 0, count);
                    stream.close();
                }
                String body = buffer.toString(StandardCharsets.UTF_8.name());
                if (status == 401 || status == 403) throw new ApiException("登录凭证失效或无权限（HTTP " + status + "）", status);
                if (status >= 300 && status < 400) throw new ApiException("登录状态已失效，请重新授权", 401);
                if (status < 200 || status >= 300) throw new ApiException("服务器 HTTP 错误：" + status, status);
                return body;
            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                lastIo = e;
                if (attempt + 1 >= attempts) throw e;
                try { Thread.sleep(350L); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        throw lastIo == null ? new IOException("签到接口请求失败") : lastIo;
    }

    private static String cookieHeader(String token) throws Exception {
        return "sklToken=" + URLEncoder.encode("Bearer " + token, "UTF-8").replace("+", "%20")
                + "; version=2.0.6";
    }

    private static String enc(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    public static final class ApiException extends Exception {
        public final int httpStatus;
        ApiException(String message, int httpStatus) {
            super(message);
            this.httpStatus = httpStatus;
        }
        public boolean isAuthFailure() { return httpStatus == 401 || httpStatus == 403; }
    }
}
