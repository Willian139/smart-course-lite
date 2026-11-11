package com.zuwe.smartcourselite;

import android.content.Context;
import com.zuwe.smartcourselite.TokenParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class AuthManager {

    public enum State {
        EMPTY,
        READY,
        AUTHORIZATION_UPDATED,
        REAUTH_REQUIRED
    }

    public static final class Credentials {
        public final String authorizationJwt;
        public final String sklTokenJwt;

        Credentials(String str, String str2) {
            this.authorizationJwt = str;
            this.sklTokenJwt = str2;
        }
    }

    public static final class ImportResult {
        public final boolean authorizationUpdated;
        public final boolean cookieUpdated;
        public final Credentials credentials;

        ImportResult(Credentials credentials, boolean z, boolean z2) {
            this.credentials = credentials;
            this.authorizationUpdated = z;
            this.cookieUpdated = z2;
        }
    }

    private AuthManager() {
    }

    public static Credentials load(Context context) throws Exception {
        JSONObject load = AuthVault.load(context);
        String cleanJwt = TokenParser.cleanJwt(load.optString("authorization", ""));
        String cleanJwt2 = TokenParser.cleanJwt(load.optString("sklToken", ""));
        if (!OAuthCallback.looksLikeJwt(cleanJwt)) {
            return null;
        }
        String str = (cleanJwt2.isEmpty() || OAuthCallback.looksLikeJwt(cleanJwt2)) ? cleanJwt2 : "";
        if (!str.isEmpty()) {
            ensureSameAccount(cleanJwt, str);
        }
        return new Credentials(cleanJwt, str);
    }

    public static ImportResult importCapture(Context context, String str) throws Exception {
        TokenParser.Result parse = TokenParser.parse(str);
        JSONObject load = AuthVault.load(context);
        String cleanJwt = TokenParser.cleanJwt(load.optString("authorization", ""));
        String cleanJwt2 = TokenParser.cleanJwt(load.optString("sklToken", ""));
        String str2 = parse.authorizationFound ? parse.authorization : cleanJwt;
        String str3 = parse.cookieFound ? parse.sklToken : cleanJwt2;
        if (!OAuthCallback.looksLikeJwt(str2)) {
            throw new IllegalArgumentException("没有识别到有效 Authorization JWT");
        }
        if (!OAuthCallback.looksLikeJwt(str3)) {
            throw new IllegalArgumentException("没有识别到 sklToken Cookie。请粘贴完整抓包或首次完整登录请求");
        }
        ensureSameAccount(str2, str3);
        AuthVault.save(context, str2, str3);
        return new ImportResult(new Credentials(str2, str3), !str2.equals(cleanJwt), !str3.equals(cleanJwt2));
    }

    public static Credentials importOAuthCallback(Context context, String str) throws Exception {
        String cleanJwt = TokenParser.cleanJwt(OAuthCallback.authorizationFromUrl(str));
        if (!OAuthCallback.looksLikeJwt(cleanJwt)) {
            throw new IllegalArgumentException("回调中没有有效 JWT");
        }
        String str2 = "";
        String cleanJwt2 = TokenParser.cleanJwt(AuthVault.load(context).optString("sklToken", ""));
        if (!cleanJwt2.isEmpty() && OAuthCallback.looksLikeJwt(cleanJwt2)) {
            ensureSameAccount(cleanJwt, cleanJwt2);
            str2 = cleanJwt2;
        }
        AuthVault.save(context, cleanJwt, str2);
        return new Credentials(cleanJwt, str2);
    }

    public static void savePair(Context context, String str, String str2) throws Exception {
        String cleanJwt = TokenParser.cleanJwt(str);
        String cleanJwt2 = TokenParser.cleanJwt(str2);
        if (!OAuthCallback.looksLikeJwt(cleanJwt) || !OAuthCallback.looksLikeJwt(cleanJwt2)) {
            throw new IllegalArgumentException("两枚凭证必须是有效 JWT");
        }
        ensureSameAccount(cleanJwt, cleanJwt2);
        AuthVault.save(context, cleanJwt, cleanJwt2);
    }

    public static void saveAuthorization(Context context, String str) throws Exception {
        String cleanJwt = TokenParser.cleanJwt(str);
        if (!OAuthCallback.looksLikeJwt(cleanJwt)) {
            throw new IllegalArgumentException("授权结果不是有效 JWT");
        }
        String str2 = "";
        String cleanJwt2 = TokenParser.cleanJwt(AuthVault.load(context).optString("sklToken", ""));
        if (!cleanJwt2.isEmpty() && OAuthCallback.looksLikeJwt(cleanJwt2)) {
            ensureSameAccount(cleanJwt, cleanJwt2);
            str2 = cleanJwt2;
        }
        AuthVault.save(context, cleanJwt, str2);
    }

    /**
     * 保存 WebView 登录拿到的凭证：Authorization JWT + 浏览器里的 sklToken Cookie。
     * Cookie 必须一起更新，否则 Authorization 与 Cookie 属于两次不同的会话，服务端会判为无效。
     */
    public static boolean saveFromWeb(Context context, String str, String str2) {
        String cleanJwt = TokenParser.cleanJwt(str);
        if (!OAuthCallback.looksLikeJwt(cleanJwt)) {
            return false;
        }
        String cookie = "";
        if (str2 != null && !str2.isEmpty()) {
            Matcher matcher = Pattern.compile("(?i)sklToken\\s*=\\s*([^;]+)").matcher(str2);
            if (matcher.find()) {
                cookie = TokenParser.cleanJwt(matcher.group(1));
            }
        }
        try {
            if (!cookie.isEmpty()) {
                savePair(context, cleanJwt, cookie);
            } else {
                saveAuthorization(context, cleanJwt);
            }
            return true;
        } catch (Exception e) {
            try {
                saveAuthorization(context, cleanJwt);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    public static void markUnauthorized(Context context) {
    }

    private static void ensureSameAccount(String str, String str2) {
        String subject = OAuthCallback.subject(str);
        String subject2 = OAuthCallback.subject(str2);
        if (!subject.isEmpty() && !subject2.isEmpty() && !subject.equals(subject2)) {
            throw new IllegalArgumentException("Authorization 与 sklToken 不属于同一账号");
        }
    }
}
