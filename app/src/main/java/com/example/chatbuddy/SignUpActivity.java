package com.example.chatbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

public class SignUpActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnSignUp;
    private TextView txtGuide, txtBirth, txtGender;
    private EditText edtText, edtNick, edtBirth, edtGender;

    GoogleSignInOptions gso;

    // firebase authentication
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference myRef;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnBack = findViewById(R.id.btnBack);
        txtGuide = findViewById(R.id.txtGuide);
        edtNick = findViewById(R.id.edtNick);
        txtBirth = findViewById(R.id.txtBirth);
        edtBirth = findViewById(R.id.edtBirth);
        txtGender = findViewById(R.id.txtGender);
        edtGender = findViewById(R.id.edtGender);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Firebase 앱 초기화
        FirebaseApp.initializeApp(SignUpActivity.this);
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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();
                signIn();
            }
        });
    }

    private void saveUserData() {
        String nickName = edtNick.getText().toString(),
                birth = edtBirth.getText().toString(),
                gender = edtGender.getText().toString();

        myRef.child(currentUser.getUid()).child("email").setValue(currentUser.getEmail());
        myRef.child(currentUser.getUid()).child("name").setValue(currentUser.getDisplayName());
        myRef.child(currentUser.getUid()).child("photo").setValue(currentUser.getPhotoUrl().toString());
        myRef.child(currentUser.getUid()).child("nickName").setValue(nickName);
        myRef.child(currentUser.getUid()).child("birth").setValue(birth);
        myRef.child(currentUser.getUid()).child("gender").setValue(gender);
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // 로그인 결과 처리
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
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

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
                                startMainActivity();
                                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 로그인 실패 시
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                            startFirstActivity();
                        }
                    }
                });
    }

    private void startFirstActivity() {
        Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        edtNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 2) {
                    txtBirth.setVisibility(View.VISIBLE);
                    edtBirth.setVisibility(View.VISIBLE);
                    txtGuide.setText("생년월일을 입력해주세요");
                } else {
                    txtBirth.setVisibility(View.INVISIBLE);
                    edtBirth.setVisibility(View.INVISIBLE);
                    txtGuide.setText("닉네임을 입력해주세요");
                }
            }
        });


        edtBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    txtGender.setVisibility(View.VISIBLE);
                    edtGender.setVisibility(View.VISIBLE);
                    txtGuide.setText("성별을 입력해주세요");
                } else {
                    txtGender.setVisibility(View.INVISIBLE);
                    edtGender.setVisibility(View.INVISIBLE);
                    txtGuide.setText("생년월일을 입력해주세요");
                }
            }
        });

        edtGender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    btnSignUp.setVisibility(View.VISIBLE);
//                    btnSignin.setBackgroundColor(Color.parseColor("#01ABB4"));
//                    btnSignin.setEnabled(true);
                } else {
                    btnSignUp.setVisibility(View.INVISIBLE);
//                    btnSignin.setBackgroundColor(Color.parseColor("#292C33"));
//                    btnSignin.setEnabled(false);
                }
            }
        });
    }
}