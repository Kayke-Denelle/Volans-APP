package com.example.volans_app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
                loadBaralhoImage(baralho.getImagemUrl());
            } else {
                // Usar ícone padrão
                ivBaralhoImage.setImageResource(R.drawable.ic_play);
                ivBaralhoImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ivBaralhoImage.setPadding(16, 16, 16, 16);
            }

            // Configurar clique no item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBaralhoClick(baralho);
                }
            });

            // Configurar menu de opções
            btnOptions.setOnClickListener(v -> {
                if (optionsListener != null) {
                    showOptionsMenu(v, baralho, optionsListener);
                }
            });

            // Animação de entrada
            itemView.setAlpha(0f);
            itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(getAdapterPosition() * 100)
                    .start();
        }

        private void loadBaralhoImage(String imagePath) {
            Context context = itemView.getContext();
            try {
                Log.d(TAG, "Tentando carregar imagem: " + imagePath);
                File imageFile = new File(context.getFilesDir(), imagePath);

                if (imageFile.exists()) {
                    Log.d(TAG, "Arquivo existe: " + imageFile.getAbsolutePath());
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    if (bitmap != null) {
                        ivBaralhoImage.setImageBitmap(bitmap);
                        ivBaralhoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ivBaralhoImage.setPadding(0, 0, 0, 0);
                        return;
                    } else {
                        Log.e(TAG, "Falha ao decodificar bitmap do arquivo");
                    }
                } else {
                    Log.e(TAG, "Arquivo de imagem não existe: " + imageFile.getAbsolutePath());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar imagem", e);
                e.printStackTrace();
            }

            // Fallback para ícone padrão
            ivBaralhoImage.setImageResource(R.drawable.ic_play);
            ivBaralhoImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ivBaralhoImage.setPadding(16, 16, 16, 16);
        }

        private void showOptionsMenu(View view, Baralho baralho, OnBaralhoOptionsListener optionsListener) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.baralho_options_menu, popup.getMenu());

            // Aplicar estilo customizado ao popup
            try {
                // Forçar exibição dos ícones
                java.lang.reflect.Field fieldPopup = popup.getClass().getDeclaredField("mPopup");
                fieldPopup.setAccessible(true);
                Object menuPopupHelper = fieldPopup.get(popup);
                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuPopupHelper, true);

                // Customizar aparência do popup
                java.lang.reflect.Method getPopup = classPopupHelper.getMethod("getPopup");
                Object listPopupWindow = getPopup.invoke(menuPopupHelper);
                if (listPopupWindow != null) {
                    java.lang.reflect.Method setBackgroundDrawable = listPopupWindow.getClass().getMethod("setBackgroundDrawable", android.graphics.drawable.Drawable.class);
                    setBackgroundDrawable.invoke(listPopupWindow, view.getContext().getResources().getDrawable(R.drawable.popup_menu_background));

                    // Definir elevação
                    java.lang.reflect.Method setElevation = listPopupWindow.getClass().getMethod("setElevation", float.class);
                    setElevation.invoke(listPopupWindow, 8f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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

            popup.show();
        }
    }
}