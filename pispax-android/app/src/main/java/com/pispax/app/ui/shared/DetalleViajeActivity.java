package com.pispax.app.ui.shared;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Ficha completa de un viaje. Recibe el ID por Intent (Constants.EXTRA_VIAJE_ID)
// y carga los datos con GET /api/viajes/{id}.
//
// Comportamiento según rol (leído de SessionManager):
//   CLIENTE:       ve el estado actual, puede cancelar, accede a MapaViajeActivity
//   TRANSPORTISTA: ve el estado y tiene botón para avanzar al siguiente estado
//                  (PUT /api/viajes/{id}/estado) o cancelar
public class DetalleViajeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_viaje);
        // TODO: implementar carga de datos y lógica de botones según rol
    }
}
