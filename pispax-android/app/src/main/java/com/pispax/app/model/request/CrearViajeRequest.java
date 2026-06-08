package com.pispax.app.model.request;

// Cuerpo de la peticion POST /viajes.
// Contiene todos los datos que el cliente rellena en CrearViajeActivity.
public class CrearViajeRequest {

    // ID del tipo de mercancia seleccionado en el Spinner
    private Long tipoMercanciaId;

    // Descripcion opcional del contenido (ej: "fruta fresca, cajas de 20kg")
    private String descripcionMercancia;

    // Direccion de recogida (texto libre)
    private String direccionRecogida;

    // Direccion de entrega (texto libre)
    private String direccionEntrega;

    // Peso total de la carga en kg
    private Double pesoKg;

    public CrearViajeRequest(Long tipoMercanciaId, String descripcionMercancia,
                             String direccionRecogida, String direccionEntrega,
                             Double pesoKg) {
        this.tipoMercanciaId      = tipoMercanciaId;
        this.descripcionMercancia = descripcionMercancia;
        this.direccionRecogida    = direccionRecogida;
        this.direccionEntrega     = direccionEntrega;
        this.pesoKg               = pesoKg;
    }
}
