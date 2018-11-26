package com.tinymonster.stepcount;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.droi.sdk.DroiCallback;
import com.droi.sdk.DroiError;
import com.droi.sdk.core.DroiPermission;
import com.droi.sdk.core.DroiUser;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import step.UpdateUiCallBack;
import step.service.StepService;
import step.utils.SharedPreferencesUtils;
import view.StepArcView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private FloatingActionButton main_float;
    private TextView tv_logout;
    private TextView tv_data;
    private TextView main_tv_nowstep;
    private TextView main_tv_planstep;
    private TextView tv_set;
    private TextView tv_isSupport;
    private TextView tv_map;
    private TextView main_tv_nowdistance;
    private  double Distance=0;
    private BigDecimal bg;
    private double DistanceRound;
    private String Current_pacenum="";
    private String name;
    private SharedPreferencesUtils sp;//暂存数据  跑步计划
    private void assignViews() {
        main_float=(FloatingActionButton)findViewById(R.id.main_float);
        tv_logout=(TextView)findViewById(R.id.tv_logout);
        tv_data = (TextView) findViewById(R.id.tv_data);
        main_tv_nowstep=(TextView)findViewById(R.id.main_tv_nowstep);
        main_tv_planstep=(TextView)findViewById(R.id.main_tv_planstep);
        tv_set = (TextView) findViewById(R.id.tv_set);
        tv_map=(TextView)findViewById(R.id.tv_map);
        tv_isSupport = (TextView) findViewById(R.id.tv_isSupport);
        main_tv_nowdistance=(TextView)findViewById(R.id.main_tv_nowdistance);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();//初始化数据  启动服务StepService
        addListener();
    }
    private void addListener() {
        main_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,ShareActivity.class);
                startActivity(intent);
            }
        });
        main_float.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("提示");
                dialog.setMessage("确认分享？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                        Date date = new Date(System.currentTimeMillis());
                        DroiPermission permission = new DroiPermission();
                        permission.setPublicReadPermission(true);
                        permission.setUserReadPermission(DroiUser.getCurrentUser().getObjectId(), true);
                        permission.setUserWritePermission(DroiUser.getCurrentUser().getObjectId(), true);
                        pace pace1=new pace(name,Current_pacenum,simpleDateFormat.format(date)+"");
                        pace1.setPermission(permission);
                        pace1.saveInBackground(new DroiCallback<Boolean>() {
                            @Override
                            public void result(Boolean aBoolean, DroiError droiError) {
                                if(aBoolean){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this,"分享成功",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
                return true;
            }
        });
        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DroiUser droiUser=DroiUser.getCurrentUser();
                if(droiUser.isLoggedIn()){
                    droiUser.logout();
                }
                SharedPreferences.Editor editor=getSharedPreferences("USER",MODE_PRIVATE).edit();
                editor.putString("name","");
                editor.apply();
                Intent intent=new Intent(MainActivity.this,LogActivity.class);
                startActivity(intent);
                finish();
            }
        });
        tv_set.setOnClickListener(this);
        tv_data.setOnClickListener(this);
        tv_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> permissionList=new ArrayList<>();
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        !=PackageManager.PERMISSION_GRANTED){
                    permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if(!permissionList.isEmpty()){
                    String[] permissions=permissionList.toArray(new String[permissionList.size()]);
                    ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
                }else {
                    Intent intent=new Intent(MainActivity.this,MapActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void initData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER", MODE_PRIVATE);
        name=sharedPreferences.getString("name","");
        sp = new SharedPreferencesUtils(this);
        //获取用户设置的计划锻炼步数，没有设置过的话默认7000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
        //设置当前步数为0  设置计划锻炼步数和当前步数
        main_tv_nowstep.setText(""+"0");
        main_tv_planstep.setText(planWalk_QTY);
        tv_isSupport.setText("计步中...");
        main_tv_nowdistance.setText("0 km");
        /*
        启动服务
         */
        setupService();
    }


    private boolean isBind = false;

    /**
     * 开启计步服务  绑定服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        /*
        绑定服务
         */
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //StepService服务 getStepCount
            StepService stepService = ((StepService.StepBinder) service).getService();
            //设置初始化数据   总步数
            String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
            /*
            设置当前步数
             */
            main_tv_nowstep.setText(""+stepService.getStepCount());
            Current_pacenum=stepService.getStepCount()+"步";
            Distance=stepService.getStepCount()*0.0005;
            bg=new BigDecimal(Distance);
            DistanceRound=bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            main_tv_nowdistance.setText(DistanceRound+" km");
            main_tv_planstep.setText(planWalk_QTY);

            //设置步数监听回调  设置步数
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
                    main_tv_nowstep.setText(""+stepCount);
                    Distance=stepCount*0.0005;
                    bg=new BigDecimal(Distance);
                    DistanceRound=bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                    main_tv_nowdistance.setText(DistanceRound+" km");
                    main_tv_planstep.setText(planWalk_QTY);
//                    cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepCount);
                }
            });
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set:
                startActivity(new Intent(this, SetPlanActivity.class));
                break;
            case R.id.tv_data:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有的权限才能使用被程序!",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    Intent intent=new Intent(MainActivity.this,MapActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
        }
        /*
        权限请求
         */
    }
}
