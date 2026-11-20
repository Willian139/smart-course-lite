package com.zuwe.smartcourselite;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 教务系统（正方 jwglxt）接口封装：
 * 登录 -> 取课表 -> 解析成「第几周 / 周几 / 第几节」的结构。
 * 只依赖 JDK + Android 自带 org.json。
 */
public final class JwApi {

    public static final String HOST = "https://ijw.zuwe.edu.cn";

    /** 抓包里这个账号的班级上下文，用于「班级课表」兜底。 */
    private static final String DEF_NJDM_ID = "2026";
    private static final String DEF_ZYH_ID = "508F277EE0F2A136E0635803C80A3CCF";
    private static final String DEF_BH_ID = "141682601";
    private static final String DEF_XQH_ID = "02";
    private static final String DEF_ZYMC = "车辆工程(中本一体化)";
    private static final String DEF_JGMC = "机械工程学院";
    private static final String DEF_BJ = "车辆ZB26-1";
    private static final String DEF_XKRS = "40";

    private final LinkedHashMap<String, String> jar = new LinkedHashMap<String, String>();
    private int lastCode;
    private String lastLocation = "";
    private String lastUrl = "";
    private byte[] lastRaw = new byte[0];
    private String csrfToken = "";
    private String homeTitle = "";
    public String lastKeyInfo = "";
    public String lastFormInfo = "";
    public String lastPageHints = "";
    public String lastPwInfo = "";
    private String lastLoginMode = "";
    private String referer = HOST + "/jwglxt/xtgl/index_initMenu.html";
    private String lastCharset = "";
    private String lastBody = "";

    // ---------------------------------------------------------------- 模型

    public static final class KbItem {
        public String name = "";
        public String teacher = "";
        public String room = "";
        public String exam = "";
        public int weekDay = 1;
        public int start = 1;
        public int end = 1;
        public int[] weeks = new int[0];

        public boolean inWeek(int w) {
            for (int i = 0; i < weeks.length; i++) {
                if (weeks[i] == w) return true;
            }
            return false;
        }
    }

    public static final class WeekRange {
        public int week;
        public LocalDate start;
        public LocalDate end;
    }

    public static final class Result {
        public String source = "";
        public List<KbItem> items = new ArrayList<KbItem>();
        public List<WeekRange> weeks = new ArrayList<WeekRange>();
        public String student = "";
        public String rawHead = "";
        public String xnm = "";
        public String xqm = "";
    }

    // ---------------------------------------------------------------- 工具

    public static String enc(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private String cookieStr() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = jar.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            if (sb.length() > 0) sb.append("; ");
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    private void absorbCookies(HttpURLConnection c) {
        try {
            Map<String, List<String>> h = c.getHeaderFields();
            if (h == null) return;
            List<String> sc = h.get("Set-Cookie");
            if (sc == null) return;
            for (int i = 0; i < sc.size(); i++) {
                String line = sc.get(i);
                if (line == null) continue;
                int semi = line.indexOf(';');
                String pair = semi >= 0 ? line.substring(0, semi) : line;
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    String k = pair.substring(0, eq).trim();
                    String v = pair.substring(eq + 1).trim();
                    if (k.length() > 0) jar.put(k, v);
                }
            }
        } catch (Throwable t) {
            // ignore
        }
    }

    private static String charsetOf(String contentType) {
        if (contentType == null) return "";
        int i = contentType.toLowerCase().indexOf("charset=");
        if (i < 0) return "";
        String cs = contentType.substring(i + 8).trim();
        int sc = cs.indexOf(';');
        if (sc > 0) cs = cs.substring(0, sc);
        return cs.trim();
    }

    /** 教务系统有些页面是 GBK，先按声明解，解不动再退回 UTF-8 / GBK 二选一。 */
    public static String decodeText(byte[] raw, String charset) {
        if (raw == null || raw.length == 0) return "";
        String declared = charset == null ? "" : charset.trim();
        if (declared.length() > 0) {
            try {
                return new String(raw, Charset.forName(declared));
            } catch (Throwable t) {
                // 继续尝试
            }
        }
        try {
            CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
            dec.onMalformedInput(CodingErrorAction.REPORT);
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            return dec.decode(ByteBuffer.wrap(raw)).toString();
        } catch (Throwable t) {
            try {
                return new String(raw, Charset.forName("GBK"));
            } catch (Throwable t2) {
                return new String(raw);
            }
        }
    }

    public String lastBodyHead() {
        if (lastBody == null) return "";
        return lastBody.length() > 1200 ? lastBody.substring(0, 1200) : lastBody;
    }

    private static byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) {
            bo.write(buf, 0, n);
        }
        try {
            is.close();
        } catch (Throwable t) {
        }
        return bo.toByteArray();
    }

    private String open(String method, String absUrl, String body, int depth) throws Exception {
        return open(method, absUrl, body, depth, true);
    }

    private String open(String method, String absUrl, String body, int depth, boolean ajax) throws Exception {
        lastUrl = absUrl;
        HttpURLConnection c = (HttpURLConnection) new URL(absUrl).openConnection();
        c.setConnectTimeout(12000);
        c.setReadTimeout(25000);
        c.setRequestMethod(method);
        c.setInstanceFollowRedirects(false);
        c.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        c.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        if (ajax) {
            c.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            c.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        } else {
            c.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            c.setRequestProperty("Upgrade-Insecure-Requests", "1");
        }
        c.setRequestProperty("Referer", referer);
        if (jar.size() > 0) {
            c.setRequestProperty("Cookie", cookieStr());
        }
        if (body != null) {
            byte[] data = body.getBytes("UTF-8");
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            c.setFixedLengthStreamingMode(data.length);
            OutputStream os = c.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
        }
        lastCode = c.getResponseCode();
        String loc = c.getHeaderField("Location");
        lastLocation = loc == null ? "" : loc;
        absorbCookies(c);
        InputStream is;
        if (lastCode >= 400) {
            is = c.getErrorStream();
        } else {
            is = c.getInputStream();
        }
        byte[] raw = is == null ? new byte[0] : readAllBytes(is);
        lastRaw = raw;
        lastCharset = charsetOf(c.getContentType());
        String text = decodeText(raw, lastCharset);
        lastBody = text;
        try {
            c.disconnect();
        } catch (Throwable t) {
        }
        if (lastCode >= 301 && lastCode <= 308 && lastLocation.length() > 0 && depth < 3) {
            String next = lastLocation;
            if (!next.startsWith("http")) {
                if (next.startsWith("/")) {
                    next = HOST + next;
                } else {
                    next = HOST + "/" + next;
                }
            }
            return open("GET", next, null, depth + 1, ajax);
        }
        return text;
    }

    private String req(String method, String path, String body) throws Exception {
        return open(method, HOST + path, body, 0, true);
    }

    private String reqPage(String method, String path, String body) throws Exception {
        return open(method, HOST + path, body, 0, false);
    }

    // ---------------------------------------------------------------- 登录

    /** 取验证码图片；返回 null 表示这套系统不需要验证码。 */
    public byte[] loadCaptcha() throws Exception {
        if (jar.size() == 0) {
            try {
                req("GET", "/jwglxt/xtgl/login_slogin.html", null);
            } catch (Throwable t) {
                // ignore
            }
        }
        req("GET", "/jwglxt/kaptcha?time=" + System.currentTimeMillis(), null);
        byte[] d = lastRaw;
        if (d != null && d.length > 100) {
            int b0 = d[0] & 0xFF;
            int b1 = d[1] & 0xFF;
            if ((b0 == 0xFF && b1 == 0xD8) || (b0 == 0x89 && b1 == 0x50)) {
                return d;
            }
        }
        return null;
    }

    /** 把网页登录拿到的 Cookie 头（k=v; k=v）塞进会话，跳过账号密码登录。 */
    public void setCookieHeader(String header) {
        if (header == null || header.length() == 0) return;
        String[] parts = header.split(";");
        for (int i = 0; i < parts.length; i++) {
            String kv = parts[i].trim();
            int p = kv.indexOf('=');
            if (p <= 0) continue;
            jar.put(kv.substring(0, p).trim(), kv.substring(p + 1).trim());
        }
    }

    /** 探一下当前会话是否已经登录（用网页登录的 Cookie 时先调它）。 */
    public boolean checkSession() throws Exception {
        referer = HOST + "/jwglxt/xtgl/index_initMenu.html";
        String html = reqPage("GET", "/jwglxt/xtgl/index_initMenu.html?jsdm=xs&_t="
                + System.currentTimeMillis() + "&echarts=1", null);
        String r = html == null ? "" : html;
        homeTitle = titleOf(r);
        if (isPortal(r)) {
            throw new Exception("当前网络还没通过校园网上网认证，请先在浏览器完成上网认证");
        }
        if (r.length() == 0) return false;
        return !isLoginPage(r);
    }

    public void login(String user, String pass) throws Exception {
        login(user, pass, "");
    }

    /**
     * 登录正方教务系统。失败抛异常，异常 message 是给用户看的中文说明。
     * captcha 为空表示不填验证码。
     */
    public void login(String user, String pass, String captcha) throws Exception {
        String[] tries = passwordVariants(pass);
        boolean hasCap = captcha != null && captcha.length() > 0;
        String last = null;
        int n = 0;
        for (int i = 0; i < tries.length && n < 3; i++) {
            String p = tries[i];
            String err = attempt(user, p, captcha, true);
            n++;
            if (err == null) {
                lastLoginMode = (i == 0 ? "rsa" : "rsa-v" + i);
                return;
            }
            if (err.indexOf("上网") >= 0 || err.indexOf("网络") >= 0 || err.indexOf("连接") >= 0) {
                lastLoginMode = "rsa-net";
                throw new Exception(err);
            }
            last = err;
            if (hasCap || n >= 3) break;
            String err2 = attempt(user, p, captcha, false);
            n++;
            if (err2 == null) {
                lastLoginMode = (i == 0 ? "rsa-then-plain" : "rsa-then-plain-v" + i);
                return;
            }
            last = err2.indexOf("验证码") >= 0 ? err : err2;
        }
        lastLoginMode = "tried" + n;
        throw new Exception(last == null ? "登录失败" : last);
    }

    /** 输入法常见的「看着一样其实不一样」：全角字符、首尾空格。 */
    private static String[] passwordVariants(String pass) {
        String norm = normalizePass(pass);
        if (norm.length() == 0) return new String[]{pass};
        if (!norm.equals(pass)) return new String[]{norm, pass};
        return new String[]{pass};
    }

    private static String normalizePass(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u3000') c = ' ';
            if (c >= '\uFF01' && c <= '\uFF5E') c = (char) (c - 0xFEE0);
            sb.append(c);
        }
        return sb.toString().trim();
    }

    private String attempt(String user, String pass, String captcha, boolean useRsa) throws Exception {
        referer = HOST + "/jwglxt/xtgl/login_slogin.html";
        String html = reqPage("GET", "/jwglxt/xtgl/login_slogin.html", null);
        if (html == null) html = "";

        String csrf = match(html, "name=\"csrftoken\"[^>]*value=\"([^\"]*)\"");
        if (csrf == null || csrf.length() == 0) {
            csrf = match(html, "id=\"csrftoken\"[^>]*value=\"([^\"]*)\"");
        }
        if (csrf == null) csrf = "";
        csrfToken = csrf;

        lastFormInfo = formInfo(html);
        lastPageHints = pageHints(html);
        String pwd = pass;
        if (useRsa) {
            String mod = "";
            String exp = "";
            String keyRaw = null;
            try {
                keyRaw = req("GET",
                        "/jwglxt/xtgl/login_getPublicKey.html?time=" + System.currentTimeMillis(), null);
                JSONObject k = new JSONObject(keyRaw == null ? "{}" : keyRaw.trim());
                mod = k.optString("modulus", "");
                exp = k.optString("exponent", "");
            } catch (Throwable t) {
                // 没有公钥接口 / 不是 JSON，下面用兜底正则
            }
            if (exp.length() == 0) exp = "10001";
            if (mod.length() == 0 && keyRaw != null
                    && keyRaw.matches("(?s).*[0-9a-fA-F]{" + 128 + ",}.*")) {
                String guess = pickLongestHex(keyRaw);
                if (guess.length() > mod.length()) mod = guess;
            }
            lastKeyInfo = "modLen=" + mod.length() + " exp=" + exp;
            if (mod.length() > 0) {
                try {
                    pwd = rsa(pass, mod, exp);
                } catch (Throwable t) {
                    // 公钥有问题就按明文提交
                    pwd = pass;
                    lastKeyInfo = lastKeyInfo + " rsaFail=" + t.getMessage();
                }
            } else {
                lastKeyInfo = lastKeyInfo + " noKey";
            }
        }

        lastPwInfo = "pwLen=" + pass.length() + " ascii=" + isAscii(pass)
                + " rsa=" + (pwd != pass) + " mmLen=" + pwd.length();
        String body = "csrftoken=" + enc(csrf)
                + "&language=zh_CN"
                + "&yhm=" + enc(user)
                + "&mm=" + enc(pwd);
        if (captcha != null && captcha.length() > 0) {
            body = body + "&kaptcha=" + enc(captcha) + "&yzm=" + enc(captcha);
        }
        String resp = reqPage("POST", "/jwglxt/xtgl/login_slogin.html", body);
        String r = resp == null ? "" : resp;
        homeTitle = titleOf(r);

        if (isPortal(r)) {
            return "当前网络还没通过校园网上网认证（返回的是「上网登录页」），"
                    + "请先在手机浏览器完成上网认证再试";
        }
        if (isLoginPage(r)) {
            // 登录页正文里到处是「密码错误」这类 JS 文案，只有 #tips / alert 里的才是真提示
            String tip = errorText(r);
            if (tip.length() > 0) {
                lastKeyInfo = lastKeyInfo + " tip=" + tip;
                if (tip.indexOf("验证码") >= 0) {
                    return "验证码不正确，请重新输入";
                }
                return tip;
            }
            String known = knownTip(r);
            if (known.length() > 0) {
                lastKeyInfo = lastKeyInfo + " tip=" + known;
                return known;
            }
            String snip = plainOf(r);
            if (snip.length() > 40) snip = snip.substring(0, 40);
            return "登录未成功：服务器仍返回登录页"
                    + (snip.length() > 0 ? "（" + snip + "）" : "")
                    + "，账号密码或验证码不对";
        }
        if (r.indexOf("用户名或密码错误") >= 0 || r.indexOf("用户不存在") >= 0) {
            return "账号或密码不正确";
        }
        String err = errorText(r);
        if (err.length() > 0) {
            return err;
        }
        return null;
    }

    /** 登录页里常见的固定提示语，直接认。 */
    private static boolean isAscii(String s) {
        if (s == null) return true;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return false;
        }
        return true;
    }

    private static String knownTip(String html) {
        if (html == null) return "";
        String[] keys = {"用户名或密码不正确", "用户名或密码错误", "密码不正确", "密码错误",
                "验证码输入错误", "验证码错误", "用户不存在", "账号被锁定", "密码错误次数过多"};
        for (int i = 0; i < keys.length; i++) {
            if (html.indexOf(keys[i]) >= 0) return keys[i];
        }
        return "";
    }

    /** 登录页里所有 input 的 name，一次看清要提交哪些字段。 */
    private static String formInfo(String html) {
        if (html == null) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("name=\"([^\"]{1,30})\"").matcher(html);
        StringBuilder sb = new StringBuilder();
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<String>();
        while (m.find()) set.add(m.group(1));
        java.util.Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(it.next());
        }
        return sb.toString();
    }

    /** 页面里与密码加密有关的线索（rsa / encrypt / 公钥接口地址）。 */
    private static String pageHints(String html) {
        if (html == null) return "";
        StringBuilder sb = new StringBuilder();
        if (html.indexOf("login_rsa_encrypt") >= 0) sb.append("rsa_encrypt,");
        if (html.indexOf("login_getPublicKey") >= 0) sb.append("getPublicKey,");
        if (html.indexOf("encrypt") >= 0) sb.append("encrypt,");
        if (html.indexOf("kaptcha") >= 0) sb.append("kaptcha,");
        if (html.indexOf("yzm") >= 0) sb.append("yzm,");
        return sb.toString();
    }

    /** 判断返回的 HTML 是不是登录页（教务首页不会带登录表单）。 */
    public static boolean isLoginPage(String html) {
        if (html == null || html.length() == 0) return false;
        return html.indexOf("login_slogin") >= 0 || html.indexOf("id=\"yhm\"") >= 0
                || html.indexOf("name=\"yhm\"") >= 0
                || html.indexOf("统一身份认证") >= 0 || html.indexOf("authserver") >= 0;
    }

    /** 从登录失败页里抠出服务器给的中文提示。 */
    private static String errorText(String html) {
        if (html == null || html.length() == 0) return "";
        String s = match(html, "(?s)<div[^>]*class=\"[^\"]*(?:alert|error)[^\"]*\"[^>]*>(.*?)</div>");
        if (s == null) s = match(html, "(?s)id=\"tips\"[^>]*>(.*?)</div>");
        if (s == null) s = match(html, "(?s)id=\"tips\"[^>]*>(.*?)</span>");
        if (s == null) s = match(html, "(?s)<font[^>]*color=[\"']?red[^>]*>(.*?)</font>");
        if (s == null) s = match(html, "(?s)class=\"[^\"]*(?:wrong|errmsg|errMsg)[^\"]*\"[^>]*>(.*?)</div>");
        if (s == null) return "";
        String t = s.replaceAll("<[^>]+>", " ").replace("&nbsp;", " ");
        t = t.replace("&amp;", "&").trim().replaceAll("\\s+", " ");
        if (t.length() > 120) t = t.substring(0, 120);
        return t;
    }

    /**
     * 正方教务前端用的是 jsbn 的 RSA，实现是 PKCS#1 v1.5 填充：
     * 00 || 02 || 随机非零字节 || 00 || 明文，再对整块做 modPow。
     * 直接对明文做 modPow 服务器解出来是乱码，只会回「密码错误」。
     */
    private static String rsa(String plain, String modulusStr, String exponentStr) throws Exception {
        BigInteger mod = decodeKey(modulusStr);
        BigInteger exp = decodeKey(exponentStr);
        if (mod.bitLength() < 512 || exp.bitLength() == 0) {
            throw new Exception("公钥格式不对");
        }
        int k = (mod.bitLength() + 7) / 8;
        byte[] msg = plain.getBytes("UTF-8");
        if (msg.length > k - 11) {
            throw new Exception("密码太长");
        }
        byte[] em = new byte[k];
        em[0] = 0x00;
        em[1] = 0x02;
        int psLen = k - 3 - msg.length;
        SecureRandom rnd = new SecureRandom();
        for (int i = 0; i < psLen; i++) {
            int b;
            do {
                b = rnd.nextInt(256);
            } while (b == 0);
            em[2 + i] = (byte) b;
        }
        em[2 + psLen] = 0x00;
        System.arraycopy(msg, 0, em, 3 + psLen, msg.length);
        BigInteger cipher = new BigInteger(1, em).modPow(exp, mod);
        String hex = cipher.toString(16);
        // 补足到模长长度：有的服务端按固定长度做 hex 解码
        int want = k * 2;
        if (hex.length() < want) {
            StringBuilder sb = new StringBuilder();
            for (int i = hex.length(); i < want; i++) sb.append('0');
            sb.append(hex);
            hex = sb.toString();
        }
        return hex;
    }

    /**
     * 正方返回的 modulus/exponent 有的是十六进制，有的是 Base64
     * （例如 exponent 常见值 AQAB 就是 Base64 的 65537）。
     */
    private static BigInteger decodeKey(String s) throws Exception {
        String t = s == null ? "" : s.trim().replaceAll("\\s+", "");
        if (t.length() == 0) throw new Exception("空公钥");
        if (t.matches("[0-9a-fA-F]+") && t.length() >= 64 && (t.length() & 1) == 0) {
            return new BigInteger(t, 16);
        }
        try {
            byte[] raw = android.util.Base64.decode(t, android.util.Base64.DEFAULT);
            return new BigInteger(1, raw);
        } catch (Throwable ignored) {
            // 再试 URL 安全变体
        }
        byte[] raw = android.util.Base64.decode(t,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
        return new BigInteger(1, raw);
    }

    /** 公钥接口返回的格式各版本不一样，兜底从整段响应里挑最长的一段十六进制。 */
    private static String pickLongestHex(String text) {
        if (text == null || text.length() == 0) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("[0-9a-fA-F]{64,}").matcher(text);
        String best = "";
        while (m.find()) {
            String g = m.group();
            if (g.length() > best.length()) best = g;
        }
        return best;
    }

    private static String cleanHex(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String match(String src, String regex) {
        try {
            Matcher m = Pattern.compile(regex).matcher(src);
            if (m.find()) return m.group(1);
        } catch (Throwable t) {
        }
        return null;
    }

    // ---------------------------------------------------------------- 取课表

    /** 当前学年 / 学期（正方 xqm：3=第一学期，12=第二学期）。 */
    public static String curXnm() {
        LocalDate d = LocalDate.now();
        int m = d.getMonthValue();
        int y = d.getYear();
        if (m >= 9) return String.valueOf(y);
        if (m <= 1) return String.valueOf(y - 1);
        return String.valueOf(y - 1);
    }

    public static String curXqm() {
        int m = LocalDate.now().getMonthValue();
        if (m >= 9 || m <= 1) return "3";
        return "12";
    }

    /**
     * 拉取课表：只用「班级课表」。
     */
    public Result fetch(String xnm, String xqm) throws Exception {
        Result r = null;
        String clsErr = null;
        try {
            r = fetchClass(xnm, xqm);
        } catch (Throwable t) {
            clsErr = t.getMessage();
        }
        if (r == null || r.items.isEmpty()) {
            // 班级课表没数据时，退到学生个人课表再试一次
            try {
                Result s = fetchStudent(xnm, xqm);
                if (s != null && !s.items.isEmpty()) {
                    return s;
                }
            } catch (Throwable ignored) {
                // 个人课表也不可用，用班级课表的结果报错
            }
        }
        if (r == null) {
            throw new Exception(clsErr == null || clsErr.length() == 0 ? "取课表失败" : clsErr);
        }
        if (r.items.isEmpty()) {
            throw new Exception("没有查到课程（" + r.source + "），可能本学期尚未选课");
        }
        return r;
    }

    private Result fetchStudent(String xnm, String xqm) throws Exception {
        String body = "xnm=" + enc(xnm) + "&xqm=" + enc(xqm) + csrfPart();
        String resp = req("POST", "/jwglxt/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151", body);
        Result r = parse(resp, "学生个人课表");
        r.xnm = xnm;
        r.xqm = xqm;
        return r;
    }

    /** 班级课表的 POST body，网页里的 JS 也用同一份。 */
    public String classBody(String xnm, String xqm) {
        return "xnm=" + enc(xnm)
                + "&xqm=" + enc(xqm)
                + "&xnmc=" + enc(xnm + "-" + (Integer.parseInt(xnm) + 1))
                + "&xqmmc=" + enc("3".equals(xqm) ? "1" : "2")
                + "&xqh_id=" + enc(DEF_XQH_ID)
                + "&njdm_id=" + enc(DEF_NJDM_ID)
                + "&zyh_id=" + enc(DEF_ZYH_ID)
                + "&bh_id=" + enc(DEF_BH_ID)
                + "&tjkbzdm=1&tjkbzxsdm=0"
                + "&zymc=" + enc(DEF_ZYMC)
                + "&jgmc=" + enc(DEF_JGMC)
                + "&njmc=" + enc(DEF_NJDM_ID)
                + "&bj=" + enc(DEF_BJ)
                + "&xkrs=" + enc(DEF_XKRS)
                + "&jsxm=&lxdh=&bh=" + enc(DEF_BH_ID)
                + "&zs=&zxszjjs=false&akcxqjchb=false"
                + "&xsdm=&kclxdm=&kclbdm=&kbsjlyqz=&yf="
                + "&kzlx=ck&sfcxxqh=1" + csrfPart();
    }

    /** 把服务器返回的课表数据直接交给解析器（网页里 JS 取回来的走这里）。 */
    public Result parseRaw(String resp, String xnm, String xqm) throws Exception {
        Result r = parse(resp, "班级课表");
        r.xnm = xnm;
        r.xqm = xqm;
        return r;
    }

    private Result fetchClass(String xnm, String xqm) throws Exception {
        referer = HOST + "/jwglxt/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=N214505&layout=default";
        String body = classBody(xnm, xqm);
        String resp = req("POST", "/jwglxt/kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505", body);
        Result r = parse(resp, "班级课表");
        r.xnm = xnm;
        r.xqm = xqm;
        return r;
    }

    // ---------------------------------------------------------------- 解析

    private String csrfPart() {
        if (csrfToken == null || csrfToken.length() == 0) return "";
        return "&csrftoken=" + enc(csrfToken);
    }

    /** 校园网网关的上网认证拦截页。 */
    public static boolean isPortal(String html) {
        if (html == null) return false;
        return html.indexOf("上网登录页") >= 0 || html.indexOf("上网认证") >= 0
                || html.indexOf("网络接入认证") >= 0;
    }

    /** 去掉标签后的正文摘要，方便把服务器的真实提示显示给用户。 */
    public static String plainOf(String html) {
        if (html == null || html.length() == 0) return "";
        String t = html.replaceAll("(?s)<script.*?</script>", " ");
        t = t.replaceAll("(?s)<style.*?</style>", " ");
        t = t.replaceAll("<[^>]+>", " ");
        t = t.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
        t = t.trim().replaceAll("\\s+", " ");
        return t.length() > 150 ? t.substring(0, 150) : t;
    }

    /** 从返回的 HTML 里抠出 <title>，方便把真实原因显示给用户。 */
    public static String titleOf(String html) {
        String t = match(html == null ? "" : html, "(?s)<title[^>]*>(.*?)</title>");
        if (t == null || t.length() == 0) return "";
        t = t.replaceAll("<[^>]+>", " ").trim().replaceAll("\\s+", " ");
        return t.length() > 40 ? t.substring(0, 40) : t;
    }

    private Result parse(String resp, String source) throws Exception {
        String t = resp == null ? "" : resp.trim();
        Result r = new Result();
        r.source = source;
        r.rawHead = t.length() > 400 ? t.substring(0, 400) : t;
        if (t.length() == 0) {
            throw new Exception(source + "：服务器返回空内容（可能是登录已失效）");
        }
        if (isPortal(t)) {
            throw new Exception("当前网络还没通过校园网上网认证（返回的是「上网登录页」），"
                    + "请先在手机浏览器完成上网认证再试");
        }
        if (t.charAt(0) == '<') {
            String ti = titleOf(t);
            String pl = plainOf(t);
            StringBuilder sb = new StringBuilder();
            sb.append(source).append("：返回的是网页（status=").append(lastCode);
            if (ti.length() > 0) sb.append("，标题：").append(ti);
            if (pl.length() > 0) sb.append("，内容：").append(pl);
            sb.append("）");
            throw new Exception(sb.toString());
        }
        JSONObject root;
        try {
            root = new JSONObject(t);
        } catch (Exception e) {
            JSONArray arr;
            try {
                arr = new JSONArray(t);
            } catch (Exception e2) {
                throw new Exception(source + "：返回内容无法解析：" + r.rawHead);
            }
            root = new JSONObject();
            root.put("kbList", arr);
        }
        fill(root, r);
        return r;
    }

    private void fill(JSONObject root, Result r) {
        JSONArray kb = root.optJSONArray("kbList");
        if (kb == null) kb = root.optJSONArray("kbListNew");
        if (kb != null) {
            for (int i = 0; i < kb.length(); i++) {
                JSONObject o = kb.optJSONObject(i);
                if (o == null) continue;
                KbItem it = new KbItem();
                it.name = o.optString("kcmc", "未命名课程").trim();
                it.teacher = o.optString("xm", "").trim();
                it.room = o.optString("cdmc", "").trim();
                it.exam = o.optString("khfsmc", "").trim();
                it.weekDay = toInt(o.optString("xqj", "1"), 1);
                int[] se = sections(o.optString("jc", ""), o.optString("xsdj", ""));
                it.start = se[0];
                it.end = se[1];
                it.weeks = weeks(o.optString("zcds", ""), o.optString("zcd", ""));
                if (it.name.length() > 0) r.items.add(it);
            }
        }
        JSONArray wn = root.optJSONArray("weekNum");
        if (wn != null) {
            for (int i = 0; i < wn.length(); i++) {
                JSONObject o = wn.optJSONObject(i);
                if (o == null) continue;
                String rq = o.optString("rq", "");
                WeekRange wr = rangeOf(rq, i + 1);
                if (wr != null) r.weeks.add(wr);
            }
        }
        JSONObject xs = root.optJSONObject("xsxx");
        if (xs != null) {
            String xh = xs.optString("XH", xs.optString("xh", ""));
            String xm = xs.optString("XM", xs.optString("xm", ""));
            String zymc = xs.optString("ZYMC", xs.optString("zymc", ""));
            StringBuilder sb = new StringBuilder();
            if (xh.length() > 0) sb.append(xh);
            if (xm.length() > 0) sb.append(' ').append(xm);
            if (zymc.length() > 0) sb.append(' ').append(zymc);
            r.student = sb.toString().trim();
        }
    }

    private static WeekRange rangeOf(String rq, int week) {
        if (rq == null) return null;
        Matcher m = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})").matcher(rq);
        LocalDate first = null;
        LocalDate second = null;
        while (m.find()) {
            if (first == null) {
                first = looseDate(m.group(1));
            } else if (second == null) {
                second = looseDate(m.group(1));
            }
        }
        if (first == null) return null;
        WeekRange w = new WeekRange();
        w.week = week;
        w.start = first;
        w.end = second == null ? first.plusDays(6) : second;
        return w;
    }

    private static LocalDate looseDate(String s) {
        String[] p = s.split("-");
        try {
            return LocalDate.of(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) {
            return null;
        }
    }

    private static int toInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static int[] sections(String jc, String xsdj) {
        int[] r = new int[]{1, 1};
        String src = jc != null && jc.length() > 0 ? jc : xsdj;
        if (src == null) return r;
        Matcher m = Pattern.compile("(\\d+)\\s*(?:-\\s*(\\d+))?").matcher(src);
        if (m.find()) {
            r[0] = toInt(m.group(1), 1);
            if (m.group(2) != null) {
                r[1] = toInt(m.group(2), r[0]);
            } else {
                r[1] = r[0];
            }
        }
        if (r[1] < r[0]) r[1] = r[0];
        return r;
    }

    private static int[] weeks(String zcds, String zcd) {
        ArrayList<Integer> out = new ArrayList<Integer>();
        if (zcds != null && zcds.trim().length() > 0) {
            String[] ps = zcds.split(",");
            for (int i = 0; i < ps.length; i++) {
                int v = toInt(ps[i], -1);
                if (v > 0) out.add(Integer.valueOf(v));
            }
        }
        if (out.isEmpty() && zcd != null && zcd.length() > 0) {
            String s = zcd.replace("周", ",").replace("（", "(").replace("）", ")");
            String[] segs = s.split(",");
            for (int i = 0; i < segs.length; i++) {
                String seg = segs[i].trim();
                if (seg.length() == 0) continue;
                boolean odd = seg.indexOf("单") >= 0;
                boolean even = seg.indexOf("双") >= 0;
                Matcher m = Pattern.compile("(\\d+)(?:-(\\d+))?").matcher(seg);
                if (!m.find()) continue;
                int a = toInt(m.group(1), 0);
                int b = m.group(2) == null ? a : toInt(m.group(2), a);
                for (int w = a; w <= b && w > 0 && w <= 30; w++) {
                    if (odd && w % 2 == 0) continue;
                    if (even && w % 2 == 1) continue;
                    out.add(Integer.valueOf(w));
                }
            }
        }
        int[] r = new int[out.size()];
        for (int i = 0; i < r.length; i++) r[i] = out.get(i).intValue();
        return r;
    }

    public String debugInfo() {
        return "mode=" + lastLoginMode + " " + lastKeyInfo
                + " form=[" + lastFormInfo + "] hints=[" + lastPageHints + "] "
                + lastPwInfo
                + " lastCode=" + lastCode + " url=" + lastUrl + " loc=" + lastLocation
                + " home=" + homeTitle + " cookie=" + cookieStr();
    }
}