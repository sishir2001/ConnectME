package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.example.chatapplication.common.Util;
import com.example.chatapplication.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private ConnectivityManager.NetworkCallback networkCallback;
    // to use networkCallback , build version > 21 (Lollipop)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // When Passed to this activity , the first thing is to reconnect to the internet
        // Use of network callback to perform what to when the internet comes back

        // checking for build version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    // navigate to the previos activity
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    binding.tvMessage.setText(getString(R.string.no_internet));
                }
            };
            // binding network callback to connectivityManager
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),networkCallback);

        }

        // setting click listeners to button
        binding.buttonRetry.setOnClickListener(view -> {
            binding.messageProgressBar.setVisibility(View.VISIBLE);
            // once again check for internet connection
            if(Util.connectionAvailable(this)){
                // true , navigate back to the previous screen
                finish();
            }
            else{
                // false, delay the visibility of progress bar
                new android.os.Handler().postDelayed(() -> {
                    // after 1 sec the below code will run
                    binding.messageProgressBar.setVisibility(View.GONE);
                },1000);
            }
        });
        binding.btnClose.setOnClickListener(view -> {
            finishAffinity();
//            System.exit(0);
        });
    }
}