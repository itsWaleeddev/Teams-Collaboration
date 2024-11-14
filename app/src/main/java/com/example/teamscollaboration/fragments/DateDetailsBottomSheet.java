package com.example.teamscollaboration.fragments;

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
                for (String detail : detailsList) {
                    if(detail.equals("No Event Scheduled")){
                        detailsText.append(detail);
                        break;
                    }
                    else{
                        if(counter == detailsList.size()-1){
                            detailsText.append("• ").append(detail).append(" Deadline");
                        }
                        else{
                            detailsText.append("• ").append(detail).append(" Deadline").append("\n");
                        }
                    }
                    counter++;
                }
                binding.detailsTextView.setText(detailsText.toString());
                if(binding.detailsTextView.getText().toString().equals("No Event Scheduled")){
                    binding.detailsTextView.setTextColor(getResources().getColor(R.color.bblue));
                }
                else{
                    binding.detailsTextView.setTextColor(getResources().getColor(R.color.red));

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