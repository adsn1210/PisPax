package com.pispax.app.ui.shared;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Simulación visual del viaje. Ver PLANTEAMIENTO_SIMULACION_VIAJE.md para la
// arquitectura completa de esta pantalla.
//
// Componentes:
//   - MapView (OSMDroid) con marcador de camión que se mueve entre origen y destino
//   - Timeline de estados (RecyclerView vertical o vista custom)
//   - Botón "Iniciar simulación" → POST /api/viajes/{id}/simular
//   - Handler de polling cada 3 segundos → GET /api/viajes/{id}
//     el polling se detiene cuando estado == COMPLETADO o CANCELADO
public class MapaViajeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_viaje);
        // TODO: implementar mapa OSMDroid + polling + animación
    }
}
