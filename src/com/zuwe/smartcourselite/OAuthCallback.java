package com.zuwe.smartcourselite;

import android.net.Uri;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class OAuthCallback {
    private static final Pattern TOKEN = Pattern.compile("(?:[?&])token=([^&]+)");

    private OAuthCallback() {
    }

    public static String authorizationFromUrl(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        int indexOf = str.indexOf(35);
        if (indexOf >= 0) {
            str = str.substring(indexOf + 1);
        }
        Pattern pattern = TOKEN;
        if (str.startsWith("#")) {
            str = str.substring(1);
        }
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            return "";
        }
        try {
            return Uri.decode(matcher.group(1)).trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean looksLikeJwt(String str) {
        if (str == null) {
            return false;
        }
        String[] split = str.trim().split("\\.", -1);
        if (split.length != 3) {
            return false;
        }
        try {
            new JSONObject(new String(Base64.decode(split[1], 11), StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String subject(String str) {
        try {
            JSONObject jSONObject = new JSONObject(new String(Base64.decode(str.split("\\.", -1)[1], 11), StandardCharsets.UTF_8));
            String optString = jSONObject.optString("sub", "");
            return optString.isEmpty() ? jSONObject.optString("user", "") : optString;
        } catch (Exception e) {
            return "";
        }
    }
}
