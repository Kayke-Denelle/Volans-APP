package com.example.volans_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.volans_app.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1700; // 1,7 segundos

    private ImageView logoImage;
    private TextView appName;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar tela cheia
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        // Inicializar views
        initViews();

        // Configurar status bar
        setupStatusBar();

        // Iniciar animações
        startAnimations();

        // Iniciar timer para próxima tela
        startSplashTimer();
    }

    private void initViews() {
        logoImage = findViewById(R.id.logo_image);
        appName = findViewById(R.id.app_name);
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

    private void startAnimations() {
        // Animação de fade in para o logo
        logoImage.setAlpha(0f);
        logoImage.animate()
                .alpha(1f)
                .setDuration(1000)
                .start();

        // Animação de fade in para o texto (com delay)
        appName.setAlpha(0f);
        appName.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(500)
                .start();

    }

    private void startSplashTimer() {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToWelcomeActivity();
            }
        }, SPLASH_DURATION);
    }

    private void navigateToWelcomeActivity() {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Desabilitar botão voltar na splash screen
    }
}