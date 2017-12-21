/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder.folderpicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topratedapps.screenrecorder.Const;
import com.topratedapps.screenrecorder.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bullhead on 01-12-2017.
 */

class DirectoryRecyclerAdapter extends RecyclerView.Adapter<DirectoryRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<File> directories;
    private static OnDirectoryClickedListerner onDirectoryClickedListerner;

    interface OnDirectoryClickedListerner {
        void OnDirectoryClicked(File directory);
    }

    DirectoryRecyclerAdapter(Context context, OnDirectoryClickedListerner listerner, ArrayList<File> directories) {
        this.context = context;
        onDirectoryClickedListerner = listerner;
        this.directories = directories;
    }

    @Override
    public DirectoryRecyclerAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_directory_chooser, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DirectoryRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.dir.setText(directories.get(position).getName());
        holder.dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Const.TAG, "Item clicked: " + directories.get(holder.getAdapterPosition()));
                onDirectoryClickedListerner.OnDirectoryClicked(directories.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return directories.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView dir;
        LinearLayout dir_view;

        public ItemViewHolder(View itemView) {
            super(itemView);
            dir = (TextView) itemView.findViewById(R.id.directory);
            dir_view = (LinearLayout) itemView.findViewById(R.id.directory_view);
        }
    }
}
