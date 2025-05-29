package com.example.volans_app.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://volans-api-javaspring.onrender.com";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Para o AuthApi
    public static AuthApi getAuthApi() {
        return getRetrofitInstance().create(AuthApi.class);
    }

    // Para o ApiService de baralho e flashcard
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}