package com.example.chatapplication.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Messages.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.common.Extras;
import com.example.chatapplication.common.NodeNames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ChatListModel> chatList;
    private StorageReference fileRef;

    public ChatListAdapter(Context context, List<ChatListModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatListViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        // binding the data with the layouts
        ChatListModel listElement = chatList.get(position);
        holder.userName.setText(listElement.getUserName());

        // initilizing the reference
        fileRef = FirebaseStorage.getInstance().getReference(NodeNames.IMAGES+"/"+listElement.getUserPhoto());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .error(R.drawable.default_profile)
                        .placeholder(R.drawable.default_profile)
                        .into(holder.civImageViewChat);

            }
        });
        // But this is a bad practice of setting clicklisteners on Items of recyclerView
        // Setting view listener on the whole chat layout
        holder.clChatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // navigate to main activity with the friends user id
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Extras.USER_KEY,listElement.getUserId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{
//        private LinearLayout llchatList;
        private TextView userName,lastMessage,lastMessageTime,unseenCount;
        private CircleImageView civImageViewChat;
        private ConstraintLayout clChatLayout;
        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textViewUserNameChat);
            lastMessageTime = itemView.findViewById(R.id.textViewLastMsgTime);
            lastMessage = itemView.findViewById(R.id.textViewlastMessage);
            unseenCount = itemView.findViewById(R.id.textViewUnseenMessageCount);
            civImageViewChat = itemView.findViewById(R.id.civDefaultProfileChat);
            clChatLayout = itemView.findViewById(R.id.clChatLayout);
        }
    }
}
