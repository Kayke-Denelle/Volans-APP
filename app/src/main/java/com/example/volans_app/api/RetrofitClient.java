package com.example.volans_app.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    // Cliente HTTP com timeout aumentado
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS) // tempo para estabelecer conexão
            .readTimeout(90, TimeUnit.SECONDS)    // tempo máximo para esperar resposta
            .writeTimeout(90, TimeUnit.SECONDS)   // tempo máximo para envio de dados
            .build();

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // adiciona o cliente com timeout customizado
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Para autenticação
    public static AuthApi getAuthApi() {
        return getRetrofitInstance().create(AuthApi.class);
    }

    // Para baralhos, flashcards, quizzes, etc.
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
