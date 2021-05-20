package com.baker.engrave.demo.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baker.engrave.demo.R;
import com.baker.engrave.demo.activity.DbDetectionActivity;
import com.baker.engrave.demo.activity.EngraveActivity;
import com.baker.engrave.demo.permission.PermissionUtil;

/**
 * Create by hsj55
 * 2020/3/3
 */
public class HomeFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, null);

        view.findViewById(R.id.experience_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !PermissionUtil.hasPermission(getActivity(), Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionUtil.needPermission(getActivity(), 89, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    );
                } else {
//                    if (BuildConfig.DEBUG) {
//                        startActivity(new Intent(getActivity(), EngraveActivity.class));
//                    } else {
                        startActivity(new Intent(getActivity(), DbDetectionActivity.class));
//                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}