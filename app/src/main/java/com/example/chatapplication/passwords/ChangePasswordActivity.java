package com.example.chatapplication.passwords;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding ;
    private String password,confirmPassword;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = findViewById(R.id.progressBar);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.buttonResetPassword.setOnClickListener(view1 -> clickedChangePassBtn());
    }
    private void clickedChangePassBtn(){
        // verify the edittext
        password = binding.etPassword.getText().toString().trim();
        confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if(password.equals("")){
            binding.etPassword.setError(getString(R.string.enter_password));
        }
        else if(confirmPassword.equals("")){
            binding.etConfirmPassword.setError(getString(R.string.enter_confirm_password));
        }
        else if(!password.equals(confirmPassword)){
            binding.etConfirmPassword.setError(getString(R.string.passwords_not_matching));
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            // all the edit texts are properly filled
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null){
                firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this,getString(R.string.password_success_update),Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        Toast.makeText(ChangePasswordActivity.this,getString(R.string.something_went_wrong,task.getException()),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }
}