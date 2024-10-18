package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.databinding.ItemMemberBinding;

import java.util.ArrayList;
import java.util.List;

public class TasksMembersAdapter extends RecyclerView.Adapter<TasksMembersAdapter.ViewHolder> {
    private Context context;
    List<MembersModel> membersList;

    public TasksMembersAdapter(Context context,  List<MembersModel> membersList) {
        this.context = context;
        this.membersList = membersList;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMemberBinding binding = ItemMemberBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MembersModel member = membersList.get(position);
        holder.binding.memberName.setText(member.getName());
        holder.binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                member.setChecked(b);
            }
        });
    }
    public List<MembersModel> getSelectedMembers() {
        List<MembersModel> selectedMembers = new ArrayList<>();
        for (MembersModel member : membersList) {
            if (member.getChecked()) {
                selectedMembers.add(member);
            }
        }
        return selectedMembers;
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemMemberBinding binding;

        public ViewHolder(@NonNull ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        }
    }