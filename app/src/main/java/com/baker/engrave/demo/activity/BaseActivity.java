package com.baker.engrave.demo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baker.engrave.demo.R;
import com.baker.engrave.lib.BakerVoiceEngraver;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showToolbar(int resId) {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_chevron_left_white_36dp);
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(resId);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            onBack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onBack() {
        finish();
    }

    @Override
    public void onClick(View view) {

    }

    public void showNormalDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(BaseActivity.this);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("如果退出了，录音信息就没有了哦。");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //非常建议在录音过程中异常退出的话，调用此方法通知服务器，这样的话会及时释放当前训练模型所占用的名额。
                        BakerVoiceEngraver.getInstance().recordInterrupt();
                        //关闭activity
                        finish();
                    }
                });
        normalDialog.setNegativeButton("返回",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        // 显示
        normalDialog.show();
    }

    /**
     * 显示正在请求网络的进度条
     */
    public void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                progressDialog = new Dialog(BaseActivity.this, R.style.progress_dialog);
                View view = View.inflate(BaseActivity.this, R.layout.view_progress_dialog, null);
                progressDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
                progressDialog.setCancelable(false);
                progressDialog.setContentView(view);
                if (!isFinishing()) {
                    progressDialog.show();
                }
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
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }
}
