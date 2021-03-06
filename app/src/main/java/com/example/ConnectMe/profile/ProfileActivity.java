package com.example.ConnectMe.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ConnectMe.Authentication.LoginActivity;
import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.passwords.ChangePasswordActivity;
import com.example.ConnectMe.MessageActivity;
import com.example.ConnectMe.R;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.common.Util;
import com.example.ConnectMe.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

// TODO : no problem with Photo Uri here , as we can use userURI here
public class ProfileActivity extends AppCompatActivity {

    private String email,name;
    private ActivityProfileBinding binding;
    // for creating user in firebase
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    // DatabaseReference for realTime Database
    private DatabaseReference databaseReference;
    // StorageReference for static file storage
    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;
    private MaterialAlertDialogBuilder alertBuilder;
    private AlertDialog alertDialog;
//    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        progressBar = findViewById(R.id.progressBar);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        alertBuilder = new MaterialAlertDialogBuilder(this)
                .setCustomTitle(getLayoutInflater().inflate(R.layout.custom_progressbar,null));
        alertDialog = alertBuilder.create();


        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){

            actionBar.setTitle(getString(R.string.profile));
            actionBar.setElevation(1);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Initializing filestorage Storage reference
        fileStorage = FirebaseStorage.getInstance().getReference();// will give reference to the root folder
        // Initializing firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // checking whether the users login session is valid
        if(firebaseUser!=null){
            // Setting the text of Edit Texts
            // Initializing the required ones
            binding.etEmail.setText(firebaseUser.getEmail());
            FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.USERS).child(firebaseUser.getUid()).child(NodeNames.NAME).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        binding.etName.setText(snapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            serverFileUri = firebaseUser.getPhotoUrl();
//            Toast.makeText(ProfileActivity.this, "photoUrl : "+serverFileUri, Toast.LENGTH_LONG).show();

            if(serverFileUri!=null){
                // user has uploaded the picture
                // using glide to load the file from Firebase storage
                Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(binding.ImageViewdefaultProfile);

            }
        }

        // setting click listener to the image view
        binding.ImageViewdefaultProfile.setOnClickListener(view1 -> changeImage(view1));
        // setting click listener to the Save Button
        binding.buttonSave.setOnClickListener(view1 -> clickedSaveBtn() );

        // setting click listener to logout button
        binding.buttonLogout.setOnClickListener(view1 -> {
            MaterialAlertDialogBuilder materialBuilder = new MaterialAlertDialogBuilder(this)
                    .setCustomTitle(getLayoutInflater().inflate(R.layout.custom_progressbar,null));

            AlertDialog alertDialog = materialBuilder.create();
            alertDialog.show();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            DatabaseReference tokenDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference()
                    .child(NodeNames.TOKENS).child(NodeNames.DEVICE_TOKEN).child(currentUser.getUid());
            tokenDatabaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
//                        Toast.makeText(ProfileActivity.this,"Token revoked",Toast.LENGTH_SHORT);
                        alertDialog.dismiss();
                        firebaseAuth.signOut();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        finish(); // even though user clicks the back button, he cannot navigate to this activity

                    }
                    else{
                        alertDialog.dismiss();
                        Toast.makeText(ProfileActivity.this,"Something went wrong"+task.getException(),Toast.LENGTH_SHORT);
                    }
                }
            });
        });

        // setting click listerner to change password text view
        binding.textViewChangePassword.setOnClickListener(view1 -> {
            startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
        });

    }
    private void clickedSaveBtn(){
        email = binding.etEmail.getText().toString().trim();
        name = binding.etName.getText().toString().trim();

        if(email.equals("")){
            binding.tilEmailProfile.setError(getString(R.string.enter_email));
            binding.tilEmailProfile.requestFocus();
        }
        else if(name.equals("")){
            binding.tilNameProfile.setError(getString(R.string.enter_name));
            binding.tilNameProfile.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.tilEmailProfile.setError(getString(R.string.enter_correct_email));
            binding.tilEmailProfile.requestFocus();
        }
        else{
            if(Util.connectionAvailable(this)){
                if(localFileUri!=null){
                    // user has picked a profile picture
                    updatePicAndName();
                }
                else{
                    updateOnlyName();
                }
            }
            else{
                startActivity(new Intent(ProfileActivity.this,MessageActivity.class));
            }
        }
    }
    private void changeImage(View view){
        // checking whether user has uploaded the profile picture
        if(serverFileUri!=null){
            // user already has a profile picture
            // User might pick for two options
            // TODO : Remove picture
            // TODO : change the actual picture

            // Using popUpMenu to handle the click
            PopupMenu popupMenu = new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.image_pick_menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if(menuItem.getItemId() == R.id.menuChangePic){
                        // change the picture
                        pickImage();
                    }
                    else if(menuItem.getItemId() == R.id.menuRemovePic){
                        // need to remove the pic
                        removePhoto();
                    }
                    return false;
                }
            });
            popupMenu.show();

        }
        else{
            pickImage();
        }
    }

    private void removePhoto(){

//        progressBar.setVisibility(View.VISIBLE);
        alertDialog.show();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(binding.etName.getText().toString().trim())
                .setPhotoUri(null)
                .build();
        // sending request to Firebase Realtime database
        if(Util.connectionAvailable(this)){
            firebaseUser.updateProfile(request).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    // successfully removed the profile picture . but we need to load the default pic
                    binding.ImageViewdefaultProfile.setImageDrawable(getResources().getDrawable(R.drawable.default_profile1));
                    databaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root

                    String userId = firebaseUser.getUid();
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME,binding.etName.getText().toString().trim());
                    hashMap.put(NodeNames.EMAIL,binding.etEmail.getText().toString().trim());
                    hashMap.put(NodeNames.ONLINE,getString(R.string.online));
                    hashMap.put(NodeNames.PHOTO,"");

                    databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task1 -> {
//                    progressBar.setVisibility(View.GONE);
                        if(task1.isSuccessful()){
                            alertDialog.dismiss();
//                            Toast.makeText(ProfileActivity.this, "Profile Picture Removed Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // remove file from cloud storage
                    String strFileName = firebaseUser.getUid() + ".jpg";
                    final StorageReference fileRef = fileStorage.child(NodeNames.IMAGES + "/" +strFileName);
                    fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
//                            Toast.makeText(ProfileActivity.this, "Deleted File even in storage", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(ProfileActivity.this, "Deletion Failed in Cloud", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
//                progressBar.setVisibility(View.GONE);
                    alertDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Error in removing Profile Picture", Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{
            alertDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
        }
    }

    private void pickImage(){
        // TODO : Modify the code, so that user is able to choose an image from gallery or external storage
        // when the user clicks on the default profile pic to choose a pic from gallery
        // Permission needs to be taken in the manifest file
        // Reading External Storage is a dangerous type of permission -> first we need to check the permission of the user , else take the permission from the user
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,101); // The request code can be any numeber, its just for reference
        }
        else{
            // request for the user permission
//            Toast.makeText(ProfileActivity.this,"Requesting Permission from activityCompat",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
//                Toast.makeText(ProfileActivity.this,"Should Show Rationale",Toast.LENGTH_SHORT).show();
                // build a AlertBox
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission Needed")
                        .setMessage("to read external storage")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                Toast.makeText(ProfileActivity.this,getString(R.string.permission_not_granted_file), Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.create().show();
            }
            else{
                // directly request permission
//                Toast.makeText(ProfileActivity.this,"Not required to Show Rationale",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
            }
            // need to handle this response of a permission in a callback
        }

    }
//    ActivityResultLauncher<Intent> externalFileAcitivityLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result.getResultCode() == Activity.RESULT_OK){
//                        // granted permission
//                        localFileUri = result.getData().toUri();
//                    }
//                }
//            }
//    );
//    private void getActivityResultForExtFileStorage(){
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//    }

    // to handle the response of user for dangerous permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("SignUpActivity","Inside onRequestPermissionsResult callback");
        if(requestCode == 102){
            // a random choosen while requesting for permission
            Log.i("SignUpActivity","Request code of 102 matched");
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // we can access the external storge
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,101); // The request code can be any numeber, its just for reference
            }
            else{
                Log.i("SignUpActivity","Permission not granted");
                Toast.makeText(ProfileActivity.this,getString(R.string.permission_not_granted_file), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // this callback is to check for the results of external activity that returns a result
        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                // User has selected an image
                // need to store the uri of the data
                localFileUri = data.getData(); // give us the path of the image
                // setting the imageView
                binding.ImageViewdefaultProfile.setImageURI(localFileUri);
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(ProfileActivity.this,getString(R.string.did_not_choose_any_photo), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePicAndName(){
        // First we need to upload the image
        String strFileName = firebaseUser.getUid() + ".jpg";

        alertDialog.show();
//        progressBar.setVisibility(View.VISIBLE);
        final StorageReference fileRef = fileStorage.child(NodeNames.IMAGES + "/" +strFileName); // refernce to a particular destination where the file can be uploaded
        fileRef.putFile(localFileUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                // we need to update the location of the file in the server for Users realtime database
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    serverFileUri = uri;
                    // now we need to update the User profile in realtime database with the link of Profile Picture stored in Firebase Storage
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(binding.etName.getText().toString().trim())
                            .setPhotoUri(serverFileUri)
                            .build();
                    firebaseUser.updateProfile(request).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            // Updating of user profile is succesful
                            // Last node for reference of the data
                            String userID = firebaseUser.getUid();
                            // if completed update the realtime database
                            databaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root
                            // Hashmap is like schema for database
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put(NodeNames.NAME,binding.etName.getText().toString().trim());
                            hashMap.put(NodeNames.EMAIL,binding.etEmail.getText().toString().trim());
                            hashMap.put(NodeNames.ONLINE,getString(R.string.online));
//                            hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());
                            hashMap.put(NodeNames.PHOTO,serverFileUri.toString());

                            // pushing the data to the child node
                            databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task2 -> {
//                                progressBar.setVisibility(View.GONE);

                                if(task2.isSuccessful()){
                                    alertDialog.dismiss();
                                    finish();
                                }
                                else{
                                    alertDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this,"Syncing of user profile with firebase error",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            alertDialog.dismiss();
//                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this,getString(R.string.user_updation_failed,task.getException()), Toast.LENGTH_LONG).show();
                            Log.i("SignUpActivity","Failed to Update profile using firebaseUser.updateProfile()");
                        }

                    });

                });

            }
            else{
//                progressBar.setVisibility(View.GONE);
                alertDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Error in Syncing Profile picture", Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            // Handling the Up button
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOnlyName(){
        // Updating User details in the realtime database
//        progressBar.setVisibility(View.VISIBLE);
        alertDialog.show();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(binding.etName.getText().toString().trim())
                .build();
        firebaseUser.updateProfile(request).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                // Getting the UID of the user
                Toast.makeText(ProfileActivity.this, "Profile Updation Successful,FireBaseUser", Toast.LENGTH_SHORT).show();
                String userID = firebaseUser.getUid();
                // We need a databaseReference object to update the user details
                Log.i("SignUpActivity","Before initializing database reference ");
                // TODO : To get reference of database other than in US region , we need to pass database url to getInstance method
                databaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root
                Log.i("SignUpActivity","After initializing database reference ");

                // Hashmap is similar to dictionary in python
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put(NodeNames.NAME,binding.etName.getText().toString().trim());
                hashMap.put(NodeNames.EMAIL,binding.etEmail.getText().toString().trim());
                hashMap.put(NodeNames.ONLINE,getString(R.string.online));
                hashMap.put(NodeNames.PHOTO,"");

                databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task1 -> {
//                    progressBar.setVisibility(View.GONE);
                    if(task1.isSuccessful()){
//                        Toast.makeText(ProfileActivity.this,getString(R.string.profile_updated_succ), Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        finish();
                    }
                    else{
                        alertDialog.dismiss();
//                        Toast.makeText(ProfileActivity.this, "Updation of Hashmap of userProfile failed : "+task1.getException(), Toast.LENGTH_SHORT).show();
                        Log.i("SignUpActivity","Updation of user profile failed : "+task1.getException());
                    }
                });
            }
            else{
//                progressBar.setVisibility(View.GONE);
                alertDialog.dismiss();
                Toast.makeText(ProfileActivity.this,getString(R.string.user_updation_failed,task.getException()), Toast.LENGTH_LONG).show();
                Log.i("SignUpActivity","Failed to Update profile using firebaseUser.updateProfile()");
            }
        });
    }
}