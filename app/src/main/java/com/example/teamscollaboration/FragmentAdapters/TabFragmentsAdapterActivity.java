package com.example.teamscollaboration.FragmentAdapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.teamscollaboration.fragments.SubmissionsFragment;
import com.example.teamscollaboration.fragments.TaskDetailsFragment;
import com.example.teamscollaboration.fragments.TaskMembersFragment;

public class TabFragmentsAdapterActivity extends FragmentStateAdapter {

    public TabFragmentsAdapterActivity(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TaskDetailsFragment();
            case 1:
                return new TaskMembersFragment();
            case 2:
                return new SubmissionsFragment();
            default:
                return new TaskDetailsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}
