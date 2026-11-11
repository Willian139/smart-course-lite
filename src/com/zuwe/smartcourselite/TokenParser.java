package com.zuwe.smartcourselite;

import android.util.Base64;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class TokenParser {
    private static final Pattern JWT = Pattern.compile("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");
    private static final Pattern AUTH_HEADER = Pattern.compile("(?i)Authorization\\s*[:=]\\s*(?:\\\\?\"?Bearer\\s+)?([^\\s,;\\\"<>]+)");
    private static final Pattern COOKIE = Pattern.compile("(?i)sklToken\\s*=\\s*([^;\\s\\\"<>]+)");
    private static final Pattern URL_TOKEN = Pattern.compile("(?i)(?:[?&#]|%3[fF]|%26)token(?:=|%3[dD])([^&#\\s\\\"<>]+)");
    private static final Pattern OAUTH_FRAGMENT = Pattern.compile("(?i)#/login\\?token=([^&\\s\\\"<>]+)");

    private TokenParser() {
    }

    public static final class Result {
        public final String authorization;
        public final boolean authorizationFound;
        public final boolean cookieFound;
        public final String sklToken;

        Result(String str, String str2) {
            this.authorization = str;
            this.sklToken = str2;
            this.authorizationFound = !str.isEmpty();
            this.cookieFound = !str2.isEmpty();
        }

        public boolean complete() {
            return this.authorizationFound && this.cookieFound;
        }
    }

    public static Result parse(String str) {
        String str2 = "";
        if (str == null || str.trim().isEmpty()) {
            return new Result("", "");
        }
        String replace = str.replace("\\u003d", "=").replace("\\u003D", "=").replace("\\u0026", "&").replace("\\u003f", "?").replace("\\u003F", "?").replace("\\/", "/");
        Matcher matcher = OAUTH_FRAGMENT.matcher(replace);
        String str3 = "";
        while (matcher.find()) {
            String cleanJwt = cleanJwt(matcher.group(1));
            if (!cleanJwt.isEmpty()) {
                str3 = cleanJwt;
            }
        }
        int i = -1;
        if (str3.isEmpty()) {
            LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
            Matcher matcher2 = AUTH_HEADER.matcher(replace);
            while (matcher2.find()) {
                String cleanJwt2 = cleanJwt(matcher2.group(1));
                if (!cleanJwt2.isEmpty()) {
                    linkedHashMap.put(cleanJwt2, Integer.valueOf(linkedHashMap.getOrDefault(cleanJwt2, 0).intValue() + 1));
                }
            }
            int i2 = -1;
            for (Map.Entry<String, Integer> entry : linkedHashMap.entrySet()) {
                if (entry.getValue().intValue() > i2) {
                    str3 = entry.getKey();
                    i2 = entry.getValue().intValue();
                }
            }
        }
        if (str3.isEmpty()) {
            Matcher matcher3 = URL_TOKEN.matcher(replace);
            while (matcher3.find()) {
                String cleanJwt3 = cleanJwt(matcher3.group(1));
                if (!cleanJwt3.isEmpty()) {
                    str3 = cleanJwt3;
                }
            }
        }
        if (str3.isEmpty()) {
            Matcher matcher4 = JWT.matcher(replace);
            if (matcher4.find()) {
                str3 = matcher4.group();
            }
        }
        LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<String, Integer>();
        Matcher matcher5 = COOKIE.matcher(replace);
        while (matcher5.find()) {
            String cleanJwt4 = cleanJwt(matcher5.group(1));
            if (!cleanJwt4.isEmpty()) {
                linkedHashMap2.put(cleanJwt4, Integer.valueOf(linkedHashMap2.getOrDefault(cleanJwt4, 0).intValue() + 1));
            }
        }
        for (Map.Entry<String, Integer> entry2 : linkedHashMap2.entrySet()) {
            if (entry2.getValue().intValue() > i) {
                str2 = entry2.getKey();
                i = entry2.getValue().intValue();
            }
        }
        return new Result(str3, str2);
    }

    /** 解析 JWT 的 exp，返回「有效至 21:05」/「已过期 11-11 21:05」，无法解析时返回空串。 */
    public static String expiryLabel(String str) {
        try {
            String[] split = str.trim().split("\\.");
            if (split.length < 2) {
                return "";
            }
            JSONObject payload = new JSONObject(new String(Base64.decode(split[1], 11), StandardCharsets.UTF_8));
            long exp = payload.optLong("exp", 0L);
            if (exp <= 0L) {
                return "";
            }
            java.time.LocalDateTime time = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(exp), java.time.ZoneId.systemDefault());
            String clock = time.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            return exp * 1000L < System.currentTimeMillis() ? ("已过期 " + clock) : ("有效至 " + clock);
        } catch (Exception e) {
            return "";
        }
    }

    public static String cleanJwt(String str) {
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        for (int i = 0; i < 2; i++) {
            try {
                trim = URLDecoder.decode(trim, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
            }
        }
        String trim2 = trim.replace("\\u003d", "=").trim();
        if (trim2.regionMatches(true, 0, "Bearer ", 0, 7)) {
            trim2 = trim2.substring(7).trim();
        }
        if (trim2.regionMatches(true, 0, "Bearer%20", 0, 9)) {
            trim2 = trim2.substring(9).trim();
        }
        Matcher matcher = JWT.matcher(trim2);
        return matcher.find() ? matcher.group() : "";
    }
}
