package com.pispax.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

// Adaptador para listas de VehiculoDTO en RecyclerView.
// Usado en: MisVehiculosActivity
// TODO: implementar con ViewHolder y item_vehiculo.xml
public class VehiculosAdapter extends RecyclerView.Adapter<VehiculosAdapter.VehiculoViewHolder> {

    @NonNull
    @Override
    public VehiculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        throw new UnsupportedOperationException("TODO: implementar");
    }

    @Override
    public void onBindViewHolder(@NonNull VehiculoViewHolder holder, int position) {
        // TODO: implementar
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class VehiculoViewHolder extends RecyclerView.ViewHolder {
        VehiculoViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            // TODO: bindear vistas de item_vehiculo.xml
        }
    }
}
