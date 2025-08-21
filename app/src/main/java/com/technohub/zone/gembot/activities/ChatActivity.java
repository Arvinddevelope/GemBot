package com.technohub.zone.gembot.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.technohub.zone.gembot.R;
import com.technohub.zone.gembot.adapters.MessageAdapter;
import com.technohub.zone.gembot.models.MessageModel;
import com.technohub.zone.gembot.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private MessageAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private TextView userNameTitle;

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewChat);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        userNameTitle = findViewById(R.id.userNameTitle);

        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null) {
            userNameTitle.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        } else {
            userNameTitle.setText("GemBot");
        }

        adapter = new MessageAdapter(messageList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                editMessage.setText("");
            }
        });
    }

    private void sendMessage(String text) {
        // Add user message
        messageList.add(new MessageModel("user", text, System.currentTimeMillis()));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // Call Gemini API
        callGeminiAPI(text);
    }

    private void callGeminiAPI(String userMessage) {
        try {
            String url = Constants.BASE_URL + Constants.MODEL + "?key=" + Constants.API_KEY;

            JSONObject json = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", userMessage);

            JSONObject content = new JSONObject();
            content.put("role", "user");
            content.put("parts", new JSONArray().put(part));
            contents.put(content);

            json.put("contents", contents);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showBotMessage("⚠️ Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        showBotMessage("❌ API Error: " + response.code());
                        return;
                    }

                    try {
                        String res = response.body().string();
                        JSONObject jsonRes = new JSONObject(res);
                        String reply = jsonRes
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        showBotMessage(reply);

                    } catch (Exception e) {
                        showBotMessage("⚠️ Parsing error: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showBotMessage("⚠️ Error: " + e.getMessage());
        }
    }

    private void showBotMessage(String message) {
        mainHandler.post(() -> {
            messageList.add(new MessageModel("bot", message, System.currentTimeMillis()));
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        });
    }
}
