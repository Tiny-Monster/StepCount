package com.tinymonster.stepcount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.droi.sdk.DroiError;
import com.droi.sdk.core.DroiCondition;
import com.droi.sdk.core.DroiObject;
import com.droi.sdk.core.DroiQuery;
import com.droi.sdk.core.DroiQueryCallback;

import java.util.ArrayList;
import java.util.List;

import adapter.ShareAdapter;

public class ShareActivity extends AppCompatActivity {
    private Button Share_back_button;
    private ListView share_list;
    private List<pace> paceList=new ArrayList<>();
    public static final int DOWNLOAD_SUCCESS=3;
    public static final int DOWNLOAD_FAIL=4;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initView();
        download();
    }
    private void initView(){
        share_list=(ListView)findViewById(R.id.share_recycle);
        Share_back_button=(Button)findViewById(R.id.Share_back_button);
        Share_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void download(){
        progressDialog=new ProgressDialog(ShareActivity.this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("网络连接中……");
        progressDialog.setCancelable(false);
        progressDialog.show();
        DroiQuery query=DroiQuery.Builder.newBuilder().cloudStorage().query(pace.class).build();
        query.runQueryInBackground(new DroiQueryCallback<pace>() {
            @Override
            public void result(List<pace> list, DroiError droiError) {
                if(droiError.isOk()&&list.size()>0){
                    paceList=list;
                    mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                }else {
                    mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                }
            }
        });
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_SUCCESS://去主页
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            ShareAdapter shareAdapter=new ShareAdapter(ShareActivity.this,R.layout.paceitem,paceList);
                            share_list.setAdapter(shareAdapter);
                        }
                    });
                    break;
                case DOWNLOAD_FAIL://去登录页
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(ShareActivity.this,"暂无信息",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }
    };
}
