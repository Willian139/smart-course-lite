package cn.zuwe.hygiene;

import android.webkit.CookieManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class HygieneApi {
    public static final String BASE = "https://ihome.zuwe.edu.cn";
    public static final String HISTORY_DETAIL = "/appsi/mobile/api/history/getHistoryRoomDetailById/";
    public static final String HISTORY_PAGE = "/appsi/mobile/api/history/getHistoryRoomPageBySn";
    public static final String LOGIN_ROUTE_H5 = "https://ihome.zuwe.edu.cn/wxweb/#/zjweuOAuthLogin";
    public static final String USER_INFO = "/appsys/sys/user/info";
    private String overrideToken = "";
    private final HygieneSession session;

    private static String nvl(String str) {
        return str == null ? "" : str;
    }

    public HygieneApi(HygieneSession hygieneSession) {
        this.session = hygieneSession;
    }

    public HygieneApi withToken(String str) {
        this.overrideToken = str == null ? "" : str.trim();
        return this;
    }

    public JSONObject userInfo() throws IOException {
        return request("GET", USER_INFO, null, null);
    }

    public JSONObject historyPage(int i, int i2, String str, String str2) throws IOException {
        return request("POST", "/appsi/mobile/api/history/getHistoryRoomPageBySn?page=" + enc(String.valueOf(i)) + "&limit=" + enc(String.valueOf(i2)) + "&planId=" + enc(nvl(str)) + "&personSn=" + enc(nvl(str2)), "", "application/x-www-form-urlencoded; charset=UTF-8");
    }

    public JSONObject historyDetail(String str) throws IOException {
        return request("GET", HISTORY_DETAIL + pathEnc(str), null, null);
    }

    private JSONObject request(String str, String str2, String str3, String str4) throws IOException {
        InputStream errorStream;
        String read;
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(BASE + str2).openConnection();
        httpURLConnection.setRequestMethod(str);
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(25000);
        httpURLConnection.setRequestProperty("Accept", "application/json, text/plain, */*");
        httpURLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        httpURLConnection.setRequestProperty("Referer", "https://ihome.zuwe.edu.cn/wxweb/");
        String str5 = this.overrideToken.isEmpty() ? this.session.token() : this.overrideToken;
        if (!str5.isEmpty()) {
            httpURLConnection.setRequestProperty("token", str5);
        }
        String cookie = CookieManager.getInstance().getCookie("https://ihome.zuwe.edu.cn/");
        if (cookie != null && !cookie.isEmpty()) {
            httpURLConnection.setRequestProperty("Cookie", cookie);
        }
        if ("POST".equalsIgnoreCase(str) || "PUT".equalsIgnoreCase(str)) {
            httpURLConnection.setDoOutput(true);
            if (str4 == null) {
                str4 = "application/json;charset=UTF-8";
            }
            httpURLConnection.setRequestProperty("Content-Type", str4);
            if (str3 == null) {
                str3 = "";
            }
            byte[] bytes = str3.getBytes(StandardCharsets.UTF_8);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            try {
                outputStream.write(bytes);
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        try {
            int responseCode = httpURLConnection.getResponseCode();
            String headerField = httpURLConnection.getHeaderField("Set-Cookie");
            if (headerField != null) {
                CookieManager.getInstance().setCookie("https://ihome.zuwe.edu.cn/", headerField);
                CookieManager.getInstance().flush();
            }
            if (responseCode >= 200 && responseCode < 400) {
                errorStream = httpURLConnection.getInputStream();
                read = read(errorStream);
                if (responseCode >= 200 || responseCode >= 300) {
                    throw new IOException("HTTP " + responseCode + ": " + read);
                }
                try {
                    return new JSONObject(read);
                } catch (Exception e) {
                    throw new IOException("响应不是 JSON：" + read, e);
                }
            }
            errorStream = httpURLConnection.getErrorStream();
            read = read(errorStream);
            if (responseCode >= 200) {
            }
            throw new IOException("HTTP " + responseCode + ": " + read);
        } finally {
            httpURLConnection.disconnect();
        }
    }

    public static String usernameOf(JSONObject jSONObject) {
        JSONObject unwrapData = unwrapData(jSONObject);
        JSONObject optJSONObject = unwrapData.optJSONObject("user");
        if (optJSONObject == null) {
            optJSONObject = unwrapData.optJSONObject("userInfo");
        }
        if (optJSONObject != null) {
            unwrapData = optJSONObject;
        }
        String[] strArr = {"username", "personSn", "userName", "userSn", "personNo"};
        for (int i = 0; i < 5; i++) {
            String trim = unwrapData.optString(strArr[i], "").trim();
            if (!trim.isEmpty()) {
                return trim;
            }
        }
        return "";
    }

    public static JSONObject unwrapData(JSONObject jSONObject) {
        JSONObject optJSONObject = jSONObject.optJSONObject("data");
        return optJSONObject == null ? jSONObject : optJSONObject;
    }

    public static JSONObject findPage(JSONObject jSONObject) {
        JSONObject optJSONObject;
        JSONObject optJSONObject2 = jSONObject.optJSONObject("data");
        JSONObject optJSONObject3 = jSONObject.optJSONObject("page");
        if (optJSONObject3 != null) {
            return optJSONObject3;
        }
        if (optJSONObject2 == null || (optJSONObject = optJSONObject2.optJSONObject("page")) == null) {
            return null;
        }
        return optJSONObject;
    }

    public static JSONArray findList(JSONObject jSONObject) {
        if (jSONObject == null) {
            return new JSONArray();
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("list");
        if (optJSONArray != null) {
            return optJSONArray;
        }
        JSONArray optJSONArray2 = jSONObject.optJSONArray("records");
        return optJSONArray2 == null ? new JSONArray() : optJSONArray2;
    }

    public static void ensureSuccess(JSONObject jSONObject) throws IOException {
        int optInt = jSONObject.optInt("code", Integer.MIN_VALUE);
        if (optInt != Integer.MIN_VALUE && optInt != 0 && optInt != 200 && optInt != 20000) {
            throw new IOException(jSONObject.optString("msg", jSONObject.optString("message", "业务接口失败，code=" + optInt)));
        }
        if (!jSONObject.optBoolean("success", true)) {
            throw new IOException(jSONObject.optString("msg", jSONObject.optString("message", "业务接口返回失败")));
        }
    }

    private static String read(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return byteArrayOutputStream.toString("UTF-8");
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    private static String enc(String str) throws IOException {
        return URLEncoder.encode(str, "UTF-8");
    }

    private static String pathEnc(String str) throws IOException {
        return enc(str).replace("+", "%20");
    }
}
