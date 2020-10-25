package com.example.android.justchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class recyclerViewGroupsAdapter extends RecyclerView.Adapter<recyclerViewGroupsAdapter.recyclerViewGroupViewHolder> {
    Context mCTx;
    List<String> groupNames;
    private onNoteListener monNoteListener;

    public recyclerViewGroupsAdapter(Context mCTx, List<String> groupNames,onNoteListener onNoteListener) {
        this.mCTx = mCTx;
        this.groupNames = groupNames;
        this.monNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public recyclerViewGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCTx).inflate(R.layout.groups_recyclerview,null);
        return new recyclerViewGroupViewHolder(view,monNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerViewGroupViewHolder holder, int position) {
        String curr = groupNames.get(position);

        holder.textViewGroupName.setText(curr);
    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    public class recyclerViewGroupViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener{
        TextView textViewGroupName;
        onNoteListener onNoteListener;
        public recyclerViewGroupViewHolder(@NonNull View itemView,onNoteListener onNoteListener) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.recyclerViewGroupsName);
            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) { onNoteListener.onNoteClick(getLayoutPosition()); }
    }

    public interface onNoteListener{
        void onNoteClick(int position);
    }
}
