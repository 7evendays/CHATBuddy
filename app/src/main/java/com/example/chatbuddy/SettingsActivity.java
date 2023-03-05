package com.example.chatbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

public class SettingsActivity extends AppCompatActivity {

    ImageButton btnBack;
    LinearLayout btnSignOut;
    LinearLayout btnLeave;

    TextView tvUID, tvDisplayName, tvEmail;
    ImageView ivPhoto;

    // firebase authentication
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference myRef;
    User user;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 버튼
        btnBack = findViewById(R.id.btnBack);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnLeave = findViewById(R.id.btnLeave);

        // 프로필
        tvUID = findViewById(R.id.userId);
        tvDisplayName = findViewById(R.id.displayName);
        tvEmail = findViewById(R.id.userEmail);
        ivPhoto = findViewById(R.id.userPhoto);

        // Firebase 앱 초기화
        FirebaseApp.initializeApp(getApplicationContext());
        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Google 로그인 클라이언트 초기화
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);

        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 사용자 프로필 가져오기
        getUserData();

        // 로그아웃

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        // 탈퇴
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 탈퇴하기 전에 확인
                builder.setMessage("관련된 모든 정보가 삭제됩니다. 정말로 탈퇴하시겠습니까? 🥺")
                        .setTitle("회원탈퇴")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // 확인 버튼 클릭 시 동작할 코드
                                leave();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // 취소 버튼 클릭 시 동작할 코드
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(), "저희와 계속 함께 해주셔서 감사합니다!🥰", Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        // Firebase Authentication에서 로그아웃
        mAuth.signOut();
        startFirstActivity();
    }

    private void leave() {
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "User account deleted.");
                            Toast.makeText(getApplicationContext(), "탈퇴가 완료되었습니다.😥", Toast.LENGTH_LONG).show();
                            startFirstActivity();
                        }
                    }
                });
    }

    // 사용자 프로필 가져오기
    private void getUserData() {
        String uid, displayName, email;
        // String nickName, birth, gender;
        Uri photo;

        if (currentUser != null) {
            uid = currentUser.getUid();
            displayName = currentUser.getDisplayName();
            email = currentUser.getEmail();
            photo = currentUser.getPhotoUrl();

            tvUID.setText(uid);
            tvDisplayName.setText(displayName);
            tvEmail.setText(email);
            Glide.with(this).load(photo).into(ivPhoto);

            /*
            // Read from the database
            myRef.child(uid).child("nickName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    Log.d("TAG", "Value is: " + value);
                    tvNickName.setText(value);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });

            myRef.child(uid).child("birth").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    Log.d("TAG", "Value is: " + value);
                    tvBirth.setText(value);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });

            myRef.child(uid).child("gender").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    Log.d("TAG", "Value is: " + value);
                    tvGender.setText(value);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException());
                }
            });
             */
        }
    }

    private void startFirstActivity() {
        Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
        startActivity(intent);
        finish();
    }
}