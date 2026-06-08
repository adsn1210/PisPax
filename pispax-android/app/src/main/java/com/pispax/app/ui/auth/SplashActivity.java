package com.pispax.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.pispax.app.R;
import com.pispax.app.ui.cliente.HomeClienteActivity;
import com.pispax.app.ui.transportista.HomeTransportistaActivity;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

// Pantalla de carga inicial. Muestra el logo 1,2 segundos y luego redirige:
// - Si hay sesion guardada → Home del rol correspondiente (Cliente o Transportista)
// - Si no hay sesion     → LoginActivity
public class SplashActivity extends AppCompatActivity {

    // Tiempo en milisegundos que se muestra el logo antes de redirigir
    private static final int SPLASH_DELAY_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Espera el tiempo indicado en el hilo principal y luego redirige
        new Handler(Looper.getMainLooper()).postDelayed(() -> redirigir(), SPLASH_DELAY_MS);
    }

    // Decide a que pantalla ir segun el token guardado en SessionManager
    private void redirigir() {
        Intent destino;

        if (SessionManager.haySesion(this)) {
            // Hay sesion activa: redirige al home del rol guardado
            String rol = SessionManager.getRol(this);
            if (Constants.ROL_TRANSPORTISTA.equals(rol)) {
                destino = new Intent(this, HomeTransportistaActivity.class);
            } else {
                destino = new Intent(this, HomeClienteActivity.class);
            }
        } else {
            // No hay sesion: el usuario debe hacer login
            destino = new Intent(this, LoginActivity.class);
        }

        startActivity(destino);
        // finish() elimina SplashActivity de la pila, asi el boton atras no vuelve al splash
        finish();
    }
}
