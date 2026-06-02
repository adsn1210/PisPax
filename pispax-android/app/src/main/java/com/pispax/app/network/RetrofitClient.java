package com.pispax.app.network;

// TODO: implementar el Singleton de Retrofit con:
//   - BASE_URL desde Constants.BASE_URL
//   - OkHttpClient con interceptor que inyecta el JWT de SessionManager
//   - GsonConverterFactory
//   - Instancia de ApiService

public class RetrofitClient {

    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient() {
        // TODO: construir Retrofit
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
