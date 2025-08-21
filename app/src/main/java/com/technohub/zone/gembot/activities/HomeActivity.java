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

        // Initialize views
        btnSettings = findViewById(R.id.btnSettings);
        btnStartChat = findViewById(R.id.btnStartChat);

        // Settings icon click -> open SettingsActivity
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Start Chat button click -> Open ChatActivity
        btnStartChat.setOnClickListener(v -> {
            Intent chatIntent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(chatIntent);
        });
    }
}
