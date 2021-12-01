package com.example.ConnectMe.Messages;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ConnectMe.common.Constants;
import com.example.ConnectMe.common.Extras;
import com.example.ConnectMe.common.NodeNames;
import com.example.ConnectMe.R;
import com.example.ConnectMe.common.Util;
import com.example.ConnectMe.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    // this is the activity where the conversational screen appears
    private ActivityChatBinding binding;
    private String chatUserId,currentUserId;
    private DatabaseReference rootDatabaseReference;
    private FirebaseAuth firebaseAuth;


    // Displaying chats
    private MessageAdapter messageAdapter;
    private List<MessagesModel> messagesModelList;
    private int currentPage = 1;
    private static final int PAGE_LIMIT = 30;
    private ChildEventListener childEventListener;
    private DatabaseReference messagesDatabaseReferences;


    // for actionBar
    private TextView tvActionBarUserName;
    private CircleImageView civProfileImage;

    // For attachments
    private BottomSheetDialog bottomSheetDialog;
    private final static int REQ_IMG_CAPTURE = 101;
    private final static int REQ_GAL = 102;
    private final static int REQ_VID = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar code
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.empty_string));
            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_actionbar_layout,null);
            // for up button in this activity
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(1);

            // setting up the custom action bar
            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);

        }

        // how im able to know the id's -> because im in an activity
        civProfileImage = findViewById(R.id.ivProfilePicture);
        tvActionBarUserName= findViewById(R.id.tvUserName);

        // Checking for extras in intent
        if(getIntent().hasExtra(Extras.USER_KEY)){
            chatUserId = getIntent().getStringExtra(Extras.USER_KEY);
        }

        if(getIntent().hasExtra(Extras.USER_NAME)){
            tvActionBarUserName.setText(getIntent().getStringExtra(Extras.USER_NAME));
        }

        if(getIntent().hasExtra(Extras.USER_PHOTO_NAME)){
            Uri imgUri = Uri.parse(getIntent().getStringExtra(Extras.USER_PHOTO_NAME));
            Glide.with(ChatActivity.this)
                    .load(imgUri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile1)
                    .into(civProfileImage);
        }
        rootDatabaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_LINK).getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        // Setting click Lisnteners to attach and send button
        binding.ivSend.setOnClickListener(this);
        binding.ivAttachment.setOnClickListener(this);

        // For fetching the chats
        messagesModelList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messagesModelList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(messageAdapter);
        messagesDatabaseReferences = rootDatabaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);// Directly to the messages

        loadMessages();
        binding.rvMessages.scrollToPosition(messagesModelList.size()-1);

        binding.srlMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // this method will be called when the user swipes the page
                currentPage++; // loading the messages according to the pages
//                binding.srlMessages.setRefreshing(true);
                Toast.makeText(ChatActivity.this, "currentPage :"+currentPage, Toast.LENGTH_SHORT).show();
                loadMessages();
            }
        });


        // For attachments
        bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.message_attachment_layout,null);// converting xml to view
        // Setting clicklisteners on the bottonDialog Buttons
        view.findViewById(R.id.llCamera).setOnClickListener(this);
        view.findViewById(R.id.llGallery).setOnClickListener(this);
        view.findViewById(R.id.llVideo).setOnClickListener(this);
        view.findViewById(R.id.ivClose).setOnClickListener(this);
        bottomSheetDialog.setContentView(view); // attaching a view to bottomSheetDialog

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

    private void loadMessages(){
        // load the adapter with the messages , at once only display 30 chats
        // Clearing the list of messages whenever this function is called
        Query query = messagesDatabaseReferences.limitToLast(PAGE_LIMIT*currentPage); // as we move up current Page value increases
        messagesModelList.clear();
        if(childEventListener!=null){
            query.removeEventListener(childEventListener);
        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessagesModel messagesModel = snapshot.getValue(MessagesModel.class); // automatically deserializes the data to that class object
                messagesModelList.add(messagesModel);
                messageAdapter.notifyDataSetChanged();
                binding.rvMessages.scrollToPosition(messagesModelList.size()-1);// scroll to the latest message
                binding.srlMessages.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // When the child is edited

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // when the message is deleted
                loadMessages();// calling the same function again to clearing the list and fetching the list altogether

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // when the message could not be fetched
                Toast.makeText(ChatActivity.this,"Message Fetching Error : "+error.getMessage(),Toast.LENGTH_SHORT).show();
                binding.srlMessages.setRefreshing(false);
            }
        };
        query.addChildEventListener(childEventListener);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.ivSend:
                // check for internet connection
                if(Util.connectionAvailable(this)){
                    String msg = binding.etMessage.getText().toString().trim();
                    // clearing the edit text
                    binding.etMessage.setText(getString(R.string.empty_string));
                    // how to generate push id
                    DatabaseReference sendMessagePush = rootDatabaseReference.child(currentUserId).child(chatUserId).push();
                    String pushId = sendMessagePush.getKey();
                    SendMessage(msg,Constants.MSG_TYPE_TEXT,pushId);
                }
                else{
                    Toast.makeText(ChatActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ivAttachment:
                // check for permissions from  the OS
                // For camera capture, Gallery,Video -> External File storage is required
                if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    // need to show the bottom navigation
                    if(bottomSheetDialog!=null){
                        bottomSheetDialog.show();
                    }
                }
                else{
                   // Check to whether we need to rationale
                   if(ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                       // Should show request Rationale
                       // show a AlertDialogue box
                       Toast.makeText(ChatActivity.this, "Should Show Rationale", Toast.LENGTH_SHORT).show();
                       new MaterialAlertDialogBuilder(this)
                               .setTitle("Permission Needed")
                               .setMessage("Need to read external storage")
                               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {
                                       // request the user for permission
                                       ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                                   }
                               })
                               .create()
                               .show();
                    }
                   else {
                       // not required to show the dialogue box
                       Toast.makeText(ChatActivity.this, "Should not Show Rationale", Toast.LENGTH_SHORT).show();
                       ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                   }
                }
                // close the keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager != null){
                    inputMethodManager.hideSoftInputFromInputMethod(view.getWindowToken(),0);
                }
                break;

            case R.id.llCamera:
                // Start an intent for capturing the photo
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQ_IMG_CAPTURE);
                break;

            case R.id.llGallery:
                // Start an intent for capturing the photo
                bottomSheetDialog.dismiss();
                Intent imgIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// for images from gallery
                startActivityForResult(imgIntent,REQ_GAL);
                break;

            case R.id.llVideo:
                // Start an intent for capturing the photo
                bottomSheetDialog.dismiss();
                Intent vidIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);// for videos from gallery
                startActivityForResult(vidIntent,REQ_VID);
                break;

            case R.id.ivClose: // clicked the close button
                bottomSheetDialog.dismiss();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check for result code
        if(resultCode == RESULT_OK){
            // we can access data
            // check for request code
            if(requestCode == REQ_IMG_CAPTURE){
                // store the image in bitmap
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                // sending this picture to Firebase Storage using a particular message id , type : Image,Message = ? ,
                // Name of image in Starage = pushId + jpg
                // convert the Bitmap in array bytes to upload to storage ref
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes); // conversion of bitmap into ByteArrayOutputStream
                uploadByteArray(bytes,Constants.MSG_TYPE_IMG);
            }
            else if(requestCode == REQ_GAL){
                // get uri from local files
                Uri uriImg = data.getData();
                // use putFile method of Firebase using the localFile uri
                uploadFile(uriImg,Constants.MSG_TYPE_IMG);

            }
            else if(requestCode == REQ_VID){
               // similar to images
               Uri uriVid = data.getData();
               uploadFile(uriVid,Constants.MSG_TYPE_VID);
            }
        }
        else{
            Toast.makeText(ChatActivity.this, "Did not access any data", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadByteArray(ByteArrayOutputStream bytes,String msgType){
        // Start The progress bar
        // keeping videos and photos in seperate folder
        StorageReference rootStorageReference = FirebaseStorage.getInstance().getReference();// to the root of the cloud
        String pushId = messagesDatabaseReferences.getKey();// gives a unique id
        String folderName = (msgType.equals(Constants.MSG_TYPE_IMG)) ? Constants.MESSAGE_IMAGES : Constants.MESSAGE_VIDEOS;
        String fileName = (msgType.equals(Constants.MSG_TYPE_IMG)) ? pushId + ".jpg" : pushId + ".mp4";

        StorageReference fileRef = rootStorageReference.child(folderName).child(fileName);
        UploadTask task = fileRef.putBytes(bytes.toByteArray());
        // next monitor the progress of the file uploading
        uploadProgress(task,pushId,fileRef,msgType);
    }

    public void downloadFile(String messageId,String messageType,Boolean isShare){
        // Check permission for external Storage write
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
           // check for rationale
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                // show rationale
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Permission Needed")
                        .setMessage("Need to write to external storage")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // request the user for permission
                                ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
        }
        else{
            // We can write data to external file
            // We know the message through messageId
            // get uri
            StorageReference rootStorageReference = FirebaseStorage.getInstance().getReference();
            String folderName = (messageType.equals(Constants.MSG_TYPE_IMG))?Constants.MESSAGE_IMAGES:Constants.MESSAGE_VIDEOS;
            String fileName = (messageType.equals(Constants.MSG_TYPE_IMG))?messageId+".jpg":messageId+".mp4";
            StorageReference fileRef = rootStorageReference.child(folderName).child(fileName);

            String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" +fileName;
            File localFile= new File(localFilePath);
            try {
                if(localFile.exists() || localFile.createNewFile()){
                    FileDownloadTask downloadTask = fileRef.getFile(localFile);
                    downloadProgress(downloadTask,localFilePath,messageType,isShare);
                }

            }
            catch (Exception ex){
                Toast.makeText(ChatActivity.this,getString(R.string.failed_to_download_file,ex.getMessage()), Toast.LENGTH_SHORT).show();// Debug

            }


        }
    }
    private void uploadFile(Uri uri,String msgType){
        // keeping videos and photos in seperate folder
        StorageReference rootStorageReference = FirebaseStorage.getInstance().getReference();// to the root of the cloud
        String pushId = messagesDatabaseReferences.getKey();// gives a unique id
        String folderName = (msgType.equals(Constants.MSG_TYPE_IMG)) ? Constants.MESSAGE_IMAGES : Constants.MESSAGE_VIDEOS;
        String fileName = (msgType.equals(Constants.MSG_TYPE_IMG)) ? pushId + ".jpg" : pushId + ".mp4";

        StorageReference fileRef = rootStorageReference.child(folderName).child(fileName);
        UploadTask task =  fileRef.putFile(uri);
        // next monitor the progress of the file uploading
        uploadProgress(task,pushId,fileRef,msgType);
    }
    private void downloadProgress(FileDownloadTask downloadTask,String localFilePath,String messageType,Boolean isShare){
        /*
         * @param downloadTask - To know the progress of the download
         * @param localFilePath - where to download the file
         * messageType - to identify the type of message while displaying the chats
         */
        // Creating a view out of upload xml
        View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.progress_file_layout,null);
        // get views
        ProgressBar pbProfress = view.findViewById(R.id.pbProgress);
        TextView tvFileProgress = view.findViewById(R.id.tvFileProgress);
        ImageView ivPlay = view.findViewById(R.id.ivPlay);
        ImageView ivPause = view.findViewById(R.id.ivPause);
        ImageView ivCancel = view.findViewById(R.id.ivCancel);

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivPause.setVisibility(View.GONE);
                ivPlay.setVisibility(View.VISIBLE);
                downloadTask.pause();
            }
        });
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                downloadTask.resume();
            }
        });
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ivPlay.setVisibility(View.GONE);
//                ivPause.setVisibility(View.VISIBLE);
                // Confirmation with alertBox
                new MaterialAlertDialogBuilder(ChatActivity.this)
                        .setTitle(getString(R.string.cancel_downloading_file))
                        .setMessage(getString(R.string.are_you_sure_cancel_file_download))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                downloadTask.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // dont want to cancel the file upload
                                dialogInterface.cancel();
                            }
                        }).create().show();
            }
        });

        // Setting the progress Bar

        // Adding this View to the progress Linear layout
        binding.llProgress.addView(view);
        tvFileProgress.setText(getString(R.string.downloading_progress,messageType,"0"));


        // adding callbacks to tasks
        downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred())/ snapshot.getTotalByteCount();
                pbProfress.setProgress((int)progress);
                tvFileProgress.setText(getString(R.string.downloading_progress,messageType,String.valueOf(pbProfress.getProgress())));
            }
        });
        downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                //close the progress layout
                binding.llProgress.removeView(view);
                if(task.isSuccessful()){
                    // show snackbar to show the file
                    Toast.makeText(ChatActivity.this,"Download Successful", Toast.LENGTH_SHORT).show();// Debug
                    if(isShare){
                        // open intent to share files
                        Intent shareFileIntent = new Intent();
                        shareFileIntent.setAction(Intent.ACTION_SEND);
                        shareFileIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(localFilePath));
                        if(messageType.equals(Constants.MSG_TYPE_VID)){
                            shareFileIntent.setType("video/mp4");
                        }
                        else if(messageType.equals(Constants.MSG_TYPE_IMG)){
                            shareFileIntent.setType("image/jpg");
                        }
                        startActivity(Intent.createChooser(shareFileIntent,getString(R.string.intent_share_with)));
                    }
                    else{
                        Snackbar snackbar = Snackbar.make(binding.llProgress, R.string.file_downloaded_successfully,Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.view, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // creating an intent
                                Uri uri = Uri.parse(localFilePath);
                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                if(messageType.equals(Constants.MSG_TYPE_IMG)){
                                    intent.setDataAndType(uri,"image/jpg") ;
                                }
                                else if(messageType.equals(Constants.MSG_TYPE_VID)){
                                    intent.setDataAndType(uri,"video/mp4") ;
                                }
                                startActivity(intent);
                            }
                        }).show();
                    }
                }
            }
        });
        downloadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.llProgress.removeView(view);
                Toast.makeText(ChatActivity.this,getString(R.string.failed_to_download_file,e.getMessage()), Toast.LENGTH_SHORT).show();// Debug
            }
        });
        downloadTask.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(ChatActivity.this,"Cancelled Downloading the file", Toast.LENGTH_SHORT).show();// Debug
            }
        });

    }
    private void uploadProgress(UploadTask task,String pushId,StorageReference fileRef,String messageType){
        /*
            * @param task - To know the progress of the upload
            * @param pushId - useful for sending message to the user
            * fileRef - to get a downloadable uri to set in message key of message hashmap
            * messageType - to identify the type of message while displaying the chats
        */
        // Creating a view out of upload xml
        View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.progress_file_layout,null);
        // get views
        ProgressBar pbProfress = view.findViewById(R.id.pbProgress);
        TextView tvFileProgress = view.findViewById(R.id.tvFileProgress);
        ImageView ivPlay = view.findViewById(R.id.ivPlay);
        ImageView ivPause = view.findViewById(R.id.ivPause);
        ImageView ivCancel = view.findViewById(R.id.ivCancel);

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivPause.setVisibility(View.GONE);
                ivPlay.setVisibility(View.VISIBLE);
                task.pause();
            }
        });
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                task.resume();
            }
        });
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ivPlay.setVisibility(View.GONE);
//                ivPause.setVisibility(View.VISIBLE);
                // Confirmation with alertBox
                new MaterialAlertDialogBuilder(ChatActivity.this)
                        .setTitle(getString(R.string.cancel_uploading_file))
                        .setMessage(getString(R.string.are_you_sure_cancel_file_upload))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                               task.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // dont want to cancel the file upload
                                dialogInterface.cancel();
                            }
                        }).create().show();
            }
        });

        // Setting the progress Bar



        // Adding this View to the progress Linear layout
        binding.llProgress.addView(view);
        tvFileProgress.setText(getString(R.string.uploading_progress,messageType,"0"));


        // adding callbacks to tasks
        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred())/ snapshot.getTotalByteCount();
                pbProfress.setProgress((int)progress);
                tvFileProgress.setText(getString(R.string.uploading_progress,messageType,String.valueOf(pbProfress.getProgress())));
            }
        });
        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //close the progress layout
                binding.llProgress.removeView(view);
                if(task.isSuccessful()){
                    // upload to the message
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri = uri.toString();
                            SendMessage(downloadUri,messageType,pushId);
                        }
                    });
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.llProgress.removeView(view);
                Toast.makeText(ChatActivity.this,getString(R.string.failed_to_upload,e.getMessage()), Toast.LENGTH_SHORT).show();// Debug
            }
        });
        task.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(ChatActivity.this,"Cancelled Uploading the file", Toast.LENGTH_SHORT).show();// Debug
            }
        });
    }

    // for all then permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // checking whether permission is given or not
        Toast.makeText(ChatActivity.this, "Inside onRequestPermissionResult Callback", Toast.LENGTH_SHORT).show();// Debug

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(requestCode == 1){
                // Show the bottom dialog now
                Toast.makeText(ChatActivity.this, "Inside onRequestPermissionResult Callback,request matched", Toast.LENGTH_SHORT).show(); // Debug
                if(bottomSheetDialog != null){
                    Toast.makeText(ChatActivity.this, "Showing bottomDialog", Toast.LENGTH_SHORT).show(); // Debug
                    bottomSheetDialog.show();
                }
            }
            else if(requestCode == 2){
                Toast.makeText(ChatActivity.this, "Please Click the download button again", Toast.LENGTH_SHORT).show(); // Debug
            }
        }
        else{
            Toast.makeText(ChatActivity.this, "Permission Denied to access external storage", Toast.LENGTH_SHORT).show();
        }
    }

    private void SendMessage(String msg, String msgType, String pushId){
        // pushId should be same in both the users messages nodes

        // just need to update the nodes
        // messages -> CurrentUserId -> ChatUserId -> PushId -> HashMap(msgId/pushId,msg,senderId/currentUserId,sentTime,msgType)
        try {
            // checking if the message is not null
            if(!msg.equals(getString(R.string.empty_string))){
                // HashMap for updating after the message id
                HashMap messageMap = new HashMap();
                messageMap.put(NodeNames.MSG_ID,pushId);
                messageMap.put(NodeNames.MSG,msg);
                messageMap.put(NodeNames.MSG_FROM,currentUserId);
                messageMap.put(NodeNames.MSG_TIME, ServerValue.TIMESTAMP);
                messageMap.put(NodeNames.MSG_TYPE, msgType);

                String currentUserReference = NodeNames.MESSAGES + "/" + currentUserId +"/"+chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId ;

                HashMap messageUsermap = new HashMap();
                messageUsermap.put(currentUserReference + "/" + pushId,messageMap);
                messageUsermap.put(chatUserRef + "/" + pushId,messageMap);

                rootDatabaseReference.updateChildren(messageUsermap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                       if(error!=null){
                           // there was some error
                           Toast.makeText(ChatActivity.this,getString(R.string.msg_send_error,error.getMessage()), Toast.LENGTH_SHORT).show();
                       }
                       else{
                           Toast.makeText(ChatActivity.this,getString(R.string.message_sent_success), Toast.LENGTH_SHORT).show();

                           String notificationTitle = "";
                           if(msgType.equals(Constants.MSG_TYPE_TEXT)){
                               notificationTitle = "New Message";
                           }
                           else if(msgType.equals(Constants.MSG_TYPE_IMG)){
                               notificationTitle = "New Image";
                           }
                           else if(msgType.equals(Constants.MSG_TYPE_VID)){
                               notificationTitle = "New Video";
                           }
                           String notificationMessage = msg;
                           Util.sendNotification(ChatActivity.this,notificationTitle,notificationMessage,chatUserId);
                       }
                    }
                });
            }
        }
        catch (Exception e){
            Toast.makeText(ChatActivity.this,getString(R.string.msg_send_error,e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
    public void deleteMessage(String messageId,String messageType){
        // we are declaring this function in ChatActivity instead of MessengerAdapter because -> currentUserId,ChatUserId
        if(currentUserId != null && chatUserId != null){
            DatabaseReference currentUserDatabaseRef = rootDatabaseReference.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).child(messageId);
            DatabaseReference chatUserDatabaseRef = rootDatabaseReference.child(NodeNames.MESSAGES).child(chatUserId).child(currentUserId).child(messageId);

            currentUserDatabaseRef.removeValue()
                    .addOnCompleteListener(task -> {
                       if(task.isSuccessful()){
                           // delete the same message in chatUser
                           chatUserDatabaseRef.removeValue()
                                   .addOnCompleteListener(task1 -> {
                                      if(task1.isSuccessful()){
                                          if(!messageType.equals(Constants.MSG_TYPE_TEXT)){
                                              // img or vid
                                              StorageReference rootStorageRef = FirebaseStorage.getInstance().getReference();
                                              String folderName = (messageType.equals(Constants.MSG_TYPE_IMG))?Constants.MESSAGE_IMAGES:Constants.MESSAGE_VIDEOS;
                                              String fileName = (messageType.equals(Constants.MSG_TYPE_IMG))?messageId+".jpg":messageId+".mp4";
                                              StorageReference fileRef = rootStorageRef.child(folderName).child(fileName);
                                              fileRef.delete().addOnCompleteListener(task2 -> {
                                                  if(task2.isSuccessful()){
                                                      Toast.makeText(ChatActivity.this,getString(R.string.message_deleted_successfully), Toast.LENGTH_SHORT).show();
                                                  }
                                                  else{
                                                      Toast.makeText(ChatActivity.this,getString(R.string.error_deletion_cloud,task2.getException()), Toast.LENGTH_SHORT).show();
                                                  }
                                              });
                                          }
                                          else{
                                              // text
                                              Toast.makeText(ChatActivity.this,getString(R.string.message_deleted_successfully), Toast.LENGTH_SHORT).show();
                                          }
                                      }
                                      else{
                                          Toast.makeText(ChatActivity.this,getString(R.string.message_deletion_error,task1.getException()), Toast.LENGTH_SHORT).show();
                                      }
                                   });

                       }
                       else{
                           Toast.makeText(ChatActivity.this,getString(R.string.message_deletion_error,task.getException()), Toast.LENGTH_SHORT).show();
                       }
                    });
        }
        else{
            Toast.makeText(ChatActivity.this, "currentUserId,chatUserId not available,ChatActivity->deleteMessages", Toast.LENGTH_SHORT).show();

        }
    }
}