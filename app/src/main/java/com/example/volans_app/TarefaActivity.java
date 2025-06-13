package com.example.volans_app;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
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

import com.example.volans_app.DTO.Tarefa;
import com.example.volans_app.adapter.TarefaAdapter;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TarefaActivity extends AppCompatActivity implements
        TarefaAdapter.OnTarefaClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TarefaActivity";
    private static final int PICK_PROFILE_IMAGE_REQUEST = 2;

    private RecyclerView recyclerViewTarefas;
    private TarefaAdapter tarefaAdapter;
    private List<Tarefa> listaTarefas;
    private FloatingActionButton fabAddTarefa;
    private TextView tvTarefaCount, tvTarefasPendentes, tvTarefasConcluidas;
    private LinearLayout layoutEmptyState;

    // Navigation
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnHamburger;
    private BottomNavigationView bottomNavigationView;

    // Drawer profile views
    private TextView tvNomeUsuarioDrawer;
    private ImageView profileImage;
    private CardView profileImageContainer;

    // Para edição
    private Tarefa tarefaEditando = null;
    private int posicaoEditando = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TarefaActivity onCreate");

        setupStatusBar();
        setContentView(R.layout.activity_tarefa);

        initViews();
        setupDrawer();
        setupProfileImage();
        setupBottomNavigation();
        setupRecyclerView();
        setupFab();
        setupQuickAction();
        setupUserData();
        loadTarefas();
        updateCounters();
    }

    private void setupStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void initViews() {
        recyclerViewTarefas = findViewById(R.id.recyclerViewTarefas);
        fabAddTarefa = findViewById(R.id.fabAddTarefa);
        tvTarefaCount = findViewById(R.id.tvTarefaCount);
        tvTarefasPendentes = findViewById(R.id.tvTarefasPendentes);
        tvTarefasConcluidas = findViewById(R.id.tvTarefasConcluidas);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnHamburger = findViewById(R.id.btnHamburger);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Header do drawer
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            tvNomeUsuarioDrawer = headerView.findViewById(R.id.tvNomeUsuarioDrawer);
            profileImage = headerView.findViewById(R.id.profileImage);
            profileImageContainer = headerView.findViewById(R.id.profileImageContainer);
        }

        Log.d(TAG, "Views inicializadas");
    }

    private void setupQuickAction() {
        CardView quickActionCreate = findViewById(R.id.quickActionCreate);
        if (quickActionCreate != null) {
            quickActionCreate.setOnClickListener(v -> {
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
                            showAddTarefaDialog();
                        })
                        .start();
            });
        }
    }

    private void setupFab() {
        if (fabAddTarefa != null) {
            fabAddTarefa.setOnClickListener(v -> {
                animateFAB(fabAddTarefa);
                showAddTarefaDialog();
            });
        }
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

    private void showAddTarefaDialog() {
        tarefaEditando = null;
        posicaoEditando = -1;
        showTarefaDialog("Criar Nova Tarefa", "Criar", null);
    }

    private void showEditTarefaDialog(Tarefa tarefa, int position) {
        tarefaEditando = tarefa;
        posicaoEditando = position;
        showTarefaDialog("Editar Tarefa", "Salvar", tarefa);
    }

    private void showTarefaDialog(String titulo, String botaoTexto, Tarefa tarefaExistente) {
        try {
            // Criar um layout inflater personalizado
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_add_tarefa, null);

            // Criar um diálogo personalizado
            Dialog dialog = new Dialog(this);
            dialog.setContentView(dialogView);

            // Configurar a janela do diálogo
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // Configurar views do diálogo
            TextView tvTituloDialog = dialogView.findViewById(R.id.tvTituloDialog);
            ChipGroup chipGroupDias = dialogView.findViewById(R.id.chipGroupDias);
            TextInputEditText etTitulo = dialogView.findViewById(R.id.etTitulo);
            TextInputEditText etHorario = dialogView.findViewById(R.id.etHorario);
            TextInputEditText etDataLimite = dialogView.findViewById(R.id.etDataLimite);
            TextInputEditText etProfessor = dialogView.findViewById(R.id.etProfessor);
            RadioGroup radioGroupModalidade = dialogView.findViewById(R.id.radioGroupModalidade);
            RadioButton rbPresencial = dialogView.findViewById(R.id.rbPresencial);
            RadioButton rbEAD = dialogView.findViewById(R.id.rbEAD);
            MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
            MaterialButton btnSalvar = dialogView.findViewById(R.id.btnSalvar);

            // Configurar título e texto do botão
            tvTituloDialog.setText(titulo);
            btnSalvar.setText(botaoTexto);

            // CORREÇÃO: Configurar radio buttons corretamente
            int volansBlue = getResources().getColor(R.color.volans_blue);
            rbPresencial.setButtonTintList(ColorStateList.valueOf(volansBlue));
            rbEAD.setButtonTintList(ColorStateList.valueOf(volansBlue));

            // CORREÇÃO: Limpar seleções primeiro
            radioGroupModalidade.clearCheck();

            // CORREÇÃO: Configurar listener ANTES de definir valores iniciais
            radioGroupModalidade.setOnCheckedChangeListener((group, checkedId) -> {
                // Garantir seleção única
                if (checkedId == R.id.rbPresencial) {
                    rbEAD.setChecked(false);
                    Log.d(TAG, "Presencial selecionado");
                } else if (checkedId == R.id.rbEAD) {
                    rbPresencial.setChecked(false);
                    Log.d(TAG, "EAD selecionado");
                }
            });

            // Preencher dados se for edição
            if (tarefaExistente != null) {
                etTitulo.setText(tarefaExistente.getTitulo());
                etHorario.setText(tarefaExistente.getHorario());
                etDataLimite.setText(tarefaExistente.getDataLimite());
                etProfessor.setText(tarefaExistente.getProfessor());

                // CORREÇÃO: Definir modalidade usando o RadioGroup
                if (tarefaExistente.getModalidade().equals("Presencial")) {
                    radioGroupModalidade.check(R.id.rbPresencial);
                } else {
                    radioGroupModalidade.check(R.id.rbEAD);
                }

                // Selecionar o dia da semana
                String diaSemana = tarefaExistente.getDiaSemana();
                for (int i = 0; i < chipGroupDias.getChildCount(); i++) {
                    View child = chipGroupDias.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (chip.getText().toString().equals(diaSemana)) {
                            chip.setChecked(true);
                            break;
                        }
                    }
                }
            } else {
                // CORREÇÃO: Para nova tarefa, definir Presencial como padrão usando RadioGroup
                radioGroupModalidade.check(R.id.rbPresencial);
            }

            // Configurar listeners para seletores de tempo e data
            etHorario.setOnClickListener(v -> showTimePicker(etHorario));
            etDataLimite.setOnClickListener(v -> showDatePicker(etDataLimite));

            // Configurar botão Cancelar
            btnCancelar.setOnClickListener(v -> dialog.dismiss());

            // Configurar botão Salvar/Criar
            btnSalvar.setOnClickListener(v -> {
                String tituloTarefa = etTitulo.getText().toString().trim();
                String diaSelecionado = getDiaSelecionado(chipGroupDias);
                String horario = etHorario.getText().toString().trim();
                String dataLimite = etDataLimite.getText().toString().trim();
                String professor = etProfessor.getText().toString().trim();

                // CORREÇÃO: Determinar modalidade selecionada usando RadioGroup de forma mais robusta
                String modalidade = "Presencial"; // padrão
                if (radioGroupModalidade != null) {
                    int checkedId = radioGroupModalidade.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbEAD) {
                        modalidade = "EAD";
                    } else if (checkedId == R.id.rbPresencial) {
                        modalidade = "Presencial";
                    }
                    Log.d(TAG, "Modalidade selecionada: " + modalidade);
                }

                if (tituloTarefa.isEmpty() || diaSelecionado.isEmpty() || horario.isEmpty() || dataLimite.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tarefaEditando == null) {
                    // Criar nova tarefa
                    Tarefa novaTarefa = new Tarefa(tituloTarefa, diaSelecionado, horario, dataLimite, modalidade, professor);
                    listaTarefas.add(novaTarefa);
                    tarefaAdapter.notifyItemInserted(listaTarefas.size() - 1);
                    Toast.makeText(this, "Tarefa criada com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    // Editar tarefa existente
                    tarefaEditando.setTitulo(tituloTarefa);
                    tarefaEditando.setDiaSemana(diaSelecionado);
                    tarefaEditando.setHorario(horario);
                    tarefaEditando.setDataLimite(dataLimite);
                    tarefaEditando.setModalidade(modalidade);
                    tarefaEditando.setProfessor(professor);

                    tarefaAdapter.notifyItemChanged(posicaoEditando);
                    Toast.makeText(this, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                }

                updateCounters();
                updateEmptyState();
                saveTarefas(); // CORREÇÃO: Salvar tarefas após criar/editar
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar diálogo: ", e);
            Toast.makeText(this, "Erro ao abrir o diálogo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getDiaSelecionado(ChipGroup chipGroup) {
        try {
            // Percorrer todos os chips para encontrar o selecionado
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        return chip.getText().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter dia selecionado: ", e);
        }

        return "";
    }

    private void showTimePicker(TextInputEditText editText) {
        try {
            // Inflar o layout personalizado
            View dialogView = getLayoutInflater().inflate(R.layout.custom_time_picker, null);

            // Criar o diálogo
            Dialog dialog = new Dialog(this, R.style.CustomTimePickerDialog);
            dialog.setContentView(dialogView);

            // Configurar a janela do diálogo
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // Inicializar componentes
            TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
            TextView tvTimeHeader = dialogView.findViewById(R.id.tvTimeHeader);
            Button btnCancelTime = dialogView.findViewById(R.id.btnCancelTime);
            Button btnOkTime = dialogView.findViewById(R.id.btnOkTime);

            // Configurar TimePicker
            timePicker.setIs24HourView(true);

            // CORREÇÃO: Aplicar cores personalizadas - SEMPRE BRANCO COM #404CCF
            int volansBlueColor = getResources().getColor(R.color.volans_blue);
            int whiteColor = getResources().getColor(R.color.white);

            // Forçar cores independente do tema
            tvTimeHeader.setTextColor(whiteColor);
            btnCancelTime.setTextColor(volansBlueColor);
            btnOkTime.setTextColor(volansBlueColor);

            // Tentar aplicar a cor ao seletor do TimePicker usando reflexão
            try {
                Field[] timePickerFields = TimePicker.class.getDeclaredFields();
                for (Field field : timePickerFields) {
                    if (field.getName().contains("mSelectionDivider") ||
                            field.getName().contains("selectionDivider") ||
                            field.getName().contains("mSelectorWheelPaint")) {
                        field.setAccessible(true);
                        Object divider = field.get(timePicker);
                        if (divider instanceof Drawable) {
                            ((Drawable) divider).setColorFilter(volansBlueColor, PorterDuff.Mode.SRC_IN);
                        } else if (divider instanceof Paint) {
                            ((Paint) divider).setColor(volansBlueColor);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Não foi possível personalizar a cor do seletor do TimePicker", e);
            }

            // Obter hora atual
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Configurar hora inicial
            timePicker.setHour(hour);
            timePicker.setMinute(minute);

            // Atualizar o cabeçalho
            updateTimeHeader(tvTimeHeader, hour, minute);

            // Listener para atualizar o cabeçalho quando o tempo mudar
            timePicker.setOnTimeChangedListener((view, hourOfDay, minuteOfHour) -> {
                updateTimeHeader(tvTimeHeader, hourOfDay, minuteOfHour);
            });

            // Configurar botões
            btnCancelTime.setOnClickListener(v -> dialog.dismiss());

            btnOkTime.setOnClickListener(v -> {
                int selectedHour = timePicker.getHour();
                int selectedMinute = timePicker.getMinute();
                String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                editText.setText(time);
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar seletor de tempo: ", e);
            // Fallback para o seletor padrão
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (TimePicker view, int hourOfDay, int minuteOfHour) -> {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        editText.setText(time);
                    }, hour, minute, true);

            timePickerDialog.show();
        }
    }

    private void updateTimeHeader(TextView tvTimeHeader, int hour, int minute) {
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        tvTimeHeader.setText(timeText);
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        btnHamburger.setOnClickListener(v -> {
            animateButton(btnHamburger);

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_dashboard) {
            navigateToActivity(DashboardActivity.class);
            return true;
        } else if (id == R.id.nav_baralhos) {
            navigateToActivity(BaralhoActivity.class);
            return true;
        } else if (id == R.id.nav_atividades) {
            navigateToActivity(AtividadeActivity.class);
            return true;
        } else if (id == R.id.nav_tarefas) {
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
        profileImageContainer.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem de perfil"), PICK_PROFILE_IMAGE_REQUEST);
        });
        loadProfileImage();
    }

    private void setupUserData() {
        String nomeUsuario = SharedPrefManager.getInstance(this).getNomeUsuario();
        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = "Usuário";
        }
        tvNomeUsuarioDrawer.setText(nomeUsuario);
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

    private Bitmap processProfileImage(Uri imageUri) throws FileNotFoundException {
        InputStream imageStream = getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);

        if (originalBitmap == null) {
            return null;
        }

        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 500);
        return getCircularBitmap(resizedBitmap);
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
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_tarefas);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                animateBottomNavigationIcon(bottomNavigationView.findViewById(itemId));

                if (itemId == R.id.nav_dashboard) {
                    navigateToActivity(DashboardActivity.class);
                    return true;
                } else if (itemId == R.id.nav_baralhos) {
                    navigateToActivity(BaralhoActivity.class);
                    return true;
                } else if (itemId == R.id.nav_atividade) {
                    navigateToActivity(AtividadeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_tarefas) {
                    return true;
                }

                return false;
            }
        });
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

    private void animateBottomNavigationIcon(View view) {
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

    private void setupRecyclerView() {
        listaTarefas = new ArrayList<>();
        tarefaAdapter = new TarefaAdapter(listaTarefas, this);
        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTarefas.setAdapter(tarefaAdapter);
    }

    // CORREÇÃO: Método loadTarefas completo com integração ao SharedPreferences
    private void loadTarefas() {
        // Carregar tarefas do SharedPreferences
        List<Tarefa> tarefasSalvas = SharedPrefManager.getInstance(this).getTarefas();
        if (tarefasSalvas != null && !tarefasSalvas.isEmpty()) {
            listaTarefas.clear();
            listaTarefas.addAll(tarefasSalvas);
            Log.d(TAG, "Carregadas " + tarefasSalvas.size() + " tarefas do SharedPreferences");
        } else {
            // Carregar tarefas de exemplo se não houver tarefas salvas
            listaTarefas.add(new Tarefa("Entrega do Projeto Final", "Quinta", "19:00", "30/05/2024", "Presencial", "Prof. Silva"));
            listaTarefas.add(new Tarefa("Prova de Matemática", "Sexta", "14:00", "02/06/2024", "EAD", "Prof. Santos"));
            listaTarefas.add(new Tarefa("Apresentação TCC", "Segunda", "08:00", "15/05/2024", "Presencial", "Prof. Costa"));
            Log.d(TAG, "Carregadas tarefas de exemplo");

            // Salvar as tarefas de exemplo
            saveTarefas();
        }
        tarefaAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    // CORREÇÃO: Método saveTarefas implementado
    private void saveTarefas() {
        try {
            SharedPrefManager.getInstance(this).saveTarefas(listaTarefas);
            Log.d(TAG, "Tarefas salvas com sucesso: " + listaTarefas.size() + " tarefas");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar tarefas", e);
            Toast.makeText(this, "Erro ao salvar tarefas", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCounters() {
        int totalTarefas = listaTarefas.size();
        int pendentes = (int) listaTarefas.stream().filter(t -> !t.isConcluida()).count();
        int concluidas = totalTarefas - pendentes;

        animateCounterUpdate(tvTarefaCount, totalTarefas + " tarefas criadas");
        animateCounterUpdate(tvTarefasPendentes, String.valueOf(pendentes));
        animateCounterUpdate(tvTarefasConcluidas, String.valueOf(concluidas));
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

    private void updateEmptyState() {
        if (listaTarefas.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewTarefas.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewTarefas.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onTarefaClick(Tarefa tarefa, int position) {
        Toast.makeText(this, "Tarefa: " + tarefa.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTarefaDelete(Tarefa tarefa, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_tarefa, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();

        TextView tvNomeTarefaExcluir = dialogView.findViewById(R.id.tvNomeTarefaExcluir);
        TextView tvDetalhesExcluir = dialogView.findViewById(R.id.tvDetalhesExcluir);
        MaterialButton btnCancelarDelete = dialogView.findViewById(R.id.btnCancelarDelete);
        MaterialButton btnConfirmarDelete = dialogView.findViewById(R.id.btnConfirmarDelete);

        tvNomeTarefaExcluir.setText(tarefa.getTitulo());
        tvDetalhesExcluir.setText(tarefa.getDiaSemana() + " • " + tarefa.getHorario());

        btnCancelarDelete.setOnClickListener(v -> dialog.dismiss());

        btnConfirmarDelete.setOnClickListener(v -> {
            listaTarefas.remove(position);
            tarefaAdapter.notifyItemRemoved(position);
            updateCounters();
            updateEmptyState();
            saveTarefas(); // CORREÇÃO: Salvar após excluir
            Toast.makeText(this, "Tarefa excluída com sucesso!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        dialog.show();
    }

    @Override
    public void onTarefaEdit(Tarefa tarefa, int position) {
        showEditTarefaDialog(tarefa, position);
    }

    @Override
    public void onTarefaToggleComplete(Tarefa tarefa, int position) {
        updateCounters();
        saveTarefas(); // CORREÇÃO: Salvar após alterar status
        String status = tarefa.isConcluida() ? "concluída" : "reaberta";
        Toast.makeText(this, "Tarefa " + status + "!", Toast.LENGTH_SHORT).show();
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
        bottomNavigationView.setSelectedItemId(R.id.nav_tarefas);
    }
}
