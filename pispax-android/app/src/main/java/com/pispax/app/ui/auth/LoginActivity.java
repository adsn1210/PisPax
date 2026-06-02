package com.pispax.app.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Formulario de login. Llama a POST /api/auth/login, guarda JWT en SessionManager
// y redirige al Home según el rol recibido en la respuesta.
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // TODO: implementar lógica de login con Retrofit
    }
}
