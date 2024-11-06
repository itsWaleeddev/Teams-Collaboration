package com.example.teamscollaboration;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teamscollaboration.Adapters.WorkSpaceAdapter;
import com.example.teamscollaboration.Adapters.WorkSpaceDetailsAdapter;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.UserModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityWorkSpaceDetailsBinding;
import com.example.teamscollaboration.fragments.AddWorkSpaceFragment;
import com.example.teamscollaboration.fragments.ChatsFragment;
import com.example.teamscollaboration.fragments.DashboardFragment;
import com.example.teamscollaboration.fragments.HomeFragment;
import com.example.teamscollaboration.fragments.MembersFragment;
import com.example.teamscollaboration.fragments.ProfileFragment;
import com.example.teamscollaboration.fragments.TasksFragment;
import com.example.teamscollaboration.fragments.WorkspaceDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class WorkSpaceDetails extends AppCompatActivity {
    ActivityWorkSpaceDetailsBinding binding;
    WorkSpaceModel workSpaceModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkSpaceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        workSpaceModel = (WorkSpaceModel) getIntent().getSerializableExtra("workSpace");
        binding.toolbar.setTitle(workSpaceModel.getWorkSpaceName());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new TasksFragment())
                    .commit();
        }
        binding.bottomBar.selectTabAt(0, true);
        binding.bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                Fragment selectedFragment = null;

                // Switch between fragments based on the selected tab's index
                switch (i1) {
                    case 0:
                        selectedFragment = new TasksFragment();
                        break;
                    case 1:
                        selectedFragment = new MembersFragment();
                        break;
                    case 2:
                        selectedFragment = new WorkspaceDetailsFragment();
                        break;
                }

                // Make sure to replace the fragment in your FragmentContainerView
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainerView, selectedFragment)
                            .commit();
                }
            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {
                //reselection logic if needed
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
}