package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.SubmissionDetailsActivity;
import com.example.teamscollaboration.databinding.SubmisssionItemBinding;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TaskSubmissionsAdapter extends RecyclerView.Adapter<TaskSubmissionsAdapter.ViewHolder> {
    private Context context;
    List<TaskUploadModel> taskUploadModels;

    public TaskSubmissionsAdapter(Context context, List<TaskUploadModel> taskUploadModels) {
        this.context = context;
        this.taskUploadModels = taskUploadModels;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
       SubmisssionItemBinding binding = SubmisssionItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskUploadModel taskUploadModel = taskUploadModels.get(position);
        holder.binding.submitterName.setText(taskUploadModel.getUserName());
        Date date = new Date(taskUploadModel.getUpload_time());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        String formattedDate = sdf.format(date);
        holder.binding.submissionDate.setText(formattedDate);
        holder.binding.viewSubmissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SubmissionDetailsActivity.class);
                intent.putExtra("task", (Serializable) taskUploadModel);
                context.startActivity(intent);
            }
        });
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return taskUploadModels.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        SubmisssionItemBinding binding;

        public ViewHolder(@NonNull SubmisssionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}