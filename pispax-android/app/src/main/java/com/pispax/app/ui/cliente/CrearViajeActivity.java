package com.pispax.app.ui.cliente;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Formulario para crear un nuevo viaje. Campos: tipo de mercancía (Spinner cargado
// desde GET /api/tipo-mercancia), descripción, dirección recogida, dirección entrega,
// peso en kg. Llama a POST /api/viajes.
public class CrearViajeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_viaje);
        // TODO: cargar tipos de mercancía y enviar el formulario
    }
}
