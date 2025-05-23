package com.example.volans_app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.volans_app.adapter.BaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaralhoActivity extends AppCompatActivity {

    private EditText etNome, etDescricao;
    private RecyclerView rvBaralhos;
    private BaralhoAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baralho);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_baralhos); // seleciona o item atual

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_baralhos) {
                // já está na Dashboard
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(BaralhoActivity.this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_atividade) {
                startActivity(new Intent(BaralhoActivity.this, AtividadeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        // Inicialização dos componentes
        etNome = findViewById(R.id.etNomeBaralho);
        etDescricao = findViewById(R.id.etDescricaoBaralho);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        Button btnCriarBaralho = findViewById(R.id.btnCriarBaralho);

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
                Intent intent = new Intent(BaralhoActivity.this, FlashcardActivity.class);
                intent.putExtra("baralhoId", baralho.getId());
                startActivity(intent);
            }

        }, false); // <-- Passa o terceiro parâmetro boolean aqui


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
                    Toast.makeText(BaralhoActivity.this, "Erro ao carregar baralhos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Log.e("DASHBOARD", "Falha na requisição: " + t.getMessage());
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BaralhoActivity.this, "Baralho criado!", Toast.LENGTH_SHORT).show();
                } else {
                    tratarErroCriacao(response);
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
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

}