package com.example.volans_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.R;

import java.util.List;

public class AtividadeBaralhoAdapter extends RecyclerView.Adapter<AtividadeBaralhoAdapter.BaralhoViewHolder> {

    private List<Baralho> baralhos;
    private OnQuizClickListener listener;

    public interface OnQuizClickListener {
        void onQuizClick(Baralho baralho);
    }

    public AtividadeBaralhoAdapter(List<Baralho> baralhos, OnQuizClickListener listener) {
        this.baralhos = baralhos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baralho_atividade, parent, false); // esse layout tem o botão de Iniciar Quiz
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = baralhos.get(position);
        holder.tvNome.setText(baralho.getNome());
        holder.tvDescricao.setText(baralho.getDescricao());

        holder.btnIniciarQuiz.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizClick(baralho);
            }
        });
    }

    @Override
    public int getItemCount() {
        return baralhos.size();
    }

    public static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDescricao;
        Button btnIniciarQuiz;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeBaralho);
            tvDescricao = itemView.findViewById(R.id.tvDescricaoBaralho);
            btnIniciarQuiz = itemView.findViewById(R.id.btnIniciarQuiz);
        }
    }
}

