package com.pispax.app.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pispax.app.R;
import com.pispax.app.model.ViajeDTO;

import java.util.ArrayList;
import java.util.List;

// Adaptador generico para listas de ViajeDTO.
// Usado en HomeCliente, MisViajes (cliente y transportista), ViajesDisponibles.
public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajeViewHolder> {

    public interface OnViajeClickListener {
        void onViajeClick(ViajeDTO viaje);
    }

    private final OnViajeClickListener listener;
    private List<ViajeDTO> datos = new ArrayList<>();

    public ViajesAdapter(OnViajeClickListener listener) {
        this.listener = listener;
    }

    public void setDatos(List<ViajeDTO> nuevos) {
        datos = nuevos != null ? nuevos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_viaje, parent, false);
        return new ViajeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViajeViewHolder holder, int position) {
        ViajeDTO viaje = datos.get(position);
        holder.bind(viaje, listener);
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class ViajeViewHolder extends RecyclerView.ViewHolder {

        // ── VISTAS del item_viaje.xml (modificar apariencia ahi) ─────────────
        private final TextView tvMercancia, tvEstadoBadge, tvRuta, tvPeso, tvFecha;
        private final View viewAccentLine;

        ViajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMercancia    = itemView.findViewById(R.id.tv_mercancia);
            tvEstadoBadge  = itemView.findViewById(R.id.tv_estado_badge);
            tvRuta         = itemView.findViewById(R.id.tv_ruta);
            tvPeso         = itemView.findViewById(R.id.tv_peso);
            tvFecha        = itemView.findViewById(R.id.tv_fecha);
            viewAccentLine = itemView.findViewById(R.id.view_accent_line);
        }

        void bind(ViajeDTO viaje, OnViajeClickListener listener) {
            Context ctx = itemView.getContext();

            // MERCANCIA
            tvMercancia.setText(viaje.getTipoMercanciaNombre());

            // RUTA resumida
            tvRuta.setText(resumir(viaje.getDireccionRecogida())
                    + " → " + resumir(viaje.getDireccionEntrega()));

            // PESO
            tvPeso.setText(viaje.getPesoKg() != null
                    ? String.format("%.0f kg", viaje.getPesoKg()) : "—");

            // FECHA
            tvFecha.setText(formatearFecha(viaje.getFechaSolicitud()));

            // ESTADO — colores y texto segun el enum del backend
            aplicarEstado(ctx, viaje.getEstado());

            // CLICK en la card
            itemView.setOnClickListener(v -> listener.onViajeClick(viaje));
        }

        // Colorea el badge y la linea lateral segun el estado del viaje
        // MODIFICAR: cambiar los colores de los estados aqui o en colors.xml
        private void aplicarEstado(Context ctx, String estado) {
            if (estado == null) return;
            int textColor, bgColor, lineColor;

            switch (estado) {
                case "PENDIENTE":
                    textColor = ContextCompat.getColor(ctx,R.color.estado_pendiente_text);
                    bgColor   = ContextCompat.getColor(ctx,R.color.estado_pendiente_bg);
                    lineColor = ContextCompat.getColor(ctx,R.color.estado_pendiente_text);
                    tvEstadoBadge.setText("Pendiente");
                    break;
                case "COMPLETADO":
                    textColor = ContextCompat.getColor(ctx,R.color.estado_completado_text);
                    bgColor   = ContextCompat.getColor(ctx,R.color.estado_completado_bg);
                    lineColor = ContextCompat.getColor(ctx,R.color.estado_completado_text);
                    tvEstadoBadge.setText("Completado");
                    break;
                case "CANCELADO":
                    textColor = ContextCompat.getColor(ctx,R.color.estado_cancelado_text);
                    bgColor   = ContextCompat.getColor(ctx,R.color.estado_cancelado_bg);
                    lineColor = ContextCompat.getColor(ctx,R.color.estado_cancelado_text);
                    tvEstadoBadge.setText("Cancelado");
                    break;
                default:
                    // Todos los estados intermedios (en curso)
                    textColor = ContextCompat.getColor(ctx,R.color.estado_activo_text);
                    bgColor   = ContextCompat.getColor(ctx,R.color.estado_activo_bg);
                    lineColor = ContextCompat.getColor(ctx,R.color.estado_activo_text);
                    tvEstadoBadge.setText(estadoLegible(estado));
                    break;
            }

            tvEstadoBadge.setTextColor(textColor);
            GradientDrawable badge = new GradientDrawable();
            badge.setColor(bgColor);
            badge.setCornerRadius(100f);
            tvEstadoBadge.setBackground(badge);
            viewAccentLine.setBackgroundColor(lineColor);
        }

        // Convierte la direccion larga en una version corta (primera parte antes de la coma)
        private String resumir(String dir) {
            if (dir == null) return "—";
            int coma = dir.indexOf(',');
            return coma > 0 ? dir.substring(0, coma).trim() : dir;
        }

        // Formatea la fecha ISO a DD/MM/YYYY
        private String formatearFecha(String fecha) {
            if (fecha == null || fecha.length() < 10) return "—";
            // formato original: "2026-06-03T10:00:00"
            String[] partes = fecha.substring(0, 10).split("-");
            if (partes.length == 3) return partes[2] + "/" + partes[1] + "/" + partes[0];
            return fecha.substring(0, 10);
        }

        // Convierte el enum de estado en texto legible
        // MODIFICAR: cambiar los textos de estado si se quiere otro idioma o formato
        private String estadoLegible(String estado) {
            switch (estado) {
                case "ACEPTADO":           return "Aceptado";
                case "SALIDA_RECOGIDA":    return "En camino a recogida";
                case "LLEGADA_RECOGIDA":   return "En punto de recogida";
                case "MERCANCIA_RECOGIDA": return "Mercancia cargada";
                case "SALIDA_ENTREGA":     return "En ruta a destino";
                case "LLEGADA_ENTREGA":    return "En punto de entrega";
                default:                   return estado;
            }
        }
    }
}
