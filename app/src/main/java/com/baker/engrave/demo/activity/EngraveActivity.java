package com.baker.engrave.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.engrave.demo.R;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.callback.ContentTextCallback;
import com.baker.engrave.lib.callback.RecordCallback;

public class EngraveActivity extends BaseActivity implements ContentTextCallback, RecordCallback {
    private Button btnRecordStart;
    private TextView tvIndex, tvTotal, tvContentText, tvTips;
    private ImageView imgRecording;
    private String[] contentTexts;
    private String mVoiceMouldId;
    private int currentIndex = 0;
    private boolean startOrEnd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engrave);
        showToolbar(R.string.string_step_2);

        initView();
        //TODO 注意设置回调在调用对应方法之前。
        initCallback();
        BakerVoiceEngraver.getInstance().getTextList();
        BakerVoiceEngraver.getInstance().getVoiceMouldId();
    }

    @Override
    public void contentTextList(String[] strList) {
        if (strList != null) {
            contentTexts = strList;

            tvIndex.setText(String.valueOf(currentIndex + 1));
            tvTotal.setText(String.format(getString(R.string.string_content_total), contentTexts.length));
            tvContentText.setText(contentTexts[0]);
            currentIndex = 0;
        }
    }

    @Override
    public void onContentTextError(int errorCode, String message) {

    }

    @Override
    public void voiceMouldId(String mouldId) {
        if (!TextUtils.isEmpty(mouldId)) {
            mVoiceMouldId = mouldId;
        }
    }

    /**
     * 识别回调
     *
     * @param typeCode        1=录音中， 2=识别中， 3=最终结果：通过， 4=最终结果：不通过
     * @param recognizeResult 识别率
     */
    @Override
    public void recordsResult(final int typeCode, final int recognizeResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (typeCode == 1) {
                    imgRecording.setVisibility(View.VISIBLE);
                    tvTips.setVisibility(View.INVISIBLE);
                    startOrEnd = false;
                    tvTips.setText("录音中...");
                    btnRecordStart.setEnabled(true);
                    btnRecordStart.setText("上传识别");
                } else if (typeCode == 2) {
                    tvTips.setVisibility(View.VISIBLE);
                    imgRecording.setVisibility(View.INVISIBLE);
                    tvTips.setText("识别中...");
                    btnRecordStart.setEnabled(false);
                } else if (typeCode == 3) {
                    tvTips.setVisibility(View.VISIBLE);
                    imgRecording.setVisibility(View.INVISIBLE);
                    currentIndex++;
                    if (currentIndex == contentTexts.length) {
                        Intent intent = new Intent(EngraveActivity.this, ConfirmActivity.class);
                        intent.putExtra("mouldId", mVoiceMouldId);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    startOrEnd = true;
                    tvTips.setText("太棒了，准确率：" + recognizeResult + "%，请录制下一段吧。");
                    btnRecordStart.setEnabled(true);
                    btnRecordStart.setText("开始录制");
                    tvIndex.setText(String.valueOf(currentIndex + 1));
                    tvContentText.setText(contentTexts[currentIndex]);
                } else if (typeCode == 4) {
                    tvTips.setVisibility(View.VISIBLE);
                    imgRecording.setVisibility(View.INVISIBLE);
                    startOrEnd = true;
                    tvTips.setText("识别率：" + recognizeResult + "%，请重新录制本段。");
                    btnRecordStart.setEnabled(true);
                    btnRecordStart.setText("重新录制");
                }
            }
        });
    }

    /**
     * 录音过程中音量数值回调。
     * @param volume
     */
    @Override
    public void recordVolume(final int volume) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeVolumeImg(volume);
            }
        });
    }

    @Override
    public void onRecordError(final int errorCode, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorCode == 90007) {
                    //Token过期，重新提交。
                    BakerVoiceEngraver.getInstance().reUploadRecords(contentTexts[currentIndex]);
                } else {
                    Log.e("EngraveActivity", "errorCode=" + errorCode + ", message=" + message);
                    Toast.makeText(EngraveActivity.this, message, Toast.LENGTH_SHORT).show();
                    imgRecording.setVisibility(View.INVISIBLE);
                    startOrEnd = true;
                    tvTips.setText("抱歉，识别出错啦，请重新录制本段。");
                    btnRecordStart.setEnabled(true);
                    btnRecordStart.setText("重新录制");
                }
            }
        });
    }

    private void initView() {
        btnRecordStart = findViewById(R.id.record_start);
        btnRecordStart.setOnClickListener(this);
        tvIndex = findViewById(R.id.tv_index_value);
        tvTotal = findViewById(R.id.tv_index_total);
        tvContentText = findViewById(R.id.tv_content);
        tvTips = findViewById(R.id.tv_recognize_result);
        tvTips.setVisibility(View.INVISIBLE);
        imgRecording = findViewById(R.id.img_recording);
        imgRecording.setVisibility(View.INVISIBLE);
    }

    private void initCallback() {
        BakerVoiceEngraver.getInstance().setContentTextCallback(this);
        BakerVoiceEngraver.getInstance().setRecordCallback(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_start:
                if (startOrEnd) {
                    //开始录音 0=mouldId为空，1=无权限，2=开启成功
                    int result = BakerVoiceEngraver.getInstance().startRecord(contentTexts[currentIndex]);
                } else {
                    //结束录音上传  0=mouldId为空, 1=结束成功，开始上传识别。
                    int result = BakerVoiceEngraver.getInstance().uploadRecords(contentTexts[currentIndex]);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBack() {
        showNormalDialog();
    }

    private long time;

    private void changeVolumeImg(int volume) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - time > 100) {
            if (volume < 30) {
                imgRecording.setImageResource(R.mipmap.volume_1);
            } else if (volume < 40) {
                imgRecording.setImageResource(R.mipmap.volume_2);
            } else if (volume < 50) {
                imgRecording.setImageResource(R.mipmap.volume_3);
            } else if (volume < 60) {
                imgRecording.setImageResource(R.mipmap.volume_4);
            } else if (volume < 70) {
                imgRecording.setImageResource(R.mipmap.volume_5);
            } else if (volume < 80) {
                imgRecording.setImageResource(R.mipmap.volume_6);
            } else if (volume < 90) {
                imgRecording.setImageResource(R.mipmap.volume_7);
            } else {
                imgRecording.setImageResource(R.mipmap.volume_8);
            }
            time = currentTime;
        }
    }
}
