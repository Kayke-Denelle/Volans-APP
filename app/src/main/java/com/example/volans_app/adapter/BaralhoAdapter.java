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

    public interface OnItemClickListener {
        void onBaralhoClick(Baralho baralho);  // Renomeado para ficar mais claro
        void onQuizClick(Baralho baralho);
    }

    private List<Baralho> lista;
    private final OnItemClickListener listener;

    public BaralhoAdapter(List<Baralho> lista, OnItemClickListener listener) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.listener = listener;
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
        holder.bind(baralho, listener);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void atualizarLista(List<Baralho> novaLista) {
        this.lista = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Baralho getItem(int position) {
        return lista.get(position);
    }

    static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNome;
        private final TextView tvDescricao;
        private final Button btnAcessarBaralho;
        private final Button btnIniciarQuiz;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            btnAcessarBaralho = itemView.findViewById(R.id.btnAcessarBaralho);
            btnIniciarQuiz = itemView.findViewById(R.id.btnIniciarQuiz);
        }

        public void bind(Baralho baralho, OnItemClickListener listener) {
            tvNome.setText(baralho.getNome());
            tvDescricao.setText(baralho.getDescricao());

            // Configuração dos cliques nos botões
            btnAcessarBaralho.setOnClickListener(v -> listener.onBaralhoClick(baralho));
            btnIniciarQuiz.setOnClickListener(v -> listener.onQuizClick(baralho));

            // Removemos o clique da view inteira
        }
    }

}

