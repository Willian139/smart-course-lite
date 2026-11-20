package cn.zuwe.hygiene;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* loaded from: classes.dex */
public final class HygieneSession {
    private static final String ALIAS = "hygiene_session_aes_v1";
    private static final String KEY_PERSON_SN = "hygiene_person_sn_enc";
    private static final String KEY_TIME = "hygiene_saved_at";
    private static final String KEY_TOKEN = "hygiene_token_enc";
    private static final String KEY_USER = "hygiene_user_enc";
    private static final String PREF = "hygiene_session_v1";
    private final SharedPreferences prefs;

    public HygieneSession(Context context) {
        this.prefs = context.getSharedPreferences(PREF, 0);
    }

    public synchronized void save(String str, String str2) {
        save(str, str2, "");
    }

    public synchronized void save(String str, String str2, String str3) {
        try {
            SharedPreferences.Editor edit = this.prefs.edit();
            if (str == null) {
                str = "";
            }
            SharedPreferences.Editor putString = edit.putString(KEY_TOKEN, encrypt(str));
            if (str2 == null) {
                str2 = "";
            }
            SharedPreferences.Editor putString2 = putString.putString(KEY_PERSON_SN, encrypt(str2));
            if (str3 == null) {
                str3 = "";
            }
            putString2.putString(KEY_USER, encrypt(str3)).putLong(KEY_TIME, System.currentTimeMillis()).apply();
        } catch (Exception e) {
            throw new IllegalStateException("无法安全保存卫生检查会话", e);
        }
    }

    public synchronized String user() {
        return decryptSafe(this.prefs.getString(KEY_USER, ""));
    }

    public synchronized long savedAt() {
        return this.prefs.getLong(KEY_TIME, 0L);
    }

    public synchronized String token() {
        return decryptSafe(this.prefs.getString(KEY_TOKEN, ""));
    }

    public synchronized String personSn() {
        return decryptSafe(this.prefs.getString(KEY_PERSON_SN, ""));
    }

    public synchronized boolean isAuthenticated() {
        return !token().isEmpty() ? !personSn().isEmpty() : false;
    }

    public synchronized void clear() {
        this.prefs.edit().clear().apply();
    }

    private String decryptSafe(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        try {
            return decrypt(str);
        } catch (Exception unused) {
            clear();
            return "";
        }
    }

    private String encrypt(String str) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, getKey());
        byte[] doFinal = cipher.doFinal(str.getBytes("UTF-8"));
        byte[] iv = cipher.getIV();
        byte[] bArr = new byte[iv.length + doFinal.length];
        System.arraycopy(iv, 0, bArr, 0, iv.length);
        System.arraycopy(doFinal, 0, bArr, iv.length, doFinal.length);
        return Base64.encodeToString(bArr, 2);
    }

    private String decrypt(String str) throws Exception {
        byte[] decode = Base64.decode(str, 2);
        if (decode.length < 13) {
            throw new IllegalArgumentException("bad encrypted session");
        }
        byte[] bArr = new byte[12];
        int length = decode.length - 12;
        byte[] bArr2 = new byte[length];
        System.arraycopy(decode, 0, bArr, 0, 12);
        System.arraycopy(decode, 12, bArr2, 0, length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, getKey(), new GCMParameterSpec(128, bArr));
        return new String(cipher.doFinal(bArr2), "UTF-8");
    }

    private SecretKey getKey() throws Exception {
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
}
