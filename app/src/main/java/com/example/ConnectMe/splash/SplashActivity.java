package com.example.ConnectMe.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ConnectMe.R;
import com.example.ConnectMe.slideInfo.slideinfoActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;
    ImageView connectmetext;
    LottieAnimationView splashanimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        Thread background = new Thread() {
//            public void run() {
//                try {
//                    // Thread will sleep for 5 seconds
//                    sleep(5*1000);
//
//                    // After 5 seconds redirect to another intent
//                    Intent i=new Intent(getBaseContext(), slideinfoActivity.class);
//                    startActivity(i);
//
//                    //Remove activity
//                    finish();
//                } catch (Exception e) {
//                }
//            }
//        };


//        getSupportActionBar().hide();
        logo=findViewById(R.id.logo);
//        rectangle=findViewById(R.id.rectangle);
        connectmetext=findViewById(R.id.connectmetext);
        splashanimation=findViewById(R.id.splashanimation);

        // logo and text fade in and out
        AnimatorSet s=new AnimatorSet();
        ObjectAnimator fade_out_logo=ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f);
        fade_out_logo.setDuration(400).setStartDelay(1500);
        ObjectAnimator fade_out_text=ObjectAnimator.ofFloat(connectmetext, "alpha", 1f, 0f);
        fade_out_text.setDuration(400).setStartDelay(1500);
        s.playTogether(fade_out_logo,fade_out_text);
        s.start();
        //logo and text translate
        logo.animate().translationY(-900).setDuration(500);
        connectmetext.animate().translationY(-900).setDuration(500);

        splashanimation.animate().alpha(1).setDuration(2000).setStartDelay(2000);
        //animation fade in and out
        AnimatorSet S=new AnimatorSet();
        ObjectAnimator animation_fade_out=ObjectAnimator.ofFloat(splashanimation, "alpha", 1f, 0f);
        animation_fade_out.setStartDelay(5000);
        S.playSequentially(animation_fade_out);
        S.start();

        // start thread
//        background.start();
        // Helps when the app is used from stack !
        new Handler().postDelayed(() -> {
            Intent i=new Intent(getBaseContext(), slideinfoActivity.class);
            startActivity(i);

            //Remove activity
            finish();

        },5000);
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Checking whether user is already logged
//        Log.i("LoginActivity","Inside onStart()");
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null){
//            // User is already logged in
//            Toast.makeText(SplashActivity.this,"User is already loggedin "+firebaseUser.getEmail(),Toast.LENGTH_LONG).show();
//            startActivity(new Intent(SplashActivity.this,MainActivity.class));
//        }
//        else{
//            // else an user must login
//            Toast.makeText(SplashActivity.this,"User is not loggedin ",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(SplashActivity.this,slideinfoActivity.class));
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Checking whether user is already logged
//        Log.i("LoginActivity","Inside onStart()");
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null){
//            // User is already logged in
//            Toast.makeText(SplashActivity.this,"User is already loggedin "+firebaseUser.getEmail(),Toast.LENGTH_LONG).show();
//            startActivity(new Intent(SplashActivity.this,MainActivity.class));
//        }
//        else{
//            // else an user must login
//            Toast.makeText(SplashActivity.this,"User is not loggedin ",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(SplashActivity.this,slideinfoActivity.class));
//        }
//    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//
//    }
}