package com.example.teamscollaboration;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.databinding.ActivityWorkSpaceDetailsBinding;
import com.example.teamscollaboration.fragments.MembersFragment;
import com.example.teamscollaboration.fragments.StreamFragment;
import com.example.teamscollaboration.fragments.TasksFragment;

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
        binding.toolbar.setTitle(workSpaceModel.getWorkSpaceName().toUpperCase());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new StreamFragment())
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
                        selectedFragment = new StreamFragment();
                        break;
                    case 1:
                        selectedFragment = new TasksFragment();
                        break;
                    case 2:
                        selectedFragment = new MembersFragment();
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