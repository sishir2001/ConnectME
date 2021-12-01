package com.example.ConnectMe.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConnectMe.common.Extras;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.Messages.ChatActivity;
import com.example.ConnectMe.R;
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

        Log.i("ChatListAdapter",listElement.getUserPhoto());
        // initilizing the reference
//        fileRef = FirebaseStorage.getInstance().getReference(NodeNames.IMAGES + "/" +listElement.getUserPhoto()); //getting the user link
//        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Glide.with(context)
//                        .load(uri)
//                        .error(R.drawable.default_profile1)
//                        .placeholder(R.drawable.default_profile)
//                        .into(holder.civImageViewChat);
//
//            }
//        });
        // Unseen Count
        if(listElement.getUnseenCount().equals("0")){
            holder.unseenCount.setVisibility(View.GONE);
        }
        else{
            holder.unseenCount.setVisibility(View.VISIBLE);
            holder.unseenCount.setText(listElement.getUnseenCount());
        }

        try{
           Uri userPhotoUri = Uri.parse(listElement.getUserPhoto());
           Glide.with(context)
                   .load(userPhotoUri)
                   .error(R.drawable.default_profile1)
                   .placeholder(R.drawable.default_profile)
                   .into(holder.civImageViewChat);
        }
        catch (Exception e){
           Toast.makeText(context,"Error in downloading " + listElement.getUserName() + "photo",Toast.LENGTH_SHORT).show();
        }

        // But this is a bad practice of setting clicklisteners on Items of recyclerView
        // Setting view listener on the whole chat layout
        holder.clChatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // navigate to main activity with the friends user id
                // need to set unseen count to 0
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Extras.USER_KEY,listElement.getUserId());
                intent.putExtra(Extras.USER_NAME,listElement.getUserName());
                intent.putExtra(Extras.USER_PHOTO_NAME,listElement.getUserPhoto());
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
