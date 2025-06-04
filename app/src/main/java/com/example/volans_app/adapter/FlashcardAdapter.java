package com.example.volans_app.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Flashcard;
import com.example.volans_app.R;

import java.util.Collections;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> lista;
    private OnItemClickListener onItemClickListener;
    private OnStartDragListener onStartDragListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onItemClick(int position);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public FlashcardAdapter(List<Flashcard> lista) {
        this.lista = lista;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.onStartDragListener = listener;
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
        holder.bind(flashcard, position);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Métodos para drag & drop (sem interface externa)
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(lista, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(lista, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void onItemDismiss(int position) {
        // Implementar se quiser swipe para deletar
        if (onItemClickListener != null) {
            onItemClickListener.onDeleteClick(position);
        }
    }

    // Método para remover item da lista
    public void removeItem(int position) {
        if (position >= 0 && position < lista.size()) {
            lista.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Método para obter item em uma posição
    public Flashcard getItem(int position) {
        if (position >= 0 && position < lista.size()) {
            return lista.get(position);
        }
        return null;
    }

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView tvPergunta, tvResposta;
        ImageView btnDelete, btnDrag;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPergunta = itemView.findViewById(R.id.tvPergunta);
            tvResposta = itemView.findViewById(R.id.tvResposta);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDrag = itemView.findViewById(R.id.btnDrag);
        }

        public void bind(Flashcard flashcard, int position) {
            tvPergunta.setText(flashcard.getPergunta());
            tvResposta.setText(flashcard.getResposta());

            // Lógica original de mostrar/esconder pergunta e resposta
            if (flashcard.isMostrandoResposta()) {
                tvPergunta.setVisibility(View.GONE);
                tvResposta.setVisibility(View.VISIBLE);
            } else {
                tvPergunta.setVisibility(View.VISIBLE);
                tvResposta.setVisibility(View.GONE);
            }

            // Click listener para excluir
            btnDelete.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    // Animação de clique
                    v.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100)
                                        .start();
                            })
                            .start();

                    onItemClickListener.onDeleteClick(position);
                }
            });

            // Touch listener para drag
            btnDrag.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (onStartDragListener != null) {
                        onStartDragListener.onStartDrag(this);
                    }
                }
                return false;
            });

            // Click listener para o item inteiro (funcionalidade original de flip)
            itemView.setOnClickListener(v -> {
                // Manter a funcionalidade original de mostrar/esconder resposta
                flashcard.setMostrandoResposta(!flashcard.isMostrandoResposta());
                notifyItemChanged(position);

                // Também chamar o listener se existir
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }
    }
}