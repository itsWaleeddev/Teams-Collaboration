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

import com.example.teamscollaboration.Adapters.AssignedMembersAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentTaskMembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TaskMembersFragment extends Fragment {
    FragmentTaskMembersBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    TasksModel tasksModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksModel = (TasksModel) requireActivity().getIntent().getSerializableExtra("task");
        retrieveAssignedMembers();
    }
    private void retrieveAssignedMembers(){
        databaseReference.child("Tasks").child(tasksModel.getWorkSpaceKey())
                .child(tasksModel.getTaskKey()).child("membersList")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<MembersModel> membersModels = new ArrayList<>();
                        if(snapshot.exists()){
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                MembersModel membersModel = dataSnapshot.getValue(MembersModel.class);
                                if(membersModel!=null){
                                    membersModels.add(membersModel);
                                }
                            }
                            setMembersAdapter(membersModels);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("errorCheck", "onCancelled: " + error.getDetails());
                    }
                });
    }
    private void setMembersAdapter(ArrayList<MembersModel> membersModels){
        AssignedMembersAdapter assignedMembersAdapter = new AssignedMembersAdapter(requireContext(),membersModels);
        binding.assignedMembersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.assignedMembersRecyclerView.setAdapter(assignedMembersAdapter);
    }

}