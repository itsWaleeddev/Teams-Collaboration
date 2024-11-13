package com.example.teamscollaboration.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.teamscollaboration.Adapters.StreamsAdapter;
import com.example.teamscollaboration.AddStreamActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.StreamModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentStreamBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class StreamFragment extends Fragment {
    FragmentStreamBinding binding;
    String workSpaceKey = null;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ArrayList<StreamModel> streamModels = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStreamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workSpaceKey = requireActivity().getIntent().getStringExtra("workSpaceKey");
        binding.addStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), AddStreamActivity.class);
                intent.putExtra("workSpaceKey", workSpaceKey);
                requireContext().startActivity(intent);
            }
        });
        retrieveStreams();
    }
    private void retrieveStreams(){
        workSpaceKey = requireActivity().getIntent().getStringExtra("workSpaceKey");
        databaseReference.child("Workspaces").child(workSpaceKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    WorkSpaceModel workSpaceModel = snapshot.getValue(WorkSpaceModel.class);
                    if (workSpaceModel != null) {
                        List<StreamModel> streamModelList = workSpaceModel.getStreamModel();
                        if (streamModelList != null) {
                            for (StreamModel streamModel : streamModelList) {
                                if (streamModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                    streamModels.add(streamModel);  // Add the matching StreamModel to your list
                                }
                            }
                            List<MembersModel> membersModelList = workSpaceModel.getMembersList();
                            if (membersModelList != null) {
                                for (MembersModel membersModel : membersModelList) {
                                    if (membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                        for (StreamModel streamModel : streamModelList) {
                                            streamModels.add(streamModel);  // Add the matching StreamModel to your list
                                        }
                                    }
                                }
                            }
                        }
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
        StreamsAdapter streamsAdapter = new StreamsAdapter(requireContext(), streamModels);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setAdapter(streamsAdapter);
    }

}