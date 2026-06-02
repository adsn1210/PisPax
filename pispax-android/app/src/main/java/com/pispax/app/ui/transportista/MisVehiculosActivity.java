package com.pispax.app.ui.transportista;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Lista de vehículos del transportista (GET /api/vehiculos/mis-vehiculos).
// Incluye FAB para ir a CrearVehiculoActivity.
public class MisVehiculosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_vehiculos);
        // TODO: implementar RecyclerView con VehiculosAdapter
    }
}
