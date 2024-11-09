package com.example.teamscollaboration;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityTaskDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskDetailsActivity extends AppCompatActivity {
    ActivityTaskDetailsBinding binding;
    TasksModel tasksModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Uri selectedFileUri;
    String fileName = null;
    String newTaskKey = null;
    String userName = null;
    String workSpaceKey = null;
    String taskStatus = null;
    String userID = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Retrieve the selected file's URI
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    fileName = getFileName(selectedFileUri);
                    binding.uploadedFileName.setText(fileName);
                    Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();

                    // Check if the file is a PDF
                    if (isPdfFile(selectedFileUri)) {
                        Bitmap bitmap = displayPdfFirstPage(selectedFileUri);
                        binding.uploadedFile.setImageBitmap(bitmap);
                        binding.uploadedFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Create an intent to view the PDF file directly using the content URI
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(selectedFileUri, "application/pdf");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant permission to read the file

                                // Check if there is a PDF viewer available
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(TaskDetailsActivity.this, "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else if (isImageFile(selectedFileUri)) {
                        Bitmap bitmap = displayImage(selectedFileUri);
                        binding.uploadedFile.setImageBitmap(bitmap);
                        binding.uploadedFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(TaskDetailsActivity.this, ImageViewerActivity.class);
                                intent.putExtra("image_url", selectedFileUri.toString()); // Passed the image URL to the new activity
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTaskDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        tasksModel = (TasksModel) getIntent().getSerializableExtra("task");
        newTaskKey = tasksModel.getTaskKey();
        workSpaceKey = tasksModel.getWorkSpaceKey();
        getTaskStatus();
        binding.toolbar.setTitle(tasksModel.getTaskName());
        binding.ownerName.setText(tasksModel.getTaskOwner());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        // Convert the timestamp to a Date object
        Date date = new Date(tasksModel.getCreated_at());
        // Format the date to a readable 12-hour format with AM/PM
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        String formattedDate = sdf.format(date);

        binding.createdDate.setText(formattedDate);
        Uri uri = Uri.parse(tasksModel.getFileUri());
        Log.d("uriCheck", "taskFile Uri" + uri.toString());
        checkIfRemoteFileIsPdf(uri.toString(), isPdf -> {
            if (isPdf) {
                downloadAndDisplayPdf(uri.toString(), new BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        binding.taskFile.setImageBitmap(bitmap); // Display the bitmap in an ImageView
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(TaskDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }, new pdfFileCallback() {
                    @Override
                    public void pdfFileReady(File pdfFile) {
                        binding.taskFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri pdfUri = FileProvider.getUriForFile(TaskDetailsActivity.this, "com.example.teamscollaboration.fileprovider", pdfFile);
                                intent.setDataAndType(pdfUri, "application/pdf");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(TaskDetailsActivity.this, "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(TaskDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Glide.with(this).load(uri).into(binding.taskFile);
                binding.taskFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(TaskDetailsActivity.this, ImageViewerActivity.class);
                        intent.putExtra("image_url", uri.toString()); // Passed the image URL to the new activity
                        startActivity(intent);
                    }
                });
            }
        });


        binding.taskFileName.setText(tasksModel.getFileName());
        binding.taskDeadline.setText(tasksModel.getDeadLine());
        binding.taskEndTime.setText(tasksModel.getEndTime());
        binding.taskDescription.setText(tasksModel.getTaskDescription());

        binding.uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(taskStatus.equals("pending")) {
                    openFileChooser();
                }
            }
        });
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(taskStatus.equals("pending")){
                    retrieveUser();
                }
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
        intent.setType("*/*"); // Allow all file types; you can specify "application/pdf" or "image/*" for specific types

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

    private boolean isImageFile(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(uri);
        return type != null && type.startsWith("image/");
    }

    private boolean isPdfFile(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(uri);
        return type != null && type.equals("application/pdf");
    }

    private Bitmap displayImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Get the EXIF orientation (if any) and correct the bitmap orientation
            int orientation = getExifOrientation(uri);
            if (orientation != 0) {
                bitmap = rotateBitmap(bitmap, orientation);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bitmap displayPdfFirstPage(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                page.close();
                pdfRenderer.close();
                return bitmap;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying PDF first page", Toast.LENGTH_SHORT).show();
            Log.d("displayPdfFirstPage", "Error rendering first page: " + e.getMessage());
            return null;
        }
    }

    private void uploadFileToFirebase() {
        if (selectedFileUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("Uploaded_Tasks/" + userID +"/");
            StorageReference fileRef = storageRef.child(tasksModel.getTaskKey() + "." + getFileExtension(selectedFileUri));

            fileRef.putFile(selectedFileUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        saveFileMetadataToDatabase(downloadUrl);
                    }))
                    .addOnFailureListener(e -> Log.d("fileUpload", "uploadFileToFirebase: " + e.getMessage()));
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void saveFileMetadataToDatabase(String downloadUrl) {
        updateTaskStatusForMember();
        TaskUploadModel taskUploadModel = new TaskUploadModel(userName, auth.getCurrentUser().getUid(),
                downloadUrl, fileName, System.currentTimeMillis());
        DatabaseReference taskUploadsRef = databaseReference.child("Tasks").child(workSpaceKey).child(newTaskKey).child("taskUploads");

        // Retrieve the current taskUploads list, add the new model, and update Firebase
        taskUploadsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the existing list or create a new one if empty
                List<TaskUploadModel> taskUploads = new ArrayList<>();

                if (task.getResult().exists()) {
                    // Deserialize existing data into a list
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        TaskUploadModel existingTask = snapshot.getValue(TaskUploadModel.class);
                        taskUploads.add(existingTask);
                    }
                }
                // Add the new TaskUploadModel
                taskUploads.add(taskUploadModel);

                // Write the updated list back to Firebase
                taskUploadsRef.setValue(taskUploads)
                        .addOnSuccessListener(aVoid -> {
                            // Success handling
                            Toast.makeText(this, "Task upload added successfully.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Failure handling
                            Toast.makeText(this, "Failed to add task upload.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("FirebaseUpdate", "Failed to retrieve task uploads.", task.getException());
            }
        });

    }

    private void retrieveUser() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userName = snapshot.child("name").getValue(String.class);
                            userID = snapshot.child("userId").getValue(String.class);
                        }
                        uploadFileToFirebase();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("userProblem", "onCancelled: " + error.getDetails());
                    }
                });

    }
    private void checkIfRemoteFileIsPdf(String url, PdfCheckCallback callback) {
        new Thread(() -> {
            boolean isPdf = false;

            try {
                // Check if the URL has a .pdf extension
                if (url.endsWith(".pdf")) {
                    isPdf = true;
                } else {
                    // Open a connection to get the file type
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("HEAD"); // We only need the headers
                    connection.connect();

                    // Get the content type from headers
                    String contentType = connection.getContentType();
                    connection.disconnect();

                    isPdf = contentType != null && contentType.equals("application/pdf");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Pass the result to the callback on the main thread
            boolean finalIsPdf = isPdf;
            runOnUiThread(() -> callback.onResult(finalIsPdf));
        }).start();
    }



    private void downloadAndDisplayPdf(String url, BitmapCallback callback, pdfFileCallback pdfCallback) {
        // Create a unique filename based on the URL (or other unique identifier)
        String fileName = getCacheFileName(url);
        File cachedPdfFile = new File(getCacheDir(), fileName);

        // Check if the file exists in the cache
        if (cachedPdfFile.exists()) {
            // If the file is already cached, directly use it
            runOnUiThread(() -> {
                pdfCallback.pdfFileReady(cachedPdfFile);
                Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));
                if (bitmap != null) {
                    callback.onBitmapReady(bitmap);
                } else {
                    callback.onError("Failed to render PDF page");
                }
            });
        } else {
            // If the file is not cached, download and save it
            new Thread(() -> {
                try {
                    // Step 1: Download the PDF file
                    URL pdfUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) pdfUrl.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();

                    // Save the file to cache directory
                    FileOutputStream outputStream = new FileOutputStream(cachedPdfFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    // Step 2: Notify the callback that the PDF file is ready
                    runOnUiThread(() -> {
                        if (cachedPdfFile.exists()) {
                            pdfCallback.pdfFileReady(cachedPdfFile);
                        } else {
                            callback.onError("Wait for the PDF file to load");
                        }
                    });

                    // Step 3: Render the first page of the PDF and display it
                    Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));

                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            callback.onBitmapReady(bitmap);
                        } else {
                            callback.onError("Failed to render PDF page");
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error downloading PDF", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    // Helper method to generate a unique cache file name based on the URL
    private String getCacheFileName(String url) {
        return String.valueOf(url.hashCode()) + ".pdf";
    }

    private void updateTaskStatusForMember() {
        DatabaseReference memberStatusRef = databaseReference.child("Tasks")
                .child(workSpaceKey)
                .child(newTaskKey)
                .child("membersList");

        memberStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    // Assuming the member ID field in your MembersModel is called "id"
                    MembersModel member = memberSnapshot.getValue(MembersModel.class);
                    if (member != null && member.getuID().equals(auth.getCurrentUser().getUid())) {
                        // Match found, update the taskStatus
                        memberSnapshot.getRef().child("taskStatus").setValue("Submitted")
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        binding.submitButton.setText("Submitted");
                                        binding.uploadFile.setText("File Uploaded");
                                        binding.uploadFile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uplaod_done, 0, 0, 0);
                                        binding.uploadFile.setClickable(false);
                                        binding.submitButton.setClickable(false);
                                        Log.d("TaskStatusUpdate", "Task status updated successfully.");
                                    } else {
                                        Log.e("TaskStatusUpdate", "Failed to update task status: " + task.getException().getMessage());
                                    }
                                });
                        break; // Exit the loop since we found the matching member
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "onCancelled: " + error.getDetails());
            }
        });
    }

    public interface PdfCheckCallback {
        void onResult(boolean isPdf);
    }
    private void retrieveUploadFile(){
        DatabaseReference fileUploadRef = databaseReference.child("Tasks")
                .child(workSpaceKey)
                .child(newTaskKey)
                .child("taskUploads");

       fileUploadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String uploadedFileUri = null;
                String uploadedFileName = null;
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    TaskUploadModel taskUploadModel = memberSnapshot.getValue(TaskUploadModel.class);
                    if (taskUploadModel != null && taskUploadModel.getuID().equals(auth.getCurrentUser().getUid())) {
                        uploadedFileUri = taskUploadModel.getFileUri();
                        uploadedFileName = taskUploadModel.getFileName();
                        if(uploadedFileUri!=null && uploadedFileName!=null){
                            binding.uploadedFileName.setText(uploadedFileName);
                            String finalFileUri = uploadedFileUri;
                            Log.d("uriCheck", "uploadedFile uri: " + finalFileUri);
                            checkIfRemoteFileIsPdf(finalFileUri, isPdf -> {
                                if (isPdf) {
                                    downloadAndDisplayPdf(finalFileUri, new BitmapCallback() {
                                        @Override
                                        public void onBitmapReady(Bitmap bitmap) {
                                            binding.uploadedFile.setImageBitmap(bitmap); // Display the bitmap in an ImageView
                                        }
                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(TaskDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }, new pdfFileCallback() {
                                        @Override
                                        public void pdfFileReady(File pdfFile) {
                                            binding.uploadedFile.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    Uri pdfUri = FileProvider.getUriForFile(TaskDetailsActivity.this, "com.example.teamscollaboration.fileprovider", pdfFile);
                                                    intent.setDataAndType(pdfUri, "application/pdf");
                                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                    try {
                                                        startActivity(intent);
                                                    } catch (ActivityNotFoundException e) {
                                                        Toast.makeText(TaskDetailsActivity.this, "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(TaskDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Glide.with(TaskDetailsActivity.this).load(Uri.parse(finalFileUri)).into(binding.uploadedFile);
                                    binding.uploadedFile.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(TaskDetailsActivity.this, ImageViewerActivity.class);
                                            intent.putExtra("image_url", finalFileUri); // Passed the image URL to the new activity
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                        break; // Exit the loop since we found the matching userUpload
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "onCancelled: " + error.getDetails());
            }
        });
    }
    private void getTaskStatus(){
        DatabaseReference memberStatusRef = databaseReference.child("Tasks")
                .child(workSpaceKey)
                .child(newTaskKey)
                .child("membersList");

        memberStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    // Assuming the member ID field in your MembersModel is called "id"
                    MembersModel member = memberSnapshot.getValue(MembersModel.class);
                    if (member != null && member.getuID().equals(auth.getCurrentUser().getUid())) {
                        taskStatus = member.getTaskStatus();
                        if(taskStatus!=null && taskStatus.equals("Submitted")){
                            retrieveUploadFile();
                            binding.submitButton.setText("Submitted");
                            binding.submitButton.setClickable(false);
                            binding.uploadFile.setText("File Uploaded");
                            binding.uploadFile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uplaod_done, 0, 0, 0);
                            binding.uploadFile.setClickable(false);
                            break;
                        }
                        break; // Exit the loop since we found the matching member
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "onCancelled: " + error.getDetails());
            }
        });
    }
    public interface BitmapCallback {
        void onBitmapReady(Bitmap bitmap);
        void onError(String error);
    }
    public interface pdfFileCallback {
       void pdfFileReady(File pdfFile);
        void onError(String error);
    }
    private int getExifOrientation(Uri uri) {
        try {
            // Get the EXIF metadata from the image
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(uri));

            // Retrieve the orientation tag from EXIF data
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            return orientation;
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // Return 0 if no EXIF data is found
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap; // No rotation needed
        }

        // Create a new bitmap with the corrected orientation
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}