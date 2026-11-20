package com.zuwe.smartcourselite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/* loaded from: classes.dex */
public final class AlarmOverlay {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static Runnable autoDismiss;
    private static WindowManager manager;
    private static View view;

    private AlarmOverlay() {
    }

    public static boolean canDraw(Context context) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                return true;
            }
            return Settings.canDrawOverlays(context);
        } catch (Exception unused) {
            return false;
        }
    }

    public static void request(Activity activity) {
        try {
            try {
                Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            } catch (Exception unused) {
            }
        } catch (Exception unused2) {
            activity.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION"));
        }
    }

    public static boolean showing() {
        return view != null;
    }

    public static boolean show(final Context context, String str, String str2) {
        if (!canDraw(context)) {
            return false;
        }
        try {
            dismiss();
            WindowManager windowManager = (WindowManager) context.getSystemService("window");
            manager = windowManager;
            if (windowManager == null) {
                return false;
            }
            int i = (int) context.getResources().getDisplayMetrics().density;
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundColor(-15986931);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(1);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2, 17);
            int i2 = i * 24;
            layoutParams.leftMargin = i2;
            layoutParams.rightMargin = i2;
            frameLayout.addView(linearLayout, layoutParams);
            TextView textView = new TextView(context);
            textView.setText("闹 钟");
            textView.setTextSize(12.0f);
            textView.setTextColor(-8615297);
            textView.setLetterSpacing(0.4f);
            linearLayout.addView(textView);
            TextView textView2 = new TextView(context);
            textView2.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            textView2.setTextSize(64.0f);
            textView2.setTextColor(-854543);
            textView2.setTypeface(Typeface.create("sans-serif-light", 0));
            linearLayout.addView(textView2);
            final TextView textView3 = new TextView(context);
            textView3.setText(str);
            textView3.setTextSize(20.0f);
            textView3.setTextColor(-854543);
            textView3.setGravity(17);
            int i3 = i * 8;
            textView3.setPadding(0, i3, 0, 0);
            linearLayout.addView(textView3);
            final TextView textView4 = new TextView(context);
            if (str2 == null) {
                str2 = "";
            }
            textView4.setText(str2);
            textView4.setTextSize(14.0f);
            textView4.setTextColor(-5655124);
            textView4.setGravity(17);
            textView4.setPadding(0, i3, 0, 0);
            linearLayout.addView(textView4);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(0);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = i * 28;
            linearLayout.addView(linearLayout2, layoutParams2);
            TextView action = action(context, "稍后 5 分钟", false, i);
            linearLayout2.addView(action, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView action2 = action(context, "我知道了", true, i);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams3.leftMargin = i * 10;
            linearLayout2.addView(action2, layoutParams3);
            action2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AlarmOverlay.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    AlarmOverlay.finish(context);
                }
            });
            action.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AlarmOverlay.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    try {
                        ReminderScheduler.scheduleSnooze(context, 5, textView3.getText().toString(), textView4.getText().toString());
                    } catch (Exception unused) {
                    }
                    AlarmOverlay.finish(context);
                }
            });
            WindowManager.LayoutParams layoutParams4 = new WindowManager.LayoutParams(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002, -1, -1, 896, -3);
            layoutParams4.gravity = 8388659;
            manager.addView(frameLayout, layoutParams4);
            view = frameLayout;
            Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.AlarmOverlay.3
                @Override // java.lang.Runnable
                public void run() {
                    AlarmOverlay.finish(context);
                }
            };
            autoDismiss = runnable;
            HANDLER.postDelayed(runnable, 120000L);
            return true;
        } catch (Exception e) {
            ReminderPrefs.pushLog(context, "悬浮窗闹钟失败：" + e.getClass().getSimpleName());
            dismiss();
            return false;
        }
    }

    private static TextView action(Context context, String str, boolean z, int i) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextSize(15.0f);
        textView.setGravity(17);
        int i2 = i * 15;
        textView.setPadding(0, i2, 0, i2);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(i * 16);
        if (z) {
            gradientDrawable.setColor(-6559564);
            textView.setTextColor(-15986931);
        } else {
            gradientDrawable.setColor(0);
            gradientDrawable.setStroke(Math.max(1, i), -12959173);
            textView.setTextColor(-2695465);
        }
        textView.setBackground(gradientDrawable);
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void finish(Context context) {
        try {
            AlarmTone.stop();
        } catch (Exception unused) {
        }
        try {
            ReminderPrefs.markStopped(context);
            ReminderNotifier.cancelAll(context);
        } catch (Exception unused2) {
        }
        dismiss();
    }

    public static void dismiss() {
        View view2;
        try {
            Runnable runnable = autoDismiss;
            if (runnable != null) {
                HANDLER.removeCallbacks(runnable);
                autoDismiss = null;
            }
        } catch (Exception unused) {
        }
        try {
            WindowManager windowManager = manager;
            if (windowManager != null && (view2 = view) != null) {
                windowManager.removeViewImmediate(view2);
            }
        } catch (Exception unused2) {
        }
        view = null;
        manager = null;
    }
}
