package com.pispax.app.util;

public class Constants {

    // 10.0.2.2 es la direccion que el emulador Android usa para llegar a localhost del PC
    // Si usas un dispositivo fisico, cambialo por la IP WiFi de tu PC (ej: 192.168.1.X)
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // Claves para SharedPreferences (SessionManager)
    public static final String PREFS_NAME  = "pispax_prefs";
    public static final String KEY_TOKEN   = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_ROL     = "user_rol";
    public static final String KEY_NOMBRE  = "user_nombre";

    // Preferencia de tema claro/oscuro
    public static final String KEY_DARK_MODE = "dark_mode";

    // Valores de rol (deben coincidir con el ENUM del backend)
    public static final String ROL_CLIENTE       = "CLIENTE";
    public static final String ROL_TRANSPORTISTA = "TRANSPORTISTA";

    // Clave para pasar el ID de un viaje entre Activities via Intent
    public static final String EXTRA_VIAJE_ID = "viaje_id";
}
