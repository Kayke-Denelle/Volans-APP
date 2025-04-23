package com.example.volans_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.R;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> lista;

    public FlashcardAdapter(List<Flashcard> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = lista.get(position);

        holder.tvPergunta.setText(flashcard.getPergunta());
        holder.tvResposta.setText(flashcard.getResposta());

        if (flashcard.isMostrandoResposta()) {
            holder.tvPergunta.setVisibility(View.GONE);
            holder.tvResposta.setVisibility(View.VISIBLE);
        } else {
            holder.tvPergunta.setVisibility(View.VISIBLE);
            holder.tvResposta.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            flashcard.setMostrandoResposta(!flashcard.isMostrandoResposta());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView tvPergunta, tvResposta;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPergunta = itemView.findViewById(R.id.tvPergunta);
            tvResposta = itemView.findViewById(R.id.tvResposta);
        }
    }
}
