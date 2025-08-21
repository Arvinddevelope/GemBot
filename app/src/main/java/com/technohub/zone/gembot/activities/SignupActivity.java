package com.technohub.zone.gembot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.technohub.zone.gembot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword;
    private Button btnSignup;
    private TextView txtLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignup = findViewById(R.id.btnSignup);
        txtLogin = findViewById(R.id.txtLogin);

        btnSignup.setOnClickListener(v -> registerUser());
        txtLogin.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates);

                        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Signup Failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
