package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;
import com.example.teamscollaboration.fragments.WorkspaceOptions;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class WorkSpaceAdapter extends RecyclerView.Adapter<WorkSpaceAdapter.ViewHolder> {
    private Context context;
    private Fragment fragment;
    private final int REQUEST_CODE = 123;
    List<WorkSpaceModel> workSpaceModelList;

    public WorkSpaceAdapter(Fragment fragment, Context context,  List<WorkSpaceModel> workSpaceModelList) {
        this.fragment = fragment;
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
        holder.binding.constraint.setBackgroundResource(workSpaceModel.getBackground());
        if(workSpaceModel.getBackground() == R.drawable.two_tone_one){
            holder.binding.constraint.setElevation(40);
        }
        else{
            holder.binding.constraint.setElevation(0);
        }
        holder.binding.constraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkSpaceDetails.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                intent.putExtra("workSpaceKey", workSpaceModel.getWorkSpaceKey());
                context.startActivity(intent);
            }
        });
        holder.binding.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkspaceOptions dialog = WorkspaceOptions.newInstance(workSpaceModel.getWorkSpaceKey());
                dialog.setTargetFragment(fragment, REQUEST_CODE);
                dialog.show(fragment.getParentFragmentManager(), "WorkspaceOptions");
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