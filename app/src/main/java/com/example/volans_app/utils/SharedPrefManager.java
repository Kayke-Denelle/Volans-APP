package com.example.volans_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "volans_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOME_USUARIO = "nome_usuario";
    private static final String KEY_EMAIL_USUARIO = "email_usuario"; // Nova chave para email

    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    // Verificar se está logado
    public boolean isLoggedIn() {
        return sharedPreferences.getString(KEY_TOKEN, null) != null;
    }

    // Token
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // User ID
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // Nome do usuário
    public void saveNomeUsuario(String nomeUsuario) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOME_USUARIO, nomeUsuario);
        editor.apply();
    }

    public String getNomeUsuario() {
        return sharedPreferences.getString(KEY_NOME_USUARIO, "");
    }

    // Email do usuário - NOVO
    public void saveEmailUsuario(String emailUsuario) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        editor.apply();
    }

    public String getEmailUsuario() {
        return sharedPreferences.getString(KEY_EMAIL_USUARIO, "");
    }

    // Método para salvar dados completos do usuário de uma vez
    public void saveUserData(String token, String userId, String nomeUsuario, String emailUsuario) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NOME_USUARIO, nomeUsuario);
        editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        editor.apply();
    }

    // Logout - limpar tudo
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}