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

import com.example.teamscollaboration.Adapters.TaskSubmissionsAdapter;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentSubmissionsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SubmissionsFragment extends Fragment {
    FragmentSubmissionsBinding binding;
    TasksModel tasksModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ArrayList<TaskUploadModel> taskUploadModels = new ArrayList<>();
    TaskSubmissionsAdapter taskSubmissionsAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubmissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksModel = (TasksModel) requireActivity().getIntent().getSerializableExtra("task");
        retrieveUploadedTasks();
    }

    private void setSubmissionsAdapter() {
        if(!taskUploadModels.isEmpty()) {
            taskSubmissionsAdapter = new TaskSubmissionsAdapter(requireContext(), taskUploadModels);
            binding.submissionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.submissionsRecyclerView.setAdapter(taskSubmissionsAdapter);
        }else{
            binding.submissionsRecyclerView.setVisibility(View.GONE);
            binding.noSubmissionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void retrieveUploadedTasks() {
        databaseReference.child("Tasks").child(tasksModel.getWorkSpaceKey())
                .child(tasksModel.getTaskKey()).child("taskUploads")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                TaskUploadModel taskUploadModel = snapshot1.getValue(TaskUploadModel.class);
                                if (taskUploadModel != null) {
                                    taskUploadModels.add(taskUploadModel);
                                }
                            }
                            setSubmissionsAdapter();
                        }
                        else {
                            setSubmissionsAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("errorCheck", "onCancelled: " + error.getDetails());
                    }
                });

    }
}