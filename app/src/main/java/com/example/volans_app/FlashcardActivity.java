package com.example.volans_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.adapter.FlashcardAdapter;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardActivity extends AppCompatActivity {

    private EditText etPergunta, etResposta;
    private Button btnAdicionarFlashcard;
    private Button btnVoltar;
    private RecyclerView rvFlashcards;
    private LinearLayout layoutEmptyState;
    private TextView tvFlashcardCount;
    private ExtendedFloatingActionButton fabAddFlashcard;
    private FlashcardAdapter adapter;
    private List<Flashcard> lista = new ArrayList<>();
    private String baralhoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard2);

        // Inicializando os componentes
        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        // Pegando o ID do baralho da Intent
        baralhoId = getIntent().getStringExtra("baralhoId");

        // Carregar os flashcards ao iniciar
        carregarFlashcards();
    }

    private void initializeViews() {
        etPergunta = findViewById(R.id.etPergunta);
        etResposta = findViewById(R.id.etResposta);
        btnAdicionarFlashcard = findViewById(R.id.btnAdicionarFlashcard);
        btnVoltar = findViewById(R.id.btnVoltar);
        rvFlashcards = findViewById(R.id.rvFlashcards);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvFlashcardCount = findViewById(R.id.tvFlashcardCount);
        fabAddFlashcard = findViewById(R.id.fabAddFlashcard);
    }

    private void setupRecyclerView() {
        adapter = new FlashcardAdapter(lista);
        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        rvFlashcards.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Ação para adicionar flashcard
        btnAdicionarFlashcard.setOnClickListener(v -> {
            animateButton(btnAdicionarFlashcard);
            adicionarFlashcard();
        });

        // FAB também adiciona flashcard
        fabAddFlashcard.setOnClickListener(v -> {
            animateFAB(fabAddFlashcard);
            // Scroll para o topo para mostrar o formulário
            findViewById(R.id.etPergunta).requestFocus();
        });

        // Ação para voltar
        btnVoltar.setOnClickListener(v -> {
            animateButton(btnVoltar);
            Intent intent = new Intent(FlashcardActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
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
                    updateUI();
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

    private void adicionarFlashcard() {
        String pergunta = etPergunta.getText().toString().trim();
        String resposta = etResposta.getText().toString().trim();

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

                    // Limpar campos
                    etPergunta.setText("");
                    etResposta.setText("");

                    // Atualizar UI
                    updateUI();

                    // Scroll para mostrar o novo item
                    rvFlashcards.smoothScrollToPosition(lista.size() - 1);

                    Toast.makeText(FlashcardActivity.this, "✨ Flashcard criado com sucesso!", Toast.LENGTH_SHORT).show();
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

    private void updateUI() {
        int count = lista.size();

        // Atualizar contador
        String countText = count == 0 ? "Nenhum flashcard" :
                count == 1 ? "1 flashcard" :
                        count + " flashcards";
        tvFlashcardCount.setText(countText);

        // Mostrar/esconder estado vazio
        if (count == 0) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvFlashcards.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvFlashcards.setVisibility(View.VISIBLE);
        }
    }

    // Animações sutis
    private void animateButton(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateFAB(View fab) {
        fab.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .withEndAction(() -> {
                    fab.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
}
