package com.zuwe.smartcourselite;

import android.animation.TimeInterpolator;
import android.view.ViewPropertyAnimator;
import android.view.animation.PathInterpolator;

/* loaded from: classes.dex */
public final class Motion {
    public static final long BASE = 280;
    public static final long FAST = 180;
    public static final long INSTANT = 90;
    public static final long SCENE = 420;
    public static final PathInterpolator STANDARD = new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f);
    public static final PathInterpolator ENTER = new PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f);
    public static final PathInterpolator SPRING = new PathInterpolator(0.18f, 1.08f, 0.32f, 1.0f);

    private Motion() {
    }

    public static ViewPropertyAnimator base(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.setDuration(280L).setInterpolator(ENTER);
    }

    public static ViewPropertyAnimator scene(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.setDuration(420L).setInterpolator(ENTER);
    }

    public static ViewPropertyAnimator fast(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.setDuration(180L).setInterpolator(STANDARD);
    }

    public static ViewPropertyAnimator instant(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.setDuration(90L).setInterpolator(STANDARD);
    }

    public static void to(ViewPropertyAnimator viewPropertyAnimator, long j, TimeInterpolator timeInterpolator) {
        viewPropertyAnimator.setDuration(j).setInterpolator(timeInterpolator).start();
    }
}
