package com.example.teamscollaboration;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.AllMembersAdapter;
import com.example.teamscollaboration.Adapters.MembersModel;
import com.example.teamscollaboration.databinding.ActivityAllMembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllMembersActivity extends AppCompatActivity {
    ActivityAllMembersBinding  binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String workSpaceKey = null;
    List<MembersModel> membersModelList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAllMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        retrieveMembers();
    }
    private void retrieveMembers(){
        workSpaceKey = getIntent().getStringExtra("workSpaceKey");
        databaseReference.child("Workspaces").child(workSpaceKey).child("membersList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        MembersModel membersModel = dataSnapshot.getValue(MembersModel.class);
                        membersModelList.add(membersModel);
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
    private void setAdapter(){
        binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        AllMembersAdapter adapter = new AllMembersAdapter(this, membersModelList);
        binding.myRecyclerView.setAdapter(adapter);
    }
}