package com.pispax.app.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pispax.app.model.TipoMercanciaDTO;

import java.util.List;

// Adaptador para el Spinner de tipos de mercancia en CrearViajeActivity.
// TipoMercanciaDTO.toString() devuelve el nombre, asi que el display es automatico.
public class TipoMercanciaAdapter extends ArrayAdapter<TipoMercanciaDTO> {

    public TipoMercanciaAdapter(Context context, List<TipoMercanciaDTO> tipos) {
        super(context, android.R.layout.simple_spinner_item, tipos);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        // MODIFICAR: estilo del item seleccionado del spinner (textColor, textSize)
        ((TextView) v).setTextSize(15f);
        return v;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        // MODIFICAR: estilo de los items del desplegable del spinner
        ((TextView) v).setPadding(24, 20, 24, 20);
        return v;
    }
}
