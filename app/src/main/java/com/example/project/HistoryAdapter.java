package com.example.project;

/*
Name: HistoryAdapter.java
Version: 1.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the adapter to bind data to views within the History Recycler view.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    // Declares the item list for the adapter
    private List<HistoryItem> historyItemList;

    // Declare History Adapter using the historyItemList
    public HistoryAdapter(List<HistoryItem> historyItemList) {
        this.historyItemList = historyItemList;
    }

    // Create process for the view holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext(); // Retrieves the context from the parent view group
        LayoutInflater inflater = LayoutInflater.from(context);
        View historyItemView = inflater.inflate(R.layout.history_item_layout, parent, false);
        return new ViewHolder(historyItemView); // Instantiates the new ViewHolder object and passes the historyItemView with each item
    }

    // Binding for views - sets the 3 text views with the history item data
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem historyItem = historyItemList.get(position);

        holder.userTextView.setText(historyItem.getUserName());
        holder.actionTextView.setText(historyItem.getActionString());
        holder.timestampTextView.setText(historyItem.getTimestamp());
    }

    /**
     * Grabs item count
     * @return size of historyItemList
     */
    @Override
    public int getItemCount() {
        return historyItemList.size();
    }

    /**
     * Extension of RecyclerView to use the use, action, and timestamp text views
     */
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

