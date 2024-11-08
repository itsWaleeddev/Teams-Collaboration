package com.example.teamscollaboration;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.Models.TaskUploadModel;
import com.example.teamscollaboration.databinding.ActivitySubmissionDetailsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubmissionDetailsActivity extends AppCompatActivity {
    ActivitySubmissionDetailsBinding binding;
    File pdfFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySubmissionDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TaskUploadModel taskUploadModel = getIntent().getSerializableExtra("task", TaskUploadModel.class);
        binding.fileName.setText(taskUploadModel.getFileName());
        checkIfRemoteFileIsPdf(taskUploadModel.getFileUri(), isPdf -> {
            if (isPdf) {
                downloadAndDisplayPdf(taskUploadModel.getFileUri());
            } else {
                Glide.with(this).load(Uri.parse(taskUploadModel.getFileUri())).into(binding.fileImage);
            }
        });
        binding.fileImage.setOnClickListener(view -> {
            if(pdfFile!=null){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri pdfUri = FileProvider.getUriForFile(this, "com.example.teamscollaboration.fileprovider", pdfFile);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No PDF viewer found. Please install one to open this file.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Wait for the file to load", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void downloadAndDisplayPdf(String url) {
        new Thread(() -> {
            try {
                // Step 1: Download the PDF file
                URL pdfUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) pdfUrl.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                // Save the file to cache directory
                pdfFile = new File(getCacheDir(), "downloaded_temp.pdf");
                FileOutputStream outputStream = new FileOutputStream(pdfFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                // Step 2: Render the first page of the PDF
                Bitmap bitmap = displayPdfFirstPage(Uri.fromFile(pdfFile));

                // Step 3: Update the ImageView on the main thread
                runOnUiThread(() -> {
                    if (bitmap != null) {
                        binding.fileImage.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "Failed to render PDF page", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error downloading PDF", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
            runOnUiThread(() -> callback.onResult(finalIsPdf));
        }).start();
    }
    private Bitmap displayPdfFirstPage(Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
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
            Toast.makeText(this, "Error displaying PDF first page", Toast.LENGTH_SHORT).show();
            Log.d("displayPdfFirstPage", "Error rendering first page: " + e.getMessage());
            return null;
        }
    }

}