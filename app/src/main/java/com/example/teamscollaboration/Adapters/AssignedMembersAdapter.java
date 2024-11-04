package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.MemberDetailsActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.AssignedMembersItemBinding;
import com.example.teamscollaboration.databinding.ItemAllmembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AssignedMembersAdapter extends RecyclerView.Adapter<AssignedMembersAdapter.ViewHolder> {
    private Context context;
    List<MembersModel> selectedMembersList;

    public AssignedMembersAdapter(Context context, List<MembersModel> selectedMembersList) {
        this.context = context;
        this.selectedMembersList = selectedMembersList;

    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        AssignedMembersItemBinding binding = AssignedMembersItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MembersModel member = selectedMembersList.get(position);
        holder.binding.memberName.setText(member.getName());
        Glide.with(context).load(member.getUserImage()).into(holder.binding.memberImage);
        holder.binding.submissionStatusIndicator.setText(member.getTaskStatus());
        if(member.getTaskStatus().equals("Submitted")){
            holder.binding.submissionStatusIndicator.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else{
            holder.binding.submissionStatusIndicator.setTextColor(context.getResources().getColor(R.color.red));
        }
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return selectedMembersList.size();
    }


public class ViewHolder extends RecyclerView.ViewHolder {
    AssignedMembersItemBinding binding;

    public ViewHolder(@NonNull AssignedMembersItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
}