package com.example.volans_app;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.adapter.AtividadeBaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.LoadingDialog;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AtividadeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "AtividadeActivity";
    private static final int PICK_PROFILE_IMAGE_REQUEST = 2;

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnHamburger;
    private RecyclerView rvBaralhos;
    private BottomNavigationView bottomNavigationView;

    // Drawer profile views
    private TextView tvNomeUsuarioDrawer;
    private ImageView profileImage;
    private CardView profileImageContainer;

    // Data
    private AtividadeBaralhoAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;

    // Loading dialog
    private LoadingDialog loadingDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AtividadeActivity onCreate");

        setupStatusBar();
        setContentView(R.layout.activity_atividade);

        // Inicializar loading dialog
        loadingDialog = new LoadingDialog(this);

        // Inicializar views
        initializeViews();
        setupDrawer();
        setupProfileImage();
        setupBottomNavigation();
        setupUserData();

        // Verificar token
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.equals("Bearer ")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configurar RecyclerView e carregar dados
        setupRecyclerView();
        listarBaralhos();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnHamburger = findViewById(R.id.btnHamburger);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Inicializar views do header do drawer
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            tvNomeUsuarioDrawer = headerView.findViewById(R.id.tvNomeUsuarioDrawer);
            profileImage = headerView.findViewById(R.id.profileImage);
            profileImageContainer = headerView.findViewById(R.id.profileImageContainer);
        }

        Log.d(TAG, "Views inicializadas - BottomNav: " + (bottomNavigationView != null));
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        if (btnHamburger != null) {
            btnHamburger.setOnClickListener(v -> {
                animateButton(btnHamburger);

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        // NAVEGAÇÃO CORRETA DO DRAWER
        if (id == R.id.nav_dashboard) {
            navigateToActivity(DashboardActivity.class);
            return true;
        } else if (id == R.id.nav_baralhos) {
            navigateToActivity(BaralhoActivity.class);
            return true;
        } else if (id == R.id.nav_atividades) {
            return true; // Já está na AtividadeActivity
        } else if (id == R.id.nav_tarefas) {
            navigateToActivity(TarefaActivity.class);
            return true;
        } else if (id == R.id.nav_termos) {
            showTermsOfUse();
            return true;
        } else if (id == R.id.nav_sair) {
            logout();
            return true;
        }

        return false;
    }

    private void setupProfileImage() {
        if (profileImageContainer != null) {
            profileImageContainer.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem de perfil"), PICK_PROFILE_IMAGE_REQUEST);
            });
        }
        loadProfileImage();
    }

    private void setupUserData() {
        String nomeUsuario = SharedPrefManager.getInstance(this).getNomeUsuario();

        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = "Usuário";
        }

        // Atualizar nome no header do drawer
        if (tvNomeUsuarioDrawer != null) {
            tvNomeUsuarioDrawer.setText(nomeUsuario);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = processProfileImage(imageUri);
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                    saveProfileImage(bitmap);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private Bitmap processProfileImage(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);

            if (originalBitmap == null) {
                return null;
            }

            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 500);
            return getCircularBitmap(resizedBitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / (float) height;

        int newWidth, newHeight;
        if (width > height) {
            newWidth = maxSize;
            newHeight = (int) (maxSize / ratio);
        } else {
            newHeight = maxSize;
            newWidth = (int) (maxSize * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        int left = (width - size) / 2;
        int top = (height - size) / 2;

        canvas.drawBitmap(bitmap, new Rect(left, top, left + size, top + size),
                new Rect(0, 0, size, size), paint);

        return output;
    }

    private void saveProfileImage(Bitmap bitmap) {
        try {
            String filename = "profile_image.png";
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    openFileOutput(filename, MODE_PRIVATE));
            SharedPrefManager.getInstance(this).saveProfileImagePath(filename);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage() {
        String imagePath = SharedPrefManager.getInstance(this).getProfileImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(openFileInput(imagePath));
                if (bitmap != null && profileImage != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "BottomNavigationView não encontrada!");
            return;
        }

        // Selecionar "Atividades" como ativo
        bottomNavigationView.setSelectedItemId(R.id.nav_atividade);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Log.d(TAG, "Bottom nav item clicado: " + getResourceName(itemId));

                animateBottomNavigationIcon(bottomNavigationView.findViewById(itemId));

                // NAVEGAÇÃO CORRETA - CADA ID VAI PARA SUA ACTIVITY ESPECÍFICA
                if (itemId == R.id.nav_dashboard) {
                    Log.d(TAG, "Navegando para DashboardActivity");
                    navigateToActivity(DashboardActivity.class);
                    return true;
                } else if (itemId == R.id.nav_baralhos) {
                    Log.d(TAG, "Navegando para BaralhoActivity");
                    navigateToActivity(BaralhoActivity.class);
                    return true;
                } else if (itemId == R.id.nav_atividade) {
                    return true; // Já está na AtividadeActivity
                } else if (itemId == R.id.nav_tarefas) {
                    Log.d(TAG, "Navegando para TarefaActivity");
                    navigateToActivity(TarefaActivity.class);
                    return true;
                }

                return false;
            }
        });
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        // REMOVIDO: overridePendingTransition - sem efeitos de transição
        Log.d(TAG, "Navegando para: " + activityClass.getSimpleName());
    }

    private String getResourceName(int resourceId) {
        try {
            return getResources().getResourceEntryName(resourceId);
        } catch (Exception e) {
            return "unknown_" + resourceId;
        }
    }

    private void animateBottomNavigationIcon(View view) {
        if (view != null) {
            ValueAnimator animator = ValueAnimator.ofFloat(0.8f, 1.2f, 1.0f);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
            });
            animator.start();
        }
    }

    private void animateButton(View button) {
        button.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
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
        // Mostrar diálogo de loading customizado
        loadingDialog.show("Gerando novo quiz...");

        // Simular diferentes etapas do carregamento com mensagens dinâmicas
        handler.postDelayed(() -> {
            if (loadingDialog.isShowing()) {
                loadingDialog.updateMessage("Analisando flashcards...");
            }
        }, 800);

        handler.postDelayed(() -> {
            if (loadingDialog.isShowing()) {
                loadingDialog.updateMessage("Criando perguntas personalizadas...");
            }
        }, 1600);

        handler.postDelayed(() -> {
            if (loadingDialog.isShowing()) {
                loadingDialog.updateMessage("Finalizando preparação...");
            }
        }, 2400);

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<QuestaoQuiz>> call = api.gerarQuiz(token, baralhoId);
        call.enqueue(new Callback<List<QuestaoQuiz>>() {
            @Override
            public void onResponse(Call<List<QuestaoQuiz>> call, Response<List<QuestaoQuiz>> response) {
                // Remover todos os handlers pendentes
                handler.removeCallbacksAndMessages(null);

                // Fechar diálogo de loading
                loadingDialog.dismiss();

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
                    // REMOVIDO: overridePendingTransition
                } else {
                    Log.e("QUIZ_API", "Erro ao gerar quiz. Código: " + response.code());
                    Toast.makeText(AtividadeActivity.this,
                            "Erro ao gerar quiz: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuestaoQuiz>> call, Throwable t) {
                // Remover todos os handlers pendentes
                handler.removeCallbacksAndMessages(null);

                // Fechar diálogo de loading
                loadingDialog.dismiss();

                Log.e("QUIZ_API", "Falha ao gerar quiz: " + t.getMessage(), t);
                Toast.makeText(AtividadeActivity.this,
                        "Falha na conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTermsOfUse() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_terms_of_use, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        builder.setPositiveButton("Aceitar", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Fechar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#5B4CF5"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(16);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#6B7280"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(16);
        }
    }

    private void logout() {
        SharedPrefManager.getInstance(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_atividade);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar handlers para evitar memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Fechar diálogo se ainda estiver aberto
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
