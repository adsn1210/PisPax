package com.pispax.app.model;

public class TipoMercanciaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    // nombreDb no se expone en la API publica, no se mapea aqui

    public Long getId()          { return id; }
    public String getNombre()    { return nombre; }
    public String getDescripcion(){ return descripcion; }

    @Override
    public String toString() {
        return nombre; // Para que el Spinner/Adapter lo muestre directamente
    }
}
