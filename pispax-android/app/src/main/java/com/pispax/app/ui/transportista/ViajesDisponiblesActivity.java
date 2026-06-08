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

// Lista de viajes en estado PENDIENTE disponibles para que el TRANSPORTISTA los acepte.
// Tap en un item → DetalleViajeActivity donde puede aceptar el viaje y asignar vehiculo.
public class ViajesDisponiblesActivity extends AppCompatActivity {

    private MaterialButton btnVolver, btnRefresh;
    private ProgressBar progress;
    private RecyclerView rvDisponibles;
    private TextView tvSinDisponibles;
    private ViajesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viajes_disponibles);

        // Enlace de vistas con sus IDs del XML
        btnVolver        = findViewById(R.id.btn_volver);
        btnRefresh       = findViewById(R.id.btn_refresh);
        progress         = findViewById(R.id.progress_disponibles);
        rvDisponibles    = findViewById(R.id.rv_disponibles);
        tvSinDisponibles = findViewById(R.id.tv_sin_disponibles);

        // Boton atras: cierra y vuelve al HomeTransportista
        btnVolver.setOnClickListener(v -> finish());

        // Boton de refresco manual: util para comprobar si hay nuevos viajes
        btnRefresh.setOnClickListener(v -> cargarDisponibles());

        // Adapter generico de viajes: tap → abre detalle pasando el ID del viaje
        adapter = new ViajesAdapter(viaje -> {
            Intent i = new Intent(this, DetalleViajeActivity.class);
            i.putExtra(Constants.EXTRA_VIAJE_ID, viaje.getId());
            startActivity(i);
        });
        rvDisponibles.setLayoutManager(new LinearLayoutManager(this));
        rvDisponibles.setAdapter(adapter);
    }

    // onResume recarga la lista cada vez que se vuelve a esta pantalla
    // (por ejemplo, al volver desde DetalleViajeActivity tras aceptar un viaje).
    @Override
    protected void onResume() {
        super.onResume();
        cargarDisponibles();
    }

    // Llama a GET /viajes/disponibles y muestra los viajes PENDIENTES.
    // Si no hay ninguno, muestra el texto "Sin viajes disponibles".
    private void cargarDisponibles() {
        progress.setVisibility(View.VISIBLE);
        tvSinDisponibles.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi()
                .getViajesDisponibles()
                .enqueue(new Callback<List<ViajeDTO>>() {
                    @Override
                    public void onResponse(Call<List<ViajeDTO>> call, Response<List<ViajeDTO>> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setDatos(response.body());
                            // Muestra aviso si no hay viajes disponibles en este momento
                            tvSinDisponibles.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(ViajesDisponiblesActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
