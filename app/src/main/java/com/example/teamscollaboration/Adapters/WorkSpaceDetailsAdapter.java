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
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.TaskDetailsActivity;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;
import com.example.teamscollaboration.databinding.ItemtaskBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceDetailsAdapter extends RecyclerView.Adapter<WorkSpaceDetailsAdapter.ViewHolder> {
    private Context context;
    List<TasksModel> tasksModelList;

    public WorkSpaceDetailsAdapter(Context context,  List<TasksModel> tasksModelList) {
        this.context = context;
        this.tasksModelList = tasksModelList;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemtaskBinding binding = ItemtaskBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       TasksModel tasksModel = tasksModelList.get(position);
       holder.binding.taskName.setText(tasksModel.getTaskName());
       holder.binding.taskStatus.setText(tasksModel.getTaskStatus());
       holder.binding.taskDetailsButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(context, TaskDetailsActivity.class);
               intent.putExtra("task", (Serializable) tasksModel);
               context.startActivity(intent);
           }
       });
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return tasksModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemtaskBinding binding;
        public ViewHolder(@NonNull ItemtaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}