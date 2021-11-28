package com.example.chatapplication.FriendRequest;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.common.NodeNames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private List<FriendRequestModel> requestList;

    public FriendRequestAdapter(Context context,List<FriendRequestModel> requestList) {
        this.requestList = requestList;
        this.context = context;
    }

    private Context context;

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // return a viewHolder in terms of view, initialize it here
        View view = LayoutInflater.from(this.context).inflate(R.layout.friend_request_layout,parent,false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        // binding of the data here
        FriendRequestModel listElement = requestList.get(position);

        holder.username.setText(listElement.getUserName());
        // getting the uri of the photo from firebase Storage
        StorageReference fileRef = FirebaseStorage.getInstance().getReference(NodeNames.IMAGES+"/"+listElement.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // using glide to display the image from Firebase Storage
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(holder.civImageViewFriendReq);
            }
        });

        // adding click listeners to buttons

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        // holds the data
        private TextView username;
        private CircleImageView civImageViewFriendReq;
        private Button btnAccept,btnDeny;
        private ProgressBar pbFriendRequest;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // we need to map id here
            username = itemView.findViewById(R.id.tvFullNameFriendReq);
            civImageViewFriendReq = itemView.findViewById(R.id.civDefualtProfileFriendReq);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDeny = itemView.findViewById(R.id.btnDeny);
            pbFriendRequest = itemView.findViewById(R.id.pbFriendReq);
        }
    }
}
