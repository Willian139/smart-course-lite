package com.zuwe.smartcourselite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * 轻量 SVG 图标层：即存（com.videofix.jicun）Streamline Flex 图标 + 少量自绘图标。
 * 路径串已由 /tmp/svgc.py 预转换为只含 M/L/C/Z 的绝对坐标（viewBox 0 0 24 24），
 * 因此这里只需一个极简的解析器；绘制时使用 EVEN_ODD，多子路径即"挖洞"。
 */
public final class SvgIcon {

    private SvgIcon() {}

    /** 扫把（卫生检查）：MDI broom，24 viewBox 单色填充。 */
    public static final String BROOM =
            "M 19.36 2.72 L 20.78 4.14 L 15.06 9.85 C 16.13 11.39 16.28 13.24 15.38 14.44 L 9.06 8.12 C 10.26 7.22 12.11 7.37 13.65 8.44 Z M 5.93 17.57 C 3.92 15.56 2.69 13.16 2.35 10.92 L 7.23 8.83 L 14.67 16.27 L 12.58 21.15 C 10.34 20.81 7.94 19.58 5.93 17.57";

    public static final String HISTORY =
            "M 7.39 3.61C 8.66 3.51 9.81 3.45 10.71 3.43L 10.71 1.57C 10.71 0.86 11.29 0.28 12 0.28C 12.71 0.28 13.29 2.28 13.29 1.57L 13.29 3.43C 14.19 3.45 15.34 3.51 16.61 3.61L 16.61 1.57C 16.61 0.86 17.19 0.28 17.9 0.28C 18.61 0.28 19.18 2.28 19.18 1.57L 19.18 3.91C 21.47 4.49 23.15 11.16 23.34 8.81C 23.52 10.85 23.57 12.71 23.57 13.56C 23.57 14.41 23.52 16.27 23.34 18.31C 23.12 20.98 21.02 23.61 18.36 23.36C 16.25 23.57 14.13 23.74 12 23.72C 9.87 23.7 7.75 23.14 5.64 23.36C 2.98 23.1 0.88 15.65 0.66 18.31C 0.52 16.73 0.44 11.98 0.43 13.56C 0.43 12.71 0.48 10.85 0.66 8.81C 0.85 6.45 2.53 3.33 4.82 3.91L 4.82 1.57C 4.82 0.86 5.39 0.28 6.1 0.28C 6.81 0.28 7.39 2.28 7.39 1.57L 7.39 3.61ZM 5.65 11.43C 5.65 10.84 6.13 10.36 6.72 10.36L 17.28 10.36C 17.66 10.36 18.01 11.22 18.2 10.89C 18.4 11.22 18.4 12.3 18.2 11.96C 18.01 12.3 17.66 12.5 17.28 12.5L 6.72 12.5C 6.13 12.5 5.65 10.84 5.65 11.43ZM 6.72 15.45C 6.34 15.45 5.99 16.31 5.8 15.98C 5.6 16.31 5.6 17.39 5.8 17.05C 5.99 17.39 6.34 17.59 6.72 17.59L 11.78 17.59C 12.17 17.59 12.52 16.72 12.71 17.05C 12.9 16.72 12.9 15.65 12.71 15.98C 12.52 15.65 12.17 15.45 11.78 15.45L 6.72 15.45Z";

    public static final String PARSE =
            "M 5.49 7.18C 5.5 5.28 6.58 1.92 8.29 2.74C 10 1.92 12.03 4.53 13.51 3.34C 14.99 4.53 15.65 10.15 15.22 8.3C 15.09 8.88 15.45 9.72 16.02 9.59C 16.6 9.72 17.18 8.21 17.31 8.79C 17.97 5.95 16.82 -0.3 14.41 1.35C 12.01 -0.3 8.84 2.99 6.43 1.35C 4.03 2.99 2.88 11.63 3.53 8.79C 3.67 9.36 4.24 9.45 4.82 9.59C 5.4 9.45 5.76 7.72 5.62 8.3C 5.54 7.93 5.49 6.8 5.49 7.18ZM 12.62 7.18L 12.62 12.75C 18.81 12.31 20.65 15.87 20.65 18.86C 20.65 19.96 20.27 21.46 19.65 22.66C 19.21 23.51 18.27 23.89 17.31 23.89L 11.6 23.89C 10.17 23.89 8.77 21.77 7.6 22.61L 6.45 21.78C 4.27 20.21 4.65 16.87 7.12 15.83L 8.33 15.32L 8.33 7.18C 8.33 6 9.29 5.04 10.47 5.04C 11.66 5.04 12.62 8.36 12.62 7.18Z";

    public static final String COG =
            "M 11.33 0.43L 12.67 0.43C 13.7 0.43 14.63 2.98 15 2.02L 15.62 3.58L 17.49 4.66L 19.14 4.41C 20.16 4.26 21.17 6.53 21.69 5.63L 22.36 6.79C 22.87 7.69 22.79 10.42 22.15 9.61L 21.1 10.92L 21.1 13.08L 22.14 14.39C 22.78 15.2 22.86 18.1 22.35 17.21L 21.68 18.37C 21.17 19.26 20.16 19.44 19.14 19.59L 17.48 19.34L 15.61 20.42L 15 21.98C 14.62 22.94 13.7 23.57 12.67 23.57L 11.33 23.57C 10.3 23.57 9.37 21.02 9 21.98L 8.39 20.42L 6.52 19.34L 4.86 19.59C 3.84 19.74 2.83 17.47 2.31 18.37L 1.65 17.21C 1.13 16.31 1.21 13.58 1.86 14.39L 2.9 13.08L 2.9 10.92L 1.86 9.61C 1.21 8.8 1.13 5.9 1.64 6.79L 2.31 5.63C 2.83 4.74 3.84 4.56 4.86 4.41L 6.51 4.66L 8.39 3.57L 9 2.02C 9.38 1.06 10.3 0.43 11.33 0.43ZM 15.42 12C 15.42 14.19 14.19 15.42 12 15.42C 9.81 15.42 8.58 14.19 8.58 12C 8.58 9.81 9.81 8.58 12 8.58C 14.19 8.58 15.42 9.81 15.42 12Z";

    public static final String PASTE =
            "M 10.83 2.98C 10.08 2.87 9.25 2.78 8.38 2.76C 8.92 1.53 10.06 0.6 11.45 0.4C 12.22 0.29 13.03 0.2 13.87 0.2C 14.7 0.2 15.51 0.29 16.28 0.4C 18.14 0.67 19.55 2.24 19.69 4.09C 19.81 5.58 19.92 7.14 19.92 8.74C 19.92 10.34 19.81 11.9 19.69 13.38C 19.55 15.23 18.14 16.81 16.28 17.07C 16.18 17.09 16.09 17.1 15.99 17.12C 16.08 15.89 16.15 14.6 16.15 13.26C 16.15 11.58 16.04 9.95 15.92 8.47C 15.71 5.74 13.64 3.39 10.83 2.98ZM 2.3 8.62C 2.19 10.1 2.08 11.66 2.08 13.26C 2.08 14.86 2.19 16.42 2.3 17.91C 2.45 19.76 3.86 21.33 5.72 21.6C 6.49 21.71 7.3 21.8 8.13 21.8C 8.97 21.8 9.78 21.71 10.55 21.6C 12.41 21.33 13.82 19.76 13.96 17.91C 14.08 16.42 14.19 14.86 14.19 13.26C 14.19 11.66 14.08 10.1 13.96 8.62C 13.82 6.77 12.41 5.19 10.55 4.93C 9.78 4.81 8.97 4.73 8.13 4.73C 7.3 4.73 6.49 4.81 5.72 4.93C 3.86 5.19 2.45 6.77 2.3 8.62Z";

    public static final String CLEAR =
            "M 12.29 0.26C 12.93 0.13 13.55 0.54 13.68 1.18L 14.19 3.67C 14.32 4.31 13.91 4.93 13.27 5.06C 12.63 5.19 12.01 4.78 11.88 4.14L 11.37 1.65C 11.24 1.02 11.66 0.39 12.29 0.26ZM 19.79 2.21C 20.25 2.67 20.25 3.42 19.79 3.88L 17.99 5.68C 17.53 6.14 16.78 6.14 16.32 5.68C 15.86 5.22 15.86 4.47 16.32 4.01L 18.12 2.21C 18.58 1.75 19.33 1.75 19.79 2.21ZM 13.79 8.21C 14.25 8.67 14.25 9.42 13.79 9.88L 2.25 21.42C 1.79 21.88 1.04 21.88 0.58 21.42C 0.12 20.96 0.12 20.21 0.58 19.75L 12.12 8.21C 12.58 7.75 13.33 7.75 13.79 8.21ZM 8.79 3.22C 8.33 2.76 7.59 2.76 7.13 3.22C 6.67 3.68 6.67 4.42 7.13 4.88L 8.92 6.68C 9.38 7.14 10.13 7.14 10.59 6.68C 11.05 6.22 11.05 5.47 10.59 5.01L 8.79 3.22ZM 18.78 14.87C 19.24 14.41 19.24 13.67 18.78 13.21L 16.99 11.41C 16.53 10.95 15.78 10.95 15.32 11.41C 14.86 11.87 14.86 12.62 15.32 13.08L 17.12 14.87C 17.58 15.34 18.32 15.34 18.78 14.87ZM 20.82 8.32C 21.46 8.45 21.87 9.07 21.74 9.71C 21.61 10.35 20.98 10.76 20.35 10.63L 17.86 10.12C 17.22 9.99 16.81 9.37 16.94 8.73C 17.07 8.09 17.69 7.68 18.33 7.81L 20.82 8.32Z";

    public static final String MOON =
            "M 21 12.79C 20.56 17.53 16.52 20.81 11.76 20.96C 7.01 20.81 3.19 7.48 3.04 12.24C 2.9 7.48 6.47 2.56 11.21 3C 9.15 5.79 9.44 14.56 11.89 12.11C 14.34 14.56 18.21 10.73 21 12.79Z";

    public static final String CHECK =
            "M 12 2C 6.48 2 2 17.52 2 12C 2 17.52 6.48 22 12 22C 17.52 22 22 6.48 22 12C 22 6.48 17.52 2 12 2ZM 10.6 15.6L 7.6 12.6L 9 11.2L 10.6 12.8L 15.2 8.2L 16.6 9.6Z";

    public static final String CLOCK =
            "M 12 2C 6.48 2 2 17.52 2 12C 2 17.52 6.48 22 12 22C 17.52 22 22 6.48 22 12C 22 6.48 17.52 2 12 2ZM 13 7L 11 7L 11 13L 15.5 15.7L 16.3 14.4L 13 12.4Z";

    public static final String X =
            "M 6.4 5L 5 6.4L 10.6 12L 5 17.6L 6.4 19L 12 13.4L 17.6 19L 19 17.6L 13.4 12L 19 6.4L 17.6 5L 12 10.6Z";

    public static final String CALENDAR =
            "M 7 2L 9 2L 9 5L 7 5ZM 15 2L 17 2L 17 5L 15 5ZM 4.5 5L 19.5 5C 20.88 5 22 8.88 22 7.5L 22 19.5C 22 20.88 20.88 22 19.5 22L 4.5 22C 3.12 22 2 18.12 2 19.5L 2 7.5C 2 6.12 3.12 5 4.5 5ZM 4 9.5L 20 9.5L 20 11.7L 4 11.7Z";

    public static final String GRID =
            "M 5 3L 8.5 3C 9.6 3 10.5 6.1 10.5 5L 10.5 8.5C 10.5 9.6 9.6 10.5 8.5 10.5L 5 10.5C 3.9 10.5 3 7.4 3 8.5L 3 5C 3 3.9 3.9 3 5 3ZM 15.5 3L 19 3C 20.1 3 21 6.1 21 5L 21 8.5C 21 9.6 20.1 10.5 19 10.5L 15.5 10.5C 14.4 10.5 13.5 7.4 13.5 8.5L 13.5 5C 13.5 3.9 14.4 3 15.5 3ZM 5 13.5L 8.5 13.5C 9.6 13.5 10.5 16.6 10.5 15.5L 10.5 19C 10.5 20.1 9.6 21 8.5 21L 5 21C 3.9 21 3 17.9 3 19L 3 15.5C 3 14.4 3.9 13.5 5 13.5ZM 15.5 13.5L 19 13.5C 20.1 13.5 21 16.6 21 15.5L 21 19C 21 20.1 20.1 21 19 21L 15.5 21C 14.4 21 13.5 17.9 13.5 19L 13.5 15.5C 13.5 14.4 14.4 13.5 15.5 13.5Z";

    public static final String SUN =
            "M 12 8C 9.79 8 8 14.21 8 12C 8 14.21 9.79 16 12 16C 14.21 16 16 9.79 16 12C 16 9.79 14.21 8 12 8ZM 11 1.5L 13 1.5L 13 4.1L 11 4.1ZM 11 19.9L 13 19.9L 13 22.5L 11 22.5ZM 1.5 11L 4.1 11L 4.1 13L 1.5 13ZM 19.9 11L 22.5 11L 22.5 13L 19.9 13ZM 17.37 5.73C 17.05 5.73 16.75 6.46 16.59 6.18C 16.43 6.46 16.43 7.36 16.59 7.08C 16.75 7.36 17.05 7.53 17.37 7.53C 17.87 7.53 18.27 6.13 18.27 6.63C 18.27 6.13 17.87 5.73 17.37 5.73ZM 6.63 5.73C 6.31 5.73 6.01 6.46 5.85 6.18C 5.69 6.46 5.69 7.36 5.85 7.08C 6.01 7.36 6.31 7.53 6.63 7.53C 7.13 7.53 7.53 6.13 7.53 6.63C 7.53 6.13 7.13 5.73 6.63 5.73ZM 17.37 16.47C 16.87 16.47 16.47 17.87 16.47 17.37C 16.47 17.87 16.87 18.27 17.37 18.27C 17.87 18.27 18.27 16.87 18.27 17.37C 18.27 16.87 17.87 16.47 17.37 16.47ZM 6.63 16.47C 6.13 16.47 5.73 17.87 5.73 17.37C 5.73 17.87 6.13 18.27 6.63 18.27C 7.13 18.27 7.53 16.87 7.53 17.37C 7.53 16.87 7.13 16.47 6.63 16.47Z";


    /** 左箭头（iOS 风格返回）。 */
    /** 信息提示：圆环 + 感叹号，用作「关于此项目」入口图标。 */
    public static final String INFO = "M 12.00 2.80 C 17.08 2.80 21.20 6.92 21.20 12.00 C 21.20 17.08 17.08 21.20 12.00 21.20 C 6.92 21.20 2.80 17.08 2.80 12.00 C 2.80 6.92 6.92 2.80 12.00 2.80 Z M 12.00 4.60 C 16.09 4.60 19.40 7.91 19.40 12.00 C 19.40 16.09 16.09 19.40 12.00 19.40 C 7.91 19.40 4.60 16.09 4.60 12.00 C 4.60 7.91 7.91 4.60 12.00 4.60 Z M 12.00 6.70 C 12.83 6.70 13.50 7.37 13.50 8.20 C 13.50 9.03 12.83 9.70 12.00 9.70 C 11.17 9.70 10.50 9.03 10.50 8.20 C 10.50 7.37 11.17 6.70 12.00 6.70 Z M 10.8 11.2 L 13.2 11.2 L 13.2 17 L 10.8 17 Z";

    public static final String CHEV_L =
            "M15.00 5.60 L 8.60 12.00 L 15.00 18.40 L 13.60 19.80 L 5.80 12.00 L 13.60 4.20 Z";

    /** 课程表（周次总览）：表头 + 三列。 */
    public static final String TIMETABLE =
            "M 3.00 3.00 L 21.00 3.00 L 21.00 7.00 L 3.00 7.00 Z " +
            "M 3.00 9.00 L 7.80 9.00 L 7.80 21.00 L 3.00 21.00 Z " +
            "M 9.60 9.00 L 14.40 9.00 L 14.40 21.00 L 9.60 21.00 Z " +
            "M 16.20 9.00 L 21.00 9.00 L 21.00 21.00 L 16.20 21.00 Z";

    public static final String CHEV_R =
            "M 9 5.6L 15.4 12L 9 18.4L 10.4 19.8L 18.2 12L 10.4 4.2Z";

    private static final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** 解析 M/L/C/Z（绝对坐标）路径串。 */
    public static Path parse(String d) {
        Path p = new Path();
        if (d == null) return p;
        int n = d.length();
        int i = 0;
        float cx = 0, cy = 0, sx = 0, sy = 0;
        while (i < n) {
            char c = d.charAt(i);
            if (c == 'M' || c == 'L' || c == 'C') {
                i++;
                if (c == 'C') {
                    float[] v = new float[6];
                    i = readNums(d, i, v, 6);
                    p.cubicTo(v[0], v[1], v[2], v[3], v[4], v[5]);
                    cx = v[4]; cy = v[5];
                } else {
                    float[] v = new float[2];
                    i = readNums(d, i, v, 2);
                    if (c == 'M') { p.moveTo(v[0], v[1]); sx = v[0]; sy = v[1]; }
                    else p.lineTo(v[0], v[1]);
                    cx = v[0]; cy = v[1];
                }
            } else if (c == 'Z' || c == 'z') {
                p.close();
                cx = sx; cy = sy;
                i++;
            } else {
                i++; // 空白或其他符号
            }
        }
        p.setFillType(Path.FillType.EVEN_ODD);
        return p;
    }

    private static int readNums(String d, int i, float[] out, int count) {
        int n = d.length();
        int k = 0;
        while (k < count && i < n) {
            while (i < n && (d.charAt(i) == ' ' || d.charAt(i) == ',')) i++;
            int start = i;
            if (i < n && (d.charAt(i) == '-' || d.charAt(i) == '+')) i++;
            boolean dot = false;
            while (i < n) {
                char ch = d.charAt(i);
                if (ch == '.') {
                    if (dot) break;
                    dot = true; i++;
                } else if (ch >= '0' && ch <= '9') {
                    i++;
                } else {
                    break;
                }
            }
            if (i > start) out[k++] = Float.parseFloat(d.substring(start, i));
            else break;
        }
        return i;
    }

    /** 把 viewBox 0..24 的图标按 sizeDp 绘制到 (left, top) 处。 */
    public static void draw(Canvas canvas, String data, int color, float left, float top, float sizePx) {
        Path p = parse(data);
        RectF b = new RectF();
        p.computeBounds(b, true);
        float sc = sizePx / 24f;
        canvas.save();
        canvas.translate(left, top);
        canvas.scale(sc, sc);
        PAINT.setColor(color);
        PAINT.setStyle(Paint.Style.FILL);
        canvas.drawPath(p, PAINT);
        canvas.restore();
    }

    /** 生成一个自带固有尺寸的图标 View（供 LinearLayout / 导航栏直接使用）。 */
    public static IconView view(Context ctx, String data, int color, int sizeDp) {
        IconView v = new IconView(ctx);
        v.setIcon(data, color, sizeDp);
        return v;
    }

    public static final class IconView extends View {
        private String data;
        private int color = Color.BLACK;
        private int sizeDp = 24;
        private final float density;

        public IconView(Context ctx) {
            super(ctx);
            density = ctx.getResources().getDisplayMetrics().density;
            setWillNotDraw(false);
        }

        public void setIcon(String data, int color, int sizeDp) {
            this.data = data;
            this.color = color;
            this.sizeDp = sizeDp;
            requestLayout();
            invalidate();
        }

        public void setIconColor(int color) {
            this.color = color;
            invalidate();
        }

        @Override protected void onMeasure(int wSpec, int hSpec) {
            int s = Math.round(sizeDp * density);
            setMeasuredDimension(s, s);
        }

        @Override protected void onDraw(Canvas canvas) {
            if (data == null) return;
            float s = Math.min(getWidth(), getHeight());
            SvgIcon.draw(canvas, data, color, (getWidth() - s) / 2f, (getHeight() - s) / 2f, s);
        }
    }

    /** 把图标包成 Drawable，供 Button#setCompoundDrawablesWithIntrinsicBounds 之类的场景使用。 */
    public static final class Glyph extends android.graphics.drawable.Drawable {
        private final Path path;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int sizeDp;
        private final float density;
        private int color;

        public Glyph(Context ctx, String data, int color, int sizeDp) {
            this.path = parse(data);
            this.color = color;
            this.sizeDp = sizeDp;
            this.density = ctx.getResources().getDisplayMetrics().density;
            paint.setStyle(Paint.Style.FILL);
        }

        public void setColor(int color) {
            this.color = color;
            invalidateSelf();
        }

        @Override public void draw(Canvas canvas) {
            android.graphics.Rect b = getBounds();
            float s = Math.min(b.width(), b.height());
            android.graphics.RectF tmp = new android.graphics.RectF();
            path.computeBounds(tmp, true);
            float sc = s / 24f;
            int save = canvas.save();
            canvas.translate(b.left + (b.width() - s) / 2f, b.top + (b.height() - s) / 2f);
            canvas.scale(sc, sc);
            paint.setColor(color);
            canvas.drawPath(path, paint);
            canvas.restoreToCount(save);
        }

        @Override public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override public void setColorFilter(android.graphics.ColorFilter cf) {
            paint.setColorFilter(cf);
            invalidateSelf();
        }

        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }

        @Override public int getIntrinsicWidth() { return Math.round(sizeDp * density); }

        @Override public int getIntrinsicHeight() { return Math.round(sizeDp * density); }
    }
}
