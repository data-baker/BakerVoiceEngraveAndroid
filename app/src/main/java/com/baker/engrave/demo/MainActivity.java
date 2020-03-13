package com.baker.engrave.demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baker.engrave.demo.activity.BaseActivity;
import com.baker.engrave.demo.base.Constants;
import com.baker.engrave.demo.fragment.ExperienceFragment;
import com.baker.engrave.demo.fragment.HomeFragment;
import com.baker.engrave.demo.permission.PermissionUtil;
import com.baker.engrave.demo.util.SharedPreferencesUtil;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private ExperienceFragment experienceFragment;
    private FragmentManager fragmentManager;
    private LinearLayout lytClientInfo;
    private EditText edtClientId, edtClientSecret;
    private Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionUtil.hasPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionUtil.needPermission(MainActivity.this, 89, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        }
        initView();
    }

    private void initView() {
        lytClientInfo = findViewById(R.id.lyt_client_info);
        lytClientInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        edtClientId = findViewById(R.id.edt_client_id);
        edtClientSecret = findViewById(R.id.edt_client_secret);
        btnSave = findViewById(R.id.save_client_info);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clientId = edtClientId.getText().toString().trim();
                String clientSecret = edtClientSecret.getText().toString().trim();
                if (TextUtils.isEmpty(clientId)) {
                    Toast.makeText(MainActivity.this, "ClientId is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(clientSecret)) {
                    Toast.makeText(MainActivity.this, "ClientSecret is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferencesUtil.saveClientId(clientId);
                SharedPreferencesUtil.saveClientSecret(clientSecret);
                //初始化复刻SDK
                BakerVoiceEngraver.getInstance().initSDK(MainActivity.this, clientId, clientSecret, SharedPreferencesUtil.getQueryId());
                lytClientInfo.setVisibility(View.GONE);
            }
        });

        if (TextUtils.isEmpty(SharedPreferencesUtil.getClientId())
                || TextUtils.isEmpty(SharedPreferencesUtil.getClientSecret())) {
            lytClientInfo.setVisibility(View.VISIBLE);
        } else {
            //初始化复刻SDK
            BakerVoiceEngraver.getInstance().initSDK(this, SharedPreferencesUtil.getClientId(),
                    SharedPreferencesUtil.getClientSecret(), SharedPreferencesUtil.getQueryId());
            lytClientInfo.setVisibility(View.GONE);
        }

        fragmentManager = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        experienceFragment = new ExperienceFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = homeFragment;
                        break;
                    case R.id.action_experience:
                        fragment = experienceFragment;
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }
}
