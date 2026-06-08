package com.pispax.app.network;

import android.content.Context;

import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Singleton que construye y provee el cliente HTTP de la app.
// Usa OkHttp para añadir el JWT a cada peticion y Retrofit para mapear los endpoints de ApiService.
public class RetrofitClient {

    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context ctx) {
        // Log de requests/responses en Logcat (solo para desarrollo)
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor que inyecta el JWT en cada peticion protegida
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = SessionManager.getToken(ctx);
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(logger)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // Inicializa o reutiliza la instancia. Llamar siempre con Application Context.
    public static synchronized RetrofitClient getInstance(Context ctx) {
        if (instance == null) {
            instance = new RetrofitClient(ctx.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}
