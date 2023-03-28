package com.example.chatbuddy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import org.json.JSONArray;
import org.json.JSONException;
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

    // firebase authentication
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference userRef, uidRef;
    User user;

    // chat
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    String openai_api_key;
    public final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    String userName;
    JSONArray messagesArray;
    JSONObject messageObject;
    final int MAX_MESSAGE_COUNT = 1000; // 최대 메시지 수
    int selfHarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        // chat
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        // api key
        database.getReference("api_key").child("open_ai").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    openai_api_key = String.valueOf(task.getResult().getValue());
                }
            }
        });

        messageList = new ArrayList<>();
        messagesArray = new JSONArray();
        selfHarm = 0;

        // setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // chat
        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();

            if (question.length() == 0) {
                // do nothing
            } else {
                addToChat(question, Message.SENT_BY_ME);
                messageEditText.setText("");
                blockEdit(false);
                try {
                    callChatAPI(question);
                    callModAPI(question);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                welcomeTextView.setVisibility(View.GONE);
            }
        });
    }

    void blockEdit(boolean bool) {
        messageEditText.setEnabled(bool);
        messageEditText.setFocusable(bool);
        messageEditText.setFocusableInTouchMode(bool);
        sendButton.setEnabled(bool);
        sendButton.setClickable(bool);
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                blockEdit(true);
            }
        });
    }

    void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    /*
    void embedding() {
        String jsonString = messagesArray.toString();
        int jsonLength = jsonString.length();
        System.out.println(jsonLength);

        if (jsonLength >= MAX_MESSAGE_COUNT) {
            while (messagesArray.length() > 3) {
                messagesArray.remove(1);
            }
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("input", "gpt-3.5-turbo");
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 2048);
            jsonBody.put("temperature", 1);
            jsonBody.put("frequency_penalty", 1);
            jsonBody.put("presence_penalty", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + getString(R.string.openai_api_key))
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
                messagesArray.remove(messagesArray.length() - 1);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());

                        JSONObject resMessageObject = new JSONObject();
                        resMessageObject.put("role", "assistant");
                        resMessageObject.put("content", result.trim());
                        userRef.child(currentUser.getUid()).child("chat").child("assistant").child(String.valueOf(System.currentTimeMillis())).setValue(result.trim());
                        messagesArray.put(resMessageObject);

                        String finish = jsonArray.getJSONObject(0).getString("finish_reason");
                        System.out.println("finish: " + finish.trim());


                        System.out.println("usage: " + jsonObject.getJSONObject("usage"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                    messagesArray.remove(messagesArray.length() - 1);
                }
            }
        });
    }
     */

    void callChatAPI(String question) throws JSONException {
        if (messagesArray.length() == 0) {
            messageObject = new JSONObject();
            messageObject.put("role", "system");
            messageObject.put("content", userName + " is talking with you. " +
                    userName + " is your best friend. And your name is Buddy. So introduce yourself as Buddy in first time. " +
                    "Use friendly tone and speak informally." +
                    "If user says something negative repeatedly, asks if user wants help.");
            messagesArray.put(messageObject);
        }

        //okhttp
        messageList.add(new Message("입력 중... ", Message.SENT_BY_BOT));

        messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", question);

        userRef.child(currentUser.getUid()).child("chat").child("user").child(String.valueOf(System.currentTimeMillis())).setValue(question);
        messagesArray.put(messageObject);

        String jsonString = messagesArray.toString();
        int jsonLength = jsonString.length();
        System.out.println(jsonLength + ": " + jsonString);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 200);
            jsonBody.put("temperature", 0.8);
            jsonBody.put("frequency_penalty", 1);
            jsonBody.put("presence_penalty", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openai_api_key)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
                messagesArray.remove(messagesArray.length() - 1);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());

                        JSONObject resMessageObject = new JSONObject();
                        resMessageObject.put("role", "assistant");
                        resMessageObject.put("content", result.trim());
                        userRef.child(currentUser.getUid()).child("chat").child("assistant").child(String.valueOf(System.currentTimeMillis())).setValue(result.trim());
                        messagesArray.put(resMessageObject);

                        String finish = jsonArray.getJSONObject(0).getString("finish_reason");
                        System.out.println("finish: " + finish.trim());

                        System.out.println("total usage: " + Integer.parseInt(jsonObject.getJSONObject("usage").getString("total_tokens")));

                        if (Integer.parseInt(jsonObject.getJSONObject("usage").getString("total_tokens")) > 300) {
                            String jsonString = messagesArray.toString();
                            int jsonLength = jsonString.length();

                            while (jsonLength > 500) {
                                messagesArray.remove(1);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                    messagesArray.remove(messagesArray.length() - 1);
                }
            }
        });
    }

    void callModAPI(String question) throws JSONException {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("input", question);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/moderations")
                .header("Authorization", "Bearer " + openai_api_key)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                        //String result = jsonArray.getJSONObject(0).getJSONObject("category_scores").getString("self-harm");
                        //double score = Double.parseDouble(result.trim());
                        //System.out.println("score: " + score);

                        String result = jsonArray.getJSONObject(0).getJSONObject("categories").getString("self-harm");
                        System.out.println("self-harm t/f: " + result.trim());

                        if (result == "true") {
                            selfHarm++;
                        }

                        System.out.println(selfHarm);

                        if (selfHarm > 2) {
                            messageObject = new JSONObject();
                            messageObject.put("role", "system");
                            messageObject.put("content", "Have a deep conversation with her before recommend a counseling center. " +
                                    "However, if she is about to self-harm, recommend a consultation with an expert. " +
                                    "Let her know that the suicide prevention consultation call is '1393'.");
                            messagesArray.put(messageObject);
                            selfHarm = 0;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Failed to load response due to " + response.body().string());
                    messagesArray.remove(messagesArray.length() - 1);
                }
            }
        });
    }
}