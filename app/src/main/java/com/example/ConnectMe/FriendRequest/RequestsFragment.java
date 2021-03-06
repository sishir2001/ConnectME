package com.example.ConnectMe.FriendRequest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.R;
import com.example.ConnectMe.databinding.FragmentRequestsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {
    // TODO : Redo layout file for all three tabs

    private FragmentRequestsBinding binding;
//    private View progressBar;

    // Adapter variables
    private List<FriendRequestModel> friendRequestList;
    private FriendRequestAdapter requestAdapter;

    // Firebase varibles
    private DatabaseReference friendReqDatabaseReference; // traversing only through the friend Request List
    private DatabaseReference databaseReference; // traversing only through the friend Request List
    final private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRequestsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        progressBar = view.findViewById(R.id.customProgressBar);
        binding.textViewEmptyRequest.setVisibility(View.VISIBLE);

        // fetching the list from the FriendReq->CurrentUserID->RequestType
        databaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.USERS);

        friendReqDatabaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.REQ_FRIENDS)
                .child(currentUser.getUid());// points current UserId as root


        friendRequestList = new ArrayList<>();
        binding.recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new FriendRequestAdapter(getActivity(),friendRequestList);
        binding.recyclerViewRequests.setAdapter(requestAdapter);

        // should listen to the child users
        fetchRequests();
//        friendReqDatabaseReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
////                fetchRequests();
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                binding.textViewEmptyRequest.setVisibility(View.VISIBLE);
//                binding.progressBarRequests.setVisibility(View.GONE);
//            }
//        });

    }

    private void fetchRequests(){
//        progressBar.setVisibility(View.VISIBLE);
        binding.progressBarRequests.setVisibility(View.VISIBLE);
        binding.textViewEmptyRequest.setVisibility(View.GONE);

        // listen everytime
        friendReqDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // when there is a change in the data , this callback will be fired
                // databaseReference is to the users , so who so evers users details is changed , this will be called and sent us the whole data
                friendRequestList.clear();
                // we will get a snapshot
                for(DataSnapshot ds:snapshot.getChildren()){
                    // ds : friend Request Sender
                    // TODO : get Username from userId of the user
                    // TODO : get PhotoName from the userId of the user
                    // User users database with the userId to fetch the above details

                    String otherUserId = ds.getKey();
                    // only of request type received
                    if(ds.child(NodeNames.REQ_TYPE).getValue().toString().equals(Constants.REQ_RECIEVED)){
                        // using databaseReference to fetch the details of the user

                        assert otherUserId != null;
                        databaseReference.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            // for reading only once
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                binding.progressBarRequests.setVisibility(View.GONE);

                                if(snapshot1.exists()){
                                    // add the details to list
                                    Log.i("RequestsFragment","Got details of particular user : "+snapshot1.child(NodeNames.NAME).getValue().toString());
//                                    Toast.makeText(getContext(),"Got Details of a particular user"+snapshot1.child(NodeNames.NAME).getValue().toString(), Toast.LENGTH_SHORT).show();
                                    Log.i("RequestsFragment",snapshot1.child(NodeNames.NAME).getValue().toString());
                                    String usrName = snapshot1.child(NodeNames.NAME).getValue().toString();
                                    String photoName = "";
                                    if(snapshot.child(NodeNames.PHOTO).getValue() != null){
                                        photoName = snapshot1.child(NodeNames.PHOTO).getValue().toString();
                                    }

                                    binding.progressBarRequests.setVisibility(View.GONE);
                                    friendRequestList.add(new FriendRequestModel(usrName,photoName,otherUserId));
                                }
                                else{
                                    Toast.makeText(getContext(),"SnapShot Doest Exist"+snapshot1.child(NodeNames.NAME).getValue().toString(), Toast.LENGTH_SHORT).show();
                                    binding.progressBarRequests.setVisibility(View.GONE);
                                }
                                // Updates the list after everychange
                                requestAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                binding.progressBarRequests.setVisibility(View.GONE);
                                binding.textViewEmptyRequest.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(),"reqType:recieved,onCancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                requestAdapter.notifyDataSetChanged();
                if(friendRequestList.isEmpty()){
                    Toast.makeText(getContext(),"Inside the if statement after loop", Toast.LENGTH_SHORT).show();
                    binding.progressBarRequests.setVisibility(View.GONE);
                    binding.textViewEmptyRequest.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarRequests.setVisibility(View.GONE);
                friendRequestList.clear();
                binding.textViewEmptyRequest.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(),getContext().getString(R.string.failed_to_fetch_data,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
