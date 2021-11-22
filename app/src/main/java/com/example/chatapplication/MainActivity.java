package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding ;
    private String email,password;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity","Inside onStart()");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // initializing FireBase
        mAuth = FirebaseAuth.getInstance();

        // setting click listener on Login Button
        binding.buttonLogin.setOnClickListener(view1 -> clickedLoginBtn());
        binding.textViewForgotPassWord.setOnClickListener(view1 -> {
            Toast.makeText(MainActivity.this, "Forgot Password", Toast.LENGTH_SHORT).show();
        });
        binding.textViewSignUp.setOnClickListener(view1 -> {
            // intent to SignUp Activity
            startActivity(new Intent(MainActivity.this,SignUpActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Checking whether user is already logged
        Log.i("MainActivity","Inside onStart()");
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser != null){
            // User is already logged in
            Toast.makeText(MainActivity.this,"User is already loggedin "+firebaseUser.getEmail(),Toast.LENGTH_LONG).show();


        }
        else{
            Toast.makeText(MainActivity.this,"User is not loggedin ",Toast.LENGTH_LONG).show();
        }
        // else an user must login
    }

    //    public void clickedLoginBtn(View view){
    private void clickedLoginBtn(){
        // getText is very important
        email = binding.etEmail.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();

        // conditions for checking whether email and pass are empty
        if(email.equals("")){
            binding.etEmail.setError(getString(R.string.enter_email));
        }
        else if(password.equals("")){
            binding.etPassword.setError(getString(R.string.enter_password));
        }
        else{
            // password and email are not empty
            // Need to use firebase for authentication
            Log.i("MainActivity","In Else, clickedLoginBtn()");
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        // navigate to Chat Activity
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        // Login Unsuccesful
                        Toast.makeText(MainActivity.this, getString(R.string.login_failed,task.getException()), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}