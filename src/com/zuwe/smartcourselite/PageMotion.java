package com.zuwe.smartcourselite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.PathInterpolator;

/**
 * 页面切换的平移动画：新页面从某个方向整体滑入，旧页面留在下面透出来。
 * 目标 Activity 需要把自己 window 背景设成透明，否则滑动过程中露出的是窗口底色。
 */
public final class PageMotion {

    private static final PathInterpolator EASE_OUT = new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);

    /** 上一页的截图，用作新页面滑动时的底图，避免露出黑屏。 */
    private static Bitmap backdrop;

    private PageMotion() { }

    /** 启动新页面前：把当前页面截一张（半分辨率）留作底图。 */
    public static void captureBackdrop(Activity activity) {
        try {
            View decor = activity.getWindow().getDecorView();
            int w = decor.getWidth() / 2;
            int h = decor.getHeight() / 2;
            if (w <= 0 || h <= 0) { backdrop = null; return; }
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.scale(0.5f, 0.5f);
            decor.draw(canvas);
            backdrop = bmp;
        } catch (Throwable t) {
            backdrop = null;
        }
    }

    /** 新页面把窗口底图画成上一页截图；没有截图时退回纯色，绝不露黑。 */
    public static void installBackdrop(Activity activity, int fallbackColor) {
        try {
            if (backdrop != null) {
                BitmapDrawable d = new BitmapDrawable(activity.getResources(), backdrop);
                d.setGravity(android.view.Gravity.FILL);
                activity.getWindow().setBackgroundDrawable(d);
                backdrop = null;
            } else {
                activity.getWindow().setBackgroundDrawable(new ColorDrawable(fallbackColor));
            }
        } catch (Throwable ignored) {
        }
    }

    /** 兼容旧调用：透明窗口。 */
    public static void clearWindowBackground(Activity activity) {
        try {
            activity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        } catch (Throwable ignored) {
        }
    }

    /** 从屏幕上方滑下来。 */
    public static void fromTop(final View root, long duration) {
        final float h = root.getResources().getDisplayMetrics().heightPixels;
        root.setTranslationY(-h);
        root.post(new Runnable() {
            @Override public void run() {
                root.animate().translationY(0f).setDuration(duration)
                        .setInterpolator(EASE_OUT).start();
            }
        });
    }

    /** 从屏幕右侧推入。 */
    public static void fromRight(final View root, long duration) {
        final float w = root.getResources().getDisplayMetrics().widthPixels;
        root.setTranslationX(w);
        root.post(new Runnable() {
            @Override public void run() {
                root.animate().translationX(0f).setDuration(duration)
                        .setInterpolator(EASE_OUT).start();
            }
        });
    }

    /** 从屏幕下方升起。 */
    public static void fromBottom(final View root, long duration) {
        final float h = root.getResources().getDisplayMetrics().heightPixels;
        root.setTranslationY(h * 0.55f);
        root.setAlpha(0.3f);
        root.post(new Runnable() {
            @Override public void run() {
                root.animate().translationY(0f).alpha(1f).setDuration(duration)
                        .setInterpolator(EASE_OUT).start();
            }
        });
    }
}
