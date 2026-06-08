package com.pispax.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SessionManager() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Sesion JWT ───────────────────────────────────────────────────────────

    public static void guardarSesion(Context ctx, String token, long userId,
                                     String rol, String nombre) {
        SharedPreferences.Editor editor = prefs(ctx).edit();
        editor.putString(Constants.KEY_TOKEN,  token);
        editor.putLong(Constants.KEY_USER_ID,  userId);
        editor.putString(Constants.KEY_ROL,    rol);
        editor.putString(Constants.KEY_NOMBRE, nombre);
        editor.apply();
    }

    public static String getToken(Context ctx) {
        return prefs(ctx).getString(Constants.KEY_TOKEN, null);
    }

    public static long getUserId(Context ctx) {
        return prefs(ctx).getLong(Constants.KEY_USER_ID, -1);
    }

    public static String getRol(Context ctx) {
        return prefs(ctx).getString(Constants.KEY_ROL, null);
    }

    public static String getNombre(Context ctx) {
        return prefs(ctx).getString(Constants.KEY_NOMBRE, null);
    }

    public static boolean haySesion(Context ctx) {
        return getToken(ctx) != null;
    }

    public static void cerrarSesion(Context ctx) {
        // Conserva la preferencia de tema al cerrar sesion
        boolean darkMode = isDarkMode(ctx);
        prefs(ctx).edit().clear().apply();
        setDarkMode(ctx, darkMode);
    }

    // ── Tema claro / oscuro ──────────────────────────────────────────────────

    public static boolean isDarkMode(Context ctx) {
        return prefs(ctx).getBoolean(Constants.KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context ctx, boolean dark) {
        prefs(ctx).edit().putBoolean(Constants.KEY_DARK_MODE, dark).apply();
    }
}
