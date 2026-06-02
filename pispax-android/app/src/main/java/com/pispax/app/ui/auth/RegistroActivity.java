package com.pispax.app.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Formulario de registro. Incluye selector de rol (CLIENTE / TRANSPORTISTA).
// Llama a POST /api/auth/registro y navega a LoginActivity tras el éxito.
public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        // TODO: implementar lógica de registro con Retrofit
    }
}
