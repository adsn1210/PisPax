package com.pispax.app.ui.cliente;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pispax.app.R;
import com.pispax.app.model.TipoMercanciaDTO;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.model.request.CrearViajeRequest;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.TipoMercanciaAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Formulario para solicitar un nuevo viaje. Carga el catalogo de mercancias del backend.
public class CrearViajeActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_crear_viaje.xml) ────────────────
    private Spinner spinnerMercancia;
    private TextInputEditText etDescripcion, etRecogida, etEntrega, etPeso;
    private MaterialButton btnCrear, btnVolver;
    private ProgressBar progress;

    private List<TipoMercanciaDTO> tiposMercancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_viaje);

        spinnerMercancia = findViewById(R.id.spinner_mercancia);
        etDescripcion    = findViewById(R.id.et_descripcion);
        etRecogida       = findViewById(R.id.et_recogida);
        etEntrega        = findViewById(R.id.et_entrega);
        etPeso           = findViewById(R.id.et_peso);
        btnCrear         = findViewById(R.id.btn_crear);
        btnVolver        = findViewById(R.id.btn_volver);
        progress         = findViewById(R.id.progress_crear);

        btnVolver.setOnClickListener(v -> finish());
        btnCrear.setOnClickListener(v -> intentarCrear());

        cargarTiposMercancia();
    }

    // Carga el catalogo de tipos de mercancia para el Spinner (sin token necesario)
    private void cargarTiposMercancia() {
        RetrofitClient.getInstance(this).getApi()
                .getTiposMercancia()
                .enqueue(new Callback<List<TipoMercanciaDTO>>() {
                    @Override
                    public void onResponse(Call<List<TipoMercanciaDTO>> call,
                                           Response<List<TipoMercanciaDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tiposMercancia = response.body();
                            // MODIFICAR: el adaptador del spinner en TipoMercanciaAdapter.java
                            spinnerMercancia.setAdapter(
                                    new TipoMercanciaAdapter(CrearViajeActivity.this, tiposMercancia));
                        } else {
                            Toast.makeText(CrearViajeActivity.this,
                                    "No se pudo cargar el catalogo de mercancias",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TipoMercanciaDTO>> call, Throwable t) {
                        Toast.makeText(CrearViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Valida los campos obligatorios, construye el request y llama a POST /viajes.
    // Si tiene exito cierra el formulario; HomeClienteActivity recargara la lista en onResume.
    private void intentarCrear() {
        if (tiposMercancia == null || tiposMercancia.isEmpty()) {
            Toast.makeText(this, "Espera a que se cargue el catalogo", Toast.LENGTH_SHORT).show();
            return;
        }

        String recogida = texto(etRecogida);
        String entrega  = texto(etEntrega);
        String pesoStr  = texto(etPeso);

        if (TextUtils.isEmpty(recogida) || TextUtils.isEmpty(entrega) || TextUtils.isEmpty(pesoStr)) {
            Toast.makeText(this, "Rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoStr);
            if (peso <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El peso debe ser un numero mayor que 0", Toast.LENGTH_SHORT).show();
            return;
        }

        TipoMercanciaDTO tipoSeleccionado = tiposMercancia.get(spinnerMercancia.getSelectedItemPosition());

        setUiCargando(true);

        CrearViajeRequest req = new CrearViajeRequest(
                tipoSeleccionado.getId(),
                texto(etDescripcion).isEmpty() ? null : texto(etDescripcion),
                recogida,
                entrega,
                peso
        );

        RetrofitClient.getInstance(this).getApi()
                .crearViaje(req)
                .enqueue(new Callback<ViajeDTO>() {
                    @Override
                    public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                        setUiCargando(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(CrearViajeActivity.this,
                                    R.string.crear_viaje_ok, Toast.LENGTH_LONG).show();
                            finish();
                        } else if (response.code() == 403) {
                            Toast.makeText(CrearViajeActivity.this,
                                    "Solo los clientes pueden crear viajes", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CrearViajeActivity.this,
                                    R.string.error_servidor, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViajeDTO> call, Throwable t) {
                        setUiCargando(false);
                        Toast.makeText(CrearViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUiCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnCrear.setEnabled(!cargando);
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
