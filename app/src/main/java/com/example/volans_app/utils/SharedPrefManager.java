package com.example.volans_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "AuthPrefs"; // Nome do arquivo SharedPreferences
    private static final String KEY_TOKEN = "authToken"; // Chave para o token
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains("userToken"); // Verifica se o token existe
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userToken", token); // Salva o token
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString("userToken", null); // Retorna o token salvo ou null
    }

    // Remover o token (ex.: logout)
    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Remove todas as informações salvas
        editor.apply();
    }
}
