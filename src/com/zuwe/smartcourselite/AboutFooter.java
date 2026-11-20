package com.zuwe.smartcourselite;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.zuwe.smartcourselite.SvgIcon;
import java.io.InputStream;

/* loaded from: classes.dex */
public final class AboutFooter {
    private static final PathInterpolator BOUNCE = Motion.SPRING;
    public static final String DEVELOPER = "vv";
    private static final String DISCLAIMER = "本应用为个人学习用途的非官方客户端，与学校及任何官方机构无关。数据来自学校对外接口，登录凭证仅加密保存在本机，不会上传到第三方服务器。因使用本应用造成的任何后果由使用者自行承担。";
    private static final String DONATE = "本项目完全免费、无广告、无内购。如果觉得对你有帮助，可以无偿捐赠开发者，完全自愿，不影响任何功能。";
    public static final String EMAIL = "weirenwuwanren@qq.com";
    private static final String LICENSE = "MIT License · 可自由使用、修改与再分发，但需保留版权声明与许可声明；软件按「原样」提供，不附带任何明示或暗示的担保。";
    private static final long MOVE_DURATION = 800;
    private static final long SLIDE_DURATION = 1000;
    private static final int SLIDE_FROM_DP = 120;
    private static final long STEP_DELAY = 700;

    private AboutFooter() {
    }

    public static LinearLayout build(final Activity activity, int i, final int i2, final int i3, int i4, int i5, int i6, int i7) {
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        LinearLayout linearLayout2 = new LinearLayout(activity);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(dp(activity, 14.0f), 0, dp(activity, 12.0f), 0);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i6);
        gradientDrawable.setCornerRadius(dp(activity, 14.0f));
        gradientDrawable.setStroke(Math.max(1, dp(activity, 1.0f)), i4);
        linearLayout2.setBackground(gradientDrawable);
        SvgIcon.IconView view = SvgIcon.view(activity, SvgIcon.INFO, i7, 17);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(activity, 17.0f), dp(activity, 17.0f));
        layoutParams.rightMargin = dp(activity, 8.0f);
        linearLayout2.addView(view, layoutParams);
        TextView textView = new TextView(activity);
        textView.setText("关于此项目");
        textView.setTextSize(13.0f);
        textView.setTextColor(i);
        textView.setTypeface(Typeface.DEFAULT, 1);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        final TextView textView2 = new TextView(activity);
        textView2.setText("展开 ▾");
        textView2.setTextSize(11.0f);
        textView2.setTextColor(i2);
        textView2.setGravity(17);
        linearLayout2.addView(textView2, new LinearLayout.LayoutParams(-2, -2));
        final LinearLayout linearLayout3 = new LinearLayout(activity);
        linearLayout3.setOrientation(1);
        linearLayout3.setVisibility(8);
        LinearLayout card = card(activity, i5, i4, row(activity, "问题反馈", EMAIL, i3, i7, 13.0f));
        card.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AboutFooter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse("mailto:weirenwuwanren@qq.com"));
                intent.putExtra("android.intent.extra.SUBJECT", "智慧思政课表 · 问题反馈");
                try {
                    activity.startActivity(intent);
                } catch (Exception unused) {
                    Toast.makeText(activity, "没有找到可用的邮件应用", 0).show();
                }
            }
        });
        UiPolish.pressable(card);
        LinearLayout block = block(activity, "捐赠", DONATE, i2, i3);
        TextView textView3 = new TextView(activity);
        textView3.setText("点击捐赠 ›");
        textView3.setTextSize(12.0f);
        textView3.setTextColor(i7);
        textView3.setTypeface(Typeface.DEFAULT, 1);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.topMargin = dp(activity, 8.0f);
        block.addView(textView3, layoutParams2);
        LinearLayout card2 = card(activity, i5, i4, block);
        card2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AboutFooter.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                AboutFooter.showDonateCode(activity, i2, i3);
            }
        });
        LinearLayout card3 = card(activity, i5, i4, row(activity, "开发者", DEVELOPER, i3, i, 18.0f));
        UiPolish.pressable(card3);
        card3.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AboutFooter.3
            private int tapCount = 0;

            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                if (!ReminderPrefs.hiddenModeUnlocked(activity) && ReminderPrefs.incrementDevTap(activity)) {
                    ReminderPrefs.setHiddenModeUnlocked(activity);
                    Toast.makeText(activity, "您已解锁隐藏模式🎉", 1).show();
                    try {
                        Haptics.confirm(view2);
                    } catch (Throwable unused) {
                    }
                }
            }
        });
        final View[] viewArr = {card3, card, card(activity, i5, i4, block(activity, "免责声明", DISCLAIMER, i2, i3)), card(activity, i5, i4, block(activity, "MIT 开源许可", LICENSE, i2, i3)), card2, card(activity, i5, i4, UpdateManager.buildUpdateRow(activity, i, i2, i3, i5, i4, i7))};
        int i8 = 0;
        while (i8 < 6) {
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.topMargin = i8 == 0 ? dp(activity, 6.0f) : dp(activity, 8.0f);
            linearLayout3.addView(viewArr[i8], layoutParams3);
            i8++;
        }
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(-1, dp(activity, 46.0f)));
        linearLayout.addView(linearLayout3, margins(activity, 0.0f, 4.0f, 0.0f, 0.0f));
        final ValueAnimator[] valueAnimatorArr = new ValueAnimator[1];
        linearLayout2.setClickable(true);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.zuwe.smartcourselite.AboutFooter.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                boolean z = linearLayout3.getVisibility() != 0;
                textView2.setText(z ? "收起 ▴" : "展开 ▾");
                if (z) {
                    linearLayout3.setVisibility(0);
                    linearLayout3.getLayoutParams().height = -2;
                    linearLayout3.requestLayout();
                    for (View view3 : viewArr) {
                        view3.animate().cancel();
                        view3.setAlpha(0.0f);
                        view3.setTranslationX(-AboutFooter.dp(activity, 120.0f));
                        view3.setScaleX(0.86f);
                        view3.setScaleY(0.86f);
                    }
                    final ViewTreeObserver viewTreeObserver = linearLayout.getViewTreeObserver();
                    final ViewTreeObserver.OnGlobalLayoutListener[] onGlobalLayoutListenerArr = new ViewTreeObserver.OnGlobalLayoutListener[1];
                    final LinearLayout linearLayout4 = linearLayout;
                    final Activity activity2 = activity;
                    final ValueAnimator[] valueAnimatorArr2 = valueAnimatorArr;
                    final View[] viewArr2 = viewArr;
                    onGlobalLayoutListenerArr[0] = new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.zuwe.smartcourselite.AboutFooter.4.1
                        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                        public void onGlobalLayout() {
                            long j;
                            int i9 = 0;
                            if (viewTreeObserver.isAlive()) {
                                viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListenerArr[0]);
                            }
                            ScrollView findScrollParent = AboutFooter.findScrollParent(linearLayout4);
                            if (findScrollParent != null) {
                                int scrollTarget = AboutFooter.scrollTarget(linearLayout4, findScrollParent, AboutFooter.dp(activity2, 18.0f));
                                ValueAnimator[] valueAnimatorArr3 = valueAnimatorArr2;
                                j = AboutFooter.MOVE_DURATION;
                                valueAnimatorArr3[0] = AboutFooter.animateScroll(findScrollParent, scrollTarget, AboutFooter.MOVE_DURATION);
                            } else {
                                j = 0;
                            }
                            while (true) {
                                View[] viewArr3 = viewArr2;
                                if (i9 >= viewArr3.length) {
                                    return;
                                }
                                viewArr3[i9].animate().alpha(1.0f).translationX(0.0f).scaleX(1.0f).scaleY(1.0f).setStartDelay(80 + j + (i9 * AboutFooter.STEP_DELAY)).setDuration(AboutFooter.SLIDE_DURATION).setInterpolator(Motion.SPRING).start();
                                i9++;
                            }
                        }
                    };
                    viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListenerArr[0]);
                    return;
                }
                ValueAnimator valueAnimator = valueAnimatorArr[0];
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                    valueAnimatorArr[0] = null;
                }
                for (View view4 : viewArr) {
                    view4.animate().cancel();
                    view4.setAlpha(1.0f);
                    view4.setTranslationX(0.0f);
                    view4.setScaleX(1.0f);
                    view4.setScaleY(1.0f);
                }
                UiPolish.accordion(linearLayout3, false);
            }
        });
        UiPolish.pressable(linearLayout2);
        return linearLayout;
    }

    private static LinearLayout card(Activity activity, int i, int i2, View view) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(activity, 14.0f));
        gradientDrawable.setStroke(Math.max(1, dp(activity, 1.0f)), withAlpha(i2, 170));
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setPadding(dp(activity, 16.0f), dp(activity, 2.0f), dp(activity, 16.0f), dp(activity, 2.0f));
        linearLayout.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return linearLayout;
    }

    public static ScrollView findScrollParent(View view) {
        while (view != null) {
            Object parent = view.getParent();
            if (parent instanceof ScrollView) {
                return (ScrollView) parent;
            }
            if (!(parent instanceof View)) {
                return null;
            }
            view = (View) parent;
        }
        return null;
    }

    public static int scrollTarget(View view, ScrollView scrollView, int i) {
        int i2 = 0;
        View view2 = view;
        while (view2 != null) {
            i2 += view2.getTop();
            Object parent = view2.getParent();
            if (parent == scrollView) {
                break;
            }
            if (!(parent instanceof View)) {
                return scrollView.getScrollY();
            }
            view2 = (View) parent;
        }
        return Math.max(0, ((i2 + view.getHeight()) - scrollView.getHeight()) + i);
    }

    public static ValueAnimator animateScroll(final ScrollView scrollView, int i, long j) {
        ValueAnimator ofInt = ValueAnimator.ofInt(scrollView.getScrollY(), i);
        ofInt.setDuration(j);
        ofInt.setInterpolator(Motion.STANDARD);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.zuwe.smartcourselite.AboutFooter.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                scrollView.scrollTo(0, ((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        });
        ofInt.start();
        return ofInt;
    }

    public static void showDonateCode(Activity activity, int i, int i2) {
        Bitmap loadDonateBitmap = loadDonateBitmap(activity);
        if (loadDonateBitmap == null) {
            Toast.makeText(activity, "没有找到收款码图片", 0).show();
            return;
        }
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        int dp = dp(activity, 18.0f);
        linearLayout.setPadding(dp, dp(activity, 4.0f), dp, 0);
        TextView textView = new TextView(activity);
        textView.setText("如果觉得对你有帮助，可以无偿捐赠开发者。完全自愿，不影响任何功能。");
        textView.setTextSize(12.0f);
        textView.setTextColor(i);
        textView.setLineSpacing(dp(activity, 3.0f), 1.0f);
        linearLayout.addView(textView);
        ImageView imageView = new ImageView(activity);
        imageView.setImageBitmap(loadDonateBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = dp(activity, 12.0f);
        linearLayout.addView(imageView, layoutParams);
        TextView textView2 = new TextView(activity);
        textView2.setText("打开支付宝「扫一扫 → 相册」，选择这张图即可。");
        textView2.setTextSize(11.0f);
        textView2.setTextColor(i2);
        textView2.setPadding(0, dp(activity, 10.0f), 0, 0);
        linearLayout.addView(textView2);
        new AlertDialog.Builder(activity).setTitle("捐赠").setView(linearLayout).setPositiveButton("关闭", (DialogInterface.OnClickListener) null).show();
    }

    private static Bitmap loadDonateBitmap(Activity activity) {
        try (InputStream inputStream = activity.getAssets().open("donate.png")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            return null;
        }
    }

    private static LinearLayout row(Activity activity, String str, String str2, int i, int i2, float f) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 14.0f));
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(11.0f);
        textView.setTextColor(i);
        textView.setLetterSpacing(0.06f);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(activity);
        textView2.setText(str2);
        textView2.setTextSize(f);
        textView2.setTextColor(i2);
        textView2.setGravity(5);
        textView2.setTypeface(Typeface.DEFAULT, f >= 16.0f ? 1 : 0);
        linearLayout.addView(textView2, new LinearLayout.LayoutParams(-2, -2));
        return linearLayout;
    }

    private static LinearLayout block(Activity activity, String str, String str2, int i, int i2) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(0, dp(activity, 14.0f), 0, dp(activity, 14.0f));
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(12.0f);
        textView.setTextColor(i);
        textView.setTypeface(Typeface.DEFAULT, 1);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(activity);
        textView2.setText(str2);
        textView2.setTextSize(11.5f);
        textView2.setTextColor(i2);
        textView2.setLineSpacing(dp(activity, 4.0f), 1.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = dp(activity, 7.0f);
        linearLayout.addView(textView2, layoutParams);
        return linearLayout;
    }

    private static View hairline(Activity activity, int i, int i2, int i3) {
        View view = new View(activity);
        view.setBackgroundColor(i);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, Math.max(1, dp(activity, 1.0f)));
        layoutParams.topMargin = dp(activity, i2);
        layoutParams.bottomMargin = dp(activity, i3);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private static LinearLayout.LayoutParams margins(Activity activity, float f, float f2, float f3, float f4) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.leftMargin = dp(activity, f);
        layoutParams.topMargin = dp(activity, f2);
        layoutParams.rightMargin = dp(activity, f3);
        layoutParams.bottomMargin = dp(activity, f4);
        return layoutParams;
    }

    private static int dp(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int dp(Activity activity, float f) {
        return dp((Context) activity, f);
    }

    private static int withAlpha(int i, int i2) {
        return Color.argb((i2 * Color.alpha(i)) / 255, Color.red(i), Color.green(i), Color.blue(i));
    }

    public static int alpha(int i, int i2) {
        return Color.argb(i2, Color.red(i), Color.green(i), Color.blue(i));
    }

    static float sp(Activity activity, float f) {
        return TypedValue.applyDimension(2, f, activity.getResources().getDisplayMetrics());
    }
}
