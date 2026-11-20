package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class SignInApi {
    private static final String BASE = "https://skl.zuwe.edu.cn";
    private static final String KEY_AUTH_MODE = "auth_mode";
    private static final int MODE_AUTH = 1;
    private static final int MODE_AUTH_COOKIE = 0;
    private static final int MODE_COOKIE = 2;
    private static final String PREFS = "smartcourse_prefs";
    private final String authorization;
    private final SharedPreferences prefs;
    private final String sklToken;
    private volatile int workingMode;

    public SignInApi(Context context, String str, String str2) {
        this.authorization = str == null ? "" : str;
        this.sklToken = str2 == null ? "" : str2;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, MODE_AUTH_COOKIE);
        this.prefs = sharedPreferences;
        int i = MODE_AUTH;
        int i2 = sharedPreferences.getInt(KEY_AUTH_MODE, MODE_AUTH);
        if (i2 >= 0 && i2 <= MODE_COOKIE) {
            i = i2;
        }
        this.workingMode = i;
    }

    public JSONObject getStatistics(LocalDate localDate, LocalDate localDate2) throws Exception {
        JSONArray array = getArray("/skl/stat/stu/user?startDate=" + enc(localDate.toString()) + "&endDate=" + enc(localDate2.toString()));
        if (array.length() > 0) {
            return array.optJSONObject(MODE_AUTH_COOKIE);
        }
        return null;
    }

    public JSONArray getHistory(LocalDate localDate, LocalDate localDate2) throws Exception {
        return getArray("/skl/check-in-student-detail/my?startDate=" + enc(localDate.toString()) + "&endDate=" + enc(localDate2.toString()));
    }

    public JSONObject validateCode(String str, double d, double d2) throws Exception {
        return new JSONObject(getRaw("/skl/checkIn/valid-code?code=" + enc(str) + "&latitude=" + enc(Double.toString(d)) + "&longitude=" + enc(Double.toString(d2)), false));
    }

    private JSONArray getArray(String str) throws Exception {
        JSONArray optJSONArray;
        String raw = getRaw(str, true);
        String trim = raw == null ? "" : raw.trim();
        if (trim.startsWith("[")) {
            return new JSONArray(trim);
        }
        if (trim.startsWith("{")) {
            JSONObject jSONObject = new JSONObject(trim);
            int optInt = jSONObject.optInt("code", -1);
            if (optInt == 401 || optInt == 403) {
                throw new ApiException(jSONObject.optString("message", "登录凭证失效"), optInt);
            }
            if (optInt != -1 && optInt != 200 && optInt != 0) {
                throw new ApiException(jSONObject.optString("message", "签到数据接口业务错误：" + optInt), optInt);
            }
            JSONArray optJSONArray2 = jSONObject.optJSONArray("data");
            if (optJSONArray2 != null) {
                return optJSONArray2;
            }
            JSONObject optJSONObject = jSONObject.optJSONObject("data");
            return (optJSONObject == null || (optJSONArray = optJSONObject.optJSONArray("list")) == null) ? new JSONArray() : optJSONArray;
        }
        throw new IOException("签到接口返回了非 JSON 内容");
    }

    private String getRaw(String str, boolean z) throws Exception {
        int[] iArr;
        if (this.workingMode == 0) {
            iArr = new int[]{MODE_AUTH_COOKIE, MODE_AUTH, MODE_COOKIE};
        } else if (this.workingMode == MODE_COOKIE) {
            iArr = new int[]{MODE_COOKIE, MODE_AUTH_COOKIE, MODE_AUTH};
        } else {
            iArr = new int[]{MODE_AUTH, MODE_AUTH_COOKIE, MODE_COOKIE};
        }
        int length = iArr.length;
        ApiException e = null;
        for (int i = MODE_AUTH_COOKIE; i < length; i += MODE_AUTH) {
            int i2 = iArr[i];
            boolean z2 = i2 != MODE_COOKIE;
            boolean z3 = i2 != MODE_AUTH;
            if ((!z2 || !this.authorization.isEmpty()) && (!z3 || !this.sklToken.isEmpty())) {
                try {
                    String request = request(str, z2, z3, z);
                    if (i2 != this.workingMode) {
                        this.workingMode = i2;
                        this.prefs.edit().putInt(KEY_AUTH_MODE, i2).apply();
                    }
                    return request;
                } catch (ApiException e2) {
                    e = e2;
                    if (!e.isAuthFailure()) {
                        throw e;
                    }
                }
            }
        }
        if (e != null) {
            throw e;
        }
        throw new ApiException("没有可用的登录凭证", 401);
    }

    /* JADX WARN: Code restructure failed: missing block: B:43:0x00d5, code lost:
    
        if (r0 >= 300) goto L45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x00d7, code lost:
    
        if (r6 == null) goto L44;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x00d9, code lost:
    
        r6.disconnect();
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x00dc, code lost:
    
        return r10;
     */
    /* JADX WARN: Removed duplicated region for block: B:59:0x0121  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x013d  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x0138 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private java.lang.String request(java.lang.String r16, boolean r17, boolean r18, boolean r19) throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 331
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.SignInApi.request(java.lang.String, boolean, boolean, boolean):java.lang.String");
    }

    private static String cookieHeader(String str) throws Exception {
        return "sklToken=" + URLEncoder.encode("Bearer " + str, "UTF-8").replace("+", "%20") + "; version=2.0.6";
    }

    private static String enc(String str) throws Exception {
        return URLEncoder.encode(str, "UTF-8");
    }

    public static final class ApiException extends Exception {
        public final int httpStatus;

        ApiException(String str, int i) {
            super(str);
            this.httpStatus = i;
        }

        public boolean isAuthFailure() {
            int i = this.httpStatus;
            return i == 401 || i == 403;
        }
    }
}
