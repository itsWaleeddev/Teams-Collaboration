package com.example.teamscollaboration.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Adapters.WorkSpaceAdapter;
import com.example.teamscollaboration.MainActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    List<WorkSpaceModel> workSpaceModelList = new ArrayList<>();
    WorkSpaceAdapter workSpaceAdapter;
    String role = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded() && getActivity() != null) {
            binding.userName.setText(auth.getCurrentUser().getDisplayName());
            Glide.with(this).load(auth.getCurrentUser().getPhotoUrl()).into(binding.userProfile);
            retrieveRole();
        }
    }

    private void retrieveRole() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    binding.Role.setText(userModel.getRole());
                    role = userModel.getRole();
                    if (role != null) {
                        setAdapter();
                        retrieveWorkSpaces();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workSpaceAdapter = new WorkSpaceAdapter(requireContext(), workSpaceModelList);
        binding.recyclerView.setAdapter(workSpaceAdapter);

    }

    private void retrieveWorkSpaces() {
        if (role.equals("Admin")) {
            retrieveAdminWorkSpaces();
        } else {
            retrieveTeamWorkSpaces();
        }
    }

    private void retrieveAdminWorkSpaces() {
        DatabaseReference workspaceRef = FirebaseDatabase.getInstance().getReference("Workspaces");
        // Query workspaces where adminId matches a specific value
        Query query = workspaceRef.orderByChild("adminId").equalTo(auth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot workspaceSnapshot : dataSnapshot.getChildren()) {
                        // Extract each workspace where the adminId matches
                        WorkSpaceModel workspace = workspaceSnapshot.getValue(WorkSpaceModel.class);
                        workSpaceModelList.add(workspace);
                        workSpaceAdapter.notifyDataSetChanged();
                    }
                }
                 else {
                    // Handle no matching workspaces
                    Toast.makeText(requireContext(), "No workspace found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Log.d("errorCheck", "onCancelled: " + databaseError.getMessage());

            }
        });
    }

    private void retrieveTeamWorkSpaces() {
        DatabaseReference workspaceRef = FirebaseDatabase.getInstance().getReference("Workspaces");
        // Listen to all workspaces
        workspaceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot workspaceSnapshot : dataSnapshot.getChildren()) {
                        // Access the memberList child inside each workspace
                        DataSnapshot memberListSnapshot = workspaceSnapshot.child("membersList");
                        for(DataSnapshot member : memberListSnapshot.getChildren()){
                            MembersModel membersModel = member.getValue(MembersModel.class);
                            if (membersModel!=null && membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                // If it matches, get the workspace data
                                WorkSpaceModel workspace = workspaceSnapshot.getValue(WorkSpaceModel.class);

                                // Add to your list and update the adapter
                                workSpaceModelList.add(workspace);
                                workSpaceAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                    // Handle case where no matching workspaces are found
                    if (workSpaceModelList.isEmpty() && getActivity() != null && isAdded()) {
                        Toast.makeText(requireContext(), "No workspace found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("errorCheck", "onCancelled: " + databaseError.getMessage());
            }
        });
    }
}