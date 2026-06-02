package com.pispax.app.ui.cliente;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Lista de viajes del cliente (GET /api/viajes). Muestra estado de cada viaje.
// Tap en un viaje → DetalleViajeActivity.
public class MisViajesClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes_cliente);
        // TODO: implementar RecyclerView con ViajesAdapter
    }
}
