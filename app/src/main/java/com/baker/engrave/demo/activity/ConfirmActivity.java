package com.baker.engrave.demo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.engrave.demo.R;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.callback.UploadRecordsCallback;

public class ConfirmActivity extends BaseActivity implements UploadRecordsCallback {
    private TextView tvTips, tvResult;
    private Button btnFinishEngrave;
    private String mVoiceMouldId;
    private boolean canBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        showToolbar(R.string.string_step_3);

        mVoiceMouldId = getIntent().getStringExtra("mouldId");
        initView();
        BakerVoiceEngraver.getInstance().setUploadRecordsCallback(this);
    }

    private void initView() {
        btnFinishEngrave = findViewById(R.id.engrave_finish);
        btnFinishEngrave.setOnClickListener(this);
        tvTips = findViewById(R.id.tv_finish_record);
        tvResult = findViewById(R.id.tv_result);
        tranStrColor(getString(R.string.string_finish_record_tip));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.engrave_finish:
                if (canBack) {
                    finish();
                } else {
                    AlertDialog.Builder customizeDialog = new AlertDialog.Builder(ConfirmActivity.this);
                    final View dialogView = LayoutInflater.from(ConfirmActivity.this).inflate(R.layout.dialog_edittext,null);
                    customizeDialog.setTitle("请填写一个手机号，方便接收模型训练进度信息。");
                    customizeDialog.setView(dialogView);
                    customizeDialog.setPositiveButton("提交",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText edit_text = dialogView.findViewById(R.id.phone);
                                    BakerVoiceEngraver.getInstance().finishRecords(edit_text.getText().toString().trim());
                                }
                            });
                    customizeDialog.setNegativeButton("跳过",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText edit_text = dialogView.findViewById(R.id.phone);
                                    BakerVoiceEngraver.getInstance().finishRecords(edit_text.getText().toString().trim());
                                }
                            });
                    customizeDialog.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void uploadRecordsResult(final boolean result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result) {
                    //TODO 此处应该将mouldId与用户的关系储存维护起来，此后要体验声音，都需要用到mouldId。
                    tvResult.setVisibility(View.INVISIBLE);
                    btnFinishEngrave.setText("返回再次体验");
                    tvTips.setText("已完成提交，\n感谢体验。");
                    canBack = true;
                } else {
                    canBack = false;
                    tvResult.setVisibility(View.VISIBLE);
                    btnFinishEngrave.setText("开启模型训练");
                    tranStrColor(getString(R.string.string_finish_record_tip));
                }
            }
        });

    }

    @Override
    public void onUploadError(int errorCode, String message) {
        Toast.makeText(ConfirmActivity.this, message, Toast.LENGTH_SHORT).show();
        canBack = false;
        tvResult.setVisibility(View.VISIBLE);
        btnFinishEngrave.setText("开启模型训练");
        tranStrColor(getString(R.string.string_finish_record_tip));
    }

    @Override
    public void onBack() {
        showNormalDialog();
    }

    private void tranStrColor(String str) {
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)),
                23, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTips.setText(spannableString);
    }
}
