package com.zuwe.smartcourselite;

import android.view.MotionEvent;
import android.view.View;

/* loaded from: classes.dex */
public final class Haptics {
    private Haptics() {
    }

    public static void tick(View view) {
        if (view == null) {
            return;
        }
        view.performHapticFeedback(3);
    }

    public static void confirm(View view) {
        if (view == null) {
            return;
        }
        view.performHapticFeedback(6);
    }

    public static void attach(View view) {
        if (view == null) {
            return;
        }
        view.setHapticFeedbackEnabled(true);
        view.setOnTouchListener(new View.OnTouchListener() { // from class: com.zuwe.smartcourselite.Haptics.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() != 0) {
                    return false;
                }
                Haptics.tick(view2);
                return false;
            }
        });
    }
}
