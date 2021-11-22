package com.example.chatapplication.Authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.common.NodeNames;
import com.example.chatapplication.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String email,password,name,confirmPassword;
    // for creating user in firebase
    private FirebaseUser firebaseUser;
    // DatabaseReference for realTime Database
    private DatabaseReference databaseReference;
    // StorageReference for static file storage
    private StorageReference fileStorage;
    private Uri localFileUri,serverFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        // Initializing filestorage
        fileStorage = FirebaseStorage.getInstance().getReference();// will give reference to the root folder
        // View Binding
        View view = binding.getRoot();
        setContentView(view);
        binding.signUpButton.setOnClickListener(view1 -> signUpBtnClicked());
        // setting click listeners on image view
        binding.ImageViewdefaultProfile.setOnClickListener(view1 -> pickImage());

    }
    private void pickImage(){
        // when the user clicks on the default profile pic to choose a pic from gallery
        // Permission needs to be taken in the manifest file
        // Reading External Storage is a dangerous type of permission -> first we need to check the permission of the user , else take the permission from the user
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,101); // The request code can be any numeber, its just for reference
        }
        else{
            // request for the user permission
            Toast.makeText(SignUpActivity.this,"Requesting Permission from activityCompat",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(SignUpActivity.this,"Should Show Rationale",Toast.LENGTH_SHORT).show();
                // build a AlertBox
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission Needed")
                        .setMessage("to read external storage")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(SignUpActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                Toast.makeText(SignUpActivity.this,getString(R.string.permission_not_granted_file), Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.create().show();
            }
            else{
                // directly request permission
                Toast.makeText(SignUpActivity.this,"Not required to Show Rationale",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(SignUpActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
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
                Toast.makeText(SignUpActivity.this,getString(R.string.permission_not_granted_file), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SignUpActivity.this,getString(R.string.did_not_choose_any_photo), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePicAndName(){
        // First we need to upload the image
        String strFileName = firebaseUser.getUid() + ".jpg";

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
                           databaseReference = FirebaseDatabase.getInstance("https://chatapplication-abf5b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root
                           // Hashmap is like schema for database
                           HashMap<String,String> hashMap = new HashMap<>();
                           hashMap.put(NodeNames.NAME,binding.etName.getText().toString().trim());
                           hashMap.put(NodeNames.EMAIL,binding.etEmail.getText().toString().trim());
                           hashMap.put(NodeNames.ONLINE,getResources().getString(R.string.online));
                           hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());

                           // pushing the data to the child node
                           databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task2 -> {
                               if(task2.isSuccessful()){
                                   // navigate to LoginScreen
                                   Toast.makeText(SignUpActivity.this,getString(R.string.signup_success),Toast.LENGTH_SHORT).show();
                               }
                               else{
                                   Toast.makeText(SignUpActivity.this,"Syncing of user profile with firebase error",Toast.LENGTH_SHORT).show();
                               }
                           });
                       }
                       else{
                           Toast.makeText(SignUpActivity.this,getString(R.string.user_updation_failed,task.getException()), Toast.LENGTH_LONG).show();
                           Log.i("SignUpActivity","Failed to Update profile using firebaseUser.updateProfile()");
                       }

                   });

               });

           }
           else{
               Toast.makeText(SignUpActivity.this, "Error in Syncing Profile picture", Toast.LENGTH_SHORT).show();
           }
        });

    }

    private void updateOnlyName(){
        // Updating User details in the realtime database
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(binding.etName.toString().trim())
                .build();
        firebaseUser.updateProfile(request).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               // Getting the UID of the user
               Toast.makeText(SignUpActivity.this, "Profile Updation Successful,FireBaseUser", Toast.LENGTH_SHORT).show();
               String userID = firebaseUser.getUid();
               // We need a databaseReference object to update the user details
               Log.i("SignUpActivity","Before initializing database reference ");
               // TODO : To get reference of database other than in US region , we need to pass database url to getInstance method
               databaseReference = FirebaseDatabase.getInstance("https://chatapplication-abf5b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(NodeNames.USERS); // getRefernce gives the refernce of the root
               Log.i("SignUpActivity","After initializing database reference ");

               // Hashmap is similar to dictionary in python
               HashMap<String,String> hashMap = new HashMap<>();
               hashMap.put(NodeNames.NAME,binding.etName.getText().toString().trim());
               hashMap.put(NodeNames.EMAIL,binding.etEmail.getText().toString().trim());
               hashMap.put(NodeNames.ONLINE,getResources().getString(R.string.online));
               hashMap.put(NodeNames.PHOTO,"");

               databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task1 -> {
                   if(task1.isSuccessful()){
                       Toast.makeText(SignUpActivity.this,getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                       startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                   }
                   else{
                       Toast.makeText(SignUpActivity.this, "Updation of Hashmap of userProfile failed : "+task1.getException(), Toast.LENGTH_SHORT).show();
                       Log.i("SignUpActivity","Updation of user profile failed : "+task1.getException());
                   }
               });
           }
           else{
               Toast.makeText(SignUpActivity.this,getString(R.string.user_updation_failed,task.getException()), Toast.LENGTH_LONG).show();
               Log.i("SignUpActivity","Failed to Update profile using firebaseUser.updateProfile()");
           }
        });
    }
    private void signUpBtnClicked(){
        email = binding.etEmail.getText().toString().trim();
        name = binding.etName.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();
        confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if(email.equals("")){
            binding.etEmail.setError(getString(R.string.enter_email));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.setError(getString(R.string.enter_correct_email));
        }
        else if(name.equals("")){
            binding.etName.setError(getString(R.string.enter_name));
        }
        else if(password.equals("")){
            binding.etName.setError(getString(R.string.enter_password));
        }
        else if(confirmPassword.equals("")){
            binding.etName.setError(getString(R.string.enter_confirm_password));
        }
        else if(!password.equals(confirmPassword)){
            binding.etConfirmPassword.setError(getString(R.string.passwords_not_matching));
        }
        // Registering with Firebase
        else{
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    // geting the firebase user
                    firebaseUser = firebaseAuth.getCurrentUser();
                    Toast.makeText(SignUpActivity.this,"Creating User in Authentication FireBase is done", Toast.LENGTH_LONG).show();
                    if(localFileUri!=null){
                        updatePicAndName();
                    }
                    else{
                        updateOnlyName();
                    }
                }
                else{
                    Toast.makeText(SignUpActivity.this,getString(R.string.signup_failed,task.getException()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}