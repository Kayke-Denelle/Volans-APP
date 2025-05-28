package com.example.volans_app;

import android.graphics.Color; import android.os.Build; import android.os.Bundle; import android.util.Log; import android.view.View; import android.view.WindowManager; import android.widget.Button; import android.widget.EditText; import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.volans_app.api.AuthApi; import com.example.volans_app.api.RetrofitClient; import com.example.volans_app.DTO.RegisterRequest; import com.example.volans_app.DTO.RegisterResponse;

import retrofit2.Call; import retrofit2.Callback; import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupStatusBar();

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText emailInput = findViewById(R.id.email_input);
        Button registerButton = findViewById(R.id.register_button);
        Button loginLink = findViewById(R.id.btnLogin);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (validateInputs(username, password, email)) {
                registerUser(username, password, email);
            }
        });

        loginLink.setOnClickListener(v -> finish()); // Volta para a tela de login

    }

    private boolean validateInputs(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser(String username, String password, String email) {
        RegisterRequest request = new RegisterRequest(username, password, email);
        AuthApi apiService = RetrofitClient.getRetrofitInstance().create(AuthApi.class);
        Call<RegisterResponse> call = apiService.register(request);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show();
                    finish(); // Retorna para a tela de login
                } else {
                    Log.e("RegisterError", "Code: " + response.code() + " - " + response.message());
                    Toast.makeText(RegisterActivity.this, "Erro no registro: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("API Failure", t.getMessage());
                Toast.makeText(RegisterActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStatusBar() {
        // Remove a flag fullscreen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Torna a status bar transparente
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Permite que o conteúdo vá atrás da status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Define ícones da status bar como escuros (para fundo claro) ou claros (para fundo escuro)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR  // Para ícones escuros
            );
        }
    }
}