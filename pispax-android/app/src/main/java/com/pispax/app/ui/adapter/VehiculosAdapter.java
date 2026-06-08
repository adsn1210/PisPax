package com.pispax.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pispax.app.R;
import com.pispax.app.model.VehiculoDTO;

import java.util.ArrayList;
import java.util.List;

// Adaptador para listas de VehiculoDTO.
// Usado en MisVehiculosActivity y en el selector de vehiculo al aceptar un viaje.
public class VehiculosAdapter extends RecyclerView.Adapter<VehiculosAdapter.VehiculoViewHolder> {

    public interface OnVehiculoClickListener {
        void onVehiculoClick(VehiculoDTO vehiculo);
    }

    private final OnVehiculoClickListener listener;
    private List<VehiculoDTO> datos = new ArrayList<>();
    // En modo selector se muestra el checkbox y se resalta el seleccionado
    private boolean modoSelector = false;
    private Long vehiculoSeleccionadoId = null;

    public VehiculosAdapter(OnVehiculoClickListener listener) {
        this.listener = listener;
    }

    public void setDatos(List<VehiculoDTO> nuevos) {
        datos = nuevos != null ? nuevos : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Activa el modo selector (usado en el dialogo de aceptar viaje)
    public void setModoSelector(boolean selector) {
        modoSelector = selector;
        notifyDataSetChanged();
    }

    public Long getVehiculoSeleccionadoId() {
        return vehiculoSeleccionadoId;
    }

    @NonNull
    @Override
    public VehiculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehiculo, parent, false);
        return new VehiculoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VehiculoViewHolder holder, int position) {
        VehiculoDTO vehiculo = datos.get(position);
        holder.bind(vehiculo, modoSelector, vehiculoSeleccionadoId, v -> {
            vehiculoSeleccionadoId = vehiculo.getId();
            listener.onVehiculoClick(vehiculo);
            if (modoSelector) notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class VehiculoViewHolder extends RecyclerView.ViewHolder {

        // ── VISTAS del item_vehiculo.xml (modificar apariencia ahi) ──────────
        private final TextView tvNombre, tvTipo, tvCapacidad, tvCarnet;
        private final CheckBox cbSeleccionado;

        VehiculoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre       = itemView.findViewById(R.id.tv_vehiculo_nombre);
            tvTipo         = itemView.findViewById(R.id.tv_tipo_vehiculo);
            tvCapacidad    = itemView.findViewById(R.id.tv_capacidad);
            tvCarnet       = itemView.findViewById(R.id.tv_carnet);
            cbSeleccionado = itemView.findViewById(R.id.cb_seleccionado);
        }

        void bind(VehiculoDTO v, boolean modoSelector, Long selId, View.OnClickListener click) {
            tvNombre.setText(v.getMarca() + " " + v.getModelo() + " · " + v.getMatricula());
            tvTipo.setText(tipoLegible(v.getTipoVehiculo()));
            tvCapacidad.setText(v.getCapacidadKg() != null
                    ? String.format("%.0f kg", v.getCapacidadKg()) : "—");
            tvCarnet.setText("Carnet " + carnetLegible(v.getCarnetRequerido()));

            // Checkbox visible solo en modo selector
            cbSeleccionado.setVisibility(modoSelector ? View.VISIBLE : View.GONE);
            if (modoSelector) {
                cbSeleccionado.setChecked(v.getId().equals(selId));
            }

            itemView.setOnClickListener(click);
        }

        private String tipoLegible(String tipo) {
            if (tipo == null) return "—";
            switch (tipo) {
                case "FURGONETA":          return "Furgoneta";
                case "FURGON_GRANDE":      return "Furgon Grande";
                case "FRIGORIFICO":        return "Frigorifico";
                case "CAMION_LIGERO":      return "Camion Ligero";
                case "CAMION_PESADO":      return "Camion Pesado";
                case "CAMION_ARTICULADO":  return "Camion Articulado";
                case "PLATAFORMA":         return "Plataforma";
                default:                   return tipo;
            }
        }

        private String carnetLegible(String carnet) {
            if (carnet == null) return "—";
            if ("C_E".equals(carnet)) return "C+E";
            return carnet;
        }
    }
}
