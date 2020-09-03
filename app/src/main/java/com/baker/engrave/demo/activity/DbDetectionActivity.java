package com.baker.engrave.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.engrave.demo.R;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.callback.DetectCallback;

/**
 * 检测噪音
 */
public class DbDetectionActivity extends BaseActivity implements DetectCallback {
    private String TAG = "DbDetectionActivity";
    private int BTN_TYPE = 0; //0=默认状态，检测未通过。1=检测通过，可以复刻。
    private Button btnStartEngrave;
    private TextView tvDetectValue, tvDetectTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_detection);

        showToolbar(R.string.string_step_1);

        initView();

        BakerVoiceEngraver.getInstance().setDetectCallback(this);
    }

    private void initView() {
        btnStartEngrave = findViewById(R.id.engrave_start);
        btnStartEngrave.setOnClickListener(this);
        tvDetectValue = findViewById(R.id.tv_db_value);
        tvDetectTip = findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.engrave_start:
                //0=默认状态，检测未通过。1=检测通过，可以复刻。
                if (BTN_TYPE == 0) {
                    //返回结果=1，开启检测成功。否则开启检测失败，最有可能的原因是没有麦克风、写SD卡权限。
                    int resultCode = BakerVoiceEngraver.getInstance().startDBDetection();
                    if (resultCode != 1) {
                        Log.d(TAG, "开启失败");
                    } else {
                        btnStartEngrave.setEnabled(false);
                        tvDetectTip.setText("环境噪音检测中，请稍候...");
                    }
                } else {
                    startActivity(new Intent(DbDetectionActivity.this, EngraveActivity.class));
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void dbDetecting(int value) {
        tvDetectValue.setText(value + " db");
    }

    @Override
    public void dbDetectionResult(boolean result, int value) {
        btnStartEngrave.setEnabled(true);
        tvDetectValue.setText(value + " db");
        if (value > 70) {
            tvDetectTip.setText("环境噪音检测不通过，请换安静环境再试哦。");
        } else if (value > 50) {
            tvDetectTip.setText("环境很一般，在更安静的环境下效果更佳哦");
        } else {
            tvDetectTip.setText("环境很安静，开始去复刻吧");
        }
        if (result) {
            BTN_TYPE = 1;
            btnStartEngrave.setText("开始复刻");
        } else {
            BTN_TYPE = 0;
            btnStartEngrave.setText("重新检测");
        }
    }

    @Override
    public void onDetectError(final int errorCode, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorCode == 90013) {
                    //因音频焦点丢失或电话等异常中断检测
                    btnStartEngrave.setEnabled(true);
                    tvDetectValue.setText("0 db");
                    BTN_TYPE = 0;
                    tvDetectTip.setText("检测中断啦，请再试一次吧");
                    btnStartEngrave.setText("重新检测");
                } else {
                    Toast.makeText(DbDetectionActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBack() {
        super.onBack();
    }
}
