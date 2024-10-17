package com.example.teamscollaboration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.MembersModel;
import com.example.teamscollaboration.Adapters.TasksMembersAdapter;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityTasksMembersBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TasksMembers extends AppCompatActivity {
    ActivityTasksMembersBinding binding;
    WorkSpaceModel workSpaceModel;
    List<MembersModel> membersModelList = new ArrayList<>();
    TasksMembersAdapter adapter;
    List<MembersModel> selectedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTasksMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        workSpaceModel = (WorkSpaceModel) getIntent().getSerializableExtra("workSpace");
        if(workSpaceModel!=null){
            membersModelList = workSpaceModel.getMembersList();
        }
        setAdapter();
        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMembers = adapter.getSelectedMembers();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedMembers", (Serializable) selectedMembers);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
    private void setAdapter(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TasksMembersAdapter(this, membersModelList);
        binding.recyclerView.setAdapter(adapter);
    }
}