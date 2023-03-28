package com.example.chatbuddy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener{

    public static BottomSheetDialog getInstance() { return new BottomSheetDialog(); }

    private Button btnEmail;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_manual, container,false);
        btnEmail = (Button) view.findViewById(R.id.btnEmail);

        btnEmail.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getContext(),"이메일 전송하기", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
