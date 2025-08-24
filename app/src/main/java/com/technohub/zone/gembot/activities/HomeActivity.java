package com.technohub.zone.gembot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.technohub.zone.gembot.R;

public class HomeActivity extends AppCompatActivity {

    private ImageView btnSettings;
    private Button btnStartChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bind UI elements
        btnSettings = findViewById(R.id.btnSettings);
        btnStartChat = findViewById(R.id.btnStartChat);

        // Open settings page
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Open chat page
        btnStartChat.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
        });
    }
}
