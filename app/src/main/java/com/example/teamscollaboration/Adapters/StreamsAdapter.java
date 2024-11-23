package com.example.teamscollaboration.Adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.AdminTaskDetails;
import com.example.teamscollaboration.Models.StreamModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.TaskDetailsActivity;
import com.example.teamscollaboration.databinding.ItemtaskBinding;
import com.example.teamscollaboration.databinding.StreamItemBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StreamsAdapter extends RecyclerView.Adapter<StreamsAdapter.ViewHolder> {
    private Context context;
    List<StreamModel> streamModels;
    File pdfFile;

    public StreamsAdapter(Context context, List<StreamModel> streamModels) {
        this.context = context;
        this.streamModels = streamModels;
    }

    // where to get the single card as a viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        StreamItemBinding binding = StreamItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    //what will happen after we create the viewholder object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StreamModel streamModel = streamModels.get(position);
        holder.binding.userName.setText(streamModel.getUserName());
        Glide.with(context).load(streamModel.getUserImage()).into(holder.binding.userImage);
        String topicName = "Topic: " + streamModel.getTopicName().toUpperCase();
        holder.binding.topicName.setText(topicName);
        String topicDescription = "Description: " + streamModel.getTopicComment();
        holder.binding.topicDescription.setText(topicDescription);
        holder.binding.fileName.setText(streamModel.getFileName());
        Date date = new Date(streamModel.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd");
        String formattedDate = sdf.format(date);
        holder.binding.date.setText(formattedDate);
        holder.downloadAndDisplayPdf(streamModel.getTopicFile());
        holder.binding.fileImage.setOnClickListener(view -> {
            if(pdfFile!=null && pdfFile.exists()){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri pdfUri = FileProvider.getUriForFile(context, "com.example.teamscollaboration.fileprovider", pdfFile);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(context, "Wait for the file to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //How many Items?
    @Override
    public int getItemCount() {
        return streamModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        StreamItemBinding binding;

        public ViewHolder(@NonNull StreamItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void downloadAndDisplayPdf(String url) {
            // Create a unique filename based on the URL (or other unique identifier)
            String fileName = getCacheFileName(url);
            File cachedPdfFile = new File(context.getCacheDir(), fileName);

            // Check if the file exists in the cache
            if (cachedPdfFile.exists()) {
                // If the file is already cached, directly use it
                pdfFile = cachedPdfFile;
                Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));
                if (bitmap != null) {
                    binding.fileImage.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(context, "File loading failed", Toast.LENGTH_SHORT).show();
                }

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

                        pdfFile = cachedPdfFile;

                        // Step 3: Render the first page of the PDF and display it
                        Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(cachedPdfFile));

                        if (bitmap != null) {
                            ((Activity) context).runOnUiThread(() ->
                            binding.fileImage.setImageBitmap(bitmap));
                        } else {
                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "File loading failed", Toast.LENGTH_SHORT).show());
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error downloading PDF", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        }

        // Helper method to generate a unique cache file name based on the URL
        private String getCacheFileName(String url) {
            return String.valueOf(url.hashCode()) + ".pdf";
        }

        private Bitmap displayPdfFirstPage(Uri uri) {
            try {
                ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
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
                Toast.makeText(context, "Error displaying PDF first page", Toast.LENGTH_SHORT).show();
                Log.d("displayPdfFirstPage", "Error rendering first page: " + e.getMessage());
                return null;
            }
        }
    }
}
