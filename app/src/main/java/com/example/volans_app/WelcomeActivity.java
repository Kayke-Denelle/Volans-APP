package com.example.volans_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setupStatusBar();

        Button btnComeceJa = findViewById(R.id.btnComeceJa);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Encontre o TextView
        TextView titleText = findViewById(R.id.title_text);

        // Crie o texto com formatação
        SpannableString spannableString = new SpannableString("Transforme\nconhecimento\nem hábito.");

        // Defina "Transforme" como bold (posição 0-10)
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Defina "conhecimento" como medium (posição 11-23)
        spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 11, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Defina "hábito" como bold (posição 27-33)
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 27, 33, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplique ao TextView
        titleText.setText(spannableString);

        // Ação do botão "Comece Já!"
        btnComeceJa.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Ação do botão "Login"
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
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