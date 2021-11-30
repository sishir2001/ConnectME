package com.example.ConnectMe.FriendRequest;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private Context context;
    private List<FriendRequestModel> requestList;
    private FirebaseUser currentUser;
    private DatabaseReference friendRequestDatabaseReference;
    private DatabaseReference chatDatabaseReference;

    public FriendRequestAdapter(Context context,List<FriendRequestModel> requestList) {
        this.requestList = requestList;
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.friendRequestDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.REQ_FRIENDS);
        this.chatDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.CHAT);

    }


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
        holder.btnDeny.setOnClickListener(view -> btnDenyClicked(listElement,holder));
        holder.btnAccept.setOnClickListener(view -> btnAcceptClicked(listElement,holder));

    }
    private void btnAcceptClicked(FriendRequestModel otherUserDetails,@NonNull FriendRequestViewHolder holder){
        // first need to add both users to chat database with timestamp
        holder.pbFriendRequest.setVisibility(View.VISIBLE);
        chatDatabaseReference.child(currentUser.getUid()).child(otherUserDetails.getUserID()).child(NodeNames.TIMESTAMP).setValue(ServerValue.TIMESTAMP)
                .addOnCompleteListener(task -> {
                    holder.pbFriendRequest.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        chatDatabaseReference.child(otherUserDetails.getUserID()).child(currentUser.getUid())
                                .child(NodeNames.TIMESTAMP)
                                .setValue(ServerValue.TIMESTAMP)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        Toast.makeText(context, "Accepted Request Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(context, "Couldnt accept request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        Toast.makeText(context, "Couldnt accept request", Toast.LENGTH_SHORT).show();
                    }
                    this.notifyDataSetChanged();
                });
        // second need to set request type as accepted in both users
        friendRequestDatabaseReference.child(currentUser.getUid()).child(otherUserDetails.getUserID()).child(NodeNames.REQ_TYPE).setValue(Constants.REQ_ACCEPTED)
                .addOnCompleteListener(task -> {
                    holder.pbFriendRequest.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        // then delete from the other user node
                        friendRequestDatabaseReference.child(otherUserDetails.getUserID()).child(currentUser.getUid()).child(NodeNames.REQ_TYPE).setValue(Constants.REQ_ACCEPTED)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
//                                        need to modify the users database to trigger an event change in find Friend adapter
                                        Toast.makeText(context, "See Your Chat Tab !!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        Toast.makeText(context, "Problem in Find Tab", Toast.LENGTH_SHORT).show();
                    }
                    this.notifyDataSetChanged();
                });
    }
    private void btnDenyClicked(FriendRequestModel otherUserDetails,@NonNull FriendRequestViewHolder holder){
        // need to update the friendReq node
        holder.pbFriendRequest.setVisibility(View.VISIBLE);

        // first deleting the data from the current user
        friendRequestDatabaseReference.child(currentUser.getUid()).child(otherUserDetails.getUserID()).child(NodeNames.REQ_TYPE).setValue(null)
                .addOnCompleteListener(task -> {
                    holder.pbFriendRequest.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        // then delete from the other user node
                        friendRequestDatabaseReference.child(otherUserDetails.getUserID()).child(currentUser.getUid()).child(NodeNames.REQ_TYPE).setValue(null)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
//                                        need to modify the users database to trigger an event change in find Friend adapter
                                        Toast.makeText(context, "Deleted Request Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        Toast.makeText(context, "Couldn't delete the request", Toast.LENGTH_SHORT).show();
                    }
                    this.notifyDataSetChanged();
                });

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
