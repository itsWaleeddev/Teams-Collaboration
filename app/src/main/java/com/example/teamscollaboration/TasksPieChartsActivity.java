package com.example.teamscollaboration;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.TasksPieChartAdapter;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.databinding.ActivityTasksPieChartsBinding;

import java.util.ArrayList;

public class TasksPieChartsActivity extends AppCompatActivity {
    ActivityTasksPieChartsBinding binding;
    ArrayList<TasksModel> tasksModels = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTasksPieChartsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.toolbar.setTitle("Pie Charts");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        tasksModels = (ArrayList<TasksModel>) getIntent().getSerializableExtra("TasksList");
        setAdapter();
    }
    private void setAdapter(){
        TasksPieChartAdapter tasksPieChartAdapter = new TasksPieChartAdapter(this, tasksModels);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(tasksPieChartAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}