package com.example.volans_app;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.adapter.AtividadeBaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AtividadeActivity extends AppCompatActivity {

    private RecyclerView rvBaralhos;
    private AtividadeBaralhoAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atividade);

        // Inicializar views
        initializeViews();

        // Configurar bottom navigation moderna
        setupBottomNavigation();

        // Verificar token
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configurar RecyclerView e carregar dados
        setupRecyclerView();
        listarBaralhos();
    }

    private void initializeViews() {
        rvBaralhos = findViewById(R.id.rvBaralhos);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_atividade);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Animar o ícone selecionado
                animateBottomNavigationIcon(bottomNavigationView.findViewById(itemId));

                if (itemId == R.id.nav_atividade) {
                    // Já está na tela de atividades
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(AtividadeActivity.this, DashboardActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_baralhos) {
                    startActivity(new Intent(AtividadeActivity.this, BaralhoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                }

                return false;
            }
        });
    }

    private void animateBottomNavigationIcon(View view) {
        if (view != null) {
            ValueAnimator animator = ValueAnimator.ofFloat(0.8f, 1.0f);
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
            });
            animator.start();
        }
    }

    private void setupRecyclerView() {
        rvBaralhos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AtividadeBaralhoAdapter(listaBaralhos, new AtividadeBaralhoAdapter.OnQuizClickListener() {
            @Override
            public void onQuizClick(Baralho baralho) {
                iniciarQuiz(baralho.getId());
            }
        });
        rvBaralhos.setAdapter(adapter);
    }

    private void listarBaralhos() {
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.listarBaralhos(token).enqueue(new Callback<List<Baralho>>() {
            @Override
            public void onResponse(Call<List<Baralho>> call, Response<List<Baralho>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaBaralhos.clear();
                    listaBaralhos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AtividadeActivity.this, "Erro ao carregar baralhos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Toast.makeText(AtividadeActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarQuiz(String baralhoId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Gerando novo quiz...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<QuestaoQuiz>> call = api.gerarQuiz(token, baralhoId);
        call.enqueue(new Callback<List<QuestaoQuiz>>() {
            @Override
            public void onResponse(Call<List<QuestaoQuiz>> call, Response<List<QuestaoQuiz>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    List<QuestaoQuiz> questoes = response.body();

                    if (questoes.isEmpty()) {
                        Toast.makeText(AtividadeActivity.this,
                                "Nenhuma questão foi gerada para este quiz.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QuizModel quiz = new QuizModel();
                    quiz.setId("quiz-local-" + System.currentTimeMillis());
                    quiz.setBaralhoId(baralhoId);
                    quiz.setPerguntas(questoes);

                    Intent intent = new Intent(AtividadeActivity.this, QuizActivity.class);
                    intent.putExtra("quiz", quiz);
                    startActivity(intent);
                } else {
                    Log.e("QUIZ_API", "Erro ao gerar quiz. Código: " + response.code());
                    Toast.makeText(AtividadeActivity.this,
                            "Erro ao gerar quiz: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuestaoQuiz>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("QUIZ_API", "Falha ao gerar quiz: " + t.getMessage(), t);
                Toast.makeText(AtividadeActivity.this,
                        "Falha na conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garantir que o item correto esteja selecionado
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_atividade);
        }
    }

    @Override
    public void onBackPressed() {
        // Navegar para o Dashboard ao pressionar voltar
        startActivity(new Intent(this, DashboardActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}