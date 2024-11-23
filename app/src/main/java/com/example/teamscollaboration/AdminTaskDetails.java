package com.example.teamscollaboration;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.AssignedMembersAdapter;
import com.example.teamscollaboration.Adapters.TaskSubmissionsAdapter;
import com.example.teamscollaboration.FragmentAdapters.TabFragmentsAdapterActivity;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.databinding.ActivityAdminTaskDetailsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AdminTaskDetails extends AppCompatActivity {
    ActivityAdminTaskDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminTaskDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TasksModel tasksModel = (TasksModel) getIntent().getSerializableExtra("task");
        String title = tasksModel.getTaskName().toUpperCase() + " Details";
        binding.collapsingToolbar.setTitle(title);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        //set up the adapter
        TabFragmentsAdapterActivity tabFragmentsAdapterActivity = new TabFragmentsAdapterActivity(this);
        binding.viewPager.setAdapter(tabFragmentsAdapterActivity);
        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Details");
                   // tab.setIcon(R.drawable.detailsicon);
                    break;
                case 1:
                    tab.setText("Members");
                  //  tab.setIcon(R.drawable.members);
                    break;
                case 2:
                    tab.setText("Submissions");
                  //tab.setIcon(R.drawable.submissions);
                    break;
            }
        }).attach();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back navigation (finish the current activity or navigate up)
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

        /*
        if(taskUploadModels.isEmpty()){
            binding.submissionsLabel.setText("No Submissions");
        }
        */
