package com.example.teamscollaboration;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityAddWorkSpaceBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.A;

public class AddWorkSpace extends AppCompatActivity {
    ActivityAddWorkSpaceBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    String created_at = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddWorkSpaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        binding.chooseMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddWorkSpace.this, ChooseMembers.class);
                startActivity(intent);
            }
        });
        binding.submitWorkspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workSpaceName = binding.workspaceNameInput.getText().toString().trim();
                workSpaceDescription = binding.workspaceDescriptionInput.getText().toString().trim();
                deadLine = binding.deadlineDateInput.getText().toString().trim();
                priority = binding.prioritySpinner.getSelectedItem().toString().trim();
                if(workSpaceName.isEmpty()){
                    binding.workspaceNameInput.setError("Please fill this field");
                }
                else if(workSpaceDescription.isEmpty()){
                    binding.workspaceDescriptionInput.setError("Please fill this field");
                }
                else if(deadLine.isEmpty()){
                    binding.deadlineDateInput.setError("Please fill this field");
                }
                else{
                    saveWorkSpace();
                }
            }
        });
    }
    private void saveWorkSpace(){
        DatabaseReference workSpaceRef = databaseReference.child("Workspaces");
        String newWorkSpaceKey = workSpaceRef.push().getKey();
        WorkSpaceModel workSpaceModel = new WorkSpaceModel(workSpaceName, workSpaceDescription,
                deadLine, priority, System.currentTimeMillis(), auth.getCurrentUser().getUid());
        workSpaceRef.child(newWorkSpaceKey).setValue(workSpaceModel);
    }
}