package com.example.volans_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.R;

import java.util.List;
import java.util.Random;

public class BaralhoGridAdapter extends RecyclerView.Adapter<BaralhoGridAdapter.BaralhoViewHolder> {

    private final Context context;
    private final List<Baralho> listaBaralhos;
    private final OnBaralhoClickListener listener;
    private final OnImageClickListener imageClickListener;

    public interface OnBaralhoClickListener {
        void onBaralhoClick(Baralho baralho);
    }

    public interface OnImageClickListener {
        void onImageClick(Baralho baralho, int position);
    }

    public BaralhoGridAdapter(Context context, List<Baralho> listaBaralhos,
                              OnBaralhoClickListener listener, OnImageClickListener imageClickListener) {
        this.context = context;
        this.listaBaralhos = listaBaralhos;
        this.listener = listener;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baralho_grid, parent, false);
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = listaBaralhos.get(position);
        holder.bind(baralho, position);
    }

    @Override
    public int getItemCount() {
        return listaBaralhos.size();
    }

    class BaralhoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNomeBaralho;
        private final TextView tvDescricaoBaralho;
        private final TextView tvQuantidadeFlashcards;
        private final ImageView ivBaralhoBackground;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeBaralho = itemView.findViewById(R.id.tvNomeBaralho);
            tvDescricaoBaralho = itemView.findViewById(R.id.tvDescricaoBaralho);
            tvQuantidadeFlashcards = itemView.findViewById(R.id.tvQuantidadeFlashcards);
            ivBaralhoBackground = itemView.findViewById(R.id.ivBaralhoBackground);
        }

        public void bind(Baralho baralho, int position) {
            tvNomeBaralho.setText(baralho.getNome());
            tvDescricaoBaralho.setText(baralho.getDescricao());

            // Quantidade de flashcards (simulado por enquanto)
            int qtdFlashcards = new Random().nextInt(50) + 1;
            tvQuantidadeFlashcards.setText(qtdFlashcards + " cards");

            // Carregar imagem com proporção fixa
            loadBaralhoImage(baralho);

            // Configurar clique no card (abre o baralho)
            itemView.setOnClickListener(v -> {
                animateCardClick(v);
                listener.onBaralhoClick(baralho);
            });

            // Configurar clique na imagem (abre galeria)
            ivBaralhoBackground.setOnClickListener(v -> {
                animateImageClick(v);
                imageClickListener.onImageClick(baralho, position);
            });

            // Animação de entrada
            animateItemAppearance(itemView, position);
        }

        private void loadBaralhoImage(Baralho baralho) {
            // Configurações do Glide para manter proporção e qualidade
            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.logo_volans)
                    .error(R.drawable.logo_volans)
                    .override(400, 400) // Tamanho fixo para otimização
                    .centerCrop();

            if (baralho.getImagemUrl() != null && !baralho.getImagemUrl().isEmpty()) {
                // Carregar imagem personalizada do servidor
                Glide.with(context)
                        .load(baralho.getImagemUrl())
                        .apply(options)
                        .into(ivBaralhoBackground);
            } else {
                // Usar logo padrão
                Glide.with(context)
                        .load(R.drawable.logo_volans)
                        .apply(options)
                        .into(ivBaralhoBackground);
            }
        }

        private void animateCardClick(View view) {
            view.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }

        private void animateImageClick(View view) {
            view.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }

        private void animateItemAppearance(View view, int position) {
            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(position * 50)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}
