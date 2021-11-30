package com.example.ConnectMe.slideInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.ConnectMe.Authentication.LoginActivity;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentFive;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentFour;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentOne;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentSix;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentThree;
import com.example.ConnectMe.slideInfo.fragments.SlideFragmentTwo;
import com.example.ConnectMe.MainActivity;
import com.example.ConnectMe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class slideinfoActivity extends AppCompatActivity {

    Fragment[] slideFragments = new Fragment[]{
            new SlideFragmentOne(),
            new SlideFragmentTwo(),
            new SlideFragmentThree(),
            new SlideFragmentFour(),
            new SlideFragmentFive(),
            new SlideFragmentSix()
    };
    private static final int NUM_PAGES = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideinfo);

        // * instantiating viewPager and pageAdapter
        ViewPager2 viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new SlidePageAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // button
        Button buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(view1 -> {
            // navigate to LoginActivity
            Intent intent = new Intent(slideinfoActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    private class SlidePageAdapter extends FragmentStateAdapter{
        public SlidePageAdapter(FragmentActivity fa){
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return slideFragments[position];
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Checking whether user is already logged
        Log.i("slideInfoActivity","Inside onStart()");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            // User is already logged in
            Toast.makeText(slideinfoActivity.this,"User is already loggedin "+firebaseUser.getEmail(),Toast.LENGTH_LONG).show();
            startActivity(new Intent(slideinfoActivity.this, MainActivity.class));
        }
        else{
            Toast.makeText(slideinfoActivity.this,"User is not loggedin ",Toast.LENGTH_LONG).show();
        }
        // else an user must login
    }
}