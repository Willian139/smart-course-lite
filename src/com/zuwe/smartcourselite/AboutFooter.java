package com.zuwe.smartcourselite;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;

/**
 * 页面底部的「关于此项目」折叠块：默认收起成一行按钮，展开后逐条弹出
 * 开发者、反馈邮箱、免责声明、开源许可与捐赠入口，并自动把内容滑进视野。
 */
public final class AboutFooter {

    public static final String DEVELOPER = "vv";
    public static final String EMAIL = "weirenwuwanren@qq.com";

    private static final String DISCLAIMER =
            "本应用为个人学习用途的非官方客户端，与学校及任何官方机构无关。"
                    + "数据来自学校对外接口，登录凭证仅加密保存在本机，不会上传到第三方服务器。"
                    + "因使用本应用造成的任何后果由使用者自行承担。";
    private static final String LICENSE =
            "MIT License · 可自由使用、修改与再分发，但需保留版权声明与许可声明；"
                    + "软件按「原样」提供，不附带任何明示或暗示的担保。";
    private static final String DONATE =
            "本项目完全免费、无广告、无内购。如果觉得对你有帮助，可以无偿捐赠开发者，"
                    + "完全自愿，不影响任何功能。";

    /** 每一条之间的滑入间隔（毫秒）。 */
    private static final long STEP_DELAY = 700L;
    /** 单条滑入动画时长（毫秒）。 */
    private static final long SLIDE_DURATION = 1000L;
    /** 点开时整块内容缓慢上移的时长（毫秒）。 */
    private static final long MOVE_DURATION = 800L;
    /** 回弹更明显的滑入曲线：中段冲过终点约 40%，再回落到 1。 */
    private static final PathInterpolator BOUNCE = new PathInterpolator(0.16f, 1.65f, 0.38f, 1f);
    /** 卡片起点偏移，越大甩得越远。 */
    private static final int SLIDE_FROM_DP = 120;

    private AboutFooter() {
    }

    public static LinearLayout build(final Activity activity, final int ink, final int muted,
                                     final int faint, final int line, final int cardBg,
                                     final int bgAlt, final int accent) {
        final LinearLayout wrap = new LinearLayout(activity);
        wrap.setOrientation(LinearLayout.VERTICAL);

        // ---------------------------------------------------------------- 表头按钮
        final LinearLayout headRow = new LinearLayout(activity);
        headRow.setOrientation(LinearLayout.HORIZONTAL);
        headRow.setGravity(Gravity.CENTER_VERTICAL);
        headRow.setPadding(dp(activity, 14), 0, dp(activity, 12), 0);

        GradientDrawable headBg = new GradientDrawable();
        headBg.setColor(bgAlt);
        headBg.setCornerRadius(dp(activity, 14));
        headBg.setStroke(Math.max(1, dp(activity, 1)), line);
        headRow.setBackground(headBg);

        SvgIcon.IconView info = SvgIcon.view(activity, SvgIcon.INFO, accent, 17);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(dp(activity, 17), dp(activity, 17));
        infoLp.rightMargin = dp(activity, 8);
        headRow.addView(info, infoLp);

        TextView title = new TextView(activity);
        title.setText("关于此项目");
        title.setTextSize(13f);
        title.setTextColor(ink);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        headRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));

        final TextView chevron = new TextView(activity);
        chevron.setText("展开 ▾");
        chevron.setTextSize(11f);
        chevron.setTextColor(muted);
        chevron.setGravity(Gravity.CENTER);
        headRow.addView(chevron, new LinearLayout.LayoutParams(-2, -2));

        // ---------------------------------------------------------------- 内容：每条一张卡片
        final LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(View.GONE);

        final View[] items = new View[5];

        items[0] = card(activity, cardBg, line,
                row(activity, "开发者", DEVELOPER, faint, ink, 18f));

        final LinearLayout feedback = card(activity, cardBg, line,
                row(activity, "问题反馈", EMAIL, faint, accent, 13f));
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, "智慧思政课表 · 问题反馈");
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(activity, "没有找到可用的邮件应用", Toast.LENGTH_SHORT).show();
                }
            }
        });
        UiPolish.pressable(feedback);
        items[1] = feedback;

        items[2] = card(activity, cardBg, line,
                block(activity, "免责声明", DISCLAIMER, muted, faint));
        items[3] = card(activity, cardBg, line,
                block(activity, "MIT 开源许可", LICENSE, muted, faint));

        final LinearLayout donateInner = block(activity, "捐赠", DONATE, muted, faint);
        TextView donateHint = new TextView(activity);
        donateHint.setText("点击捐赠 ›");
        donateHint.setTextSize(12f);
        donateHint.setTextColor(accent);
        donateHint.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
        hintLp.topMargin = dp(activity, 8);
        donateInner.addView(donateHint, hintLp);
        final LinearLayout donate = card(activity, cardBg, line, donateInner);
        donate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                showDonateCode(activity, muted, faint);
            }
        });
        UiPolish.pressable(donate);
        items[4] = donate;

        for (int i = 0; i < items.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = i == 0 ? dp(activity, 6) : dp(activity, 8);
            body.addView(items[i], lp);
        }

        wrap.addView(headRow, new LinearLayout.LayoutParams(-1, dp(activity, 46)));
        wrap.addView(body, margins(activity, 0, 4, 0, 0));

        final ValueAnimator[] scroller = new ValueAnimator[1];

        headRow.setClickable(true);
        headRow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                boolean expand = body.getVisibility() != View.VISIBLE;
                chevron.setText(expand ? "收起 ▴" : "展开 ▾");
                if (expand) {
                    // 先把内容撑开，等这一帧布局完成拿到新高度，再缓慢滑上去
                    body.setVisibility(View.VISIBLE);
                    body.getLayoutParams().height = -2;
                    body.requestLayout();
                    for (View item : items) {
                        item.animate().cancel();
                        item.setAlpha(0f);
                        item.setTranslationX(-dp(activity, SLIDE_FROM_DP));
                        item.setScaleX(0.86f);
                        item.setScaleY(0.86f);
                    }
                    final ViewTreeObserver observer = wrap.getViewTreeObserver();
                    final ViewTreeObserver.OnGlobalLayoutListener[] once =
                            new ViewTreeObserver.OnGlobalLayoutListener[1];
                    once[0] = new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override public void onGlobalLayout() {
                            if (observer.isAlive()) {
                                observer.removeOnGlobalLayoutListener(once[0]);
                            }
                            ScrollView scroll = findScrollParent(wrap);
                            long moveMs = 0L;
                            if (scroll != null) {
                                int target = scrollTarget(wrap, scroll, dp(activity, 18));
                                scroller[0] = animateScroll(scroll, target, MOVE_DURATION);
                                moveMs = MOVE_DURATION;
                            }
                            // 移动停稳之后，再逐条带回弹滑入
                            for (int i = 0; i < items.length; i++) {
                                items[i].animate()
                                        .alpha(1f)
                                        .translationX(0f)
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setStartDelay(moveMs + 80L + i * STEP_DELAY)
                                        .setDuration(SLIDE_DURATION)
                                        .setInterpolator(BOUNCE)
                                        .start();
                            }
                        }
                    };
                    observer.addOnGlobalLayoutListener(once[0]);
                } else {
                    if (scroller[0] != null) {
                        scroller[0].cancel();
                        scroller[0] = null;
                    }
                    for (View item : items) {
                        item.animate().cancel();
                        item.setAlpha(1f);
                        item.setTranslationX(0f);
                        item.setScaleX(1f);
                        item.setScaleY(1f);
                    }
                    UiPolish.accordion(body, false);
                }
            }
        });
        UiPolish.pressable(headRow);
        return wrap;
    }

    /** 把内容包成一张圆角卡片。 */
    private static LinearLayout card(Activity activity, int cardBg, int line, View content) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardBg);
        bg.setCornerRadius(dp(activity, 14));
        bg.setStroke(Math.max(1, dp(activity, 1)), withAlpha(line, 0xAA));
        card.setBackground(bg);
        card.setPadding(dp(activity, 16), dp(activity, 2), dp(activity, 16), dp(activity, 2));
        card.addView(content, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    /** 找到包含该视图的最近一个 ScrollView。 */
    private static ScrollView findScrollParent(View target) {
        View v = target;
        while (v != null) {
            ViewParent parent = v.getParent();
            if (parent instanceof ScrollView) {
                return (ScrollView) parent;
            }
            if (!(parent instanceof View)) {
                return null;
            }
            v = (View) parent;
        }
        return null;
    }

    /** 让 target 的整体刚好露出可见区时，ScrollView 应该滚到的位置。 */
    private static int scrollTarget(View target, ScrollView scroll, int extra) {
        View v = target;
        int y = 0;
        while (v != null) {
            y += v.getTop();
            ViewParent parent = v.getParent();
            if (parent == scroll) {
                break;
            }
            if (!(parent instanceof View)) {
                return scroll.getScrollY();
            }
            v = (View) parent;
        }
        int to = y + target.getHeight() - scroll.getHeight() + extra;
        // ScrollView 自身会限制在内容范围内，这里只需要保证不为负
        return Math.max(0, to);
    }

    /** 手动补间滚动：系统 smoothScrollTo 的时长不可控，这里自己按曲线走 0.8 秒。 */
    private static ValueAnimator animateScroll(final ScrollView scroll, final int targetY, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(scroll.getScrollY(), targetY);
        animator.setDuration(duration);
        animator.setInterpolator(UiPolish.EASE_IN_OUT);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                scroll.scrollTo(0, (Integer) animation.getAnimatedValue());
            }
        });
        animator.start();
        return animator;
    }

    /** 捐赠收款码：只有点开「捐赠」才弹出来，平时不占用页面空间。 */
    private static void showDonateCode(Activity activity, int muted, int faint) {
        Bitmap bitmap = loadDonateBitmap(activity);
        if (bitmap == null) {
            Toast.makeText(activity, "没有找到收款码图片", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 18);
        box.setPadding(pad, dp(activity, 4), pad, 0);

        TextView message = new TextView(activity);
        message.setText("如果觉得对你有帮助，可以无偿捐赠开发者。完全自愿，不影响任何功能。");
        message.setTextSize(12f);
        message.setTextColor(muted);
        message.setLineSpacing(dp(activity, 3), 1f);
        box.addView(message);

        ImageView image = new ImageView(activity);
        image.setImageBitmap(bitmap);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setAdjustViewBounds(true);
        LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(-1, -2);
        imageLp.topMargin = dp(activity, 12);
        box.addView(image, imageLp);

        TextView caption = new TextView(activity);
        caption.setText("打开支付宝「扫一扫 → 相册」，选择这张图即可。");
        caption.setTextSize(11f);
        caption.setTextColor(faint);
        caption.setPadding(0, dp(activity, 10), 0, 0);
        box.addView(caption);

        new AlertDialog.Builder(activity)
                .setTitle("捐赠")
                .setView(box)
                .setPositiveButton("关闭", null)
                .show();
    }

    private static Bitmap loadDonateBitmap(Activity activity) {
        InputStream in = null;
        try {
            in = activity.getAssets().open("donate.png");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(in, null, options);
        } catch (Exception e) {
            return null;
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) { }
            }
        }
    }

    /** 一行「标签左、值右」：两端对齐，值可放大加粗（开发者用）。 */
    private static LinearLayout row(Activity activity, String label, String value,
                                    int labelColor, int valueColor, float valueSize) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(activity, 14), 0, dp(activity, 14));

        TextView name = new TextView(activity);
        name.setText(label);
        name.setTextSize(11f);
        name.setTextColor(labelColor);
        name.setLetterSpacing(0.06f);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(valueSize);
        text.setTextColor(valueColor);
        text.setGravity(Gravity.RIGHT);
        text.setTypeface(Typeface.DEFAULT, valueSize >= 16f ? Typeface.BOLD : Typeface.NORMAL);
        row.addView(text, new LinearLayout.LayoutParams(-2, -2));
        return row;
    }

    /** 小标题 + 多行说明，标题与正文左右都对齐到卡片内边距。 */
    private static LinearLayout block(Activity activity, String label, String text, int muted, int faint) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(activity, 14), 0, dp(activity, 14));

        TextView name = new TextView(activity);
        name.setText(label);
        name.setTextSize(12f);
        name.setTextColor(muted);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        box.addView(name);

        TextView body = new TextView(activity);
        body.setText(text);
        body.setTextSize(11.5f);
        body.setTextColor(faint);
        body.setLineSpacing(dp(activity, 4), 1f);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(-1, -2);
        bodyLp.topMargin = dp(activity, 7);
        box.addView(body, bodyLp);
        return box;
    }

    private static View hairline(Activity activity, int color, int topDp, int bottomDp) {
        View view = new View(activity);
        view.setBackgroundColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, dp(activity, 1)));
        lp.topMargin = dp(activity, topDp);
        lp.bottomMargin = dp(activity, bottomDp);
        view.setLayoutParams(lp);
        return view;
    }

    private static LinearLayout.LayoutParams margins(Activity activity, float l, float t, float r, float b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.leftMargin = dp(activity, l);
        lp.topMargin = dp(activity, t);
        lp.rightMargin = dp(activity, r);
        lp.bottomMargin = dp(activity, b);
        return lp;
    }

    private static int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int dp(Activity activity, float value) {
        return dp((Context) activity, value);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb((alpha * Color.alpha(color)) / 255, Color.red(color), Color.green(color), Color.blue(color));
    }

    /** 供调用方统一取色时用（避免各页面重复写 Color.parseColor）。 */
    public static int alpha(int color, int a) {
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    static float sp(Activity activity, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                activity.getResources().getDisplayMetrics());
    }
}
