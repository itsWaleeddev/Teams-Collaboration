package com.example.teamscollaboration.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpacesList;
import com.example.teamscollaboration.databinding.FragmentDashboardBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment {
    FragmentDashboardBinding binding;
    FirebaseAuth auth;
    DatabaseReference databasereference;
    ArrayList<WorkSpaceModel> adminWorkSpacesList = new ArrayList<>();
    ArrayList<WorkSpaceModel> memberWorkSpacesList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databasereference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retrieveWorkspaces();
    }

    private void retrieveWorkspaces() {
        databasereference.child("Workspaces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String workSpaceKey = dataSnapshot.getKey();
                        databasereference.child("Workspaces").child(workSpaceKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        WorkSpaceModel workSpaceModel = snapshot.getValue(WorkSpaceModel.class);
                                        if (workSpaceModel.getAdminId().equals(auth.getCurrentUser().getUid())) {
                                            adminWorkSpacesList.add(workSpaceModel);
                                        }
                                        List<MembersModel> membersModelList = workSpaceModel.getMembersList();
                                        for (MembersModel membersModel : membersModelList) {
                                            if (membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                                memberWorkSpacesList.add(workSpaceModel);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("errorCheck", "onCancelled: " + error.getDetails());
                                    }
                                });
                    }
                }
                binding.nextButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(requireActivity(), WorkSpacesList.class);
                        intent.putExtra("title", "You are Admin");
                        intent.putExtra("WorkSpaces", (Serializable) adminWorkSpacesList);
                        requireContext().startActivity(intent);
                    }
                });
                binding.nextButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(requireActivity(), WorkSpacesList.class);
                        intent.putExtra("title", "You are Member");
                        intent.putExtra("WorkSpaces", (Serializable) memberWorkSpacesList);
                        requireContext().startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("workSpaces", "onCancelled: error in retrieving workspaces");
            }
        });
    }
}