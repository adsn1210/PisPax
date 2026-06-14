package com.pispax.app.ui.cliente;

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

import android.widget.ImageButton;
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

// Dashboard del cliente. Muestra boton de nuevo viaje y lista de viajes activos.
public class HomeClienteActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_home_cliente.xml) ───────────────
    private TextView tvSaludo, tvSinViajes, btnVerTodos;
    private MaterialButton btnNuevoViaje, btnCerrarSesion;
    private ImageButton btnToggleTema;
    private RecyclerView rvViajesActivos;
    private ProgressBar progressViajes;
    private ViajesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_cliente);

        bindVistas();
        configurarTextos();
        configurarToggleTema();

        // ── RecyclerView de viajes activos ────────────────────────────────────
        adapter = new ViajesAdapter(viaje -> abrirDetalle(viaje.getId()));
        rvViajesActivos.setLayoutManager(new LinearLayoutManager(this));
        rvViajesActivos.setAdapter(adapter);

        // ── ACCIONES ──────────────────────────────────────────────────────────
        // BOTON: Nuevo Viaje
        btnNuevoViaje.setOnClickListener(v ->
                startActivity(new Intent(this, CrearViajeActivity.class)));

        // ENLACE: Ver todos mis viajes
        btnVerTodos.setOnClickListener(v ->
                startActivity(new Intent(this, MisViajesClienteActivity.class)));

        // CERRAR SESION
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga la lista cada vez que se vuelve al home (ej: tras crear un viaje)
        cargarViajesActivos();
    }

    private void bindVistas() {
        tvSaludo        = findViewById(R.id.tv_saludo);
        tvSinViajes     = findViewById(R.id.tv_sin_viajes);
        btnVerTodos     = findViewById(R.id.btn_ver_todos);
        btnNuevoViaje   = findViewById(R.id.btn_nuevo_viaje);
        btnToggleTema   = findViewById(R.id.btn_toggle_tema);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        rvViajesActivos = findViewById(R.id.rv_viajes_activos);
        progressViajes  = findViewById(R.id.progress_viajes);
    }

    private void configurarTextos() {
        String nombre = SessionManager.getNombre(this);
        // Muestra solo el primer nombre para el saludo (estilo Uber)
        if (nombre != null && nombre.contains(" ")) {
            nombre = nombre.split(" ")[0];
        }
        tvSaludo.setText(getString(R.string.home_cliente_saludo, nombre));
        actualizarIconoTema();
    }

    // ── Carga los viajes activos del cliente desde la API ──────────────────
    private void cargarViajesActivos() {
        progressViajes.setVisibility(View.VISIBLE);
        tvSinViajes.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi()
                .getMisViajes()
                .enqueue(new Callback<List<ViajeDTO>>() {
                    @Override
                    public void onResponse(Call<List<ViajeDTO>> call, Response<List<ViajeDTO>> response) {
                        progressViajes.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            // Filtra solo los viajes que no estan completados ni cancelados
                            List<ViajeDTO> todos = response.body();
                            List<ViajeDTO> activos = new ArrayList<>();
                            for (ViajeDTO viaje : todos) {
                                if (!viaje.estaFinalizado()) {
                                    activos.add(viaje);
                                }
                            }
                            adapter.setDatos(activos);
                            tvSinViajes.setVisibility(activos.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
                        progressViajes.setVisibility(View.GONE);
                        Toast.makeText(HomeClienteActivity.this,
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

    // Toggle claro/oscuro
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
        btnToggleTema.setImageResource(dark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }
}
