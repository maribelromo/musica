package com.goosebay.musica;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

/**
 * Created by maribel on 2016-12-29.
 */

public final class SharedPreferencesManager {
    private static final String PREFS_FILE_NAME = SharedPreferencesManager.class.getName();

    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRATION_DATE = "expiration_date";

    private static final String FIRST_LOGIN = "first_login";

    private SharedPreferencesManager(){}

    private static SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the access token in the app's private shared preferences.
     *
     * @param accessToken token
     * @param expiresIn how long the token is valid for in seconds
     *
     */
    public static void setAccessToken(Context context, String accessToken, long expiresIn) {
        Context appContext = context.getApplicationContext();

        long now = System.currentTimeMillis();
        long expirationDate = now + TimeUnit.SECONDS.toMillis(expiresIn);

        SharedPreferences sharedPref = getSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.putLong(EXPIRATION_DATE, expirationDate);
        editor.apply();
    }

    /**
     * Saves the access token in the app's private shared preferences.
     *
     * @return  returns the previously stored token or null if no token was saved
     *          or has expired.
     *
     */
    public static String getToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);

        String token = sharedPref.getString(ACCESS_TOKEN, null);
        long expirationDate = sharedPref.getLong(EXPIRATION_DATE, 0L);

        // Check if the token has expired
        if (token == null || expirationDate < System.currentTimeMillis()) {
            return null;
        }

        return token;
    }

    /**
     * Set to false if the user has previously logged.
     */
    public static void setFirstLogin(Context context, boolean firstLogin) {
        Context appContext = context.getApplicationContext();

        SharedPreferences sharedPref = getSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(FIRST_LOGIN, firstLogin);
        editor.apply();
    }

    /**
     * Returns true if the user has not logged in before.
     */
    public static boolean isFirstLogin(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);

        return sharedPref.getBoolean(FIRST_LOGIN, true);
    }
}
