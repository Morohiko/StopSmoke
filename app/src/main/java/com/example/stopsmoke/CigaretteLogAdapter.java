package com.example.stopsmoke;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CigaretteLogAdapter extends RecyclerView.Adapter<CigaretteLogAdapter.LogViewHolder> {

    private List<CigaretteLog> logs = new ArrayList<>();

    public void setLogs(List<CigaretteLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewLogDate);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cigarette_log, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        CigaretteLog log = logs.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateString = sdf.format(new Date(log.getTimestamp()));
        holder.textViewDate.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }
}
