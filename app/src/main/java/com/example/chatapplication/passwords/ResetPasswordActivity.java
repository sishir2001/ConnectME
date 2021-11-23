package com.example.chatapplication.passwords;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityResetPasswordBinding;
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

//            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
//                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    binding.llResetPassword.setVisibility(View.GONE);
                    binding.llResetPassword.setVisibility(View.VISIBLE);
                    binding.textViewMessage.setText(getString(R.string.reset_password_intructions,email));
                    // adding timer to reset button
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
                                binding.llResetPassword.setVisibility(View.GONE);
                            });
                        }
                    };
                    countDownTimer.start();
                }
                else {
                    binding.textViewMessage.setText(getString(R.string.failed_to_send_email));
                    binding.buttonReset.setText(getString(R.string.reset));
                    binding.buttonReset.setOnClickListener(view -> {
                        binding.llResetPassword.setVisibility(View.VISIBLE);
                        binding.llResetPassword.setVisibility(View.GONE);
                    });
                }
                binding.buttonClose.setOnClickListener(view -> finish());
            });

        }
    }
}