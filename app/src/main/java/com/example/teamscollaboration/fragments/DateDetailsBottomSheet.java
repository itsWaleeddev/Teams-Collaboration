package com.example.teamscollaboration.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.example.teamscollaboration.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.teamscollaboration.databinding.FragmentDateDetailsBottomSheetListDialogBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class DateDetailsBottomSheet extends BottomSheetDialogFragment {
    private FragmentDateDetailsBottomSheetListDialogBinding binding;
    private static final String ARG_DETAILS = "details";

    public static DateDetailsBottomSheet newInstance(List<String> details) {
        DateDetailsBottomSheet fragment = new DateDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_DETAILS, new ArrayList<>(details));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDateDetailsBottomSheetListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            ArrayList<String> detailsList = getArguments().getStringArrayList(ARG_DETAILS);
            int counter = 0;
            if (detailsList != null && !detailsList.isEmpty()) {
                StringBuilder detailsText = new StringBuilder();
                StringBuilder dotText = new StringBuilder();
                for (String detail : detailsList) {
                    if(detail.equals("No Event Scheduled")){
                        binding.dottextView.setVisibility(View.GONE);
                        detailsText.append(detail);
                        break;
                    }
                    else{
                        binding.dottextView.setVisibility(View.VISIBLE);
                        if(counter == detailsList.size()-1){
                            dotText.append("•");
                            detailsText.append(detail.toUpperCase()).append(" Deadline");
                        }
                        else{
                            detailsText.append(detail.toUpperCase()).append(" Deadline").append("\n");
                            dotText.append("•").append("\n");
                        }
                    }
                    counter++;
                }
                binding.dottextView.setText(dotText.toString());
                binding.detailsTextView.setText(detailsText.toString());
                if(binding.detailsTextView.getText().toString().equals("No Event Scheduled")){
                    binding.detailsTextView.setTextColor(getResources().getColor(R.color.primary));
                    binding.getRoot().setBackgroundColor(getResources().getColor(R.color.background));
                }
                else{
                    binding.detailsTextView.setTextColor(getResources().getColor(R.color.primary));
                    binding.getRoot().setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}