package com.example.volans_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.adapter.FlashcardAdapter;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardActivity extends AppCompatActivity {

    private EditText etPergunta, etResposta;
    private Button btnAdicionarFlashcard, btnVoltar;
    private RecyclerView rvFlashcards;
    private FlashcardAdapter adapter;
    private List<Flashcard> lista = new ArrayList<>();
    private String baralhoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard2);

        // Inicializando os componentes
        etPergunta = findViewById(R.id.etPergunta);
        etResposta = findViewById(R.id.etResposta);
        btnAdicionarFlashcard = findViewById(R.id.btnAdicionarFlashcard);
        btnVoltar = findViewById(R.id.btnVoltar); // Referência ao botão Voltar
        rvFlashcards = findViewById(R.id.rvFlashcards);

        // Pegando o ID do baralho da Intent
        baralhoId = getIntent().getStringExtra("baralhoId");

        // Configurando o RecyclerView
        adapter = new FlashcardAdapter(lista);
        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        rvFlashcards.setAdapter(adapter);

        // Carregar os flashcards ao iniciar
        carregarFlashcards();

        // Ação para adicionar flashcard
        btnAdicionarFlashcard.setOnClickListener(v -> adicionarFlashcard());

        // Ação para voltar para a tela de baralhos
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(FlashcardActivity.this, DashboardActivity.class); // Ajuste para a Activity dos baralhos
            startActivity(intent);
            finish(); // Finaliza a FlashcardActivity para não deixar ela na pilha de atividades
        });
    }

    private void carregarFlashcards() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        Log.d("FlashcardActivity", "Token: " + token);
        Log.d("FlashcardActivity", "Baralho ID: " + baralhoId);

        Call<List<Flashcard>> call = RetrofitClient.getApiService().getFlashcardsPorBaralho(baralhoId, token);
        call.enqueue(new Callback<List<Flashcard>>() {
            @Override
            public void onResponse(Call<List<Flashcard>> call, Response<List<Flashcard>> response) {
                Log.d("FlashcardActivity", "Resposta recebida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FlashcardActivity", "Flashcards recebidos: " + response.body().size());

                    lista.clear();
                    lista.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    try {
                        String erro = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
                        Log.e("FlashcardActivity", "Erro ao carregar flashcards: " + erro);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(FlashcardActivity.this, "Erro ao carregar flashcards", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Flashcard>> call, Throwable t) {
                Log.e("FlashcardActivity", "Falha na requisição: " + t.getMessage(), t);
                Toast.makeText(FlashcardActivity.this, "Erro ao carregar flashcards", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Método para adicionar um novo flashcard
    private void adicionarFlashcard() {
        String pergunta = etPergunta.getText().toString();
        String resposta = etResposta.getText().toString();

        if (pergunta.isEmpty() || resposta.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();

        Flashcard novo = new Flashcard();
        novo.setPergunta(pergunta);
        novo.setResposta(resposta);
        novo.setBaralhoId(baralhoId);

        RetrofitClient.getApiService().criarFlashcard(novo, token).enqueue(new Callback<Flashcard>() {
            @Override
            public void onResponse(Call<Flashcard> call, Response<Flashcard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lista.add(response.body());
                    adapter.notifyItemInserted(lista.size() - 1);
                    etPergunta.setText("");
                    etResposta.setText("");
                    Toast.makeText(FlashcardActivity.this, "Flashcard criado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FlashcardActivity.this, "Erro ao criar flashcard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Flashcard> call, Throwable t) {
                Toast.makeText(FlashcardActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

