package com.example.volans_app.api;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.LoginRequest;
import com.example.volans_app.DTO.AuthResponse;
import com.example.volans_app.DTO.RegisterRequest;
import com.example.volans_app.DTO.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

    public interface AuthApi {

        @POST("api/auth/register")
        Call<RegisterResponse> register(@Body RegisterRequest request);


        @POST("api/auth/login")
        Call<AuthResponse> login(@Body LoginRequest loginRequest);


    }

