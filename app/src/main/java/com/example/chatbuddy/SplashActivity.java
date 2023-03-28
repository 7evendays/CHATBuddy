package com.example.chatbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

public class SplashActivity extends AppCompatActivity {

    // firebase authentication
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference userRef, uidRef;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Firebase 앱 초기화
        FirebaseApp.initializeApp(getApplicationContext());
        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        // Google 로그인 클라이언트 초기화
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        currentUser = mAuth.getCurrentUser();

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");
        if (currentUser != null) {
            uidRef = userRef.child(currentUser.getUid());
        }

        autoSignIn();
    }

    // 앱 시작 시 자동 로그인
    private void autoSignIn() {
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // 로그아웃한 사람, 탈퇴한 사람
            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            startSignInActivity();
        } else {
            // 유효한 계정입니다.
            // Firebase에서 해당 계정이 존재하는지 확인합니다.
            String email = currentUser.getEmail();
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                        if (isNewUser) {
                            // authentication에 존재하지 않는 계정입니다.
                            Toast.makeText(getApplicationContext(), "회원가입이 필요합니다.", Toast.LENGTH_SHORT).show();
                            startSignUpActivity();
                        } else {
                            // authenticaiton에 존재하는 계정입니다.
                            // realtime database로 더블체크
                            String uid = currentUser.getUid();
                            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // uid가 존재하는 경우
                                        signIn();
                                    } else {
                                        // uid가 존재하지 않는 경우
                                        Toast.makeText(getApplicationContext(), "회원가입이 필요합니다.", Toast.LENGTH_SHORT).show();
                                        startSignUpActivity();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // 쿼리가 취소된 경우
                                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // onActivityResult() 메서드에서 로그인 결과 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글 로그인 시
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 로그인 성공 시
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // 로그인 실패 시
                Log.w("TAG", "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase 인증을 위한 GoogleSignInAccount를 사용하여 로그인하는 메서드
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String uid = acct.getId();
        Log.d("TAG", "firebaseAuthWithGoogle:" + uid);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공 시
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                                uidRef.child("password").child("on").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Boolean on = snapshot.getValue(Boolean.class);
                                            if (on.equals(true)) {
                                                startPasswordActivity();
                                            } else if (on.equals(false)) {
                                                startMainActivity();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // 데이터베이스 액세스 중 오류 발생 시 처리 방법

                                    }
                                });
                            }

                        } else {
                            // 로그인 실패 시
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.anim_fade_out);
    }

    private void startSignInActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
    }

    private void startMainActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void startSignUpActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    private void startPasswordActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
        intent.putExtra("preAct", "SplashActivity");
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }
}