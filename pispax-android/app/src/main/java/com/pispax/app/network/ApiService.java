package com.pispax.app.network;

import com.pispax.app.model.LoginResponse;
import com.pispax.app.model.TipoMercanciaDTO;
import com.pispax.app.model.UsuarioDTO;
import com.pispax.app.model.VehiculoDTO;
import com.pispax.app.model.ViajeDTO;
import com.pispax.app.model.request.AceptarViajeRequest;
import com.pispax.app.model.request.ActualizarEstadoRequest;
import com.pispax.app.model.request.CrearVehiculoRequest;
import com.pispax.app.model.request.CrearViajeRequest;
import com.pispax.app.model.request.LoginRequest;
import com.pispax.app.model.request.RegistroRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

// Define todos los endpoints de la API PisPax.
// Retrofit usa esta interfaz para generar las implementaciones HTTP automaticamente.
// El JWT se añade en RetrofitClient (interceptor), no aqui.
public interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/registro")
    Call<UsuarioDTO> registro(@Body RegistroRequest request);

    // ── TIPO MERCANCIA (publico, sin token) ───────────────────────────────────

    @GET("tipo-mercancia")
    Call<List<TipoMercanciaDTO>> getTiposMercancia();

    // ── VIAJES ────────────────────────────────────────────────────────────────

    // CLIENTE: sus viajes creados | TRANSPORTISTA: sus viajes aceptados
    @GET("viajes")
    Call<List<ViajeDTO>> getMisViajes();

    // Solo TRANSPORTISTA: viajes en estado PENDIENTE
    @GET("viajes/disponibles")
    Call<List<ViajeDTO>> getViajesDisponibles();

    @GET("viajes/{id}")
    Call<ViajeDTO> getViaje(@Path("id") long id);

    // Solo CLIENTE
    @POST("viajes")
    Call<ViajeDTO> crearViaje(@Body CrearViajeRequest request);

    // Solo TRANSPORTISTA
    @PUT("viajes/{id}/aceptar")
    Call<ViajeDTO> aceptarViaje(@Path("id") long id, @Body AceptarViajeRequest request);

    // Solo TRANSPORTISTA: avanza el estado del viaje
    @PUT("viajes/{id}/estado")
    Call<ViajeDTO> actualizarEstado(@Path("id") long id, @Body ActualizarEstadoRequest request);

    // Simulacion visual: lanza el proceso @Async en el backend
    @POST("viajes/{id}/simular")
    Call<Void> simularViaje(@Path("id") long id);

    // ── VEHICULOS (solo TRANSPORTISTA) ───────────────────────────────────────

    @GET("vehiculos/mis-vehiculos")
    Call<List<VehiculoDTO>> getMisVehiculos();

    @POST("vehiculos")
    Call<VehiculoDTO> crearVehiculo(@Body CrearVehiculoRequest request);
}
