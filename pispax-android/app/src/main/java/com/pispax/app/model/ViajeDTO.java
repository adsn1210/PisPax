package com.pispax.app.model;

// Datos completos de un viaje que devuelve la API.
// Es el objeto mas importante de la app: se usa en listas, detalle, mapa y simulacion.
public class ViajeDTO {

    // Identificador unico del viaje en base de datos
    private Long id;

    // ID y nombre del cliente que solicito el viaje
    private Long clienteId;
    private String clienteNombre;

    // ID y nombre del transportista que lo acepto (null si aun es PENDIENTE)
    private Long transportistaId;
    private String transportistaNombre;

    // ID y matricula del vehiculo asignado (null si aun es PENDIENTE)
    private Long vehiculoId;
    private String vehiculoMatricula;

    // Tipo y descripcion de la mercancia a transportar
    private Long tipoMercanciaId;
    private String tipoMercanciaNombre;
    private String descripcionMercancia;

    // Direcciones de recogida y entrega (texto libre)
    private String direccionRecogida;
    private String direccionEntrega;

    // Peso de la carga en kg
    private Double pesoKg;

    // Estado actual del viaje. Coincide con EstadoViaje enum del backend.
    // Valores posibles: PENDIENTE, ACEPTADO, SALIDA_RECOGIDA, LLEGADA_RECOGIDA,
    // MERCANCIA_RECOGIDA, SALIDA_ENTREGA, LLEGADA_ENTREGA, COMPLETADO, CANCELADO
    private String estado;

    // Fechas del ciclo de vida del viaje (pueden ser null si no ha llegado a ese punto)
    private String fechaSolicitud;
    private String fechaInicio;
    private String fechaFin;

    public Long getId()                    { return id; }
    public Long getClienteId()             { return clienteId; }
    public String getClienteNombre()       { return clienteNombre; }
    public Long getTransportistaId()       { return transportistaId; }
    public String getTransportistaNombre() { return transportistaNombre; }
    public Long getVehiculoId()            { return vehiculoId; }
    public String getVehiculoMatricula()   { return vehiculoMatricula; }
    public Long getTipoMercanciaId()       { return tipoMercanciaId; }
    public String getTipoMercanciaNombre() { return tipoMercanciaNombre; }
    public String getDescripcionMercancia(){ return descripcionMercancia; }
    public String getDireccionRecogida()   { return direccionRecogida; }
    public String getDireccionEntrega()    { return direccionEntrega; }
    public Double getPesoKg()              { return pesoKg; }
    public String getEstado()              { return estado; }
    public String getFechaSolicitud()      { return fechaSolicitud; }
    public String getFechaInicio()         { return fechaInicio; }
    public String getFechaFin()            { return fechaFin; }

    // Devuelve true si el viaje esta activo (ni pendiente ni terminado).
    // Se usa para decidir si mostrar el boton "Ver Mapa" en DetalleViajeActivity.
    public boolean estaEnCurso() {
        return estado != null && !estado.equals("PENDIENTE")
                && !estado.equals("COMPLETADO")
                && !estado.equals("CANCELADO");
    }

    // Devuelve true si el viaje ha terminado (completado o cancelado).
    // Se usa para desactivar acciones en la pantalla de detalle.
    public boolean estaFinalizado() {
        return "COMPLETADO".equals(estado) || "CANCELADO".equals(estado);
    }
}
