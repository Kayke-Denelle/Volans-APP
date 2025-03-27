package com.example.volans_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.volans_app.api.AuthApi;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.DTO.LoginRequest;
import com.example.volans_app.DTO.AuthResponse;
import com.example.volans_app.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private ImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se o usuário já está logado
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            redirectToDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        // Inicializa a logo
        logoImageView = findViewById(R.id.logoImageView);
        setupLogo();

        // Inicializa os componentes
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.register_link);

        // Configura o clique no botão de login
        loginButton.setOnClickListener(v -> handleLogin());

        // Configura o clique no "Comece já!"
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupLogo() {
        try {
            // Método 1: Usando recurso local
            logoImageView.setImageResource(R.drawable.logo_volans);

            // Ou Método 2: Usando Glide para mais opções (carregamento remoto, placeholder, etc)
            // Glide.with(this)
            //     .load(R.drawable.logo_volans)
            //     .placeholder(R.drawable.placeholder_logo)
            //     .error(R.drawable.error_logo)
            //     .into(logoImageView);

            // Ajustes opcionais na imagem
            logoImageView.setAdjustViewBounds(true);
            logoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        } catch (Exception e) {
            Log.e("LoginActivity", "Erro ao carregar logo", e);
            // Caso a logo não carregue, você pode esconder o ImageView
            logoImageView.setVisibility(View.GONE);
        }
    }

    // ... (restante dos seus métodos permanece igual) ...
    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Usuário e senha são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        login(username, password);
    }

    private void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);

        AuthApi authApi = RetrofitClient.getRetrofitInstance().create(AuthApi.class);
        Call<AuthResponse> call = authApi.login(loginRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    handleSuccessfulLogin(authResponse.getToken());
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login falhou! Verifique suas credenciais",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("API Failure", "Erro: " + t.getMessage());
                Toast.makeText(LoginActivity.this,
                        "Erro na conexão. Tente novamente.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(String token) {
        SharedPrefManager.getInstance(this).saveToken(token);
        Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
        redirectToDashboard();
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}