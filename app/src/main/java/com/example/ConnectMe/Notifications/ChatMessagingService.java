package com.example.ConnectMe.Notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ConnectMe.R;
import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.Util;
import com.example.ConnectMe.slideInfo.slideinfoActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        // Serivice can also be considered as context
        // here s is the new token
        Util.updateDeviceToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // this function is called when a data notification is received

        // TODO : receive a data from the notification of FCM
        // TODO :form a general notification and notify
        // TODO : remoteMessage is used to fetch the details of the notifications

        String notificationTitle = remoteMessage.getData().get(Constants.NOTIFICATION_TITLE);
        String notificationMessage = remoteMessage.getData().get(Constants.NOTIFICATION_MESSAGE);

        // when clicked on the notification redirect the user to login activity
        Intent chatIntent = new Intent(this, slideinfoActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, chatIntent, PendingIntent.FLAG_IMMUTABLE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
//            pendingIntent = PendingIntent.getActivity(this,0,chatIntent,PendingIntent.FLAG_IMMUTABLE);
//        }

        // to show a notification we need an object of a notificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // whenever we recieve notification , we need to play some sound -> the default sound of the android phone
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // to build notification , we need notification builder
        NotificationCompat.Builder notificationBuilder ;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // need another type of initialization
            // build a channel
            NotificationChannel notificationChannel = new NotificationChannel(Constants.CHANNEL_ID,Constants.CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(Constants.CHANNEL_DESC);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationBuilder = new NotificationCompat.Builder(this,Constants.CHANNEL_ID);
        }
        else{
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        notificationBuilder.setSmallIcon(R.drawable.ic_chat);
        notificationBuilder.setColor(getColor(R.color.primaryColor));
        notificationBuilder.setContentTitle(notificationTitle);
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setContentIntent(pendingIntent);
        // check for the message type
        if(notificationMessage.startsWith("https://firebasestorage")){
            try{

                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                Glide.with(this)
                        .asBitmap()
                        .load(notificationMessage)
                        .into(new CustomTarget<Bitmap>(200,100) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                bigPictureStyle.bigPicture(resource);
                                notificationBuilder.setStyle(bigPictureStyle);
                                // this finally builds the notification
                                notificationManager.notify(999,notificationBuilder.build());

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            }
            catch (Exception ex){
                notificationBuilder.setContentText("New File recieved");
            }

        }
        else{
            // for text message
            notificationBuilder.setContentText(notificationMessage);
            // this finally builds the notification
            notificationManager.notify(999,notificationBuilder.build());
        }


        super.onMessageReceived(remoteMessage);
    }
}