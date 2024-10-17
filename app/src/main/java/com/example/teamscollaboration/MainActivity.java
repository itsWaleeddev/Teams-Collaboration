package com.example.teamscollaboration;

import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Adapters.WorkSpaceAdapter;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    List<WorkSpaceModel> workSpaceModelList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        binding.userName.setText(auth.getCurrentUser().getDisplayName());
        Glide.with(this).load(auth.getCurrentUser().getPhotoUrl()).into(binding.userProfile);
        retrieveWorkSpaces();
        binding.addWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddWorkSpace.class);
                startActivity(intent);
            }
        });
    }
    private void setAdapter(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        WorkSpaceAdapter workSpaceAdapter = new WorkSpaceAdapter(MainActivity.this, workSpaceModelList);
        binding.recyclerView.setAdapter(workSpaceAdapter);

    }
    private void retrieveWorkSpaces(){
        databaseReference.child("Workspaces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot oneWorkSpace : snapshot.getChildren()){
                        WorkSpaceModel workSpaceModel = oneWorkSpace.getValue(WorkSpaceModel.class);
                        Log.d("workSpaceModelDetails", "onDataChange: " + workSpaceModel.toString());
                        workSpaceModelList.add(workSpaceModel);
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
}