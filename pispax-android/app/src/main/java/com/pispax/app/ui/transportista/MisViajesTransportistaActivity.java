package com.pispax.app.ui.transportista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pispax.app.R;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.ViajesAdapter;
import com.pispax.app.ui.shared.DetalleViajeActivity;
import com.pispax.app.util.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Lista de todos los viajes que el TRANSPORTISTA ha aceptado (activos e historico).
// Tap en un item → DetalleViajeActivity para ver el estado y avanzar la simulacion.
public class MisViajesTransportistaActivity extends AppCompatActivity {

    private MaterialButton btnVolver;
    private ProgressBar progress;
    private RecyclerView rvViajes;
    private TextView tvSinViajes;
    private ViajesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes_transportista);

        // Enlace de vistas con sus IDs del XML
        btnVolver   = findViewById(R.id.btn_volver);
        progress    = findViewById(R.id.progress_viajes_trans);
        rvViajes    = findViewById(R.id.rv_viajes_trans);
        tvSinViajes = findViewById(R.id.tv_sin_viajes_trans);

        // Boton atras: cierra y vuelve al HomeTransportista
        btnVolver.setOnClickListener(v -> finish());

        // Adapter generico de viajes: tap → abre detalle pasando el ID del viaje
        adapter = new ViajesAdapter(viaje -> {
            Intent i = new Intent(this, DetalleViajeActivity.class);
            i.putExtra(Constants.EXTRA_VIAJE_ID, viaje.getId());
            startActivity(i);
        });
        rvViajes.setLayoutManager(new LinearLayoutManager(this));
        rvViajes.setAdapter(adapter);
    }

    // onResume recarga la lista cada vez que se vuelve a esta pantalla
    // (por ejemplo, al volver desde DetalleViajeActivity despues de simular un estado).
    @Override
    protected void onResume() {
        super.onResume();
        cargarViajes();
    }

    // Llama a GET /viajes/mis-viajes y actualiza la lista.
    // El backend filtra automaticamente por el token JWT para devolver
    // solo los viajes del transportista autenticado.
    private void cargarViajes() {
        progress.setVisibility(View.VISIBLE);
        tvSinViajes.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi()
                .getMisViajes()
                .enqueue(new Callback<List<ViajeDTO>>() {
                    @Override
                    public void onResponse(Call<List<ViajeDTO>> call, Response<List<ViajeDTO>> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setDatos(response.body());
                            // Si no hay viajes aceptados todavia, muestra el aviso
                            tvSinViajes.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(MisViajesTransportistaActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
