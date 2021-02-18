package com.khs.sample_download_file.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.khs.sample_download_file.R;
import com.khs.sample_download_file.Utils;
import com.khs.sample_download_file.adapter.DownloadItem;
import com.khs.sample_download_file.adapter.DownloadItemAdapter;
import com.khs.sample_download_file.server.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by khs on 12/22/2020.
 */

public class DownloadMainActivity extends AppCompatActivity implements View.OnClickListener {
    static final public String ITEMS    = "items";

    private final int PERMISSIONS_REQUEST_STORAGE = 100;
    private final int MSG_WHAT_LOAD_SUCCESS = 1000;

    private Context mContext;

    private ListView dlListView;
    private CheckBox dlAllCheckBox;
    private TextView dlAllTextView;
    private Button dlButton;

    private List<DownloadItem> downloadList;
    private List<Uri> selectDownloadList;
    private DownloadItemAdapter downloadItemAdapter;

    private ArrayList<String> mUrlList = new ArrayList<>();

    private DownloadManager mDownloadManager;
    private Long mDownloadQueueId;

    private View mRootLoadingView;
    private LottieAnimationView mLoadingView;

    private String outputFilePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/khs") + "/downloadFile.txt";

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null) return;

            if (msg.what == MSG_WHAT_LOAD_SUCCESS) {
                if (mLoadingView.isAnimating())
                    mLoadingView.cancelAnimation();
                if (mRootLoadingView.getVisibility() == View.VISIBLE)
                    mRootLoadingView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        initRes();

        retrofitConnection();

    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteReceiver, completeFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(downloadCompleteReceiver);
    }

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(mDownloadQueueId == reference){
                DownloadManager.Query query = new DownloadManager.Query();  // 다운로드 항목 조회에 필요한 정보 포함
                query.setFilterById(reference);
                Cursor cursor = mDownloadManager.query(query);

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                int status = cursor.getInt(columnIndex);
                int reason = cursor.getInt(columnReason);

                cursor.close();

                switch (status){
                    case DownloadManager.STATUS_SUCCESSFUL :
                        Toast.makeText(mContext, R.string.download_success, Toast.LENGTH_SHORT).show();
                        break;

                    case DownloadManager.STATUS_PAUSED :
                        Toast.makeText(mContext, R.string.download_pause, Toast.LENGTH_SHORT).show();
                        break;

                    case DownloadManager.STATUS_FAILED :
                        Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    private void initRes() {
        if (mRootLoadingView == null) {
            mRootLoadingView = findViewById(R.id.loading_layout);
        }
        mLoadingView = mRootLoadingView.findViewById(R.id.animation_view);
        mLoadingView.setAnimation("tree_icon.json");
        mLoadingView.loop(true);
        mLoadingView.playAnimation();
        mRootLoadingView.setVisibility(View.VISIBLE);

        dlListView = (ListView)findViewById(R.id.download_list_view);
        dlAllCheckBox = (CheckBox)findViewById(R.id.all_select_checkbox);
        dlAllCheckBox.setOnClickListener(this);
        dlAllTextView = (TextView)findViewById(R.id.count_txt);
        dlAllTextView.setText(R.string.select_all);
        dlButton = (Button)findViewById(R.id.download_button);
        dlButton.setEnabled(false);
        dlButton.setOnClickListener(this);

        mUrlList = new ArrayList<>();
        downloadList = new ArrayList<>();
        selectDownloadList = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_select_checkbox:
                int size = downloadList.size();

                selectDownloadList.clear();
                for(int i=0; i<size; i++) {
                    DownloadItem items = downloadList.get(i);
                    items.setChecked(dlAllCheckBox.isChecked());
                    if(dlAllCheckBox.isChecked()) {
                        selectDownloadList.add(items.getItemUri());
                        dlAllTextView.setText(R.string.select_dismiss);
                    } else {
                        dlAllTextView.setText(R.string.select_all);
                    }
                }

                dlButton.setEnabled(selectDownloadList.size() != 0);
                downloadItemAdapter.notifyDataSetChanged();
                break;
            case R.id.download_button:
                int networkStatus = Utils.getActiveNetworkStatus(mContext);
                if(networkStatus != Utils.TYPE_NOT_CONNECTED) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck!= PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getApplicationContext(), R.string.msg_permission, Toast.LENGTH_SHORT).show();
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSIONS_REQUEST_STORAGE);
                        }
                    } else {
                        if (!selectDownloadList.isEmpty()) {
                            for (int i = 0; i < selectDownloadList.size(); i++) {
                                URLDownloading(selectDownloadList.get(i));
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_network, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!selectDownloadList.isEmpty()) {
                        for (int i = 0; i < selectDownloadList.size(); i++) {
                            URLDownloading(selectDownloadList.get(i));
                        }
                    }
                }
                return;
        }
    }

    private void URLDownloading(Uri url) {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        File outputFile = new File(outputFilePath);
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        Uri downloadUri = url;
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        List<String> pathSegmentList = downloadUri.getPathSegments();
        request.setTitle("다운로드 항목");
        request.setDestinationUri(Uri.fromFile(outputFile));
        request.setAllowedOverMetered(true);

        mDownloadQueueId = mDownloadManager.enqueue(request);
    }

    private void retrofitConnection() {
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<ResponseBody> call = retrofitClient.retrofitAPI.getURL();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("khskhs", "khskhs onResponse : " + response.body().toString());
                ResponseBody responseBody = response.body();
                try {
                    String result = responseBody.string();
                    if (jsonParseResult(result)) mMainHandler.sendEmptyMessage(MSG_WHAT_LOAD_SUCCESS);
                } catch (IOException e) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("khskhs", "khskhs onFailure");
            }
        });
    }

    private boolean jsonParseResult(String jsonString) {
        if (jsonString == null ) return false;

        mUrlList.clear();
        downloadList.clear();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray items = jsonObject.getJSONArray(ITEMS);

            for (int i = 0; i < items.length(); i++) {
                JSONObject info = items.getJSONObject(i);
                String url = info.getString("url");
                mUrlList.add(url);
            }
            getInitListView();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getInitListView() {
        int length = mUrlList.size();
        for(int i=0;i<length;i++) {
            String itemText = mUrlList.get(i);
            Uri itemUri = Uri.parse(itemText);
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setChecked(false);
            downloadItem.setItemUri(itemUri);

            downloadList.add(downloadItem);
        }

        downloadItemAdapter = new DownloadItemAdapter(getApplicationContext(), downloadList);
        downloadItemAdapter.notifyDataSetChanged();

        dlListView.setAdapter(downloadItemAdapter);

        dlListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long l) {
                Object itemObject = adapterView.getAdapter().getItem(itemIndex);
                DownloadItem downloadItem = (DownloadItem)itemObject;

                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.list_view_item_checkbox);

                if(downloadItem.isChecked()) {
                    for(int i=0; i<selectDownloadList.size(); i++) {
                        if(selectDownloadList.get(i) == downloadItem.getItemUri())
                            selectDownloadList.remove(i);
                    }
                    itemCheckbox.setChecked(false);
                    downloadItem.setChecked(false);
                } else {
                    selectDownloadList.add(downloadItem.getItemUri());
                    itemCheckbox.setChecked(true);
                    downloadItem.setChecked(true);
                }

                dlButton.setEnabled(selectDownloadList.size() != 0);
            }
        });
    }
}