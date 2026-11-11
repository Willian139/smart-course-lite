package com.zuwe.smartcourselite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * UiPolish v2 — 全局动效系统。
 * 所有入场动画用 OnPreDrawListener 保证在布局完成后才播放，避免“动画跑完了视图还没画出来”。
 */
public final class UiPolish {

    // M3 motion tokens（emphasized 家族）：入场减速、离场加速、常态强调
    static final PathInterpolator EASE_OUT = new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);
    static final PathInterpolator EASE_IN_OUT = new PathInterpolator(0.2f, 0f, 0f, 1f);
    private static final PathInterpolator PREMIUM_EASE = new PathInterpolator(0.2f, 0f, 0f, 1f);
    static final PathInterpolator OVERSHOOT = new PathInterpolator(0.34f, 1.3f, 0.64f, 1f);
    private static final long DUR_SHORT = 180L;
    private static final long DUR_MED = 380L;
    private static final long DUR_LONG = 600L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private UiPolish() {}

    // ===== 按压反馈 =====

    static final class Press implements View.OnTouchListener {
        @Override public boolean onTouch(View view, MotionEvent e) {
            boolean down = e.getActionMasked() == MotionEvent.ACTION_DOWN;
            float scale = down ? 0.97f : 1f;
            long dur = down ? DUR_SHORT : 260L;
            view.animate().scaleX(scale).scaleY(scale).setDuration(dur)
                    .setInterpolator(down ? EASE_IN_OUT : EASE_OUT).start();
            return false;
        }
    }

    public static void pressable(View v) {
        if (v != null) v.setOnTouchListener(new Press());
    }

    public static void ripple(View v, int color) {
        if (v == null) return;
        Drawable bg = v.getBackground();
        int pl = v.getPaddingLeft(), pt = v.getPaddingTop(), pr = v.getPaddingRight(), pb = v.getPaddingBottom();
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(color), bg, null));
        v.setPadding(pl, pt, pr, pb);
    }

    // ===== Stagger 错开级联入场 (45ms 间隔，布局后播放) =====

    /**
     * 单个视图滑入淡入。index 控制延迟（45ms × index）。
     * 用 OnPreDrawListener 确保布局完成后才播放，解决缓存态动画不可见问题。
     */
    public static void staggerIn(final View view, final int index) {
        if (view == null) return;
        view.animate().cancel();
        final float density = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setTranslationX(0f);
        view.setTranslationY(14f * density);
        view.setScaleX(0.97f);
        view.setScaleY(0.97f);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.animate().cancel();
                view.animate()
                        .alpha(1f).translationX(0f).translationY(0f).scaleX(1f).scaleY(1f)
                        .setStartDelay(Math.min(index, 12) * 30L)
                        .setDuration(DUR_LONG)
                        .setInterpolator(EASE_OUT)
                        .start();
                return true;
            }
        });
    }

    /** 仿「关于此项目」的左侧滑入 + 明显回弹。index 控制级联延迟。 */
    static final PathInterpolator BOUNCE = new PathInterpolator(0.16f, 1.65f, 0.38f, 1f);
    private static final long FLY_DURATION = 700L;
    private static final long FLY_STEP = 120L;

    public static void flyIn(final View view, final int index) {
        if (view == null) return;
        view.animate().cancel();
        final float density = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setTranslationX(-110f * density);
        view.setTranslationY(0f);
        view.setScaleX(0.86f);
        view.setScaleY(0.86f);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.animate().cancel();
                view.animate()
                        .alpha(1f).translationX(0f).scaleX(1f).scaleY(1f)
                        .setStartDelay(Math.min(index, 12) * FLY_STEP)
                        .setDuration(FLY_DURATION)
                        .setInterpolator(BOUNCE)
                        .start();
                return true;
            }
        });
    }

    /**
     * 级联入场整组子视图。用 OnPreDrawListener 在布局后逐个播放。
     */
    public static void staggerGroup(final ViewGroup group) {
        if (group == null) return;
        final int n = group.getChildCount();
        final float density = group.getResources().getDisplayMetrics().density;
        for (int i = 0; i < n; i++) {
            View child = group.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(14f * density);
            child.setScaleX(0.97f);
            child.setScaleY(0.97f);
        }
        group.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                group.getViewTreeObserver().removeOnPreDrawListener(this);
                for (int i = 0; i < n; i++) {
                    final View child = group.getChildAt(i);
                    final int idx = i;
                    child.animate()
                            .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                            .setStartDelay(Math.min(idx, 10) * 30L)
                            .setDuration(DUR_LONG)
                            .setInterpolator(EASE_OUT)
                            .start();
                }
                return true;
            }
        });
    }

    public static void slideInHoriz(View view, int dir, int delay) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        view.animate().cancel();
        view.setAlpha(0f);
        view.setTranslationX(24f * density * dir);
        view.setScaleX(0.97f);
        view.setScaleY(0.97f);
        view.animate().alpha(1f).translationX(0f).scaleX(1f).scaleY(1f)
                .setStartDelay(delay).setDuration(DUR_LONG).setInterpolator(EASE_OUT).start();
    }

    // ===== 页面/容器过渡：缩放+淡入 =====

    /**
     * 整个容器从 0.92 缩放 + 淡入，模拟 Container Transform 的“展开”感。
     */
    public static void containerExpand(final View container) {
        if (container == null) return;
        container.animate().cancel();
        container.setAlpha(0f);
        container.setScaleX(0.97f);
        container.setScaleY(0.97f);
        container.setTranslationY(12f * container.getResources().getDisplayMetrics().density);
        container.setPivotY(0f);
        container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                container.getViewTreeObserver().removeOnPreDrawListener(this);
                container.animate().cancel();
                container.animate()
                        .alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
                        .setDuration(DUR_LONG)
                        .setInterpolator(EASE_OUT)
                        .start();
                return true;
            }
        });
    }

    /**
     * 点击源视图 → 目标 Activity 的过渡。用 overridePendingTransition + 源视图缩放。
     */
    public static void containerTransform(final View source, final Runnable launchActivity) {
        if (source == null) {
            launchActivity.run();
            return;
        }
        source.animate()
                .scaleX(0.96f).scaleY(0.96f).alpha(0.85f)
                .setDuration(120L).setInterpolator(EASE_IN_OUT)
                .withEndAction(() -> {
                    launchActivity.run();
                    source.animate().scaleX(1f).scaleY(1f).alpha(1f)
                            .setDuration(220L).setStartDelay(40L)
                            .setInterpolator(EASE_OUT).start();
                })
                .start();
    }

    // ===== 表面 Surface =====

    /** 即存风格表面：纯色圆角卡片 + 极细描边，不用渐变/阴影。 */
    public static void glass(View view, int cardBg, int lineColor, float cornerDp) {
        if (view == null) return;
        final float d = view.getResources().getDisplayMetrics().density;
        final int r = Math.round(cornerDp * d);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardBg);
        bg.setCornerRadius(r);
        bg.setStroke(Math.max(1, Math.round(1 * d)), lineColor);
        view.setBackground(bg);
        view.setElevation(0f);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), r);
            }
        });
        view.setClipToOutline(true);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    // ===== 骨架屏 Skeleton + Shimmer =====

    public static Runnable skeleton(ViewGroup container, int count, int skelBg, int shimmerColor, int lineColor) {
        if (container == null) return () -> {};
        container.removeAllViews();
        float d = container.getResources().getDisplayMetrics().density;
        List<View> shimmerBars = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LinearLayout card = new LinearLayout(container.getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(Math.round(16 * d), Math.round(16 * d), Math.round(16 * d), Math.round(16 * d));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(skelBg);
            bg.setCornerRadius(Math.round(16 * d));
            bg.setStroke(Math.round(d), lineColor);
            card.setBackground(bg);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
            cardLp.topMargin = i == 0 ? 0 : Math.round(10 * d);
            card.addView(skelBar(container.getContext(), 160 * d, shimmerColor, shimmerBars));
            card.setElevation(0f);
            card.addView(skelBar(container.getContext(), 220 * d, shimmerColor, shimmerBars), lpTop(8 * d));
            card.addView(skelBar(container.getContext(), 100 * d, shimmerColor, shimmerBars), lpTop(8 * d));
            container.addView(card, cardLp);
        }
        ShimmerController ctrl = new ShimmerController(shimmerBars);
        ctrl.start();
        return () -> { ctrl.stop(); container.removeAllViews(); };
    }

    private static LinearLayout.LayoutParams lpTop(float dp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.topMargin = Math.round(dp);
        return lp;
    }

    private static View skelBar(Context ctx, float widthDp, int shimmerColor, List<View> bars) {
        float d = ctx.getResources().getDisplayMetrics().density;
        FrameLayout frame = new FrameLayout(ctx);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(0x33FFFFFF);
        barBg.setCornerRadius(Math.round(4 * d));
        frame.setBackground(barBg);
        View shimmer = new View(ctx);
        int shimmerW = Math.round(100 * d);
        FrameLayout.LayoutParams sLp = new FrameLayout.LayoutParams(shimmerW, -1);
        shimmer.setLayoutParams(sLp);
        GradientDrawable shDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ 0x00FFFFFF, shimmerColor, 0x00FFFFFF });
        shDrawable.setCornerRadius(Math.round(4 * d));
        shimmer.setBackground(shDrawable);
        shimmer.setX(-shimmerW);
        frame.addView(shimmer);
        bars.add(shimmer);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Math.round(widthDp), Math.round(14 * d));
        return frame;
    }

    static final class ShimmerController {
        final List<View> bars;
        boolean running;

        ShimmerController(List<View> bars) { this.bars = bars; }

        void start() {
            if (running) return;
            running = true;
            MAIN.post(loop);
        }

        private final Runnable loop = new Runnable() {
            long startTs;
            @Override public void run() {
                if (!running) return;
                if (startTs == 0) startTs = System.currentTimeMillis();
                float t = ((System.currentTimeMillis() - startTs) % 1400L) / 1400f;
                for (View bar : bars) {
                    View parent = (View) bar.getParent();
                    if (parent == null || parent.getWidth() == 0) continue;
                    int travel = parent.getWidth() + bar.getWidth();
                    bar.setTranslationX(-bar.getWidth() + t * travel);
                }
                MAIN.postDelayed(this, 16);
            }
        };

        void stop() {
            running = false;
            MAIN.removeCallbacks(loop);
        }
    }

    // ===== 手风琴 Accordion（高度动画） =====

    public static void accordion(final View view, final boolean expand) {
        if (view == null) return;
        if (expand) {
            view.setVisibility(View.VISIBLE);
            view.measure(View.MeasureSpec.makeMeasureSpec(
                            ((View) view.getParent()).getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            final int targetH = view.getMeasuredHeight();
            if (targetH <= 0) {
                view.getLayoutParams().height = -2;
                return;
            }
            view.getLayoutParams().height = 0;
            view.requestLayout();
            ValueAnimator anim = ValueAnimator.ofInt(0, targetH);
            anim.setDuration(320L);
            anim.setInterpolator(EASE_IN_OUT);
            anim.addUpdateListener(a -> {
                view.getLayoutParams().height = (int) a.getAnimatedValue();
                view.requestLayout();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator a) {
                    view.getLayoutParams().height = -2;
                    view.requestLayout();
                }
            });
            anim.start();
        } else {
            final int startH = view.getHeight();
            if (startH <= 0) { view.setVisibility(View.GONE); return; }
            ValueAnimator anim = ValueAnimator.ofInt(startH, 0);
            anim.setDuration(280L);
            anim.setInterpolator(EASE_IN_OUT);
            anim.addUpdateListener(a -> {
                view.getLayoutParams().height = (int) a.getAnimatedValue();
                view.requestLayout();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator a) {
                    view.setVisibility(View.GONE);
                    view.getLayoutParams().height = -2;
                    view.requestLayout();
                }
            });
            anim.start();
        }
    }

    // ===== 视差 Parallax =====

    public static void parallax(final ScrollView scroll, final View[] backgroundLayers, final float[] factors) {
        if (scroll == null) return;
        scroll.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override public void onScrollChanged() {
                        int sy = scroll.getScrollY();
                        for (int i = 0; i < backgroundLayers.length; i++) {
                            View v = backgroundLayers[i];
                            if (v == null) continue;
                            float f = factors.length > i ? factors[i] : 0.5f;
                            v.setTranslationY(sy * f);
                        }
                    }
                });
    }

    // ===== 签到成功弹窗 =====

    public static void successPopup(final Activity activity, final String title, final String subtitle, final int accentColor) {
        activity.runOnUiThread(() -> {
            final Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
            dialog.getWindow().setDimAmount(0.4f);

            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            float d = activity.getResources().getDisplayMetrics().density;
            card.setPadding(Math.round(40 * d), Math.round(36 * d), Math.round(40 * d), Math.round(30 * d));

            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(0xF2202018);
            cardBg.setCornerRadius(Math.round(28 * d));
            cardBg.setStroke(Math.round(2f * d), UiPolish.withAlpha(accentColor, 140));
            card.setBackground(cardBg);
            card.setElevation(0f);

            CheckCircleView check = new CheckCircleView(activity, accentColor);
            int checkSize = Math.round(80 * d);
            LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(checkSize, checkSize);
            card.addView(check, checkLp);

            TextView titleView = new TextView(activity);
            titleView.setText(title);
            titleView.setTextSize(22f);
            titleView.setTextColor(0xFFFFFFFF);
            titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            titleView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(-2, -2);
            tLp.topMargin = Math.round(16 * d);
            card.addView(titleView, tLp);

            if (subtitle != null && !subtitle.isEmpty()) {
                TextView sub = new TextView(activity);
                sub.setText(subtitle);
                sub.setTextSize(13f);
                sub.setTextColor(0xCCFFFFFF);
                sub.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(-2, -2);
                sLp.topMargin = Math.round(8 * d);
                card.addView(sub, sLp);
            }

            FrameLayout root = new FrameLayout(activity);
            FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(-2, -2);
            cardLp.gravity = Gravity.CENTER;
            root.addView(card, cardLp);
            root.setBackgroundColor(0x00000000);

            dialog.setContentView(root);
            dialog.setCancelable(false);
            dialog.show();

            card.setAlpha(0f);
            card.setScaleX(0.6f);
            card.setScaleY(0.6f);
            card.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(400L).setInterpolator(OVERSHOOT).start();
            check.animateDraw();

            MAIN.postDelayed(() -> {
                card.animate().alpha(0f).scaleX(0.85f).scaleY(0.85f)
                        .setDuration(300L).setInterpolator(EASE_IN_OUT)
                        .withEndAction(() -> { if (dialog.isShowing()) dialog.dismiss(); })
                        .start();
            }, 2200L);
        });
    }

    // ===== 自绘勾号圆 =====

    static final class CheckCircleView extends View {
        private final Paint circlePaint;
        private final Paint checkPaint;
        private float progress = 0f;

        CheckCircleView(Context ctx, int accent) {
            super(ctx);
            float d = ctx.getResources().getDisplayMetrics().density;
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(4f * d);
            circlePaint.setColor(accent);
            checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            checkPaint.setStyle(Paint.Style.STROKE);
            checkPaint.setStrokeWidth(5f * d);
            checkPaint.setStrokeCap(Paint.Cap.ROUND);
            checkPaint.setStrokeJoin(Paint.Join.ROUND);
            checkPaint.setColor(0xFFFFFFFF);
        }

        void animateDraw() {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(600L);
            animator.setStartDelay(250L);
            animator.setInterpolator(new DecelerateInterpolator(1.5f));
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            float cx = w / 2f, cy = h / 2f;
            float r = Math.min(w, h) / 2f - circlePaint.getStrokeWidth();
            android.graphics.RectF oval = new android.graphics.RectF(cx - r, cy - r, cx + r, cy + r);
            float sweep = Math.min(progress * 2f, 1f) * 360f;
            canvas.drawArc(oval, -90f, sweep, false, circlePaint);
            float checkProgress = Math.max(0f, (progress - 0.4f) / 0.6f);
            if (checkProgress > 0) {
                float scale = r / 30f;
                Path path = new Path();
                path.moveTo(cx - 9f * scale, cy + 0f * scale);
                path.lineTo(cx - 2f * scale, cy + 7f * scale);
                path.lineTo(cx + 9f * scale, cy - 7f * scale);
                android.graphics.PathMeasure pm = new android.graphics.PathMeasure(path, false);
                Path dst = new Path();
                pm.getSegment(0, pm.getLength() * checkProgress, dst, true);
                canvas.drawPath(dst, checkPaint);
            }
        }
    }
}
