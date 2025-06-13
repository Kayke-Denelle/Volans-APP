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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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
import com.example.volans_app.utils.ImageUploadManager;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
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
    private String selectedImageBase64 = null;

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

        // Verificar se deve abrir o diálogo de criação automaticamente
        if (getIntent().getBooleanExtra("openCreateDialog", false)) {
            Log.d(TAG, "Abrindo diálogo de criação automaticamente");
            // Pequeno delay para garantir que a UI esteja completamente carregada
            new android.os.Handler().postDelayed(() -> {
                showCreateBaralhoDialog();
            }, 300);
        }
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
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar views", e);
            Toast.makeText(this, "Erro ao inicializar componentes", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupProfileImage() {
        if (profileImageContainer != null) {
            profileImageContainer.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem de perfil"), PICK_PROFILE_IMAGE_REQUEST);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao abrir seletor de imagem", e);
                    Toast.makeText(this, "Erro ao abrir seletor de imagem", Toast.LENGTH_SHORT).show();
                }
            });
        }
        loadProfileImage();
    }

    private void setupDrawer() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar drawer", e);
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

        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar bottom navigation", e);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            Intent intent = new Intent(this, activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            // REMOVIDO: overridePendingTransition - sem efeitos de transição
            Log.d(TAG, "Navegando para: " + activityClass.getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao navegar para " + activityClass.getSimpleName(), e);
            Toast.makeText(this, "Erro ao navegar para a tela", Toast.LENGTH_SHORT).show();
        }
    }

    private String getResourceName(int resourceId) {
        try {
            return getResources().getResourceEntryName(resourceId);
        } catch (Exception e) {
            return "unknown_" + resourceId;
        }
    }

    private void setupButtons() {
        try {
            if (quickActionCreate != null) {
                quickActionCreate.setOnClickListener(v -> {
                    Log.d(TAG, "Botão 'Criar novo baralho' clicado");
                    animateQuickAction(quickActionCreate);
                    showCreateBaralhoDialog();
                });
            } else {
                Log.e(TAG, "quickActionCreate é nulo");
            }

            if (fabAddBaralho != null) {
                fabAddBaralho.setOnClickListener(v -> {
                    Log.d(TAG, "FAB 'Adicionar baralho' clicado");
                    animateFAB(fabAddBaralho);
                    showCreateBaralhoDialog();
                });
            } else {
                Log.e(TAG, "fabAddBaralho é nulo");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar botões", e);
        }
    }

    private void setupRecyclerView() {
        try {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            rvBaralhos.setLayoutManager(gridLayoutManager);

            adapter = new BaralhoAdapter(this, listaBaralhos, baralho -> {
                // Clique no baralho - navegar para flashcards
                try {
                    Log.d(TAG, "Baralho clicado: " + baralho.getNome());
                    Intent intent = new Intent(BaralhoActivity.this, FlashcardActivity.class);
                    intent.putExtra("baralhoId", baralho.getId());
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao abrir FlashcardActivity", e);
                    Toast.makeText(BaralhoActivity.this, "Erro ao abrir flashcards", Toast.LENGTH_SHORT).show();
                }
            });

            // Configurar listener para opções
            adapter.setOnBaralhoOptionsListener(new BaralhoAdapter.OnBaralhoOptionsListener() {
                @Override
                public void onEditBaralho(Baralho baralho) {
                    Log.d(TAG, "Opção editar clicada para baralho: " + baralho.getNome());
                    showEditBaralhoDialog(baralho);
                }

                @Override
                public void onDeleteBaralho(Baralho baralho) {
                    Log.d(TAG, "Opção excluir clicada para baralho: " + baralho.getNome());
                    showDeleteConfirmationDialog(baralho);
                }
            });

            rvBaralhos.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar RecyclerView", e);
        }
    }

    private void setupUserData() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar dados do usuário", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                // Seleção de imagem para baralho
                selectedImageUri = data.getData();
                Log.d(TAG, "URI da imagem selecionada: " + selectedImageUri.toString());

                // Converter a imagem para Base64 usando o ImageUploadManager
                selectedImageBase64 = ImageUploadManager.convertImageToBase64(this, selectedImageUri);

                if (selectedImageBase64 != null) {
                    Log.d(TAG, "Imagem convertida para Base64 com sucesso");

                    // Salvar a imagem no armazenamento interno
                    String userId = SharedPrefManager.getInstance(this).getUserId();
                    String tempId = "temp_" + System.currentTimeMillis();
                    selectedImagePath = ImageUploadManager.generateImageFileName(userId, tempId);

                    // Salvar a imagem no armazenamento interno
                    saveImageFromBase64(selectedImageBase64, selectedImagePath);

                    // Atualizar o preview
                    updateImagePreview();
                } else {
                    Log.e(TAG, "Falha ao converter imagem para Base64");
                    Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                }
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resultado da atividade", e);
            Toast.makeText(this, "Erro ao processar imagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageFromBase64(String base64Image, String fileName) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

            File file = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedString);
            fos.flush();
            fos.close();

            Log.d(TAG, "Imagem salva com sucesso: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");

            // Verificar se o arquivo foi salvo corretamente
            if (file.exists() && file.length() > 0) {
                // Tentar carregar a imagem para verificar se está corrompida
                Bitmap testBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (testBitmap != null) {
                    Log.d(TAG, "Imagem verificada e está OK: " + testBitmap.getWidth() + "x" + testBitmap.getHeight());
                    testBitmap.recycle();
                } else {
                    Log.e(TAG, "Imagem salva, mas não pode ser decodificada");
                }
            } else {
                Log.e(TAG, "Arquivo não foi salvo corretamente");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar imagem do Base64", e);
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
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar imagem de perfil", e);
        }
    }

    // Resto dos métodos de baralho permanecem iguais...
    private void showCreateBaralhoDialog() {
        try {
            Log.d(TAG, "Iniciando showCreateBaralhoDialog");
            baralhoEditando = null;
            selectedImagePath = null;
            selectedImageUri = null;
            selectedImageBase64 = null;
            showBaralhoDialog("Criar Novo Baralho", "Criar", null, null);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo de criação", e);
            Toast.makeText(this, "Erro ao abrir diálogo de criação", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditBaralhoDialog(Baralho baralho) {
        try {
            Log.d(TAG, "Iniciando showEditBaralhoDialog para: " + baralho.getNome());
            baralhoEditando = baralho;
            selectedImagePath = baralho.getImagemUrl();
            selectedImageUri = null;
            selectedImageBase64 = null;
            showBaralhoDialog("Editar Baralho", "Salvar", baralho.getNome(), baralho.getDescricao());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo de edição", e);
            Toast.makeText(this, "Erro ao abrir diálogo de edição", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBaralhoDialog(String titulo, String botaoTexto, String nomeInicial, String descricaoInicial) {
        try {
            Log.d(TAG, "Iniciando showBaralhoDialog: " + titulo);
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

            if (etNome != null) {
                etNome.setTextColor(Color.parseColor("#1F2937"));
                etNome.setHintTextColor(Color.parseColor("#6B7280"));
            }
            if (etDescricao != null) {
                etDescricao.setTextColor(Color.parseColor("#1F2937"));
                etDescricao.setHintTextColor(Color.parseColor("#6B7280"));
            }

            if (nomeInicial != null) etNome.setText(nomeInicial);
            if (descricaoInicial != null) etDescricao.setText(descricaoInicial);

            updateImagePreview();

            View.OnClickListener imageClickListener = v -> {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao abrir seletor de imagem", e);
                    Toast.makeText(this, "Erro ao abrir seletor de imagem", Toast.LENGTH_SHORT).show();
                }
            };

            cardImagePreview.setOnClickListener(imageClickListener);
            btnSelectImage.setOnClickListener(imageClickListener);

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            builder.setView(dialogView);
            builder.setPositiveButton(botaoTexto, null);
            builder.setNegativeButton("Cancelar", (dialog, which) -> {
                currentImagePreview = null;
                selectedImageUri = null;
                selectedImageBase64 = null;
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();

            // CORREÇÃO: Usar ColorDrawable em vez de resource que está causando erro
            if (dialog.getWindow() != null) {
                try {
                    // Tentar usar o drawable_background_gradient que está funcionando
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_gradient);
                    Log.d(TAG, "Usando dialog_background_gradient como background");
                } catch (Exception e) {
                    // Fallback para um ColorDrawable branco com cantos arredondados
                    Log.e(TAG, "Erro ao definir background do diálogo: " + e.getMessage());
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
            }

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
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
                    selectedImageBase64 = null;
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar criação/edição de baralho", e);
                    Toast.makeText(this, "Erro ao processar baralho", Toast.LENGTH_SHORT).show();
                }
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo de baralho", e);
            Toast.makeText(this, "Erro ao abrir diálogo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(Baralho baralho) {
        try {
            Log.d(TAG, "Iniciando showDeleteConfirmationDialog para: " + baralho.getNome());
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

            // CORREÇÃO: Usar ColorDrawable em vez de resource que está causando erro
            if (dialog.getWindow() != null) {
                try {
                    // Tentar usar o drawable_background_gradient que está funcionando
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_gradient);
                    Log.d(TAG, "Usando dialog_background_gradient como background");
                } catch (Exception e) {
                    // Fallback para um ColorDrawable branco com cantos arredondados
                    Log.e(TAG, "Erro ao definir background do diálogo: " + e.getMessage());
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo de exclusão", e);
            Toast.makeText(this, "Erro ao abrir diálogo de exclusão", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImagePreview() {
        if (currentImagePreview == null) {
            Log.d(TAG, "currentImagePreview é nulo");
            return;
        }

        try {
            // Primeiro, tentar carregar da URI selecionada
            if (selectedImageUri != null) {
                Log.d(TAG, "Carregando preview da URI: " + selectedImageUri);
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    if (bitmap != null) {
                        currentImagePreview.setImageBitmap(bitmap);
                        currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        currentImagePreview.setPadding(0, 0, 0, 0);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao carregar preview da URI", e);
                }
            }

            // Se não conseguiu carregar da URI, tentar carregar do Base64
            if (selectedImageBase64 != null) {
                Log.d(TAG, "Carregando preview do Base64");
                try {
                    byte[] decodedString = Base64.decode(selectedImageBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    if (bitmap != null) {
                        currentImagePreview.setImageBitmap(bitmap);
                        currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        currentImagePreview.setPadding(0, 0, 0, 0);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao carregar preview do Base64", e);
                }
            }

            // Se não conseguiu carregar do Base64, tentar carregar do arquivo salvo
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                Log.d(TAG, "Carregando preview do arquivo: " + selectedImagePath);
                File imageFile = new File(getFilesDir(), selectedImagePath);

                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        currentImagePreview.setImageBitmap(bitmap);
                        currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        currentImagePreview.setPadding(0, 0, 0, 0);
                        return;
                    } else {
                        Log.e(TAG, "Falha ao decodificar bitmap do arquivo");
                    }
                } else {
                    Log.e(TAG, "Arquivo de imagem não existe: " + imageFile.getAbsolutePath());
                }
            }

            // Fallback para ícone padrão
            Log.d(TAG, "Usando ícone padrão para preview");
            currentImagePreview.setImageResource(R.drawable.ic_photo);
            currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            currentImagePreview.setPadding(16, 16, 16, 16);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar preview da imagem", e);
            currentImagePreview.setImageResource(R.drawable.ic_photo);
            currentImagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            currentImagePreview.setPadding(16, 16, 16, 16);
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        try {
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

            Log.d(TAG, "Redimensionando de " + width + "x" + height + " para " + newWidth + "x" + newHeight);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao redimensionar bitmap", e);
            return bitmap;
        }
    }

    private void criarBaralho(String nome, String descricao) {
        try {
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
                        Baralho baralhoRetornado = response.body();
                        Log.d(TAG, "Baralho criado com sucesso. ID: " + baralhoRetornado.getId() + ", Imagem: " + baralhoRetornado.getImagemUrl());

                        // Se o baralho foi criado com sucesso e temos uma imagem, renomear o arquivo
                        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                            String userId = SharedPrefManager.getInstance(BaralhoActivity.this).getUserId();
                            String newImagePath = ImageUploadManager.generateImageFileName(userId, baralhoRetornado.getId());

                            // Renomear o arquivo
                            File oldFile = new File(getFilesDir(), selectedImagePath);
                            File newFile = new File(getFilesDir(), newImagePath);

                            if (oldFile.exists()) {
                                boolean renamed = oldFile.renameTo(newFile);
                                Log.d(TAG, "Arquivo renomeado: " + renamed + " para " + newImagePath);

                                if (renamed) {
                                    // Atualizar o caminho da imagem no baralho
                                    baralhoRetornado.setImagemUrl(newImagePath);

                                    // Atualizar o baralho no servidor
                                    apiService.editarBaralho(baralhoRetornado.getId(), baralhoRetornado, token).enqueue(new Callback<Baralho>() {
                                        @Override
                                        public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                                            if (response.isSuccessful()) {
                                                Log.d(TAG, "Caminho da imagem atualizado com sucesso");
                                            } else {
                                                Log.e(TAG, "Erro ao atualizar caminho da imagem: " + response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Baralho> call, Throwable t) {
                                            Log.e(TAG, "Falha ao atualizar caminho da imagem", t);
                                        }
                                    });
                                }
                            }
                        }

                        listaBaralhos.add(baralhoRetornado);
                        adapter.notifyItemInserted(listaBaralhos.size() - 1);
                        updateBaralhoCount();
                        Toast.makeText(BaralhoActivity.this, "Baralho criado com sucesso!", Toast.LENGTH_SHORT).show();

                        // Limpar variáveis de imagem
                        selectedImagePath = null;
                        selectedImageUri = null;
                        selectedImageBase64 = null;
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar baralho", e);
            Toast.makeText(this, "Erro ao criar baralho", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarBaralho(Baralho baralho, String nome, String descricao) {
        try {
            baralho.setNome(nome);
            baralho.setDescricao(descricao);

            // Se selecionou uma nova imagem
            if (selectedImageBase64 != null) {
                // Gerar um novo nome de arquivo
                String userId = SharedPrefManager.getInstance(this).getUserId();
                String newImagePath = ImageUploadManager.generateImageFileName(userId, baralho.getId());

                // Salvar a nova imagem
                saveImageFromBase64(selectedImageBase64, newImagePath);

                // Excluir a imagem antiga se existir
                if (baralho.getImagemUrl() != null && !baralho.getImagemUrl().isEmpty()) {
                    File oldFile = new File(getFilesDir(), baralho.getImagemUrl());
                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        Log.d(TAG, "Imagem antiga excluída: " + deleted);
                    }
                }

                // Atualizar o caminho da imagem
                baralho.setImagemUrl(newImagePath);
                Log.d(TAG, "Editando baralho com nova imagem: " + newImagePath);
            }

            apiService.editarBaralho(baralho.getId(), baralho, token).enqueue(new Callback<Baralho>() {
                @Override
                public void onResponse(Call<Baralho> call, Response<Baralho> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Baralho editado com sucesso");
                        adapter.notifyDataSetChanged();
                        Toast.makeText(BaralhoActivity.this, "Baralho editado com sucesso!", Toast.LENGTH_SHORT).show();

                        // Limpar variáveis de imagem
                        selectedImagePath = null;
                        selectedImageUri = null;
                        selectedImageBase64 = null;
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao editar baralho", e);
            Toast.makeText(this, "Erro ao editar baralho", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirBaralho(Baralho baralho) {
        try {
            apiService.excluirBaralho(baralho.getId(), token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        int position = listaBaralhos.indexOf(baralho);
                        if (position != -1) {
                            // Excluir arquivo de imagem se existir
                            if (baralho.getImagemUrl() != null && !baralho.getImagemUrl().isEmpty()) {
                                try {
                                    File imageFile = new File(getFilesDir(), baralho.getImagemUrl());
                                    if (imageFile.exists()) {
                                        boolean deleted = imageFile.delete();
                                        Log.d(TAG, "Arquivo de imagem excluído: " + deleted);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao excluir arquivo de imagem", e);
                                }
                            }

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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir baralho", e);
            Toast.makeText(this, "Erro ao excluir baralho", Toast.LENGTH_SHORT).show();
        }
    }

    private void listarBaralhos() {
        try {
            apiService.listarBaralhos(token).enqueue(new Callback<List<Baralho>>() {
                @Override
                public void onResponse(Call<List<Baralho>> call, Response<List<Baralho>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listaBaralhos.clear();
                        listaBaralhos.addAll(response.body());
                        adapter.notifyDataSetChanged();
                        updateBaralhoCount();

                        // Log das imagens dos baralhos
                        for (Baralho baralho : listaBaralhos) {
                            Log.d(TAG, "Baralho: " + baralho.getNome() + ", Imagem: " + baralho.getImagemUrl());
                        }
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao listar baralhos", e);
            Toast.makeText(this, "Erro ao carregar baralhos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBaralhoCount() {
        try {
            int count = listaBaralhos.size();
            String text = count == 1 ? "1 baralho criado" : count + " baralhos criados";
            animateCounterUpdate(tvBaralhoCount, text);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar contador de baralhos", e);
        }
    }

    private void showTermsOfUse() {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_terms_of_use, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            builder.setView(dialogView);
            builder.setPositiveButton("Aceitar", (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton("Fechar", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();

            // CORREÇÃO: Usar ColorDrawable em vez de resource que está causando erro
            if (dialog.getWindow() != null) {
                try {
                    // Tentar usar o drawable_background_gradient que está funcionando
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_gradient);
                    Log.d(TAG, "Usando dialog_background_gradient como background");
                } catch (Exception e) {
                    // Fallback para um ColorDrawable branco com cantos arredondados
                    Log.e(TAG, "Erro ao definir background do diálogo: " + e.getMessage());
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar termos de uso", e);
            Toast.makeText(this, "Erro ao abrir termos de uso", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        try {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer logout", e);
            Toast.makeText(this, "Erro ao fazer logout", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos de animação
    private void animateButton(View button) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro na animação do botão", e);
        }
    }

    private void animateQuickAction(View action) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro na animação da ação rápida", e);
        }
    }

    private void animateFAB(View fab) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro na animação do FAB", e);
        }
    }

    private void animateBottomNavigationIcon(View view) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro na animação do ícone da navegação inferior", e);
        }
    }

    private void animateCounterUpdate(TextView textView, String newText) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Erro na animação de atualização do contador", e);
            if (textView != null) {
                textView.setText(newText);
            }
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
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_baralhos);
        }
    }
}
