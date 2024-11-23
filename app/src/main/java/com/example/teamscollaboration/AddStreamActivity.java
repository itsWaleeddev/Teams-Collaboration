package com.example.teamscollaboration;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.StreamModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityAddStreamBinding;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddStreamActivity extends AppCompatActivity {
    ActivityAddStreamBinding binding;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    String workSpaceKey = null;
    String topicName = null;
    String comment = null;
    Uri selectedFile = null;
    String fileName = null;
    String fileUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFile = data.getData();
            String mimeType = getContentResolver().getType(selectedFile);

            // Get the file name
            fileName = getFileName(selectedFile);

            // Display the file name in a TextView (assuming you have a TextView for it)
            binding.fileName.setText(fileName); // Example TextView for displaying file name

            // Check file type and display the first page accordingly
            if (mimeType != null) {
                if (mimeType.equals("application/pdf")) {
                    displayPdfFirstPage(selectedFile);
                }/* else if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    displayDocxFirstPage(fileUri);
                } else if (mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    displayPptxFirstPage(fileUri);
                }*/ else {
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
                }
                // Enable user to open file in external app on click
                binding.chooseFile.setOnClickListener(v -> openFileInExternalApp(selectedFile, mimeType));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddStreamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        workSpaceKey = getIntent().getStringExtra("workSpaceKey");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        binding.chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        binding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topicName = binding.topicNameInput.getText().toString().trim();
                comment = binding.topicCommentInput.getText().toString().trim();
                fileUri = selectedFile.toString();
                if (topicName.isEmpty()) {
                    binding.topicNameInput.setError("Field is Empty");
                    return;
                }

                if (comment.isEmpty()) {
                    binding.topicCommentInput.setError("Field is Empty");
                    return;
                }

                if (fileUri.isEmpty()) {
                    Toast.makeText(AddStreamActivity.this, "Please select the file first", Toast.LENGTH_SHORT).show();
                    return;
                }
                // If all fields are filled, proceed to save the stream in workspace
                binding.interactionBlocker.setVisibility(View.VISIBLE);
                binding.uploadProgressBar.setVisibility(View.VISIBLE);
                binding.uploadButton.setClickable(false);
                binding.chooseFile.setClickable(false);
                binding.uploadButton.setFocusable(false);
                binding.chooseFileButton.setFocusable(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                binding.topicNameInput.setFocusable(false);
                binding.topicCommentInput.setFocusable(false);
                binding.topicNameInput.setClickable(false);
                binding.topicCommentInput.setClickable(false);
                retriveUserData();
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

    private void openFileChooser() {
        // Intent to open file picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*"); // Allow all file types; you can specify "application/pdf" or "image/*" for specific types
        String[] mimeTypes = {"application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.presentationml.presentation"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // If targeting API 19+, use DocumentsContract to let users pick from external storage providers like Google Drive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/document/primary:"));
        }

        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    // Method to get the file name from URI
    private String getFileName(Uri uri) {
        String result = uri.getLastPathSegment();
        int cut = result.lastIndexOf('/');
        if (cut != -1) {
            result = result.substring(cut + 1);
        }
        return result;
    }

    // Method to open the selected file in an external app
    private void openFileInExternalApp(Uri fileUri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPdfFirstPage(Uri pdfUri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            PdfRenderer.Page page = pdfRenderer.openPage(0);

            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            binding.chooseFile.setImageBitmap(bitmap);
            page.close();
            pdfRenderer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveStream(String userImage, String userName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("Workspaces/"+ workSpaceKey + "/Streams/" + auth.getCurrentUser().getUid() + "/");
        String file_Name = fileName;
        StorageReference fileRef = storageRef.child(file_Name);

        // Upload the file to Firebase Storage
        UploadTask uploadTask = fileRef.putFile(Uri.parse(fileUri));
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the file's download URL after successful upload
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                StreamModel streamModel = new StreamModel(topicName, comment, uri.toString(), fileName,
                        auth.getCurrentUser().getUid(), userImage, userName, System.currentTimeMillis());
                // Reference to the stream node in Firebase
                DatabaseReference streamRef = databaseReference.child("Workspaces").child(workSpaceKey).child("streamModel");

                // Get the existing list of stream files
                streamRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if the streamModel list already exists
                        List<StreamModel> streamModels = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            // If data exists, retrieve the existing list
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                StreamModel existingStream = snapshot.getValue(StreamModel.class);
                                streamModels.add(existingStream);
                            }
                        }

                        // Add the new streamModel to the list
                        streamModels.add(streamModel);

                        // Update the streamModel list in Firebase
                        streamRef.setValue(streamModels).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddStreamActivity.this, "Stream Created Successfully", Toast.LENGTH_SHORT).show();
                                binding.uploadProgressBar.setVisibility(View.GONE);
                                binding.interactionBlocker.setVisibility(View.GONE);
                                binding.uploadButton.setText("Uploaded");
                                binding.uploadButton.setClickable(false);
                                binding.chooseFile.setClickable(true);
                                setSupportActionBar(binding.toolbar);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                                binding.topicNameInput.setFocusable(false);
                                binding.topicCommentInput.setFocusable(false);
                                binding.topicNameInput.setClickable(false);
                                binding.topicCommentInput.setClickable(false);
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(AddStreamActivity.this, "Failed to update stream", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AddStreamActivity.this, "Error retrieving stream data", Toast.LENGTH_SHORT).show();
                    }
                });

            });
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Failed to upload file", e);
        });
    }
    private void retriveUserData() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel != null) {
                       String userImage = userModel.getUserImage();
                       String userName = userModel.getName();
                       saveStream(userImage, userName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("UserError", "onCancelled: " + error.getDetails());
            }
        });
    }
}