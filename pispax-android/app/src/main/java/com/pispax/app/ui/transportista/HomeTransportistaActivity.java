package com.pispax.app.ui.transportista;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Dashboard del transportista. Punto de entrada tras el login con rol TRANSPORTISTA.
// Acciones: Viajes Disponibles, Mis Viajes, Mis Vehículos, Cerrar Sesión.
public class HomeTransportistaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transportista);
        // TODO: implementar navegación
    }
}
