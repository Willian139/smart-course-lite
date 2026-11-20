package com.zuwe.smartcourselite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public final class UiPolish {
    private static final long DUR_LONG = 600;
    private static final long DUR_MED = 380;
    private static final long DUR_SHORT = 180;
    private static final long FLY_DURATION = 700;
    private static final long FLY_STEP = 120;
    static final PathInterpolator EASE_OUT = Motion.ENTER;
    static final PathInterpolator EASE_IN_OUT = Motion.STANDARD;
    private static final PathInterpolator PREMIUM_EASE = Motion.STANDARD;
    static final PathInterpolator OVERSHOOT = Motion.SPRING;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final PathInterpolator BOUNCE = Motion.SPRING;

    static void lambda$skeleton$0() {
    }

    private UiPolish() {
    }

    static final class Press implements View.OnTouchListener {
        Press() {
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean z = motionEvent.getActionMasked() == 0;
            if (z) {
                Haptics.tick(view);
            }
            float f = z ? 0.97f : 1.0f;
            view.animate().scaleX(f).scaleY(f).setDuration(z ? 180L : 260L).setInterpolator(z ? Motion.STANDARD : Motion.ENTER).start();
            return false;
        }
    }

    public static void pressable(View view) {
        if (view != null) {
            view.setOnTouchListener(new Press());
        }
    }

    public static void ripple(View view, int i) {
        if (view == null) {
            return;
        }
        Drawable background = view.getBackground();
        int paddingLeft = view.getPaddingLeft();
        int paddingTop = view.getPaddingTop();
        int paddingRight = view.getPaddingRight();
        int paddingBottom = view.getPaddingBottom();
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(i), background, null));
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public static void staggerIn(final View view, final int i) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        float f = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0.0f);
        view.setTranslationX(0.0f);
        view.setTranslationY(f * 14.0f);
        view.setScaleX(0.97f);
        view.setScaleY(0.97f);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.UiPolish.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.animate().cancel();
                view.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(Math.min(i, 12) * 30).setDuration(UiPolish.DUR_LONG).setInterpolator(Motion.ENTER).start();
                return true;
            }
        });
    }

    public static void flyIn(final View view, final int i) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        float f = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0.0f);
        view.setTranslationX(f * (-110.0f));
        view.setTranslationY(0.0f);
        view.setScaleX(0.86f);
        view.setScaleY(0.86f);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.UiPolish.2
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.animate().cancel();
                view.animate().alpha(1.0f).translationX(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(Math.min(i, 12) * UiPolish.FLY_STEP).setDuration(UiPolish.FLY_DURATION).setInterpolator(Motion.SPRING).start();
                return true;
            }
        });
    }

    public static void staggerGroup(final ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        final int childCount = viewGroup.getChildCount();
        float f = viewGroup.getResources().getDisplayMetrics().density;
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            childAt.setAlpha(0.0f);
            childAt.setTranslationY(14.0f * f);
            childAt.setScaleX(0.97f);
            childAt.setScaleY(0.97f);
        }
        viewGroup.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.UiPolish.3
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                viewGroup.getViewTreeObserver().removeOnPreDrawListener(this);
                for (int i2 = 0; i2 < childCount; i2++) {
                    viewGroup.getChildAt(i2).animate().alpha(1.0f).translationY(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(Math.min(i2, 10) * 30).setDuration(UiPolish.DUR_LONG).setInterpolator(Motion.ENTER).start();
                }
                return true;
            }
        });
    }

    public static void slideInHoriz(View view, int i, int i2) {
        if (view == null) {
            return;
        }
        float f = view.getResources().getDisplayMetrics().density;
        view.animate().cancel();
        view.setAlpha(0.0f);
        view.setTranslationX(f * 24.0f * i);
        view.setScaleX(0.97f);
        view.setScaleY(0.97f);
        view.animate().alpha(1.0f).translationX(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(i2).setDuration(DUR_LONG).setInterpolator(EASE_OUT).start();
    }

    public static void containerExpand(final View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.setAlpha(0.0f);
        view.setScaleX(0.97f);
        view.setScaleY(0.97f);
        view.setTranslationY(view.getResources().getDisplayMetrics().density * 12.0f);
        view.setPivotY(0.0f);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.UiPolish.4
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.animate().cancel();
                view.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).setDuration(UiPolish.DUR_LONG).setInterpolator(Motion.ENTER).start();
                return true;
            }
        });
    }

    public static void containerTransform(final View view, final Runnable runnable) {
        if (view == null) {
            runnable.run();
        } else {
            view.animate().scaleX(0.96f).scaleY(0.96f).alpha(0.85f).setDuration(FLY_STEP).setInterpolator(EASE_IN_OUT).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.5
                @Override // java.lang.Runnable
                public final void run() {
                    UiPolish.lambda$containerTransform$0(runnable, view);
                }
            }).start();
        }
    }

    static void lambda$containerTransform$0(Runnable runnable, View view) {
        runnable.run();
        view.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(180L).setStartDelay(40L).setInterpolator(EASE_OUT).start();
    }

    public static void glass(View view, int i, int i2, float f) {
        if (view == null) {
            return;
        }
        float f2 = view.getResources().getDisplayMetrics().density;
        final int round = Math.round(f * f2);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(round);
        gradientDrawable.setStroke(Math.max(1, Math.round(f2 * 1.0f)), i2);
        view.setBackground(gradientDrawable);
        view.setElevation(0.0f);
        view.setOutlineProvider(new ViewOutlineProvider() { // from class: com.zuwe.smartcourselite.UiPolish.6
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(0, 0, view2.getWidth(), view2.getHeight(), round);
            }
        });
        view.setClipToOutline(true);
    }

    public static int withAlpha(int i, int i2) {
        return Color.argb(i2, Color.red(i), Color.green(i), Color.blue(i));
    }

    public static Runnable skeleton(final ViewGroup viewGroup, int i, int i2, int i3, int i4) {
        if (viewGroup == null) {
            return new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.7
                @Override // java.lang.Runnable
                public final void run() {
                    UiPolish.lambda$skeleton$0();
                }
            };
        }
        viewGroup.removeAllViews();
        float f = viewGroup.getResources().getDisplayMetrics().density;
        ArrayList arrayList = new ArrayList();
        int i5 = 0;
        while (i5 < i) {
            LinearLayout linearLayout = new LinearLayout(viewGroup.getContext());
            linearLayout.setOrientation(1);
            float f2 = 16.0f * f;
            linearLayout.setPadding(Math.round(f2), Math.round(f2), Math.round(f2), Math.round(f2));
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(i2);
            gradientDrawable.setCornerRadius(Math.round(f2));
            gradientDrawable.setStroke(Math.round(f), i4);
            linearLayout.setBackground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = i5 == 0 ? 0 : Math.round(10.0f * f);
            linearLayout.addView(skelBar(viewGroup.getContext(), 160.0f * f, i3, arrayList));
            linearLayout.setElevation(0.0f);
            float f3 = 8.0f * f;
            linearLayout.addView(skelBar(viewGroup.getContext(), 220.0f * f, i3, arrayList), lpTop(f3));
            linearLayout.addView(skelBar(viewGroup.getContext(), 100.0f * f, i3, arrayList), lpTop(f3));
            viewGroup.addView(linearLayout, layoutParams);
            i5++;
        }
        final ShimmerController shimmerController = new ShimmerController(arrayList);
        shimmerController.start();
        return new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.8
            @Override // java.lang.Runnable
            public final void run() {
                UiPolish.lambda$skeleton$1(shimmerController, viewGroup);
            }
        };
    }

    static void lambda$skeleton$1(ShimmerController shimmerController, ViewGroup viewGroup) {
        shimmerController.stop();
        viewGroup.removeAllViews();
    }

    private static LinearLayout.LayoutParams lpTop(float f) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = Math.round(f);
        return layoutParams;
    }

    private static View skelBar(Context context, float f, int i, List<View> list) {
        float f2 = context.getResources().getDisplayMetrics().density;
        FrameLayout frameLayout = new FrameLayout(context);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(872415231);
        float f3 = 4.0f * f2;
        gradientDrawable.setCornerRadius(Math.round(f3));
        frameLayout.setBackground(gradientDrawable);
        View view = new View(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(Math.round(100.0f * f2), -1));
        GradientDrawable gradientDrawable2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{16777215, i, 16777215});
        gradientDrawable2.setCornerRadius(Math.round(f3));
        view.setBackground(gradientDrawable2);
        view.setX((-100.0f) * f2);
        frameLayout.addView(view);
        list.add(view);
        new LinearLayout.LayoutParams(Math.round(f), Math.round(f2 * 14.0f));
        return frameLayout;
    }

    static final class ShimmerController {
        final List<View> bars;
        private final Runnable loop = new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.ShimmerController.1
            long startTs;

            @Override // java.lang.Runnable
            public void run() {
                if (ShimmerController.this.running) {
                    if (this.startTs == 0) {
                        this.startTs = System.currentTimeMillis();
                    }
                    float currentTimeMillis = ((System.currentTimeMillis() - this.startTs) % 1400) / 1400.0f;
                    for (View view : ShimmerController.this.bars) {
                        View view2 = (View) view.getParent();
                        if (view2 != null && view2.getWidth() != 0) {
                            view.setTranslationX((-view.getWidth()) + ((view2.getWidth() + view.getWidth()) * currentTimeMillis));
                        }
                    }
                    UiPolish.MAIN.postDelayed(this, 16L);
                }
            }
        };
        boolean running;

        ShimmerController(List<View> list) {
            this.bars = list;
        }

        void start() {
            if (this.running) {
                return;
            }
            this.running = true;
            UiPolish.MAIN.post(this.loop);
        }

        void stop() {
            this.running = false;
            UiPolish.MAIN.removeCallbacks(this.loop);
        }
    }

    public static void accordion(final View view, boolean z) {
        if (view == null) {
            return;
        }
        if (z) {
            view.setVisibility(0);
            view.measure(View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(0, 0));
            int measuredHeight = view.getMeasuredHeight();
            if (measuredHeight <= 0) {
                view.getLayoutParams().height = -2;
                return;
            }
            view.getLayoutParams().height = 0;
            view.requestLayout();
            ValueAnimator ofInt = ValueAnimator.ofInt(0, measuredHeight);
            ofInt.setDuration(180L);
            ofInt.setInterpolator(EASE_IN_OUT);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.UiPolish.9
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    UiPolish.lambda$accordion$0(view, valueAnimator);
                }
            });
            ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.UiPolish.10
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    view.getLayoutParams().height = -2;
                    view.requestLayout();
                }
            });
            ofInt.start();
            return;
        }
        int height = view.getHeight();
        if (height <= 0) {
            view.setVisibility(8);
            return;
        }
        ValueAnimator ofInt2 = ValueAnimator.ofInt(height, 0);
        ofInt2.setDuration(180L);
        ofInt2.setInterpolator(EASE_IN_OUT);
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.UiPolish.11
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                UiPolish.lambda$accordion$1(view, valueAnimator);
            }
        });
        ofInt2.addListener(new AnimatorListenerAdapter() { // from class: com.zuwe.smartcourselite.UiPolish.12
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(8);
                view.getLayoutParams().height = -2;
                view.requestLayout();
            }
        });
        ofInt2.start();
    }

    static void lambda$accordion$0(View view, ValueAnimator valueAnimator) {
        view.getLayoutParams().height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        view.requestLayout();
    }

    static void lambda$accordion$1(View view, ValueAnimator valueAnimator) {
        view.getLayoutParams().height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        view.requestLayout();
    }

    public static void parallax(final ScrollView scrollView, final View[] viewArr, final float[] fArr) {
        if (scrollView == null) {
            return;
        }
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { // from class: com.zuwe.smartcourselite.UiPolish.13
            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                int i = 0;
                while (true) {
                    View[] viewArr2 = viewArr;
                    if (i >= viewArr2.length) {
                        return;
                    }
                    View view = viewArr2[i];
                    if (view != null) {
                        float[] fArr2 = fArr;
                        view.setTranslationY(scrollY * (fArr2.length > i ? fArr2[i] : 0.5f));
                    }
                    i++;
                }
            }
        });
    }

    public static void successPopup(final Activity activity, final String str, final String str2, final int i) {
        activity.runOnUiThread(new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.14
            @Override // java.lang.Runnable
            public final void run() {
                UiPolish.lambda$successPopup$0(activity, i, str, str2);
            }
        });
    }

    static void lambda$successPopup$0(Activity activity, int i, String str, String str2) {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.getWindow().setDimAmount(0.4f);
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(1);
        float f = activity.getResources().getDisplayMetrics().density;
        float f2 = 40.0f * f;
        linearLayout.setPadding(Math.round(f2), Math.round(36.0f * f), Math.round(f2), Math.round(30.0f * f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(-232775656);
        gradientDrawable.setCornerRadius(Math.round(28.0f * f));
        gradientDrawable.setStroke(Math.round(2.0f * f), withAlpha(i, 140));
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setElevation(0.0f);
        CheckCircleView checkCircleView = new CheckCircleView(activity, i);
        int round = Math.round(80.0f * f);
        linearLayout.addView(checkCircleView, new LinearLayout.LayoutParams(round, round));
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(22.0f);
        textView.setTextColor(-1);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(17);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = Math.round(16.0f * f);
        linearLayout.addView(textView, layoutParams);
        if (str2 != null && !str2.isEmpty()) {
            TextView textView2 = new TextView(activity);
            textView2.setText(str2);
            textView2.setTextSize(13.0f);
            textView2.setTextColor(-855638017);
            textView2.setGravity(17);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.topMargin = Math.round(8.0f * f);
            linearLayout.addView(textView2, layoutParams2);
        }
        FrameLayout frameLayout = new FrameLayout(activity);
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-2, -2);
        layoutParams3.gravity = 17;
        frameLayout.addView(linearLayout, layoutParams3);
        frameLayout.setBackgroundColor(0);
        RingView ringView = new RingView(activity, i);
        float f3 = f * 310.0f;
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(Math.round(f3), Math.round(f3));
        layoutParams4.gravity = 17;
        frameLayout.addView(ringView, 0, layoutParams4);
        dialog.setContentView(frameLayout);
        dialog.setCancelable(false);
        dialog.show();
        linearLayout.setAlpha(0.0f);
        linearLayout.setScaleX(0.6f);
        linearLayout.setScaleY(0.6f);
        linearLayout.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(420L).setInterpolator(OVERSHOOT).start();
        checkCircleView.animateDraw();
        ringView.setAlpha(0.9f);
        ringView.setScaleX(0.5f);
        ringView.setScaleY(0.5f);
        ringView.animate().scaleX(1.7f).scaleY(1.7f).alpha(0.0f).setDuration(420L).setInterpolator(Motion.ENTER).start();
        MAIN.postDelayed(new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.15
            @Override // java.lang.Runnable
            public final void run() {
                linearLayout.animate().alpha(0.0f).scaleX(0.85f).scaleY(0.85f).setDuration(280L).setInterpolator(Motion.STANDARD).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.UiPolish.15.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        UiPolish.lambda$successPopup$2(dialog);
                    }
                }).start();
            }
        }, 2200L);
    }

    static void lambda$successPopup$2(Dialog dialog) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    static final class CheckCircleView extends View {
        private final Paint checkPaint;
        private final Paint circlePaint;
        private float progress;

        CheckCircleView(Context context, int i) {
            super(context);
            this.progress = 0.0f;
            float f = context.getResources().getDisplayMetrics().density;
            Paint paint = new Paint(1);
            this.circlePaint = paint;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.0f * f);
            paint.setColor(i);
            Paint paint2 = new Paint(1);
            this.checkPaint = paint2;
            paint2.setStyle(Paint.Style.STROKE);
            paint2.setStrokeWidth(f * 5.0f);
            paint2.setStrokeCap(Paint.Cap.ROUND);
            paint2.setStrokeJoin(Paint.Join.ROUND);
            paint2.setColor(-1);
        }

        void animateDraw() {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setDuration(UiPolish.DUR_LONG);
            ofFloat.setStartDelay(250L);
            ofFloat.setInterpolator(new DecelerateInterpolator(1.5f));
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.UiPolish.CheckCircleView.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CheckCircleView.this.m24x45fa2981(valueAnimator);
                }
            });
            ofFloat.start();
        }

        void m24x45fa2981(ValueAnimator valueAnimator) {
            this.progress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            float f = width / 2.0f;
            float f2 = height / 2.0f;
            float min = (Math.min(width, height) / 2.0f) - this.circlePaint.getStrokeWidth();
            canvas.drawArc(new RectF(f - min, f2 - min, f + min, f2 + min), -90.0f, Math.min(this.progress * 2.0f, 1.0f) * 360.0f, false, this.circlePaint);
            float max = Math.max(0.0f, (this.progress - 0.4f) / 0.6f);
            if (max > 0.0f) {
                float f3 = min / 30.0f;
                Path path = new Path();
                float f4 = 9.0f * f3;
                path.moveTo(f - f4, (f3 * 0.0f) + f2);
                float f5 = f - (2.0f * f3);
                float f6 = f3 * 7.0f;
                path.lineTo(f5, f2 + f6);
                path.lineTo(f + f4, f2 - f6);
                PathMeasure pathMeasure = new PathMeasure(path, false);
                Path path2 = new Path();
                pathMeasure.getSegment(0.0f, pathMeasure.getLength() * max, path2, true);
                canvas.drawPath(path2, this.checkPaint);
            }
        }
    }

    public static final class RingView extends View {
        private final Paint paint;

        public RingView(Context context, int i) {
            super(context);
            Paint paint = new Paint(1);
            this.paint = paint;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(context.getResources().getDisplayMetrics().density * 2.0f);
            paint.setColor(UiPolish.withAlpha(i, 190));
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, (Math.min(getWidth(), getHeight()) / 2.0f) - this.paint.getStrokeWidth(), this.paint);
        }
    }
}
