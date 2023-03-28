package com.example.chatbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class SolutionFragment extends Fragment {

    LinearLayout todo1;

    public SolutionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solution,container,false);
        // Inflate the layout for this fragment

        todo1 = view.findViewById(R.id.todo1);
        todo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), TodoActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}