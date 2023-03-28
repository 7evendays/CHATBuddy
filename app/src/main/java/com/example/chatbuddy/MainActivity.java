package com.example.chatbuddy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {

    private final int frag1 = 1;
    private final int frag2 = 2;
    private ImageButton btnNext;

    // 드로어
    ImageButton btnMenu;
    TextView tvDrawerName;
    LinearLayout mainActivity, btnFirst, btnSetting, btnThird;
    String userName;

    // firebase authentication
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    // realtime database
    FirebaseDatabase database;
    DatabaseReference userRef, uidRef;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        // 드로어
        mainActivity = findViewById(R.id.mainActivity);
        btnMenu = findViewById(R.id.btnMenu);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.drawer, null);

        AtomicInteger state = new AtomicInteger(); // 메뉴 상태 - 0이면 닫힌 상태, 1이면 열린 상태

        final Animation animOpen = AnimationUtils.loadAnimation(this, R.anim.anim_translate_left);
        final Animation animClose = AnimationUtils.loadAnimation(this, R.anim.anim_translate_right);


        // 드로어
        btnMenu.setOnClickListener(view -> {
            if (state.get() == 0) {
                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                addContentView(ll, paramll);
                ll.startAnimation(animOpen);
                //ll.setBackgroundColor(Color.parseColor("#70000000"));
                state.set(1);

                tvDrawerName = findViewById(R.id.drawerName);

                // 이름
                if (currentUser != null) {
                    String uid = currentUser.getUid();
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
                                        tvDrawerName.setText(value.toString() + " 님");
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
                }

                btnFirst = findViewById(R.id.first);
                btnFirst.setOnClickListener(fistview -> {
                    Toast.makeText(getApplicationContext(), "Selected First", Toast.LENGTH_LONG).show();
                });

                btnSetting = findViewById(R.id.second);
                btnSetting.setOnClickListener(fistview -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                });

                btnThird = findViewById(R.id.third);
                btnThird.setOnClickListener(fistview -> {
                    Toast.makeText(getApplicationContext(), "Selected Third", Toast.LENGTH_LONG).show();
                });
            } else {
                ll.startAnimation(animClose);
                ((ViewManager) ll.getParent()).removeView(ll);
                state.set(0);
            }
        });


        // 드로어
        btnMenu.setOnClickListener(view -> {
            if (state.get() == 0) {
                LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                addContentView(ll, paramll);
                ll.startAnimation(animOpen);
                //ll.setBackgroundColor(Color.parseColor("#70000000"));
                state.set(1);

                tvDrawerName = findViewById(R.id.drawerName);

                // 이름
                tvDrawerName.setText(userName + " 님");

                btnFirst = findViewById(R.id.first);
                btnFirst.setOnClickListener(fistview -> {
                    Intent intent = new Intent(getApplicationContext(), MindMapActivity.class);
                    startActivity(intent);
                });

                btnSetting = findViewById(R.id.second);
                btnSetting.setOnClickListener(fistview -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                });

                btnThird = findViewById(R.id.third);
                btnThird.setOnClickListener(fistview -> {
                    Toast.makeText(getApplicationContext(), "Selected Third", Toast.LENGTH_LONG).show();
                });
            } else {
                ll.startAnimation(animClose);
                ((ViewManager) ll.getParent()).removeView(ll);
                state.set(0);
            }
        });

        mainActivity.setOnClickListener(view -> {
            if (state.get() == 1) {
                ll.startAnimation(animClose);
                ((ViewManager) ll.getParent()).removeView(ll);
                state.set(0);
            }
        });

        btnNext = (ImageButton) findViewById(R.id.btnNext);
        final int[] fState = {1};

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fState[0] ==1){
                    callFragment(2);
                    fState[0] = 2;
                    btnNext.setBackgroundResource(R.drawable.btn_solution);
                } else if (fState[0] ==2) {
                    callFragment(1);
                    fState[0] = 1;
                    btnNext.setBackgroundResource(R.drawable.btn_chat);
                }
            }
        });

        callFragment(frag1);
    }

    private void callFragment(int frament_no){

        // 프래그먼트 사용을 위해
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (frament_no){
            case 1:
                // '프래그먼트1' 호출
                ChatFragment fragment1 = new ChatFragment();
                transaction.replace(R.id.fragment_container, fragment1);
                transaction.commit();
                break;

            case 2:
                // '프래그먼트2' 호출
                SolutionFragment fragment2 = new SolutionFragment();
                transaction.replace(R.id.fragment_container, fragment2);
                transaction.commit();
                break;
        }

    }

}