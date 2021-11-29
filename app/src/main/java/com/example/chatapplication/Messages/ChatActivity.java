package com.example.chatapplication.Messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.common.Constants;
import com.example.chatapplication.common.Extras;
import com.example.chatapplication.common.NodeNames;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    // this is the activity where the conversational screen appears
    private ActivityChatBinding binding;
    private String chatUserId,currentUserId;
    private DatabaseReference rootDatabaseReference;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Checking for extras in intent
        if(getIntent().hasExtra(Extras.USER_KEY)){
            chatUserId = getIntent().getStringExtra(Extras.USER_KEY);
        }

        rootDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        binding.ivSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.ivSend:
                String msg = binding.etMessage.getText().toString().trim();
                // clearing the edit text
                binding.etMessage.setText(getString(R.string.empty_string));
                // how to generate push id
                DatabaseReference sendMessagePush = rootDatabaseReference.child(currentUserId).child(chatUserId).push();
                String pushId = sendMessagePush.getKey();
                SendMessage(msg,Constants.MSG_TYPE_TEXT,pushId);
                break;
        }
    }

    private void SendMessage(String msg,String msgType,String pushId){
        // pushId should be same in both the users messages nodes

        // just need to update the nodes
        // messages -> CurrentUserId -> ChatUserId -> PushId -> HashMap(msgId/pushId,msg,senderId/currentUserId,sentTime,msgType)
        try {
            // checking if the message is not null
            if(!msg.equals(getString(R.string.empty_string))){
                // HashMap for updating after the message id
                HashMap messageMap = new HashMap();
                messageMap.put(NodeNames.MSG_ID,pushId);
                messageMap.put(NodeNames.MSG,msg);
                messageMap.put(NodeNames.MSG_FROM,currentUserId);
                messageMap.put(NodeNames.MSG_TIME, ServerValue.TIMESTAMP);
                messageMap.put(NodeNames.MSG_TYPE, msgType);

                String currentUserReference = NodeNames.MESSAGES + "/" + currentUserId +"/"+chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId ;

                HashMap messageUsermap = new HashMap();
                messageUsermap.put(currentUserReference + "/" + pushId,messageMap);
                messageUsermap.put(chatUserRef + "/" + pushId,messageMap);

                rootDatabaseReference.updateChildren(messageUsermap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                       if(error!=null){
                           // there was some error
                           Toast.makeText(ChatActivity.this,getString(R.string.msg_send_error,error.getMessage()), Toast.LENGTH_SHORT).show();
                       }
                       else{
                           Toast.makeText(ChatActivity.this,getString(R.string.message_sent_success), Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }
        }
        catch (Exception e){
            Toast.makeText(ChatActivity.this,getString(R.string.msg_send_error,e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}