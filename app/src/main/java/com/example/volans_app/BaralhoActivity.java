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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.adapter.BaralhoAdapter;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaralhoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaralhoActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 2;

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnHamburger;
    private TextView tvBaralhoCount;
    private RecyclerView rvBaralhos;
    private CardView quickActionCreate;
    private FloatingActionButton fabAddBaralho;
    private BottomNavigationView bottomNavigationView;

    // Drawer profile views
    private TextView tvNomeUsuarioDrawer;
    private ImageView profileImage;
    private CardView profileImageContainer;

    // Data
    private BaralhoAdapter adapter;
    private List<Baralho> listaBaralhos = new ArrayList<>();
    private String token;
    private ApiService apiService;

    // Para edição de baralho
    private Baralho baralhoEditando = null;
    private String selectedImagePath = null;
    private ImageView currentImagePreview = null;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BaralhoActivity onCreate");

        setupStatusBar();
        setContentView(R.layout.activity_baralho);

        initializeViews();
        setupDrawer();
        setupProfileImage();
        setupBottomNavigation();
        setupButtons();
        setupRecyclerView();
        setupUserData();
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
        tvBaralhoCount = findViewById(R.id.tvBaralhoCount);
        rvBaralhos = findViewById(R.id.rvBaralhos);
        quickActionCreate = findViewById(R.id.quickActionCreate);
        fabAddBaralho = findViewById(R.id.fabAddBaralho);
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
            return true; // Já está na BaralhoActivity
        } else if (id == R.id.nav_atividades) {
            navigateToActivity(AtividadeActivity.class);
            return true;
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

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "BottomNavigationView não encontrada!");
            return;
        }

        // Selecionar "Baralhos" como ativo
        bottomNavigationView.setSelectedItemId(R.id.nav_baralhos);

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
                    return true; // Já está na BaralhoActivity
                } else if (itemId == R.id.nav_atividade) {
                    Log.d(TAG, "Navegando para AtividadeActivity");
                    navigateToActivity(AtividadeActivity.class);
                    return true;
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

    private void setupButtons() {
        if (quickActionCreate != null) {
            quickActionCreate.setOnClickListener(v -> {
                animateQuickAction(quickActionCreate);
                showCreateBaralhoDialog();
            });
        }

        if (fabAddBaralho != null) {
            fabAddBaralho.setOnClickListener(v -> {
                animateFAB(fabAddBaralho);
                showCreateBaralhoDialog();
            });
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvBaralhos.setLayoutManager(gridLayoutManager);

        adapter = new BaralhoAdapter(this, listaBaralhos, baralho -> {
            // Clique no baralho - navegar para flashcards
            Intent intent = new Intent(BaralhoActivity.this, FlashcardActivity.class);
            intent.putExtra("baralhoId", baralho.getId());
            startActivity(intent);
            // REMOVIDO: overridePendingTransition
        });

        // Configurar listener para opções
        adapter.setOnBaralhoOptionsListener(new BaralhoAdapter.OnBaralhoOptionsListener() {
            @Override
            public void onEditBaralho(Baralho baralho) {
                showEditBaralhoDialog(baralho);
            }

            @Override
            public void onDeleteBaralho(Baralho baralho) {
                showDeleteConfirmationDialog(baralho);
            }
        });

        rvBaralhos.setAdapter(adapter);
    }

    private void setupUserData() {
        token = "Bearer " + SharedPrefManager.getInstance(this).getToken();

        if (token.equals("Bearer ")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String nomeUsuario = SharedPrefManager.getInstance(this).getNomeUsuario();

        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = "Usuário";
        }

        // Atualizar nome no header do drawer
        if (tvNomeUsuarioDrawer != null) {
            tvNomeUsuarioDrawer.setText(nomeUsuario);
        }

        apiService = RetrofitClient.getApiService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Seleção de imagem para baralho
            selectedImageUri = data.getData();
            Log.d(TAG, "URI da imagem selecionada: " + selectedImageUri.toString());

            updateImagePreview();
            processAndSaveImage();
        } else if (requestCode == PICK_PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Seleção de imagem de perfil
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

    // Resto dos métodos de baralho permanecem iguais...
    private void showCreateBaralhoDialog() {
        baralhoEditando = null;
        selectedImagePath = null;
        selectedImageUri = null;
        showBaralhoDialog("Criar Novo Baralho", "Criar", null, null);
    }

    private void showEditBaralhoDialog(Baralho baralho) {
        baralhoEditando = baralho;
        selectedImagePath = baralho.getImagemUrl();
        selectedImageUri = null;
        showBaralhoDialog("Editar Baralho", "Salvar", baralho.getNome(), baralho.getDescricao());
    }

    private void showBaralhoDialog(String titulo, String botaoTexto, String nomeInicial, String descricaoInicial) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_criar_baralho, null);

        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialog);
        TextInputEditText etNome = dialogView.findViewById(R.id.etNomeBaralhoDialog);
        TextInputEditText etDescricao = dialogView.findViewById(R.id.etDescricaoBaralhoDialog);
        CardView cardImagePreview = dialogView.findViewById(R.id.cardImagePreview);
        ImageView ivImagePreview = dialogView.findViewById(R.id.ivImagePreview);
        CardView btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        tvTitulo.setText(titulo);
        currentImagePreview = ivImagePreview;

        if (nomeInicial != null) etNome.setText(nomeInicial);
        if (descricaoInicial != null) etDescricao.setText(descricaoInicial);

        updateImagePreview();

        View.OnClickListener imageClickListener = v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST);
        };

        cardImagePreview.setOnClickListener(imageClickListener);
        btnSelectImage.setOnClickListener(imageClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        builder.setPositiveButton(botaoTexto, null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            currentImagePreview = null;
            selectedImageUri = null;
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String descricao = etDescricao.getText().toString().trim();

            if (nome.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (baralhoEditando == null) {
                criarBaralho(nome, descricao);
            } else {
                editarBaralho(baralhoEditando, nome, descricao);
            }
            currentImagePreview = null;
            selectedImageUri = null;
            dialog.dismiss();
        });

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

    private void showDeleteConfirmationDialog(Baralho baralho) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmar_exclusao, null);

        TextView tvNomeBaralho = dialogView.findViewById(R.id.tvNomeBaralhoExcluir);
        tvNomeBaralho.setText(baralho.getNome());

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        builder.setPositiveButton("Excluir", (dialog, which) -> {
            excluirBaralho(baralho);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#EF4444"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(16);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#6B7280"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(16);
        }
    }

    private void processAndSaveImage() {
        if (selectedImageUri == null) return;

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) {
                Log.e(TAG, "InputStream é nulo");
                Toast.makeText(this, "Erro ao acessar a imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "Falha ao decodificar bitmap");
                Toast.makeText(this, "Erro ao processar a imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Bitmap original: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

            Bitmap processedBitmap = resizeBitmap(originalBitmap, 500);
            selectedImagePath = saveBaralhoImage(processedBitmap);

            if (selectedImagePath != null) {
                Log.d(TAG, "Imagem processada e salva: " + selectedImagePath);
                Toast.makeText(this, "Imagem selecionada com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Falha ao salvar imagem processada");
                Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            Toast.makeText(this, "Erro ao processar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImagePreview() {
        if (currentImagePreview == null) {
            Log.d(TAG, "currentImagePreview é nulo");
            return;
        }

        if (selectedImageUri != null) {
            Log.d(TAG, "Carregando preview da URI: " + selectedImageUri);
            try {
                currentImagePreview.setImageURI(selectedImageUri);
                currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar URI para preview", e);
            }
        }

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            try {
                Log.d(TAG, "Carregando preview do arquivo: " + selectedImagePath);
                File imageFile = new File(getFilesDir(), selectedImagePath);

                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        currentImagePreview.setImageBitmap(bitmap);
                        currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar arquivo para preview", e);
            }
        }

        Log.d(TAG, "Usando ícone padrão para preview");
        currentImagePreview.setImageResource(R.drawable.ic_photo);
        currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

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

    private String saveBaralhoImage(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap é nulo");
            return null;
        }

        String filename = "baralho_" + System.currentTimeMillis() + ".png";
        File file = new File(getFilesDir(), filename);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();

            if (success && file.exists() && file.length() > 0) {
                Log.d(TAG, "Imagem salva: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");
                return filename;
            } else {
                Log.e(TAG, "Falha ao salvar imagem");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar imagem", e);
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao fechar FileOutputStream", e);
                }
            }
        }
    }

    private void criarBaralho(String nome, String descricao) {
        Baralho novoBaralho = new Baralho(nome, descricao);
        if (selectedImagePath != null) {
            novoBaralho.setImagemUrl(selectedImagePath);
            Log.d(TAG, "Criando baralho com imagem: " + selectedImagePath);
        } else {
            Log.d(TAG, "Criando baralho sem imagem");
        }

        apiService.criarBaralho(novoBaralho, token).enqueue(new Callback<Baralho>() {
            @Override
            public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaBaralhos.add(response.body());
                    adapter.notifyItemInserted(listaBaralhos.size() - 1);
                    updateBaralhoCount();
                    Toast.makeText(BaralhoActivity.this, "Baralho criado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BaralhoActivity.this, "Erro ao criar baralho", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao criar baralho: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha na conexão", t);
            }
        });
    }

    private void editarBaralho(Baralho baralho, String nome, String descricao) {
        baralho.setNome(nome);
        baralho.setDescricao(descricao);
        if (selectedImagePath != null) {
            baralho.setImagemUrl(selectedImagePath);
            Log.d(TAG, "Editando baralho com imagem: " + selectedImagePath);
        }

        apiService.editarBaralho(baralho.getId(), baralho, token).enqueue(new Callback<Baralho>() {
            @Override
            public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                if (response.isSuccessful()) {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(BaralhoActivity.this, "Baralho editado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BaralhoActivity.this, "Erro ao editar baralho", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao editar baralho: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Baralho> call, Throwable t) {
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha na conexão", t);
            }
        });
    }

    private void excluirBaralho(Baralho baralho) {
        apiService.excluirBaralho(baralho.getId(), token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int position = listaBaralhos.indexOf(baralho);
                    if (position != -1) {
                        listaBaralhos.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateBaralhoCount();
                        Toast.makeText(BaralhoActivity.this, "Baralho excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BaralhoActivity.this, "Erro ao excluir baralho", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao excluir baralho: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha na conexão", t);
            }
        });
    }

    private void listarBaralhos() {
        apiService.listarBaralhos(token).enqueue(new Callback<List<Baralho>>() {
            @Override
            public void onResponse(Call<List<Baralho>> call, Response<List<Baralho>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaBaralhos.clear();
                    listaBaralhos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateBaralhoCount();
                } else {
                    Toast.makeText(BaralhoActivity.this, "Erro ao carregar baralhos", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao carregar baralhos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Baralho>> call, Throwable t) {
                Toast.makeText(BaralhoActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha na conexão", t);
            }
        });
    }

    private void updateBaralhoCount() {
        int count = listaBaralhos.size();
        String text = count == 1 ? "1 baralho criado" : count + " baralhos criados";
        animateCounterUpdate(tvBaralhoCount, text);
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

    // Métodos de animação
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

    private void animateQuickAction(View action) {
        action.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(150)
                .withEndAction(() -> {
                    action.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateFAB(View fab) {
        fab.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(() -> {
                    fab.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                })
                .start();
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_baralhos);
        }
    }
}
