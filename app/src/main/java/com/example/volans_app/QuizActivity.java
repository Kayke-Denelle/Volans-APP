package com.example.volans_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;

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
    private Button botaoAcao;

    // Quiz data
    private QuizModel quiz;
    private List<QuestaoQuiz> perguntas;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Log.d(TAG, "onCreate: Iniciando QuizActivity");

        initViews();

        // Verify intent and data
        if (!verifyIntentAndData()) {
            return;
        }

        // Show first question
        mostrarPerguntaAtual();

        // Set up button listener
        setupButtonListener();
    }

    private void initViews() {
        contadorPergunta = findViewById(R.id.contadorPergunta);
        perguntaTextView = findViewById(R.id.perguntaTextView);
        alternativasGroup = findViewById(R.id.alternativasGroup);
        botaoAcao = findViewById(R.id.botaoAcao);
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
                alternativasGroup.addView(createRadioButton(option));
            }
        }
    }

    private RadioButton createRadioButton(String text) {
        RadioButton radioButton = new RadioButton(new ContextThemeWrapper(this, R.style.QuizRadioButton));
        radioButton.setText(text);
        radioButton.setId(View.generateViewId());
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        ));
        radioButton.setPadding(32, 16, 32, 16);
        return radioButton;
    }

    private void updateButtonText() {
        botaoAcao.setText(currentQuestionIndex == perguntas.size() - 1 ?
                "Finalizar Quiz" : "Próxima Pergunta");
    }

    private void setupButtonListener() {
        botaoAcao.setOnClickListener(v -> {
            if (!isOptionSelected()) {
                showToast("Selecione uma alternativa");
                return;
            }

            checkAnswer();
            goToNextQuestion();
        });
    }

    private boolean isOptionSelected() {
        return alternativasGroup.getCheckedRadioButtonId() != -1;
    }

    private void checkAnswer() {
        RadioButton selectedRadioButton = findViewById(alternativasGroup.getCheckedRadioButtonId());
        String selectedAnswer = selectedRadioButton.getText().toString();
        QuestaoQuiz currentQuestion = perguntas.get(currentQuestionIndex);

        if (selectedAnswer.equals(currentQuestion.getRespostaCorreta())) {
            correctAnswers++;
            showToastWithStyle("Resposta correta!", R.style.CorrectAnswerToast);
        } else {
            showToastWithStyle(
                    String.format("Resposta incorreta! A correta era: %s",
                            currentQuestion.getRespostaCorreta()),
                    R.style.WrongAnswerToast);
        }
    }

    private void goToNextQuestion() {
        currentQuestionIndex++;
        if (hasMoreQuestions()) {
            mostrarPerguntaAtual();
        } else {
            showFinalResults();
        }
    }

    private boolean hasMoreQuestions() {
        return currentQuestionIndex < perguntas.size();
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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Excluindo todos os quizzes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.deletarQuizzesPorBaralho(token, quiz.getBaralhoId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Todos quizzes do baralho excluídos com sucesso");
                    Toast.makeText(QuizActivity.this,
                            "Todos os quizzes deste baralho foram excluídos",
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem detalhes";
                        Log.e(TAG, "Erro ao excluir quizzes - Código: " +
                                response.code() + " - Erro: " + errorBody);
                        Toast.makeText(QuizActivity.this,
                                "Erro ao excluir quizzes",
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler corpo do erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Falha na requisição: " + t.getMessage());
                Toast.makeText(QuizActivity.this,
                        "Falha na conexão ao excluir quizzes",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showFinalResults() {
        double percentageCorrect = (double) correctAnswers / perguntas.size() * 100;
        String result = String.format(
                "Você acertou %d de %d perguntas!\n(%.1f%%)",
                correctAnswers, perguntas.size(), percentageCorrect);

        new AlertDialog.Builder(this)
                .setTitle("Quiz Finalizado")
                .setMessage(result)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Exclui automaticamente sem perguntar
                    deletarTodosQuizzesDoBaralho();
                    finish();
                })
                .setCancelable(false)
                .show();
    }
    private void deletarQuizNoBackend() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        if (token.equals("Bearer ")) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (quiz == null || quiz.getId() == null) {
            Log.e(TAG, "Quiz ID é nulo");
            Toast.makeText(this, "Erro: Quiz inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Iniciando exclusão do quiz ID: " + quiz.getId());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Excluindo quiz...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.deletarQuiz(token, quiz.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Resposta do servidor: Sucesso (HTTP " + response.code() + ")");
                    Toast.makeText(QuizActivity.this, "Quiz excluído", Toast.LENGTH_SHORT).show();

                    // DEBUG: Verifique no backend se realmente foi excluído
                    verificarSeQuizFoiExcluido();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e(TAG, "Erro ao excluir - Código: " + response.code() + " - Erro: " + errorBody);
                        Toast.makeText(QuizActivity.this, "Erro ao excluir", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao ler corpo do erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Falha na requisição: " + t.getMessage());
                Toast.makeText(QuizActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método adicional para debug
    private void verificarSeQuizFoiExcluido() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        ApiService apiService = RetrofitClient.getApiService();

        apiService.buscarQuiz(token, quiz.getId()).enqueue(new Callback<QuizModel>() {
            @Override
            public void onResponse(Call<QuizModel> call, Response<QuizModel> response) {
                if (response.code() == 404) {
                    Log.d(TAG, "Quiz realmente não existe mais no servidor (404)");
                } else if (response.isSuccessful()) {
                    Log.d(TAG, "O quiz AINDA EXISTE no servidor!");
                    Toast.makeText(QuizActivity.this,
                            "Erro: O quiz não foi excluído", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<QuizModel> call, Throwable t) {
                Log.e(TAG, "Erro ao verificar quiz", t);
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

}