package com.example.teamscollaboration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamscollaboration.Adapters.MembersModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityAddWorkSpaceBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddWorkSpace extends AppCompatActivity {
    ActivityAddWorkSpaceBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    String created_at = null;
    List<MembersModel> selectedMembers = new ArrayList<>();
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
        binding = ActivityAddWorkSpaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        binding.chooseMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddWorkSpace.this, ChooseMembers.class);
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
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, getResources().getTextArray(R.array.priority_levels)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE); // Set the background color of the dropdown
                return view;
            }
        };

        binding.prioritySpinner.setAdapter(adapter);
        binding.submitWorkspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workSpaceName = binding.workspaceNameInput.getText().toString().trim();
                workSpaceDescription = binding.workspaceDescriptionInput.getText().toString().trim();
                deadLine = binding.deadlineDateInput.getText().toString().trim();
                priority = binding.prioritySpinner.getSelectedItem().toString().trim();

                if (workSpaceName.isEmpty()) {
                    binding.workspaceNameInput.setError("Field is Empty");
                    return;
                }

                if (workSpaceDescription.isEmpty()) {
                    binding.workspaceDescriptionInput.setError("Field is Empty");
                    return;
                }

                if (deadLine.isEmpty()) {
                    Toast.makeText(AddWorkSpace.this, "Please Choose the Deadline", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedMembers.isEmpty()) {
                    Toast.makeText(AddWorkSpace.this, "Please Choose the members for WorkSpace", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled, proceed to save the workspace
                saveWorkSpace();
                Toast.makeText(AddWorkSpace.this, "WorkSpace Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void saveWorkSpace(){
        DatabaseReference workSpaceRef = databaseReference.child("Workspaces");
        String newWorkSpaceKey = workSpaceRef.push().getKey();
        WorkSpaceModel workSpaceModel = new WorkSpaceModel(newWorkSpaceKey,workSpaceName, workSpaceDescription,
                deadLine, priority, System.currentTimeMillis(), auth.getCurrentUser().getUid(),selectedMembers, null);
        workSpaceRef.child(newWorkSpaceKey).setValue(workSpaceModel);
    }
    // DatePickerDialog method
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
}