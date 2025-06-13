package com.example.volans_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.volans_app.DTO.Tarefa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "volans_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOME_USUARIO = "nome_usuario";
    private static final String KEY_EMAIL_USUARIO = "email_usuario";
    private static final String KEY_PROFILE_IMAGE_PATH = "profile_image_path"; // Nova chave para imagem de perfil
    private static final String KEY_PROFILE_IMAGE_URL = "profile_image_url"; // Para URLs remotas se necessário

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

    // User ID - ATUALIZADO para retornar valor padrão
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        // CORREÇÃO: Retorna "default_user" se não houver ID salvo
        return sharedPreferences.getString(KEY_USER_ID, "default_user");
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

    // Email do usuário
    public void saveEmailUsuario(String emailUsuario) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        editor.apply();
    }

    public String getEmailUsuario() {
        return sharedPreferences.getString(KEY_EMAIL_USUARIO, "");
    }

    // ========== MÉTODOS PARA IMAGEM DE PERFIL ==========

    // Salvar caminho da imagem de perfil local
    public void saveProfileImagePath(String imagePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_IMAGE_PATH, imagePath);
        editor.apply();
    }

    // Recuperar caminho da imagem de perfil local
    public String getProfileImagePath() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_PATH, null);
    }

    // Salvar URL da imagem de perfil remota (se usar servidor)
    public void saveProfileImageUrl(String imageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_IMAGE_URL, imageUrl);
        editor.apply();
    }

    // Recuperar URL da imagem de perfil remota
    public String getProfileImageUrl() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, null);
    }

    // Verificar se existe imagem de perfil (local ou remota)
    public boolean hasProfileImage() {
        String localPath = getProfileImagePath();
        String remoteUrl = getProfileImageUrl();
        return (localPath != null && !localPath.isEmpty()) ||
                (remoteUrl != null && !remoteUrl.isEmpty());
    }

    // Limpar dados da imagem de perfil
    public void clearProfileImage() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PROFILE_IMAGE_PATH);
        editor.remove(KEY_PROFILE_IMAGE_URL);
        editor.apply();
    }

    // ========== MÉTODOS COMBINADOS ==========

    // Método para salvar dados completos do usuário de uma vez (incluindo imagem)
    public void saveUserData(String token, String userId, String nomeUsuario, String emailUsuario) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NOME_USUARIO, nomeUsuario);
        editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        editor.apply();
    }

    // Método para salvar dados completos do usuário incluindo imagem
    public void saveUserDataWithImage(String token, String userId, String nomeUsuario,
                                      String emailUsuario, String profileImagePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NOME_USUARIO, nomeUsuario);
        editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            editor.putString(KEY_PROFILE_IMAGE_PATH, profileImagePath);
        }
        editor.apply();
    }

    // Método para atualizar apenas o perfil do usuário
    public void updateUserProfile(String nomeUsuario, String emailUsuario, String profileImagePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (nomeUsuario != null) {
            editor.putString(KEY_NOME_USUARIO, nomeUsuario);
        }
        if (emailUsuario != null) {
            editor.putString(KEY_EMAIL_USUARIO, emailUsuario);
        }
        if (profileImagePath != null) {
            editor.putString(KEY_PROFILE_IMAGE_PATH, profileImagePath);
        }
        editor.apply();
    }

    // Logout - limpar tudo (incluindo imagem de perfil)
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Logout mantendo apenas dados básicos (se necessário)
    public void logoutKeepingBasicData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        // Mantém nome, email e imagem de perfil para próximo login
        editor.apply();
    }

    // ========== MÉTODOS UTILITÁRIOS ==========

    // Verificar se todos os dados do usuário estão completos
    public boolean isUserDataComplete() {
        return getToken() != null &&
                getUserId() != null &&
                !getNomeUsuario().isEmpty() &&
                !getEmailUsuario().isEmpty();
    }

    public void saveTarefas(List<Tarefa> tarefas) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(tarefas);
        editor.putString("tarefas_list", json);
        editor.apply();
    }

    public List<Tarefa> getTarefas() {
        String json = sharedPreferences.getString("tarefas_list", "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Tarefa>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Método para obter próximas tarefas para o Dashboard
    public List<Tarefa> getProximasTarefas(int limit) {
        List<Tarefa> todasTarefas = getTarefas();
        return todasTarefas.stream()
                .filter(t -> !t.isConcluida())
                .sorted((t1, t2) -> t1.getDataLimite().compareTo(t2.getDataLimite()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Obter resumo dos dados do usuário para debug
    public String getUserDataSummary() {
        return "Token: " + (getToken() != null ? "✓" : "✗") +
                ", UserID: " + (getUserId() != null ? "✓" : "✗") +
                ", Nome: " + (getNomeUsuario().isEmpty() ? "✗" : "✓") +
                ", Email: " + (getEmailUsuario().isEmpty() ? "✗" : "✓") +
                ", Imagem: " + (hasProfileImage() ? "✓" : "✗");
    }
}
