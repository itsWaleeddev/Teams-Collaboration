package com.example.teamscollaboration;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.AssignedMembersAdapter;
import com.example.teamscollaboration.Adapters.TaskSubmissionsAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.databinding.ActivityAdminTaskDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AdminTaskDetails extends AppCompatActivity {
    ActivityAdminTaskDetailsBinding binding;
    TasksModel tasksModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ArrayList<TaskUploadModel> taskUploadModels = new ArrayList<>();
    TaskSubmissionsAdapter taskSubmissionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminTaskDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        tasksModel = (TasksModel) getIntent().getSerializableExtra("task");
        binding.taskTitle.setText(tasksModel.getTaskName());
        binding.taskDescription.setText(tasksModel.getTaskDescription());
        binding.taskDeadline.setText(tasksModel.getDeadLine());
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(taskUploadModels.isEmpty()){
            binding.submissionsLabel.setText("No Submissions");
        }
        retrieveAssignedMembers();
        setSubmissionsAdapter(taskUploadModels);
        retrieveUploadedTasks();
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
        AssignedMembersAdapter assignedMembersAdapter = new AssignedMembersAdapter(this,membersModels);
        binding.assignedMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.assignedMembersRecyclerView.setAdapter(assignedMembersAdapter);
    }
    private void retrieveUploadedTasks(){
        databaseReference.child("Tasks").child(tasksModel.getWorkSpaceKey())
                .child(tasksModel.getTaskKey()).child("taskUploads")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                TaskUploadModel taskUploadModel = snapshot1.getValue(TaskUploadModel.class);
                                if(taskUploadModel!=null){
                                    taskUploadModels.add(taskUploadModel);
                                }
                            }
                            binding.submissionsLabel.setText("Submissions: ");
                            taskSubmissionsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("errorCheck", "onCancelled: " + error.getDetails());
                    }
                });

    }
    private void setSubmissionsAdapter(ArrayList<TaskUploadModel> taskUploadModels){
        taskSubmissionsAdapter = new TaskSubmissionsAdapter(this, taskUploadModels);
        binding.submissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.submissionsRecyclerView.setAdapter(taskSubmissionsAdapter);
    }
}