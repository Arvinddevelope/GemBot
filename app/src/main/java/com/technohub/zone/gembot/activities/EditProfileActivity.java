package com.technohub.zone.gembot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technohub.zone.gembot.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgProfile;
    private TextView txtChangePhoto;
    private TextInputEditText edtFullName, edtEmail, edtPhone;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Bind views
        imgProfile = findViewById(R.id.imgProfile);
        txtChangePhoto = findViewById(R.id.txtChangePhoto);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Saving...");

        // Load current user data
        loadUserProfile();

        // Change photo click
        txtChangePhoto.setOnClickListener(v -> openImagePicker());

        // Save button click
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        // Load email
        edtEmail.setText(currentUser.getEmail());

        // Load name and photo from Firestore
        DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtFullName.setText(documentSnapshot.getString("name"));
                edtPhone.setText(documentSnapshot.getString("phone"));

                String photoUrl = documentSnapshot.getString("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).into(imgProfile);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imgProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfile() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (name.isEmpty()) {
            edtFullName.setError("Enter full name");
            return;
        }

        progressDialog.show();

        if (selectedImageUri != null) {
            uploadProfilePhoto(name, phone);
        } else {
            updateProfileData(name, phone, null);
        }
    }

    private void uploadProfilePhoto(String name, String phone) {
        String fileName = "profile_images/" + UUID.randomUUID().toString();
        StorageReference ref = storageReference.child(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                updateProfileData(name, phone, uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileData(String name, String phone, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        firestore.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
