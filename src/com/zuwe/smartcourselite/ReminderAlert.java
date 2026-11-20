package com.zuwe.smartcourselite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/* loaded from: classes.dex */
public final class ReminderAlert {
    private ReminderAlert() {
    }

    public static void show(final Activity activity, final String str, final String str2, final String str3, final int i, final int i2, final int i3, final int i4, final int i5) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(activity, 22), dp(activity, 22), dp(activity, 22), dp(activity, 14));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i3);
        gradientDrawable.setCornerRadius(dp(activity, 22));
        gradientDrawable.setStroke(Math.max(1, dp(activity, 1)), i4);
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(11.0f);
        textView.setTextColor(i5);
        textView.setLetterSpacing(0.08f);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText(str2);
        textView2.setTextSize(20.0f);
        textView2.setTextColor(i);
        textView2.setTypeface(Typeface.DEFAULT, 1);
        textView2.setPadding(0, dp(activity, 8), 0, 0);
        linearLayout.addView(textView2);
        if (str3 != null && !str3.isEmpty()) {
            TextView textView3 = new TextView(activity);
            textView3.setText(str3);
            textView3.setTextSize(13.0f);
            textView3.setTextColor(i2);
            textView3.setPadding(0, dp(activity, 6), 0, 0);
            linearLayout.addView(textView3);
        }
        final Ringtone[] ringtoneArr = new Ringtone[1];
        final Vibrator vibrator = (Vibrator) activity.getSystemService("vibrator");
        LinearLayout linearLayout2 = new LinearLayout(activity);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(8388613);
        linearLayout2.setPadding(0, dp(activity, 14), 0, 0);
        TextView textView4 = new TextView(activity);
        textView4.setText("稍后 5 分钟");
        textView4.setTextSize(13.0f);
        textView4.setTextColor(i2);
        textView4.setPadding(dp(activity, 10), dp(activity, 10), dp(activity, 10), dp(activity, 10));
        linearLayout2.addView(textView4);
        TextView textView5 = new TextView(activity);
        textView5.setText("知道了");
        textView5.setTextSize(13.0f);
        textView5.setTextColor(i5);
        textView5.setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 6), dp(activity, 10));
        linearLayout2.addView(textView5);
        linearLayout.addView(linearLayout2);
        final AlertDialog create = new AlertDialog.Builder(activity).setView(linearLayout).create();
        if (create.getWindow() != null) {
            create.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        startRingtone(activity, ringtoneArr);
        startVibration(vibrator);
        final Runnable runnable = new Runnable() { // from class: com.zuwe.smartcourselite.ReminderAlert.1
            @Override // java.lang.Runnable
            public void run() {
                ReminderAlert.stopRingtone(ringtoneArr[0]);
                Vibrator vibrator2 = vibrator;
                if (vibrator2 != null) {
                    vibrator2.cancel();
                }
            }
        };
        create.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.zuwe.smartcourselite.ReminderAlert$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                runnable.run();
            }
        });
        textView5.setOnClickListener(new ReminderAlert$$ExternalSyntheticLambda0(create));
        textView4.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.ReminderAlert$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReminderAlert.lambda$show$2(create, activity, str, str2, str3, i, i2, i3, i4, i5, view);
            }
        });
        create.show();
    }

    static /* synthetic */ void lambda$show$2(AlertDialog alertDialog, Activity activity, String str, String str2, String str3, int i, int i2, int i3, int i4, int i5, View view) {
        alertDialog.dismiss();
        postAgain(activity, str, str2, str3, 300000L, i, i2, i3, i4, i5);
    }

    public static void confirm(Activity activity, String str, String str2, int i, int i2, int i3, int i4, int i5) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(activity, 22), dp(activity, 20), dp(activity, 22), dp(activity, 20));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i3);
        gradientDrawable.setCornerRadius(dp(activity, 20));
        gradientDrawable.setStroke(Math.max(1, dp(activity, 1)), i4);
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(activity);
        textView.setText("提醒已创建");
        textView.setTextSize(11.0f);
        textView.setTextColor(i5);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText(str);
        textView2.setTextSize(17.0f);
        textView2.setTextColor(i);
        textView2.setTypeface(Typeface.DEFAULT, 1);
        textView2.setPadding(0, dp(activity, 6), 0, 0);
        linearLayout.addView(textView2);
        if (str2 != null && !str2.isEmpty()) {
            TextView textView3 = new TextView(activity);
            textView3.setText(str2);
            textView3.setTextSize(12.0f);
            textView3.setTextColor(i2);
            textView3.setPadding(0, dp(activity, 4), 0, 0);
            linearLayout.addView(textView3);
        }
        final AlertDialog create = new AlertDialog.Builder(activity).setView(linearLayout).create();
        if (create.getWindow() != null) {
            create.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        linearLayout.setOnClickListener(new ReminderAlert$$ExternalSyntheticLambda0(create));
        create.show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.ReminderAlert.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (create.isShowing()) {
                        create.dismiss();
                    }
                } catch (Exception unused) {
                }
            }
        }, 1800L);
    }

    private static void postAgain(final Activity activity, final String str, final String str2, final String str3, long j, final int i, final int i2, final int i3, final int i4, final int i5) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.ReminderAlert.3
            @Override // java.lang.Runnable
            public void run() {
                if (activity.isFinishing()) {
                    return;
                }
                ReminderAlert.show(activity, str, str2, str3, i, i2, i3, i4, i5);
            }
        }, j);
    }

    private static void startRingtone(Context context, Ringtone[] ringtoneArr) {
        try {
            Uri defaultUri = RingtoneManager.getDefaultUri(4);
            if (defaultUri == null) {
                defaultUri = RingtoneManager.getDefaultUri(2);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, defaultUri);
            if (ringtone == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 28) {
                ringtone.setLooping(true);
            }
            ringtone.setAudioAttributes(new AudioAttributes.Builder().setUsage(4).setContentType(4).build());
            ringtone.play();
            ringtoneArr[0] = ringtone;
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void stopRingtone(Ringtone ringtone) {
        if (ringtone != null) {
            try {
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
            } catch (Exception unused) {
            }
        }
    }

    private static void startVibration(Vibrator vibrator) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        long[] jArr = {0, 600, 240, 600, 240, 600};
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(jArr, -1));
            } else {
                vibrator.vibrate(jArr, -1);
            }
        } catch (Exception unused) {
        }
    }

    private static int dp(Context context, int i) {
        return Math.round(TypedValue.applyDimension(1, i, context.getResources().getDisplayMetrics()));
    }
}
