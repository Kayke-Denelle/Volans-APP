package com.example.volans_app.api;

import com.example.volans_app.DTO.LoginRequest;
import com.example.volans_app.DTO.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

    public interface AuthApi {

        @POST("api/auth/login")
        Call<AuthResponse> login(@Body LoginRequest loginRequest);
    }

