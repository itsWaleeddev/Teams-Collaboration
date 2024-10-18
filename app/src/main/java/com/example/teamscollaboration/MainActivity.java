package com.example.teamscollaboration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Adapters.WorkSpaceAdapter;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    List<WorkSpaceModel> workSpaceModelList = new ArrayList<>();
    String role = null;
    WorkSpaceAdapter workSpaceAdapter;

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
        role = getIntent().getStringExtra("role");
        if (role != null) {
            binding.Role.setText(role);
        }
        retrieveRole();
        setAdapter();
        retrieveWorkSpaces();
        binding.addWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddWorkSpace.class);
                startActivity(intent);
            }
        });
    }

    private void setAdapter() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        workSpaceAdapter = new WorkSpaceAdapter(MainActivity.this, workSpaceModelList, role);
        binding.recyclerView.setAdapter(workSpaceAdapter);

    }

    private void retrieveWorkSpaces(){
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
                        // Use the workspace object
                    }
                } else {
                    // Handle no matching workspaces
                    Toast.makeText(MainActivity.this, "No workspace found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Log.d("errorCheck", "onCancelled: " + databaseError.getMessage());

            }
        });
    }

    private void retrieveRole() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    binding.Role.setText(userModel.getRole());
                    if (userModel.getRole().equals("Team Member") || userModel.getRole().equals("Team Leader")) {
                        binding.addWorkspace.setVisibility(View.GONE);
                    }
                    if (role == null) {
                        role = userModel.getRole();
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