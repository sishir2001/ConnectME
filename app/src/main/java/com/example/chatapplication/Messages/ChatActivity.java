package com.example.chatapplication.Messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.common.Constants;
import com.example.chatapplication.common.Extras;
import com.example.chatapplication.common.NodeNames;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    // this is the activity where the conversational screen appears
    private ActivityChatBinding binding;
    private String chatUserId,currentUserId;
    private DatabaseReference rootDatabaseReference;
    private FirebaseAuth firebaseAuth;


    // Displaying chats
    private MessageAdapter messageAdapter;
    private List<MessagesModel> messagesModelList;
    private int currentPage = 1;
    private static final int PAGE_LIMIT = 30;
    private ChildEventListener childEventListener;
    private DatabaseReference messagesDatabaseReferences;

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

        // For fetching the chats
        messagesModelList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messagesModelList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(messageAdapter);
        messagesDatabaseReferences = rootDatabaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);// Directly to the messages


        loadMessages();
        binding.rvMessages.scrollToPosition(messagesModelList.size()-1);

        binding.srlMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // this method will be called when the user swipes the page
                currentPage++; // loading the messages according to the pages
//                binding.srlMessages.setRefreshing(true);
                Toast.makeText(ChatActivity.this, "currentPage :"+currentPage, Toast.LENGTH_SHORT).show();
                loadMessages();
            }
        });

    }

    private void loadMessages(){
        // load the adapter with the messages , at once only display 30 chats
        // Clearing the list of messages whenever this function is called
        Query query = messagesDatabaseReferences.limitToLast(PAGE_LIMIT*currentPage); // as we move up current Page value increases
        messagesModelList.clear();
        if(childEventListener!=null){
            query.removeEventListener(childEventListener);
        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessagesModel messagesModel = snapshot.getValue(MessagesModel.class); // automatically deserializes the data to that class object
                messagesModelList.add(messagesModel);
                messageAdapter.notifyDataSetChanged();
                binding.rvMessages.scrollToPosition(messagesModelList.size()-1);// scroll to the latest message
                binding.srlMessages.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // When the child is edited

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // when the message is deleted

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // moved to somewhere else

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // when the message could not be fetched
                Toast.makeText(ChatActivity.this,"Message Fetching Error : "+error.getMessage(),Toast.LENGTH_SHORT).show();
                binding.srlMessages.setRefreshing(false);
            }
        };
        query.addChildEventListener(childEventListener);
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