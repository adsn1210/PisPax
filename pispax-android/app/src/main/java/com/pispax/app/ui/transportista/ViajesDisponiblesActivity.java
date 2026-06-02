package com.pispax.app.ui.transportista;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Lista de viajes PENDIENTES compatibles con los vehículos del transportista.
// Llama a GET /api/viajes/disponibles. Tap en un viaje → DetalleViajeActivity
// donde el transportista puede aceptarlo.
public class ViajesDisponiblesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viajes_disponibles);
        // TODO: implementar RecyclerView con ViajesAdapter
    }
}
