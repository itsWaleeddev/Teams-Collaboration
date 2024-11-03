package com.example.teamscollaboration;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.teamscollaboration.databinding.ActivityMemberDetailsBinding;

public class MemberDetailsActivity extends AppCompatActivity {
    ActivityMemberDetailsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMemberDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String name = getIntent().getStringExtra("name");
        String about = getIntent().getStringExtra("about");
        String imageUrl = getIntent().getStringExtra("image");
        binding.name.setText(name);
        Glide.with(this).load(imageUrl).into(binding.memberImage);
        if(about.isEmpty()){
            binding.Description.setText("Not Any Information Provided by User");
        }
        else{
            binding.Description.setText(about);
        }
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}