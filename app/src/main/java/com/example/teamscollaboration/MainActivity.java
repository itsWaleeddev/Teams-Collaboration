package com.example.teamscollaboration;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.teamscollaboration.databinding.ActivityMainBinding;
import com.example.teamscollaboration.fragments.AddWorkSpaceFragment;
import com.example.teamscollaboration.fragments.CalendarFragment;
import com.example.teamscollaboration.fragments.DashboardFragment;
import com.example.teamscollaboration.fragments.HomeFragment;
import com.example.teamscollaboration.fragments.ProfileFragment;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private boolean isDataFetched = false;
    private boolean isBottomBarEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new HomeFragment())
                    .commit();
        }
        binding.bottomBar.selectTabAt(0, true);
        binding.bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                Fragment selectedFragment = null;
                if (!isBottomBarEnabled) {
                    // Prevent tab switching by re-selecting the previous tab
                    binding.bottomBar.selectTabAt(4, true);
                    Toast toast = Toast.makeText(MainActivity.this, "Action blocked during loading", Toast.LENGTH_SHORT);
                    toast.show();
                    new android.os.Handler().postDelayed(toast::cancel, 300);
                    return;
                }
                // Switch between fragments based on the selected tab's index
                switch (i1) {
                    case 0:
                        selectedFragment = new HomeFragment();
                        break;
                    case 1:
                        selectedFragment = new CalendarFragment();
                        break;
                    case 2:
                        selectedFragment = new AddWorkSpaceFragment();
                        break;
                    case 3:
                        selectedFragment = new DashboardFragment();
                        break;
                    case 4:
                        selectedFragment = new ProfileFragment();
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
    public void setBottomBarEnabled(boolean enabled) {
        isBottomBarEnabled = enabled;
    }
}