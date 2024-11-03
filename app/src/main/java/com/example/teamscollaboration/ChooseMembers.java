package com.example.teamscollaboration;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.ChooseMembersAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.databinding.ActivityChooseMembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChooseMembers extends AppCompatActivity {
    ActivityChooseMembersBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<MembersModel> membersModelList = new ArrayList<>();
    ChooseMembersAdapter adapter;
    List<MembersModel> selectedMembers = new ArrayList<>();
    List<MembersModel> members = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChooseMembersBinding.inflate(getLayoutInflater());
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
        members = (ArrayList<MembersModel>) getIntent().getSerializableExtra("Members");
        if (members != null && !members.isEmpty()) {
            setUpdatedAdapter();
            retrieveDataForAdapter();
        } else {
            setAdapter();
            retrieveDataForAdapter();
        }
        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMembers = adapter.getSelectedMembers();
                Toast.makeText(ChooseMembers.this, "Members Selected Successfully", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedMembers", (Serializable) selectedMembers);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveDataForAdapter() {
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserId = auth.getCurrentUser().getUid();
                for (DataSnapshot oneSnapShot : snapshot.getChildren()) {
                    String userId = oneSnapShot.getKey();
                    if (!userId.equals(currentUserId)) {
                        String name = oneSnapShot.child("name").getValue(String.class);
                        String role = oneSnapShot.child("role").getValue(String.class);
                        String uID = oneSnapShot.child("userId").getValue(String.class);
                        String email = oneSnapShot.child("email").getValue(String.class);
                        String imageUrl = oneSnapShot.child("userImage").getValue(String.class);
                        String about = oneSnapShot.child("about").getValue(String.class);
                        if (!role.equals("Admin")) {
                            MembersModel membersModel = new MembersModel(email, uID, name, false, role, imageUrl, about);
                            membersModelList.add(membersModel);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                if (members!=null && !members.isEmpty() &&membersModelList != null && !membersModelList.isEmpty()) {
                    // Iterate over membersModelList
                    for (MembersModel membersModelFromList : membersModelList) {
                        boolean exists = false;
                        for (MembersModel member : members) {
                            if (member.getuID().equals(membersModelFromList.getuID())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            members.add(membersModelFromList);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChooseMembersAdapter(this, membersModelList);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setUpdatedAdapter() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChooseMembersAdapter(this, members);
        binding.recyclerView.setAdapter(adapter);
    }
}