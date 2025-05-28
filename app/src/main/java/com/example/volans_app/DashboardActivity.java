package com.example.volans_app;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvQuantidadeBaralhos, tvQuantidadeFlashcards;
    private TextView tvNomeUsuario;
    private ApiService apiService;

    private Button btnLogout;
    private String token;

    private BarChart barChart;  // Declaração do gráfico

    // Adicionado para o WebView do chatbot
    private WebView webView;
    private FrameLayout chatContainer;
    private View mainInterface;
    private boolean isChatbotLoaded = false;
    private Button btnCriarBaralho, btnMeusBaralhos;

    // Drawer
    private DrawerLayout drawerLayout;
    private ImageView profileImageDrawer;
    private TextView tvNomeUsuarioDrawer;

    // Galeria
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mantém o layout atual do Dashboard
        setContentView(R.layout.activity_dashboard);

        // Inicializar views do dashboard
        tvQuantidadeBaralhos = findViewById(R.id.tvQuantidadeBaralhos);
        tvQuantidadeFlashcards = findViewById(R.id.tvQuantidadeFlashcards);
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        btnLogout = findViewById(R.id.btnLogout);
        barChart = findViewById(R.id.barChartRevisoes);
        btnCriarBaralho = findViewById(R.id.btnCriarBaralho);
        btnMeusBaralhos = findViewById(R.id.btnMeusBaralhos);

        // Drawer views
        drawerLayout = findViewById(R.id.drawer_layout);
        profileImageDrawer = findViewById(R.id.profileImageDrawer);
        tvNomeUsuarioDrawer = findViewById(R.id.tvNomeUsuarioDrawer);

        // Configurar drawer
        setupDrawer();

        btnCriarBaralho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AtividadeActivity.class);
                startActivity(intent);
            }
        });

        btnMeusBaralhos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, BaralhoActivity.class);
                startActivity(intent);
            }
        });

        // Inicializar views do chatbot
        webView = findViewById(R.id.webview);
        chatContainer = findViewById(R.id.chat_container);
        mainInterface = findViewById(R.id.layoutAcoes);
        FloatingActionButton chatButton = findViewById(R.id.chat_button);
        FloatingActionButton closeButton = findViewById(R.id.close_chat_button);

        // Configurar WebView do chatbot
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Botão abrir chat
        chatButton.setOnClickListener(v -> {
            chatContainer.setVisibility(View.VISIBLE);

            if (!isChatbotLoaded) {
                webView.loadUrl("https://cdn.botpress.cloud/webchat/v2.4/shareable.html?configUrl=https://files.bpcontent.cloud/2025/04/30/22/20250430222055-KNV4LQWN.json");
                isChatbotLoaded = true;
            }
        });

        // Botão fechar chat
        closeButton.setOnClickListener(v -> chatContainer.setVisibility(View.GONE));

        // Ajustar padding para EdgeToEdge (se estiver usando)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutAcoes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true; // Já está aqui
            } else if (itemId == R.id.nav_baralhos) {
                startActivity(new Intent(DashboardActivity.this, BaralhoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_atividade) {
                startActivity(new Intent(DashboardActivity.this, AtividadeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(DashboardActivity.this).logout();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();

        if (token.equals("Bearer ")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String nomeUsuario = SharedPrefManager.getInstance(this).getNomeUsuario();
        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = "Usuário";
        }
        tvNomeUsuario.setText("Olá, " + nomeUsuario + "!");
        tvNomeUsuarioDrawer.setText(nomeUsuario);

        apiService = RetrofitClient.getApiService();

        carregarQuantidades();
        configurarGrafico();
    }

    private void setupDrawer() {
        // Configurar toolbar para abrir drawer
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Configurar clique na foto para abrir galeria
        profileImageDrawer.setOnClickListener(v -> openImagePicker());

        // Configurar cliques do menu drawer
        setupDrawerMenuClicks();
    }

    private void setupDrawerMenuClicks() {
        // Dashboard
        findViewById(R.id.drawerMenuDashboard).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Baralhos
        findViewById(R.id.drawerMenuBaralhos).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, BaralhoActivity.class));
        });

        // Atividades
        findViewById(R.id.drawerMenuAtividades).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AtividadeActivity.class));
        });

        // Meu Perfil
        findViewById(R.id.drawerMenuPerfil).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Implementar se necessário
        });

        // Termos
        findViewById(R.id.drawerMenuTermos).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Implementar se necessário
        });

        // Sair
        findViewById(R.id.drawerMenuSair).setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                profileImageDrawer.setImageURI(imageUri);
                // Salvar URI da imagem se necessário
                // SharedPrefManager.getInstance(this).saveProfileImageUri(imageUri.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (chatContainer.getVisibility() == View.VISIBLE) {
            chatContainer.setVisibility(View.GONE);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void carregarQuantidades() {
        if (token.equals("Bearer ")) {
            tvQuantidadeBaralhos.setText("Baralhos: sem token");
            tvQuantidadeFlashcards.setText("Flashcards: sem token");
            return;
        }

        apiService.getQuantidadeBaralhos(token).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvQuantidadeBaralhos.setText("" + response.body());
                } else {
                    tvQuantidadeBaralhos.setText("Baralhos: erro");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                tvQuantidadeBaralhos.setText("Baralhos: falha");
            }
        });

        apiService.getQuantidadeFlashcards(token).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvQuantidadeFlashcards.setText("" + response.body());
                } else {
                    tvQuantidadeFlashcards.setText("Flashcards: erro");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                tvQuantidadeFlashcards.setText("Flashcards: falha");
            }
        });
    }

    private void configurarGrafico() {
        List<BarEntry> entradas = new ArrayList<>();
        entradas.add(new BarEntry(0f, 4));
        entradas.add(new BarEntry(1f, 7));
        entradas.add(new BarEntry(2f, 3));
        entradas.add(new BarEntry(3f, 6));
        entradas.add(new BarEntry(4f, 8));

        BarDataSet dataSet = new BarDataSet(entradas, "Quizzes realizados");
        dataSet.setColor(getResources().getColor(R.color.blue));
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setBackgroundColor(getResources().getColor(R.color.background_dark));

        XAxis xAxis = barChart.getXAxis();
        final String[] labels = new String[]{"S1", "S2", "S3", "S4", "S5"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(android.graphics.Color.WHITE);
        xAxis.setTextSize(14f);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(android.graphics.Color.WHITE);

        barChart.getAxisLeft().setTextColor(android.graphics.Color.WHITE);
        barChart.getAxisLeft().setTextSize(14f);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(android.graphics.Color.WHITE);

        barChart.getAxisRight().setTextColor(android.graphics.Color.WHITE);
        barChart.getAxisRight().setTextSize(14f);
        barChart.getAxisRight().setDrawGridLines(false);

        barChart.getLegend().setTextColor(android.graphics.Color.WHITE);
        barChart.getLegend().setTextSize(14f);

        barChart.invalidate();
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