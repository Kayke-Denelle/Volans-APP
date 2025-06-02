package com.example.volans_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.R;

import java.util.List;

public class BaralhoGridAdapter extends RecyclerView.Adapter<BaralhoGridAdapter.BaralhoViewHolder> {

    private Context context;
    private List<Baralho> baralhos;
    private OnItemClickListener listener;
    private boolean somenteQuiz;

    public interface OnItemClickListener {
        void onBaralhoClick(Baralho baralho);
    }

    // Construtor principal usado no BaralhoActivity
    public BaralhoGridAdapter(Context context, List<Baralho> baralhos, OnItemClickListener listener) {
        this.context = context;
        this.baralhos = baralhos;
        this.listener = listener;
        this.somenteQuiz = false;
    }

    // Construtor alternativo com somenteQuiz
    public BaralhoGridAdapter(Context context, List<Baralho> baralhos, OnItemClickListener listener, boolean somenteQuiz) {
        this.context = context;
        this.baralhos = baralhos;
        this.listener = listener;
        this.somenteQuiz = somenteQuiz;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_baralho_grid, parent, false);
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = baralhos.get(position);
        holder.bind(baralho, listener, somenteQuiz);
    }

    @Override
    public int getItemCount() {
        return baralhos.size();
    }

    static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBaralhoImage;
        private TextView tvNomeBaralho;
        private TextView tvDescricaoBaralho;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBaralhoImage = itemView.findViewById(R.id.ivBaralhoImage);
            tvNomeBaralho = itemView.findViewById(R.id.tvNomeBaralho);
            tvDescricaoBaralho = itemView.findViewById(R.id.tvDescricaoBaralho);
        }

        public void bind(Baralho baralho, OnItemClickListener listener, boolean somenteQuiz) {
            tvNomeBaralho.setText(baralho.getNome());
            tvDescricaoBaralho.setText(baralho.getDescricao());

            // Usar ícone padrão
            ivBaralhoImage.setImageResource(R.drawable.ic_play);

            // Configurar clique no item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBaralhoClick(baralho);
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
    }
}
