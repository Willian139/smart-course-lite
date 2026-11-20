package com.zuwe.smartcourselite;

/** 后台取验证码图片；系统不需要验证码时返回 null。 */
public final class JwCaptchaTask extends Thread {

    private final JwUi ui;
    private final JwApi api;

    public JwCaptchaTask(JwUi ui, JwApi api) {
        this.ui = ui;
        this.api = api;
        setName("jw-captcha");
        setPriority(MIN_PRIORITY);
    }

    @Override
    public void run() {
        byte[] img = null;
        String err = null;
        try {
            img = api.loadCaptcha();
        } catch (Throwable t) {
            err = t.getMessage();
            if (err == null || err.length() == 0) err = t.toString();
            JwLog.w(ui.activity(), "captcha", err);
        }
        ui.postCaptcha(img, err);
    }
}
