package com.example.volans_app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.adapter.BaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private EditText etNome, etDescricao;
    private RecyclerView rvBaralhos;
    private BaralhoAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicialização dos componentes
        etNome = findViewById(R.id.etNomeBaralho);
        etDescricao = findViewById(R.id.etDescricaoBaralho);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        Button btnCriarBaralho = findViewById(R.id.btnCriarBaralho);
        Button btnLogout = findViewById(R.id.logoutButton);

        // Verificação do token
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configuração do RecyclerView
        setupRecyclerView();

        // Configuração dos listeners
        btnCriarBaralho.setOnClickListener(v -> criarBaralho());
        btnLogout.setOnClickListener(v -> fazerLogout());

        // Carregar baralhos
        listarBaralhos();
    }

    private void setupRecyclerView() {
        // Verificação do LayoutManager
        if (rvBaralhos.getLayoutManager() == null) {
            rvBaralhos.setLayoutManager(new LinearLayoutManager(this));
        }

        adapter = new BaralhoAdapter(listaBaralhos, new BaralhoAdapter.OnItemClickListener() {
            @Override
            public void onBaralhoClick(Baralho baralho) {
                // Abrir tela de Flashcards
                Intent intent = new Intent(DashboardActivity.this, FlashcardActivity.class);
                intent.putExtra("baralhoId", baralho.getId());
                startActivity(intent);
            }

            @Override
            public void onQuizClick(Baralho baralho) {
                // Chamar método para iniciar quiz
                iniciarQuiz(baralho.getId());
            }
        });

        rvBaralhos.setAdapter(adapter);
    }

    private void listarBaralhos() {
        Log.d("DASHBOARD", "Iniciando listagem de baralhos...");
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.listarBaralhos(token).enqueue(new Callback<List<Baralho>>() {
            @Override
            public void onResponse(Call<List<Baralho>> call, Response<List<Baralho>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DASHBOARD", "Baralhos recebidos: " + response.body().size());

                    listaBaralhos.clear();
                    listaBaralhos.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    Log.d("DASHBOARD", listaBaralhos.size() + " baralhos carregados no adapter");
                } else {
                    Log.e("DASHBOARD", "Erro na resposta: " + response.code());
                    Toast.makeText(DashboardActivity.this, "Erro ao carregar baralhos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Log.e("DASHBOARD", "Falha na requisição: " + t.getMessage());
                Toast.makeText(DashboardActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void criarBaralho() {
        String nome = etNome.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Baralho novoBaralho = new Baralho(nome, descricao);
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        api.criarBaralho(novoBaralho, token).enqueue(new Callback<Baralho>() {
            @Override
            public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaBaralhos.add(response.body());
                    adapter.notifyItemInserted(listaBaralhos.size() - 1);
                    etNome.setText("");
                    etDescricao.setText("");
                    Toast.makeText(DashboardActivity.this, "Baralho criado!", Toast.LENGTH_SHORT).show();
                } else {
                    tratarErroCriacao(response);
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para iniciar quiz
    // Método para iniciar quiz - Versão com logs detalhados
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
                        Toast.makeText(DashboardActivity.this,
                                "Nenhuma questão foi gerada para este quiz.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QuizModel quiz = new QuizModel();
                    quiz.setId("quiz-local-" + System.currentTimeMillis()); // ID local, não persistido no backend
                    quiz.setBaralhoId(baralhoId);
                    quiz.setPerguntas(questoes);

                    Intent intent = new Intent(DashboardActivity.this, QuizActivity.class);
                    intent.putExtra("quiz", quiz);
                    startActivity(intent);
                } else {
                    Log.e("QUIZ_API", "Erro ao gerar quiz. Código: " + response.code());
                    Toast.makeText(DashboardActivity.this,
                            "Erro ao gerar quiz: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuestaoQuiz>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("QUIZ_API", "Falha ao gerar quiz: " + t.getMessage(), t);
                Toast.makeText(DashboardActivity.this,
                        "Falha na conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void buscarQuizDetalhado(String quizId, ProgressDialog progressDialog) {
        Log.d("QUIZ_FLOW", "Buscando detalhes do quiz. ID: " + quizId);
        progressDialog.setMessage("Carregando quiz...");

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Log.d("QUIZ_FLOW", "Enviando requisição para buscar quiz detalhado...");
        api.buscarQuiz(token, quizId).enqueue(new Callback<QuizModel>() {
            @Override
            public void onResponse(Call<QuizModel> call, Response<QuizModel> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("QUIZ_FLOW", "Quiz detalhado recebido com sucesso");
                        Log.d("QUIZ_DATA_DETAILED", "Dados completos: " + response.body().toString());

                        // 3. Abrimos a tela do quiz com os dados completos
                        Intent intent = new Intent(DashboardActivity.this, QuizActivity.class);
                        intent.putExtra("quiz", response.body());
                        Log.d("QUIZ_FLOW", "Iniciando QuizActivity com dados do quiz");
                        startActivity(intent);
                    } else {
                        Log.e("QUIZ_ERROR", "Resposta vazia ao buscar quiz detalhado");
                        Toast.makeText(DashboardActivity.this,
                                "Erro: dados do quiz não disponíveis", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("QUIZ_ERROR", "Erro ao buscar quiz detalhado. Código: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e("QUIZ_ERROR_DETAILS", "Detalhes do erro: " + errorBody);
                    } catch (IOException e) {
                        Log.e("QUIZ_ERROR", "Erro ao ler corpo de erro", e);
                    }
                    Toast.makeText(DashboardActivity.this,
                            "Erro ao carregar quiz", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuizModel> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("QUIZ_ERROR", "Falha na comunicação ao buscar quiz detalhado", t);
                Toast.makeText(DashboardActivity.this,
                        "Falha ao carregar quiz: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tratarErroCriacao(Response<Baralho> response) {
        try {
            String erro = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
            Log.e("DASHBOARD", "Erro ao criar baralho: " + erro);
            Toast.makeText(this, "Erro: " + erro, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DASHBOARD", "Erro ao ler mensagem de erro", e);
        }
    }

    private void fazerLogout() {
        SharedPrefManager.getInstance(this).logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}