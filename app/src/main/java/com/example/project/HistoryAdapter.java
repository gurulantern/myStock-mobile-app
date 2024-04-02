package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyItemList;

    public HistoryAdapter(List<HistoryItem> historyItemList) {
        this.historyItemList = historyItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View historyItemView = inflater.inflate(R.layout.history_item_layout, parent, false);
        return new ViewHolder(historyItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem historyItem = historyItemList.get(position);

        holder.userTextView.setText(historyItem.getUserName());
        holder.actionTextView.setText(historyItem.getActionString());
        holder.timestampTextView.setText(historyItem.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return historyItemList.size();
    }

    public void setItems(List<HistoryItem> filteredList) {
        // Set a new list of items and notify adapter of data change
        historyItemList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userTextView;
        public TextView actionTextView;
        public TextView timestampTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.user_text);
            actionTextView = itemView.findViewById(R.id.action_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }
    }
}

