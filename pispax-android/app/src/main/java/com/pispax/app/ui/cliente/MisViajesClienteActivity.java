package com.pispax.app.ui.cliente;

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

// Lista completa de viajes del CLIENTE (todos los estados: activos, completados, cancelados).
// Tap en un item abre DetalleViajeActivity para ver y gestionar ese viaje.
public class MisViajesClienteActivity extends AppCompatActivity {

    private MaterialButton btnVolver;
    private ProgressBar progress;
    private RecyclerView rvMisViajes;
    private TextView tvSinViajes;
    private ViajesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes_cliente);

        // Enlace de vistas con sus IDs del XML
        btnVolver   = findViewById(R.id.btn_volver);
        progress    = findViewById(R.id.progress_mis_viajes);
        rvMisViajes = findViewById(R.id.rv_mis_viajes);
        tvSinViajes = findViewById(R.id.tv_sin_viajes_lista);

        // Boton atras: cierra esta Activity y vuelve al HomeCliente
        btnVolver.setOnClickListener(v -> finish());

        // Adapter generico de viajes: tap → abre detalle pasando el ID del viaje
        adapter = new ViajesAdapter(viaje -> {
            Intent i = new Intent(this, DetalleViajeActivity.class);
            i.putExtra(Constants.EXTRA_VIAJE_ID, viaje.getId());
            startActivity(i);
        });
        rvMisViajes.setLayoutManager(new LinearLayoutManager(this));
        rvMisViajes.setAdapter(adapter);
    }

    // onResume se llama cada vez que esta pantalla vuelve a ser visible:
    // al entrar la primera vez y al volver desde DetalleViajeActivity.
    // Asi la lista siempre refleja el estado mas reciente sin necesidad de boton refrescar.
    @Override
    protected void onResume() {
        super.onResume();
        cargarViajes();
    }

    // Llama a GET /viajes/mis-viajes y actualiza la lista.
    // Muestra el mensaje vacio si el cliente aun no tiene ningun viaje.
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
                            // Si la lista esta vacia muestra el texto "Sin viajes"
                            tvSinViajes.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(MisViajesClienteActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
