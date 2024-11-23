package com.example.teamscollaboration.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.ChooseMembers;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.databinding.FragmentAddWorkSpaceBinding;
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
import java.util.Random;

public class AddWorkSpaceFragment extends Fragment {
    FragmentAddWorkSpaceBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String workSpaceName = null;
    String workSpaceDescription = null;
    String deadLine = null;
    String priority = null;
    String created_at = null;
    List<MembersModel> selectedMembers = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddWorkSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded() && getActivity() != null) {
            binding.chooseMembersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), ChooseMembers.class);
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
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(requireContext(), R.layout.spinner_item, getResources().getTextArray(R.array.priority_levels)) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    // drawable = ContextCompat.getDrawable(requireContext(), R.drawable.edittextshape_2);
                    view.setBackgroundColor(getResources().getColor(R.color.light_2)); // Set the background color of the dropdown
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
                        Toast.makeText(requireContext(), "Please Choose the Deadline", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedMembers.isEmpty()) {
                        Toast.makeText(requireContext(), "Please Choose the members for WorkSpace", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // If all fields are filled, proceed to save the workspace
                    retrieveUserData(); //first retreive userName then saveWorkSpace called init
                    Toast.makeText(requireContext(), "WorkSpace Created Successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            List<MembersModel> selectedMembers = (ArrayList<MembersModel>) data.getSerializableExtra("selectedMembers");
            if (selectedMembers != null) {
                this.selectedMembers = selectedMembers;
            }
        }
        if(!selectedMembers.isEmpty()){
            binding.chooseMembersButton.setText("Update Members");
        }
    }

    private void saveWorkSpace(String userName, String userImage) {
        DatabaseReference workSpaceRef = databaseReference.child("Workspaces");
        String newWorkSpaceKey = workSpaceRef.push().getKey();
        int[] backgrounds = {
                R.drawable.two_tone_one,
                R.drawable.two_tone_second
        };
        int randomBackground = backgrounds[new Random().nextInt(backgrounds.length)];
        WorkSpaceModel workSpaceModel = new WorkSpaceModel(newWorkSpaceKey, workSpaceName, workSpaceDescription,
                deadLine, priority, System.currentTimeMillis(), auth.getCurrentUser().getUid(),
                userName, selectedMembers, "No Leader Yet", userImage, null, randomBackground);
        workSpaceRef.child(newWorkSpaceKey).setValue(workSpaceModel);
    }

    // DatePickerDialog method
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                R.style.CustomDatePickerTheme,
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
        datePickerDialog.setOnShowListener(dialog -> {
            // Access the dialog's buttons
            Button positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            // Change button text colors
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        });
        datePickerDialog.show();
    }
    private void retrieveUserData() {
        databaseReference.child("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(userModel!=null){
                        String userName = userModel.getName();
                        String userImage = userModel.getUserImage();
                        saveWorkSpace(userName, userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("databaseError", "onCancelled: " + error.getMessage());
            }
        });
    }
}