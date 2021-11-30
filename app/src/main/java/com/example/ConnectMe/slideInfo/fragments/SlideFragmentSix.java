package com.example.ConnectMe.slideInfo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.ConnectMe.R;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SlideFragmentSix extends Fragment {

    public SlideFragmentSix() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slide, container, false);
    }
    @Override
    public void onViewCreated(View view,Bundle onSavedInstances){
        DotIndicator dot=getActivity().findViewById(R.id.dots);
        dot.setSelectedItem(5,true);
        TextView textView =  view.findViewById(R.id.textViewInfo);
        // changing the text
        textView.setText(getResources().getString(R.string.page_6));
        ImageView imageView =  view.findViewById(R.id.imageViewInfo);
        imageView.setImageResource(R.drawable.ic_pg6_img);
    }
    @Override
    public void onResume() {
        super.onResume();
        DotIndicator dot=getActivity().findViewById(R.id.dots);
        dot.setSelectedItem(5,true);
    }

}