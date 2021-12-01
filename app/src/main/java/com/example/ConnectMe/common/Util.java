package com.example.ConnectMe.common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ConnectMe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


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
    public static void sendNotification(Context context,String title,String messsage,String userId){
        // userId - > other user uuid
        // For sending messages we need device token of userId

        DatabaseReference rootDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference();
        DatabaseReference tokenDatabaseReference = rootDatabaseReference.child(NodeNames.TOKENS).child(userId);

        tokenDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(NodeNames.DEVICE_TOKEN).getValue() != null){
                            String deviceToken = snapshot.child(NodeNames.DEVICE_TOKEN).getValue().toString();

                            // creating a json object for notification
                            JSONObject notification = new JSONObject();
                            JSONObject notificationData = new JSONObject();

                            // filling the json object
                            try {
                                notificationData.put(Constants.NOTIFICATION_TITLE,title);
                                notificationData.put(Constants.NOTIFICATION_MESSAGE,messsage);

                                notification.put(Constants.NOTIFICATION_TO,deviceToken);
                                notification.put(Constants.NOTIFICATION_DATA,notificationData);

                                // two famous libraries for using api's - Volley and Retrofit
                                // Web api call to firebase
                                // We need to provide authentication
                                String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
                                String contentType = "application/json";

                                Response.Listener successListener = new Response.Listener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        Toast.makeText(context,"Notification Successfully sent",Toast.LENGTH_SHORT).show();
                                    }
                                };

                                Response.ErrorListener failureListener =  new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(context,context.getString(R.string.failed_send_notification,error.getMessage()),Toast.LENGTH_SHORT).show();
                                    }
                                };

                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(fcmApiUrl,notification,successListener,failureListener){

                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String,String> params = new HashMap<>();
                                        params.put("Authorization","key="+Constants.FIREBASE_KEY);
                                        params.put("Sender","id="+Constants.SENDER_ID);
                                        params.put("Content-Type",contentType);
                                        return params ;
                                    }
                                };

                                RequestQueue requestQueue = Volley.newRequestQueue(context);
                                requestQueue.add(jsonObjectRequest);

                            }
                            catch(JSONException e){
                                Toast.makeText(context,context.getString(R.string.failed_send_notification,e.getMessage()),Toast.LENGTH_SHORT).show();
                            }

                        }
                        else{
                            // does not exist
                            Toast.makeText(context,context.getString(R.string.failed_send_notification,""),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // error
                        Toast.makeText(context,context.getString(R.string.failed_send_notification,error.getMessage()),Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public static void updateChatDetails(Context context,String currentUserId,String chatUserId){

        // for unseen count , just update the NodeName.Chat with NodeName UnseenCount
        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference();
        DatabaseReference chatDatabaseRef = rootDatabaseRef.child(NodeNames.CHAT).child(chatUserId).child(currentUserId);

        // first read the current count and increment it
        chatDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentCount = "0";
                if(snapshot.child(NodeNames.UNSEEN_COUNT).getValue() != null){
                    currentCount = snapshot.child(NodeNames.UNSEEN_COUNT).getValue().toString();
                }
                Map chatMap = new HashMap();
                chatMap.put(NodeNames.TIMESTAMP, ServerValue.TIMESTAMP);
                chatMap.put(NodeNames.UNSEEN_COUNT,Integer.valueOf(currentCount)+1);

                Map userChatMap = new HashMap<>();
                userChatMap.put(NodeNames.CHAT + "/"+chatUserId+"/"+currentUserId,chatMap);

                rootDatabaseRef.updateChildren(userChatMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error != null){
                            Toast.makeText(context,"Something went wrong : "+error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context,"Something went wrong : "+error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
