package com.example.volans_app;

import android.annotation.SuppressLint;
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
import com.example.volans_app.adapter.BaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;

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
                abrirFlashcards(baralho);
            }

            @Override
            public void onQuizClick(Baralho baralho) {
                abrirQuiz(baralho);
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

    private void tratarErroCriacao(Response<Baralho> response) {
        try {
            String erro = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
            Log.e("DASHBOARD", "Erro ao criar baralho: " + erro);
            Toast.makeText(this, "Erro: " + erro, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DASHBOARD", "Erro ao ler mensagem de erro", e);
        }
    }

    private void abrirFlashcards(Baralho baralho) {
        Intent intent = new Intent(this, FlashcardActivity.class);
        intent.putExtra("baralhoId", baralho.getId());
        startActivity(intent);
    }

    private void abrirQuiz(Baralho baralho) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("baralhoId", baralho.getId());
        startActivity(intent);
    }

    private void fazerLogout() {
        SharedPrefManager.getInstance(this).logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}