package com.example.volans_app;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.adapter.BaralhoGridAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.ImageUploadManager;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaralhoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText etNome, etDescricao;
    private MaterialButton btnCriarBaralho;
    private RecyclerView rvBaralhos;
    private TextView tvBaralhoCount;
    private FloatingActionButton fabAddBaralho;
    private BaralhoGridAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;
    private BottomNavigationView bottomNavigationView;

    // Para controlar qual baralho está sendo editado
    private Baralho baralhoSendoEditado;
    private int posicaoBaralhoEditado;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Status bar transparente para design premium
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_baralho);

        // Inicializar views
        initializeViews();

        // Configurar bottom navigation moderna
        setupBottomNavigation();

        // Verificação do token
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.equals("Bearer ")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configuração do RecyclerView em Grid
        setupRecyclerView();

        // Configuração dos listeners
        setupButtons();

        // Carregar baralhos
        listarBaralhos();
    }

    private void initializeViews() {
        etNome = findViewById(R.id.etNomeBaralho);
        etDescricao = findViewById(R.id.etDescricaoBaralho);
        btnCriarBaralho = findViewById(R.id.btnCriarBaralho);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        tvBaralhoCount = findViewById(R.id.tvBaralhoCount);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddBaralho = findViewById(R.id.fabAddBaralho);
    }

    private void setupButtons() {
        btnCriarBaralho.setOnClickListener(v -> {
            animateButton(btnCriarBaralho);
            criarBaralho();
        });

        fabAddBaralho.setOnClickListener(v -> {
            animateFab();
            scrollToCreateForm();
        });

        setupEditTextAnimations();
    }

    private void animateFab() {
        fabAddBaralho.animate()
                .rotation(45f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    fabAddBaralho.animate()
                            .rotation(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void scrollToCreateForm() {
        View createCard = findViewById(R.id.createCard);
        if (createCard != null) {
            createCard.requestFocus();
            etNome.requestFocus();
        }
    }

    private void setupEditTextAnimations() {
        etNome.setOnFocusChangeListener((v, hasFocus) -> {
            animateEditTextFocus(v, hasFocus);
        });

        etDescricao.setOnFocusChangeListener((v, hasFocus) -> {
            animateEditTextFocus(v, hasFocus);
        });
    }

    private void animateEditTextFocus(View editText, boolean hasFocus) {
        View parent = (View) editText.getParent();
        ValueAnimator animator = ValueAnimator.ofFloat(
                hasFocus ? 1.0f : 1.02f,
                hasFocus ? 1.02f : 1.0f
        );
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            parent.setScaleX(value);
            parent.setScaleY(value);
        });
        animator.start();
    }

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

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_baralhos);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                animateBottomNavigationIcon(bottomNavigationView.findViewById(itemId));

                if (itemId == R.id.nav_baralhos) {
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(BaralhoActivity.this, DashboardActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_atividade) {
                    startActivity(new Intent(BaralhoActivity.this, AtividadeActivity.class));
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
            ValueAnimator scaleAnimator = ValueAnimator.ofFloat(0.8f, 1.2f, 1.0f);
            scaleAnimator.setDuration(400);
            scaleAnimator.setInterpolator(new DecelerateInterpolator());
            scaleAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
            });
            scaleAnimator.start();
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvBaralhos.setLayoutManager(gridLayoutManager);

        adapter = new BaralhoGridAdapter(this, listaBaralhos,
                // Clique no baralho (abre flashcards)
                baralho -> {
                    animateItemClick(() -> {
                        Intent intent = new Intent(BaralhoActivity.this, FlashcardActivity.class);
                        intent.putExtra("baralhoId", baralho.getId());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    });
                },
                // Clique na imagem (abre galeria)
                (baralho, position) -> {
                    baralhoSendoEditado = baralho;
                    posicaoBaralhoEditado = position;
                    abrirGaleria();
                }
        );

        rvBaralhos.setAdapter(adapter);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null && baralhoSendoEditado != null) {
                uploadImagemBaralho(imageUri, baralhoSendoEditado);
            }
        }
    }

    private void uploadImagemBaralho(Uri imageUri, Baralho baralho) {
        // Mostrar loading
        Toast.makeText(this, "Processando imagem...", Toast.LENGTH_SHORT).show();

        // Converter imagem para Base64 em thread separada
        new Thread(() -> {
            String imagemBase64 = ImageUploadManager.convertImageToBase64(this, imageUri);

            if (imagemBase64 != null) {
                // Voltar para a thread principal para fazer a requisição
                runOnUiThread(() -> {
                    enviarImagemParaServidor(imagemBase64, baralho);
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void enviarImagemParaServidor(String imagemBase64, Baralho baralho) {
        // Gerar nome único para o arquivo
        String userId = SharedPrefManager.getInstance(this).getNomeUsuario(); // ou getUserId() se existir
        String nomeArquivo = ImageUploadManager.generateImageFileName(userId, baralho.getId());

        // Criar request
        ApiService.ImageUploadRequest request = new ApiService.ImageUploadRequest(imagemBase64, nomeArquivo, "jpg");

        // Fazer upload
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.uploadImagemBaralho(baralho.getId(), request, token).enqueue(new Callback<ApiService.ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ApiService.ImageUploadResponse> call, Response<ApiService.ImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ImageUploadResponse uploadResponse = response.body();

                    if (uploadResponse.isSucesso()) {
                        // Atualizar o baralho localmente com a URL retornada
                        baralho.setImagemUrl(uploadResponse.getUrlImagem());

                        // Notificar o adapter
                        adapter.notifyItemChanged(posicaoBaralhoEditado);

                        Toast.makeText(BaralhoActivity.this, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
                        Log.d("UPLOAD", "Imagem salva: " + uploadResponse.getUrlImagem());
                    } else {
                        Toast.makeText(BaralhoActivity.this, "Erro: " + uploadResponse.getMensagem(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("UPLOAD", "Erro na resposta: " + response.code());
                    Toast.makeText(BaralhoActivity.this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ImageUploadResponse> call, Throwable t) {
                Log.e("UPLOAD", "Erro no upload", t);
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateItemClick(Runnable action) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.98f, 1.0f);
        animator.setDuration(150);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            rvBaralhos.setScaleX(value);
            rvBaralhos.setScaleY(value);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                action.run();
            }
        });
        animator.start();
    }

    private void listarBaralhos() {
        Log.d("BARALHO", "Iniciando listagem de baralhos...");
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        api.listarBaralhos(token).enqueue(new Callback<List<Baralho>>() {
            @Override
            public void onResponse(Call<List<Baralho>> call, Response<List<Baralho>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("BARALHO", "Baralhos recebidos: " + response.body().size());

                    listaBaralhos.clear();
                    listaBaralhos.addAll(response.body());

                    animateListUpdate();
                    adapter.notifyDataSetChanged();
                    updateBaralhoCount();

                    Log.d("BARALHO", listaBaralhos.size() + " baralhos carregados no adapter");
                } else {
                    Log.e("BARALHO", "Erro na resposta: " + response.code());
                    showErrorToast("Erro ao carregar baralhos");
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Log.e("BARALHO", "Falha na requisição: " + t.getMessage());
                showErrorToast("Falha na conexão");
            }
        });
    }

    private void animateListUpdate() {
        rvBaralhos.setAlpha(0f);
        rvBaralhos.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void updateBaralhoCount() {
        int count = listaBaralhos.size();
        String text = count == 1 ? "1 baralho criado" : count + " baralhos criados";
        animateCounterUpdate(tvBaralhoCount, text);
    }

    private void animateCounterUpdate(TextView textView, String newText) {
        textView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void criarBaralho() {
        String nome = etNome.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty()) {
            showErrorToast("Preencha todos os campos");
            shakeEmptyFields(nome.isEmpty(), descricao.isEmpty());
            return;
        }

        animateButtonLoading(true);

        Baralho novoBaralho = new Baralho(nome, descricao);
        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        api.criarBaralho(novoBaralho, token).enqueue(new Callback<Baralho>() {
            @Override
            public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                animateButtonLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    listaBaralhos.add(response.body());
                    adapter.notifyItemInserted(listaBaralhos.size() - 1);

                    clearFieldsWithAnimation();
                    updateBaralhoCount();

                    showSuccessToast("Baralho criado com sucesso!");
                } else {
                    tratarErroCriacao(response);
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                animateButtonLoading(false);
                showErrorToast("Falha na conexão");
            }
        });
    }

    private void shakeEmptyFields(boolean nomeEmpty, boolean descricaoEmpty) {
        if (nomeEmpty) {
            shakeView(etNome.getParent());
        }
        if (descricaoEmpty) {
            shakeView(etDescricao.getParent());
        }
    }

    private void shakeView(Object view) {
        if (view instanceof View) {
            View v = (View) view;
            v.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        v.animate()
                                .translationX(10f)
                                .setDuration(50)
                                .withEndAction(() -> {
                                    v.animate()
                                            .translationX(0f)
                                            .setDuration(50)
                                            .start();
                                })
                                .start();
                    })
                    .start();
        }
    }

    private void animateButtonLoading(boolean loading) {
        if (loading) {
            btnCriarBaralho.setText("Criando...");
            btnCriarBaralho.setEnabled(false);
            btnCriarBaralho.setAlpha(0.7f);
        } else {
            btnCriarBaralho.setText("Criar Baralho");
            btnCriarBaralho.setEnabled(true);
            btnCriarBaralho.setAlpha(1.0f);
        }
    }

    private void clearFieldsWithAnimation() {
        etNome.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    etNome.setText("");
                    etNome.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();

        etDescricao.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    etDescricao.setText("");
                    etDescricao.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void showSuccessToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void tratarErroCriacao(Response<Baralho> response) {
        try {
            String erro = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
            Log.e("BARALHO", "Erro ao criar baralho: " + erro);
            showErrorToast("Erro: " + erro);
        } catch (Exception e) {
            Log.e("BARALHO", "Erro ao ler mensagem de erro", e);
            showErrorToast("Erro ao criar baralho");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_baralhos);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DashboardActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
