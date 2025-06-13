package com.example.volans_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.volans_app.DTO.Tarefa;
import com.example.volans_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private List<Tarefa> tarefaList;
    private List<Tarefa> tarefaListFull;
    private Context context;
    private OnTarefaClickListener listener;

    public TarefaAdapter(List<Tarefa> tarefaList, Context context) {
        this.tarefaList = tarefaList;
        this.tarefaListFull = new ArrayList<>(tarefaList);
        this.context = context;

        if (context instanceof OnTarefaClickListener) {
            this.listener = (OnTarefaClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTarefaClickListener");
        }
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = tarefaList.get(position);

        // Configurar dados básicos
        holder.tvTituloTarefa.setText(tarefa.getTitulo());
        holder.tvDetalhes.setText(tarefa.getDiaSemana() + " • " + tarefa.getHorario());
        holder.tvDataLimite.setText("Entrega: " + tarefa.getDataLimite());
        holder.tvProfessor.setText(tarefa.getProfessor().isEmpty() ? "Sem professor" : tarefa.getProfessor());
        holder.tvModalidade.setText(tarefa.getModalidade());

        // Configurar dias restantes
        int diasRestantes = calcularDiasRestantes(tarefa.getDataLimite());
        if (diasRestantes > 0) {
            holder.tvDiasRestantes.setText(diasRestantes + " dias");
            holder.tvDiasRestantes.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvDiasRestantes.setBackgroundResource(R.drawable.days_badge_background);
            holder.tvDiasRestantes.setVisibility(View.VISIBLE);
        } else if (diasRestantes == 0) {
            holder.tvDiasRestantes.setText("Hoje");
            holder.tvDiasRestantes.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvDiasRestantes.setBackgroundResource(R.drawable.today_badge_background);
            holder.tvDiasRestantes.setVisibility(View.VISIBLE);
        } else {
            holder.tvDiasRestantes.setText("Atrasado");
            holder.tvDiasRestantes.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvDiasRestantes.setBackgroundResource(R.drawable.overdue_badge_background);
            holder.tvDiasRestantes.setVisibility(View.VISIBLE);
        }

        // Configurar status com cores mais legíveis
        holder.checkBox.setChecked(tarefa.isConcluida());
        if (tarefa.isConcluida()) {
            holder.tvStatus.setText("Concluída");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvStatus.setBackgroundResource(R.drawable.success_badge_background);
        } else {
            if (diasRestantes < 0) {
                holder.tvStatus.setText("Atrasada");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
                holder.tvStatus.setBackgroundResource(R.drawable.overdue_badge_background);
            } else {
                holder.tvStatus.setText("Pendente");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
                holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background);
            }
        }

        // Configurar listeners
        holder.checkBox.setOnClickListener(v -> {
            boolean isChecked = holder.checkBox.isChecked();
            tarefa.setStatus(isChecked ? "Concluída" : "Pendente");

            // Atualizar UI
            if (isChecked) {
                holder.tvStatus.setText("Concluída");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
                holder.tvStatus.setBackgroundResource(R.drawable.success_badge_background);
            } else {
                int dias = calcularDiasRestantes(tarefa.getDataLimite());
                if (dias < 0) {
                    holder.tvStatus.setText("Atrasada");
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
                    holder.tvStatus.setBackgroundResource(R.drawable.overdue_badge_background);
                } else {
                    holder.tvStatus.setText("Pendente");
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background);
                }
            }

            if (listener != null) {
                listener.onTarefaToggleComplete(tarefa, holder.getAdapterPosition());
            }
        });

        holder.btnEditTarefa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTarefaEdit(tarefa, holder.getAdapterPosition());
            }
        });

        holder.btnDeleteTarefa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTarefaDelete(tarefa, holder.getAdapterPosition());
            }
        });

        // Configurar clique no item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTarefaClick(tarefa, holder.getAdapterPosition());
            }
        });
    }

    private int calcularDiasRestantes(String dataLimite) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataLimiteDate = sdf.parse(dataLimite);
            Date hoje = Calendar.getInstance().getTime();

            // Remover horas, minutos, segundos
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(hoje);
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
            hoje = cal1.getTime();

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(dataLimiteDate);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);
            dataLimiteDate = cal2.getTime();

            long diff = dataLimiteDate.getTime() - hoje.getTime();
            return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return tarefaList.size();
    }

    public void filterByStatus(String status) {
        tarefaList = new ArrayList<>(tarefaListFull);

        if (!status.equals("Todas")) {
            if (status.equals("Pendentes")) {
                tarefaList = tarefaListFull.stream()
                        .filter(tarefa -> !tarefa.isConcluida())
                        .collect(Collectors.toList());
            } else if (status.equals("Concluídas")) {
                tarefaList = tarefaListFull.stream()
                        .filter(Tarefa::isConcluida)
                        .collect(Collectors.toList());
            } else if (status.equals("Atrasadas")) {
                tarefaList = tarefaListFull.stream()
                        .filter(tarefa -> !tarefa.isConcluida() && calcularDiasRestantes(tarefa.getDataLimite()) < 0)
                        .collect(Collectors.toList());
            }
        }

        notifyDataSetChanged();
    }

    public void updateTarefaList(List<Tarefa> novaLista) {
        this.tarefaList = novaLista;
        this.tarefaListFull = new ArrayList<>(novaLista);
        notifyDataSetChanged();
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTituloTarefa, tvDetalhes, tvStatus, tvDataLimite, tvDiasRestantes, tvProfessor, tvModalidade;
        ImageButton btnEditTarefa, btnDeleteTarefa;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxTarefa);
            tvTituloTarefa = itemView.findViewById(R.id.tvTituloTarefa);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhes);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDataLimite = itemView.findViewById(R.id.tvDataLimite);
            tvDiasRestantes = itemView.findViewById(R.id.tvDiasRestantes);
            tvProfessor = itemView.findViewById(R.id.tvProfessor);
            tvModalidade = itemView.findViewById(R.id.tvModalidade);
            btnEditTarefa = itemView.findViewById(R.id.btnEditTarefa);
            btnDeleteTarefa = itemView.findViewById(R.id.btnDeleteTarefa);
        }
    }

    public interface OnTarefaClickListener {
        void onTarefaClick(Tarefa tarefa, int position);
        void onTarefaDelete(Tarefa tarefa, int position);
        void onTarefaEdit(Tarefa tarefa, int position);
        void onTarefaToggleComplete(Tarefa tarefa, int position);
    }
}
