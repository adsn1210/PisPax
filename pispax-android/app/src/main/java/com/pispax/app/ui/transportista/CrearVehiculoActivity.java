package com.pispax.app.ui.transportista;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pispax.app.R;

// Formulario para dar de alta un nuevo vehículo. Campos: matrícula, marca, modelo,
// tipo de vehículo (Spinner), capacidad en kg, carnet requerido (Spinner).
// Llama a POST /api/vehiculos.
public class CrearVehiculoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_vehiculo);
        // TODO: implementar formulario y llamada Retrofit
    }
}
