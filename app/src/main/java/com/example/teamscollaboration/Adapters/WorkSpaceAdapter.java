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
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkSpaceAdapter extends RecyclerView.Adapter<WorkSpaceAdapter.ViewHolder> {
    private Context context;
    List<WorkSpaceModel> workSpaceModelList;
    private final int[] backgrounds = {
            R.drawable.two_tone_background,
            R.drawable.two_tone_two,
            R.drawable.two_tone_three
    };

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
        holder.binding.AdminName.setText(workSpaceModel.getAdminName());
        Glide.with(context).load(workSpaceModel.getAdminImage()).into(holder.binding.AdminImage);
        int randomBackground = backgrounds[new Random().nextInt(backgrounds.length)];
        holder.binding.constraint.setBackgroundResource(randomBackground);
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkSpaceDetails.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                intent.putExtra("workSpaceKey", workSpaceModel.getWorkSpaceKey());
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