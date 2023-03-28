package com.example.chatbuddy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    LinearLayout btnSignOut, btnLeave, btnPasswd;

    TextView tvPasswd, tvUID, tvDisplayName, tvEmail, tvNickName, tvBirth, tvGender;
    ImageView ivPhoto;

    // firebase authentication
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference userRef, uidRef;
    User user;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        userRef = database.getReference("users");
        uidRef = userRef.child(currentUser.getUid());

        // 버튼
        btnBack = findViewById(R.id.btnBack);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnLeave = findViewById(R.id.btnLeave);
        btnPasswd = findViewById(R.id.btnPasswd);
        tvPasswd = findViewById(R.id.tvPasswd);
        setTvPasswd();

        // 프로필
        tvEmail = findViewById(R.id.userEmail);
        /*
        tvUID = findViewById(R.id.userId);
        tvDisplayName = findViewById(R.id.displayName);

        ivPhoto = findViewById(R.id.userPhoto);
        tvNickName = findViewById(R.id.nickName);
        tvBirth = findViewById(R.id.birth);
        tvGender = findViewById(R.id.gender);
         */

        // 알림창
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

        // 비밀번호
        btnPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uidRef.child("password").child("on").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean on = snapshot.getValue(Boolean.class);
                        if (on.equals(true)) {
                            uidRef.child("password").child("on").setValue(false);
                            setTvPasswd();
                        } else if (on.equals(false)) {
                            uidRef.child("password").child("val").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String password = snapshot.getValue(String.class);
                                    if (snapshot.exists()) {
                                        uidRef.child("password").child("on").setValue(true);
                                        setTvPasswd();
                                    } else {
                                        startPasswordActivity();
                                        setTvPasswd();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // 데이터베이스 액세스 중 오류 발생 시 처리 방법
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 데이터베이스 액세스 중 오류 발생 시 처리 방법
                    }
                });
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        // Firebase Authentication에서 로그아웃
        mAuth.signOut();
        startSignInActivity();
    }

    private void leave() {
        String uid = mAuth.getCurrentUser().getUid();

        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // 사용자 정보 파기
                            uidRef = userRef.child(uid);
                            uidRef.setValue(null);

                            Log.d("TAG", "User account deleted.");
                            Toast.makeText(getApplicationContext(), "탈퇴가 완료되었습니다.😥", Toast.LENGTH_LONG).show();
                            startSignInActivity();
                        }
                    }
                });
    }

    // 사용자 프로필 가져오기
    private void getUserData() {
        String uid, displayName, email;
        String nickName, birth, gender;
        Uri photo;

        if (currentUser != null) {
            email = currentUser.getEmail();
            tvEmail.setText(email);

            /*
            uid = currentUser.getUid();
            displayName = currentUser.getDisplayName();
            photo = currentUser.getPhotoUrl();
            tvUID.setText(uid);
            tvDisplayName.setText(displayName);
            Glide.with(this).load(photo).into(ivPhoto);

            uidRef = userRef.child(uid);
            uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // uid 노드의 모든 데이터를 가져옴
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey(); // 하위 노드의 키값을 가져옴
                        Object value = snapshot.getValue(); // 하위 노드의 값을 가져옴
                        // 이곳에서 하위 노드의 키와 값을 사용하여 원하는 작업을 수행할 수 있음
                        switch (key) {
                            case "nickName":
                                tvNickName.setText(value.toString());
                                break;
                            case "birth":
                                tvBirth.setText(value.toString());
                                break;
                            case "gender":
                                tvGender.setText(value.toString());
                                break;
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // 데이터 로드에 실패한 경우 호출됨
                    Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                }
            });
            */
        }
    }

    private void setTvPasswd() {
        uidRef.child("password").child("on").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean on = snapshot.getValue(Boolean.class);
                    if (on.equals(true)) {
                        tvPasswd.setText("비밀번호 : 끄기");
                    } else if (on.equals(false)) {
                        tvPasswd.setText("비밀번호 : 켜기");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 데이터베이스 액세스 중 오류 발생 시 처리 방법
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setTvPasswd();
    }

    private void startSignInActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
    }

    private void startPasswordActivity() {
        Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
        intent.putExtra("preAct", "SettingsActivity");
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }
}