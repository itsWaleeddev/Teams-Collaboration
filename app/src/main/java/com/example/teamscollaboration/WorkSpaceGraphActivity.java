package com.example.teamscollaboration;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.databinding.ActivityWorkSpaceGraphBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class WorkSpaceGraphActivity extends AppCompatActivity {
    ActivityWorkSpaceGraphBinding binding;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ArrayList<TasksModel> tasksModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkSpaceGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String workSpaceKey = getIntent().getStringExtra("WorkSpaceKey");
        retrieveTasks(workSpaceKey);
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void retrieveTasks(String workSpaceKey) {
        databaseReference.child("Tasks").child(workSpaceKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final int totalTasks = (int) snapshot.getChildrenCount();
                    tasksModels.clear(); // Clear list to prevent duplication

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String taskKey = dataSnapshot.getKey();
                        databaseReference.child("Tasks").child(workSpaceKey).child(taskKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        TasksModel tasksModel = snapshot.getValue(TasksModel.class);
                                        if (tasksModel != null) {
                                            tasksModels.add(tasksModel);
                                        }

                                        // Check if all tasks have been loaded
                                        if (tasksModels.size() == totalTasks) {
                                            displayChart();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("taskError", "onCancelled: " + error.getDetails());
                                    }
                                });
                    }
                    binding.nextButton1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(WorkSpaceGraphActivity.this, TasksPieChartsActivity.class);
                            intent.putExtra("TasksList",(Serializable) tasksModels);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("taskError", "onCancelled: " + error.getDetails());
            }
        });
    }

    private void displayChart() {
        BarChart barChart = binding.workspaceBarChart;
        List<BarDataSet> dataSets = new ArrayList<>();
        int taskIndex = 0;

        // Loop through each task and create a BarDataSet for it
        for (TasksModel tasksModel : tasksModels) {
            float progress = (float) tasksModel.getSubmittedCount() /
                    (tasksModel.getSubmittedCount() + tasksModel.getUnSubmittedCount()) * 100;

            // Create a single BarEntry for this task with its progress value
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(taskIndex++, progress));

            // Create a BarDataSet for this single entry and give it the task name as a label
            BarDataSet dataSet = new BarDataSet(entries, tasksModel.getTaskName());
            dataSet.setColor(ColorTemplate.MATERIAL_COLORS[taskIndex % ColorTemplate.MATERIAL_COLORS.length]);
            dataSet.setValueTextSize(12F);
            // dataSet.setDrawValues(false); // Optional: hide values on bars

            dataSets.add(dataSet);
        }

        // Combine all datasets into BarData
        BarData barData = new BarData();
        for (BarDataSet dataSet : dataSets) {
            barData.addDataSet(dataSet);
        }

        barChart.setData(barData);
        barChart.animateY(1000); // Animate the bars appearing vertically

        //barChart.setExtraOffsets(5, 5, 5, 5); // Add margins if needed
        barChart.setFitBars(true); // Makes bars fit nicely in the chart
        barChart.getDescription().setEnabled(false); // Disable description label
        // Customize x-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawLabels(false); // Hide x-axis labels
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false); // Remove axis line for cleaner look

        barChart.getAxisLeft().setDrawGridLines(false); // Remove grid lines on the y-axis
        barChart.getAxisRight().setEnabled(false); // Hide the right axis

        // Customize y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setTextSize(15f);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD); // Set the text to bold
        leftAxis.setAxisMinimum(0f); // Set min value
        leftAxis.setAxisMaximum(100f); // Set max value

        Legend legend = barChart.getLegend();
        legend.setEnabled(true); // Disable legend if not needed
        // Customizing legend if enabled
        legend.setTextColor(Color.DKGRAY);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(15f); // Add horizontal space between legend entries
        legend.setTextSize(12f);    // Adjust text size if necessary
        legend.setWordWrapEnabled(true); // Enable word wrapping if needed for long names

        barChart.setDrawBorders(false);
        // Customize the border color
      //  barChart.setBorderColor(Color.GRAY); // Set your preferred color for the border
        // Customize the border width
       // barChart.setBorderWidth(2f); // Set the border thickness in dp (e.g., 2f for 2dp)
        // Customize background color inside the chart borders (optional)
        barChart.setBackgroundColor(getResources().getColor(R.color.background)); // Set background color inside the border

        barChart.invalidate(); // Refresh chart
    }

}