package com.zuwe.smartcourselite;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 把运行日志写到 Download/DSH-GO/jw_log.txt，方便排查（无存储权限时退到应用私有目录）。 */
public final class JwLog {

    private static final StringBuilder BUF = new StringBuilder();
    private static Uri uri;
    private static File file;

    public static synchronized void w(Context ctx, String tag, String msg) {
        String line = stamp() + " [" + tag + "] " + (msg == null ? "" : msg);
        if (BUF.length() > 60000) BUF.setLength(0);
        BUF.append(line).append('\n');
        flush(ctx);
    }

    public static synchronized String dump() {
        return BUF.toString();
    }

    private static String stamp() {
        try {
            return new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date());
        } catch (Throwable t) {
            return "";
        }
    }

    private static void flush(Context ctx) {
        if (ctx == null) return;
        String text = BUF.toString();
        byte[] data = text.getBytes();
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                ContentResolver cr = ctx.getContentResolver();
                if (uri == null) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "jw_log.txt");
                    cv.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                    cv.put(MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/DSH-GO");
                    uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                }
                if (uri != null) {
                    OutputStream os = cr.openOutputStream(uri, "wt");
                    if (os != null) {
                        os.write(data);
                        os.flush();
                        os.close();
                    }
                    return;
                }
            } catch (Throwable t) {
                uri = null;
            }
        }
        try {
            if (file == null) {
                File dir = ctx.getExternalFilesDir(null);
                if (dir == null) dir = ctx.getFilesDir();
                file = new File(dir, "jw_log.txt");
            }
            FileOutputStream fo = new FileOutputStream(file, false);
            fo.write(data);
            fo.flush();
            fo.close();
        } catch (Throwable t) {
            // 日志失败不影响主流程
        }
    }
}