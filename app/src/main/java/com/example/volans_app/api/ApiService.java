package com.example.volans_app.api;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.DTO.QuestaoQuiz;
import com.example.volans_app.DTO.QuizModel;
import com.example.volans_app.DTO.RegistroRevisao;
import com.example.volans_app.DTO.ResultadoQuiz;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===== ENDPOINTS EXISTENTES =====

    // Criar baralho
    @POST("api/baralhos")
    Call<Baralho> criarBaralho(@Body Baralho baralho, @Header("Authorization") String token);

    // Listar baralhos do usuário
    @GET("api/baralhos")
    Call<List<Baralho>> listarBaralhos(@Header("Authorization") String token);

    // ===== NOVOS ENDPOINTS PARA BARALHOS =====

    // Editar baralho
    @PUT("api/baralhos/{id}")
    Call<Baralho> editarBaralho(@Path("id") String id, @Body Baralho baralho, @Header("Authorization") String token);

    // Excluir baralho
    @DELETE("api/baralhos/{id}")
    Call<Void> excluirBaralho(@Path("id") String id, @Header("Authorization") String token);

    // ===== ENDPOINTS DE FLASHCARDS =====

    // Criar flashcard
    @POST("api/flashcards")
    Call<Flashcard> criarFlashcard(@Body Flashcard flashcard, @Header("Authorization") String token);

    // Listar flashcards de um baralho
    @GET("api/flashcards")
    Call<List<Flashcard>> getFlashcardsPorBaralho(@Query("baralhoId") String baralhoId, @Header("Authorization") String token);

    // Editar flashcard
    @PUT("api/flashcards/{id}")
    Call<Flashcard> editarFlashcard(@Path("id") String id, @Body Flashcard flashcard, @Header("Authorization") String token);

    // Excluir flashcard
    @DELETE("api/flashcards/{id}")
    Call<Void> excluirFlashcard(@Path("id") String id, @Header("Authorization") String token);

    // ===== ENDPOINTS DE QUIZ =====

    // Gerar quiz
    @POST("api/quizzes/gerar/{baralhoId}")
    Call<List<QuestaoQuiz>> gerarQuiz(
            @Header("Authorization") String token,
            @Path("baralhoId") String baralhoId
    );



    // Buscar quiz por ID
    @GET("api/quizzes/{quizId}")
    Call<QuizModel> buscarQuiz(
            @Header("Authorization") String token,
            @Path("quizId") String quizId
    );

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

    // ===== ENDPOINTS PARA QUANTIDADES =====

    @GET("api/baralhos/quantidade")
    Call<Integer> getQuantidadeBaralhos(@Header("Authorization") String token);

    @GET("api/baralhos/quantidade-flashcards")
    Call<Integer> getQuantidadeFlashcards(@Header("Authorization") String token);

    // ===== ENDPOINTS PARA IMAGENS =====

    /**
     * Upload de imagem para baralho
     * Envia a imagem em Base64 para o servidor
     */
    @PUT("api/baralhos/{id}/imagem")
    Call<ImageUploadResponse> uploadImagemBaralho(
            @Path("id") String baralhoId,
            @Body ImageUploadRequest imageData,
            @Header("Authorization") String token
    );


    @GET("api/quizzes/historico/mensal")
    Call<List<RegistroRevisao>> getRevisoes(@Header("Authorization") String token);


    @DELETE("api/baralhos/{id}/imagem")
    Call<ResponseBody> deletarImagemBaralho(
            @Path("id") String baralhoId,
            @Header("Authorization") String token
    );

    // ===== CLASSES PARA REQUESTS E RESPONSES DE IMAGEM =====

    /**
     * Classe para request de upload de imagem
     */
    class ImageUploadRequest {
        private String imagemBase64;
        private String nomeArquivo;
        private String tipoImagem; // jpg, png, etc.

        public ImageUploadRequest() {}

        public ImageUploadRequest(String imagemBase64, String nomeArquivo) {
            this.imagemBase64 = imagemBase64;
            this.nomeArquivo = nomeArquivo;
            this.tipoImagem = "jpg"; // padrão
        }

        public ImageUploadRequest(String imagemBase64, String nomeArquivo, String tipoImagem) {
            this.imagemBase64 = imagemBase64;
            this.nomeArquivo = nomeArquivo;
            this.tipoImagem = tipoImagem;
        }

        // Getters e setters
        public String getImagemBase64() {
            return imagemBase64;
        }

        public void setImagemBase64(String imagemBase64) {
            this.imagemBase64 = imagemBase64;
        }

        public String getNomeArquivo() {
            return nomeArquivo;
        }

        public void setNomeArquivo(String nomeArquivo) {
            this.nomeArquivo = nomeArquivo;
        }

        public String getTipoImagem() {
            return tipoImagem;
        }

        public void setTipoImagem(String tipoImagem) {
            this.tipoImagem = tipoImagem;
        }
    }

    /**
     * Classe para response de upload de imagem
     */
    class ImageUploadResponse {
        private boolean sucesso;
        private String mensagem;
        private String urlImagem;
        private String nomeArquivo;

        public ImageUploadResponse() {}

        // Getters e setters
        public boolean isSucesso() {
            return sucesso;
        }

        public void setSucesso(boolean sucesso) {
            this.sucesso = sucesso;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getUrlImagem() {
            return urlImagem;
        }

        public void setUrlImagem(String urlImagem) {
            this.urlImagem = urlImagem;
        }

        public String getNomeArquivo() {
            return nomeArquivo;
        }

        public void setNomeArquivo(String nomeArquivo) {
            this.nomeArquivo = nomeArquivo;
        }
    }
}
