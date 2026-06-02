package com.pispax.app.ui.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

// Adaptador para el Spinner de tipos de mercancía en CrearViajeActivity.
// Carga la lista de TipoMercanciaDTO desde GET /api/tipo-mercancia y muestra el campo "nombre".
// TODO: implementar extendiendo ArrayAdapter<TipoMercanciaDTO>
public class TipoMercanciaAdapter extends ArrayAdapter<Object> {

    public TipoMercanciaAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        // TODO: implementar con lista de TipoMercanciaDTO
    }
}
