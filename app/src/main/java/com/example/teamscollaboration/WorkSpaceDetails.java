package com.example.teamscollaboration;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.WorkSpaceAdapter;
import com.example.teamscollaboration.Adapters.WorkSpaceDetailsAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityWorkSpaceDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceDetails extends AppCompatActivity {
    ActivityWorkSpaceDetailsBinding binding;
    WorkSpaceModel workSpaceModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<TasksModel> tasksModelList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkSpaceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        workSpaceModel = (WorkSpaceModel) getIntent().getSerializableExtra("workSpace");
        binding.toolbar.setTitle(workSpaceModel.getWorkSpaceName());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("title", "onCreate: " + workSpaceModel.getWorkSpaceName());
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        binding.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(WorkSpaceDetails.this, AddTaskActivity.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                startActivity(intent);
            }
        });
        retrieveRole();
        retrieveTaskData();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }
    private void setAdapter(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        WorkSpaceDetailsAdapter workSpaceDetailsAdapter = new WorkSpaceDetailsAdapter(WorkSpaceDetails.this, tasksModelList);
        binding.recyclerView.setAdapter(workSpaceDetailsAdapter);
    }
    private void retrieveRole(){
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(userModel.getRole().equals("Team Member")){
                        binding.addTask.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }
}