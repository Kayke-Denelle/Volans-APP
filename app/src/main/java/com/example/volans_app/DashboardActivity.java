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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
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

import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private TextView tvQuantidadeBaralhos, tvQuantidadeFlashcards;
    private TextView tvNomeUsuario;
    private View decorativeLine;
    private ApiService apiService;
    private String token;
    private LineChart lineChart;

    // Chatbot
    private WebView webView;
    private FrameLayout chatContainer;
    private boolean isChatbotLoaded = false;

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnHamburger;
    private TextView tvNomeUsuarioDrawer;
    private ImageView profileImage;
    private CardView profileImageContainer;

    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;

    // Schedule
    private RecyclerView rvSchedule;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> scheduleList;

    // Quick Actions
    private View quickActionStudy, quickActionCreate;

    // Gesture detector para swipe navigation
    private GestureDetector gestureDetector;

    // Classe interna para ScheduleItem
    public static class ScheduleItem {
        private String subject;
        private String professor;
        private String mode;
        private String time;
        private String day;
        private String deliveryDate;
        private String deliveryTime;

        public ScheduleItem(String subject, String professor, String mode, String time, String day, String deliveryDate, String deliveryTime) {
            this.subject = subject;
            this.professor = professor;
            this.mode = mode;
            this.time = time;
            this.day = day;
            this.deliveryDate = deliveryDate;
            this.deliveryTime = deliveryTime;
        }

        public String getSubject() { return subject; }
        public String getProfessor() { return professor; }
        public String getMode() { return mode; }
        public String getTime() { return time; }
        public String getDay() { return day; }
        public String getDeliveryDate() { return deliveryDate; }
        public String getDeliveryTime() { return deliveryTime; }

        public boolean isDeliveryOverdue() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                String deliveryDateTime = deliveryDate + " " + deliveryTime;
                Date deliveryDateParsed = sdf.parse(deliveryDateTime);
                Date currentDate = new Date();

                return currentDate.after(deliveryDateParsed);
            } catch (ParseException e) {
                return false;
            }
        }
    }

    // Classe interna para ScheduleAdapter
    public static class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

        private List<ScheduleItem> scheduleList;

        public ScheduleAdapter(List<ScheduleItem> scheduleList) {
            this.scheduleList = scheduleList;
        }

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            ScheduleItem item = scheduleList.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        static class ScheduleViewHolder extends RecyclerView.ViewHolder {
            private TextView tvDay, tvTime, tvSubject, tvProfessor, tvMode, tvDeliveryDate;

            public ScheduleViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tvDay);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvSubject = itemView.findViewById(R.id.tvSubject);
                tvProfessor = itemView.findViewById(R.id.tvProfessor);
                tvMode = itemView.findViewById(R.id.tvMode);
                tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
            }

            public void bind(ScheduleItem item) {
                tvDay.setText(item.getDay());
                tvTime.setText(item.getTime());
                tvSubject.setText(item.getSubject());
                tvProfessor.setText(item.getProfessor());
                tvMode.setText(item.getMode());

                String deliveryText = item.getDeliveryDate() + " " + item.getDeliveryTime();
                tvDeliveryDate.setText(deliveryText);

                if (item.isDeliveryOverdue()) {
                    tvDeliveryDate.setTextColor(Color.parseColor("#FF5252"));
                } else {
                    tvDeliveryDate.setTextColor(Color.parseColor("#404CCF"));
                }

                itemView.setAlpha(0f);
                itemView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setStartDelay(getAdapterPosition() * 100)
                        .start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);

            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        setContentView(R.layout.activity_dashboard);

        initializeViews();
        setupDrawer();
        setupProfileImage();
        setupBottomNavigation();
        setupSwipeNavigation();
        setupChatbot();
        setupButtons();
        setupSchedule();
        setupUserData();
        carregarQuantidades();
        configurarGraficoLinha();
        loadScheduleData();
    }

    private void initializeViews() {
        tvQuantidadeBaralhos = findViewById(R.id.tvQuantidadeBaralhos);
        tvQuantidadeFlashcards = findViewById(R.id.tvQuantidadeFlashcards);
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        decorativeLine = findViewById(R.id.decorativeLine);
        lineChart = findViewById(R.id.lineChartRevisoes);
        quickActionStudy = findViewById(R.id.quickActionStudy);
        quickActionCreate = findViewById(R.id.quickActionCreate);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnHamburger = findViewById(R.id.btnHamburger);
        webView = findViewById(R.id.webview);
        chatContainer = findViewById(R.id.chat_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvSchedule = findViewById(R.id.rvSchedule);

        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            tvNomeUsuarioDrawer = headerView.findViewById(R.id.tvNomeUsuarioDrawer);
            profileImage = headerView.findViewById(R.id.profileImage);
            profileImageContainer = headerView.findViewById(R.id.profileImageContainer);
        }
    }

    private void setupSwipeNavigation() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Verificar se é um swipe horizontal (não vertical)
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Swipe para a esquerda - ir para Baralhos
                    if (diffX < -SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        navigateToBaralhos();
                        return true;
                    }
                    // Swipe para a direita - ir para Atividades
                    else if (diffX > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        navigateToAtividades();
                        return true;
                    }
                }
                return false;
            }
        });

        // Aplicar gesture detector ao layout principal
        View mainContent = findViewById(R.id.drawer_layout);
        if (mainContent != null) {
            mainContent.setOnTouchListener((v, event) -> {
                // Só processar swipe se o drawer estiver fechado
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    gestureDetector.onTouchEvent(event);
                }
                return false;
            });
        }
    }

    private void navigateToBaralhos() {
        Intent intent = new Intent(this, BaralhoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.smooth_slide_in_right, R.anim.smooth_slide_out_left);
        finish();
    }

    private void navigateToAtividades() {
        Intent intent = new Intent(this, AtividadeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.smooth_slide_in_left, R.anim.smooth_slide_out_right);
        finish();
    }

    private void setupProfileImage() {
        if (profileImageContainer != null) {
            profileImageContainer.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST);
            });
        }
        loadProfileImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = processImage(imageUri);
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

    private Bitmap processImage(Uri imageUri) {
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

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        if (btnHamburger != null) {
            btnHamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButton(btnHamburger);

                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_dashboard) {
            return true;
        } else if (id == R.id.nav_baralhos) {
            navigateToBaralhos();
            return true;
        } else if (id == R.id.nav_atividades) {
            navigateToAtividades();
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

    private void setupButtons() {
        if (quickActionStudy != null) {
            quickActionStudy.setOnClickListener(v -> {
                animateQuickAction(quickActionStudy);
                navigateToBaralhos();
            });
        }

        if (quickActionCreate != null) {
            quickActionCreate.setOnClickListener(v -> {
                animateQuickAction(quickActionCreate);
                navigateToAtividades();
            });
        }
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

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                animateBottomNavigationIcon(bottomNavigationView.findViewById(itemId));

                if (itemId == R.id.nav_dashboard) {
                    return true;
                } else if (itemId == R.id.nav_baralhos) {
                    navigateToBaralhos();
                    return true;
                } else if (itemId == R.id.nav_atividade) {
                    navigateToAtividades();
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

    private void setupSchedule() {
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSchedule.setLayoutManager(layoutManager);
        rvSchedule.setAdapter(scheduleAdapter);
    }

    private void loadScheduleData() {
        scheduleList.clear();
        scheduleList.add(new ScheduleItem("Startup: Final Project", "Eliney Sabino", "EAD", "17:10", "Quinta", "29/05", "19:00"));
        scheduleList.add(new ScheduleItem("Programação Mobile", "João Silva", "Presencial", "19:00", "Sexta", "15/05", "23:59"));
        scheduleList.add(new ScheduleItem("Banco de Dados", "Maria Santos", "EAD", "20:30", "Segunda", "02/06", "18:00"));
        scheduleList.add(new ScheduleItem("Engenharia de Software", "Pedro Costa", "Presencial", "14:00", "Terça", "10/05", "12:00"));
        scheduleList.add(new ScheduleItem("Análise de Sistemas", "Ana Lima", "EAD", "16:30", "Quarta", "25/05", "20:00"));
        scheduleList.add(new ScheduleItem("Interface Humano-Computador", "Carlos Mendes", "Presencial", "08:00", "Sábado", "30/05", "14:30"));

        scheduleAdapter.notifyDataSetChanged();
    }

    private void setupChatbot() {
        FloatingActionButton chatButton = findViewById(R.id.chat_button);
        FloatingActionButton closeButton = findViewById(R.id.close_chat_button);

        if (chatButton != null && closeButton != null && webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient());

            chatButton.setOnClickListener(v -> {
                animateFAB(chatButton);
                chatContainer.setVisibility(View.VISIBLE);
                if (!isChatbotLoaded) {
                    webView.loadUrl("https://cdn.botpress.cloud/webchat/v2.4/shareable.html?configUrl=https://files.bpcontent.cloud/2025/04/30/22/20250430222055-KNV4LQWN.json");
                    isChatbotLoaded = true;
                }
            });

            closeButton.setOnClickListener(v -> chatContainer.setVisibility(View.GONE));
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (chatContainer.getVisibility() == View.VISIBLE) {
            chatContainer.setVisibility(View.GONE);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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

        tvNomeUsuario.setText(nomeUsuario + "!");

        if (decorativeLine != null) {
            tvNomeUsuario.post(() -> {
                ViewGroup.LayoutParams params = decorativeLine.getLayoutParams();
                params.width = tvNomeUsuario.getWidth();
                decorativeLine.setLayoutParams(params);
            });
        }

        if (tvNomeUsuarioDrawer != null) {
            tvNomeUsuarioDrawer.setText(nomeUsuario);
        }

        apiService = RetrofitClient.getApiService();
    }

    private void logout() {
        SharedPrefManager.getInstance(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void carregarQuantidades() {
        if (token.equals("Bearer ")) {
            tvQuantidadeBaralhos.setText("0");
            tvQuantidadeFlashcards.setText("0");
            return;
        }

        apiService.getQuantidadeBaralhos(token).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animateCounterUpdate(tvQuantidadeBaralhos, response.body());
                } else {
                    tvQuantidadeBaralhos.setText("0");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                tvQuantidadeBaralhos.setText("0");
            }
        });

        apiService.getQuantidadeFlashcards(token).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animateCounterUpdate(tvQuantidadeFlashcards, response.body());
                } else {
                    tvQuantidadeFlashcards.setText("0");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                tvQuantidadeFlashcards.setText("0");
            }
        });
    }

    private void animateCounterUpdate(TextView textView, int targetValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            textView.setText(String.valueOf(animation.getAnimatedValue()));
        });
        animator.start();
    }

    private void configurarGraficoLinha() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 4f));
        entries.add(new Entry(1f, 7f));
        entries.add(new Entry(2f, 3f));
        entries.add(new Entry(3f, 6f));
        entries.add(new Entry(4f, 8f));

        LineDataSet dataSet = new LineDataSet(entries, "");

        dataSet.setColor(Color.parseColor("#5B4CF5"));
        dataSet.setCircleColor(Color.parseColor("#5B4CF5"));
        dataSet.setLineWidth(4f);
        dataSet.setCircleRadius(8f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#FFFFFF"));
        dataSet.setCircleHoleRadius(4f);
        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#5B4CF5"));
        dataSet.setFillAlpha(30);

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawBorders(false);

        XAxis xAxis = lineChart.getXAxis();
        final String[] labels = new String[]{"S1", "S2", "S3", "S4", "S5"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#6B7280"));
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        lineChart.getAxisLeft().setTextColor(Color.parseColor("#6B7280"));
        lineChart.getAxisLeft().setTextSize(12f);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#F3F4F6"));
        lineChart.getAxisLeft().setDrawAxisLine(false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.animateX(1500);
        lineChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }
}
