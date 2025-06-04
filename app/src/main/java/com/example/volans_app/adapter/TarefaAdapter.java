package com.example.volans_app.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Tarefa;
import com.example.volans_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private List<Tarefa> listaTarefas;
    private List<Tarefa> listaOriginal; // Para filtros
    private Context context;
    private OnTarefaClickListener listener;

    public interface OnTarefaClickListener {
        void onTarefaClick(Tarefa tarefa, int position);
        void onTarefaDelete(Tarefa tarefa, int position);
        void onTarefaEdit(Tarefa tarefa, int position);
        void onTarefaToggleComplete(Tarefa tarefa, int position);
    }

    public TarefaAdapter(List<Tarefa> listaTarefas, Context context) {
        this.listaTarefas = listaTarefas;
        this.listaOriginal = new ArrayList<>(listaTarefas); // Cópia para filtros
        this.context = context;
        if (context instanceof OnTarefaClickListener) {
            this.listener = (OnTarefaClickListener) context;
        }
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = listaTarefas.get(position);

        // Configurar dados básicos
        holder.tvTitulo.setText(tarefa.getTitulo());
        holder.tvDiaSemana.setText(tarefa.getDiaSemana());
        holder.tvHorario.setText(tarefa.getHorario());
        holder.tvDataLimite.setText("Entrega: " + tarefa.getDataLimite());

        // Configurar checkbox de conclusão
        if (holder.cbConcluida != null) {
            holder.cbConcluida.setOnCheckedChangeListener(null); // Evitar loops
            holder.cbConcluida.setChecked(tarefa.isConcluida());
            holder.cbConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tarefa.setConcluida(isChecked);
                if (listener != null) {
                    listener.onTarefaToggleComplete(tarefa, position);
                }
                updateTaskAppearance(holder, tarefa);
                animateCheckboxChange(holder.cbConcluida, isChecked);
            });
        }

        // Configurar modalidade
        if (tarefa.getModalidade().equals("Presencial")) {
            holder.ivModalidade.setImageResource(R.drawable.ic_presencial);
            holder.tvModalidade.setText("Presencial");
            holder.tvModalidade.setTextColor(ContextCompat.getColor(context, R.color.green_500));
        } else {
            holder.ivModalidade.setImageResource(R.drawable.ic_school);
            holder.tvModalidade.setText("EAD");
            holder.tvModalidade.setTextColor(ContextCompat.getColor(context, R.color.blue_500));
        }

        // Mostrar professor se disponível
        if (tarefa.getProfessor() != null && !tarefa.getProfessor().isEmpty()) {
            holder.tvProfessor.setText(tarefa.getProfessor());
            holder.tvProfessor.setVisibility(View.VISIBLE);
        } else {
            holder.tvProfessor.setVisibility(View.GONE);
        }

        // Verificar se a tarefa está atrasada e configurar indicadores visuais
        boolean isOverdue = isTaskOverdue(tarefa);
        configurarIndicadoresVisuais(holder, tarefa, isOverdue);

        // Atualizar aparência baseada no status
        updateTaskAppearance(holder, tarefa);

        // Click listeners com animações
        holder.itemView.setOnClickListener(v -> {
            animateItemClick(v);
            if (listener != null) {
                listener.onTarefaClick(tarefa, position);
            }
        });

        if (holder.ivEdit != null) {
            holder.ivEdit.setOnClickListener(v -> {
                animateActionButton(v);
                if (listener != null) {
                    listener.onTarefaEdit(tarefa, position);
                }
            });
        }

        if (holder.ivDelete != null) {
            holder.ivDelete.setOnClickListener(v -> {
                animateActionButton(v);
                if (listener != null) {
                    listener.onTarefaDelete(tarefa, position);
                }
            });
        }

        // Animação de entrada
        animateItemEntry(holder.itemView, position);
    }

    private void configurarIndicadoresVisuais(TarefaViewHolder holder, Tarefa tarefa, boolean isOverdue) {
        if (isOverdue && !tarefa.isConcluida()) {
            // Tarefa atrasada
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light));
            holder.tvDataLimite.setTextColor(ContextCompat.getColor(context, R.color.red_500));

            // Adicionar indicador de prioridade se existir
            if (holder.ivPrioridade != null) {
                holder.ivPrioridade.setVisibility(View.VISIBLE);
                holder.ivPrioridade.setImageResource(R.drawable.ic_warning);
                holder.ivPrioridade.setColorFilter(ContextCompat.getColor(context, R.color.red_500));
            }
        } else if (tarefa.isConcluida()) {
            // Tarefa concluída
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_light));
            holder.tvDataLimite.setTextColor(ContextCompat.getColor(context, R.color.gray_500));

            if (holder.ivPrioridade != null) {
                holder.ivPrioridade.setVisibility(View.VISIBLE);
                holder.ivPrioridade.setImageResource(R.drawable.ic_check);
                holder.ivPrioridade.setColorFilter(ContextCompat.getColor(context, R.color.green_500));
            }
        } else {
            // Tarefa normal
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.tvDataLimite.setTextColor(ContextCompat.getColor(context, R.color.red_500));

            if (holder.ivPrioridade != null) {
                holder.ivPrioridade.setVisibility(View.GONE);
            }
        }
    }

    private void updateTaskAppearance(TarefaViewHolder holder, Tarefa tarefa) {
        if (tarefa.isConcluida()) {
            // Tarefa concluída - aparência "riscada" e opaca
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            setViewsAlpha(holder, 0.6f);
        } else {
            // Tarefa pendente - aparência normal
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            setViewsAlpha(holder, 1.0f);
        }
    }

    private void setViewsAlpha(TarefaViewHolder holder, float alpha) {
        holder.tvTitulo.setAlpha(alpha);
        holder.tvDiaSemana.setAlpha(alpha);
        holder.tvHorario.setAlpha(alpha);
        holder.tvDataLimite.setAlpha(alpha);
        holder.tvModalidade.setAlpha(alpha);
        holder.tvProfessor.setAlpha(alpha);
        holder.ivModalidade.setAlpha(alpha);
    }

    private boolean isTaskOverdue(Tarefa tarefa) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataLimite = sdf.parse(tarefa.getDataLimite());
            Date hoje = new Date();

            // Comparar apenas as datas, ignorando o horário
            SimpleDateFormat dateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataLimiteOnly = dateOnly.parse(dateOnly.format(dataLimite));
            Date hojeOnly = dateOnly.parse(dateOnly.format(hoje));

            return hojeOnly.after(dataLimiteOnly);
        } catch (ParseException e) {
            return false;
        }
    }

    // Animações melhoradas
    private void animateItemEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.setStartDelay(position * 80); // Delay escalonado
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(value);
            view.setTranslationY(50f * (1f - value));
            view.setScaleX(0.9f + (0.1f * value));
            view.setScaleY(0.9f + (0.1f * value));
        });
        animator.start();
    }

    private void animateItemClick(View view) {
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

    private void animateActionButton(View view) {
        view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateCheckboxChange(CheckBox checkBox, boolean isChecked) {
        if (isChecked) {
            checkBox.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        checkBox.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return listaTarefas.size();
    }

    public void removeTarefa(int position) {
        if (position >= 0 && position < listaTarefas.size()) {
            Tarefa tarefaRemovida = listaTarefas.get(position);
            listaTarefas.remove(position);
            listaOriginal.remove(tarefaRemovida);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaTarefas.size());
        }
    }

    public void updateTarefa(int position, Tarefa tarefaAtualizada) {
        if (position >= 0 && position < listaTarefas.size()) {
            listaTarefas.set(position, tarefaAtualizada);
            // Atualizar também na lista original
            int originalIndex = listaOriginal.indexOf(listaTarefas.get(position));
            if (originalIndex != -1) {
                listaOriginal.set(originalIndex, tarefaAtualizada);
            }
            notifyItemChanged(position);
        }
    }

    public void addTarefa(Tarefa novaTarefa) {
        listaTarefas.add(novaTarefa);
        listaOriginal.add(novaTarefa);
        notifyItemInserted(listaTarefas.size() - 1);
    }

    // Métodos para filtros melhorados
    public void filterByStatus(String status) {
        listaTarefas.clear();

        switch (status) {
            case "Todas":
                listaTarefas.addAll(listaOriginal);
                break;
            case "Pendentes":
                for (Tarefa tarefa : listaOriginal) {
                    if (!tarefa.isConcluida()) {
                        listaTarefas.add(tarefa);
                    }
                }
                break;
            case "Concluídas":
                for (Tarefa tarefa : listaOriginal) {
                    if (tarefa.isConcluida()) {
                        listaTarefas.add(tarefa);
                    }
                }
                break;
            case "Atrasadas":
                for (Tarefa tarefa : listaOriginal) {
                    if (!tarefa.isConcluida() && isTaskOverdue(tarefa)) {
                        listaTarefas.add(tarefa);
                    }
                }
                break;
        }

        notifyDataSetChanged();
    }

    public void updateOriginalList(List<Tarefa> novaLista) {
        this.listaOriginal.clear();
        this.listaOriginal.addAll(novaLista);
        this.listaTarefas.clear();
        this.listaTarefas.addAll(novaLista);
        notifyDataSetChanged();
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CheckBox cbConcluida;
        TextView tvTitulo, tvDiaSemana, tvHorario, tvDataLimite, tvModalidade, tvProfessor;
        ImageView ivModalidade, ivEdit, ivDelete, ivPrioridade;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);

            // Views obrigatórias (baseadas no item_tarefa.xml original)
            cardView = (CardView) itemView;
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDiaSemana = itemView.findViewById(R.id.tvDiaSemana);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            tvDataLimite = itemView.findViewById(R.id.tvDataLimite);
            tvModalidade = itemView.findViewById(R.id.tvModalidade);
            tvProfessor = itemView.findViewById(R.id.tvProfessor);
            ivModalidade = itemView.findViewById(R.id.ivModalidade);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            // Views opcionais (podem não existir em todos os layouts)
            cbConcluida = itemView.findViewById(R.id.cbConcluida);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivPrioridade = itemView.findViewById(R.id.ivPrioridade);
        }
    }
}