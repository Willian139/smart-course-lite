package com.zuwe.smartcourselite;

import android.content.Context;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/* loaded from: classes.dex */
public final class WheelPicker extends ScrollView {
    private final View bottomSpacer;
    private final LinearLayout column;
    private final int count;
    private final int faintColor;
    private final int inkColor;
    private final int itemHeight;
    private final TextView[] labels;
    private OnChange onChange;
    private boolean oneBased;
    private int selected;
    private final Runnable snapTask;
    private final View topSpacer;

    public interface OnChange {
        void onChanged(int i);
    }

    public WheelPicker(Context context, int i, int i2, int i3, int i4, int i5, int i6) {
        super(context);
        this.snapTask = new Runnable() { // from class: com.zuwe.smartcourselite.WheelPicker.1
            @Override // java.lang.Runnable
            public void run() {
                WheelPicker.this.snap();
            }
        };
        this.count = i;
        this.inkColor = i3;
        this.faintColor = i4;
        float f = context.getResources().getDisplayMetrics().density;
        this.itemHeight = Math.round(48.0f * f);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(2);
        setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(context);
        this.column = linearLayout;
        linearLayout.setOrientation(1);
        View view = new View(context);
        this.topSpacer = view;
        this.bottomSpacer = new View(context);
        linearLayout.addView(view, new LinearLayout.LayoutParams(-1, 0));
        this.labels = new TextView[i];
        for (int i7 = 0; i7 < i; i7++) {
            TextView textView = new TextView(context);
            textView.setText(String.format(Locale.US, "%02d", Integer.valueOf(i7)));
            textView.setGravity(17);
            textView.setIncludeFontPadding(false);
            textView.setSingleLine(true);
            this.column.addView(textView, new LinearLayout.LayoutParams(-1, this.itemHeight));
            this.labels[i7] = textView;
        }
        this.column.addView(this.bottomSpacer, new LinearLayout.LayoutParams(-1, 0));
        addView(this.column, new FrameLayout.LayoutParams(-1, -2));
        this.selected = Math.max(0, Math.min(i - 1, i2));
        paint();
        setLayoutParams(new ViewGroup.LayoutParams(Math.round(i5 * f), Math.round(i6 * f)));
    }

    public void setOnChange(OnChange onChange) {
        this.onChange = onChange;
    }

    public void setOneBased(boolean z) {
        this.oneBased = z;
        if (this.labels == null) {
            return;
        }
        int i = 0;
        while (true) {
            TextView[] textViewArr = this.labels;
            if (i < textViewArr.length) {
                i++;
                textViewArr[i].setText(String.format(Locale.US, "%02d", Integer.valueOf(i)));
            } else {
                paint();
                return;
            }
        }
    }

    public int value() {
        return this.selected;
    }

    public void setValue(int i) {
        this.selected = Math.max(0, Math.min(this.count - 1, i));
        paint();
        post(new Runnable() { // from class: com.zuwe.smartcourselite.WheelPicker.2
            @Override // java.lang.Runnable
            public void run() {
                WheelPicker wheelPicker = WheelPicker.this;
                wheelPicker.scrollTo(0, wheelPicker.selected * WheelPicker.this.itemHeight);
            }
        });
    }

    @Override // android.widget.ScrollView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.ScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        if (motionEvent.getActionMasked() == 0) {
            setNestedScrollingEnabled(false);
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.widget.ScrollView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        int max = Math.max(0, (i2 - this.itemHeight) / 2);
        this.topSpacer.getLayoutParams().height = max;
        this.bottomSpacer.getLayoutParams().height = max;
        this.topSpacer.requestLayout();
        this.bottomSpacer.requestLayout();
        scrollTo(0, this.selected * this.itemHeight);
    }

    @Override // android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        removeCallbacks(this.snapTask);
        postDelayed(this.snapTask, 90L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void snap() {
        int max = Math.max(0, Math.min(this.count - 1, Math.round(getScrollY() / this.itemHeight)));
        int i = this.itemHeight * max;
        if (getScrollY() != i) {
            smoothScrollTo(0, i);
        }
        if (max != this.selected) {
            this.selected = max;
            paint();
            OnChange onChange = this.onChange;
            if (onChange != null) {
                onChange.onChanged(this.selected);
            }
        }
    }

    private void paint() {
        float f = getResources().getDisplayMetrics().density;
        int i = 0;
        while (i < this.count) {
            TextView textView = this.labels[i];
            if (textView != null) {
                int i2 = i == this.selected ? 1 : 0;
                textView.setTextSize(2, i2 != 0 ? 27.0f : 21.0f);
                textView.setTextColor(i2 != 0 ? this.inkColor : this.faintColor);
                textView.setTypeface(Typeface.create("sans-serif", i2));
                textView.setAlpha(i2 != 0 ? 1.0f : 0.55f);
            }
            i++;
        }
    }
}
