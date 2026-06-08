package com.pispax.app.ui.transportista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pispax.app.R;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.ViajesAdapter;
import com.pispax.app.ui.auth.LoginActivity;
import com.pispax.app.ui.shared.DetalleViajeActivity;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Dashboard del transportista. Acceso a viajes disponibles, asignados y vehiculos.
public class HomeTransportistaActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_home_transportista.xml) ─────────
    private TextView tvSaludo, tvSinViajes, btnVerTodos;
    private MaterialButton btnDisponibles, btnMisViajes, btnVehiculos;
    private MaterialButton btnToggleTema, btnCerrarSesion;
    private RecyclerView rvViajesEnCurso;
    private ProgressBar progressViajes;
    private ViajesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transportista);

        bindVistas();
        configurarTextos();
        configurarToggleTema();

        // ── RecyclerView viajes en curso ──────────────────────────────────────
        adapter = new ViajesAdapter(viaje -> abrirDetalle(viaje.getId()));
        rvViajesEnCurso.setLayoutManager(new LinearLayoutManager(this));
        rvViajesEnCurso.setAdapter(adapter);

        // ── ACCIONES ──────────────────────────────────────────────────────────
        // BOTON PRINCIPAL: Ver viajes disponibles
        btnDisponibles.setOnClickListener(v ->
                startActivity(new Intent(this, ViajesDisponiblesActivity.class)));

        // BOTONES SECUNDARIOS
        btnMisViajes.setOnClickListener(v ->
                startActivity(new Intent(this, MisViajesTransportistaActivity.class)));

        btnVehiculos.setOnClickListener(v ->
                startActivity(new Intent(this, MisVehiculosActivity.class)));

        btnVerTodos.setOnClickListener(v ->
                startActivity(new Intent(this, MisViajesTransportistaActivity.class)));

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarViajesEnCurso();
    }

    private void bindVistas() {
        tvSaludo        = findViewById(R.id.tv_saludo);
        tvSinViajes     = findViewById(R.id.tv_sin_viajes);
        btnVerTodos     = findViewById(R.id.btn_ver_todos);
        btnDisponibles  = findViewById(R.id.btn_disponibles);
        btnMisViajes    = findViewById(R.id.btn_mis_viajes);
        btnVehiculos    = findViewById(R.id.btn_vehiculos);
        btnToggleTema   = findViewById(R.id.btn_toggle_tema);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        rvViajesEnCurso = findViewById(R.id.rv_viajes_en_curso);
        progressViajes  = findViewById(R.id.progress_viajes);
    }

    private void configurarTextos() {
        String nombre = SessionManager.getNombre(this);
        if (nombre != null && nombre.contains(" ")) {
            nombre = nombre.split(" ")[0];
        }
        tvSaludo.setText(getString(R.string.home_trans_saludo, nombre));
        actualizarIconoTema();
    }

    // Carga los viajes aceptados por el transportista que estan en curso
    private void cargarViajesEnCurso() {
        progressViajes.setVisibility(View.VISIBLE);
        tvSinViajes.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi()
                .getMisViajes()
                .enqueue(new Callback<List<ViajeDTO>>() {
                    @Override
                    public void onResponse(Call<List<ViajeDTO>> call, Response<List<ViajeDTO>> response) {
                        progressViajes.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            // Filtra solo los viajes activos para el panel principal
                            List<ViajeDTO> todos = response.body();
                            List<ViajeDTO> enCurso = new ArrayList<>();
                            for (ViajeDTO viaje : todos) {
                                if (!viaje.estaFinalizado()) {
                                    enCurso.add(viaje);
                                }
                            }
                            adapter.setDatos(enCurso);
                            tvSinViajes.setVisibility(enCurso.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
                        progressViajes.setVisibility(View.GONE);
                        Toast.makeText(HomeTransportistaActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirDetalle(Long viajeId) {
        Intent i = new Intent(this, DetalleViajeActivity.class);
        i.putExtra(Constants.EXTRA_VIAJE_ID, viajeId);
        startActivity(i);
    }

    private void cerrarSesion() {
        SessionManager.cerrarSesion(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void configurarToggleTema() {
        btnToggleTema.setOnClickListener(v -> {
            boolean dark = !SessionManager.isDarkMode(this);
            SessionManager.setDarkMode(this, dark);
            AppCompatDelegate.setDefaultNightMode(
                    dark ? AppCompatDelegate.MODE_NIGHT_YES
                         : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });
    }

    private void actualizarIconoTema() {
        boolean dark = SessionManager.isDarkMode(this);
        btnToggleTema.setIconResource(dark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }
}
