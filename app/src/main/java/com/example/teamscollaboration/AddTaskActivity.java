package com.example.teamscollaboration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamscollaboration.Adapters.MembersModel;
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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {
    ActivityAddTaskBinding binding;
    WorkSpaceModel workSpaceModel;
    List<MembersModel> selectedMembers = new ArrayList<>();
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String taskName = null;
    String taskDescription = null;
    String taskDeadline = null;
    String priority = null;
    String workSpaceKey = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            List<MembersModel> selectedMembers = (ArrayList<MembersModel>) data.getSerializableExtra("selectedMembers");
            if (selectedMembers != null) {
                this.selectedMembers = selectedMembers;
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
        binding.submitTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskName = binding.taskNameInput.getText().toString().trim();
                taskDescription = binding.taskDescriptionInput.getText().toString().trim();
                taskDeadline = binding.deadlineDateInput.getText().toString().trim();
                priority = binding.prioritySpinner.getSelectedItem().toString().trim();

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

                if (selectedMembers.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please Choose the members for Task", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled, proceed to save the workspace
                saveTask();
                Toast.makeText(AddTaskActivity.this, "Task Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        binding.chooseMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTaskActivity.this, TasksMembers.class);
                intent.putExtra("workSpace", (Serializable) workSpaceModel);
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
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item,
                getResources().getTextArray(R.array.priority_levels)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE); // Set the background color of the dropdown
                return view;
            }
        };

        binding.prioritySpinner.setAdapter(adapter);
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
    private void saveTask(){
        DatabaseReference tasksRef = databaseReference.child("Tasks").child(workSpaceKey);
        String newTaskKey = tasksRef.push().getKey();
        TasksModel tasksModel = new TasksModel(newTaskKey, taskName,taskDescription, taskDeadline,
                 priority, System.currentTimeMillis(), auth.getCurrentUser().getUid(),
                selectedMembers, null, workSpaceKey, "pending");
       tasksRef.child(newTaskKey).setValue(tasksModel);
    }
}