package com.example.teamscollaboration.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentWorkspaceOptionsListDialogBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

public class WorkspaceOptions extends BottomSheetDialogFragment {
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private static final String WORKSPACE_KEY = "workspaceKey";
    private FragmentWorkspaceOptionsListDialogBinding binding;
    boolean deletedCheck = false;


    public static WorkspaceOptions newInstance(String workspaceKey) {
        final WorkspaceOptions fragment = new WorkspaceOptions();
        final Bundle args = new Bundle();
        args.putString(WORKSPACE_KEY, workspaceKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        binding = FragmentWorkspaceOptionsListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.getRoot().setBackgroundResource(R.drawable.bottomsheet_background);
        if (getArguments() != null) {
            binding.deleteWorkspace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() != null) {
            // Passing result to the parent fragment
            Intent intent = new Intent();
            Log.d("DismissedFragment", "onDestroyView: ");
            intent.putExtra("workspacekey", getArguments().getString(WORKSPACE_KEY));
            intent.putExtra("deletedCheck",deletedCheck);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    public void showDeleteConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(R.layout.custom_alert_dialogue)
                .setCancelable(false)
                .create();
        dialog.show();
        TextView positiveButton = dialog.findViewById(R.id.deleteButton);
        TextView negativeButton = dialog.findViewById(R.id.cancelButton);
        if (positiveButton != null && negativeButton != null) {
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteWorkspace();
                    dialog.dismiss();
                }
            });
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void deleteWorkspace() {
        DatabaseReference workspaceRef = databaseReference.child("Workspaces").child(getArguments().getString(WORKSPACE_KEY));

        workspaceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Workspace exists, proceed with deletion
                    workspaceRef.removeValue().addOnSuccessListener(unused -> {
                        // Reference to the tasks path
                        DatabaseReference tasksRef = databaseReference.child("Tasks").child(getArguments().getString(WORKSPACE_KEY));

                        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot taskSnapshot) {
                                if (taskSnapshot.exists()) {
                                    // Tasks exist, proceed with deletion
                                    tasksRef.removeValue().addOnSuccessListener(unused1 -> {
                                        deleteStorageData();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to delete tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    deleteStorageData();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(requireContext(), "Error checking tasks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to delete workspace: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(requireContext(), "Workspace does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Error checking workspace: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteStorageData() {
        // Reference to the workspace storage
        StorageReference workSpaceRef = storageReference.child("Workspaces").child(getArguments().getString(WORKSPACE_KEY));
            // File exists, proceed with deletion
            workSpaceRef.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Workspace and associated data deleted successfully", Toast.LENGTH_SHORT).show();
                deletedCheck = true;
                dismiss();
            }).addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Workspace Deleted Successfully", Toast.LENGTH_SHORT).show();
                    deletedCheck = true;
                    dismiss();
                }
                Log.d("storageCheck", "deleteStorageData: Failed to delete from storage: " + e.getMessage());
            });
    }
}