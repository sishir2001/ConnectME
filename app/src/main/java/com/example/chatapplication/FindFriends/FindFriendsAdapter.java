package com.example.chatapplication.FindFriends;

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
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// adapter is just the bridge between data and widgets to display the information
public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.FindFriendsViewHolder> {
    // class variables
    private Context context;
    private List<FindFriendsModel> findFriendsList;

    public FindFriendsAdapter(Context context, List<FindFriendsModel> findFriendsList) {
        this.context = context;
        this.findFriendsList = findFriendsList;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating the layout file
        // Layout Inflator returns a view from an xml file
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout,parent,false);
        return new FindFriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendsAdapter.FindFriendsViewHolder holder, int position) {
        // getting data from the list
        FindFriendsModel listElement = findFriendsList.get(position);
        // binding the data with the view here
        holder.tvFullName.setText(listElement.getUserName());
        // using glide to dowload the image from the FirebaseStorage
        StorageReference fileRef = FirebaseStorage.getInstance().getReference(NodeNames.IMAGES+"/"+listElement.getUserPhoto());
        // get the uri of the photo using fileref
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
               // using glide to download the uri
                Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .into(holder.civProfile);
            }
        });

    }

    @Override
    public int getItemCount() {
        return findFriendsList.size();
    }

    public class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        // These variables hold the id's for the widgets in the layout file
        private Button btnSendRequest,btnCancelRequest;
        private TextView tvFullName;
        private ProgressBar pbRequest;
        private CircleImageView civProfile;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Here mapping widgets to variables with their ids
            btnSendRequest = itemView.findViewById(R.id.btnSendFriendRequest);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            pbRequest = itemView.findViewById(R.id.pbSendReq);
            civProfile = itemView.findViewById(R.id.civDefualtProfile);
        }
    }
}
