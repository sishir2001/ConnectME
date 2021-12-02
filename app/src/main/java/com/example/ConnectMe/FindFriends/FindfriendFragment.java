package com.example.ConnectMe.FindFriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.R;
import com.example.ConnectMe.databinding.FragmentFindfriendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// TODO : How and where to set clicklisteners on button inside every view of recycler view
// TODO : How and where to write the logic when the button is clicked

// TODO : user will not be updated with request status in realtime

public class FindfriendFragment extends Fragment {

    // for xml file
    private FragmentFindfriendBinding binding;
//    private View progressBar;

    // for adapter
    // list for storing the data fetched from FireBaseDatabase
    private List<FindFriendsModel> findFriendsModelList;
    private FindFriendsAdapter findFriendsAdapter;

    // Firebase details
    private DatabaseReference databaseReference , friendRequestDatabaseReference;
    final private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener userEventListener;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFindfriendBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        progressBar = view.findViewById(R.id.progressBarFindFriends);
        // here we need to retrieve the list from internet and map it according to the local storing list

        databaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.USERS); // getRefernce gives the refernce to user node

        friendRequestDatabaseReference = FirebaseDatabase
                .getInstance(Constants.DATABASE_LINK)
                .getReference()
                .child(NodeNames.REQ_FRIENDS)
                .child(currentUser.getUid()); // getRefernce gives the refernce to friend_request node

        // initialize the list
        findFriendsModelList = new ArrayList<FindFriendsModel>();

        // Initialize the recycle view adapter and map the recylerview to its adapter
        binding.recyclerViewFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        findFriendsAdapter = new FindFriendsAdapter(getActivity(),findFriendsModelList);
        binding.recyclerViewFindFriends.setAdapter(findFriendsAdapter);


        // initializing the value event listner for users fetching
        updateValueListenerForUsersList();
        // fetching users list
        fetchUsersList(); // call only after updateValueListenerForUsersList()
        binding.srlUsers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.srlUsers.setRefreshing(false);
                updateValueListenerForUsersList();
                fetchUsersList(); // call only after updateValueListenerForUsersList()
            }
        });

//         adding listeners to RequestNode of the current user
//         for changing the buttons
//        FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference()
//                .child(NodeNames.REQ_FRIENDS)
//                .child(currentUser.getUid())
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        fetchUsersList();
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        fetchUsersList();
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                        fetchUsersList();
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
    }

    private void fetchUsersList(){
        binding.progressBarFindFriends.setVisibility(View.VISIBLE);
        // fetching the list
        // Root -> USERS
        Query query = databaseReference.orderByChild(NodeNames.NAME);// listing lexically
        // Root -> USERS
        query.addValueEventListener(userEventListener);
    }

    private void updateValueListenerForUsersList(){

        // Mentioning what to do to the usersList when concerning data is changed anywhere
        findFriendsModelList.clear();
        userEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // snapshot is like a list
                // fired every time the data is chaged

                for(DataSnapshot ds:snapshot.getChildren()){
                    // here we need to check the id of the child and current user , so as to not display in the request list
                    String otherUser = ds.getKey();
                    // check for the current user
                    if(currentUser.getUid().equals(otherUser)){
                        // getKey -> Uid in firebase
//                        return;
                        continue;
                    }
                    if(ds.child(NodeNames.NAME).getValue()!= null){
                        // if the name is not blank in database
//                        Toast.makeText(getContext(), "Inside You Know 0", Toast.LENGTH_SHORT).show();
                        String fullname = ds.child(NodeNames.NAME).getValue().toString();
                        String photoName = ds.child(NodeNames.PHOTO).getValue().toString();

                        // TODO : check whether the loggedIn user has sent request to this particular user
                        // need friendReqDatabase Reference

//                        findFriendsModelList.add(new FindFriendsModel(fullname,photoName,ds.getKey(),false));
//                        findFriendsAdapter.notifyDataSetChanged();// whole data will changed

                        assert otherUser != null;
                        friendRequestDatabaseReference.child(otherUser).child(NodeNames.REQ_TYPE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String requestType = snapshot.getValue().toString();
//                                   Toast.makeText(getContext(),requestType, Toast.LENGTH_SHORT).show();
                                    Log.i("FindFriendFragment",""+requestType);
                                    if(requestType.equals(Constants.REQ_ACCEPTED)){
                                        binding.progressBarFindFriends.setVisibility(View.GONE);
                                        findFriendsModelList.add(new FindFriendsModel(fullname,photoName,otherUser,false,true));
                                        findFriendsAdapter.notifyDataSetChanged();// whole data will changed
                                    }
                                    else if(requestType.equals(Constants.REQ_SENT)){
                                        binding.progressBarFindFriends.setVisibility(View.GONE);
                                        findFriendsModelList.add(new FindFriendsModel(fullname,photoName,otherUser,true,false));
                                        findFriendsAdapter.notifyDataSetChanged();// whole data will changed
                                    }
                                    else{
                                        binding.progressBarFindFriends.setVisibility(View.GONE);
                                        findFriendsModelList.add(new FindFriendsModel(fullname,photoName,otherUser,false,false));
                                        findFriendsAdapter.notifyDataSetChanged();// whole data will changed
                                    }
                                }
                                else{
//                                   Toast.makeText(getContext(),"snapshot doesnt exist", Toast.LENGTH_SHORT).show();
                                    binding.progressBarFindFriends.setVisibility(View.GONE);
                                    findFriendsModelList.add(new FindFriendsModel(fullname,photoName,otherUser,false,false));
                                    findFriendsAdapter.notifyDataSetChanged();// whole data will changed
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
//                                Toast.makeText(getContext(), "Inside You Know 1 onCancelled", Toast.LENGTH_SHORT).show();
                                binding.progressBarFindFriends.setVisibility(View.GONE);
                                findFriendsModelList.add(new FindFriendsModel(fullname,photoName,otherUser,false,false));
                                findFriendsAdapter.notifyDataSetChanged();// whole data will changed
                            }
                        });
                    }
                }
                // if the list is empty , then no other user other than the signed user
                if(findFriendsModelList.isEmpty()){
                    binding.progressBarFindFriends.setVisibility(View.GONE);
//                    binding.textViewEmptyUsers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                binding.textViewEmptyUsers.setVisibility(View.VISIBLE);
                binding.progressBarFindFriends.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_fetch_data,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        };
    }
}