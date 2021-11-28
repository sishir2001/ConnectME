package com.example.chatapplication.FindFriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.common.Constants;
import com.example.chatapplication.common.NodeNames;
import com.example.chatapplication.common.Util;
import com.example.chatapplication.databinding.FragmentFindfriendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

// TODO : How and where to set clicklisteners on button inside every view of recycler view
// TODO : How and where to write the logic when the button is clicked

public class FindfriendFragment extends Fragment {

    // for xml file
    private FragmentFindfriendBinding binding;
    private View progressBar;

    // for adapter
    // list for storing the data fetched from FireBaseDatabase
    private List<FindFriendsModel> findFriendsModelList;
    private FindFriendsAdapter findFriendsAdapter;

    // Firebase details
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFindfriendBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.customProgressBar);
        // here we need to retrieve the list from internet and map it according to the local storing list
        databaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // initialize the list
        findFriendsModelList = new ArrayList<FindFriendsModel>();

        // Initialize the recycle view adapter and map the recylerview to its adapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        findFriendsAdapter = new FindFriendsAdapter(getActivity(),findFriendsModelList);
        binding.recyclerView.setAdapter(findFriendsAdapter);

        // fetching a list from realtime database

        if(Util.connectionAvailable(getContext())){
           fetchUsersList();
        }
        else{
            // if no internet
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        }


    }
    private void fetchUsersList(){

        progressBar.setVisibility(View.VISIBLE);

        // fetching the list
        Query query = databaseReference.orderByChild(NodeNames.NAME);// listing lexically
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findFriendsModelList.clear();
                // snapshot is like a list

                for(DataSnapshot ds:snapshot.getChildren()){
                    // here we need to check the id of the child and current user , so as to not display in the request list
                    if(currentUser.getUid().equals(ds.getKey())){
                        // getKey -> Uid in firebase
                        continue;
                    }
                    if(ds.child(NodeNames.NAME).getValue()!= null){
                        // if the name is not blank in database
                        String fullname = ds.child(NodeNames.NAME).getValue().toString();
                        String photoName = ds.child(NodeNames.PHOTO).getValue().toString();

                        findFriendsModelList.add(new FindFriendsModel(fullname,photoName,ds.getKey(),false));
                        findFriendsAdapter.notifyDataSetChanged();// whole data will changed

                        progressBar.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_fetch_data,error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}