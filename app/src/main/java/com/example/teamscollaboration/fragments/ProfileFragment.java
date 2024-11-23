package com.example.teamscollaboration.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.ImageViewerActivity;
import com.example.teamscollaboration.MainActivity;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.SignInActivity;
import com.example.teamscollaboration.TaskDetailsActivity;
import com.example.teamscollaboration.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class ProfileFragment extends Fragment {
    private final boolean isAndroid13OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    FragmentProfileBinding binding;
    Boolean isEnable = false;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    StorageReference storageReference;
    Uri imageUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retriveUserData();
        binding.userNameInput.setEnabled(false);
        binding.DescriptionInput.setEnabled(false);
        binding.saveInfoButton.setEnabled(false);
        binding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEnable = true;
                binding.userNameInput.setEnabled(true);
                binding.DescriptionInput.setEnabled(true);
                if (isEnable) {
                    binding.userNameInput.requestFocus();
                }
                binding.saveInfoButton.setEnabled(true);
            }
        });
        binding.saveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserData();
            }
        });

        binding.logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                // Clear Google Sign-In cache
                GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .signOut()
                        .addOnCompleteListener(requireActivity(), task -> {
                            //revoke permissions for uri
                            if (imageUri != null) {
                                requireContext().revokeUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }
                            // You can show a toast or navigate to login screen after signing out
                            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                            // Navigate back to the login screen, if needed
                            Intent intent = new Intent(requireContext(), SignInActivity.class);
                            startActivity(intent);
                            requireActivity().finish();  // End MainActivity so it doesn’t remain in the back stack
                        });
            }
        });
        binding.uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Determine permission based on Android version
                String permission = isAndroid13OrAbove ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

                // Check if permission is already granted
                if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, launch image picker
                    pickImage.launch("image/*");
                } else {
                    // Request permission
                    requestPermissionLauncher.launch(permission);
                }

            }
        });
        if(imageUri!=null){
            binding.userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
                    intent.putExtra("image_url",imageUri);// Passed the image URL to the new activity
                    startActivity(intent);
                }
            });
        }
    }

    private ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri o) {
                    if (o != null) {
                        imageUri = o;
                        updateDataBase();
                    }
                }
            });
    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission is granted, launch image picker
                    pickImage.launch("image/*");
                } else {
                    Toast.makeText(requireContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void retriveUserData() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel != null) {
                        binding.userNameInput.setText(userModel.getName());
                        binding.DescriptionInput.setText(userModel.getAbout());
                        Glide.with(requireContext()).load(userModel.getUserImage()).into(binding.userImage);
                        binding.userImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(requireContext(), ImageViewerActivity.class);
                                intent.putExtra("image_url", userModel.getUserImage()); // Passed the image URL to the new activity
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("UserError", "onCancelled: " + error.getDetails());
            }
        });
    }

    private void updateUserData() {
        String name = binding.userNameInput.getText().toString().trim();
        String about = binding.DescriptionInput.getText().toString().trim();
        databaseReference.child("Users").child(auth.getCurrentUser().getUid())
                .child("name").setValue(name);
        databaseReference.child("Users").child(auth.getCurrentUser().getUid())
                .child("about").setValue(about);
        binding.userNameInput.setEnabled(false);
        binding.DescriptionInput.setEnabled(false);
        binding.saveInfoButton.setEnabled(false);
        Toast.makeText(requireContext(), "Data Updated Successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateDataBase() {
        StorageReference imageRef = storageReference.child("User_Image/" + auth.getCurrentUser().getUid() + ".jpg");
        if (imageUri != null) {
            ((MainActivity) getActivity()).setBottomBarEnabled(false);
            binding.uploadProgressBar.setVisibility(View.VISIBLE);
            binding.interactionBlocker.setVisibility(View.VISIBLE);
            binding.uploadProgressBar.bringToFront();
            binding.uploadPic.setClickable(false);
            binding.editButton.setClickable(false);
            binding.saveInfoButton.setClickable(false);
            binding.logOutButton.setEnabled(false);
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            databaseReference.child("Users").child(auth.getCurrentUser().getUid())
                                    .child("userImage").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(requireContext(), "Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                            binding.userImage.setImageURI(imageUri);
                                            binding.uploadProgressBar.setVisibility(View.GONE);
                                            binding.interactionBlocker.setVisibility(View.GONE);
                                            binding.editButton.setClickable(true);
                                            binding.saveInfoButton.setClickable(true);
                                            binding.logOutButton.setEnabled(true);
                                            ((MainActivity) getActivity()).setBottomBarEnabled(true);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), "Image Update Failed", Toast.LENGTH_SHORT).show();
                                            binding.uploadProgressBar.setVisibility(View.GONE);
                                            binding.interactionBlocker.setVisibility(View.GONE);
                                            binding.uploadPic.setClickable(true);
                                            binding.editButton.setClickable(true);
                                            binding.saveInfoButton.setClickable(true);
                                            binding.logOutButton.setEnabled(true);
                                            ((MainActivity) getActivity()).setBottomBarEnabled(true);
                                        }
                                    });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), "Image Update Failed", Toast.LENGTH_SHORT).show();
                    binding.uploadProgressBar.setVisibility(View.GONE);
                    binding.interactionBlocker.setVisibility(View.GONE);
                    binding.logOutButton.setEnabled(true);
                    ((MainActivity) getActivity()).setBottomBarEnabled(true);
                }
            });
        }
    }
}