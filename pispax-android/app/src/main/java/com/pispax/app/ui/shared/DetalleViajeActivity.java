package com.pispax.app.ui.shared;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.pispax.app.R;
import com.pispax.app.model.VehiculoDTO;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.model.request.AceptarViajeRequest;
import com.pispax.app.model.request.ActualizarEstadoRequest;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.VehiculosAdapter;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Ficha completa de un viaje. Adapta los controles al rol del usuario autenticado.
public class DetalleViajeActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_detalle_viaje.xml) ──────────────
    private MaterialButton btnVolver, btnVerMapa, btnAceptar, btnAvanzar;
    private TextView tvTituloViaje, tvEstadoChip, tvRutaRecogida, tvRutaEntrega;
    private TextView tvTipoMercancia, tvDescripcionMercancia, tvPesoDetalle;
    private TextView tvClienteNombre, tvTransportista, tvVehiculoDetalle;
    private ProgressBar progressDetalle;

    private long viajeId;
    private ViajeDTO viajeActual;

    // Secuencia de transiciones validas (definida en el backend)
    private static final String[] ESTADOS_SECUENCIA = {
            "ACEPTADO", "SALIDA_RECOGIDA", "LLEGADA_RECOGIDA",
            "MERCANCIA_RECOGIDA", "SALIDA_ENTREGA", "LLEGADA_ENTREGA", "COMPLETADO"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_viaje);

        viajeId = getIntent().getLongExtra(Constants.EXTRA_VIAJE_ID, -1);
        if (viajeId == -1) { finish(); return; }

        bindVistas();

        btnVolver.setOnClickListener(v -> finish());
        btnVerMapa.setOnClickListener(v -> abrirMapa());
        btnAceptar.setOnClickListener(v -> mostrarDialogoAceptar());
        btnAvanzar.setOnClickListener(v -> avanzarEstado());

        cargarDetalle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDetalle();
    }

    private void bindVistas() {
        btnVolver           = findViewById(R.id.btn_volver);
        btnVerMapa          = findViewById(R.id.btn_ver_mapa);
        btnAceptar          = findViewById(R.id.btn_aceptar);
        btnAvanzar          = findViewById(R.id.btn_avanzar);
        tvTituloViaje       = findViewById(R.id.tv_titulo_viaje);
        tvEstadoChip        = findViewById(R.id.tv_estado_chip);
        tvRutaRecogida      = findViewById(R.id.tv_ruta_recogida);
        tvRutaEntrega       = findViewById(R.id.tv_ruta_entrega);
        tvTipoMercancia     = findViewById(R.id.tv_tipo_mercancia);
        tvDescripcionMercancia = findViewById(R.id.tv_descripcion_mercancia);
        tvPesoDetalle       = findViewById(R.id.tv_peso_detalle);
        tvClienteNombre     = findViewById(R.id.tv_cliente_nombre);
        tvTransportista     = findViewById(R.id.tv_transportista);
        tvVehiculoDetalle   = findViewById(R.id.tv_vehiculo_detalle);
        progressDetalle     = findViewById(R.id.progress_detalle);
    }

    // Carga los datos del viaje desde la API
    private void cargarDetalle() {
        progressDetalle.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi()
                .getViaje(viajeId)
                .enqueue(new Callback<ViajeDTO>() {
                    @Override
                    public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                        progressDetalle.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            viajeActual = response.body();
                            pintarViaje(viajeActual);
                        } else if (response.code() == 404) {
                            Toast.makeText(DetalleViajeActivity.this,
                                    "Viaje no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViajeDTO> call, Throwable t) {
                        progressDetalle.setVisibility(View.GONE);
                        Toast.makeText(DetalleViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Rellena todas las vistas con los datos del viaje
    private void pintarViaje(ViajeDTO v) {
        tvTituloViaje.setText("Viaje #" + v.getId());
        tvEstadoChip.setText(estadoLegible(v.getEstado()));
        tvRutaRecogida.setText(v.getDireccionRecogida());
        tvRutaEntrega.setText(v.getDireccionEntrega());
        tvTipoMercancia.setText(v.getTipoMercanciaNombre());
        tvDescripcionMercancia.setText(
                v.getDescripcionMercancia() != null ? v.getDescripcionMercancia() : "");
        tvPesoDetalle.setText(v.getPesoKg() != null
                ? String.format("%.0f kg", v.getPesoKg()) : "— kg");
        tvClienteNombre.setText(v.getClienteNombre() != null ? v.getClienteNombre() : "—");
        tvTransportista.setText(v.getTransportistaNombre() != null
                ? v.getTransportistaNombre() : "Sin asignar");
        tvVehiculoDetalle.setText(v.getVehiculoMatricula() != null
                ? v.getVehiculoMatricula() : "Sin asignar");

        // MODIFICAR: colores del badge de estado en colors.xml
        aplicarColorEstado(v.getEstado());

        // Muestra/oculta botones segun rol y estado
        configurarBotones(v);
    }

    // Colorea el chip de estado
    private void aplicarColorEstado(String estado) {
        int text, bg;
        switch (estado) {
            case "PENDIENTE":
                text = ContextCompat.getColor(this, R.color.estado_pendiente_text);
                bg   = ContextCompat.getColor(this, R.color.estado_pendiente_bg);
                break;
            case "COMPLETADO":
                text = ContextCompat.getColor(this, R.color.estado_completado_text);
                bg   = ContextCompat.getColor(this, R.color.estado_completado_bg);
                break;
            case "CANCELADO":
                text = ContextCompat.getColor(this, R.color.estado_cancelado_text);
                bg   = ContextCompat.getColor(this, R.color.estado_cancelado_bg);
                break;
            default:
                text = ContextCompat.getColor(this, R.color.estado_activo_text);
                bg   = ContextCompat.getColor(this, R.color.estado_activo_bg);
                break;
        }
        tvEstadoChip.setTextColor(text);
        GradientDrawable chip = new GradientDrawable();
        chip.setColor(bg);
        chip.setCornerRadius(100f);
        tvEstadoChip.setBackground(chip);
    }

    // Muestra los botones correctos segun rol y estado actual
    private void configurarBotones(ViajeDTO v) {
        String rol    = SessionManager.getRol(this);
        String estado = v.getEstado();
        boolean esTransportista = Constants.ROL_TRANSPORTISTA.equals(rol);

        // Comprueba si este transportista es el que tiene asignado el viaje
        boolean esTransportistaAsignado = false;
        if (esTransportista && v.getTransportistaId() != null) {
            esTransportistaAsignado = v.getTransportistaId() == SessionManager.getUserId(this);
        }

        // Boton VER MAPA: visible si el viaje ya fue aceptado (incluyendo completado)
        boolean puedeMostrarMapa = !"PENDIENTE".equals(estado) && !"CANCELADO".equals(estado);
        btnVerMapa.setVisibility(puedeMostrarMapa ? View.VISIBLE : View.GONE);

        // Boton ACEPTAR: solo transportistas en viajes PENDIENTE
        btnAceptar.setVisibility(
                esTransportista && "PENDIENTE".equals(estado) ? View.VISIBLE : View.GONE);

        // Boton AVANZAR: solo el transportista asignado, en estados en curso
        btnAvanzar.setVisibility(
                esTransportistaAsignado && v.estaEnCurso() ? View.VISIBLE : View.GONE);

        // Texto descriptivo del siguiente estado en el boton
        if (esTransportistaAsignado && v.estaEnCurso()) {
            String siguiente = siguienteEstado(estado);
            if (siguiente != null) {
                btnAvanzar.setText("Marcar: " + estadoLegible(siguiente));
            }
        }
    }

    // Dialogo para que el transportista seleccione su vehiculo y acepte el viaje
    private void mostrarDialogoAceptar() {
        progressDetalle.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi()
                .getMisVehiculos()
                .enqueue(new Callback<List<VehiculoDTO>>() {
                    @Override
                    public void onResponse(Call<List<VehiculoDTO>> call,
                                           Response<List<VehiculoDTO>> response) {
                        progressDetalle.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<VehiculoDTO> vehiculos = response.body();
                            if (vehiculos.isEmpty()) {
                                Toast.makeText(DetalleViajeActivity.this,
                                        "Primero registra un vehiculo", Toast.LENGTH_SHORT).show();
                            } else {
                                mostrarSelectorVehiculo(vehiculos);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VehiculoDTO>> call, Throwable t) {
                        progressDetalle.setVisibility(View.GONE);
                        Toast.makeText(DetalleViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Muestra un AlertDialog con la lista de vehiculos del transportista
    private void mostrarSelectorVehiculo(List<VehiculoDTO> vehiculos) {
        // MODIFICAR: reemplazar por un BottomSheetDialog para un diseño mas Uber-like
        VehiculosAdapter selectorAdapter = new VehiculosAdapter(vehiculo -> {
            // No hace nada aqui, el click se gestiona en el boton de confirmar
        });
        selectorAdapter.setDatos(vehiculos);
        selectorAdapter.setModoSelector(true);

        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(selectorAdapter);
        rv.setPadding(32, 16, 32, 16);

        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu vehiculo")
                .setView(rv)
                .setPositiveButton("Aceptar viaje", (dialog, which) -> {
                    Long vid = selectorAdapter.getVehiculoSeleccionadoId();
                    if (vid == null) {
                        Toast.makeText(this, "Selecciona un vehiculo", Toast.LENGTH_SHORT).show();
                    } else {
                        aceptarViaje(vid);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void aceptarViaje(Long vehiculoId) {
        progressDetalle.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi()
                .aceptarViaje(viajeId, new AceptarViajeRequest(vehiculoId))
                .enqueue(new Callback<ViajeDTO>() {
                    @Override
                    public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                        progressDetalle.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            viajeActual = response.body();
                            pintarViaje(viajeActual);
                            Toast.makeText(DetalleViajeActivity.this,
                                    "Viaje aceptado", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(DetalleViajeActivity.this,
                                    "El peso supera la capacidad del vehiculo",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(DetalleViajeActivity.this,
                                    R.string.error_servidor, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViajeDTO> call, Throwable t) {
                        progressDetalle.setVisibility(View.GONE);
                        Toast.makeText(DetalleViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Avanza al siguiente estado en la secuencia del viaje
    private void avanzarEstado() {
        if (viajeActual == null) return;
        String siguiente = siguienteEstado(viajeActual.getEstado());
        if (siguiente == null) return;

        progressDetalle.setVisibility(View.VISIBLE);
        btnAvanzar.setEnabled(false);

        RetrofitClient.getInstance(this).getApi()
                .actualizarEstado(viajeId, new ActualizarEstadoRequest(siguiente))
                .enqueue(new Callback<ViajeDTO>() {
                    @Override
                    public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                        progressDetalle.setVisibility(View.GONE);
                        btnAvanzar.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            viajeActual = response.body();
                            pintarViaje(viajeActual);
                        } else {
                            Toast.makeText(DetalleViajeActivity.this,
                                    R.string.error_servidor, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ViajeDTO> call, Throwable t) {
                        progressDetalle.setVisibility(View.GONE);
                        btnAvanzar.setEnabled(true);
                        Toast.makeText(DetalleViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirMapa() {
        Intent i = new Intent(this, MapaViajeActivity.class);
        i.putExtra(Constants.EXTRA_VIAJE_ID, viajeId);
        startActivity(i);
    }

    // Devuelve el siguiente estado en la secuencia o null si ya esta en el ultimo
    private String siguienteEstado(String estadoActual) {
        for (int i = 0; i < ESTADOS_SECUENCIA.length - 1; i++) {
            if (ESTADOS_SECUENCIA[i].equals(estadoActual)) {
                return ESTADOS_SECUENCIA[i + 1];
            }
        }
        return null;
    }

    // MODIFICAR: cambiar los textos de estado si se quiere otro idioma o formato
    private String estadoLegible(String estado) {
        if (estado == null) return "—";
        switch (estado) {
            case "PENDIENTE":          return "Pendiente";
            case "ACEPTADO":           return "Aceptado";
            case "SALIDA_RECOGIDA":    return "Salida a recogida";
            case "LLEGADA_RECOGIDA":   return "Llegada a recogida";
            case "MERCANCIA_RECOGIDA": return "Mercancia cargada";
            case "SALIDA_ENTREGA":     return "En ruta a destino";
            case "LLEGADA_ENTREGA":    return "En punto de entrega";
            case "COMPLETADO":         return "Completado";
            case "CANCELADO":          return "Cancelado";
            default:                   return estado;
        }
    }
}
