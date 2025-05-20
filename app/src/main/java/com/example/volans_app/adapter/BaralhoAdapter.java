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

    private List<Baralho> lista = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onBaralhoClick(Baralho baralho);
        void onQuizClick(Baralho baralho);
    }

    public BaralhoAdapter(List<Baralho> lista, OnItemClickListener listener) {
        if (lista != null) {
            this.lista = lista;
        }
        this.listener = listener;
    }

    public void setData(List<Baralho> novaLista) {
        this.lista = new ArrayList<>(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baralho, parent, false);
        return new BaralhoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = lista.get(position);
        holder.tvNome.setText(baralho.getNome());
        holder.tvDescricao.setText(baralho.getDescricao());

        holder.btnAcessarBaralho.setOnClickListener(v -> listener.onBaralhoClick(baralho));
        holder.btnIniciarQuiz.setOnClickListener(v -> listener.onQuizClick(baralho));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDescricao;
        Button btnAcessarBaralho, btnIniciarQuiz;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            btnAcessarBaralho = itemView.findViewById(R.id.btnAcessarBaralho);
            btnIniciarQuiz = itemView.findViewById(R.id.btnIniciarQuiz);
        }
    }
}
