package com.example.weighttrackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that renders weight rows and surfaces edit/delete actions back to the activity.
 */
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    public interface OnWeightActionListener {
        void onEdit(WeightEntry entry);
        void onDelete(WeightEntry entry);
    }

    private final List<WeightEntry> weightEntries = new ArrayList<>();
    private final OnWeightActionListener listener;

    public WeightAdapter(OnWeightActionListener listener) {
        this.listener = listener;
    }

    public void setWeightEntries(List<WeightEntry> entries) {
        weightEntries.clear();
        weightEntries.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightEntry entry = weightEntries.get(position);
        holder.tvRowDate.setText(entry.getDate());
        holder.tvRowWeight.setText(String.format(Locale.US, "%.1f lbs", entry.getWeight()));

        holder.itemView.setOnClickListener(v -> listener.onEdit(entry));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entry));
    }

    @Override
    public int getItemCount() {
        return weightEntries.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowDate;
        TextView tvRowWeight;
        Button btnDelete;

        public WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRowDate = itemView.findViewById(R.id.tvRowDate);
            tvRowWeight = itemView.findViewById(R.id.tvRowWeight);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
