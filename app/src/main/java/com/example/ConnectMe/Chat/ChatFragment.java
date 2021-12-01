package com.example.ConnectMe.Chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.R;
import com.example.ConnectMe.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;


import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
//    public ChatFragment() {
//        // Required empty public constructor
//    }
    private FragmentChatBinding binding;
    private View progressBar;

    final private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference chatDatabaseReference,databaseReference;

    private List<ChatListModel> chatList;
    private List<String> userIdList;

    private ChatListAdapter chatListAdapter;

    private Query query;
    private ChildEventListener childEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater,container,false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.customProgressBar);
        // initializing the database
        databaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.USERS);

        chatDatabaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.CHAT)
                .child(currentUser.getUid()); // reference to loggedIn user node

        // initializing the chat list
        chatList = new ArrayList<>();
        userIdList = new ArrayList<>();

        chatListAdapter = new ChatListAdapter(getActivity(),chatList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(chatListAdapter);

        query = chatDatabaseReference.orderByChild(NodeNames.TIMESTAMP);
        // listens to the children for a particular node
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,true,snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot,false,snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        query.addChildEventListener(childEventListener);

    }
    private void updateList(@NotNull DataSnapshot snapshot,Boolean isNew,String userId){
            // need to add data to chatList
        progressBar.setVisibility(View.VISIBLE);
        String lastMessage = "",lastMessageTime= "";// refer from messages Node
        String unseenCount = snapshot.child(NodeNames.UNSEEN_COUNT).getValue() == null ? "0" : snapshot.child(NodeNames.UNSEEN_COUNT).getValue().toString();

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // fetch the data here
                progressBar.setVisibility(View.GONE);
                binding.textViewEmpty.setVisibility(View.GONE); // check
                if(snapshot.exists()){
                    String userName = snapshot.child(NodeNames.NAME).getValue().toString() == null ?"":snapshot.child(NodeNames.NAME).getValue().toString();
                    String userPhoto = snapshot.child(NodeNames.PHOTO).getValue().toString() == null ?"":snapshot.child(NodeNames.PHOTO).getValue().toString();
                    Toast.makeText(getActivity(),userName + " : " + userPhoto,Toast.LENGTH_SHORT).show();
                    Log.i("ChatFragment",userName + " : " + userPhoto);

                    // only if the user is new , directly add to the list
                    ChatListModel chatListModel = new ChatListModel(userId,userName,userPhoto,lastMessage,lastMessageTime,unseenCount);
                    if(isNew){
                        chatList.add(chatListModel);
                        userIdList.add(userId);
                    }
                    else{
                        int indexOfClickedUser = userIdList.indexOf(userId);
                        chatList.set(indexOfClickedUser,chatListModel);
                    }
                    chatListAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getActivity(), "SnapShot doesnt Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Couldnt fetch the chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }
}