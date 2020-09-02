package com.baker.engrave.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.baker.engrave.demo.activity.BaseActivity;
import com.baker.engrave.demo.fragment.ExperienceFragment;
import com.baker.engrave.demo.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private ExperienceFragment experienceFragment;
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        fragmentManager = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        experienceFragment = new ExperienceFragment();
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_home) {
                    fragment = homeFragment;
                } else if (itemId == R.id.action_experience) {
                    fragment = experienceFragment;
                }
                if (fragment != null) {
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }
}
