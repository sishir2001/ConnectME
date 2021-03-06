package com.example.ConnectMe.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ConnectMe.passwords.ResetPasswordActivity;
import com.example.ConnectMe.MainActivity;
import com.example.ConnectMe.MessageActivity;
import com.example.ConnectMe.R;
import com.example.ConnectMe.common.Util;
import com.example.ConnectMe.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding ;
    private String email,password;
    private FirebaseAuth mAuth;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = findViewById(R.id.displayProgressBar);
        Log.i("LoginActivity","Inside onStart()");
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setTitle(getString(R.string.login));
        // initializing FireBase
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.displayProgressBar);

        // setting click listener on Login Button
        binding.buttonLogin.setOnClickListener(view1 -> clickedLoginBtn());
        binding.textViewSignUp.setOnClickListener(view1 -> {
            // intent to SignUp Activity
            startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
        });
        // click listener for forgotpassword
        binding.textViewForgotPassWord.setOnClickListener(view1 -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });
    }

    //    public void clickedLoginBtn(View view){
    private void clickedLoginBtn(){
        // getText is very important
        email = binding.etEmail.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();

        // conditions for checking whether email and pass are empty
        if(email.equals("")){
            binding.tilPasswordLogin.setErrorEnabled(false);
            binding.tilEmailLogin.setError(getString(R.string.enter_email));
            binding.tilEmailLogin.requestFocus();
        }
        else if(password.equals("")){
            binding.tilEmailLogin.setErrorEnabled(false);
            binding.tilPasswordLogin.setError(getString(R.string.enter_password));
            binding.tilPasswordLogin.requestFocus();
        }
        else{
            binding.tilEmailLogin.setErrorEnabled(false);
            binding.tilEmailLogin.setErrorEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            // password and email are not empty
            // Need to use firebase for authentication
            Log.i("LoginActivity","In Else, clickedLoginBtn()");
            // Checking connection before contacting the Firebase Authentication
            if(Util.connectionAvailable(this)){
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            // navigate to Chat Activity
//                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        else{
                            // Login Unsuccesful
                            Toast.makeText(LoginActivity.this, getString(R.string.login_failed,task.getException()), Toast.LENGTH_LONG).show();
                            binding.tilEmailLogin.setErrorEnabled(true);
                            binding.tilEmailLogin.setErrorEnabled(true);
                        }
                    }
                });
            }
            else{
                // Make the Progres bar gone
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        // exit out of the app
        finishAffinity();
        super.onBackPressed();
    }
}