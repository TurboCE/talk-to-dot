package local.zipshortcut;

import android.content.Context;

final class DotSettings {
    static final String DEFAULT_NAME = "zip";

    static String name(Context context) {
        String name = context.getSharedPreferences("shortcut", 0)
                .getString("dotName", DEFAULT_NAME);
        return name == null || name.trim().isEmpty() ? DEFAULT_NAME : name.trim();
    }

    static String entry(Context context) {
        String value=context.getSharedPreferences("shortcut",0).getString("dotEntry","");
        return value==null || value.trim().isEmpty()?name(context):value.trim();
    }
    static void save(Context context, String name) {
        context.getSharedPreferences("shortcut", 0).edit()
                .putString("dotName", name.trim()).apply();
    }
}
