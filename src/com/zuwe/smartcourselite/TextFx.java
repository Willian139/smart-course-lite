package com.zuwe.smartcourselite;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

/* loaded from: classes.dex */
public final class TextFx {
    public static final PathInterpolator SPRING = Motion.SPRING;
    public static final PathInterpolator GLIDE = Motion.ENTER;
    public static final PathInterpolator SOFT = Motion.STANDARD;

    private TextFx() {
    }

    public static View title(Context context, CharSequence charSequence, float f, int i, Typeface typeface, long j, long j2) {
        StaggerText staggerText = new StaggerText(context, charSequence, f, i, typeface);
        staggerText.stagger = Math.max(0L, j);
        staggerText.duration = Math.max(120L, j2);
        return staggerText;
    }

    public static void flip(final TextView textView, CharSequence charSequence) {
        if (textView == null || charSequence == null) {
            return;
        }
        final String charSequence2 = charSequence.toString();
        if (charSequence2.contentEquals(textView.getText())) {
            return;
        }
        textView.setCameraDistance(textView.getResources().getDisplayMetrics().density * 16000.0f);
        textView.setPivotX(textView.getWidth() / 2.0f);
        textView.setPivotY(textView.getHeight() / 2.0f);
        textView.animate().rotationX(-88.0f).alpha(0.25f).setDuration(180L).setInterpolator(Motion.STANDARD).withEndAction(new Runnable() { // from class: com.zuwe.smartcourselite.TextFx.1
            @Override // java.lang.Runnable
            public void run() {
                textView.setText(charSequence2);
                textView.setRotationX(88.0f);
                textView.animate().rotationX(0.0f).alpha(1.0f).setDuration(420L).setInterpolator(Motion.SPRING).start();
            }
        }).start();
    }

    public static void springDialog(Dialog dialog) {
        Window window;
        if (dialog == null || (window = dialog.getWindow()) == null) {
            return;
        }
        View decorView = window.getDecorView();
        decorView.setAlpha(0.0f);
        decorView.setScaleX(0.88f);
        decorView.setScaleY(0.88f);
        decorView.setTranslationY(decorView.getResources().getDisplayMetrics().density * 26.0f);
        decorView.setPivotX(decorView.getResources().getDisplayMetrics().widthPixels / 2.0f);
        decorView.setPivotY(decorView.getResources().getDisplayMetrics().heightPixels / 2.0f);
        decorView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).setDuration(420L).setInterpolator(SPRING).start();
    }

    public static void countUp(final TextView textView, int i, long j) {
        if (textView == null) {
            return;
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(0, i);
        ofInt.setDuration(Math.max(200L, j));
        ofInt.setInterpolator(new DecelerateInterpolator(1.6f));
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.TextFx.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textView.setText(String.valueOf(valueAnimator.getAnimatedValue()));
            }
        });
        ofInt.start();
    }

    public static final class StaggerText extends View {
        private ValueAnimator animator;
        private float baseline;
        private long duration;
        private final Paint paint;
        private float progress;
        private final float sizePx;
        private long stagger;
        private final String text;
        private final float totalWidth;
        private final float[] widths;

        StaggerText(Context context, CharSequence charSequence, float f, int i, Typeface typeface) {
            super(context);
            Paint paint = new Paint(1);
            this.paint = paint;
            this.stagger = 0L;
            this.duration = 620L;
            this.progress = 0.0f;
            String charSequence2 = charSequence == null ? "" : charSequence.toString();
            this.text = charSequence2;
            float f2 = f * getResources().getDisplayMetrics().scaledDensity;
            this.sizePx = f2;
            paint.setColor(i);
            paint.setTextSize(f2);
            if (typeface != null) {
                paint.setTypeface(typeface);
            }
            paint.setSubpixelText(true);
            this.widths = new float[charSequence2.length()];
            int i2 = 0;
            float f3 = 0.0f;
            while (i2 < this.text.length()) {
                int i3 = i2 + 1;
                this.widths[i2] = this.paint.measureText(this.text, i2, i3);
                f3 += this.widths[i2];
                i2 = i3;
            }
            this.totalWidth = f3;
            this.baseline = -this.paint.getFontMetrics().ascent;
            setAlpha(0.0f);
        }

        public void play(final long j) {
            setAlpha(1.0f);
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.zuwe.smartcourselite.TextFx.StaggerText.1
                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    StaggerText.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (StaggerText.this.animator != null) {
                        StaggerText.this.animator.cancel();
                    }
                    long max = StaggerText.this.duration + (StaggerText.this.stagger * Math.max(0, StaggerText.this.text.length() - 1));
                    StaggerText.this.animator = ValueAnimator.ofFloat(0.0f, 1.0f);
                    StaggerText.this.animator.setDuration(max);
                    StaggerText.this.animator.setStartDelay(j);
                    StaggerText.this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.TextFx.StaggerText.1.1
                        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            StaggerText.this.progress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                            StaggerText.this.invalidate();
                        }
                    });
                    StaggerText.this.animator.start();
                    return true;
                }
            });
            invalidate();
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            Paint.FontMetrics fontMetrics = this.paint.getFontMetrics();
            setMeasuredDimension(resolveSize(((int) Math.ceil(this.totalWidth)) + getPaddingLeft() + getPaddingRight(), i), resolveSize(((int) Math.ceil(fontMetrics.descent - fontMetrics.ascent)) + getPaddingTop() + getPaddingBottom(), i2));
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float f;
            super.onDraw(canvas);
            float max = this.duration + (this.stagger * Math.max(0, this.text.length() - 1));
            float paddingLeft = getPaddingLeft();
            float paddingTop = getPaddingTop();
            for (int i = 0; i < this.text.length(); i++) {
                float f2 = max <= 0.0f ? 1.0f : ((this.progress * max) - (i * this.stagger)) / this.duration;
                float f3 = f2 >= 0.0f ? f2 : 0.0f;
                if (f3 > 1.0f) {
                    f3 = 1.0f;
                }
                float interpolation = TextFx.SPRING.getInterpolation(f3);
                float min = Math.min(1.0f, f3 * 2.4f);
                float f4 = 1.0f - interpolation;
                float f5 = this.sizePx * f4 * 0.62f;
                float f6 = (interpolation * 0.28f) + 0.72f;
                int i2 = (int) (min * 255.0f);
                if (i2 <= 0) {
                    f = this.widths[i];
                } else {
                    int save = canvas.save();
                    canvas.translate((this.widths[i] / 2.0f) + paddingLeft, this.baseline + paddingTop + f5);
                    canvas.rotate(f4 * (-18.0f));
                    canvas.scale(f6, f6);
                    this.paint.setAlpha(i2);
                    canvas.drawText(this.text, i, i + 1, (-this.widths[i]) / 2.0f, 0.0f, this.paint);
                    canvas.restoreToCount(save);
                    f = this.widths[i];
                }
                paddingLeft += f;
            }
            this.paint.setAlpha(255);
        }
    }
}
