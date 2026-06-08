package com.pispax.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pispax.app.R;

import java.util.Arrays;
import java.util.List;

// Adapter para el timeline de estados en MapaViajeActivity.
// Muestra la secuencia de estados con el progreso actual marcado.
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<String> estados;
    private String estadoActual = null;
    private int indiceActual = -1;

    // Orden de los estados para el timeline
    private static final List<String> ORDEN = Arrays.asList(
            "ACEPTADO", "SALIDA_RECOGIDA", "LLEGADA_RECOGIDA",
            "MERCANCIA_RECOGIDA", "SALIDA_ENTREGA", "LLEGADA_ENTREGA", "COMPLETADO"
    );

    public TimelineAdapter(List<String> estados) {
        this.estados = estados;
    }

    public void setEstadoActual(String estado) {
        estadoActual = estado;
        indiceActual = ORDEN.indexOf(estado);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        String estado = estados.get(position);
        int indicePosicion = ORDEN.indexOf(estado);

        // Determina si este estado ya se ha alcanzado, es el actual, o esta pendiente
        boolean alcanzado = indicePosicion >= 0 && indicePosicion <= indiceActual;
        boolean esActual  = estado.equals(estadoActual);
        boolean esUltimo  = position == estados.size() - 1;

        holder.bind(estado, alcanzado, esActual, esUltimo);
    }

    @Override
    public int getItemCount() {
        return estados.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {

        // ── VISTAS del item_timeline.xml (modificar apariencia ahi) ──────────
        private final View viewDot, viewLine;
        private final TextView tvNombre, tvHora;

        TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot  = itemView.findViewById(R.id.view_dot);
            viewLine = itemView.findViewById(R.id.view_line);
            tvNombre = itemView.findViewById(R.id.tv_estado_nombre);
            tvHora   = itemView.findViewById(R.id.tv_estado_hora);
        }

        void bind(String estado, boolean alcanzado, boolean esActual, boolean esUltimo) {
            Context ctx = itemView.getContext();

            // Texto legible del estado
            // MODIFICAR: cambiar los textos del timeline aqui
            tvNombre.setText(estadoLegibleTimeline(estado));

            // Estilo del punto segun el progreso del viaje
            if (esActual) {
                viewDot.setBackgroundResource(R.drawable.dot_estado_activo);
                tvNombre.setTextColor(ContextCompat.getColor(ctx,R.color.naranja_principal));
                tvNombre.setTextSize(14f);
            } else if (alcanzado) {
                viewDot.setBackgroundResource(R.drawable.dot_estado_completado);
                tvNombre.setTextColor(ContextCompat.getColor(ctx,R.color.texto_primario));
                tvNombre.setTextSize(13f);
            } else {
                viewDot.setBackgroundResource(R.drawable.dot_estado_pendiente);
                tvNombre.setTextColor(ContextCompat.getColor(ctx,R.color.texto_secundario));
                tvNombre.setTextSize(13f);
            }

            // Ocultar la linea en el ultimo item del timeline
            viewLine.setVisibility(esUltimo ? View.INVISIBLE : View.VISIBLE);

            // El timestamp se actualiza desde el Activity si se dispone de el
            tvHora.setVisibility(View.GONE);
        }

        // Textos cortos para el timeline (mas compactos que en otras vistas)
        // MODIFICAR: cambiar estos textos si se quiere otro estilo de etiqueta
        private String estadoLegibleTimeline(String estado) {
            switch (estado) {
                case "ACEPTADO":           return "Aceptado";
                case "SALIDA_RECOGIDA":    return "→ Punto de recogida";
                case "LLEGADA_RECOGIDA":   return "En recogida";
                case "MERCANCIA_RECOGIDA": return "Cargado";
                case "SALIDA_ENTREGA":     return "→ Punto de entrega";
                case "LLEGADA_ENTREGA":    return "En entrega";
                case "COMPLETADO":         return "✓ Completado";
                default:                   return estado;
            }
        }
    }
}
