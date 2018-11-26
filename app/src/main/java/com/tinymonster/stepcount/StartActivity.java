package com.tinymonster.stepcount;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.droi.sdk.core.Core;
import com.droi.sdk.core.DroiObject;
import com.droi.sdk.core.DroiUser;

public class StartActivity extends AppCompatActivity {
    private static final int GO_HOME=0;
    private static final int GO_LOGIN=1;
    private static final int GO_START=2;
    private String name="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Core.initialize(this);
        DroiObject.registerCustomClass(user.class);
        DroiObject.registerCustomClass(pace.class);
        DroiObject.registerCustomClass(DroiUser.class);
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER", MODE_PRIVATE);
        name=sharedPreferences.getString("name","");
        if(name!=""){
            mHandler.sendEmptyMessageDelayed(GO_HOME,3000);
        }else {
            mHandler.sendEmptyMessageDelayed(GO_LOGIN,3000);
        }
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_HOME://去主页
                    Log.e("Handle去主页"," ");
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case GO_LOGIN://去登录页
                    Log.e("Handle去登陆页"," ");
                    Intent intent2 = new Intent(StartActivity.this, LogActivity.class);
                    startActivity(intent2);
                    finish();
                    break;
            }
        }
    };
}
