package com.example.teamscollaboration.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamscollaboration.databinding.ItemAllmembersBinding;
import com.example.teamscollaboration.databinding.ItemMemberBinding;

import java.util.ArrayList;
import java.util.List;

public class AllMembersAdapter extends RecyclerView.Adapter<AllMembersAdapter.ViewHolder> {
    private Context context;
    List<MembersModel> selectedMembersList;

    public AllMembersAdapter(Context context, List<MembersModel> selectedMembersList) {
        this.context = context;
        this.selectedMembersList = selectedMembersList;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAllmembersBinding binding = ItemAllmembersBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MembersModel member = selectedMembersList.get(position);
        holder.binding.memberName.setText(member.getName());
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return selectedMembersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAllmembersBinding binding;

        public ViewHolder(@NonNull ItemAllmembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
