package com.example.volans_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.adapter.FlashcardAdapter;
import com.example.volans_app.adapter.ItemTouchHelperCallback;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardActivity extends AppCompatActivity implements
        FlashcardAdapter.OnItemClickListener, FlashcardAdapter.OnStartDragListener {

    private EditText etPergunta, etResposta;
    private Button btnAdicionarFlashcard;
    private ImageView btnVoltar;
    private RecyclerView rvFlashcards;
    private LinearLayout layoutEmptyState;
    private TextView tvBaralhoNome;
    private ExtendedFloatingActionButton fabAddFlashcard;
    private FlashcardAdapter adapter;
    private List<Flashcard> lista = new ArrayList<>();
    private String baralhoId;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar a barra de status ANTES de setContentView
        setupStatusBar();

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

    private void setupStatusBar() {
        // Configurar a janela para desenhar atrás das barras do sistema
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Definir a cor da barra de status para combinar com o fundo
            getWindow().setStatusBarColor(0xFFF5F7FA); // #F5F7FA

            // Configurar para ícones escuros na barra de status (para fundo claro)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }

            // Configurar barra de navegação para Android 8.1+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                getWindow().setNavigationBarColor(0xFFF5F7FA); // #F5F7FA

                // Ícones escuros na barra de navegação
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Para Android 8.0, apenas definir a cor
                getWindow().setNavigationBarColor(0xFFF5F7FA); // #F5F7FA
            }
        }
    }

    private void initializeViews() {
        etPergunta = findViewById(R.id.etPergunta);
        etResposta = findViewById(R.id.etResposta);
        btnAdicionarFlashcard = findViewById(R.id.btnAdicionarFlashcard);
        btnVoltar = findViewById(R.id.btnVoltar);
        rvFlashcards = findViewById(R.id.rvFlashcards);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvBaralhoNome = findViewById(R.id.tvBaralhoNome);
        fabAddFlashcard = findViewById(R.id.fabAddFlashcard);
    }

    private void setupRecyclerView() {
        adapter = new FlashcardAdapter(lista);
        adapter.setOnItemClickListener(this);
        adapter.setOnStartDragListener(this);

        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        rvFlashcards.setAdapter(adapter);

        // Configurar ItemTouchHelper para drag & drop
        ItemTouchHelperCallback callback = new ItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvFlashcards);
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
            voltarParaDashboard();
        });
    }

    // Implementação das interfaces do adapter
    @Override
    public void onDeleteClick(int position) {
        mostrarDialogoExclusao(position);
    }

    @Override
    public void onItemClick(int position) {
        // Implementar se quiser ação ao clicar no item
        // Toast.makeText(this, "Flashcard: " + lista.get(position).getPergunta(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void mostrarDialogoExclusao(int position) {
        Flashcard flashcard = lista.get(position);

        // Criar o diálogo customizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_flashcard, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Configurar os elementos do diálogo
        TextView tvPerguntaDialog = dialogView.findViewById(R.id.tvPerguntaDialog);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        TextView btnExcluir = dialogView.findViewById(R.id.btnExcluir);

        // Definir a pergunta
        tvPerguntaDialog.setText(flashcard.getPergunta());

        // Configurar botão cancelar
        btnCancelar.setOnClickListener(v -> {
            // Animação de clique
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                        dialog.dismiss();
                    })
                    .start();
        });

        // Configurar botão excluir
        btnExcluir.setOnClickListener(v -> {
            // Animação de clique
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                        dialog.dismiss();
                        excluirFlashcard(position);
                    })
                    .start();
        });

        // Animação de entrada do diálogo
        dialog.setOnShowListener(dialogInterface -> {
            dialogView.setAlpha(0f);
            dialogView.setScaleX(0.8f);
            dialogView.setScaleY(0.8f);
            dialogView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start();
        });

        dialog.show();
    }

    private void excluirFlashcard(int position) {
        Flashcard flashcard = lista.get(position);
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();

        Log.d("FlashcardActivity", "Tentando excluir flashcard ID: " + flashcard.getBaralhoId());

        // Chamar a API para excluir o flashcard do servidor
        Call<Void> call = RetrofitClient.getApiService().excluirFlashcard(flashcard.getBaralhoId(), token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FlashcardActivity", "Flashcard excluído com sucesso no servidor");

                    // Remover da lista local apenas após confirmação do servidor
                    lista.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();

                    // Toast de sucesso
                    Toast.makeText(FlashcardActivity.this, "🗑️ Flashcard excluído com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("FlashcardActivity", "Erro ao excluir flashcard. Código: " + response.code());
                    try {
                        String erro = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
                        Log.e("FlashcardActivity", "Erro detalhado: " + erro);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(FlashcardActivity.this, "❌ Erro ao excluir flashcard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FlashcardActivity", "Falha na requisição de exclusão: " + t.getMessage(), t);
                Toast.makeText(FlashcardActivity.this, "❌ Falha na conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para voltar
    public void voltarParaDashboard() {
        Intent intent = new Intent(FlashcardActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    // Override do botão voltar do sistema
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        voltarParaDashboard();
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

        // Atualizar título com a contagem
        String titleText = count == 0 ? "Seus Flashcards" :
                count == 1 ? "Seus Flashcards (1)" :
                        "Seus Flashcards (" + count + ")";
        tvBaralhoNome.setText(titleText);

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