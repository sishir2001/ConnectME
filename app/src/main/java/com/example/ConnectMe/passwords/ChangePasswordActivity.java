package com.example.ConnectMe.passwords;

import static com.example.ConnectMe.common.PasswordRegex.acceptPassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ConnectMe.R;
import com.example.ConnectMe.databinding.ActivityChangePasswordBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding ;
    private String password,confirmPassword;
//    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        progressBar = findViewById(R.id.progressBar);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.buttonResetPassword.setOnClickListener(view1 -> clickedChangePassBtn());

        // ActionBar code
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.forgot_password));
            // for up button in this activity
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(1);

        }
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
    private void clickedChangePassBtn(){
        // verify the edittext
        password = binding.etPassword.getText().toString().trim();
        confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if(password.equals("")){
            binding.tilPassChPass.setError(getString(R.string.enter_password));
        }
        else if(!acceptPassword(password)){
            binding.tilPassChPass.setError(getString(R.string.pass_regex_error));
        }
        else if(confirmPassword.equals("")){
            binding.tilPassConPassChPass.setError(getString(R.string.enter_confirm_password));
        }
        else if(!password.equals(confirmPassword)){
            binding.tilPassConPassChPass.setError(getString(R.string.passwords_not_matching));
        }
        else{
//            progressBar.setVisibility(View.VISIBLE);
            // all the edit texts are properly filled
            // Building progress bar
            MaterialAlertDialogBuilder materialBuilder = new MaterialAlertDialogBuilder(this)
                    .setCustomTitle(getLayoutInflater().inflate(R.layout.custom_progressbar,null));
            AlertDialog alertDialog = materialBuilder.create();
            alertDialog.show();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null){
                firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
//                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this,getString(R.string.password_success_update),Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        finish();
                    }
                    else{
                        alertDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this,getString(R.string.something_went_wrong,task.getException()),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }
}