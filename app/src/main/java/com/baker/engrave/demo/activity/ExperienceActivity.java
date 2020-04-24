package com.baker.engrave.demo.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baker.engrave.demo.R;
import com.baker.engrave.demo.base.Constants;
import com.baker.engrave.lib.BakerVoiceEngraver;

import java.net.URLEncoder;

public class ExperienceActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView tvIndex, tvMouldId, tvWords;
    private EditText editText;
    private SeekBar seekBar;
    private Button btnPlay;
    private String mouldId;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience);
        showToolbar(R.string.string_listen_experience);

        initView();
        int index = getIntent().getIntExtra("index", 1);
        mouldId = getIntent().getStringExtra("mouldId");
        tvIndex.setText((index + 1) + "");
        tvMouldId.setText("声音ID：" + mouldId);
    }

    private void initView() {
        tvIndex = findViewById(R.id.tv_index);
        tvMouldId = findViewById(R.id.tv_mould_id);
        tvMouldId.setOnClickListener(this);
        editText = findViewById(R.id.edt_tts_content);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        tvWords = findViewById(R.id.tv_words_remind);
        tranStrColor(String.format(getString(R.string.string_words_remind), "0"));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString().trim()) && charSequence.toString().trim().length() < 201) {
                    tranStrColor(String.format(getString(R.string.string_words_remind), String.valueOf(charSequence.toString().trim().length())));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                disMissProgressDialog();
                return false;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekBar.setProgress(100);
                handler.sendEmptyMessageDelayed(103, 200);
                stopPlay();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                disMissProgressDialog();
                ExperienceActivity.this.mediaPlayer.start();
                updateSeekBar();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                if (mediaPlayer.isPlaying()) {
                    stopPlay();
                } else {
                    prepare();
                }
                break;
            case R.id.tv_mould_id:
                try {
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", mouldId);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(ExperienceActivity.this, "模型ID复制成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    return;
                }
                break;
            default:
                break;
        }
    }

    private void prepare() {
        if (TextUtils.isEmpty(mouldId)) {
            Toast.makeText(ExperienceActivity.this, "声音ID为空哦", Toast.LENGTH_SHORT).show();
            return;
        }
        String contentStr = editText.getText().toString().trim();
        if (TextUtils.isEmpty(contentStr)) {
            Toast.makeText(ExperienceActivity.this, "合成文本内容不能为空哦", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = BakerVoiceEngraver.getInstance().getToken();
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(ExperienceActivity.this, "合成token为空哦", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }
        try {
            String url = String.format(Constants.TTS_MY_VOICE_URL, "3", mouldId,
                    token, URLEncoder.encode(contentStr, "UTF-8"));
//            String url = String.format(Constants.TTS_BASE_URL, "5", "3", "标准合成_模仿儿童_果子",
//              token, URLEncoder.encode(contentStr, "UTF-8"));
            Log.e("ExperienceActivity","url=" + url);
            mediaPlayer.setDataSource(url);
        } catch (Exception e) {
            disMissProgressDialog();
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    private void stopPlay() {
        refreshSeekBar = false;
        mediaPlayer.stop();
        mediaPlayer.reset();
        seekTo(0);
        btnPlay.setText("合成播放");
    }

    private int messageWhat = 102;
    private boolean refreshSeekBar = false;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == messageWhat && refreshSeekBar) {
                if (!seekBarIsChange && mediaPlayer.getDuration() != 0 && mediaPlayer.getCurrentPosition() != 0) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());
                }
                handler.sendEmptyMessageDelayed(messageWhat, 200);
                return;
            } else if (msg.what == 103) {
                seekBar.setProgress(0);
            }
            super.handleMessage(msg);
        }
    };

    private void updateSeekBar() {
        seekTo(0);
        refreshSeekBar = true;
        handler.sendEmptyMessage(messageWhat);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPlay.setText("停止播放");
            }
        });
    }

    private void seekTo(int jumpPosition) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(jumpPosition);
            mediaPlayer.start();
        } else {
            mediaPlayer.seekTo(jumpPosition);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        refreshSeekBar = false;
        super.onDestroy();
    }

    private void tranStrColor(String str) {
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)),
                3, spannableString.length() - 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvWords.setText(spannableString);
    }

    private boolean seekBarIsChange = false;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        seekBarIsChange = false;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarIsChange = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBarIsChange = true;
        seekTo(seekBar.getProgress() * mediaPlayer.getDuration() / 100);
        seekBarIsChange = false;
    }
}
