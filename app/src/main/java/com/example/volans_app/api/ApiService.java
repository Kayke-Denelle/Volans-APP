package com.example.volans_app.api;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
//import com.example.volans_app.DTO.QuizRequest;
import com.example.volans_app.DTO.ResultadoQuiz;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Criar baralho
    @POST("api/baralhos")
    Call<Baralho> criarBaralho(@Body Baralho baralho, @Header("Authorization") String token);

    // Listar baralhos do usuário
    @GET("api/baralhos")
    Call<List<Baralho>> listarBaralhos(@Header("Authorization") String token);

    // Criar flashcard
    @POST("api/flashcards")
    Call<Flashcard> criarFlashcard(@Body Flashcard flashcard, @Header("Authorization") String token);

    // Listar flashcards de um baralho
    @GET("api/flashcards")
    Call<List<Flashcard>> getFlashcardsPorBaralho(@Query("baralhoId") String baralhoId, @Header("Authorization") String token);

    // Gerar quiz
    @POST("api/quizzes/gerar/{baralhoId}")
    Call<List<QuestaoQuiz>> gerarQuiz(
            @Header("Authorization") String token,
            @Path("baralhoId") String baralhoId
    );

    // Avaliar respostas do quiz
//    @POST("api/quizzes/avaliar")
//    Call<ResultadoQuiz> verificarResposta(
//            @Header("Authorization") String token,
//            @Body QuizRequest requisicao
//    );

    // Buscar quiz por ID
    @GET("api/quizzes/{quizId}")
    Call<QuizModel> buscarQuiz(
            @Header("Authorization") String token,
            @Path("quizId") String quizId
    );

    // ===== NOVOS ENDPOINTS PARA DELETAR =====

    // Deletar quiz por ID
    @DELETE("api/quizzes/{quizId}")
    Call<Void> deletarQuiz(
            @Header("Authorization") String token,
            @Path("quizId") String quizId
    );

    // Deletar todos os quizzes de um baralho
    @DELETE("api/quizzes/baralho/{baralhoId}")
    Call<Void> deletarQuizzesPorBaralho(
            @Header("Authorization") String token,
            @Path("baralhoId") String baralhoId
    );

    @GET("api/baralhos/quantidade")
    Call<Integer> getQuantidadeBaralhos(@Header("Authorization") String token);

    @GET("api/baralhos/quantidade-flashcards")
    Call<Integer> getQuantidadeFlashcards(@Header("Authorization") String token);


}