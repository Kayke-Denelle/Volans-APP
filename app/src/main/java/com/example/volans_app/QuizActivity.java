package com.example.volans_app;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.LoadingDialog;
import com.example.volans_app.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String EXTRA_QUIZ = "quiz";

    // Views
    private TextView contadorPergunta, perguntaTextView;
    private RadioGroup alternativasGroup;
    private MaterialButton botaoAcao;
    private ImageButton btnVoltarQuiz;
    private CardView cardQuiz;
    private ProgressBar progressQuiz;

    // Quiz data
    private QuizModel quiz;
    private List<QuestaoQuiz> perguntas;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Loading dialog
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar status bar
        setupStatusBar();

        setContentView(R.layout.activity_quiz);
        Log.d(TAG, "onCreate: Iniciando QuizActivity");

        // Inicializar loading dialog
        loadingDialog = new LoadingDialog(this);

        initViews();

        // Verify intent and data
        if (!verifyIntentAndData()) {
            return;
        }

        // Setup click listeners
        setupClickListeners();

        // Show first question with animation
        animateCardEntry();
        mostrarPerguntaAtual();
        updateProgressBar();
    }

    private void setupStatusBar() {
        // Remove a flag fullscreen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Torna a status bar transparente
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Permite que o conteúdo vá atrás da status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Define ícones da status bar como escuros (para fundo claro) ou claros (para fundo escuro)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR  // Para ícones escuros
            );
        }
    }

    private void initViews() {
        contadorPergunta = findViewById(R.id.contadorPergunta);
        perguntaTextView = findViewById(R.id.perguntaTextView);
        alternativasGroup = findViewById(R.id.alternativasGroup);
        botaoAcao = findViewById(R.id.botaoAcao);
        btnVoltarQuiz = findViewById(R.id.btnVoltarQuiz);
        cardQuiz = findViewById(R.id.cardQuiz);
        progressQuiz = findViewById(R.id.progressQuiz);
    }

    private void setupClickListeners() {
        botaoAcao.setOnClickListener(v -> {
            if (!isOptionSelected()) {
                showToast("Selecione uma alternativa");
                shakeButton();
                return;
            }

            checkAnswer();
        });

        btnVoltarQuiz.setOnClickListener(v -> {
            showExitDialog();
        });
    }

    private void showExitDialog() {
        // Criar o diálogo customizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_quiz, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Configurar os botões
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuar);
        MaterialButton btnSair = dialogView.findViewById(R.id.btnSair);

        // Configurar botão continuar
        btnContinuar.setOnClickListener(v -> {
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

        // Configurar botão sair
        btnSair.setOnClickListener(v -> {
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
                        finish();
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
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        });

        dialog.show();
    }

    private void shakeButton() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        botaoAcao.startAnimation(shake);
    }

    private void animateCardEntry() {
        cardQuiz.setAlpha(0f);
        cardQuiz.setScaleX(0.8f);
        cardQuiz.setScaleY(0.8f);
        cardQuiz.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private boolean verifyIntentAndData() {
        // Check intent
        if (getIntent() == null) {
            Log.e(TAG, "Intent is null");
            showErrorAndFinish("Erro ao iniciar o quiz");
            return false;
        }

        // Check extras
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e(TAG, "Extras are null");
            showErrorAndFinish("Dados do quiz não recebidos");
            return false;
        }

        // Get quiz
        quiz = extras.getParcelable(EXTRA_QUIZ);
        if (quiz == null) {
            Log.e(TAG, "Quiz is null");
            showErrorAndFinish("Quiz inválido");
            return false;
        }

        // Check questions
        perguntas = quiz.getPerguntas();
        if (perguntas == null) {
            Log.e(TAG, "Questions list is null");
            showErrorAndFinish("Nenhuma pergunta disponível");
            return false;
        }

        if (perguntas.isEmpty()) {
            Log.e(TAG, "Questions list is empty");
            showErrorAndFinish("Nenhuma pergunta disponível");
            return false;
        }

        Log.d(TAG, "Quiz loaded with " + perguntas.size() + " questions");
        return true;
    }

    private void mostrarPerguntaAtual() {
        // Clear previous state
        alternativasGroup.clearCheck();
        alternativasGroup.removeAllViews();

        // Get current question
        QuestaoQuiz perguntaAtual = perguntas.get(currentQuestionIndex);
        Log.d(TAG, "Showing question: " + perguntaAtual.getEnunciado());

        // Update UI
        updateQuestionCounter();
        setQuestionText(perguntaAtual.getEnunciado());
        addOptionsToRadioGroup(perguntaAtual.getOpcoes());
        updateButtonText();
    }

    private void updateQuestionCounter() {
        contadorPergunta.setText(String.format("Pergunta %d/%d",
                currentQuestionIndex + 1, perguntas.size()));
    }

    private void setQuestionText(String questionText) {
        perguntaTextView.setText(questionText);
    }

    private void addOptionsToRadioGroup(List<String> options) {
        if (options == null || options.isEmpty()) {
            Log.w(TAG, "No options available for this question");
            return;
        }

        for (String option : options) {
            if (option != null) {
                RadioButton radioButton = createRadioButton(option);
                alternativasGroup.addView(radioButton);

                // Animar entrada das opções
                radioButton.setAlpha(0f);
                radioButton.setTranslationY(50f);
                radioButton.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setStartDelay(alternativasGroup.getChildCount() * 100)
                        .start();
            }
        }
    }

    private RadioButton createRadioButton(String text) {
        RadioButton radioButton = new RadioButton(new ContextThemeWrapper(this, R.style.QuizRadioButton));
        radioButton.setText(text);
        radioButton.setId(View.generateViewId());

        // Configurar layout
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        radioButton.setLayoutParams(params);

        return radioButton;
    }

    private void updateButtonText() {
        botaoAcao.setText(currentQuestionIndex == perguntas.size() - 1 ?
                "Finalizar Quiz" : "Próxima Pergunta");
    }

    private boolean isOptionSelected() {
        return alternativasGroup.getCheckedRadioButtonId() != -1;
    }

    private void checkAnswer() {
        RadioButton selectedRadioButton = findViewById(alternativasGroup.getCheckedRadioButtonId());
        String selectedAnswer = selectedRadioButton.getText().toString();
        QuestaoQuiz currentQuestion = perguntas.get(currentQuestionIndex);

        boolean isCorrect = selectedAnswer.equals(currentQuestion.getRespostaCorreta());

        // Destacar a resposta selecionada
        if (isCorrect) {
            correctAnswers++;
            highlightCorrectAnswer(selectedRadioButton);
        } else {
            highlightWrongAnswer(selectedRadioButton);
            highlightCorrectOption(currentQuestion.getRespostaCorreta());
        }

        // Desabilitar interação durante a animação
        disableInteraction();

        // Aguardar um momento para mostrar o feedback antes de avançar
        handler.postDelayed(() -> {
            goToNextQuestion();
        }, 1500);
    }

    private void highlightCorrectAnswer(RadioButton button) {
        button.setBackgroundResource(R.drawable.bg_radio_correct);
        showToastWithStyle("✓ Resposta correta!", R.style.CorrectAnswerToast);
    }

    private void highlightWrongAnswer(RadioButton button) {
        button.setBackgroundResource(R.drawable.bg_radio_wrong);
        showToastWithStyle("✗ Resposta incorreta!", R.style.WrongAnswerToast);
    }

    private void highlightCorrectOption(String correctAnswer) {
        // Encontrar e destacar a opção correta
        for (int i = 0; i < alternativasGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) alternativasGroup.getChildAt(i);
            if (rb.getText().toString().equals(correctAnswer)) {
                rb.setBackgroundResource(R.drawable.bg_radio_correct);
                break;
            }
        }
    }

    private void disableInteraction() {
        botaoAcao.setEnabled(false);
        for (int i = 0; i < alternativasGroup.getChildCount(); i++) {
            alternativasGroup.getChildAt(i).setEnabled(false);
        }
    }

    private void enableInteraction() {
        botaoAcao.setEnabled(true);
        for (int i = 0; i < alternativasGroup.getChildCount(); i++) {
            alternativasGroup.getChildAt(i).setEnabled(true);
        }
    }

    private void goToNextQuestion() {
        currentQuestionIndex++;

        if (hasMoreQuestions()) {
            // Animar saída do card atual
            cardQuiz.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        // Mostrar próxima pergunta
                        mostrarPerguntaAtual();
                        updateProgressBar();
                        enableInteraction();

                        // Animar entrada do novo card
                        cardQuiz.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        } else {
            showFinalResults();
        }
    }

    private void updateProgressBar() {
        int progress = (currentQuestionIndex * 100) / perguntas.size();
        ObjectAnimator.ofInt(progressQuiz, "progress", progress)
                .setDuration(300)
                .start();
    }

    private boolean hasMoreQuestions() {
        return currentQuestionIndex < perguntas.size();
    }

    private void showFinalResults() {
        double percentageCorrect = (double) correctAnswers / perguntas.size() * 100;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);
        TextView tvScore = dialogView.findViewById(R.id.tvScore);
        TextView tvPercentage = dialogView.findViewById(R.id.tvPercentage);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

        tvScore.setText(String.format("%d/%d", correctAnswers, perguntas.size()));
        tvPercentage.setText(String.format("%.0f%%", percentageCorrect));

        // Definir mensagem baseada no desempenho
        if (percentageCorrect >= 80) {
            tvMessage.setText("Excelente! Você domina este assunto!");
        } else if (percentageCorrect >= 60) {
            tvMessage.setText("Bom trabalho! Continue praticando.");
        } else {
            tvMessage.setText("Continue estudando. Você vai melhorar!");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            deletarTodosQuizzesDoBaralho();
            finish();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void deletarTodosQuizzesDoBaralho() {
        // Verificar se temos um baralhoId válido
        if (quiz == null || quiz.getBaralhoId() == null || quiz.getBaralhoId().isEmpty()) {
            Log.e(TAG, "ID do baralho inválido");
            Toast.makeText(this, "Erro: Baralho inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.equals("Bearer ")) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "Iniciando exclusão de todos quizzes do baralho ID: " + quiz.getBaralhoId());

        // Usar o loading dialog customizado
        loadingDialog.show("Finalizando quiz...");

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.deletarQuizzesPorBaralho(token, quiz.getBaralhoId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Todos quizzes do baralho excluídos com sucesso");
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem detalhes";
                        Log.e(TAG, "Erro ao excluir quizzes - Código: " +
                                response.code() + " - Erro: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler corpo do erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Falha na requisição: " + t.getMessage());
            }
        });
    }

    private void showErrorAndFinish(String message) {
        showToast(message);
        Log.e(TAG, message);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showToastWithStyle(String message, int styleRes) {
        try {
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            View view = toast.getView();
            if (view != null) {
                view.setBackgroundResource(styleRes);
            }
            toast.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing styled toast", e);
            showToast(message);
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
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