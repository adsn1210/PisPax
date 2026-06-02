package com.pispax.app.ui.transportista;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Lista de viajes que el transportista ha aceptado (GET /api/viajes).
// Tap en un viaje → DetalleViajeActivity donde puede avanzar el estado.
public class MisViajesTransportistaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes_transportista);
        // TODO: implementar RecyclerView con ViajesAdapter
    }
}
