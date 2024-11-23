package com.example.teamscollaboration.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.teamscollaboration.Adapters.WorkSpaceDetailsAdapter;
import com.example.teamscollaboration.AddTaskActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.FragmentTasksBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TasksFragment extends Fragment {
    FragmentTasksBinding binding;
    WorkSpaceModel workSpaceModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<TasksModel> tasksModelList = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workSpaceModel = (WorkSpaceModel) requireActivity().getIntent().getSerializableExtra("workSpace");
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        binding.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(requireActivity(), AddTaskActivity.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                startActivity(intent);
            }
        });
        if(!(workSpaceModel.getAdminId().equals(auth.getCurrentUser().getUid()))){
            binding.addTask.setVisibility(View.GONE);
        }
        retrieveTaskData();

    }
    private void retrieveTaskData(){
        databaseReference.child("Tasks").child(workSpaceModel.getWorkSpaceKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot oneWorkSpace : snapshot.getChildren()){
                        TasksModel taskModel = oneWorkSpace.getValue(TasksModel.class);
                        if(workSpaceModel.getAdminId().equals(auth.getCurrentUser().getUid())){
                            tasksModelList.add(taskModel);
                        }
                        else{
                            List<MembersModel> membersModels = taskModel.getMembersList();
                            for(MembersModel membersModel : membersModels){
                                if(auth.getCurrentUser().getUid().equals(membersModel.getuID())){
                                    tasksModelList.add(taskModel);
                                }
                            }
                        }
                    }
                    setAdapter();
                }
                else{
                    setAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }
    private void setAdapter(){
        if(!tasksModelList.isEmpty()){
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
            WorkSpaceDetailsAdapter workSpaceDetailsAdapter = new WorkSpaceDetailsAdapter(requireActivity(), tasksModelList);
            binding.recyclerView.setAdapter(workSpaceDetailsAdapter);
        }
        else{
            binding.noTasks.setVisibility(View.VISIBLE);
            binding.tasksLayout.setVisibility(View.GONE);
            binding.lottieAnimationView.setAnimation(R.raw.tasksanimation);
            if(!(workSpaceModel.getAdminId().equals(auth.getCurrentUser().getUid()))){
                binding.addTask2.setVisibility(View.GONE);
            }
            binding.addTask2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =  new Intent(requireActivity(), AddTaskActivity.class);
                    intent.putExtra("workSpace", (Serializable) workSpaceModel);
                    startActivity(intent);
                }
            });
        }
    }
}