package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.profile.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    // TODO : Profile Picture not able take from External Storage (ProfileActivity , SignUpActivity)
    // TODO : ProgressBar Buggy in ProfileActivity SignUpActivity

    private ActivityMainBinding binding;
    private View progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // setting the color of default actionbar to colorSecondaryDark
        // Set back to screen

        // just showing the progress bar for a second
        progressBar = findViewById(R.id.layoutProgressbar);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            // lambda for runnable interface
            progressBar.setVisibility(View.GONE);
        },1000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_overflow,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuProfile){
            // clicked on the menu profile
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}