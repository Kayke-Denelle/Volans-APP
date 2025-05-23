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

import java.util.ArrayList;
import java.util.List;

public class BaralhoAdapter extends RecyclerView.Adapter<BaralhoAdapter.BaralhoViewHolder> {

    private List<Baralho> baralhos;
    private OnItemClickListener listener;
    private boolean somenteQuiz;

    public interface OnItemClickListener {
        void onBaralhoClick(Baralho baralho);
    }

    public BaralhoAdapter(List<Baralho> baralhos, OnItemClickListener listener, boolean somenteQuiz) {
        this.baralhos = baralhos;
        this.listener = listener;
        this.somenteQuiz = somenteQuiz;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baralho, parent, false);
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = baralhos.get(position);
        holder.tvNome.setText(baralho.getNome());
        holder.tvDescricao.setText(baralho.getDescricao());

        holder.btnAcessarBaralho.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBaralhoClick(baralho);
            }
        });
    }

    @Override
    public int getItemCount() {
        return baralhos.size();
    }

    public static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDescricao;
        Button btnAcessarBaralho;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            btnAcessarBaralho = itemView.findViewById(R.id.btnAcessarBaralho);
        }
    }
}


