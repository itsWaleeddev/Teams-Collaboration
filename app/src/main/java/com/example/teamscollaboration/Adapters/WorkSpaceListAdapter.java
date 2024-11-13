package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.AdminTaskDetails;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.TaskDetailsActivity;
import com.example.teamscollaboration.WorkSpaceGraphActivity;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;
import com.example.teamscollaboration.databinding.ItemtaskBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class WorkSpaceListAdapter extends RecyclerView.Adapter<WorkSpaceListAdapter.ViewHolder> {
    private Context context;
    List<WorkSpaceModel> workSpaceModels;
    private final int[] backgrounds = {
            R.drawable.two_tone_background,
            R.drawable.two_tone_two,
            R.drawable.two_tone_three
    };

    public WorkSpaceListAdapter(Context context,  List<WorkSpaceModel> workSpaceModels) {
        this.context = context;
        this.workSpaceModels = workSpaceModels;
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
       WorkSpaceModel workSpaceModel = workSpaceModels.get(position);
        holder.binding.workspaceName.setText(workSpaceModel.getWorkSpaceName());
        holder.binding.workspaceDescription.setText(workSpaceModel.getWorkSpaceDescription());
        holder.binding.AdminName.setText(workSpaceModel.getAdminName());
        Glide.with(context).load(workSpaceModel.getAdminImage()).into(holder.binding.AdminImage);
        int randomBackground = backgrounds[new Random().nextInt(backgrounds.length)];
        holder.binding.constraint.setBackgroundResource(randomBackground);
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkSpaceGraphActivity.class);
                intent.putExtra("WorkSpaceKey", workSpaceModel.getWorkSpaceKey());
                context.startActivity(intent);
            }
        });
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return workSpaceModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemWorkspaceBinding binding;
        public ViewHolder(@NonNull ItemWorkspaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}