package com.zuwe.smartcourselite;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/* loaded from: classes.dex */
public final class TodoFx {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final PathInterpolator EASE_OUT = new PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f);
    private static final PathInterpolator SPRING = new PathInterpolator(0.18f, 1.08f, 0.32f, 1.0f);

    private TodoFx() {
    }

    public static void buzz(final View view) {
        if (view == null) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            MAIN.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.TodoFx.1
                @Override // java.lang.Runnable
                public void run() {
                    view.performHapticFeedback(1);
                }
            }, i * 55);
        }
    }

    public static void notice(Activity activity, String str, int i, int i2, int i3) {
        final FrameLayout overlayOf = overlayOf(activity);
        if (overlayOf == null) {
            return;
        }
        final float f = activity.getResources().getDisplayMetrics().density;
        final TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(13.5f);
        textView.setTextColor(i);
        textView.setGravity(17);
        float f2 = 18.0f * f;
        float f3 = 11.0f * f;
        textView.setPadding(Math.round(f2), Math.round(f3), Math.round(f2), Math.round(f3));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(22.0f * f);
        gradientDrawable.setColor(UiPolish.withAlpha(i2, 244));
        gradientDrawable.setStroke(Math.max(1, Math.round(f)), UiPolish.withAlpha(i3, 204));
        textView.setBackground(gradientDrawable);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 81);
        layoutParams.bottomMargin = Math.round(132.0f * f);
        overlayOf.addView(textView, layoutParams);
        textView.setAlpha(0.0f);
        textView.setScaleX(0.88f);
        textView.setScaleY(0.88f);
        textView.setTranslationY(14.0f * f);
        textView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).setDuration(300L).setInterpolator(SPRING).start();
        MAIN.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.TodoFx.2
            @Override // java.lang.Runnable
            public void run() {
                textView.animate().alpha(0.0f).translationY(f * 10.0f).setDuration(320L).setInterpolator(TodoFx.EASE_OUT).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.TodoFx.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        TodoFx.remove(overlayOf);
                    }
                }).start();
            }
        }, 2000L);
    }

    public static void allDone(Activity activity, int i, int i2, int i3, int i4) {
        final FrameLayout overlayOf = overlayOf(activity);
        if (overlayOf == null) {
            return;
        }
        final float f = activity.getResources().getDisplayMetrics().density;
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(17);
        float f2 = 26.0f * f;
        float f3 = 22.0f * f;
        linearLayout.setPadding(Math.round(f2), Math.round(f3), Math.round(f2), Math.round(20.0f * f));
        linearLayout.setMinimumWidth(Math.round(232.0f * f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(f3);
        gradientDrawable.setColor(UiPolish.withAlpha(i3, 244));
        gradientDrawable.setStroke(Math.max(1, Math.round(f)), UiPolish.withAlpha(i4, 204));
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(activity);
        textView.setText("今日待办已完成 😊");
        textView.setTextSize(16.0f);
        textView.setTextColor(i);
        textView.setTypeface(Typeface.create("sans-serif", 1));
        textView.setGravity(17);
        textView.setSingleLine(false);
        textView.setLineSpacing(Math.round(3.0f * f), 1.0f);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText("明天继续加油哦！");
        textView2.setTextSize(13.0f);
        textView2.setTextColor(i2);
        textView2.setGravity(17);
        textView2.setSingleLine(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = Math.round(6.0f * f);
        linearLayout.addView(textView2, layoutParams);
        overlayOf.addView(linearLayout, new FrameLayout.LayoutParams(-2, -2, 17));
        linearLayout.setAlpha(0.0f);
        linearLayout.setScaleX(0.92f);
        linearLayout.setScaleY(0.92f);
        linearLayout.setTranslationY(10.0f * f);
        linearLayout.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).setDuration(420L).setInterpolator(SPRING).start();
        linearLayout.performHapticFeedback(6);
        MAIN.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.TodoFx.3
            @Override // java.lang.Runnable
            public void run() {
                linearLayout.animate().alpha(0.0f).translationY(f * (-12.0f)).setDuration(360L).setInterpolator(TodoFx.EASE_OUT).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.TodoFx.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        TodoFx.remove(overlayOf);
                    }
                }).start();
            }
        }, 2200L);
    }

    private static FrameLayout overlayOf(Activity activity) {
        try {
            View decorView = activity.getWindow().getDecorView();
            if (!(decorView instanceof ViewGroup)) {
                return null;
            }
            FrameLayout frameLayout = new FrameLayout(activity);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.setClickable(false);
            frameLayout.setFocusable(false);
            frameLayout.setImportantForAccessibility(2);
            ((ViewGroup) decorView).addView(frameLayout);
            return frameLayout;
        } catch (Exception unused) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void remove(View view) {
        try {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(view);
            }
        } catch (Exception unused) {
        }
    }
}
