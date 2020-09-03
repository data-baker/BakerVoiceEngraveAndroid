package com.baker.engrave.demo.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.engrave.demo.R;
import com.baker.engrave.demo.util.SharedPreferencesUtil;
import com.baker.engrave.lib.BakerVoiceEngraver;
import com.baker.engrave.lib.callback.ContentTextCallback;
import com.baker.engrave.lib.callback.PlayListener;
import com.baker.engrave.lib.callback.RecordCallback;

/**
 * 复刻开始录音界面
 */


public class EngraveActivity extends BaseActivity {

    public static final String TAG = "EngraveActivity";

    private Button btnRecordStart;
    private TextView tvIndex, tvTotal, tvContentText, tvTips;
    private ImageView imgRecording;
    private String[] contentTexts; //文本总数量
    private int currentIndex = 0;
    private boolean startOrEnd = true;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engrave);
        showToolbar(R.string.string_step_2);

        initView();
        //TODO 注意设置回调在调用对应方法之前。
        initCallback();
        //QueryId的作用是xxx，非常建议设置，如果在初始化时已经填写了QueryId，此方法不必重复设置。
        //如果要上传QueryID，请务必在调用getVoiceMouldId()方法之前调用。
        BakerVoiceEngraver.getInstance().setQueryId(SharedPreferencesUtil.getQueryId());
        BakerVoiceEngraver.getInstance().getTextList(); //获取文本列表，回调在callback里
        BakerVoiceEngraver.getInstance().getVoiceMouldId(); //获取，不知道获取的啥
    }


    private void initView() {
        btnRecordStart = findViewById(R.id.record_start);
        Button btnPre = findViewById(R.id.btnPre);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnPlay = findViewById(R.id.btnPlay);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
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
        //获取文本内容
        BakerVoiceEngraver.getInstance().setContentTextCallback(new ContentTextCallback() {
            @Override
            public void contentTextList(String[] strList) {
                runOnUiThread(() -> {
                    if (strList != null) {
                        contentTexts = strList;
                        tvIndex.setText(String.valueOf(currentIndex + 1));
                        tvTotal.setText(String.format(getString(R.string.string_content_total), contentTexts.length));
                        tvContentText.setText(contentTexts[0]);
                        currentIndex = 0;
                    }
                });
            }

            @Override
            public void onContentTextError(int errorCode, String message) {
                Log.d(TAG, "onContentTextError errorCode = " + errorCode + " message = " + message);
            }
        });
        BakerVoiceEngraver.getInstance().setRecordCallback(new RecordCallback() {
            /*
             1=录音中， 2=识别中， 3=最终结果：通过， 4=最终结果：不通过
             */
            @SuppressLint("SetTextI18n")
            @Override
            public void recordsResult(int typeCode, int recognizeResult) {
                runOnUiThread(() -> {
                    log("---6");
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

                        startOrEnd = true;
                        tvTips.setText("太棒了，准确率：" + recognizeResult + "%，请录制下一段吧。");
                        toNext();
                    } else if (typeCode == 4) {
                        tvTips.setVisibility(View.VISIBLE);
                        imgRecording.setVisibility(View.INVISIBLE);
                        startOrEnd = true;
                        tvTips.setText("识别率：" + recognizeResult + "%，请重新录制本段。");
                        btnRecordStart.setEnabled(true);
                        btnRecordStart.setText("重新录制");
                    }
                });
            }

            @Override
            public void recordVolume(int volume) {
                runOnUiThread(() -> changeVolumeImg(volume));
            }

            @Override
            public void onRecordError(int errorCode, String message) {
                runOnUiThread(() -> {
                    Log.e("EngraveActivity", "errorCode=" + errorCode + ", message=" + message);
                    Toast.makeText(EngraveActivity.this, message, Toast.LENGTH_SHORT).show();
                    startOrEnd = true;
                    tvTips.setText("抱歉，识别出错啦，请重新录制本段。");
                    imgRecording.setVisibility(View.INVISIBLE);
                    btnRecordStart.setEnabled(true);
                    btnRecordStart.setText("重新录制");
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.record_start) {
            if (startOrEnd) {
                //开始录音 0=mouldId为空，1=无权限，2=开启成功
                BakerVoiceEngraver instance = BakerVoiceEngraver.getInstance();
                int result = instance.startRecord(currentIndex);
                Log.d(TAG, result + "");
            } else {
                //结束录音上传  0=mouldId为空, 1=结束成功，开始上传识别。
                BakerVoiceEngraver.getInstance().endRecord();
            }
        } else if (id == R.id.btnPre) {//上一句
            toPre();
        } else if (id == R.id.btnNext) {//下一句
            toNext();
        } else if (id == R.id.btnPlay) {//试听
            toPlay();
        }
    }

    /**
     * 上一条
     */
    private void toPre() {
        if (currentIndex > 0) {
            currentIndex--;
            updateView();
        } else {
            Toast.makeText(this, "当前是第一条", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateView() {
        btnRecordStart.setEnabled(true);

        if (BakerVoiceEngraver.getInstance().isRecord(currentIndex)) {
            btnRecordStart.setText("重新录制");
        } else {
            btnRecordStart.setText("开始录制");
        }
        tvIndex.setText(String.valueOf(currentIndex + 1));
        tvContentText.setText(contentTexts[currentIndex]);
    }


    /**
     * 下一条
     */
    private void toNext() {
        //判断当前这条是否录制
        if (BakerVoiceEngraver.getInstance().isRecord(currentIndex)) {
            if (currentIndex >= contentTexts.length - 1) {
                startActivity(new Intent(EngraveActivity.this, ConfirmActivity.class));
                finish();
            } else {
                currentIndex++;
                updateView();
            }
        } else {
            Toast.makeText(this, "当前条未录制成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 试听
     */
    private void toPlay() {
        BakerVoiceEngraver.getInstance().startPlay(currentIndex, new PlayListener() {
            @Override
            public void playStart() {
                log("试听开始");
                showProgressDialog();
            }

            @Override
            public void playEnd() {
                Toast.makeText(EngraveActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
                disMissProgressDialog();
            }

            @Override
            public void playError(Exception e) {
                e.printStackTrace();
                log("试听出错" + e.toString());
                disMissProgressDialog();
            }
        });
    }

    /**
     * 显示正在请求网络的进度条
     */
    public void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(EngraveActivity.this);
                progressDialog.setMessage("正在播放中");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        BakerVoiceEngraver.getInstance().stopPlay();
                    }
                });
            }
        });
    }

    /**
     * 取消进度条显示
     */
    public void disMissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });
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

    private void log(String msg) {
        Log.d(EngraveActivity.class.getName(), msg);
    }
}
