package com.pispax.app.ui.transportista;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pispax.app.R;
import com.pispax.app.model.VehiculoDTO;
import com.pispax.app.model.request.CrearVehiculoRequest;
import com.pispax.app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Formulario para registrar un nuevo vehiculo del transportista.
public class CrearVehiculoActivity extends AppCompatActivity {

    // Valores enviados al backend (deben coincidir con los Java enums del backend)
    private static final String[] TIPOS_VEHICULO = {
            "FURGONETA", "FURGON_GRANDE", "FRIGORIFICO",
            "CAMION_LIGERO", "CAMION_PESADO", "CAMION_ARTICULADO", "PLATAFORMA"
    };
    // Labels legibles para el spinner (mismo orden que TIPOS_VEHICULO)
    private static final String[] TIPOS_VEHICULO_LABELS = {
            "Furgoneta", "Furgon Grande", "Frigorifico",
            "Camion Ligero", "Camion Pesado", "Camion Articulado", "Plataforma"
    };
    private static final String[] CARNETS = {"B", "C1", "C", "C_E"};
    // Labels legibles para el spinner de carnet
    private static final String[] CARNETS_LABELS = {"B", "C1", "C", "C+E"};

    // ── VISTAS (modificar estilo en activity_crear_vehiculo.xml) ─────────────
    private TextInputEditText etMatricula, etMarca, etModelo, etSubtipo,
            etCapacidad, etTara, etMma;
    private Spinner spinnerTipo, spinnerCarnet;
    private MaterialButton btnGuardar, btnVolver;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_vehiculo);

        bindVistas();
        configurarSpinners();

        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> intentarGuardar());
    }

    private void bindVistas() {
        etMatricula  = findViewById(R.id.et_matricula);
        etMarca      = findViewById(R.id.et_marca);
        etModelo     = findViewById(R.id.et_modelo);
        etSubtipo    = findViewById(R.id.et_subtipo);
        etCapacidad  = findViewById(R.id.et_capacidad);
        etTara       = findViewById(R.id.et_tara);
        etMma        = findViewById(R.id.et_mma);
        spinnerTipo  = findViewById(R.id.spinner_tipo);
        spinnerCarnet= findViewById(R.id.spinner_carnet);
        btnGuardar   = findViewById(R.id.btn_guardar);
        btnVolver    = findViewById(R.id.btn_volver);
        progress     = findViewById(R.id.progress_vehiculo);
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, TIPOS_VEHICULO_LABELS);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        ArrayAdapter<String> adapterCarnet = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CARNETS_LABELS);
        adapterCarnet.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarnet.setAdapter(adapterCarnet);
    }

    // Valida los campos obligatorios, construye el request y lo envia al backend.
    private void intentarGuardar() {
        String matricula    = texto(etMatricula);
        String marca        = texto(etMarca);
        String modelo       = texto(etModelo);
        String capacidadStr = texto(etCapacidad);

        // Comprobacion de campos obligatorios antes de hacer la peticion
        if (TextUtils.isEmpty(matricula) || TextUtils.isEmpty(marca)
                || TextUtils.isEmpty(modelo) || TextUtils.isEmpty(capacidadStr)) {
            Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double capacidad;
        try {
            capacidad = Double.parseDouble(capacidadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Capacidad invalida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tara y MMA son opcionales: si estan vacios se envia null al backend
        Double tara = parseDouble(texto(etTara));
        Double mma  = parseDouble(texto(etMma));

        setUiCargando(true);

        // La posicion del spinner se usa como indice en el array de valores del backend
        CrearVehiculoRequest req = new CrearVehiculoRequest(
                matricula.toUpperCase(), marca, modelo,
                TIPOS_VEHICULO[spinnerTipo.getSelectedItemPosition()],
                texto(etSubtipo).isEmpty() ? null : texto(etSubtipo),
                tara, capacidad, mma,
                CARNETS[spinnerCarnet.getSelectedItemPosition()]
        );

        RetrofitClient.getInstance(this).getApi()
                .crearVehiculo(req)
                .enqueue(new Callback<VehiculoDTO>() {
                    @Override
                    public void onResponse(Call<VehiculoDTO> call, Response<VehiculoDTO> response) {
                        setUiCargando(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(CrearVehiculoActivity.this,
                                    R.string.crear_vehiculo_ok, Toast.LENGTH_LONG).show();
                            // Cierra el formulario y vuelve a MisVehiculos (onResume recarga la lista)
                            finish();
                        } else {
                            // Leer el mensaje real que devuelve el backend
                            String mensajeError = getString(R.string.error_servidor);
                            try {
                                String cuerpo = response.errorBody().string();
                                JSONObject json = new JSONObject(cuerpo);
                                mensajeError = json.getString("mensaje");
                            } catch (Exception ignored) {}
                            Toast.makeText(CrearVehiculoActivity.this,
                                    mensajeError, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VehiculoDTO> call, Throwable t) {
                        setUiCargando(false);
                        Toast.makeText(CrearVehiculoActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUiCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Double parseDouble(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }
}
