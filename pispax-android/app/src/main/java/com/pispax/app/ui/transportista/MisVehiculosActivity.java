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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pispax.app.R;
import com.pispax.app.model.VehiculoDTO;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.adapter.VehiculosAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Lista de vehiculos del transportista. FAB (boton flotante +) para añadir uno nuevo.
// Tap en un item muestra un Toast con la descripcion del vehiculo (no hay pantalla de detalle).
public class MisVehiculosActivity extends AppCompatActivity {

    private MaterialButton btnVolver;
    private FloatingActionButton fabAnadir;
    private ProgressBar progress;
    private RecyclerView rvVehiculos;
    private TextView tvSinVehiculos;
    private VehiculosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_vehiculos);

        btnVolver     = findViewById(R.id.btn_volver);
        fabAnadir     = findViewById(R.id.fab_anadir);
        progress      = findViewById(R.id.progress_vehiculos);
        rvVehiculos   = findViewById(R.id.rv_vehiculos);
        tvSinVehiculos= findViewById(R.id.tv_sin_vehiculos);

        btnVolver.setOnClickListener(v -> finish());
        fabAnadir.setOnClickListener(v ->
                startActivity(new Intent(this, CrearVehiculoActivity.class)));

        // El adapter en modo lista (sin checkbox de seleccion)
        adapter = new VehiculosAdapter(vehiculo ->
                Toast.makeText(this, vehiculo.getDescripcionCorta(), Toast.LENGTH_SHORT).show());
        rvVehiculos.setLayoutManager(new LinearLayoutManager(this));
        rvVehiculos.setAdapter(adapter);

    }

    // onResume recarga la lista cada vez que se vuelve a esta pantalla
    // (por ejemplo, al volver desde CrearVehiculoActivity tras guardar un vehiculo).
    @Override
    protected void onResume() {
        super.onResume();
        cargarVehiculos();
    }

    // Llama a GET /vehiculos/mis-vehiculos y actualiza la lista.
    // Si el transportista no tiene vehiculos todavia, muestra el texto de estado vacio.
    private void cargarVehiculos() {
        progress.setVisibility(View.VISIBLE);
        tvSinVehiculos.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi()
                .getMisVehiculos()
                .enqueue(new Callback<List<VehiculoDTO>>() {
                    @Override
                    public void onResponse(Call<List<VehiculoDTO>> call, Response<List<VehiculoDTO>> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setDatos(response.body());
                            tvSinVehiculos.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VehiculoDTO>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(MisVehiculosActivity.this,
                                R.string.error_red, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
