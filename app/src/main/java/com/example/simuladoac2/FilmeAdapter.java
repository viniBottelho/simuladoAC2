package com.example.simuladoac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilmeAdapter extends RecyclerView.Adapter<FilmeAdapter.ViewHolder> {

    private List<Filme> filmes;

    public interface OnItemClickListener {
        void onItemClick(Filme filme);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Filme filme);
    }

    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public FilmeAdapter(List<Filme> filmes) {
        this.filmes = filmes;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_2,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Filme filme = filmes.get(position);

        String textoNota = filme.getNota() == null
                ? "Não avaliado"
                : String.valueOf(filme.getNota());

        holder.txt1.setText(filme.getNome());

        holder.txt2.setText(
                filme.getTipo() + " | " + filme.getGenero()
                        + "\nAno: " + filme.getAno()
                        + " | Nota: " + textoNota
                        + "\n" + (
                        filme.isAssistido()
                                ? "Já assistiu"
                                : "Deseja assistir"
                )
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(filme);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(filme);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filmes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt1;
        TextView txt2;

        public ViewHolder(View itemView) {
            super(itemView);

            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }
}