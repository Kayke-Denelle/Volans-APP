package com.example.volans_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.volans_app.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 segundos para melhor efeito

    private ImageView logoImage;
    private TextView appName;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        initViews();
        setupStatusBar();
        startEnhancedAnimations();
        startSplashTimer();
    }

    private void initViews() {
        logoImage = findViewById(R.id.logo_image);
        appName = findViewById(R.id.app_name);
    }

    private void setupStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void startEnhancedAnimations() {
        // Animação do logo - efeito bounce suave
        logoImage.setAlpha(0f);
        logoImage.setScaleX(0.3f);
        logoImage.setScaleY(0.3f);

        logoImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animação do texto - slide up suave
        appName.setAlpha(0f);
        appName.setTranslationY(50f);

        appName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void startSplashTimer() {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserLoginStatus();
            }
        }, SPLASH_DURATION);
    }

    private void checkUserLoginStatus() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        String token = sharedPrefManager.getToken();

        if (token != null && !token.isEmpty()) {
            navigateToDashboard();
        } else {
            navigateToWelcomeActivity();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
        // Efeito suave personalizado - slide in
        overridePendingTransition(R.anim.duolingo_slide_up_in, R.anim.duolingo_slide_down_out);
    }

    private void navigateToWelcomeActivity() {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
        // Efeito suave personalizado - slide in
        overridePendingTransition(R.anim.duolingo_slide_up_in, R.anim.duolingo_slide_down_out);
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
