package com.zuwe.smartcourselite;

import android.app.Activity;
import android.view.View;

/** 账户凭证区里「教务系统账号密码登录」按钮的点击入口。 */
public final class JwClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        if (v == null) return;
        Activity a = null;
        if (v.getContext() instanceof Activity) {
            a = (Activity) v.getContext();
        }
        if (a == null) return;
        try {
            JwUi.show(a);
        } catch (Throwable t) {
            JwLog.w(a, "ui", "open panel failed: " + t);
        }
    }
}