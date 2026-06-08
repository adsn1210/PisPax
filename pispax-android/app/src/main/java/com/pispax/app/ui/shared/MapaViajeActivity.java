package com.pispax.app.ui.shared;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pispax.app.R;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.TimelineAdapter;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    MAPA VIAJE — Seguimiento visual y simulacion del viaje.

    Coordenadas hardcodeadas para la demo del TFG:
      ORIGEN:  Poligono Can Fontanet, Terrassa, Barcelona
      DESTINO: Centro Logistico Mercamadrid, Madrid

    El marcador del camion se interpola entre origen y destino segun el estado actual.
    El polling cada 3s actualiza el estado y mueve el marcador.

    MODIFICAR COORDENADAS: cambiar ORIGEN y DESTINO si se quiere otra ruta de demo.
    MODIFICAR POLLING: cambiar POLLING_INTERVAL_MS para mas o menos frecuencia.
    MODIFICAR ANIMACION: cambiar la duracion en animarMarcador() o el interpolador.
*/
public class MapaViajeActivity extends AppCompatActivity {

    // ── COORDENADAS DE LA DEMO ────────────────────────────────────────────────
    // MODIFICAR: cambiar por las coordenadas reales de la ruta demo del TFG
    private static final GeoPoint COORD_ORIGEN  = new GeoPoint(41.5638, 2.0085);  // Terrassa
    private static final GeoPoint COORD_DESTINO = new GeoPoint(40.3838, -3.6765); // Mercamadrid

    // Porcentaje de la ruta donde se situa el marcador segun el estado
    // MODIFICAR: ajustar los porcentajes si se quiere otra distribucion visual
    private static final float[] PROGRESO_POR_ESTADO = {
            0.0f,  // ACEPTADO (parado en origen)
            0.25f, // SALIDA_RECOGIDA
            0.4f,  // LLEGADA_RECOGIDA (en punto de recogida)
            0.4f,  // MERCANCIA_RECOGIDA (sigue en recogida)
            0.75f, // SALIDA_ENTREGA
            1.0f,  // LLEGADA_ENTREGA (en destino)
            1.0f   // COMPLETADO
    };

    private static final int POLLING_INTERVAL_MS = 3000; // Intervalo de polling en ms

    // ── VISTAS ────────────────────────────────────────────────────────────────
    private MapView mapView;
    private FloatingActionButton btnVolverMapa;
    private TextView tvEstadoMapa, tvRutaMapa, tvPesoMapa;
    private MaterialButton btnSimular;
    private LinearLayout layoutSimulando;
    private ProgressBar progressSimular;
    private RecyclerView rvTimeline;

    // ── ESTADO ────────────────────────────────────────────────────────────────
    private long viajeId;
    private ViajeDTO viajeActual;
    private Marker marcadorCamion;
    private float progresoActual = 0f;
    private boolean pollingActivo = false;
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private TimelineAdapter timelineAdapter;

    private final String[] ESTADOS_ORDEN = {
            "ACEPTADO", "SALIDA_RECOGIDA", "LLEGADA_RECOGIDA",
            "MERCANCIA_RECOGIDA", "SALIDA_ENTREGA", "LLEGADA_ENTREGA", "COMPLETADO"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_viaje);

        viajeId = getIntent().getLongExtra(Constants.EXTRA_VIAJE_ID, -1);
        if (viajeId == -1) { finish(); return; }

        bindVistas();
        configurarMapa();
        configurarTimeline();

        btnVolverMapa.setOnClickListener(v -> finish());

        // ── BOTON INICIAR SIMULACION (solo visible para transportistas) ──────
        if (!Constants.ROL_TRANSPORTISTA.equals(SessionManager.getRol(this))) {
            btnSimular.setVisibility(View.GONE);
        }
        btnSimular.setOnClickListener(v -> iniciarSimulacion());

        cargarViaje();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (!pollingActivo && viajeActual != null && !viajeActual.estaFinalizado()) {
            iniciarPolling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        detenerPolling();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerPolling();
    }

    private void bindVistas() {
        mapView        = findViewById(R.id.map_view);
        btnVolverMapa  = findViewById(R.id.btn_volver_mapa);
        tvEstadoMapa   = findViewById(R.id.tv_estado_mapa);
        tvRutaMapa     = findViewById(R.id.tv_ruta_mapa);
        tvPesoMapa     = findViewById(R.id.tv_peso_mapa);
        btnSimular     = findViewById(R.id.btn_simular);
        layoutSimulando= findViewById(R.id.layout_simulando);
        progressSimular= findViewById(R.id.progress_simular);
        rvTimeline     = findViewById(R.id.rv_timeline);
    }

    // ── Configuracion del mapa OSMDroid ───────────────────────────────────────
    // MODIFICAR: cambiar el tile source si se quiere otro estilo de mapa
    private void configurarMapa() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController controller = mapView.getController();
        // Zoom inicial centrado entre origen y destino
        // MODIFICAR: zoom level (mayor = mas cerca) o el punto central
        controller.setZoom(6.5);
        controller.setCenter(new GeoPoint(41.0, -0.5)); // Centro de la ruta Terrassa-Madrid

        // Linea de la ruta
        dibujarRuta();

        // Marcador de origen (A)
        Marker markerOrigen = new Marker(mapView);
        markerOrigen.setPosition(COORD_ORIGEN);
        markerOrigen.setTitle("Recogida");
        markerOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(markerOrigen);

        // Marcador de destino (B)
        Marker markerDestino = new Marker(mapView);
        markerDestino.setPosition(COORD_DESTINO);
        markerDestino.setTitle("Entrega");
        markerDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(markerDestino);

        // Marcador del camion (se movera segun el estado)
        // MODIFICAR: cambiar el icono del camion con markerCamion.setIcon(drawable)
        marcadorCamion = new Marker(mapView);
        marcadorCamion.setPosition(COORD_ORIGEN);
        marcadorCamion.setTitle("PisPax");
        marcadorCamion.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        mapView.getOverlays().add(marcadorCamion);
    }

    // Dibuja la linea naranja entre origen y destino
    // MODIFICAR: cambiar el color y grosor de la ruta en el mapa
    private void dibujarRuta() {
        Polyline ruta = new Polyline();
        ruta.addPoint(COORD_ORIGEN);
        ruta.addPoint(COORD_DESTINO);
        ruta.getOutlinePaint().setColor(ContextCompat.getColor(this, R.color.naranja_principal));
        ruta.getOutlinePaint().setStrokeWidth(8f);
        mapView.getOverlays().add(ruta);
    }

    private void configurarTimeline() {
        timelineAdapter = new TimelineAdapter(Arrays.asList(ESTADOS_ORDEN));
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);
    }

    // Carga los datos iniciales del viaje
    private void cargarViaje() {
        RetrofitClient.getInstance(this).getApi()
                .getViaje(viajeId)
                .enqueue(new Callback<ViajeDTO>() {
                    @Override
                    public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            actualizarUI(response.body());
                            if (!response.body().estaFinalizado()) {
                                iniciarPolling();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ViajeDTO> call, Throwable t) {
                        Toast.makeText(MapaViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Actualiza toda la UI con los datos del viaje
    private void actualizarUI(ViajeDTO viaje) {
        viajeActual = viaje;

        // ESTADO: badge superior
        tvEstadoMapa.setText(estadoLegible(viaje.getEstado()));
        aplicarColorEstado(viaje.getEstado());

        // RUTA y PESO
        tvRutaMapa.setText(resumir(viaje.getDireccionRecogida())
                + " → " + resumir(viaje.getDireccionEntrega()));
        tvPesoMapa.setText(viaje.getPesoKg() != null
                ? String.format("%.0f kg", viaje.getPesoKg()) : "—");

        // MARCADOR DEL CAMION: interpolar posicion segun el estado
        float nuevoProgreso = progresoParaEstado(viaje.getEstado());
        if (Math.abs(nuevoProgreso - progresoActual) > 0.01f) {
            animarMarcador(progresoActual, nuevoProgreso);
        }
        progresoActual = nuevoProgreso;

        // TIMELINE: marcar el estado actual como alcanzado
        timelineAdapter.setEstadoActual(viaje.getEstado());

        // BOTON SIMULAR: ocultar si ya esta en curso o finalizado
        if (!"PENDIENTE".equals(viaje.getEstado()) && !"ACEPTADO".equals(viaje.getEstado())) {
            btnSimular.setVisibility(View.GONE);
        }

        // Si esta completado o cancelado, parar el polling
        if (viaje.estaFinalizado()) {
            detenerPolling();
            layoutSimulando.setVisibility(View.GONE);
        }
    }

    // ── Polling cada 3 segundos ───────────────────────────────────────────────
    private void iniciarPolling() {
        pollingActivo = true;
        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS);
    }

    private void detenerPolling() {
        pollingActivo = false;
        pollingHandler.removeCallbacks(pollingRunnable);
    }

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!pollingActivo) return;

            RetrofitClient.getInstance(MapaViajeActivity.this).getApi()
                    .getViaje(viajeId)
                    .enqueue(new Callback<ViajeDTO>() {
                        @Override
                        public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                actualizarUI(response.body());
                                if (!response.body().estaFinalizado() && pollingActivo) {
                                    pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ViajeDTO> call, Throwable t) {
                            if (pollingActivo) {
                                pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS);
                            }
                        }
                    });
        }
    };

    // Llama al endpoint de simulacion del backend (@Async)
    private void iniciarSimulacion() {
        btnSimular.setEnabled(false);
        layoutSimulando.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi()
                .simularViaje(viajeId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            btnSimular.setVisibility(View.GONE);
                            // El polling se encarga de actualizar el estado
                            if (!pollingActivo) iniciarPolling();
                        } else if (response.code() == 409) {
                            Toast.makeText(MapaViajeActivity.this,
                                    "El viaje debe estar en estado PENDIENTE o ACEPTADO",
                                    Toast.LENGTH_LONG).show();
                            btnSimular.setEnabled(true);
                            layoutSimulando.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MapaViajeActivity.this,
                                    R.string.error_servidor, Toast.LENGTH_SHORT).show();
                            btnSimular.setEnabled(true);
                            layoutSimulando.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MapaViajeActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                        btnSimular.setEnabled(true);
                        layoutSimulando.setVisibility(View.GONE);
                    }
                });
    }

    // Anima el marcador del camion desde una posicion a otra en la ruta
    // MODIFICAR: cambiar la duracion (1500ms) para una animacion mas rapida o lenta
    private void animarMarcador(float desde, float hasta) {
        ValueAnimator animator = ValueAnimator.ofFloat(desde, hasta);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float progreso = (float) animation.getAnimatedValue();
            GeoPoint posicion = interpolar(COORD_ORIGEN, COORD_DESTINO, progreso);
            marcadorCamion.setPosition(posicion);
            mapView.invalidate();
        });
        animator.start();
    }

    // Interpola linealmente entre dos GeoPoints segun un valor [0,1]
    private GeoPoint interpolar(GeoPoint a, GeoPoint b, float t) {
        double lat = a.getLatitude()  + (b.getLatitude()  - a.getLatitude())  * t;
        double lon = a.getLongitude() + (b.getLongitude() - a.getLongitude()) * t;
        return new GeoPoint(lat, lon);
    }

    // Devuelve el progreso (0.0-1.0) correspondiente al estado actual
    private float progresoParaEstado(String estado) {
        for (int i = 0; i < ESTADOS_ORDEN.length; i++) {
            if (ESTADOS_ORDEN[i].equals(estado)) {
                return i < PROGRESO_POR_ESTADO.length ? PROGRESO_POR_ESTADO[i] : 0f;
            }
        }
        return 0f;
    }

    // Colorea el badge de estado sobre el mapa
    private void aplicarColorEstado(String estado) {
        int text, bg;
        switch (estado) {
            case "COMPLETADO":
                text = ContextCompat.getColor(this, R.color.estado_completado_text);
                bg   = ContextCompat.getColor(this, R.color.estado_completado_bg);
                break;
            case "CANCELADO":
                text = ContextCompat.getColor(this, R.color.estado_cancelado_text);
                bg   = ContextCompat.getColor(this, R.color.estado_cancelado_bg);
                break;
            case "PENDIENTE":
                text = ContextCompat.getColor(this, R.color.estado_pendiente_text);
                bg   = ContextCompat.getColor(this, R.color.estado_pendiente_bg);
                break;
            default:
                text = ContextCompat.getColor(this, R.color.estado_activo_text);
                bg   = ContextCompat.getColor(this, R.color.estado_activo_bg);
                break;
        }
        tvEstadoMapa.setTextColor(text);
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(bg);
        pill.setCornerRadius(100f);
        tvEstadoMapa.setBackground(pill);
    }

    private String resumir(String dir) {
        if (dir == null) return "—";
        int c = dir.indexOf(',');
        return c > 0 ? dir.substring(0, c).trim() : dir;
    }

    // MODIFICAR: cambiar los textos de estado legibles
    private String estadoLegible(String estado) {
        if (estado == null) return "—";
        switch (estado) {
            case "PENDIENTE":          return "Pendiente";
            case "ACEPTADO":           return "Aceptado";
            case "SALIDA_RECOGIDA":    return "Salida a recogida";
            case "LLEGADA_RECOGIDA":   return "En recogida";
            case "MERCANCIA_RECOGIDA": return "Mercancia cargada";
            case "SALIDA_ENTREGA":     return "En ruta";
            case "LLEGADA_ENTREGA":    return "En entrega";
            case "COMPLETADO":         return "✓ Completado";
            case "CANCELADO":          return "Cancelado";
            default:                   return estado;
        }
    }
}
