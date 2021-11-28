package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.FindFriends.FindfriendFragment;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.profile.ProfileActivity;
import com.google.android.material.tabs.TabLayout;

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
        // Just checking the progress bar
        new Handler().postDelayed(() -> {
            // lambda for runnable interface
            progressBar.setVisibility(View.GONE);
        },1000);

        setViewPager();

    }
    private void setViewPager(){
        // initializing the viewpager
        viewPagerAdapter pagerAdapter = new viewPagerAdapter(getSupportFragmentManager(),getLifecycle());
        binding.viewPagerMain.setAdapter(pagerAdapter);

        // adding custom tabs to tablayout
        binding.tabLayoutMain.addTab(binding.tabLayoutMain.newTab().setCustomView(R.layout.tab_chat));
        binding.tabLayoutMain.addTab(binding.tabLayoutMain.newTab().setCustomView(R.layout.tab_requests));
        binding.tabLayoutMain.addTab(binding.tabLayoutMain.newTab().setCustomView(R.layout.tab_find));
        binding.tabLayoutMain.setTabGravity(TabLayout.GRAVITY_FILL);

        binding.tabLayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
               binding.viewPagerMain.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // sliding the fragments
        binding.viewPagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayoutMain.selectTab(binding.tabLayoutMain.getTabAt(position));
            }
        });
    }

    // For overflow menu in the activity
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

    // for handling the back button
    private boolean backPressed = false;
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        // check for tab in which the user is present
        // if user present in chatTab , then ask him to click the back button two times,use handler interface to implement it
        if(binding.viewPagerMain.getCurrentItem() == 0){
            // need to click two times
            if(backPressed){
                // true
                finishAffinity();
            }
            else{
                Toast.makeText(this, "Press back button once again", Toast.LENGTH_SHORT).show();
                backPressed = true;
                // give the user only 3 sec to press the second back button
                // runs a new thread other than main thread after the delay
                new Handler().postDelayed(() -> backPressed = false,3000);
            }

        }
        else{
            binding.viewPagerMain.setCurrentItem(binding.viewPagerMain.getCurrentItem() -1);
        }
    }

    // creating a class adapter to manage ViewPager
    // inner class
    private class viewPagerAdapter extends FragmentStateAdapter{

        public viewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment res = new ChatFragment(); // default case
            switch(position){
                case 0:
                    res = new ChatFragment();
                    break;
                case 1:
                    res = new RequestsFragment();
                    break;
                case 2:
                    res = new FindfriendFragment();
                    break;
            }
            return res;
        }

        @Override
        public int getItemCount() {
            return binding.tabLayoutMain.getTabCount();
        }
    }

}
