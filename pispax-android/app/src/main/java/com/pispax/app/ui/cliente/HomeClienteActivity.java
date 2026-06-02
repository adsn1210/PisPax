package com.pispax.app.ui.cliente;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Dashboard del cliente. Punto de entrada tras el login con rol CLIENTE.
// Acciones: Nuevo Viaje, Mis Viajes, Cerrar Sesión.
public class HomeClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_cliente);
        // TODO: implementar navegación y resumen de viajes activos
    }
}
