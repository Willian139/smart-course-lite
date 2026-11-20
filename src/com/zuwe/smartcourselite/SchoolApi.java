package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class SchoolApi {
    private static final String BASE = "https://skl.zuwe.edu.cn";
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final String KEY_AUTH_MODE = "auth_mode";
    private static final int MAX_ATTEMPTS = 2;
    private static final int MODE_AUTH = 1;
    private static final int MODE_AUTH_COOKIE = 0;
    private static final int MODE_COOKIE = 2;
    private static final String PREFS = "smartcourse_prefs";
    private static final int READ_TIMEOUT_MS = 8000;
    private final String authorizationJwt;
    private final SharedPreferences prefs;
    private final String sklToken;
    private volatile int workingAuthMode;

    public SchoolApi(Context context, String str, String str2) {
        this.workingAuthMode = MODE_AUTH;
        this.authorizationJwt = str;
        this.sklToken = str2;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, MODE_AUTH_COOKIE);
        this.prefs = sharedPreferences;
        this.workingAuthMode = sharedPreferences.getInt(KEY_AUTH_MODE, MODE_AUTH);
        if (this.workingAuthMode < 0 || this.workingAuthMode > 2) {
            this.workingAuthMode = MODE_AUTH;
        }
    }

    private void rememberMode(int i) {
        this.workingAuthMode = i;
        this.prefs.edit().putInt(KEY_AUTH_MODE, i).apply();
    }

    private JSONObject get(String str) throws Exception {
        int[] iArr;
        String str2;
        if (this.workingAuthMode == 0) {
            iArr = new int[]{MODE_AUTH_COOKIE, MODE_AUTH, 2};
        } else if (this.workingAuthMode == 2) {
            iArr = new int[]{2, MODE_AUTH_COOKIE, MODE_AUTH};
        } else {
            iArr = new int[]{MODE_AUTH, MODE_AUTH_COOKIE, 2};
        }
        int length = iArr.length;
        ApiException e = null;
        for (int i = MODE_AUTH_COOKIE; i < length; i += MODE_AUTH) {
            int i2 = iArr[i];
            boolean z = i2 != 2;
            boolean z2 = i2 != MODE_AUTH;
            if (!z2 || ((str2 = this.sklToken) != null && !str2.isEmpty())) {
                try {
                    JSONObject once = getOnce(str, z, z2);
                    if (i2 != this.workingAuthMode) {
                        rememberMode(i2);
                    }
                    return once;
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
        throw new ApiException("认证失效：本机没有可用凭证", 401);
    }

    private JSONObject getOnce(String str, boolean z, boolean z2) throws Exception {
        IOException e = null;
        int i = MODE_AUTH_COOKIE;
        while (i < 2) {
            try {
                return request(str, z, z2);
            } catch (ApiException e2) {
                throw e2;
            } catch (IOException e3) {
                e = e3;
                i += MODE_AUTH;
                if (i < 2) {
                    try {
                        Thread.sleep(450L);
                    } catch (InterruptedException unused) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        }
        if (e == null) {
            throw new IOException("网络请求失败");
        }
        throw e;
    }

    /* JADX WARN: Code restructure failed: missing block: B:38:0x00bc, code lost:
    
        if (r10 >= 300) goto L59;
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x00be, code lost:
    
        r11 = new org.json.JSONObject(r4.toString(java.nio.charset.StandardCharsets.UTF_8.name()));
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x00d1, code lost:
    
        if (r11.has("code") != false) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:43:0x00d3, code lost:
    
        if (r9 == null) goto L42;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x00d5, code lost:
    
        r9.disconnect();
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x00d8, code lost:
    
        return r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x00d9, code lost:
    
        r10 = r11.optInt("code", -1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x00df, code lost:
    
        if (r10 == 401) goto L55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x00e1, code lost:
    
        if (r10 == 403) goto L55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x00e3, code lost:
    
        if (r10 == 200) goto L52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x00e5, code lost:
    
        if (r10 != 0) goto L50;
     */
    /* JADX WARN: Code restructure failed: missing block: B:53:0x0102, code lost:
    
        throw new com.zuwe.smartcourselite.SchoolApi.ApiException(r11.optString("message", "接口业务错误：" + r10), r10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x0103, code lost:
    
        if (r9 == null) goto L54;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x0105, code lost:
    
        r9.disconnect();
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x0108, code lost:
    
        return r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:58:0x0114, code lost:
    
        throw new com.zuwe.smartcourselite.SchoolApi.ApiException(r11.optString("message", "认证失效"), r10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x011c, code lost:
    
        throw new com.zuwe.smartcourselite.SchoolApi.ApiException("服务器返回内容异常，请稍后重试", r10);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private org.json.JSONObject request(java.lang.String r9, boolean r10, boolean r11) throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 346
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.SchoolApi.request(java.lang.String, boolean, boolean):org.json.JSONObject");
    }

    private static String cookieHeader(String str) throws Exception {
        return "sklToken=" + URLEncoder.encode("Bearer " + str, "UTF-8").replace("+", "%20") + "; version=2.0.6";
    }

    /* JADX WARN: Can't wrap try/catch for region: R(16:5|6|(1:8)|(1:14)|15|(10:20|21|(3:23|(2:24|(1:26)(1:27))|28)|29|30|31|(1:33)|35|(1:37)|38)|42|21|(0)|29|30|31|(0)|35|(0)|38) */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0087 A[Catch: all -> 0x00de, Exception -> 0x00e1, TryCatch #3 {all -> 0x00de, blocks: (B:6:0x0012, B:8:0x003f, B:10:0x0054, B:12:0x0058, B:14:0x005e, B:15:0x0069, B:20:0x0076, B:21:0x007f, B:23:0x0087, B:24:0x008b, B:26:0x0091, B:28:0x0095, B:31:0x009a, B:33:0x00af, B:35:0x00c4, B:42:0x007b), top: B:5:0x0012 }] */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00af A[Catch: Exception -> 0x00c4, all -> 0x00de, TRY_LEAVE, TryCatch #2 {Exception -> 0x00c4, blocks: (B:31:0x009a, B:33:0x00af), top: B:30:0x009a }] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00da  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.String probeHomeIcon(boolean r8, boolean r9) {
        /*
            Method dump skipped, instructions count: 268
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.SchoolApi.probeHomeIcon(boolean, boolean):java.lang.String");
    }

    public List<Course> getTodayCourses(LocalDate localDate) throws Exception {
        JSONObject optJSONObject = get("/skl/course?startTime=" + URLEncoder.encode(localDate.toString(), "UTF-8") + "&type=day").optJSONObject("data");
        JSONArray jSONArray = optJSONObject == null ? new JSONArray() : optJSONObject.optJSONArray("list");
        ArrayList arrayList = new ArrayList();
        if (jSONArray != null) {
            for (int i = MODE_AUTH_COOKIE; i < jSONArray.length(); i += MODE_AUTH) {
                JSONObject optJSONObject2 = jSONArray.optJSONObject(i);
                if (optJSONObject2 != null) {
                    arrayList.add(new Course(optJSONObject2));
                }
            }
        }
        return arrayList;
    }

    public JSONObject getTeachWeeks(LocalDate localDate) throws Exception {
        return get("/skl/teachweek?date=" + URLEncoder.encode(localDate.toString(), "UTF-8"));
    }

    public JSONObject getUserInfo() throws Exception {
        return get("/backstage/auth/infoNew");
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
