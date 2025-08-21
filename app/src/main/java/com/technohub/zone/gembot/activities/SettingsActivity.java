package com.technohub.zone.gembot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.technohub.zone.gembot.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchTheme;
    private Button btnLogout;
    private TextView txtEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchTheme = findViewById(R.id.switchTheme);
        btnLogout = findViewById(R.id.btnLogout);
        txtEditProfile = findViewById(R.id.txtEditProfile);

        // Handle theme switch
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Handle logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });

        // Handle Edit Profile click
        txtEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
}
