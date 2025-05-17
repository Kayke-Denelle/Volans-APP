package com.example.volans_app.api;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.DTO.QuizRequest;
import com.example.volans_app.DTO.ResultadoQuiz;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

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
    @GET("api/flashcards/baralho/{baralhoId}")
    Call<List<Flashcard>> getFlashcardsPorBaralho(@Path("baralhoId") String baralhoId, @Header("Authorization") String token);

    @POST("/api/quizzes/gerar/{baralhoId}")
    Call<List<QuizModel>> criarQuiz(@Header("Authorization") String token, @Path("baralhoId") String baralhoId);

    @POST("/api/quizzes/avaliar")
    Call<ResultadoQuiz> verificarResposta(@Header("Authorization") String token, @Body QuizRequest requisicao);

    @GET("quizzes/baralho/{baralhoId}")
    Call<List<QuizModel>> buscarQuizzesPorBaralho(
            @Header("Authorization") String token,
            @Path("baralhoId") String baralhoId
    );
}
