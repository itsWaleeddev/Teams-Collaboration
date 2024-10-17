package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.AllMembersActivity;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceAdapter extends RecyclerView.Adapter<WorkSpaceAdapter.ViewHolder> {
    private Context context;
    List<WorkSpaceModel> workSpaceModelList;

    public WorkSpaceAdapter(Context context,  List<WorkSpaceModel> workSpaceModelList) {
        this.context = context;
        this.workSpaceModelList = workSpaceModelList;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemWorkspaceBinding binding = ItemWorkspaceBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkSpaceModel workSpaceModel = workSpaceModelList.get(position);
        holder.binding.workspaceName.setText(workSpaceModel.getWorkSpaceName());
        holder.binding.workspaceDescription.setText(workSpaceModel.getWorkSpaceDescription());
        holder.binding.showMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllMembersActivity.class);
                intent.putExtra("workSpaceKey", workSpaceModel.getWorkSpaceKey());
                context.startActivity(intent);
            }
        });
        holder.binding.viewWorkspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkSpaceDetails.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                context.startActivity(intent);
            }
        });
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return workSpaceModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemWorkspaceBinding binding;
        public ViewHolder(@NonNull ItemWorkspaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}