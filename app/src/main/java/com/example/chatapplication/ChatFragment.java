package com.example.chatapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {
//    public ChatFragment() {
//        // Required empty public constructor
//    }
    private FragmentChatBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}