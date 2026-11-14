package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 自动更新：GitHub Release 静默探测 + 后台静默下载 + 安装提示。
 *
 * 流程：
 *  1. 启动时 checkOnLaunch()：若本机已下载好安装包且版本更新 → 直接弹「安装更新」；
 *     否则静默请求 releases/latest，发现更高版本按用户上次的选择处理：
 *     立即更新 → 后台下载；稍后 → 记下该版本，下次启动再问；不再提示 → 记住并跳过。
 *  2. 下载走应用进程内的后台线程，通知栏尽量显示进度，全程不弹窗。
 *  3. 下载完成写入 done 标记 + 版本号，下次启动即可被探测到。
 */
public final class UpdateManager {

    public static final String REPO = "Willian139/smart-course-lite";
    private static final String LATEST_URL =
            "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String TAG_URL =
            "https://api.github.com/repos/" + REPO + "/releases/tags/v";

    private static final String PREFS = "smartcourse_update";
    private static final String KEY_SKIP = "skip_code";        // 「不再提示」的版本号
    private static final String KEY_DEFER = "defer_code";      // 「稍后」的版本号
    private static final String KEY_DOWNLOADED = "dl_code";    // 已下载完成的版本号
    private static final String KEY_LAST_CHECK = "last_check";
    private static final String KEY_AUTO_INSTALL = "auto_install_code"; // 用户点了「立即更新」的版本号
    private static final String KEY_SHA = "dl_sha_";           // 下载包 sha256（按版本号）
    private static final String KEY_LAST_REMOTE = "last_remote"; // 上次检查到的最新版本
    private static final String KEY_SEEN_VERSION = "seen_version"; // 上次运行时的版本名（用于迁移旧标记）

    public static final String APK_NAME = "smart-course-lite-v%s.apk";
    private static final long CHECK_INTERVAL_MS = 30L * 60L * 1000L;

    private static final Object LOCK = new Object();
    private static volatile boolean checking;
    private static AlertDialog currentDialog;
    private static volatile String pendingUrl;          // 本次探测到的下载地址
    private static volatile String statusText = "";     // 下载 / 检查的实时状态文案
    private static volatile long lastPromptAt;          // 上次弹出更新框的时间（防抖）
    private static java.lang.ref.WeakReference<TextView> statusViewRef;

    /** 把状态文案推给界面（关于页那一行）与内存，供失败时排查。 */
    static void publishStatus(String text) {
        statusText = text == null ? "" : text;
        final TextView view = statusViewRef == null ? null : statusViewRef.get();
        if (view == null) return;
        final String t = statusText;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() { view.setText(t); }
        });
    }

    public static String statusText() {
        return statusText;
    }

    private UpdateManager() {
    }

    // ------------------------------------------------------------ 基础信息

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /**
     * 版本比较统一用「名字编码」（3.5.39 → 30539）。
     * 注意不能和 PackageManager 的 versionCode 混用：本项目的 versionCode 是自增计数器
     * （3.5.37 → 378、3.5.39 → 380），和 3.5.x 的语义完全不同，混用会导致「永远有新版本」。
     */
    public static int currentCode(Context context) {
        return codeOf(currentName(context));
    }

    /** 构建号（自增计数器），仅用于展示或日志。 */
    public static int rawVersionCode(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String currentName(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "未知";
        }
    }

    public static File dir(Context context) {
        File base = context.getExternalFilesDir("update");
        if (base == null) base = new File(context.getFilesDir(), "update");
        if (!base.exists()) base.mkdirs();
        return base;
    }

    public static File apkFile(Context context, int code) {
        return new File(dir(context), String.format(APK_NAME, versionNameOf(code)));
    }

    public static File apkFile(Context context, String versionName) {
        return new File(dir(context), String.format(APK_NAME, versionName));
    }

    /** 名字编码反解：30539 -> 3.5.39。 */
    public static String versionNameOf(int code) {
        int patch = code % 100;
        int minor = (code / 100) % 100;
        int major = code / 10000;
        return major + "." + minor + "." + patch;
    }

    /** 正规化版本号字符串：「v3.5.37」→「3.5.37」。 */
    public static String cleanTag(String tag) {
        if (tag == null) return "";
        String t = tag.trim();
        if (t.startsWith("v") || t.startsWith("V")) t = t.substring(1);
        return t;
    }

    /** 解析版本的数值编码：3.5.37 → 30537。 */
    public static int codeOf(String version) {
        String v = cleanTag(version);
        String[] parts = v.split("\\.");
        int major = pick(parts, 0);
        int minor = pick(parts, 1);
        int patch = pick(parts, 2);
        return major * 10000 + minor * 100 + Math.min(patch, 99);
    }

    private static int pick(String[] parts, int index) {
        if (parts.length <= index) return 0;
        StringBuilder digits = new StringBuilder();
        String raw = parts[index];
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') digits.append(c);
            else break;
        }
        if (digits.length() == 0) return 0;
        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean hasDownloaded(Context context) {
        int code = prefs(context).getInt(KEY_DOWNLOADED, 0);
        if (code <= currentCode(context)) return false;
        File apk = apkFile(context, code);
        return apk.isFile() && apk.length() > 200 * 1024;
    }

    public static int downloadedCode(Context context) {
        return prefs(context).getInt(KEY_DOWNLOADED, 0);
    }

    // ------------------------------------------------------------ 启动检查

    /** 启动时调用：先看本地有没有下好的包，再静默查线上。 */
    public static void checkOnLaunch(final Activity activity) {
        if (activity == null || activity.isFinishing()) return;

        if (hasDownloaded(activity)) {
            final int code = downloadedCode(activity);
            activity.getWindow().getDecorView().post(new Runnable() {
                @Override public void run() { showInstallPrompt(activity, code); }
            });
            return;
        }

        // 每次打开都探测一次：此前用 30 分钟节流，导致刚装完新版本就打开时
        // 因为上次检查太近而完全静默，看起来像「不会自动检查」。
        if (System.currentTimeMillis() - lastPromptAt < 10000L) return;  // 防重复弹窗
        check(activity, false, null);
    }

    /** 手动检查：提供一个回调以便界面提示「已是最新」。 */
    public static void checkManually(final Activity activity, final Runnable upToDate) {
        check(activity, true, upToDate);
    }

    private static void check(final Activity activity, final boolean manual,
                              final Runnable upToDate) {
        SharedPreferences sp = prefs(activity);
        if (checking) {
            publishStatus("正在检查更新…");
            if (manual) toast(activity, "正在检查更新…");
            return;
        }
        checking = true;
        final Context app = activity.getApplicationContext();
        if (manual) toast(activity, "正在检查更新…");

        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    JSONObject latest = fetchJson(LATEST_URL);
                    if (latest == null) {
                        latest = fallbackLatest();
                    }
                    if (latest == null) {
                        publishStatus("检查失败：网络不可用");
                        if (manual) postToast(activity, "检查更新失败，网络不可用");
                        return;
                    }
                    prefs(app).edit()
                            .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
                            .putInt(KEY_LAST_REMOTE, Math.max(codeOf(cleanTag(latest.optString("tag_name", ""))),
                                    latest.optInt("version_code", 0)))
                            .apply();

                    final String tag = latest.optString("tag_name", "");
                    final String name = cleanTag(tag);
                    final int remote = Math.max(codeOf(name), latest.optInt("version_code", 0));
                    final String notes = latest.optString("body", "");
                    final String url = assetUrl(latest);
                    final int local = currentCode(app);

                    if (remote <= local || name.isEmpty()) {
                        publishStatus("已是最新 v" + currentName(app));
                        if (upToDate != null) {
                            post(activity, upToDate);
                        }
                        if (manual) post(activity, new Runnable() {
                            @Override public void run() {
                                toast(activity, "已是最新版本 v" + currentName(app));
                            }
                        });
                        return;
                    }

                    resetStaleChoice(app, local);

                    final int skip = prefs(app).getInt(KEY_SKIP, 0);
                    if (!manual && skip >= remote) return;

                    pendingUrl = url;
                    publishStatus("发现新版本 v" + name);
                    post(activity, new Runnable() {
                        @Override public void run() { showUpdateDialog(activity, remote, name, notes); }
                    });
                } catch (Exception e) {
                    publishStatus("检查失败：" + e.getClass().getSimpleName());
                    if (manual) postToast(activity, "检查更新失败：" + e.getClass().getSimpleName());
                } finally {
                    checking = false;
                }
            }
        }, "update-check");
        thread.setDaemon(true);
        thread.start();
    }

    /** GitHub 偶发限流时的兜底：直接按 tag 查（这里用同一个 latest，失败则返回 null）。 */
    private static JSONObject fallbackLatest() {
        String[] candidates = {"https://api.github.com/repos/" + REPO + "/releases?per_page=1"};
        try {
            JSONArray array = new JSONArray(fetchText(candidates[0]));
            return array.length() > 0 ? array.optJSONObject(0) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String assetUrl(JSONObject release) {
        JSONArray assets = release.optJSONArray("assets");
        if (assets == null) return null;
        String fallback = null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject a = assets.optJSONObject(i);
            if (a == null) continue;
            String name = a.optString("name", "");
            String url = a.optString("browser_download_url", "");
            if (url.isEmpty()) continue;
            if (name.endsWith(".apk")) {
                if (name.equals(String.format(APK_NAME, cleanTag(release.optString("tag_name", ""))))) {
                    return url;
                }
                if (fallback == null) fallback = url;
            }
        }
        return fallback;
    }

    // ------------------------------------------------------------ 更新提示

    /** 启动时展示的「升级公告」：版本号 + 更新内容 + 三选，只有用户点「立即更新」才下载。 */
    static void showUpdateDialog(final Activity activity, final int remote, final String name,
                                 final String notes) {
        if (activity == null || activity.isFinishing()) return;
        dismissDialog();

        final String body = (notes == null || notes.trim().isEmpty())
                ? "本次更新没有填写说明。"
                : notes.trim();
        final String[] action = new String[]{""};

        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 22);
        box.setPadding(pad, dp(activity, 18), pad, dp(activity, 2));

        TextView title = new TextView(activity);
        title.setText("发现新版本 v" + name);
        title.setTextSize(15f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        box.addView(title);

        TextView sub = new TextView(activity);
        sub.setText("当前 v" + currentName(activity) + "   →   最新 v" + name);
        sub.setTextSize(12f);
        sub.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        sub.setPadding(0, dp(activity, 8), 0, dp(activity, 16));
        box.addView(sub);

        TextView label = new TextView(activity);
        label.setText("更新内容");
        label.setTextSize(13f);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        box.addView(label);

        final TextView content = new TextView(activity);
        content.setText(body);
        content.setTextSize(13f);
        content.setLineSpacing(dp(activity, 6), 1f);
        content.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        content.setPadding(0, dp(activity, 8), 0, 0);
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content, new android.widget.FrameLayout.LayoutParams(-1, -2));
        box.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        scroll.post(new Runnable() {
            @Override public void run() {
                int max = dp(activity, 280);
                if (content.getHeight() > max) {
                    android.view.ViewGroup.LayoutParams lp = scroll.getLayoutParams();
                    lp.height = max;
                    scroll.setLayoutParams(lp);
                }
            }
        });

        TextView tip = new TextView(activity);
        tip.setText("更新为可选操作，不更新也能继续使用。");
        tip.setTextSize(11f);
        tip.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        tip.setPadding(0, dp(activity, 14), 0, 0);
        box.addView(tip);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(box);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                action[0] = "now";
            }
        });
        builder.setNeutralButton("稍后", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                action[0] = "later";
            }
        });
        builder.setNegativeButton("不再提示", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                action[0] = "never";
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface d) {
                if ("now".equals(action[0])) {
                    prefs(activity).edit().remove(KEY_DEFER).apply();
                    if (pendingUrl == null || pendingUrl.length() == 0) {
                        toast(activity, "下载地址无效，请稍后在「关于此项目」里重试");
                        return;
                    }
                    showDownloadProgress(activity, remote, name, pendingUrl, notes);
                } else if ("later".equals(action[0])) {
                    toast(activity, "已稍后提醒，下次启动会再询问");
                } else if ("never".equals(action[0])) {
                    prefs(activity).edit().putInt(KEY_SKIP, remote).apply();
                    toast(activity, "已跳过 v" + name + "，可在「关于此项目」里重新检查");
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        currentDialog = dialog;
        lastPromptAt = System.currentTimeMillis();
        try {
            dialog.show();
        } catch (Exception ignored) {
        }
    }

    static void showInstallPrompt(final Activity activity, final int code) {
        if (activity == null || activity.isFinishing()) return;
        final File apk = apkFile(activity, code);
        if (!apk.isFile()) return;
        dismissDialog();

        final String version = versionNameOf(code);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("v" + version + " 已下载");
        builder.setMessage("安装包已下载完成（" + String.format(java.util.Locale.US, "%.1f", apk.length() / 1048576f) + " MB）。\n\n现在安装吗？");
        builder.setPositiveButton("立即安装", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                install(activity, apk);
            }
        });
        builder.setNegativeButton("稍后", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                toast(activity, "下次启动会再提醒安装");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        currentDialog = dialog;
        lastPromptAt = System.currentTimeMillis();
        try {
            dialog.show();
        } catch (Exception ignored) {
        }
    }

    private static void dismissDialog() {
        final AlertDialog dialog = currentDialog;
        currentDialog = null;
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------ 下载

    // ------------------------------------------------------------ 前台下载进度

    private static AlertDialog progressDialog;
    private static android.widget.ProgressBar progressBarView;
    private static TextView progressTextView;
    private static volatile boolean cancelRequested;

    /** 用户点「立即更新」后：先弹带进度条的前台下载面板，可随时转为后台。 */
    static void showDownloadProgress(final Activity activity, final int code, final String name,
                                     final String url, final String notes) {
        if (activity == null || activity.isFinishing()) return;
        dismissProgress();
        cancelRequested = false;

        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 22);
        box.setPadding(pad, dp(activity, 18), pad, dp(activity, 2));

        TextView title = new TextView(activity);
        title.setText("正在下载 v" + name);
        title.setTextSize(15f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(themeColor(activity, android.R.attr.textColorPrimary));
        box.addView(title);

        final TextView stat = new TextView(activity);
        stat.setText("正在连接下载线路…");
        stat.setTextSize(12f);
        stat.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        stat.setPadding(0, dp(activity, 10), 0, dp(activity, 12));
        box.addView(stat);
        progressTextView = stat;

        android.widget.ProgressBar bar =
                new android.widget.ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        box.addView(bar, new LinearLayout.LayoutParams(-1, dp(activity, 10)));
        progressBarView = bar;

        TextView hint = new TextView(activity);
        hint.setText("下载完成后会自动打开安装界面；也可以先退出，下载会在后台继续。");
        hint.setTextSize(11f);
        hint.setTextColor(themeColor(activity, android.R.attr.textColorSecondary));
        hint.setPadding(0, dp(activity, 12), 0, 0);
        box.addView(hint);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(box);
        builder.setPositiveButton("后台下载", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                // 只关掉面板，下载继续；完成后仍会自动调起安装器
            }
        });
        builder.setNegativeButton("取消下载", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int which) {
                cancelRequested = true;
                publishStatus("已取消下载 v" + name);
                toast(activity, "已取消下载");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        progressDialog = dialog;
        try {
            dialog.show();
        } catch (Exception ignored) {
        }
        startDownload(activity, code, name, url, notes);
    }

    /** 跟随对话框主题取色，避免深色主题下文字看不见。 */
    private static int themeColor(Context context, int attr) {
        android.util.TypedValue tv = new android.util.TypedValue();
        if (context.getTheme().resolveAttribute(attr, tv, true)) {
            if (tv.resourceId != 0) {
                try {
                    return context.getResources().getColor(tv.resourceId);
                } catch (Exception ignored) {
                    return tv.data;
                }
            }
            return tv.data;
        }
        return 0xFF808080;
    }

    static void dismissProgress() {
        AlertDialog d = progressDialog;
        progressDialog = null;
        progressBarView = null;
        progressTextView = null;
        if (d != null) {
            try {
                if (d.isShowing()) d.dismiss();
            } catch (Exception ignored) {
            }
        }
    }

    private static void updateProgressUi(final int percent, final long done, final long total,
                                         final String lineText) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                if (progressBarView != null) {
                    progressBarView.setProgress(percent < 0 ? 0 : percent);
                }
                if (progressTextView != null) {
                    progressTextView.setText(lineText);
                }
            }
        });
    }

    public static void startDownload(Context context, int code, String versionName,
                                     String url, String notes) {
        if (url == null) {
            url = "https://github.com/" + REPO + "/releases/download/v" + versionName
                    + "/" + String.format(APK_NAME, versionName);
        }
        final Context app = context.getApplicationContext();
        final int targetCode = code;
        final String targetVersion = versionName;
        final String finalUrl = url;
        final String finalNotes = notes == null ? "" : notes;
        publishStatus("开始下载 v" + targetVersion + "…");
        updateProgressUi(0, 0, 0, "正在连接下载线路…");
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                boolean ok = false;
                try {
                    File apk = download(app, finalUrl, targetCode, targetVersion,
                            new ProgressListener() {
                                private long lastPush;

                                @Override public void onProgress(int percent, long done, long total) {
                                    long now = System.currentTimeMillis();
                                    if (now - lastPush < 400L && percent < 100) return;
                                    lastPush = now;
                                    String line = "下载中 " + percent + "%（"
                                            + (done / 1024) + " / " + (total / 1024) + " KB）";
                                    publishStatus(line);
                                    updateProgressUi(percent, done, total, line);
                                    UpdateNotifier.show(app,
                                            UpdateNotifier.progress(app, percent, targetVersion),
                                            UpdateNotifier.ID_PROGRESS);
                                }
                            });
                    ok = apk != null && apk.isFile();
                    if (ok) {
                        markDownloaded(app, targetCode);
                        publishStatus("已下载 v" + targetVersion + "，正在调起安装");
                        updateProgressUi(100, 0, 0, "下载完成，正在打开安装界面…");
                        UpdateNotifier.show(app,
                                UpdateNotifier.done(app, targetVersion, finalNotes),
                                UpdateNotifier.ID_DONE);
                        dismissProgress();
                        // 用户已经选过「立即更新」，下载完直接进安装器，不再二次确认
                        postInstall(app, apk);
                    } else {
                        publishStatus("下载失败：文件不完整");
                        dismissProgress();
                        postToast(app, "下载失败：文件不完整");
                    }
                } catch (Exception e) {
                    final String reason = e.getMessage() == null
                            ? e.getClass().getSimpleName() : e.getMessage();
                    dismissProgress();
                    if (cancelRequested) {
                        publishStatus("已取消下载 v" + targetVersion);
                        UpdateNotifier.cancel(app, UpdateNotifier.ID_PROGRESS);
                        return;
                    }
                    publishStatus("下载失败：" + reason);
                    UpdateNotifier.show(app,
                            UpdateNotifier.failed(app, targetVersion, reason),
                            UpdateNotifier.ID_DONE);
                    postToast(app, "下载失败：" + reason);
                } finally {
                    UpdateNotifier.cancel(app, UpdateNotifier.ID_PROGRESS);
                }
            }
        }, "update-download");
        thread.setDaemon(true);
        thread.start();
    }

    /** 下载完成的提示（无通知权限时 Toast 是唯一可靠的反馈）。 */
    private static void postToolbarInstall(final Context context, final int code) {
        postToast(context, "v" + versionNameOf(code) + " 已下载完成，可在「关于此项目 → 检查更新」里安装");
    }

    // ------------------------------------------------------------ 安装

    public static boolean canInstall(Context context) {
        if (Build.VERSION.SDK_INT < 26) return true;
        try {
            return context.getPackageManager().canRequestPackageInstalls();
        } catch (Exception e) {
            return false;
        }
    }

    public static void requestInstallPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < 26) return;
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
            toast(activity, "打开「允许来自此来源的应用」后返回，再次点击「立即更新」");
        } catch (Exception e) {
            toast(activity, "请到系统设置里允许本应用安装应用");
        }
    }

    /** 调起系统安装器：优先用 MediaStore 给出 content:// 地址，避免 file:// 被系统拒绝。 */
    public static void install(final Context context, final File apk) {
        if (context == null || apk == null || !apk.isFile()) {
            toast(context, "安装包不存在");
            return;
        }
        Uri uri = exposeForInstall(context, apk);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri != null ? uri : Uri.fromFile(apk),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            toast(context, "无法调起安装器：" + e.getClass().getSimpleName()
                    + "，安装包位置：" + apk.getAbsolutePath());
        }
    }

    /**
     * 把安装包拷进系统「下载」集合，返回可跨应用读取的 content:// 地址。
     * Android 10 以下返回 null（退回 file://）。
     */
    private static Uri exposeForInstall(Context context, File apk) {
        if (Build.VERSION.SDK_INT < 29) return null;
        InputStream in = null;
        java.io.OutputStream out = null;
        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                    String.format(APK_NAME, versionNameOf(codeOf(currentName(context)))));
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE,
                    "application/vnd.android.package-archive");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/DSH-Go");
            Uri collection = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            Uri uri = context.getContentResolver().insert(collection, values);
            if (uri == null) return null;
            in = new java.io.FileInputStream(apk);
            out = context.getContentResolver().openOutputStream(uri);
            if (out == null) return null;
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            return uri;
        } catch (Exception e) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
        }
    }

    /** 安装成功启动新版本后调用：清理安装包与状态。 */
    public static void cleanupAfterUpdate(Context context) {
        SharedPreferences sp = prefs(context);
        int downloaded = sp.getInt(KEY_DOWNLOADED, 0);
        if (downloaded > 0 && downloaded <= currentCode(context)) {
            File apk = apkFile(context, downloaded);
            if (apk.isFile()) apk.delete();
            sp.edit().remove(KEY_DOWNLOADED).remove(KEY_SHA + downloaded)
                    .remove(KEY_DEFER).remove(KEY_AUTO_INSTALL).apply();
        }
        File[] files = dir(context).listFiles();
        if (files != null) {
            int current = currentCode(context);
            for (File f : files) {
                if (f.getName().endsWith(".part")) f.delete();
                if (f.getName().endsWith(".apk") && f.getName().contains(
                        "_" + versionNameOf(current) + ".apk")) {
                    f.delete();
                }
            }
        }
    }

    static void markDownloaded(Context context, int code) {
        prefs(context).edit().putInt(KEY_DOWNLOADED, code).apply();
    }

    static void markSha(Context context, int code, String sha) {
        prefs(context).edit().putString(KEY_SHA + code, sha).apply();
    }

    // ------------------------------------------------------------ 网络工具

    static JSONObject fetchJson(String url) throws Exception {
        return new JSONObject(fetchText(url));
    }

    static String fetchText(String url) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "SmartCourseLite/" + Build.VERSION.SDK_INT);
            int code = connection.getResponseCode();
            if (code != 200) throw new IOException("HTTP " + code);
            InputStream in = connection.getInputStream();
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) > 0) out.write(buf, 0, read);
            in.close();
            return new String(out.toByteArray(), "UTF-8");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /** 下载到临时文件并校验大小，成功后改名成正式安装包。 */
    /**
     * 依次尝试直连与几个公共加速线路（国内网络直连 GitHub 的 releases 资源经常超时）。
     * 任何一条成功即返回；全部失败则抛出最后一次异常。
     */
    static File download(Context context, String url, int code, String versionName,
                         ProgressListener listener) throws Exception {
        String[] candidates = mirrorCandidates(url);
        Exception last = null;
        for (int i = 0; i < candidates.length; i++) {
            publishStatus("正在连接下载线路 " + (i + 1) + "/" + candidates.length + "…");
            try {
                return downloadOnce(context, candidates[i], code, versionName, listener);
            } catch (Exception e) {
                last = e;
            }
        }
        throw last != null ? last : new IOException("下载失败");
    }

    /** 关于页那一行的说明：当前版本 / 最近一次探测到的版本 / 检查时间。 */
    static String checkSummary(Context context) {
        SharedPreferences sp = prefs(context);
        int remote = sp.getInt(KEY_LAST_REMOTE, 0);
        long at = sp.getLong(KEY_LAST_CHECK, 0L);
        if (remote <= 0 || at <= 0) return "当前版本 v" + currentName(context);
        String time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA)
                .format(new java.util.Date(at));
        return "当前 v" + currentName(context) + " · 最新 v" + versionNameOf(remote)
                + " · " + time + " 检查";
    }

    /** 版本变化后清掉旧的「不再提示 / 稍后 / 自动安装」标记，否则新版本可能被历史选择静默跳过。 */
    private static void resetStaleChoice(Context context, int localCode) {
        SharedPreferences sp = prefs(context);
        String seen = sp.getString(KEY_SEEN_VERSION, "");
        String now = currentName(context);
        if (seen.equals(now)) return;
        sp.edit()
                .putString(KEY_SEEN_VERSION, now)
                .remove(KEY_SKIP)
                .remove(KEY_DEFER)
                .remove(KEY_AUTO_INSTALL)
                .putLong(KEY_LAST_CHECK, 0L)
                .apply();
    }

    /** 主线程调起系统安装器。 */
    private static void postInstall(final Context context, final File apk) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                try {
                    install(context, apk);
                } catch (Exception e) {
                    postToast(context, "无法调起安装器：" + e.getClass().getSimpleName());
                }
            }
        });
    }

    private static String[] mirrorCandidates(String url) {
        return new String[]{
                url,
                "https://gh-proxy.com/" + url,
                "https://ghfast.top/" + url,
                "https://ghproxy.net/" + url
        };
    }

    private static File downloadOnce(Context context, String url, int code, String versionName,
                                     ProgressListener listener) throws Exception {
        File dir = dir(context);
        File target = apkFile(context, versionName);
        File part = new File(dir, String.format(APK_NAME, versionName) + ".part");
        if (part.exists()) part.delete();
        if (target.exists()) target.delete();

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE
                            + ") SmartCourseLite/" + currentName(context));
            connection.setRequestProperty("Accept", "application/octet-stream");
            int status = connection.getResponseCode();
            if (status != 200) throw new IOException("HTTP " + status);
            int total = connection.getContentLength();
            InputStream in = connection.getInputStream();
            FileOutputStream out = new FileOutputStream(part);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[16384];
            long done = 0;
            int read;
            int lastPct = -1;
            while ((read = in.read(buf)) > 0) {
                if (cancelRequested) {
                    try { out.close(); } catch (Exception ignored) {}
                    try { in.close(); } catch (Exception ignored) {}
                    part.delete();
                    throw new IOException("已取消");
                }
                out.write(buf, 0, read);
                digest.update(buf, 0, read);
                done += read;
                if (total > 0 && listener != null) {
                    int pct = (int) (done * 100 / total);
                    if (pct != lastPct) {
                        lastPct = pct;
                        listener.onProgress(pct, done, total);
                    }
                }
            }
            out.flush();
            out.close();
            in.close();
            if (part.length() < 200 * 1024) throw new IOException("安装包大小异常");
            if (total > 0 && part.length() != total) throw new IOException("下载不完整");
            if (!part.renameTo(target)) throw new IOException("无法写入安装包");
            StringBuilder sha = new StringBuilder();
            for (byte b : digest.digest()) sha.append(String.format("%02x", b));
            markSha(context, code, sha.toString());
            return target;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    interface ProgressListener {
        void onProgress(int percent, long done, long total);
    }

    // ------------------------------------------------------------ 界面小工具（关于页调用）

    /** 「关于此项目」里的检查更新入口：显示状态 + 版本号 + 检查按钮。 */
    public static View buildUpdateRow(final Activity activity, int ink, int muted,
                                      int faint, int cardBg, final int lineColor,
                                      final int greenColor) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14));

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(cardBg);
        bg.setCornerRadius(dp(activity, 14));
        bg.setStroke(Math.max(1, dp(activity, 1)), lineColor);
        box.setBackground(bg);

        LinearLayout top = new LinearLayout(activity);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = new TextView(activity);
        label.setText("检查更新");
        label.setTextSize(12f);
        label.setTextColor(adjustAlpha(ink, 0xCC));
        top.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));

        final TextView status = new TextView(activity);
        status.setTextSize(12f);
        status.setTextColor(greenColor);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        status.setText("v" + currentName(activity) + " ›");
        top.addView(status);
        box.addView(top);

        final TextView hint = new TextView(activity);
        hint.setTextSize(10.5f);
        hint.setTextColor(adjustAlpha(faint, 0xFF));
        hint.setText(hasDownloaded(activity)
                ? "已下载 v" + versionNameOf(downloadedCode(activity)) + "，点此安装"
                : checkSummary(activity));
        hint.setPadding(0, dp(activity, 6), 0, 0);
        if (statusText != null && statusText.length() > 0) {
            hint.setText(statusText);
        }
        statusViewRef = new java.lang.ref.WeakReference<TextView>(hint);
        box.addView(hint);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (hasDownloaded(activity)) {
                    showInstallPrompt(activity, downloadedCode(activity));
                    return;
                }
                hint.setText("正在检查更新…");
                statusViewRef = new java.lang.ref.WeakReference<TextView>(hint);
                checkManually(activity, new Runnable() {
                    @Override public void run() {
                        hint.setText(checkSummary(activity));
                    }
                });
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override public void run() {
                        if (hasDownloaded(activity)) {
                            hint.setText("已下载 v" + versionNameOf(downloadedCode(activity)) + "，点此安装");
                        } else if ("正在检查更新…".equals(hint.getText().toString())) {
                            hint.setText(checkSummary(activity));
                        }
                    }
                }, 12000L);
            }
        };
        box.setOnClickListener(listener);
        // 点击态
        box.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    v.animate().alpha(0.6f).setDuration(90).start();
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                        || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    v.animate().alpha(1f).setDuration(160).start();
                }
                return false;
            }
        });
        return box;
    }

    private static int adjustAlpha(int color, int alpha) {
        return android.graphics.Color.argb(alpha,
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color));
    }

    private static int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    // ------------------------------------------------------------ 线程工具

    private static void post(Activity activity, Runnable runnable) {
        if (activity == null) return;
        activity.runOnUiThread(runnable);
    }

    private static void postToast(final Context context, final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() { toast(context, text); }
        });
    }

    static void toast(Context context, String text) {
        try {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
        }
    }

}
