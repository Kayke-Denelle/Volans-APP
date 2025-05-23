package com.example.volans_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.adapter.AtividadeBaralhoAdapter;
import com.example.volans_app.adapter.BaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atividade);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_atividade);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_baralhos) {
                startActivity(new Intent(this, BaralhoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_atividade) {
                return true;
            }
            return false;
        });

        rvBaralhos = findViewById(R.id.rvBaralhos);
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();

        if (token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupRecyclerView();
        listarBaralhos();
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
                if (response.isSuccessful()) {
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

}