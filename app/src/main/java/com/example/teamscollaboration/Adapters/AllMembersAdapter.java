package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.ItemAllmembersBinding;
import com.example.teamscollaboration.databinding.ItemMemberBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllMembersAdapter extends RecyclerView.Adapter<AllMembersAdapter.ViewHolder> {
    private Context context;
    List<MembersModel> selectedMembersList;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    WorkSpaceModel workSpaceModel;

    public AllMembersAdapter(Context context, List<MembersModel> selectedMembersList, WorkSpaceModel workSpaceModel) {
        this.context = context;
        this.selectedMembersList = selectedMembersList;
        this.workSpaceModel = workSpaceModel;
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
        holder.binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ContextThemeWrapper contextWrapper = new ContextThemeWrapper(view.getContext(), R.style.CustomPopupMenu);
                // Create a PopupMenu with the custom style
                PopupMenu popupMenu = new PopupMenu(contextWrapper, view);

                // Inflate the menu resource file
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // Set a listener for menu item clicks
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.assign_team_leader) {
                            databaseReference.child("Users").child(member.uID).child("role").setValue("Team Leader");
                            DatabaseReference workspacesRef = databaseReference.child("Workspaces").child(workSpaceModel.getWorkSpaceKey()).child("membersList");
                            updateRole(workspacesRef, member.uID);
                            databaseReference.child("Workspaces").child(workSpaceModel.getWorkSpaceKey()).child("teamLeader").setValue(member.getName());
                            Toast.makeText(view.getContext(), "Assigned as Team Leader", Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                // Show the popup menu
                popupMenu.show();
                return true;
            }
        });

    }

    //How many Items?
    @Override
    public int getItemCount() {
        return selectedMembersList.size();
    }

    private void updateRole(DatabaseReference workSpaceRef, String uID) {
        workSpaceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        // Retrieve each member object
                        MembersModel member = memberSnapshot.getValue(MembersModel.class);

                        // Check if the member's uId matches the current user's uId
                        if (member != null && member.getuID().equals(uID)) {
                            // Update the role for this member
                            memberSnapshot.getRef().child("role").setValue("Team Leader");
                            break; // Once the match is found and updated, exit the loop
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FirebaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAllmembersBinding binding;

        public ViewHolder(@NonNull ItemAllmembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
