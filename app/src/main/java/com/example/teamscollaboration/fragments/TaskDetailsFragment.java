package com.example.teamscollaboration.fragments;

import static android.app.ProgressDialog.show;
import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.ImageViewerActivity;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.TaskDetailsActivity;
import com.example.teamscollaboration.databinding.FragmentTaskDetailsBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TaskDetailsFragment extends Fragment {
    FragmentTaskDetailsBinding binding;
    TasksModel tasksModel;
    View pieChartView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksModel = (TasksModel) requireActivity().getIntent().getSerializableExtra("task");
        String title  = "Title: " + tasksModel.getTaskName().toUpperCase();
        binding.taskTitle.setText(title);
        binding.taskDescription.setText(tasksModel.getTaskDescription());
        String deadLine = "Deadline: " + tasksModel.getDeadLine();
        binding.taskDeadline.setText(deadLine);
        String endTime = "EndTime: " + tasksModel.getEndTime();
        binding.taskEndTime.setText(endTime);
        Uri uri = Uri.parse(tasksModel.getFileUri());
        checkIfRemoteFileIsPdf(uri.toString(), isPdf -> {
            if (isPdf) {
                downloadAndDisplayPdf(uri.toString(), new TaskDetailsActivity.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        binding.taskFile.setImageBitmap(bitmap); // Display the bitmap in an ImageView
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }, new TaskDetailsActivity.pdfFileCallback() {
                    @Override
                    public void pdfFileReady(File pdfFile) {
                        binding.taskFile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri pdfUri = FileProvider.getUriForFile(requireContext(), "com.example.teamscollaboration.fileprovider", pdfFile);
                                intent.setDataAndType(pdfUri, "application/pdf");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(requireContext(), "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Glide.with(this).load(uri).into(binding.taskFile);
                binding.taskFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
                        intent.putExtra("image_url", uri.toString()); // Passed the image URL to the new activity
                        startActivity(intent);
                    }
                });
            }
        });
        binding.taskFileName.setText(tasksModel.getFileName());
        // Inflate the single item layout
        pieChartView = LayoutInflater.from(requireContext()).inflate(R.layout.item_piechart, binding.pieChartView, false);
        setPieChart();

    }

    private Bitmap displayPdfFirstPage(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = requireActivity().getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                page.close();
                pdfRenderer.close();
                return bitmap;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error displaying PDF first page", Toast.LENGTH_SHORT).show();
            Log.d("displayPdfFirstPage", "Error rendering first page: " + e.getMessage());
            return null;
        }
    }

    private void checkIfRemoteFileIsPdf(String url, TaskDetailsActivity.PdfCheckCallback callback) {
        new Thread(() -> {
            boolean isPdf = false;

            try {
                // Check if the URL has a .pdf extension
                if (url.endsWith(".pdf")) {
                    isPdf = true;
                } else {
                    // Open a connection to get the file type
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("HEAD"); // We only need the headers
                    connection.connect();

                    // Get the content type from headers
                    String contentType = connection.getContentType();
                    connection.disconnect();

                    isPdf = contentType != null && contentType.equals("application/pdf");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Pass the result to the callback on the main thread
            boolean finalIsPdf = isPdf;
            ((Activity) requireContext()).runOnUiThread(() -> callback.onResult(finalIsPdf));
        }).start();
    }


    private void downloadAndDisplayPdf(String url, TaskDetailsActivity.BitmapCallback callback, TaskDetailsActivity.pdfFileCallback pdfCallback) {
        // Create a unique filename based on the URL (or other unique identifier)
        String fileName = getCacheFileName(url);
        File cachedPdfFile = new File(requireContext().getCacheDir(), fileName);

        // Check if the file exists in the cache
        if (cachedPdfFile.exists()) {
            // If the file is already cached, directly use it
            ((Activity) requireContext()).runOnUiThread(() -> {
                pdfCallback.pdfFileReady(cachedPdfFile);
                Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));
                if (bitmap != null) {
                    callback.onBitmapReady(bitmap);
                } else {
                    callback.onError("Failed to render PDF page");
                }
            });
        } else {
            // If the file is not cached, download and save it
            new Thread(() -> {
                try {
                    // Step 1: Download the PDF file
                    URL pdfUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) pdfUrl.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();

                    // Save the file to cache directory
                    FileOutputStream outputStream = new FileOutputStream(cachedPdfFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    // Step 2: Notify the callback that the PDF file is ready
                    ((Activity) requireContext()).runOnUiThread(() -> {
                        if (cachedPdfFile.exists()) {
                            pdfCallback.pdfFileReady(cachedPdfFile);
                        } else {
                            callback.onError("Wait for the PDF file to load");
                        }
                    });

                    // Step 3: Render the first page of the PDF and display it
                    Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));

                    ((Activity) requireContext()).runOnUiThread(() -> {
                        if (bitmap != null) {
                            callback.onBitmapReady(bitmap);
                        } else {
                            callback.onError("Failed to render PDF page");
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    ((Activity) requireContext()). runOnUiThread(() -> Toast.makeText(requireContext(), "Error downloading PDF", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    // Helper method to generate a unique cache file name based on the URL
    private String getCacheFileName(String url) {
        return String.valueOf(url.hashCode()) + ".pdf";
    }

    private void setPieChart(){
        PieChart pieChart = pieChartView.findViewById(R.id.taskPieChart);
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
        colors.add(getResources().getColor(R.color.littledarkgreen)); // Color for "Submitted"
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
        binding.pieChartView.addView(pieChartView);
    }
}