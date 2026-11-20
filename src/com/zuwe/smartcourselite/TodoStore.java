package com.zuwe.smartcourselite;

import android.content.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class TodoStore {
    private static final String PREFIX = "day_";
    private static final String PREFS = "smartcourse_todo";

    public static final class Item {
        public final boolean done;
        public final String text;
        public final String time;

        public Item(String str, String str2, boolean z) {
            this.time = str == null ? "" : str;
            this.text = str2 == null ? "" : str2;
            this.done = z;
        }
    }

    private TodoStore() {
    }

    private static String key(LocalDate localDate) {
        return PREFIX + localDate.toString();
    }

    public static List<Item> load(Context context, LocalDate localDate) {
        String string;
        ArrayList arrayList = new ArrayList();
        if (context != null && localDate != null && (string = context.getSharedPreferences(PREFS, 0).getString(key(localDate), "")) != null && !string.isEmpty()) {
            try {
                JSONArray jSONArray = new JSONArray(string);
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        arrayList.add(new Item(optJSONObject.optString("time", ""), optJSONObject.optString(ReminderReceiver.EXTRA_TEXT, ""), optJSONObject.optBoolean("done", false)));
                    }
                }
            } catch (Exception unused) {
            }
        }
        return arrayList;
    }

    public static void save(Context context, LocalDate localDate, List<Item> list) {
        if (context == null || localDate == null) {
            return;
        }
        JSONArray jSONArray = new JSONArray();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Item item = list.get(i);
                if (item != null) {
                    JSONObject jSONObject = new JSONObject();
                    try {
                        jSONObject.put("time", item.time);
                        jSONObject.put(ReminderReceiver.EXTRA_TEXT, item.text);
                        jSONObject.put("done", item.done);
                    } catch (Exception unused) {
                    }
                    jSONArray.put(jSONObject);
                }
            }
        }
        context.getSharedPreferences(PREFS, 0).edit().putString(key(localDate), jSONArray.toString()).apply();
    }

    public static int minutesOf(String str) {
        int parseInt;
        int parseInt2;
        if (str == null) {
            return -1;
        }
        String trim = str.trim();
        if (trim.isEmpty()) {
            return -1;
        }
        int indexOf = trim.indexOf(58);
        if (indexOf < 0) {
            indexOf = trim.indexOf(65306);
        }
        if (indexOf > 0) {
            parseInt = Integer.parseInt(trim.substring(0, indexOf).trim());
            parseInt2 = Integer.parseInt(trim.substring(indexOf + 1).trim());
        } else if (trim.length() == 4) {
            parseInt = Integer.parseInt(trim.substring(0, 2));
            parseInt2 = Integer.parseInt(trim.substring(2));
        } else {
            if (trim.length() == 3) {
                parseInt = Integer.parseInt(trim.substring(0, 1));
                parseInt2 = Integer.parseInt(trim.substring(1));
            }
            return -1;
        }
        if (parseInt >= 0 && parseInt <= 23 && parseInt2 >= 0 && parseInt2 <= 59) {
            return (parseInt * 60) + parseInt2;
        }
        return -1;
    }

    public static String normalize(String str) {
        int minutesOf = minutesOf(str);
        return minutesOf < 0 ? "" : String.format(Locale.US, "%02d:%02d", Integer.valueOf(minutesOf / 60), Integer.valueOf(minutesOf % 60));
    }
}
