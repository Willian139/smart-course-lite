package com.zuwe.smartcourselite;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** 从抓包 ZIP 里挑出请求/响应 JSON，限制条目数与体积。 */
public final class CaptureZipReader {
    private static final int MAX_ENTRIES = 1200;
    private static final int MAX_ENTRY_BYTES = 2097152;
    private static final int MAX_TOTAL_BYTES = 20971520;

    private CaptureZipReader() {
    }

    public static String read(Context context, Uri uri) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        InputStream input = resolver.openInputStream(uri);
        if (input == null) {
            throw new IllegalArgumentException("无法打开所选文件");
        }
        ZipInputStream zip = new ZipInputStream(input);
        try {
            StringBuilder out = new StringBuilder();
            byte[] buffer = new byte[8192];
            int entries = 0;
            int kept = 0;
            int total = 0;
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries++;
                if (entries > MAX_ENTRIES) {
                    throw new IllegalArgumentException("ZIP 文件条目过多");
                }
                if (entry.isDirectory() || !isCaptureMetadata(entry.getName())) {
                    zip.closeEntry();
                    continue;
                }
                ByteArrayOutputStream item = new ByteArrayOutputStream();
                int size = 0;
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    size += read;
                    total += read;
                    if (size > MAX_ENTRY_BYTES || total > MAX_TOTAL_BYTES) {
                        throw new IllegalArgumentException("抓包元数据超过安全读取上限");
                    }
                    item.write(buffer, 0, read);
                }
                if (size > 0) {
                    out.append('\n')
                            .append(new String(item.toByteArray(), StandardCharsets.UTF_8))
                            .append('\n');
                    kept++;
                }
                zip.closeEntry();
            }
            if (kept == 0) {
                throw new IllegalArgumentException("ZIP 中没有可识别的请求/响应元数据");
            }
            return out.toString();
        } finally {
            zip.close();
            input.close();
        }
    }

    private static boolean isCaptureMetadata(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith("request.json") || lower.endsWith("response.json")
                || lower.endsWith("request.hcy") || lower.endsWith("response.hcy");
    }
}
