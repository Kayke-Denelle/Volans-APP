package com.example.volans_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Código da BottomNavigationView DEVE FICAR FORA do setOnApplyWindowInsetsListener
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_baralhos); // seleciona o item atual

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_baralhos) {
                // já está na MainActivity
                return true;
            } else if (itemId == R.id.nav_atividade) {
                startActivity(new Intent(MainActivity.this, QuizActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
