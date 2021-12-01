package com.example.ConnectMe;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.example.ConnectMe.common.Util;
import com.google.firebase.messaging.FirebaseMessagingService;

public class ChatMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        // Serivice can also be considered as context
        // here s is the new token
        Util.updateDeviceToken(this,s);
    }
}