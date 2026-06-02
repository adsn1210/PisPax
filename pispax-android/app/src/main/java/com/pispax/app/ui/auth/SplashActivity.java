package com.pispax.app.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Pantalla inicial. Comprueba si hay sesión guardada en SessionManager:
//   - Con sesión válida → redirige a HomeClienteActivity o HomeTransportistaActivity según rol
//   - Sin sesión        → redirige a LoginActivity
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // TODO: implementar lógica de redirección
    }
}
