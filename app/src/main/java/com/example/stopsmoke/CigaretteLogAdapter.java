package com.example.stopsmoke;

import android.annotation.SuppressLint;
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

/**
 * Adapter for RecyclerView to display cigarette logs.
 */
public class CigaretteLogAdapter extends RecyclerView.Adapter<CigaretteLogAdapter.LogViewHolder> {

    private List<CigaretteLog> logs = new ArrayList<>();

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cigarette_log, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        CigaretteLog log = logs.get(position);
        holder.textViewTimestamp.setText(formatTimestamp(log.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * Sets the logs data and notifies the adapter.
     *
     * @param logs List of CigaretteLog objects.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setLogs(List<CigaretteLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    /**
     * Formats the timestamp into a readable date and time string.
     *
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted date and time string.
     */
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * ViewHolder class for Log items.
     */
    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTimestamp;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTimestamp = itemView.findViewById(R.id.textViewLogTimestamp);
        }
    }
}
