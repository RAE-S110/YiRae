package com.example.yirae;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserSettingsRepository {
    private static final String PREFS_NAME = "user_settings";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PRIVACY_ACKNOWLEDGED = "privacy_acknowledged";

    private UserSettingsRepository() {
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isConfigured(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return !preferences.getString(KEY_NICKNAME, "").isEmpty()
                && !preferences.getString(KEY_PASSWORD, "").isEmpty()
                && preferences.getBoolean(KEY_PRIVACY_ACKNOWLEDGED, false);
    }

    public static void saveInitialSettings(Context context, String nickname, String password) {
        getPreferences(context).edit()
                .putString(KEY_NICKNAME, safe(nickname))
                .putString(KEY_PASSWORD, safe(password))
                .putBoolean(KEY_PRIVACY_ACKNOWLEDGED, true)
                .apply();
    }

    public static void updateNickname(Context context, String nickname) {
        getPreferences(context).edit()
                .putString(KEY_NICKNAME, safe(nickname))
                .apply();
    }

    public static void updatePassword(Context context, String password) {
        getPreferences(context).edit()
                .putString(KEY_PASSWORD, safe(password))
                .apply();
    }

    public static boolean verifyPassword(Context context, String password) {
        return getPassword(context).equals(safe(password));
    }

    public static String getNickname(Context context) {
        return getPreferences(context).getString(KEY_NICKNAME, "");
    }

    public static String getPassword(Context context) {
        return getPreferences(context).getString(KEY_PASSWORD, "");
    }

    public static boolean isPrivacyAcknowledged(Context context) {
        return getPreferences(context).getBoolean(KEY_PRIVACY_ACKNOWLEDGED, false);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
