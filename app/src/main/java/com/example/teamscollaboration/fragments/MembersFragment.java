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

import com.example.teamscollaboration.Adapters.AllMembersAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentMembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {
    FragmentMembersBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String workSpaceKey = null;
    List<MembersModel> membersModelList = new ArrayList<>();
    WorkSpaceModel workSpaceModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        retrieveMembers();
    }
    private void retrieveMembers() {
        workSpaceKey = requireActivity().getIntent().getStringExtra("workSpaceKey");
        databaseReference.child("Workspaces").child(workSpaceKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    workSpaceModel = snapshot.getValue(WorkSpaceModel.class);
                    if (workSpaceModel != null) {
                        membersModelList = workSpaceModel.getMembersList();
                    }
                    setAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("dataBaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        AllMembersAdapter adapter = new AllMembersAdapter(requireActivity(), membersModelList, workSpaceModel);
        binding.myRecyclerView.setAdapter(adapter);
    }
}