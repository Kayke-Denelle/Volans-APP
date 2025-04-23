package com.example.volans_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Baralho;
import com.example.volans_app.R;

import java.util.List;

public class BaralhoAdapter extends RecyclerView.Adapter<BaralhoAdapter.BaralhoViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Baralho baralho);
    }

    private List<Baralho> lista;
    private OnItemClickListener listener;

    public BaralhoAdapter(List<Baralho> lista, OnItemClickListener listener) {
        this.lista = lista;
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
        holder.tvNome.setText(baralho.getNome());
        holder.tvDescricao.setText(baralho.getDescricao());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(baralho));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDescricao;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
        }
    }
}
