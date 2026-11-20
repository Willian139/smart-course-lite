package com.zuwe.smartcourselite;

import android.content.Context;
import com.zuwe.smartcourselite.TokenParser;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class AuthManager {

    public enum State {
        EMPTY,
        READY,
        AUTHORIZATION_UPDATED,
        REAUTH_REQUIRED
    }

    public static void markUnauthorized(Context context) {
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

    /* JADX WARN: Removed duplicated region for block: B:15:0x0036 A[Catch: Exception -> 0x003e, TryCatch #0 {Exception -> 0x003e, blocks: (B:13:0x0030, B:15:0x0036, B:17:0x003a), top: B:12:0x0030 }] */
    /* JADX WARN: Removed duplicated region for block: B:17:0x003a A[Catch: Exception -> 0x003e, TRY_LEAVE, TryCatch #0 {Exception -> 0x003e, blocks: (B:13:0x0030, B:15:0x0036, B:17:0x003a), top: B:12:0x0030 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean saveFromWeb(android.content.Context r3, java.lang.String r4, java.lang.String r5) {
        /*
            java.lang.String r4 = com.zuwe.smartcourselite.TokenParser.cleanJwt(r4)
            boolean r0 = com.zuwe.smartcourselite.OAuthCallback.looksLikeJwt(r4)
            r1 = 0
            if (r0 != 0) goto Lc
            return r1
        Lc:
            r0 = 1
            if (r5 == 0) goto L2e
            boolean r2 = r5.isEmpty()
            if (r2 != 0) goto L2e
            java.lang.String r2 = "(?i)sklToken\\s*=\\s*([^;]+)"
            java.util.regex.Pattern r2 = java.util.regex.Pattern.compile(r2)
            java.util.regex.Matcher r5 = r2.matcher(r5)
            boolean r2 = r5.find()
            if (r2 == 0) goto L2e
            java.lang.String r5 = r5.group(r0)
            java.lang.String r5 = com.zuwe.smartcourselite.TokenParser.cleanJwt(r5)
            goto L30
        L2e:
            java.lang.String r5 = ""
        L30:
            boolean r2 = r5.isEmpty()     // Catch: java.lang.Exception -> L3e
            if (r2 != 0) goto L3a
            savePair(r3, r4, r5)     // Catch: java.lang.Exception -> L3e
            goto L3d
        L3a:
            saveAuthorization(r3, r4)     // Catch: java.lang.Exception -> L3e
        L3d:
            return r0
        L3e:
            saveAuthorization(r3, r4)     // Catch: java.lang.Exception -> L42
            return r0
        L42:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.AuthManager.saveFromWeb(android.content.Context, java.lang.String, java.lang.String):boolean");
    }

    private static void ensureSameAccount(String str, String str2) {
        String subject = OAuthCallback.subject(str);
        String subject2 = OAuthCallback.subject(str2);
        if (!subject.isEmpty() && !subject2.isEmpty() && !subject.equals(subject2)) {
            throw new IllegalArgumentException("Authorization 与 sklToken 不属于同一账号");
        }
    }
}
