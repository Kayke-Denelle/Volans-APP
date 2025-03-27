package com.example.volans_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.volans_app.utils.SharedPrefManager;

public class DashboardActivity extends AppCompatActivity {

            @SuppressLint("MissingInflatedId")
            @Override
            protected void onCreate (Bundle savedInstanceState){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_dashboard);

                findViewById(R.id.logoutButton).setOnClickListener(v -> {
                    SharedPrefManager.getInstance(DashboardActivity.this).logout();
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Finaliza a tela de Dashboard
                });
            }
        }

