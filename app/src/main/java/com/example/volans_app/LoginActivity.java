package com.example.volans_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.volans_app.api.AuthApi;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.DTO.LoginRequest;
import com.example.volans_app.DTO.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username e senha são obrigatórios", Toast.LENGTH_SHORT).show();
                } else {
                    login(username, password);
                }
            }
        });
    }

    private void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);

        AuthApi authApi = RetrofitClient.getRetrofitInstance().create(AuthApi.class);
        Call<AuthResponse> call = authApi.login(loginRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    String token = authResponse.getToken();
                    Toast.makeText(LoginActivity.this, "Login bem-sucedido! Token: " + token, Toast.LENGTH_LONG).show();
                    // Armazenar token em SharedPreferences ou outra forma de persistência
                } else {
                    Toast.makeText(LoginActivity.this, "Login falhou! Verifique as credenciais", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Falha na comunicação com o servidor
                Log.e("API Failure", "Falha: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Erro na conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}