package com.example.ConnectMe.Messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConnectMe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessagesModel> messageList;
    private Context context;
    private FirebaseUser currentUser;

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
            holder.tvMessageSent.setText(listElement.getMessage());
            holder.tvMessageSentTime.setText(messageTime);
            holder.llSendMessage.setVisibility(View.VISIBLE);
            holder.llRecieveMessage.setVisibility(View.GONE);
        }
        else{

            holder.tvMessageRecieved.setText(listElement.getMessage());
            holder.tvMessageRecievedTime.setText(messageTime);
            holder.llSendMessage.setVisibility(View.GONE);
            holder.llRecieveMessage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSendMessage,llRecieveMessage;
        private TextView tvMessageSent,tvMessageSentTime,tvMessageRecieved,tvMessageRecievedTime;
        private ConstraintLayout clMessageLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            llSendMessage = itemView.findViewById(R.id.llsendMessage);
            llRecieveMessage = itemView.findViewById(R.id.llReceiveMessage);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvMessageSentTime = itemView.findViewById(R.id.tvMessageTimeSent);
            tvMessageRecieved = itemView.findViewById(R.id.tvMessageRecieved);
            tvMessageRecievedTime = itemView.findViewById(R.id.tvMessageTimeReceived);
            clMessageLayout = itemView.findViewById(R.id.clMessageLayout);
        }
    }
}
