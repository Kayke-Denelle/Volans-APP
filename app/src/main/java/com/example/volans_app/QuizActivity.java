package com.example.volans_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.api.ApiService;
import com.example.volans_app.api.RetrofitClient;
import com.example.volans_app.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private TextView perguntaTextView;
    private RadioGroup alternativasGroup;
    private Button responderButton;
    private String baralhoId;
    private String token;
    private List<QuizModel> quizzes;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Log.d(TAG, "Activity criada");

        // Inicialização dos componentes
        perguntaTextView = findViewById(R.id.perguntaTextView);
        alternativasGroup = findViewById(R.id.alternativasGroup);
        responderButton = findViewById(R.id.responderButton);

        // Obter dados da intent e do SharedPreferences
        baralhoId = getIntent().getStringExtra("baralhoId");
        token = SharedPrefManager.getInstance(this).getToken();

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token não encontrado - redirecionando para login");
            redirectToLogin();
            return;
        }

        // Adiciona o prefixo Bearer se não existir
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        Log.d(TAG, "Baralho ID: " + baralhoId);
        Log.d(TAG, "Token formatado: " + token.substring(0, Math.min(token.length(), 15)) + "...");

        criarEIniciarQuiz();
    }

    private void redirectToLogin() {
        SharedPrefManager.getInstance(this).logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void criarEIniciarQuiz() {
        // Verificação reforçada do token
        if (token == null || token.equals("Bearer null") || token.length() < 50) {
            Log.e(TAG, "Token inválido ou malformado");
            Toast.makeText(this, "Erro de autenticação. Faça login novamente.", Toast.LENGTH_LONG).show();
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<QuizModel>> call = api.criarQuiz(token, baralhoId);

        Log.d(TAG, "Enviando requisição com token: " + token.substring(0, 15) + "...");

        call.enqueue(new Callback<List<QuizModel>>() {
            @Override
            public void onResponse(Call<List<QuizModel>> call, Response<List<QuizModel>> response) {
                if (response.code() == 401) {
                    handleUnauthorizedError();
                    return;
                }

                // Restante do tratamento...
            }

            @Override
            public void onFailure(Call<List<QuizModel>> call, Throwable t) {
                Log.e(TAG, "Falha na conexão: " + t.getMessage(), t);
                runOnUiThread(() ->
                        Toast.makeText(QuizActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void handleUnauthorizedError() {
        Log.e(TAG, "Erro 401 - Acesso não autorizado");
        runOnUiThread(() -> {
            Toast.makeText(QuizActivity.this,
                    "Sessão expirada ou inválida. Faça login novamente.",
                    Toast.LENGTH_LONG).show();
            SharedPrefManager.getInstance(QuizActivity.this).logout();
            startActivity(new Intent(QuizActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void mostrarPerguntaAtual() {
        try {
            // Limpa alternativas anteriores
            alternativasGroup.removeAllViews();

            // Obtém a pergunta atual
            QuizModel perguntaAtual = quizzes.get(currentQuestionIndex);

            // Define o texto da pergunta
            perguntaTextView.setText(perguntaAtual.getPergunta());

            // Verifica se há alternativas
            if (perguntaAtual.getAlternativas() == null || perguntaAtual.getAlternativas().isEmpty()) {
                Toast.makeText(this, "Erro: Questão sem alternativas", Toast.LENGTH_SHORT).show();
                return;
            }

            // Adiciona cada alternativa como RadioButton
            for (int i = 0; i < perguntaAtual.getAlternativas().size(); i++) {
                RadioButton radioButton = new RadioButton(new ContextThemeWrapper(this, R.style.QuizRadioButton));
                radioButton.setText(perguntaAtual.getAlternativas().get(i));
                radioButton.setId(View.generateViewId()); // ID único
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                ));
                radioButton.setPadding(32, 32, 32, 32); // Espaçamento maior
                alternativasGroup.addView(radioButton);
            }

        } catch (Exception e) {
            Log.e("QUIZ_ERROR", "Erro ao mostrar pergunta: " + e.getMessage());
            Toast.makeText(this, "Erro ao carregar pergunta", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarResposta() {
        int selectedId = alternativasGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Selecione uma alternativa", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String respostaSelecionada = selectedRadioButton.getText().toString();

        // Obter a pergunta atual
        QuizModel currentQuestion = quizzes.get(currentQuestionIndex);

        // Verificar se a resposta está correta
        if (respostaSelecionada.equals(currentQuestion.getRespostaCorreta())) {
            correctAnswers++;
            Log.d(TAG, "Resposta correta! Acertos: " + correctAnswers);
            Toast.makeText(this, "Resposta correta!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Resposta incorreta. Esperado: " + currentQuestion.getRespostaCorreta());
            Toast.makeText(this, "Resposta incorreta!", Toast.LENGTH_SHORT).show();
        }

        // Avançar para a próxima pergunta ou finalizar
        currentQuestionIndex++;
        if (currentQuestionIndex < quizzes.size()) {
            mostrarPerguntaAtual();
        } else {
            mostrarResultadoFinal();
        }
    }

    private void mostrarResultadoFinal() {
        String resultado = String.format("Você acertou %d de %d perguntas!", correctAnswers, quizzes.size());
        Log.d(TAG, resultado);

        new AlertDialog.Builder(this)
                .setTitle("Quiz Finalizado")
                .setMessage(resultado)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }



    private String getTokenFromPrefs() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token inválido - redirecionando para login");
            runOnUiThread(() -> {
                Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            });
            return null;
        }
        return token;
    }
}