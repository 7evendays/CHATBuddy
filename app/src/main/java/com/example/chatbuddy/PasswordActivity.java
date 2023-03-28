package com.example.chatbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

public class PasswordActivity extends AppCompatActivity {

    ImageButton btnBack;
    TextView pswdTitle;
    EditText pswd1, pswd2, pswd3, pswd4;
    String pswd, doubleChk;

    // firebase authentication
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference userRef, uidRef;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        btnBack = findViewById(R.id.btnBack);
        pswdTitle = findViewById(R.id.pswdTitle);
        pswd1 = findViewById(R.id.pswd1);
        pswd2 = findViewById(R.id.pswd2);
        pswd3 = findViewById(R.id.pswd3);
        pswd4 = findViewById(R.id.pswd4);

        // Firebase 앱 초기화
        FirebaseApp.initializeApp(getApplicationContext());
        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");
        uidRef = userRef.child(currentUser.getUid());

        Intent intent = getIntent();
        String pre = intent.getExtras().getString("preAct");

        if (pre.equals("SplashActivity")) {
            pswdTitle.setText("비밀번호 입력");
        }


        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        pswd1.requestFocus();
        pswd1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    pswd1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.text_lightGray)));
                    pswd1.setEnabled(false);

                    pswd2.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(pswd2, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    pswd1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_lightGray)));
                }
            }
        });


        pswd2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Backspace 키를 눌렀을 때
                    if (pswd2.getText().length() == 0) {
                        pswd1.setEnabled(true);
                        pswd1.requestFocus();
                        pswd1.setText("");
                    } else {
                        pswd2.setText("");
                    }
                    return true;
                }

                return false;
            }
        });
        pswd2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력된 글자 수가 1개인 경우에만 포커스 이동
                if (s.length() == 1) {
                    pswd2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.text_lightGray)));
                    pswd2.setEnabled(false);

                    pswd3.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(pswd3, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    pswd2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_lightGray)));
                    pswd1.requestFocus();
                }
            }
        });


        pswd3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Backspace 키를 눌렀을 때
                    if (pswd3.getText().length() == 0) {
                        pswd2.setEnabled(true);
                        pswd2.requestFocus();
                        pswd2.setText("");
                    } else {
                        pswd3.setText("");
                    }
                    return true;
                }

                return false;
            }
        });
        pswd3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력된 글자 수가 1개인 경우에만 포커스 이동
                if (s.length() == 1) {
                    pswd3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.text_lightGray)));
                    pswd3.setEnabled(false);
                    pswd4.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(pswd4, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    pswd3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_lightGray)));
                    pswd2.requestFocus();
                }
            }
        });


        pswd4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Backspace 키를 눌렀을 때
                    if (pswd4.getText().length() == 0) {
                        pswd3.setEnabled(true);
                        pswd3.requestFocus();
                        pswd3.setText("");
                    } else {
                        pswd4.setText("");
                    }
                    return true;
                }

                return false;
            }
        });
        pswd4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    pswd4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.text_lightGray)));
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(pswd4.getWindowToken(), 0);

                    if (pre.equals("SplashActivity")) {
                        pswd = pswd1.getText().toString() + pswd2.getText().toString()
                                + pswd3.getText().toString() + pswd4.getText().toString();

                        uidRef.child("password").child("val").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String password = snapshot.getValue(String.class);
                                if (password.equals(pswd)) {
                                    startMainActivity();
                                } else {
                                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                                    pswd1.setText("");
                                    pswd2.setText("");
                                    pswd3.setText("");
                                    pswd4.setText("");

                                    pswd1.setEnabled(true);
                                    pswd2.setEnabled(true);
                                    pswd3.setEnabled(true);
                                    pswd4.setEnabled(true);

                                    pswd1.requestFocus();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(pswd1, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // 데이터베이스 액세스 중 오류 발생 시 처리 방법
                            }
                        });

                    } else {
                        startConfirm();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    pswd4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_lightGray)));
                }
            }
        });
    }

    private void startMainActivity() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void startConfirm() {
        if (pswd1.getText().length() != 0 && pswd2.getText().length() != 0 &&
                pswd3.getText().length() != 0 && pswd4.getText().length() != 0) {

            if (pswd == null) {
                pswd = pswd1.getText().toString() + pswd2.getText().toString()
                        + pswd3.getText().toString() + pswd4.getText().toString();

                pswdTitle.setText("비밀번호를 확인하세요.");
                pswd1.setText("");
                pswd2.setText("");
                pswd3.setText("");
                pswd4.setText("");

                pswd1.setEnabled(true);
                pswd2.setEnabled(true);
                pswd3.setEnabled(true);
                pswd4.setEnabled(true);

                pswd1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(pswd1, InputMethodManager.SHOW_IMPLICIT);
            } else {
                doubleChk = pswd1.getText().toString() + pswd2.getText().toString()
                        + pswd3.getText().toString() + pswd4.getText().toString();

                if (pswd.equals(doubleChk)) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 생성되었습니다.", Toast.LENGTH_LONG).show();
                    userRef.child(currentUser.getUid()).child("password").child("val").setValue(pswd);
                    userRef.child(currentUser.getUid()).child("password").child("on").setValue(true);
                } else {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    userRef.child(currentUser.getUid()).child("password").child("on").setValue(false);
                }

                finish();
            }
        }
    }
}