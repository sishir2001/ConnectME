package com.example.ConnectMe.passwords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ConnectMe.R;
import com.example.ConnectMe.databinding.ActivityResetPasswordBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private String email;
//    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        progressBar = findViewById(R.id.progressBar);
        binding  = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonResetPassword.setOnClickListener(view -> clickedPasswordResetBtn());

        // ActionBar code
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.change_password));
            actionBar.setElevation(1);
            // for up button in this activity
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

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

    private void clickedPasswordResetBtn(){
        email = binding.etPassword.getText().toString().trim();
        if(email.equals("")){
            binding.etPassword.setError(getString(R.string.enter_email));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etPassword.setError(getString(R.string.enter_correct_email));
        }
        else{
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            MaterialAlertDialogBuilder materialBuilder = new MaterialAlertDialogBuilder(this)
                    .setCustomTitle(getLayoutInflater().inflate(R.layout.custom_progress_reset_password,null));
            AlertDialog alertDialog = materialBuilder.create();
            alertDialog.show();
//            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
//                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    alertDialog.dismiss();
                    Toast.makeText(ResetPasswordActivity.this, "Reset Successful", Toast.LENGTH_SHORT).show();
                    binding.llResetPassword.setVisibility(View.GONE);
                    binding.llMessage.setVisibility(View.VISIBLE);
                    binding.textViewMessage.setText(getString(R.string.reset_password_intructions,email));
//                     adding timer to reset button
                    CountDownTimer countDownTimer = new CountDownTimer(60000,1000) {
                        @Override
                        public void onTick(long l) {
                            binding.buttonReset.setText(getString(R.string.reset_timer,String.valueOf(l/1000)));
                            binding.buttonReset.setOnClickListener(null);// clicking will not work
                        }

                        @Override
                        public void onFinish() {
                            binding.buttonReset.setText(getString(R.string.reset));
                            binding.buttonReset.setOnClickListener(view -> {
                                binding.llResetPassword.setVisibility(View.VISIBLE);
                                binding.llMessage.setVisibility(View.GONE);
                            });
                        }
                    };
                    countDownTimer.start();
                }
                else {
                    alertDialog.dismiss();
                    binding.llResetPassword.setVisibility(View.GONE);
                    binding.llMessage.setVisibility(View.VISIBLE);
                    Toast.makeText(ResetPasswordActivity.this, "Reset Unsuccessful", Toast.LENGTH_SHORT).show();
                    binding.textViewMessage.setText(getString(R.string.failed_to_send_email));
                    binding.buttonReset.setText(getString(R.string.reset));
                    binding.buttonReset.setOnClickListener(view -> {
                        binding.llResetPassword.setVisibility(View.VISIBLE);
                        binding.llMessage.setVisibility(View.GONE);
                    });
                }
                binding.buttonClose.setOnClickListener(view -> finish());
            });

        }
    }
}