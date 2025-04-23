package com.example.volans_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

        etNome = findViewById(R.id.etNomeBaralho);
        etDescricao = findViewById(R.id.etDescricaoBaralho);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        Button btnCriarBaralho = findViewById(R.id.btnCriarBaralho);
        Button btnLogout = findViewById(R.id.logoutButton);

        // Verifica se o token está presente e válido
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.isEmpty()) {
            // Se o token não for válido, redireciona para login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Configuração do adapter para os baralhos
        adapter = new BaralhoAdapter(listaBaralhos, baralho -> {
            // Ao clicar no baralho, abre a tela de flashcards
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("baralhoId", baralho.getId());
            startActivity(intent);
        });

        rvBaralhos.setLayoutManager(new LinearLayoutManager(this));
        rvBaralhos.setAdapter(adapter);

        // Evento de clique para criar o baralho
        btnCriarBaralho.setOnClickListener(v -> criarBaralho());

        // Evento de clique para logout
        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Listar baralhos ao carregar a tela
        listarBaralhos();
    }

    private void criarBaralho() {
        String nome = etNome.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Nome e descrição são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criação do baralho
        Baralho baralho = new Baralho(nome, descricao);

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.criarBaralho(baralho, token).enqueue(new Callback<Baralho>() {
            @Override
            public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                if (response.isSuccessful()) {
                    listaBaralhos.add(response.body());  // Adiciona o baralho à lista
                    adapter.notifyDataSetChanged();      // Notifica o adapter para atualizar a RecyclerView
                    etNome.setText("");
                    etDescricao.setText("");
                    Toast.makeText(DashboardActivity.this, "Baralho criado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    // Se a resposta não for bem-sucedida, exibe o código de erro e a resposta
                    String errorMessage = "Erro ao criar baralho!";
                    try {
                        // Tenta capturar a mensagem de erro do corpo da resposta
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                // Mostra uma mensagem de erro genérica se ocorrer uma falha na requisição
                Toast.makeText(DashboardActivity.this, "Falha na conexão ao criar baralho!", Toast.LENGTH_SHORT).show();
            }
        });
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
                    Toast.makeText(DashboardActivity.this, "Nenhum baralho encontrado!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Falha na conexão ao listar os baralhos!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
