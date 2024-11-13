package com.example.teamscollaboration;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityAddTaskBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    ActivityAddTaskBinding binding;
    WorkSpaceModel workSpaceModel;
    List<MembersModel> selectedMembers = new ArrayList<>();
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String taskName = null;
    String taskDescription = null;
    String taskDeadline = null;
    String taskEndTime = null;
    String workSpaceKey = null;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Uri selectedFileUri;
    String fileName = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            List<MembersModel> selectedMembers = (ArrayList<MembersModel>) data.getSerializableExtra("selectedMembers");
            if (selectedMembers != null) {
                this.selectedMembers = selectedMembers;
            }
            if(!selectedMembers.isEmpty()){
                binding.chooseMembersButton.setText("Assigned Members");
            }
        }
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Retrieve the selected file's URI
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    fileName = getFileName(selectedFileUri);
                    binding.fileName.setText(fileName);
                    Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();

                    // Check if the file is a PDF
                    if (isPdfFile(selectedFileUri)) {
                        displayPdfFirstPage(selectedFileUri);
                    } else if (isImageFile(selectedFileUri)) {
                        displayImage(selectedFileUri);
                    }
                }
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        workSpaceModel = (WorkSpaceModel) getIntent().getSerializableExtra("workSpace");
        workSpaceKey = workSpaceModel.getWorkSpaceKey();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        binding.chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        binding.submitTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskName = binding.taskNameInput.getText().toString().trim();
                taskDescription = binding.taskDescriptionInput.getText().toString().trim();
                taskDeadline = binding.deadlineDateInput.getText().toString().trim();
                taskEndTime = binding.deadlineTimeInput.getText().toString().trim();

                if (taskName.isEmpty()) {
                    binding.taskNameInput.setError("Field is Empty");
                    return;
                }

                if (taskDescription.isEmpty()) {
                    binding.taskDescriptionInput.setError("Field is Empty");
                    return;
                }

                if (taskDeadline.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please Choose the Deadline", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (taskEndTime.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please Choose the End Time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedMembers.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please Choose the members for Task", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled, proceed to save the workspace
                retrieveUserData();
                Toast.makeText(AddTaskActivity.this, "Task Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        binding.chooseMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTaskActivity.this, TasksMembers.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
                if(!selectedMembers.isEmpty()) {
                    intent.putExtra("Members", (Serializable) selectedMembers);
                }
                startActivityForResult(intent, 0);
            }
        });
        binding.deadlineDateInput.setInputType(InputType.TYPE_NULL);
        binding.deadlineDateInput.setFocusable(false);
        binding.deadlineDateInput.setFocusableInTouchMode(false);
        binding.deadlineDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        binding.deadlineTimeInput.setInputType(InputType.TYPE_NULL);
        binding.deadlineTimeInput.setFocusable(false);
        binding.deadlineTimeInput.setFocusableInTouchMode(false);
        binding.deadlineTimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);

                        // Format selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = dateFormat.format(calendar.getTime());

                        // Set formatted date to EditText
                        binding.deadlineDateInput.setText(formattedDate);
                    }
                },
                // Set default date (current date)
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    // TimePickerDialog method
    private void showTimePickerDialog() {
        // Get current time as default
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Create TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);

                        // Format selected time with AM/PM
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        String formattedTime = timeFormat.format(selectedTime.getTime());

                        // Set formatted time to EditText
                        binding.deadlineTimeInput.setText(formattedTime);
                    }
                },
                currentHour, currentMinute, false); // 'false' for 12-hour format with AM/PM

        timePickerDialog.show();
    }

    private void saveTask(String userName){
        DatabaseReference tasksRef = databaseReference.child("Tasks").child(workSpaceKey);
        String newTaskKey = tasksRef.push().getKey();
        // Upload the file to Firebase Storage
        TasksModel tasksModel = new TasksModel(newTaskKey, taskName,taskDescription, taskDeadline,
                 taskEndTime, System.currentTimeMillis(), auth.getCurrentUser().getUid(), userName,
                selectedMembers, null, workSpaceKey, "pending", null,
                null, new ArrayList<>(), 0, selectedMembers.size());
       tasksRef.child(newTaskKey).setValue(tasksModel);
       uploadFileToFirebase(newTaskKey);
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

    private void displayImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            binding.chooseFile.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying image", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPdfFirstPage(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                binding.chooseFile.setImageBitmap(bitmap);

                page.close();
                pdfRenderer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying PDF first page", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToFirebase(String newTaskKey) {
        if (selectedFileUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("Tasks/");
            StorageReference fileRef = storageRef.child( newTaskKey + "." + getFileExtension(selectedFileUri));
            fileRef.putFile(selectedFileUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        saveFileMetadataToDatabase(downloadUrl, newTaskKey);
                    }))
                    .addOnFailureListener(e -> Log.d("fileUpload", "uploadFileToFirebase: " + e.getMessage()));
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void saveFileMetadataToDatabase(String downloadUrl, String newTaskKey) {
        DatabaseReference tasksRef = databaseReference.child("Tasks").child(workSpaceKey);
        tasksRef.child(newTaskKey).child("fileUri").setValue(downloadUrl);
        if(fileName!=null) {
            databaseReference.child("Tasks").child(workSpaceKey);
            tasksRef.child(newTaskKey).child("fileName").setValue(fileName);
        }
    }
    private void retrieveUserData() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    String userName = userModel.getName();
                    saveTask(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }
}