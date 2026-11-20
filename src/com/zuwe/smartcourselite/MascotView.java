package com.zuwe.smartcourselite;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.provider.Settings;
import android.view.View;

/* loaded from: classes.dex */
final class MascotView extends View {
    private ValueAnimator animator;
    private final int blue;
    private float breath;
    private final Paint paint;

    MascotView(Context context, int i) {
        super(context);
        this.paint = new Paint(1);
        this.breath = 0.5f;
        this.blue = i;
        setImportantForAccessibility(2);
        if (animationsEnabled()) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.animator = ofFloat;
            ofFloat.setDuration(3200L);
            this.animator.setRepeatCount(-1);
            this.animator.setRepeatMode(2);
            this.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.MascotView.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MascotView.this.breath = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    MascotView.this.invalidate();
                }
            });
        }
    }

    private boolean animationsEnabled() {
        try {
            return Settings.Global.getFloat(getContext().getContentResolver(), "animator_duration_scale", 1.0f) != 0.0f;
        } catch (Exception unused) {
            return true;
        }
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float f = width / 2.0f;
        float f2 = 0.55f * height;
        float min = Math.min(width, height) * 0.4f * (((this.breath - 0.5f) * 0.028f) + 1.0f);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(this.blue);
        this.paint.setAlpha(42);
        canvas.drawCircle(f, f2, 1.18f * min, this.paint);
        this.paint.setAlpha(255);
        float f3 = 0.6f * min;
        float f4 = f2 - (0.92f * min);
        float f5 = 0.3f * min;
        canvas.drawCircle(f - f3, f4, f5, this.paint);
        canvas.drawCircle(f + f3, f4, f5, this.paint);
        canvas.drawCircle(f, f2, min, this.paint);
        this.paint.setColor(-725795);
        float f6 = 0.52f * min;
        canvas.drawOval(new RectF(f - f6, f2 - (0.36f * min), f6 + f, f3 + f2), this.paint);
        this.paint.setColor(-13749184);
        float f7 = f2 - (0.1f * min);
        float f8 = 0.25f * min;
        float f9 = 0.078f * min;
        canvas.drawCircle(f - f8, f7, f9, this.paint);
        canvas.drawCircle(f8 + f, f7, f9, this.paint);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(Math.max(1.5f, 0.04f * min));
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        float f10 = f2 + (0.18f * min);
        Path path = new Path();
        float f11 = 0.16f * min;
        path.moveTo(f - f11, f10);
        path.quadTo(f, (0.14f * min) + f10, f11 + f, f10);
        canvas.drawPath(path, this.paint);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(Color.argb(78, 219, 169, 142));
        float f12 = 0.44f * min;
        float f13 = f10 - (0.02f * min);
        float f14 = min * 0.085f;
        canvas.drawCircle(f - f12, f13, f14, this.paint);
        canvas.drawCircle(f + f12, f13, f14, this.paint);
    }
}
