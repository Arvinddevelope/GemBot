package com.technohub.zone.gembot.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.technohub.zone.gembot.R;
import com.technohub.zone.gembot.adapters.MessageAdapter;
import com.technohub.zone.gembot.models.MessageModel; // Ensure MessageModel is a POJO
import com.technohub.zone.gembot.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private ImageButton btnSend;
    private ImageButton btnCamera;
    private ImageView backButton;
    private MessageAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private TextView userNameTitle;

    private final OkHttpClient client = new OkHttpClient();
    // private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Not strictly needed if Firebase handles UI updates

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Bitmap capturedImageBitmap = null;

    private DatabaseReference chatDatabaseReference;
    private ChildEventListener chatEventListener;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewChat);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnCamera = findViewById(R.id.btnCamera);
        userNameTitle = findViewById(R.id.userNameTitle);
        backButton = findViewById(R.id.backButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getDisplayName() != null) {
            userNameTitle.setText(currentUser.getDisplayName());
        } else {
            userNameTitle.setText("GemBot"); // Or handle if user is null more explicitly
        }

        adapter = new MessageAdapter(messageList, this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (currentUser != null) {
            // Using a simple chat node for demonstration. You might want a more complex structure
            // e.g., "chats/{userId_otherUserId}" or "user_chats/{userId}/bot_chats"
            // For simplicity, using one global "chat_messages" node.
            // Consider creating a unique chat ID if this is a 1-on-1 chat.
            // For now, let's assume a common chat room or a chat specific to the current user.
            // If it's a chat with "GemBot", you could use:
            // chatDatabaseReference = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUser.getUid()).child("messages");
            // Or a simpler global one for now:
            chatDatabaseReference = FirebaseDatabase.getInstance().getReference("chat_messages");
            loadChatHistory();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Or redirect to login
            return;
        }


        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty() || capturedImageBitmap != null) {
                sendMessage(text, capturedImageBitmap);
                editMessage.setText("");
                capturedImageBitmap = null;
            }
        });

        btnCamera.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadChatHistory() {
        messageList.clear(); // Clear local list before loading from Firebase
        if (chatEventListener != null) {
            chatDatabaseReference.removeEventListener(chatEventListener);
        }
        chatEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);
                if (message != null) {
                    messageList.add(message);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load chat history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        chatDatabaseReference.addChildEventListener(chatEventListener);
    }


    private void sendMessage(String text, @Nullable Bitmap image) {
        String displayMessageText = text;
        if (image != null && text.isEmpty()) {
            displayMessageText = "[Image Sent]"; // Placeholder for image if no text
        } else if (image != null && !text.isEmpty()){
            displayMessageText = text + " [Image Sent]";
        }

        if (currentUser != null && (!displayMessageText.isEmpty() || image != null)) {
            // For now, we are saving the display text.
            // If you upload the image to Firebase Storage, you'd save the image URL instead/additionally.
            MessageModel userMessage = new MessageModel(currentUser.getUid(), displayMessageText, System.currentTimeMillis());
            // If MessageModel needs sender name directly:
            // MessageModel userMessage = new MessageModel(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "user", displayMessageText, System.currentTimeMillis());
            // Adjust MessageModel constructor if it expects sender type like "user" or "bot"
            // For consistency with your original MessageModel("user", text, timestamp):
            // We need to decide if sender is UID or "user"/"bot" type.
            // Assuming MessageModel constructor is (String senderIdOrType, String message, long timestamp)
            MessageModel firebaseMessage = new MessageModel("user", displayMessageText, System.currentTimeMillis());


            chatDatabaseReference.push().setValue(firebaseMessage)
                    .addOnSuccessListener(aVoid -> {
                        // Message saved to Firebase, call Gemini API
                        callGeminiAPI(text, image); // Pass original text for API
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        // User message will be added to UI via Firebase listener
    }

    private void callGeminiAPI(String userMessage, @Nullable Bitmap image) {
        // ... (your existing callGeminiAPI code remains largely the same)
        // Ensure you get the 'reply' and then save it using a method similar to showBotMessage
        try {
            String url = Constants.BASE_URL + Constants.MODEL + "?key=" + Constants.API_KEY;
            JSONObject jsonPayload = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();

            if (userMessage != null && !userMessage.isEmpty()) {
                JSONObject textPart = new JSONObject();
                textPart.put("text", userMessage);
                partsArray.put(textPart);
            }

            if (image != null) {
                String base64ImageData = bitmapToBase64(image);
                if (base64ImageData != null) {
                    JSONObject imagePart = new JSONObject();
                    JSONObject inlineData = new JSONObject();
                    inlineData.put("mime_type", "image/jpeg");
                    inlineData.put("data", base64ImageData);
                    imagePart.put("inline_data", inlineData);
                    partsArray.put(imagePart);
                }
            }

            if (partsArray.length() == 0) {
                // If you call this method, it means a user message (text or image) was intended.
                // This state should ideally not be reached if sendMessage checks partsArray.length()
                // For safety, you might show a local toast or log, but don't send to Firebase.
                Toast.makeText(this, "Cannot send empty message to API.", Toast.LENGTH_SHORT).show();
                return;
            }

            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            jsonPayload.put("contents", contentsArray);

            RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // Show error locally, don't save to Firebase as it's a network/API issue for bot reply
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "⚠️ Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "❌ API Error: " + response.code() + " " + response.message(), Toast.LENGTH_LONG).show());
                        return;
                    }
                    try {
                        String resBody = response.body().string();
                        JSONObject jsonRes = new JSONObject(resBody);
                        if (jsonRes.has("error")) {
                            String errorMessage = jsonRes.getJSONObject("error").getString("message");
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "API Error: " + errorMessage, Toast.LENGTH_LONG).show());
                            return;
                        }
                        String reply = jsonRes.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");

                        // Save bot's reply to Firebase
                        MessageModel botMessage = new MessageModel("bot", reply, System.currentTimeMillis());
                        chatDatabaseReference.push().setValue(botMessage);
                        // Bot message will be added to UI via Firebase listener

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "⚠️ Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "⚠️ Error preparing API call: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    // showBotMessage is now effectively handled by saving to Firebase and letting the listener update UI
    // private void showBotMessage(String message) { ... } // This method is no longer needed to directly update UI

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatEventListener != null && chatDatabaseReference != null) {
            chatDatabaseReference.removeEventListener(chatEventListener);
        }
    }

    // The rest of your methods like checkCameraPermissionAndOpenCamera, openCamera,
    // onRequestPermissionsResult, onActivityResult, bitmapToBase64 remain the same.
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                capturedImageBitmap = (Bitmap) extras.get("data");
                Toast.makeText(this, "Image captured! Add a message or press send.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
