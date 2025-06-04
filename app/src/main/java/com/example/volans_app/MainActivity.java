package com.example.volans_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate");

        setupStatusBar();
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView == null) {
            Log.e(TAG, "ERRO: BottomNavigationView não encontrada!");
        } else {
            Log.d(TAG, "BottomNavigationView encontrada com sucesso");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "Não é possível configurar navegação - BottomNavigationView é null");
            return;
        }

        // MainActivity representa "Início"
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Log.d(TAG, "Item clicado: " + getResourceName(itemId));

                if (itemId == R.id.nav_dashboard) {
                    // Já está na MainActivity (Início)
                    Log.d(TAG, "Já está na tela Início");
                    return true;

                } else if (itemId == R.id.nav_baralhos) {
                    Log.d(TAG, "Navegando para BaralhoActivity");
                    Intent intent = new Intent(MainActivity.this, BaralhoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.nav_atividade) {
                    Log.d(TAG, "Navegando para AtividadeActivity");
                    Intent intent = new Intent(MainActivity.this, AtividadeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.nav_tarefas) {
                    Log.d(TAG, "Navegando para TarefaActivity");
                    Intent intent = new Intent(MainActivity.this, TarefaActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                Log.w(TAG, "Item não reconhecido: " + itemId);
                return false;
            }
        });
    }

    private String getResourceName(int resourceId) {
        try {
            return getResources().getResourceEntryName(resourceId);
        } catch (Exception e) {
            return "unknown_" + resourceId;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");

        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }
    }
}