package cn.zuwe.hygiene;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.webkit.CookieManager;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** 卫生检查登录态：token 用 AndroidKeyStore AES-GCM 加密保存，登录一次长期复用。 */
public final class HygieneSession {
    private static final String PREF = "hygiene_session_v1";
    private static final String ALIAS = "hygiene_session_aes_v1";
    private static final String KEY_TOKEN = "hygiene_token_enc";
    private static final String KEY_PERSON_SN = "hygiene_person_sn_enc";
    private static final String KEY_USER = "hygiene_user_enc";
    private static final String KEY_TIME = "hygiene_saved_at";
    private final SharedPreferences prefs;

    public HygieneSession(Context context) { prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE); }
    public synchronized void save(String token, String personSn) { save(token, personSn, ""); }
    public synchronized void save(String token, String personSn, String displayUser) {
        try { prefs.edit().putString(KEY_TOKEN, encrypt(token == null ? "" : token))
                .putString(KEY_PERSON_SN, encrypt(personSn == null ? "" : personSn))
                .putString(KEY_USER, encrypt(displayUser == null ? "" : displayUser))
                .putLong(KEY_TIME, System.currentTimeMillis()).apply(); }
        catch (Exception e) { throw new IllegalStateException("无法安全保存卫生检查会话", e); }
    }
    public synchronized String user() { return decryptSafe(prefs.getString(KEY_USER, "")); }
    public synchronized long savedAt() { return prefs.getLong(KEY_TIME, 0L); }
    public synchronized String token() { return decryptSafe(prefs.getString(KEY_TOKEN, "")); }
    public synchronized String personSn() { return decryptSafe(prefs.getString(KEY_PERSON_SN, "")); }
    public synchronized boolean isAuthenticated() { return !token().isEmpty() && !personSn().isEmpty(); }
    public synchronized void clear() {
        prefs.edit().clear().apply();
    }
    private String decryptSafe(String value) {
        if (value == null || value.isEmpty()) return "";
        try { return decrypt(value); } catch (Exception e) { clear(); return ""; }
    }
    private String encrypt(String plain) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] encrypted = c.doFinal(plain.getBytes("UTF-8")); byte[] iv = c.getIV();
        byte[] packed = new byte[iv.length + encrypted.length]; System.arraycopy(iv,0,packed,0,iv.length); System.arraycopy(encrypted,0,packed,iv.length,encrypted.length);
        return Base64.encodeToString(packed, Base64.NO_WRAP);
    }
    private String decrypt(String packedText) throws Exception {
        byte[] packed = Base64.decode(packedText, Base64.NO_WRAP); if (packed.length < 13) throw new IllegalArgumentException("bad encrypted session");
        byte[] iv = new byte[12], data = new byte[packed.length - 12]; System.arraycopy(packed,0,iv,0,12); System.arraycopy(packed,12,data,0,data.length);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.DECRYPT_MODE,getKey(),new GCMParameterSpec(128,iv));
        return new String(c.doFinal(data),"UTF-8");
    }
    private SecretKey getKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        java.security.Key existing = ks.getKey(ALIAS,null); if(existing instanceof SecretKey)return (SecretKey)existing;
        KeyGenerator kg = KeyGenerator.getInstance("AES","AndroidKeyStore");
        kg.init(new android.security.keystore.KeyGenParameterSpec.Builder(ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT | android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());
        return kg.generateKey();
    }
}
