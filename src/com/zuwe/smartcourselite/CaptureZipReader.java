package com.zuwe.smartcourselite;

import java.util.Locale;

/* loaded from: classes.dex */
public final class CaptureZipReader {
    private static final int MAX_ENTRIES = 1200;
    private static final int MAX_ENTRY_BYTES = 2097152;
    private static final int MAX_TOTAL_BYTES = 20971520;

    private CaptureZipReader() {
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x005b, code lost:
    
        throw new java.lang.IllegalArgumentException("抓包元数据超过安全读取上限");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String read(android.content.Context r10, android.net.Uri r11) throws java.lang.Exception {
        /*
            android.content.ContentResolver r10 = r10.getContentResolver()
            java.io.InputStream r10 = r10.openInputStream(r11)
            if (r10 == 0) goto La3
            java.util.zip.ZipInputStream r11 = new java.util.zip.ZipInputStream
            r11.<init>(r10)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L9b
            r0.<init>()     // Catch: java.lang.Throwable -> L9b
            r1 = 8192(0x2000, float:1.148E-41)
            byte[] r1 = new byte[r1]     // Catch: java.lang.Throwable -> L9b
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
        L1c:
            java.util.zip.ZipEntry r6 = r11.getNextEntry()     // Catch: java.lang.Throwable -> L9b
            if (r6 == 0) goto L86
            int r4 = r4 + 1
            r7 = 1200(0x4b0, float:1.682E-42)
            if (r4 > r7) goto L7e
            boolean r7 = r6.isDirectory()     // Catch: java.lang.Throwable -> L9b
            if (r7 != 0) goto L7a
            java.lang.String r6 = r6.getName()     // Catch: java.lang.Throwable -> L9b
            boolean r6 = isCaptureMetadata(r6)     // Catch: java.lang.Throwable -> L9b
            if (r6 != 0) goto L39
            goto L7a
        L39:
            java.io.ByteArrayOutputStream r6 = new java.io.ByteArrayOutputStream     // Catch: java.lang.Throwable -> L9b
            r6.<init>()     // Catch: java.lang.Throwable -> L9b
            r7 = 0
        L3f:
            int r8 = r11.read(r1)     // Catch: java.lang.Throwable -> L9b
            r9 = -1
            if (r8 == r9) goto L5c
            int r7 = r7 + r8
            int r5 = r5 + r8
            r9 = 2097152(0x200000, float:2.938736E-39)
            if (r7 > r9) goto L54
            r9 = 20971520(0x1400000, float:3.526483E-38)
            if (r5 > r9) goto L54
            r6.write(r1, r2, r8)     // Catch: java.lang.Throwable -> L9b
            goto L3f
        L54:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException     // Catch: java.lang.Throwable -> L9b
            java.lang.String r1 = "抓包元数据超过安全读取上限"
            r0.<init>(r1)     // Catch: java.lang.Throwable -> L9b
            throw r0     // Catch: java.lang.Throwable -> L9b
        L5c:
            if (r7 <= 0) goto L76
            r7 = 10
            r0.append(r7)     // Catch: java.lang.Throwable -> L9b
            java.lang.String r8 = new java.lang.String     // Catch: java.lang.Throwable -> L9b
            byte[] r6 = r6.toByteArray()     // Catch: java.lang.Throwable -> L9b
            java.nio.charset.Charset r9 = java.nio.charset.StandardCharsets.UTF_8     // Catch: java.lang.Throwable -> L9b
            r8.<init>(r6, r9)     // Catch: java.lang.Throwable -> L9b
            r0.append(r8)     // Catch: java.lang.Throwable -> L9b
            r0.append(r7)     // Catch: java.lang.Throwable -> L9b
            int r3 = r3 + 1
        L76:
            r11.closeEntry()     // Catch: java.lang.Throwable -> L9b
            goto L1c
        L7a:
            r11.closeEntry()     // Catch: java.lang.Throwable -> L9b
            goto L1c
        L7e:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException     // Catch: java.lang.Throwable -> L9b
            java.lang.String r1 = "ZIP 文件条目过多"
            r0.<init>(r1)     // Catch: java.lang.Throwable -> L9b
            throw r0     // Catch: java.lang.Throwable -> L9b
        L86:
            if (r3 == 0) goto L93
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Throwable -> L9b
            r11.close()
            r10.close()
            return r0
        L93:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException     // Catch: java.lang.Throwable -> L9b
            java.lang.String r1 = "ZIP 中没有可识别的请求/响应元数据"
            r0.<init>(r1)     // Catch: java.lang.Throwable -> L9b
            throw r0     // Catch: java.lang.Throwable -> L9b
        L9b:
            r0 = move-exception
            r11.close()
            r10.close()
            throw r0
        La3:
            java.lang.IllegalArgumentException r10 = new java.lang.IllegalArgumentException
            java.lang.String r11 = "无法打开所选文件"
            r10.<init>(r11)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.zuwe.smartcourselite.CaptureZipReader.read(android.content.Context, android.net.Uri):java.lang.String");
    }

    private static boolean isCaptureMetadata(String str) {
        String lowerCase = str.toLowerCase(Locale.ROOT);
        return lowerCase.endsWith("request.json") || lowerCase.endsWith("response.json") || lowerCase.endsWith("request.hcy") || lowerCase.endsWith("response.hcy");
    }
}
