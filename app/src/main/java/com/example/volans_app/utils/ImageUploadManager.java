package com.example.volans_app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUploadManager {

    private static final String TAG = "ImageUploadManager";
    private static final int MAX_IMAGE_SIZE = 800; // Tamanho máximo em pixels
    private static final int JPEG_QUALITY = 85; // Qualidade do JPEG (0-100)

    /**
     * Converte uma URI de imagem em Base64 otimizada
     */
    public static String convertImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap == null) {
                Log.e(TAG, "Não foi possível decodificar a imagem");
                return null;
            }

            // Redimensionar mantendo proporção
            Bitmap resizedBitmap = resizeImageMaintainAspectRatio(originalBitmap, MAX_IMAGE_SIZE);

            // Converter para Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Limpar recursos
            originalBitmap.recycle();
            resizedBitmap.recycle();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter imagem para Base64", e);
            return null;
        }
    }

    /**
     * Redimensiona a imagem mantendo a proporção
     */
    private static Bitmap resizeImageMaintainAspectRatio(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Se a imagem já é menor que o tamanho máximo, retorna a original
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        // Calcular nova dimensão mantendo proporção
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Gera um nome único para a imagem baseado no ID do usuário e timestamp
     */
    public static String generateImageFileName(String userId, String baralhoId) {
        long timestamp = System.currentTimeMillis();
        return "baralho_" + userId + "_" + baralhoId + "_" + timestamp + ".jpg";
    }
}
