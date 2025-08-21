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

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private TextView txtSignup;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);

        btnLogin.setOnClickListener(v -> loginUser());
        txtSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login Failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
