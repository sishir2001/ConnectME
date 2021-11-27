package com.example.chatapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.databinding.FragmentRequestsBinding;

public class RequestsFragment extends Fragment {

    private FragmentRequestsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRequestsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}