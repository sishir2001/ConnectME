package com.example.ConnectMe.Messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConnectMe.R;
import com.example.ConnectMe.common.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessagesModel> messageList;
    private Context context;
    private FirebaseUser currentUser;
    private ActionMode actionMode;
    private ConstraintLayout selectedView;

    public MessageAdapter( Context context,List<MessagesModel> messageList) {
        this.messageList = messageList;
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();

    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.messages_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // binding to the list of recylerView
        MessagesModel listElement = messageList.get(position);
        String fromUserId = listElement.getMessage_from();

        // getting the date in proper format
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime = sdf.format(new Date(listElement.getTimestamp()));
        String[] splitString = dateTime.split(" ");
        String messageTime = splitString[1];
        // checking who sent this message

        if(fromUserId.equals(currentUser.getUid())){
            // message is sent by the user logged In
            // so right side should be visible
            // checking for type of image
            if(listElement.getType().equals(Constants.MSG_TYPE_IMG) || listElement.getType().equals(Constants.MSG_TYPE_VID)){
                // for image
                holder.llSendMessage.setVisibility(View.GONE);
                holder.llRecieveMessage.setVisibility(View.GONE);
                holder.llRecieveImage.setVisibility(View.GONE);

                Uri imageUri = Uri.parse(listElement.getMessage());
                // Loading the image into imageview
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_message_image)
                        .error(R.drawable.ic_message_image)
                        .into(holder.ivSendImage);
                holder.tvSendImageTime.setText(messageTime);

                holder.llSendImage.setVisibility(View.VISIBLE);
            }
            else if(listElement.getType().equals(Constants.MSG_TYPE_TEXT)){
                holder.tvMessageSent.setText(listElement.getMessage());
                holder.tvMessageSentTime.setText(messageTime);
                holder.llSendMessage.setVisibility(View.VISIBLE);
                holder.llRecieveMessage.setVisibility(View.GONE);
                holder.llRecieveImage.setVisibility(View.GONE);
                holder.llSendImage.setVisibility(View.GONE);
            }
//            else if(listElement.getType().equals(Constants.MSG_TYPE_VID)){
//                // For Video
//            }
        }
        else{
            // message is sent by the other user
            // so left side should be visible
            // checking for type of image
            if(listElement.getType().equals(Constants.MSG_TYPE_IMG) || listElement.getType().equals(Constants.MSG_TYPE_VID)){
                // for image
                holder.llSendMessage.setVisibility(View.GONE);
                holder.llRecieveMessage.setVisibility(View.GONE);
                holder.llSendImage.setVisibility(View.GONE);

                Uri imageUri = Uri.parse(listElement.getMessage());
                // Loading the image into imageview
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_message_image)
                        .error(R.drawable.ic_message_image)
                        .into(holder.ivRecieveImage);
                holder.tvRecieveImageTime.setText(messageTime);

                holder.llRecieveImage.setVisibility(View.VISIBLE);
            }
            else if(listElement.getType().equals(Constants.MSG_TYPE_TEXT)){
                holder.tvMessageRecieved.setText(listElement.getMessage());
                holder.tvMessageRecievedTime.setText(messageTime);
                holder.llRecieveMessage.setVisibility(View.VISIBLE);
                holder.llSendMessage.setVisibility(View.GONE);
                holder.llRecieveImage.setVisibility(View.GONE);
                holder.llSendImage.setVisibility(View.GONE);
            }
            else if(listElement.getType().equals(Constants.MSG_TYPE_VID)){
                // For Video
            }
        }
        holder.clMessageLayout.setTag(R.id.TAG_MESSAGE,listElement.getMessage());
        holder.clMessageLayout.setTag(R.id.TAG_MESSAGE_ID,listElement.getMessage_id());
        holder.clMessageLayout.setTag(R.id.TAG_MESSAGE_TYPE,listElement.getType());

        // setting clicklisteners to constraint layout
        holder.clMessageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgType = view.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri msgUri = Uri.parse(view.getTag(R.id.TAG_MESSAGE).toString());
                if(msgType.equals(Constants.MSG_TYPE_VID)){
                    // its a video
                    Intent vidIntent = new Intent(Intent.ACTION_VIEW,msgUri);
                    vidIntent.setDataAndType(msgUri,"video/mp4");
                    context.startActivity(vidIntent);
                }
                else if(msgType.equals(Constants.MSG_TYPE_IMG)){
                    Intent imgIntent = new Intent(Intent.ACTION_VIEW,msgUri);
                    imgIntent.setDataAndType(msgUri,"image/jpg");
                    context.startActivity(imgIntent);
                }
            }
        });
        // setting up the long ClickListeneres

        holder.clMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedView = holder.clMessageLayout;
                if(actionMode != null)
                    return false;
                actionMode = ((AppCompatActivity)context).startSupportActionMode(actionModeCallback);
                // changing the background color of the app
                view.setBackgroundColor(context.getColor(R.color.primaryLightColor));
//                view.setSelected(true);
//                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View view, boolean b) {
//                        if(b){
//                            Toast.makeText(context, "Has Focus", Toast.LENGTH_SHORT).show();
//                            view.setSelected(true);
//                        }
//                        else{
//                            Toast.makeText(context, "No Focus", Toast.LENGTH_SHORT).show();
//                            view.setSelected(false);
//                        }
//                    }
//                });
                return true;
            }
        });
    }
    private androidx.appcompat.view.ActionMode.Callback actionModeCallback = new androidx.appcompat.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_options,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            String messageId = selectedView.getTag(R.id.TAG_MESSAGE_ID).toString();
            String messageType = selectedView.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            String message = selectedView.getTag(R.id.TAG_MESSAGE).toString();

            switch(item.getItemId()){
                case R.id.mnuForward:
                    Toast.makeText(context, "Forward Clicked", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;

                case R.id.mnuDelete:
                    if(context instanceof ChatActivity){
                        ((ChatActivity)context).deleteMessage(messageId,messageType);
                    }
                    mode.finish();
                    return true;
                case R.id.mnuShare:
                    Toast.makeText(context, "Share Clicked", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                case R.id.mnuDownload:
                    Toast.makeText(context, "Download Clicked", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {

            actionMode = null;
            selectedView.setBackgroundColor(context.getColor(R.color.chat_bg));
        }
    };

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSendMessage,llRecieveMessage,llSendImage,llRecieveImage;
        private TextView tvMessageSent,tvMessageSentTime,tvMessageRecieved,tvMessageRecievedTime,tvSendImageTime,tvRecieveImageTime;
        private ConstraintLayout clMessageLayout;
        private ImageView ivSendImage,ivRecieveImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            llSendMessage = itemView.findViewById(R.id.llsendMessage);
            llRecieveMessage = itemView.findViewById(R.id.llReceiveMessage);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvMessageSentTime = itemView.findViewById(R.id.tvMessageTimeSent);
            tvMessageRecieved = itemView.findViewById(R.id.tvMessageRecieved);
            tvMessageRecievedTime = itemView.findViewById(R.id.tvMessageTimeReceived);
            clMessageLayout = itemView.findViewById(R.id.clMessageLayout);
            llSendImage = itemView.findViewById(R.id.llsendImage);
            llRecieveImage = itemView.findViewById(R.id.llRecieveImage);
            tvSendImageTime = itemView.findViewById(R.id.tvSendImageTime);
            tvRecieveImageTime = itemView.findViewById(R.id.tvRecieveImageTime);
            ivSendImage = itemView.findViewById(R.id.ivSendImage);
            ivRecieveImage = itemView.findViewById(R.id.ivRecieveImage);
        }
    }
}
