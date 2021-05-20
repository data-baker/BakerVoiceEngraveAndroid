package com.baker.engrave.demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baker.engrave.demo.activity.BaseActivity;
import com.baker.engrave.demo.activity.EngraveActivity;
import com.baker.engrave.demo.base.Constants;
import com.baker.engrave.demo.fragment.ExperienceFragment;
import com.baker.engrave.demo.fragment.HomeFragment;
import com.baker.engrave.demo.permission.PermissionUtil;
import com.baker.engrave.demo.util.SharedPreferencesUtil;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.callback.InitListener;
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
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
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

                showProgressDialog();

                //初始化复刻SDK
                BakerVoiceEngraver.getInstance().initSDK(MainActivity.this, clientId, clientSecret, SharedPreferencesUtil.getQueryId(), new InitListener() {
                    @Override
                    public void onInitSuccess() {
                        SharedPreferencesUtil.saveClientId(clientId);
                        SharedPreferencesUtil.saveClientSecret(clientSecret);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lytClientInfo.setVisibility(View.GONE);
                                bottomNavigationView.setVisibility(View.VISIBLE);

                                disMissProgressDialog();
                            }
                        });
                    }

                    @Override
                    public void onInitError(Exception e) {
                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                        Log.e("hsj", e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disMissProgressDialog();
                            }
                        });
                    }
                });
            }
        });

        if (TextUtils.isEmpty(SharedPreferencesUtil.getClientId())
                || TextUtils.isEmpty(SharedPreferencesUtil.getClientSecret())) {
            lytClientInfo.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            //初始化复刻SDK
            BakerVoiceEngraver.getInstance().initSDK(this, SharedPreferencesUtil.getClientId(),
                    SharedPreferencesUtil.getClientSecret(), SharedPreferencesUtil.getQueryId(), new InitListener() {
                        @Override
                        public void onInitSuccess() {
                            lytClientInfo.setVisibility(View.GONE);
                            bottomNavigationView.setVisibility(View.VISIBLE);

                            log("初始化成功");
                        }

                        @Override
                        public void onInitError(Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        fragmentManager = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        experienceFragment = new ExperienceFragment();


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

    private void log(String msg) {
        Log.d(this.getClass().getName(), msg);
    }
}
