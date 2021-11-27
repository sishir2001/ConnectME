package com.example.chatapplication.common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;


public class Util {

    // for checking internet connection
    public static boolean connectionAvailable(Context context){
        // ConnectivityManager to check for the internet connect
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null){
            // connectivity is there
            Toast.makeText(context, ""+connectivityManager.getActiveNetworkInfo(), Toast.LENGTH_LONG).show();
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else{
            // no interet connection
            return false;
        }

    }
}
