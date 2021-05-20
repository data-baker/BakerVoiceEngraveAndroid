package com.baker.engrave.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baker.engrave.demo.R;
import com.baker.engrave.demo.activity.ExperienceActivity;
import com.baker.engrave.demo.adapter.MouldRecyclerViewAdapter;
import com.baker.engrave.demo.util.RecycleGridDivider;
import com.baker.engrave.demo.util.SharedPreferencesUtil;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.bean.Mould;
import com.baker.engrave.lib.callback.MouldCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 试听体验
 * Create by hsj55
 * 2020/3/3
 */
public class ExperienceFragment extends BaseFragment implements MouldRecyclerViewAdapter.RecyclerViewItemOnClickListener {
    private RecyclerView recyclerView;
    private TextView tvNullTip;
    private MouldRecyclerViewAdapter adapter;
    private List<Mould> mouldList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_experience, null);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvNullTip = view.findViewById(R.id.tv_experience_null);
        tvNullTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BakerVoiceEngraver.getInstance().getMouldList(1, 50, SharedPreferencesUtil.getQueryId());
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BakerVoiceEngraver.getInstance().setMouldCallback(mouldCallback);

        adapter = new MouldRecyclerViewAdapter(mouldList, getActivity(), this);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.addItemDecoration(new RecycleGridDivider(20));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        BakerVoiceEngraver.getInstance().getMouldList(1, 50, SharedPreferencesUtil.getQueryId());
    }

    private final MouldCallback mouldCallback = new MouldCallback() {
        @Override
        public void onMouldError(int errorCode, String message) {
            Log.e("ExperienceFragment", "errorCode==" + errorCode + ", message=" + message);
            try {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvNullTip.setVisibility(View.VISIBLE);
                        tvNullTip.setText("网络请求出错啦\n请点我刷新重试");
                        recyclerView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 这个方法可自行决定是否重写
         * @param mould
         */
        @Override
        public void mouldInfo(Mould mould) {
            if (mould != null) {
//                HLogger.e("status=" + mould.getModelStatus() + ", statusName=" + mould.getStatusName());
            }
        }

        /**
         * 这个方法可自行决定是否重写
         * @param list
         */
        @Override
        public void mouldList(final List<Mould> list) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (list != null && list.size() > 0) {
                        mouldList.clear();
                        mouldList.addAll(list);
                        adapter.notifyDataSetChanged();
                        tvNullTip.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        tvNullTip.setVisibility(View.VISIBLE);
                        tvNullTip.setText(getResources().getString(R.string.string_listen_experience_null));
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    @Override
    public void onItemClick(int index, String mouldId) {
        Intent intent = new Intent(getActivity(), ExperienceActivity.class);
        intent.putExtra("index", index);
        intent.putExtra("mouldId", mouldId);
        startActivity(intent);
    }
}
