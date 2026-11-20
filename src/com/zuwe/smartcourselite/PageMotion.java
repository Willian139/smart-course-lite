package com.zuwe.smartcourselite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.PathInterpolator;

/* loaded from: classes.dex */
public final class PageMotion {
    private static final PathInterpolator EASE_OUT = Motion.ENTER;
    private static Bitmap backdrop;

    private PageMotion() {
    }

    public static void captureBackdrop(Activity activity) {
        try {
            View decorView = activity.getWindow().getDecorView();
            int width = decorView.getWidth() / 2;
            int height = decorView.getHeight() / 2;
            if (width > 0 && height > 0) {
                Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                canvas.scale(0.5f, 0.5f);
                decorView.draw(canvas);
                backdrop = createBitmap;
                return;
            }
            backdrop = null;
        } catch (Throwable unused) {
            backdrop = null;
        }
    }

    public static void installBackdrop(Activity activity, int i) {
        try {
            if (backdrop != null) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(), backdrop);
                bitmapDrawable.setGravity(119);
                activity.getWindow().setBackgroundDrawable(bitmapDrawable);
                backdrop = null;
                return;
            }
            activity.getWindow().setBackgroundDrawable(new ColorDrawable(i));
        } catch (Throwable unused) {
        }
    }

    public static void clearWindowBackground(Activity activity) {
        try {
            activity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        } catch (Throwable unused) {
        }
    }

    public static void fromTop(final View view, final long j) {
        view.setTranslationY(-view.getResources().getDisplayMetrics().heightPixels);
        view.post(new Runnable() { // from class: com.zuwe.smartcourselite.PageMotion.1
            @Override // java.lang.Runnable
            public void run() {
                view.animate().translationY(0.0f).setDuration(j).setInterpolator(Motion.ENTER).start();
            }
        });
    }

    public static void fromRight(final View view, final long j) {
        view.setTranslationX(view.getResources().getDisplayMetrics().widthPixels);
        view.post(new Runnable() { // from class: com.zuwe.smartcourselite.PageMotion.2
            @Override // java.lang.Runnable
            public void run() {
                view.animate().translationX(0.0f).setDuration(j).setInterpolator(Motion.ENTER).start();
            }
        });
    }

    public static void fromBottom(final View view, final long j) {
        view.setTranslationY(view.getResources().getDisplayMetrics().heightPixels * 0.55f);
        view.setAlpha(0.3f);
        view.post(new Runnable() { // from class: com.zuwe.smartcourselite.PageMotion.3
            @Override // java.lang.Runnable
            public void run() {
                view.animate().translationY(0.0f).alpha(1.0f).setDuration(j).setInterpolator(Motion.ENTER).start();
            }
        });
    }
}
