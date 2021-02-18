package com.khs.sample_download_file.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.khs.sample_download_file.R;
import com.khs.sample_download_file.Utils;

/**
 * Created by khs on 12/22/2020.
 */

public class DownloadIntroActivity extends AppCompatActivity {
    private final int MSG_WHAT_START_ACTIVITY = 1000;
    private final int MSG_WHAT_FINISH_ACTIVITY = 1001;

    private Context mContext;

    private Handler mIntroHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null) return;

            if (msg.what == MSG_WHAT_START_ACTIVITY) {
                Intent i = new Intent(getApplicationContext(), DownloadMainActivity.class);
                startActivity(i);
                finish();
            } else if (msg.what == MSG_WHAT_FINISH_ACTIVITY) {
                finishAffinity();
                System.exit(0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mContext = this;

        startActivity();
    }

    private void startActivity() {
        int networkStatus = Utils.getActiveNetworkStatus(mContext);
        if(networkStatus != Utils.TYPE_NOT_CONNECTED) {
            mIntroHandler.sendEmptyMessageDelayed(MSG_WHAT_START_ACTIVITY, 1000);
        } else {
            Toast.makeText(getApplicationContext(), R.string.msg_network, Toast.LENGTH_SHORT).show();
            mIntroHandler.sendEmptyMessageDelayed(MSG_WHAT_FINISH_ACTIVITY, 1000);
        }
    }
}
