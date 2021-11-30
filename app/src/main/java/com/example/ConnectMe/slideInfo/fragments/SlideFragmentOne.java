package com.example.ConnectMe.slideInfo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.ConnectMe.R;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

/**
 * A simple {@link Fragment} subclass.
 * Use th
 * create an instance of this fragment.
 */
public class SlideFragmentOne extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public SlideFragmentOne() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_slide, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        DotIndicator dot=getActivity().findViewById(R.id.dots);
        dot.setSelectedItem(0,true);
    }
}