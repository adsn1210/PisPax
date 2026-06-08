package com.pispax.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.pispax.app.util.SessionManager;

import org.osmdroid.config.Configuration;

// Clase Application: es el punto de entrada de toda la app.
// Android la instancia ANTES que cualquier Activity.
// Se usa para inicializar librerias que necesitan el contexto global.
public class PispaxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // OSMDroid (la libreria de mapas) necesita el nombre del paquete como identificador
        // para cachear los tiles del mapa. Sin esto el mapa no funciona.
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Aplica el tema claro u oscuro guardado antes de que se abra ninguna pantalla
        applyTheme();
    }

    // Lee la preferencia guardada y aplica el modo claro/oscuro globalmente
    public void applyTheme() {
        boolean dark = SessionManager.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                     : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
