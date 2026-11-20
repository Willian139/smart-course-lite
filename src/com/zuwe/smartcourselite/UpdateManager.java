package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class UpdateManager {
    private static volatile int ACCENT = -10522798;
    public static final String APK_NAME = "smart-course-lite-v%s.apk";
    private static final long CHECK_INTERVAL_MS = 1800000;
    private static final String KEY_AUTO_INSTALL = "auto_install_code";
    private static final String KEY_DEFER = "defer_code";
    private static final String KEY_DOWNLOADED = "dl_code";
    private static final String KEY_LAST_CHECK = "last_check";
    private static final String KEY_LAST_REMOTE = "last_remote";
    private static final String KEY_SEEN_VERSION = "seen_version";
    private static final String KEY_SHA = "dl_sha_";
    private static final String KEY_SKIP = "skip_code";
    private static final String LATEST_URL = "https://api.github.com/repos/Willian139/smart-course-lite/releases/latest";
    private static final Object LOCK = new Object();
    private static final String PREFS = "smartcourse_update";
    public static final String REPO = "Willian139/smart-course-lite";
    private static final String TAG_URL = "https://api.github.com/repos/Willian139/smart-course-lite/releases/tags/v";
    private static volatile boolean cancelRequested = false;
    private static volatile boolean checking = false;
    private static AlertDialog currentDialog = null;
    private static volatile long lastPromptAt = 0;
    private static volatile String pendingUrl = null;
    private static ProgressBar progressBarView = null;
    private static AlertDialog progressDialog = null;
    private static TextView progressTextView = null;
    private static volatile String statusText = "";
    private static WeakReference<TextView> statusViewRef;

    interface ProgressListener {
        void onProgress(int i, long j, long j2);
    }

    static void publishStatus(String str) {
        if (str == null) {
            str = "";
        }
        statusText = str;
        WeakReference<TextView> weakReference = statusViewRef;
        final TextView textView = weakReference == null ? null : weakReference.get();
        if (textView == null) {
            return;
        }
        final String str2 = statusText;
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.1
            @Override // java.lang.Runnable
            public void run() {
                textView.setText(str2);
            }
        });
    }

    public static String statusText() {
        return statusText;
    }

    private UpdateManager() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, 0);
    }

    public static int currentCode(Context context) {
        return codeOf(currentName(context));
    }

    public static int rawVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception unused) {
            return 0;
        }
    }

    public static String currentName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception unused) {
            return "未知";
        }
    }

    public static File dir(Context context) {
        File externalFilesDir = context.getExternalFilesDir("update");
        if (externalFilesDir == null) {
            externalFilesDir = new File(context.getFilesDir(), "update");
        }
        if (!externalFilesDir.exists()) {
            externalFilesDir.mkdirs();
        }
        return externalFilesDir;
    }

    public static File apkFile(Context context, int i) {
        return new File(dir(context), String.format(APK_NAME, versionNameOf(i)));
    }

    public static File apkFile(Context context, String str) {
        return new File(dir(context), String.format(APK_NAME, str));
    }

    public static String versionNameOf(int i) {
        return (i / 10000) + "." + ((i / 100) % 100) + "." + (i % 100);
    }

    public static String cleanTag(String str) {
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        return (trim.startsWith("v") || trim.startsWith("V")) ? trim.substring(1) : trim;
    }

    public static int codeOf(String str) {
        String[] split = cleanTag(str).split("\\.");
        return (pick(split, 0) * 10000) + (pick(split, 1) * 100) + Math.min(pick(split, 2), 99);
    }

    private static int pick(String[] strArr, int i) {
        char charAt;
        if (strArr.length <= i) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        String str = strArr[i];
        for (int i2 = 0; i2 < str.length() && (charAt = str.charAt(i2)) >= '0' && charAt <= '9'; i2++) {
            sb.append(charAt);
        }
        if (sb.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (Exception unused) {
            return 0;
        }
    }

    public static boolean hasDownloaded(Context context) {
        int i = prefs(context).getInt(KEY_DOWNLOADED, 0);
        if (i <= currentCode(context)) {
            return false;
        }
        File apkFile = apkFile(context, i);
        return apkFile.isFile() && apkFile.length() > 204800;
    }

    public static int downloadedCode(Context context) {
        return prefs(context).getInt(KEY_DOWNLOADED, 0);
    }

    public static void checkOnLaunch(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (hasDownloaded(activity)) {
            final int downloadedCode = downloadedCode(activity);
            activity.getWindow().getDecorView().post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.2
                @Override // java.lang.Runnable
                public void run() {
                    UpdateManager.showInstallPrompt(activity, downloadedCode);
                }
            });
        } else {
            if (System.currentTimeMillis() - lastPromptAt < 10000) {
                return;
            }
            check(activity, false, null);
        }
    }

    public static void checkManually(Activity activity, Runnable runnable) {
        check(activity, true, runnable);
    }

    private static void check(final Activity activity, final boolean z, final Runnable runnable) {
        prefs(activity);
        if (checking) {
            publishStatus("正在检查更新…");
            if (z) {
                toast(activity, "正在检查更新…");
                return;
            }
            return;
        }
        checking = true;
        final Context applicationContext = activity.getApplicationContext();
        if (z) {
            toast(activity, "正在检查更新…");
        }
        Thread thread = new Thread(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.3
            @Override // java.lang.Runnable
            public void run() {
                JSONObject fetchJson = null;
                try {
                    try {
                        fetchJson = UpdateManager.fetchJson(UpdateManager.LATEST_URL);
                        if (fetchJson == null) {
                            fetchJson = UpdateManager.fallbackLatest();
                        }
                    } finally {
                        boolean unused = UpdateManager.checking = false;
                    }
                } catch (Exception e) {
                    UpdateManager.publishStatus("检查失败：" + e.getClass().getSimpleName());
                    if (z) {
                        UpdateManager.postToast(activity, "检查更新失败：" + e.getClass().getSimpleName());
                    }
                }
                if (fetchJson == null) {
                    UpdateManager.publishStatus("检查失败：网络不可用");
                    if (z) {
                        UpdateManager.postToast(activity, "检查更新失败，网络不可用");
                    }
                    return;
                }
                final String cleanTag = UpdateManager.cleanTag(fetchJson.optString("tag_name", ""));
                final int max = Math.max(UpdateManager.codeOf(cleanTag), fetchJson.optInt("version_code", 0));
                final String optString = fetchJson.optString("body", "");
                String assetUrl = UpdateManager.assetUrl(fetchJson);
                UpdateManager.prefs(applicationContext).edit().putLong(UpdateManager.KEY_LAST_CHECK, System.currentTimeMillis()).putInt(UpdateManager.KEY_LAST_REMOTE, max).apply();
                int currentCode = UpdateManager.currentCode(applicationContext);
                if (max > currentCode) {
                    try {
                        if (!cleanTag.isEmpty()) {
                            UpdateManager.resetStaleChoice(applicationContext, currentCode);
                            int i = UpdateManager.prefs(applicationContext).getInt(UpdateManager.KEY_SKIP, 0);
                            if (!z && i >= max) {
                                UpdateManager.publishStatus("发现新版本 v" + cleanTag + "（已设定不再提示）");
                                return;
                            }
                            String unused2 = UpdateManager.pendingUrl = assetUrl;
                            UpdateManager.publishStatus("发现新版本 v" + cleanTag);
                            final Activity activity2 = activity;
                            UpdateManager.post(activity2, new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.3.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    UpdateManager.showUpdateDialog(activity2, max, cleanTag, optString);
                                }
                            });
                            return;
                        }
                    } catch (Exception e2) {
                        UpdateManager.publishStatus("检查失败：" + e2.getClass().getSimpleName());
                        if (z) {
                            UpdateManager.postToast(activity, "检查更新失败：" + e2.getClass().getSimpleName());
                        }
                    }
                }
                UpdateManager.publishStatus("已是最新 v" + UpdateManager.currentName(applicationContext));
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    UpdateManager.post(activity, runnable2);
                }
                if (z) {
                    final Activity activity3 = activity;
                    final Context context = applicationContext;
                    UpdateManager.post(activity3, new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.3.2
                        @Override // java.lang.Runnable
                        public void run() {
                            UpdateManager.toast(activity3, "已是最新版本 v" + UpdateManager.currentName(context));
                        }
                    });
                }
            }
        }, "update-check");
        thread.setDaemon(true);
        thread.start();
    }

    public static JSONObject fallbackLatest() {
        try {
            JSONArray jSONArray = new JSONArray(fetchText("https://api.github.com/repos/Willian139/smart-course-lite/releases?per_page=1"));
            if (jSONArray.length() > 0) {
                return jSONArray.optJSONObject(0);
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    public static String assetUrl(JSONObject jSONObject) {
        JSONArray optJSONArray = jSONObject.optJSONArray("assets");
        String str = null;
        if (optJSONArray == null) {
            return null;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                String optString = optJSONObject.optString("name", "");
                String optString2 = optJSONObject.optString("browser_download_url", "");
                if (!optString2.isEmpty() && optString.endsWith(".apk")) {
                    if (optString.equals(String.format(APK_NAME, cleanTag(jSONObject.optString("tag_name", ""))))) {
                        return optString2;
                    }
                    if (str == null) {
                        str = optString2;
                    }
                }
            }
        }
        return str;
    }

    static void showUpdateDialog(final Activity activity, final int i, final String str, final String str2) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        dismissDialog();
        String trim = (str2 == null || str2.trim().isEmpty()) ? "本次更新没有填写说明。" : str2.trim();
        final String[] strArr = {""};
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        int dp = dp(activity, 22.0f);
        linearLayout.setPadding(dp, dp(activity, 18.0f), dp, dp(activity, 2.0f));
        TextView textView = new TextView(activity);
        textView.setText("发现新版本 v" + str);
        textView.setTextSize(15.0f);
        textView.setTypeface(Typeface.DEFAULT, 1);
        textView.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText("当前 v" + currentName(activity) + "   →   最新 v" + str);
        textView2.setTextSize(12.0f);
        textView2.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        textView2.setPadding(0, dp(activity, 8.0f), 0, dp(activity, 16.0f));
        linearLayout.addView(textView2);
        TextView textView3 = new TextView(activity);
        textView3.setText("更新内容");
        textView3.setTextSize(13.0f);
        textView3.setTypeface(Typeface.DEFAULT, 1);
        textView3.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        linearLayout.addView(textView3);
        final TextView textView4 = new TextView(activity);
        textView4.setText(trim);
        textView4.setTextSize(13.0f);
        textView4.setLineSpacing(dp(activity, 6.0f), 1.0f);
        textView4.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        textView4.setPadding(0, dp(activity, 8.0f), 0, 0);
        final ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(textView4, new FrameLayout.LayoutParams(-1, -2));
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, -2));
        scrollView.post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.4
            @Override // java.lang.Runnable
            public void run() {
                int dp2 = UpdateManager.dp(activity, 280.0f);
                if (textView4.getHeight() > dp2) {
                    ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
                    layoutParams.height = dp2;
                    scrollView.setLayoutParams(layoutParams);
                }
            }
        });
        TextView textView5 = new TextView(activity);
        textView5.setText("更新为可选操作，不更新也能继续使用。");
        textView5.setTextSize(11.0f);
        textView5.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        textView5.setPadding(0, dp(activity, 14.0f), 0, 0);
        linearLayout.addView(textView5);
        LinearLayout dialogActions = dialogActions(activity);
        dialogActions.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 8.0f));
        TextView dialogAction = dialogAction(activity, "不再提示", themeColor(activity, android.R.attr.textColorSecondary), false, null);
        TextView dialogAction2 = dialogAction(activity, "稍后", themeColor(activity, android.R.attr.textColorSecondary), false, null);
        TextView dialogAction3 = dialogAction(activity, "立即更新", ACCENT, true, null);
        dialogActions.addView(dialogAction, actionLp(activity, true));
        dialogActions.addView(dialogAction2, actionLp(activity, false));
        dialogActions.addView(dialogAction3, actionLp(activity, false));
        linearLayout.addView(dialogActions);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(linearLayout);
        final AlertDialog create = builder.create();
        dialogAction.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                strArr[0] = "never";
                create.dismiss();
            }
        });
        dialogAction2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                strArr[0] = "later";
                create.dismiss();
            }
        });
        dialogAction3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                strArr[0] = "now";
                create.dismiss();
            }
        });
        create.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.zuwe.smartcourselite.UpdateManager.8
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                if ("now".equals(strArr[0])) {
                    UpdateManager.prefs(activity).edit().remove(UpdateManager.KEY_DEFER).apply();
                    if (UpdateManager.pendingUrl == null || UpdateManager.pendingUrl.length() == 0) {
                        UpdateManager.toast(activity, "下载地址无效，请稍后在「关于此项目」里重试");
                        return;
                    }
                    final Activity activity2 = activity;
                    final int i2 = i;
                    final String str3 = str;
                    final String str4 = UpdateManager.pendingUrl;
                    final String str5 = str2;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.8.1
                        @Override // java.lang.Runnable
                        public void run() {
                            UpdateManager.showDownloadProgress(activity2, i2, str3, str4, str5);
                        }
                    }, 150L);
                    return;
                }
                if ("later".equals(strArr[0])) {
                    UpdateManager.toast(activity, "已稍后提醒，下次启动会再询问");
                    return;
                }
                if ("never".equals(strArr[0])) {
                    UpdateManager.prefs(activity).edit().putInt(UpdateManager.KEY_SKIP, i).apply();
                    UpdateManager.toast(activity, "已跳过 v" + str + "，可在「关于此项目」里重新检查");
                }
            }
        });
        create.setCanceledOnTouchOutside(false);
        currentDialog = create;
        lastPromptAt = System.currentTimeMillis();
        try {
            create.show();
        } catch (Exception unused) {
        }
    }

    static void showInstallPrompt(final Activity activity, int i) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        final File apkFile = apkFile(activity, i);
        if (apkFile.isFile()) {
            dismissDialog();
            String versionNameOf = versionNameOf(i);
            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(1);
            int dp = dp(activity, 22.0f);
            linearLayout.setPadding(dp, dp(activity, 18.0f), dp, dp(activity, 2.0f));
            TextView textView = new TextView(activity);
            textView.setText("v" + versionNameOf + " 已下载");
            textView.setTextSize(15.0f);
            textView.setTypeface(Typeface.DEFAULT, 1);
            textView.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
            linearLayout.addView(textView);
            TextView textView2 = new TextView(activity);
            textView2.setText("安装包已下载完成（" + String.format(Locale.US, "%.1f", Float.valueOf(apkFile.length() / 1048576.0f)) + " MB）。现在安装吗？");
            textView2.setTextSize(13.0f);
            textView2.setLineSpacing((float) dp(activity, 6.0f), 1.0f);
            textView2.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
            textView2.setPadding(0, dp(activity, 10.0f), 0, 0);
            linearLayout.addView(textView2);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(linearLayout);
            final AlertDialog create = builder.create();
            LinearLayout dialogActions = dialogActions(activity);
            dialogActions.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 8.0f));
            TextView dialogAction = dialogAction(activity, "稍后", themeColor(activity, android.R.attr.textColorSecondary), false, null);
            TextView dialogAction2 = dialogAction(activity, "立即安装", ACCENT, true, null);
            dialogActions.addView(dialogAction, actionLp(activity, true));
            dialogActions.addView(dialogAction2, actionLp(activity, false));
            linearLayout.addView(dialogActions);
            dialogAction.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.9
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    UpdateManager.toast(activity, "下次启动会再提醒安装");
                    create.dismiss();
                }
            });
            dialogAction2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.10
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    create.dismiss();
                    UpdateManager.install(activity, apkFile);
                }
            });
            create.setCanceledOnTouchOutside(false);
            currentDialog = create;
            lastPromptAt = System.currentTimeMillis();
            try {
                create.show();
            } catch (Exception unused) {
            }
        }
    }

    private static void dismissDialog() {
        AlertDialog alertDialog = currentDialog;
        currentDialog = null;
        if (alertDialog == null || !alertDialog.isShowing()) {
            return;
        }
        try {
            alertDialog.dismiss();
        } catch (Exception unused) {
        }
    }

    static void showDownloadProgress(final Activity activity, int i, final String str, String str2, String str3) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        dismissProgress();
        cancelRequested = false;
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        int dp = dp(activity, 22.0f);
        linearLayout.setPadding(dp, dp(activity, 18.0f), dp, dp(activity, 2.0f));
        TextView textView = new TextView(activity);
        textView.setText("正在下载 v" + str);
        textView.setTextSize(15.0f);
        textView.setTypeface(Typeface.DEFAULT, 1);
        textView.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText("正在连接下载线路…");
        textView2.setTextSize(12.0f);
        textView2.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        textView2.setPadding(0, dp(activity, 10.0f), 0, dp(activity, 12.0f));
        linearLayout.addView(textView2);
        progressTextView = textView2;
        ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        try {
            int i2 = (ACCENT & 16777215) | 855638016;
            progressBar.setProgressTintList(ColorStateList.valueOf(ACCENT));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(i2));
        } catch (Throwable unused) {
        }
        linearLayout.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(activity, 10.0f)));
        progressBarView = progressBar;
        TextView textView3 = new TextView(activity);
        textView3.setText("下载完成后会自动打开安装界面；也可以先退出，下载会在后台继续。");
        textView3.setTextSize(11.0f);
        textView3.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        textView3.setPadding(0, dp(activity, 12.0f), 0, 0);
        linearLayout.addView(textView3);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(linearLayout);
        final AlertDialog create = builder.create();
        LinearLayout dialogActions = dialogActions(activity);
        dialogActions.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 8.0f));
        TextView dialogAction = dialogAction(activity, "取消下载", themeColor(activity, android.R.attr.textColorSecondary), false, null);
        TextView dialogAction2 = dialogAction(activity, "后台下载", ACCENT, true, null);
        dialogActions.addView(dialogAction, actionLp(activity, true));
        dialogActions.addView(dialogAction2, actionLp(activity, false));
        linearLayout.addView(dialogActions);
        dialogAction.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.11
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean unused2 = UpdateManager.cancelRequested = true;
                UpdateManager.publishStatus("已取消下载 v" + str);
                UpdateManager.toast(activity, "已取消下载");
                create.dismiss();
            }
        });
        dialogAction2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.12
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                create.dismiss();
            }
        });
        create.setCanceledOnTouchOutside(false);
        progressDialog = create;
        try {
            create.show();
        } catch (Exception unused2) {
        }
        startDownload(activity, i, str, str2, str3);
    }

    private static TextView dialogAction(Activity activity, String str, int i, boolean z, View.OnClickListener onClickListener) {
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(14.0f);
        textView.setTypeface(Typeface.DEFAULT, z ? 1 : 0);
        textView.setTextColor(i);
        textView.setGravity(17);
        textView.setPadding(dp(activity, 12.0f), dp(activity, 10.0f), dp(activity, 12.0f), dp(activity, 10.0f));
        textView.setOnClickListener(onClickListener);
        return textView;
    }

    private static LinearLayout dialogActions(Activity activity) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(8388629);
        return linearLayout;
    }

    private static LinearLayout.LayoutParams actionLp(Activity activity, boolean z) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        if (!z) {
            layoutParams.leftMargin = dp(activity, 6.0f);
        }
        return layoutParams;
    }

    private static int themeColor(Context context, int i) {
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(i, typedValue, true)) {
            return -8355712;
        }
        if (typedValue.resourceId != 0) {
            try {
                return context.getResources().getColor(typedValue.resourceId);
            } catch (Exception unused) {
                return typedValue.data;
            }
        }
        return typedValue.data;
    }

    static void dismissProgress() {
        AlertDialog alertDialog = progressDialog;
        progressDialog = null;
        progressBarView = null;
        progressTextView = null;
        if (alertDialog != null) {
            try {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            } catch (Exception unused) {
            }
        }
    }

    public static void updateProgressUi(final int i, long j, long j2, final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.13
            @Override // java.lang.Runnable
            public void run() {
                if (UpdateManager.progressBarView != null) {
                    ProgressBar progressBar = UpdateManager.progressBarView;
                    int i2 = i;
                    if (i2 < 0) {
                        i2 = 0;
                    }
                    progressBar.setProgress(i2);
                }
                if (UpdateManager.progressTextView != null) {
                    UpdateManager.progressTextView.setText(str);
                }
            }
        });
    }

    public static void startDownload(Context context, final int i, final String str, String str2, String str3) {
        final String str4;
        if (str2 == null) {
            str4 = "https://github.com/Willian139/smart-course-lite/releases/download/v" + str + "/" + String.format(APK_NAME, str);
        } else {
            str4 = str2;
        }
        final Context applicationContext = context.getApplicationContext();
        final String str5 = str3 == null ? "" : str3;
        publishStatus("开始下载 v" + str + "…");
        updateProgressUi(0, 0L, 0L, "正在连接下载线路…");
        Thread thread = new Thread(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.14
            @Override // java.lang.Runnable
            public void run() {
                File download = null;
                try {
                    final Context context2 = applicationContext;
                    String str6 = str4;
                    int i2 = i;
                    final String str7 = str;
                    download = UpdateManager.download(context2, str6, i2, str7, new ProgressListener() { // from class: com.zuwe.smartcourselite.UpdateManager.14.1
                        private long lastPush;

                        @Override // com.zuwe.smartcourselite.UpdateManager.ProgressListener
                        public void onProgress(int i3, long j, long j2) {
                            long currentTimeMillis = System.currentTimeMillis();
                            if (currentTimeMillis - this.lastPush >= 400 || i3 >= 100) {
                                this.lastPush = currentTimeMillis;
                                String str8 = "下载中 " + i3 + "%（" + (j / 1024) + " / " + (j2 / 1024) + " KB）";
                                UpdateManager.publishStatus(str8);
                                UpdateManager.updateProgressUi(i3, j, j2, str8);
                                Context context3 = context2;
                                UpdateNotifier.show(context3, UpdateNotifier.progress(context3, i3, str7), 4001);
                            }
                        }
                    });
                } catch (Exception e) {
                    String simpleName = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                    UpdateManager.dismissProgress();
                    if (!UpdateManager.cancelRequested) {
                        UpdateManager.publishStatus("下载失败：" + simpleName);
                        Context context3 = applicationContext;
                        UpdateNotifier.show(context3, UpdateNotifier.failed(context3, str, simpleName), 4002);
                        UpdateManager.postToast(applicationContext, "下载失败：" + simpleName);
                        return;
                    }
                    UpdateManager.publishStatus("已取消下载 v" + str);
                    UpdateNotifier.cancel(applicationContext, 4001);
                } finally {
                    UpdateNotifier.cancel(applicationContext, 4001);
                }
                if (download != null && download.isFile()) {
                    UpdateManager.markDownloaded(applicationContext, i);
                    UpdateManager.publishStatus("已下载 v" + str + "，正在调起安装");
                    UpdateManager.updateProgressUi(100, 0L, 0L, "下载完成，正在打开安装界面…");
                    Context context4 = applicationContext;
                    UpdateNotifier.show(context4, UpdateNotifier.done(context4, str, str5), 4002);
                    UpdateManager.dismissProgress();
                    UpdateManager.postInstall(applicationContext, download);
                }
                UpdateManager.publishStatus("下载失败：文件不完整");
                UpdateManager.dismissProgress();
                UpdateManager.postToast(applicationContext, "下载失败：文件不完整");
            }
        }, "update-download");
        thread.setDaemon(true);
        thread.start();
    }

    private static void postToolbarInstall(Context context, int i) {
        postToast(context, "v" + versionNameOf(i) + " 已下载完成，可在「关于此项目 → 检查更新」里安装");
    }

    public static boolean canInstall(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return true;
        }
        try {
            return context.getPackageManager().canRequestPackageInstalls();
        } catch (Exception unused) {
            return false;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x005a A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void requestInstallPermission(android.app.Activity r7) {
        /*
            java.lang.String r0 = "package:"
            int r1 = android.os.Build.VERSION.SDK_INT
            r2 = 26
            if (r1 >= r2) goto L9
            return
        L9:
            r1 = 1
            android.content.Intent r2 = new android.content.Intent     // Catch: java.lang.Exception -> L2f
            java.lang.String r3 = "android.settings.MANAGE_UNKNOWN_APP_SOURCES"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L2f
            r4.<init>(r0)     // Catch: java.lang.Exception -> L2f
            java.lang.String r5 = r7.getPackageName()     // Catch: java.lang.Exception -> L2f
            r4.append(r5)     // Catch: java.lang.Exception -> L2f
            java.lang.String r4 = r4.toString()     // Catch: java.lang.Exception -> L2f
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch: java.lang.Exception -> L2f
            r2.<init>(r3, r4)     // Catch: java.lang.Exception -> L2f
            r7.startActivity(r2)     // Catch: java.lang.Exception -> L2f
            java.lang.String r2 = "打开「允许来自此来源的应用」后返回，再次点击「立即更新」"
            toast(r7, r2)     // Catch: java.lang.Exception -> L2f
            r2 = 1
            goto L30
        L2f:
            r2 = 0
        L30:
            if (r2 != 0) goto L57
            android.content.Intent r3 = new android.content.Intent     // Catch: java.lang.Exception -> L56
            java.lang.String r4 = "android.settings.APPLICATION_SETTINGS"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L56
            r5.<init>(r0)     // Catch: java.lang.Exception -> L56
            java.lang.String r6 = r7.getPackageName()     // Catch: java.lang.Exception -> L56
            r5.append(r6)     // Catch: java.lang.Exception -> L56
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Exception -> L56
            android.net.Uri r5 = android.net.Uri.parse(r5)     // Catch: java.lang.Exception -> L56
            r3.<init>(r4, r5)     // Catch: java.lang.Exception -> L56
            r7.startActivity(r3)     // Catch: java.lang.Exception -> L56
            java.lang.String r3 = "在应用设置中找到「安装未知应用」并开启，返回后再点「立即更新」"
            toast(r7, r3)     // Catch: java.lang.Exception -> L56
            goto L58
        L56:
        L57:
            r1 = r2
        L58:
            if (r1 != 0) goto L83
            android.content.Intent r1 = new android.content.Intent     // Catch: java.lang.Exception -> L7e
            java.lang.String r2 = "android.settings.APPLICATION_DETAILS_SETTINGS"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L7e
            r3.<init>(r0)     // Catch: java.lang.Exception -> L7e
            java.lang.String r0 = r7.getPackageName()     // Catch: java.lang.Exception -> L7e
            r3.append(r0)     // Catch: java.lang.Exception -> L7e
            java.lang.String r0 = r3.toString()     // Catch: java.lang.Exception -> L7e
            android.net.Uri r0 = android.net.Uri.parse(r0)     // Catch: java.lang.Exception -> L7e
            r1.<init>(r2, r0)     // Catch: java.lang.Exception -> L7e
            r7.startActivity(r1)     // Catch: java.lang.Exception -> L7e
            java.lang.String r0 = "在详情页找到「安装未知应用/来源不明应用」开启权限，返回后再点「立即更新」"
            toast(r7, r0)     // Catch: java.lang.Exception -> L7e
            goto L83
        L7e:
            java.lang.String r0 = "请手动去 设置 → 应用 → 智慧思政课表 → 安装未知应用 开启权限"
            toast(r7, r0)
        L83:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.UpdateManager.requestInstallPermission(android.app.Activity):void");
    }

    public static void install(Context context, File file) {
        if (context == null || file == null || !file.isFile()) {
            toast(context, "安装包不存在");
            return;
        }
        Uri exposeForInstall = exposeForInstall(context, file);
        if (exposeForInstall == null) {
            exposeForInstall = Uri.fromFile(file);
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(exposeForInstall, "application/vnd.android.package-archive");
            intent.setFlags(268435457);
            if (context instanceof Activity) {
                ((Activity) context).startActivity(intent);
            } else {
                intent.setFlags(intent.getFlags() | 268435456);
                context.startActivity(intent);
            }
            toast(context, "请在弹出的列表中选择「文件管理器」或「浏览器」安装");
        } catch (Exception e) {
            toast(context, "无法打开文件：" + e.getClass().getSimpleName());
        }
    }

    static Uri exposeForInstall(Context context, File file) {
        if (Build.VERSION.SDK_INT < 29) {
            return null;
        }
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", String.format(APK_NAME, versionNameOf(codeOf(currentName(context)))));
            contentValues.put("mime_type", "application/vnd.android.package-archive");
            contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS + "/DSH-Go");
            Uri insert = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            if (insert == null) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            outputStream = context.getContentResolver().openOutputStream(insert);
            if (outputStream == null) {
                return null;
            }
            byte[] bArr = new byte[65536];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                outputStream.write(bArr, 0, read);
            }
            outputStream.flush();
            return insert;
        } catch (Exception e) {
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public static void cleanupAfterUpdate(Context context) {
        SharedPreferences prefs = prefs(context);
        int i = prefs.getInt(KEY_DOWNLOADED, 0);
        if (i > 0 && i <= currentCode(context)) {
            File apkFile = apkFile(context, i);
            if (apkFile.isFile()) {
                apkFile.delete();
            }
            prefs.edit().remove(KEY_DOWNLOADED).remove(KEY_SHA + i).remove(KEY_DEFER).remove(KEY_AUTO_INSTALL).apply();
        }
        File[] listFiles = dir(context).listFiles();
        if (listFiles != null) {
            int currentCode = currentCode(context);
            for (File file : listFiles) {
                if (file.getName().endsWith(".part")) {
                    file.delete();
                }
                if (file.getName().endsWith(".apk")) {
                    if (file.getName().contains("_" + versionNameOf(currentCode) + ".apk")) {
                        file.delete();
                    }
                }
            }
        }
    }

    static void markDownloaded(Context context, int i) {
        prefs(context).edit().putInt(KEY_DOWNLOADED, i).apply();
    }

    static void markSha(Context context, int i, String str) {
        prefs(context).edit().putString(KEY_SHA + i, str).apply();
    }

    static JSONObject fetchJson(String str) throws Exception {
        return new JSONObject(fetchText(str));
    }

    static String fetchText(String str) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        try {
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(12000);
            httpURLConnection.setRequestProperty("Accept", "application/vnd.github+json");
            httpURLConnection.setRequestProperty("User-Agent", "SmartCourseLite/" + Build.VERSION.SDK_INT);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode);
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[8192];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            inputStream.close();
            String str2 = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return str2;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    static File download(Context context, String str, int i, String str2, ProgressListener progressListener) throws Exception {
        String[] mirrorCandidates = mirrorCandidates(str);
        Exception e = null;
        int i2 = 0;
        while (i2 < mirrorCandidates.length) {
            StringBuilder sb = new StringBuilder("正在连接下载线路 ");
            int i3 = i2 + 1;
            sb.append(i3);
            sb.append("/");
            sb.append(mirrorCandidates.length);
            sb.append("…");
            publishStatus(sb.toString());
            try {
                return downloadOnce(context, mirrorCandidates[i2], i, str2, progressListener);
            } catch (Exception e2) {
                e = e2;
                i2 = i3;
            }
        }
        if (e != null) {
            throw e;
        }
        throw new IOException("下载失败");
    }

    static String checkSummary(Context context) {
        SharedPreferences prefs = prefs(context);
        int i = prefs.getInt(KEY_LAST_REMOTE, 0);
        long j = prefs.getLong(KEY_LAST_CHECK, 0L);
        if (i <= 0 || j <= 0) {
            return "当前版本 v" + currentName(context);
        }
        return "当前 v" + currentName(context) + " · 最新 v" + versionNameOf(i) + " · " + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date(j)) + " 检查";
    }

    public static void resetStaleChoice(Context context, int i) {
        SharedPreferences prefs = prefs(context);
        String string = prefs.getString(KEY_SEEN_VERSION, "");
        String currentName = currentName(context);
        if (string.equals(currentName)) {
            return;
        }
        prefs.edit().putString(KEY_SEEN_VERSION, currentName).remove(KEY_SKIP).remove(KEY_DEFER).remove(KEY_AUTO_INSTALL).putLong(KEY_LAST_CHECK, 0L).apply();
    }

    public static void postInstall(final Context context, final File file) {
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.15
            @Override // java.lang.Runnable
            public void run() {
                try {
                    UpdateManager.install(context, file);
                } catch (Exception e) {
                    UpdateManager.postToast(context, "无法调起安装器：" + e.getClass().getSimpleName());
                }
            }
        });
    }

    private static String[] mirrorCandidates(String str) {
        return new String[]{"https://gh-proxy.com/" + str, "https://ghfast.top/" + str, "https://ghproxy.net/" + str, str};
    }

    private static File downloadOnce(Context context, String str, int i, String str2, ProgressListener progressListener) throws Exception {
        byte[] bArr;
        File dir = dir(context);
        File apkFile = apkFile(context, str2);
        File file = new File(dir, String.format(APK_NAME, str2) + ".part");
        if (file.exists()) {
            file.delete();
        }
        if (apkFile.exists()) {
            apkFile.delete();
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        try {
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setConnectTimeout(6000);
            httpURLConnection.setReadTimeout(20000);
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + ") SmartCourseLite/" + currentName(context));
            httpURLConnection.setRequestProperty("Accept", "application/octet-stream");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode);
            }
            int contentLength = httpURLConnection.getContentLength();
            InputStream inputStream = httpURLConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bArr2 = new byte[16384];
            long j = 0;
            int i2 = -1;
            while (true) {
                int read = inputStream.read(bArr2);
                if (read > 0) {
                    if (cancelRequested) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception unused) {
                        }
                        try {
                            inputStream.close();
                        } catch (Exception unused2) {
                        }
                        file.delete();
                        throw new IOException("已取消");
                    }
                    fileOutputStream.write(bArr2, 0, read);
                    messageDigest.update(bArr2, 0, read);
                    j += read;
                    if (contentLength <= 0 || progressListener == null) {
                        bArr = bArr2;
                    } else {
                        long j2 = contentLength;
                        bArr = bArr2;
                        int i3 = (int) ((100 * j) / j2);
                        if (i3 != i2) {
                            progressListener.onProgress(i3, j, j2);
                            i2 = i3;
                        }
                    }
                    bArr2 = bArr;
                } else {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                    if (file.length() < 204800) {
                        throw new IOException("安装包大小异常");
                    }
                    if (contentLength > 0 && file.length() != contentLength) {
                        throw new IOException("下载不完整");
                    }
                    if (!file.renameTo(apkFile)) {
                        throw new IOException("无法写入安装包");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (byte b : messageDigest.digest()) {
                        sb.append(String.format("%02x", Byte.valueOf(b)));
                    }
                    markSha(context, i, sb.toString());
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    return apkFile;
                }
            }
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public static View buildUpdateRow(final Activity activity, int i, int i2, int i3, int i4, int i5, int i6) {
        String checkSummary;
        ACCENT = i6;
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 14.0f));
        LinearLayout linearLayout2 = new LinearLayout(activity);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        TextView textView = new TextView(activity);
        textView.setText("检查更新");
        textView.setTextSize(12.0f);
        textView.setTextColor(adjustAlpha(i, 204));
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(activity);
        textView2.setTextSize(12.0f);
        textView2.setTextColor(i6);
        textView2.setTypeface(Typeface.DEFAULT, 1);
        textView2.setText("v" + currentName(activity) + " ›");
        linearLayout2.addView(textView2);
        linearLayout.addView(linearLayout2);
        final TextView textView3 = new TextView(activity);
        textView3.setTextSize(10.5f);
        textView3.setTextColor(adjustAlpha(i3, 255));
        if (hasDownloaded(activity)) {
            checkSummary = "已下载 v" + versionNameOf(downloadedCode(activity)) + "，点此安装";
        } else {
            checkSummary = checkSummary(activity);
        }
        textView3.setText(checkSummary);
        textView3.setPadding(0, dp(activity, 6.0f), 0, 0);
        if (statusText != null && statusText.length() > 0) {
            textView3.setText(statusText);
        }
        statusViewRef = new WeakReference<>(textView3);
        linearLayout.addView(textView3);
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.UpdateManager.16
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (UpdateManager.hasDownloaded(activity)) {
                    Activity activity2 = activity;
                    UpdateManager.showInstallPrompt(activity2, UpdateManager.downloadedCode(activity2));
                    return;
                }
                textView3.setText("正在检查更新…");
                WeakReference unused = UpdateManager.statusViewRef = new WeakReference(textView3);
                final Activity activity3 = activity;
                final TextView textView4 = textView3;
                UpdateManager.checkManually(activity3, new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.16.1
                    @Override // java.lang.Runnable
                    public void run() {
                        textView4.setText(UpdateManager.checkSummary(activity3));
                    }
                });
                Handler handler = new Handler(Looper.getMainLooper());
                final Activity activity4 = activity;
                final TextView textView5 = textView3;
                handler.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.16.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (UpdateManager.hasDownloaded(activity4)) {
                            textView5.setText("已下载 v" + UpdateManager.versionNameOf(UpdateManager.downloadedCode(activity4)) + "，点此安装");
                            return;
                        }
                        if ("正在检查更新…".equals(textView5.getText().toString())) {
                            textView5.setText(UpdateManager.checkSummary(activity4));
                        }
                    }
                }, 12000L);
            }
        });
        linearLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.UpdateManager.17
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    view.animate().alpha(0.6f).setDuration(90L).start();
                    return false;
                }
                if (motionEvent.getAction() != 1 && motionEvent.getAction() != 3) {
                    return false;
                }
                view.animate().alpha(1.0f).setDuration(180L).start();
                return false;
            }
        });
        return linearLayout;
    }

    private static int adjustAlpha(int i, int i2) {
        return Color.argb(i2, Color.red(i), Color.green(i), Color.blue(i));
    }

    public static int dp(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static void post(Activity activity, Runnable runnable) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(runnable);
    }

    public static void postToast(final Context context, final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.zuwe.smartcourselite.UpdateManager.18
            @Override // java.lang.Runnable
            public void run() {
                UpdateManager.toast(context, str);
            }
        });
    }

    static void toast(Context context, String str) {
        try {
            Toast.makeText(context, str, 0).show();
        } catch (Exception unused) {
        }
    }
}
