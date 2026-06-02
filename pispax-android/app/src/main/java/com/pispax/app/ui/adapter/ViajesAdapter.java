package com.pispax.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

// Adaptador para listas de ViajeDTO en RecyclerView.
// Usado en: MisViajesClienteActivity, ViajesDisponiblesActivity, MisViajesTransportistaActivity
// TODO: implementar con ViewHolder y item_viaje.xml
public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajeViewHolder> {

    @NonNull
    @Override
    public ViajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        throw new UnsupportedOperationException("TODO: implementar");
    }

    @Override
    public void onBindViewHolder(@NonNull ViajeViewHolder holder, int position) {
        // TODO: implementar
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViajeViewHolder extends RecyclerView.ViewHolder {
        ViajeViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            // TODO: bindear vistas de item_viaje.xml
        }
    }
}
