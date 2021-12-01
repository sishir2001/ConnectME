package com.example.ConnectMe.common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ConnectMe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


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

    public static void updateDeviceToken(Context context,String token){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            String currentUserId = currentUser.getUid();
            // updating the database reference with the node TOKEN -> DEVICE_TOKEN
            DatabaseReference tokenDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference()
                    .child(NodeNames.TOKENS).child(currentUserId);

            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put(NodeNames.DEVICE_TOKEN,token);
            tokenDatabaseReference.setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(context,context.getString(R.string.failed_to_save_token,task.getException()),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
