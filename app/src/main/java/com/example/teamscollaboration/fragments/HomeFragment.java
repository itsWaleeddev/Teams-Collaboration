package com.example.teamscollaboration.fragments;

import android.app.Activity;
import android.content.Intent;
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
import com.example.teamscollaboration.ImageViewerActivity;
import com.example.teamscollaboration.MainActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.TaskDetailsActivity;
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
    private final int REQUEST_CODE = 123;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("DismissedFragment", "onActivityResult: " + REQUEST_CODE);
                String workspacekey = data.getStringExtra("workspacekey");
                Boolean deletedCheck = data.getBooleanExtra("deletedCheck",false);
                if(deletedCheck) {
                    for (int i = 0; i < workSpaceModelList.size(); i++) {
                        WorkSpaceModel workSpaceModel = workSpaceModelList.get(i);
                        if (workspacekey.equals(workSpaceModel.getWorkSpaceKey())) {
                            workSpaceModelList.remove(i);
                            workSpaceAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    if (workSpaceModelList.isEmpty()) {
                        binding.workspacesLayout.setVisibility(View.GONE);
                        binding.noWorkspaces.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

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
            retrieveUserData();
        }
    }

    private void retrieveUserData() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    binding.userName.setText(userModel.getName());
                    Glide.with(requireContext()).load(userModel.getUserImage()).into(binding.userProfile);
                    binding.userProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
                            intent.putExtra("image_url", userModel.getUserImage()); // Passed the image URL to the new activity
                            startActivity(intent);
                        }
                    });
                    retrieveWorkSpaces();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        if(!workSpaceModelList.isEmpty()) {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            workSpaceAdapter = new WorkSpaceAdapter(this ,requireContext(), workSpaceModelList);
            binding.recyclerView.setAdapter(workSpaceAdapter);
        }else{
            binding.workspacesLayout.setVisibility(View.GONE);
            binding.noWorkspaces.setVisibility(View.VISIBLE);
        }

    }

    private void retrieveWorkSpaces() {
        retrieveAdminWorkSpaces(); //For retrieving workspaces in which user is admin
       // retrieveTeamWorkSpaces();  //For retrieving workspaces in which user is member
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
                    }
                    retrieveTeamWorkSpaces();
                } else {
                    retrieveTeamWorkSpaces();
                    // Handle no matching workspaces
                    Log.d("adminCheck", "onDataChange:  user is not admin in any workspace" );
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
                        for (DataSnapshot member : memberListSnapshot.getChildren()) {
                            MembersModel membersModel = member.getValue(MembersModel.class);
                            if (membersModel != null && membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                // If it matches, get the workspace data
                                WorkSpaceModel workspace = workspaceSnapshot.getValue(WorkSpaceModel.class);

                                // Add to your list and update the adapter
                                workSpaceModelList.add(workspace);
                            }
                        }
                    }
                    setAdapter();
                    // Handle case where no matching workspaces are found
                    if (workSpaceModelList.isEmpty() && getActivity() != null && isAdded()) {
                        Log.d("adminCheck", "onDataChange:  user is  admin in all workspace" );
                    }
                }
                else {
                    setAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("errorCheck", "onCancelled: " + databaseError.getMessage());
            }
        });
    }
}