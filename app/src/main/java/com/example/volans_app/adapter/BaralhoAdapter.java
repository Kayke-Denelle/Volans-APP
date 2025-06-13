package com.example.volans_app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BaralhoAdapter extends RecyclerView.Adapter<BaralhoAdapter.BaralhoViewHolder> {

    private static final String TAG = "BaralhoAdapter";
    private Context context;
    private List<Baralho> baralhos;
    private OnItemClickListener listener;
    private OnBaralhoOptionsListener optionsListener;
    private boolean somenteQuiz;

    public interface OnItemClickListener {
        void onBaralhoClick(Baralho baralho);
    }

    public interface OnBaralhoOptionsListener {
        void onEditBaralho(Baralho baralho);
        void onDeleteBaralho(Baralho baralho);
    }

    public BaralhoAdapter(Context context, List<Baralho> baralhos, OnItemClickListener listener) {
        this.context = context;
        this.baralhos = baralhos;
        this.listener = listener;
        this.somenteQuiz = false;
    }

    public BaralhoAdapter(List<Baralho> baralhos, OnItemClickListener listener, boolean somenteQuiz) {
        this.context = null;
        this.baralhos = baralhos;
        this.listener = listener;
        this.somenteQuiz = somenteQuiz;
    }

    public void setOnBaralhoOptionsListener(OnBaralhoOptionsListener optionsListener) {
        this.optionsListener = optionsListener;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (context == null) {
            context = parent.getContext();
            view = LayoutInflater.from(context).inflate(R.layout.item_baralho_grid, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_baralho_grid, parent, false);
        }
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = baralhos.get(position);
        holder.bind(baralho, listener, optionsListener, somenteQuiz);
    }

    @Override
    public int getItemCount() {
        return baralhos.size();
    }

    static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBaralhoImage;
        private TextView tvNomeBaralho;
        private TextView tvDescricaoBaralho;
        private ImageButton btnOptions;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBaralhoImage = itemView.findViewById(R.id.ivBaralhoImage);
            tvNomeBaralho = itemView.findViewById(R.id.tvNomeBaralho);
            tvDescricaoBaralho = itemView.findViewById(R.id.tvDescricaoBaralho);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }

        public void bind(Baralho baralho, OnItemClickListener listener, OnBaralhoOptionsListener optionsListener, boolean somenteQuiz) {
            tvNomeBaralho.setText(baralho.getNome());
            tvDescricaoBaralho.setText(baralho.getDescricao());

            // Carregar imagem personalizada se existir
            if (baralho.getImagemUrl() != null && !baralho.getImagemUrl().isEmpty()) {
                Log.d(TAG, "Tentando carregar imagem para baralho: " + baralho.getNome() + ", path: " + baralho.getImagemUrl());
                loadBaralhoImage(baralho.getImagemUrl());
            } else {
                Log.d(TAG, "Baralho sem imagem: " + baralho.getNome());
                // Usar ícone padrão
                setDefaultImage();
            }

            // Configurar clique no item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBaralhoClick(baralho);
                }
            });

            // Configurar menu de opções
            if (btnOptions != null) {
                btnOptions.setVisibility(View.VISIBLE);
                btnOptions.setOnClickListener(v -> {
                    if (optionsListener != null) {
                        showOptionsMenu(v, baralho, optionsListener);
                    }
                });
            }

            // Animação de entrada
            itemView.setAlpha(0f);
            itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(getAdapterPosition() * 100)
                    .start();
        }

        private void setDefaultImage() {
            ivBaralhoImage.setImageResource(R.drawable.ic_play);
            ivBaralhoImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ivBaralhoImage.setPadding(16, 16, 16, 16);
        }

        private void loadBaralhoImage(String imagePath) {
            Context context = itemView.getContext();

            if (imagePath == null || imagePath.trim().isEmpty()) {
                Log.d(TAG, "Path da imagem é nulo ou vazio");
                setDefaultImage();
                return;
            }

            try {
                Log.d(TAG, "Tentando carregar imagem: " + imagePath);

                // Primeiro, verificar se é um path absoluto ou relativo
                File imageFile;
                if (imagePath.startsWith("/")) {
                    // Path absoluto
                    imageFile = new File(imagePath);
                } else {
                    // Path relativo - procurar no diretório de arquivos da app
                    imageFile = new File(context.getFilesDir(), imagePath);
                }

                Log.d(TAG, "Caminho completo do arquivo: " + imageFile.getAbsolutePath());

                if (imageFile.exists()) {
                    Log.d(TAG, "Arquivo existe: " + imageFile.getAbsolutePath() + " (" + imageFile.length() + " bytes)");

                    // Verificar se o arquivo não está vazio
                    if (imageFile.length() == 0) {
                        Log.e(TAG, "Arquivo de imagem está vazio");
                        setDefaultImage();
                        return;
                    }

                    // Tentar decodificar o bitmap com opções otimizadas
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    // Primeiro, obter as dimensões da imagem sem carregá-la na memória
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    if (options.outWidth == -1 || options.outHeight == -1) {
                        Log.e(TAG, "Arquivo não é uma imagem válida");
                        setDefaultImage();
                        return;
                    }

                    Log.d(TAG, "Dimensões da imagem: " + options.outWidth + "x" + options.outHeight);

                    // Calcular sample size para evitar OutOfMemoryError
                    options.inSampleSize = calculateInSampleSize(options, 400, 400); // Aumentei o tamanho alvo
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565; // Usar menos memória

                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    if (bitmap != null) {
                        Log.d(TAG, "Bitmap carregado com sucesso: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                        // Verificar se o bitmap não está corrompido (não é apenas branco)
                        if (isBitmapValid(bitmap)) {
                            ivBaralhoImage.setImageBitmap(bitmap);
                            ivBaralhoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            ivBaralhoImage.setPadding(0, 0, 0, 0);
                            return;
                        } else {
                            Log.e(TAG, "Bitmap parece estar corrompido ou vazio");
                            bitmap.recycle();
                        }
                    } else {
                        Log.e(TAG, "Falha ao decodificar bitmap do arquivo");
                    }
                } else {
                    Log.e(TAG, "Arquivo de imagem não existe: " + imageFile.getAbsolutePath());

                    // Listar arquivos no diretório para debug
                    File filesDir = context.getFilesDir();
                    File[] files = filesDir.listFiles();
                    if (files != null) {
                        Log.d(TAG, "Arquivos no diretório (" + filesDir.getAbsolutePath() + "):");
                        for (File file : files) {
                            Log.d(TAG, "  - " + file.getName() + " (" + file.length() + " bytes)");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar imagem: " + imagePath, e);
            }

            // Fallback para ícone padrão
            Log.d(TAG, "Usando ícone padrão");
            setDefaultImage();
        }

        private boolean isBitmapValid(Bitmap bitmap) {
            if (bitmap == null) return false;

            // Verificar se o bitmap não é apenas uma cor sólida (indicativo de corrupção)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width < 10 || height < 10) return false; // Muito pequeno

            // Verificar alguns pixels para ver se há variação
            int centerPixel = bitmap.getPixel(width / 2, height / 2);
            int cornerPixel = bitmap.getPixel(0, 0);
            int otherCornerPixel = bitmap.getPixel(width - 1, height - 1);

            // Se todos os pixels são iguais, provavelmente está corrompido
            if (centerPixel == cornerPixel && cornerPixel == otherCornerPixel) {
                // Verificar se é branco puro (indicativo de corrupção comum)
                if (centerPixel == Color.WHITE || centerPixel == Color.TRANSPARENT) {
                    Log.w(TAG, "Bitmap parece ser uma cor sólida (possivelmente corrompido)");
                    return false;
                }
            }

            return true;
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Dimensões originais da imagem
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calcular o maior inSampleSize que mantenha ambas as dimensões
                // maiores que as dimensões solicitadas
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        private void showOptionsMenu(View view, Baralho baralho, OnBaralhoOptionsListener optionsListener) {
            try {
                // CORREÇÃO: Criar o PopupMenu com o contexto correto
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.getMenuInflater().inflate(R.menu.baralho_options_menu, popup.getMenu());

                // Configurar o fundo branco arredondado para o popup
                try {
                    Field field = popup.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);

                    // Tentar definir o background branco arredondado
                    Method getPopup = menuPopupHelper.getClass().getDeclaredMethod("getPopup");
                    getPopup.setAccessible(true);
                    Object popupWindow = getPopup.invoke(menuPopupHelper);
                    if (popupWindow != null) {
                        // Criar um drawable arredondado branco
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setColor(Color.WHITE);
                        drawable.setCornerRadius(16); // 16dp de raio para cantos arredondados

                        popupWindow.getClass().getDeclaredMethod("setBackgroundDrawable", android.graphics.drawable.Drawable.class)
                                .invoke(popupWindow, drawable);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao configurar ícones e fundo do menu", e);
                    // Fallback para fundo branco simples
                    try {
                        Method getPopup = popup.getClass().getDeclaredMethod("getPopup");
                        getPopup.setAccessible(true);
                        Object popupObj = getPopup.invoke(popup);
                        if (popupObj != null) {
                            popupObj.getClass().getDeclaredMethod("setBackgroundDrawable", android.graphics.drawable.Drawable.class)
                                    .invoke(popupObj, new ColorDrawable(Color.WHITE));
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Erro no fallback para fundo branco", ex);
                    }
                }

                // Configurar os listeners de clique para as opções do menu
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit) {
                        // Adicionar animação ao botão antes de executar ação
                        view.animate()
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    view.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
                                            .setDuration(100)
                                            .start();
                                    optionsListener.onEditBaralho(baralho);
                                })
                                .start();
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        // Adicionar animação ao botão antes de executar ação
                        view.animate()
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    view.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
                                            .setDuration(100)
                                            .start();
                                    optionsListener.onDeleteBaralho(baralho);
                                })
                                .start();
                        return true;
                    }
                    return false;
                });

                // Mostrar o menu
                popup.show();

                // Registrar no log que o menu foi exibido
                Log.d(TAG, "Menu de opções exibido para baralho: " + baralho.getNome());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao exibir menu de opções", e);
            }
        }
    }
}
