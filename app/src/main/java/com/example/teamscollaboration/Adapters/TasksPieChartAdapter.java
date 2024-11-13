package com.example.teamscollaboration.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpaceDetails;
import com.example.teamscollaboration.databinding.ItemPiechartBinding;
import com.example.teamscollaboration.databinding.ItemWorkspaceBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TasksPieChartAdapter extends RecyclerView.Adapter<TasksPieChartAdapter.ViewHolder> {
    private Context context;
    ArrayList<TasksModel> tasksModels;

    public TasksPieChartAdapter(Context context,   ArrayList<TasksModel> tasksModels) {
        this.context = context;
        this.tasksModels = tasksModels;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPiechartBinding binding = ItemPiechartBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TasksModel tasksModel = tasksModels.get(position);
        PieChart pieChart = holder.binding.taskPieChart;
        // Create data entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(tasksModel.getSubmittedCount(), "Submitted"));
        entries.add(new PieEntry(tasksModel.getUnSubmittedCount(), "Not Submitted"));

        // Create the dataset and customize the colors
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(false); // Show the values (percentages) in the slices
      //  dataSet.setValueTextSize(12f); // Customize text size if needed

        // Set custom colors: Red for "Not Submitted" and green for "Submitted" (you can choose other colors too)
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN); // Color for "Submitted"
        colors.add(Color.RED);   // Color for "Not Submitted"
        dataSet.setColors(colors); // Set the custom colors

        // Set data and customize the chart
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart)); // Display values as percentages
        pieData.setValueTextSize(12f); // Customize text size if needed
        pieChart.setUsePercentValues(true);
        pieChart.setDescription(null); // Remove description label
        pieChart.setDrawEntryLabels(false);// Hide category labels (like "Submitted", "Not Submitted")// Show values as percentages
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refresh chart
        // Customize Legend text size
        Legend legend = pieChart.getLegend();
        legend.setTextSize(14f); // Increase the text size of legend entries
        legend.setXEntrySpace(10f); // Add horizontal space between legend entries
        legend.setYEntrySpace(5f); // Add vertical space between legend entries

        // Set label in the center of the pie chart
        pieChart.setCenterText(tasksModel.getTaskName()); // Add the label in the center
        pieChart.setCenterTextSize(14f); // Set the text size for the center label
        pieChart.setCenterTextColor(Color.BLACK); // Optional: Set color for center text

        // Adjust legend's position (optional) to add more space between legend and chart
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Set vertical alignment
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Set horizontal alignment
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Change legend orientation
        legend.setDrawInside(false); // Ensure that the legend is drawn outside the chart
        // Add vertical offset to move the legend away from the chart
        legend.setYOffset(5f); // Adjust Y offset to create space between legend and chart
        // Customize padding for the pie chart
        //pieChart.setExtraOffsets(0f, 10f, 0f, 10f); // Adds padding at the top and bottom
        // Add animation to the PieChart
         pieChart.animateY(1000); // Animate along the Y-axis over 1000 milliseconds (1 second)
         //pieChart.animateXY(1000, 1000);
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return tasksModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPiechartBinding binding;
        public ViewHolder(@NonNull ItemPiechartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}