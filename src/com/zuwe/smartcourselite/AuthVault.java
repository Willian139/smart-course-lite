package com.zuwe.smartcourselite;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class AuthVault {
    private static final String ALIAS = "smart_ideology_aes_key";
    private static final String PREF = "smart_ideology_auth";
    private static final String VALUE = "credential_blob";

    private AuthVault() {
    }

    private static SecretKey key() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Key key = keyStore.getKey(ALIAS, null);
        if (key instanceof SecretKey) {
            return (SecretKey) key;
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setRandomizedEncryptionRequired(true).build());
        return keyGenerator.generateKey();
    }

    public static void save(Context context, String str, String str2) throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("authorization", str);
        jSONObject.put("sklToken", str2);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key());
        byte[] iv = cipher.getIV();
        byte[] doFinal = cipher.doFinal(jSONObject.toString().getBytes(StandardCharsets.UTF_8));
        byte[] bArr = new byte[iv.length + doFinal.length];
        System.arraycopy(iv, 0, bArr, 0, iv.length);
        System.arraycopy(doFinal, 0, bArr, iv.length, doFinal.length);
        prefs(context).edit().putString(VALUE, Base64.encodeToString(bArr, 2)).apply();
    }

    public static JSONObject load(Context context) throws Exception {
        String string = prefs(context).getString(VALUE, null);
        if (string == null) {
            return new JSONObject();
        }
        byte[] decode = Base64.decode(string, 2);
        if (decode.length < 29) {
            return new JSONObject();
        }
        byte[] bArr = new byte[12];
        int length = decode.length - 12;
        byte[] bArr2 = new byte[length];
        System.arraycopy(decode, 0, bArr, 0, 12);
        System.arraycopy(decode, 12, bArr2, 0, length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, key(), new GCMParameterSpec(128, bArr));
        return new JSONObject(new String(cipher.doFinal(bArr2), StandardCharsets.UTF_8));
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(VALUE).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF, 0);
    }
}
