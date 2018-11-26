package com.tinymonster.stepcount;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import step.UpdateUiCallBack;
import step.service.StepService;

public class MapActivity extends AppCompatActivity implements SensorEventListener{
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private ImageView map_iv_left;
    private TextView Map_distance_tv;
    private TextView Map_time_tv;
    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
    Date date;
    Date firstDate;
    MapView mMapView;
    BaiduMap mBaiduMap;
    int steps;
    int BaseSteps;
    private TextView info;
    private RelativeLayout progressBarRl;

    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    float mCurrentZoom = 7.0f;//默认地图缩放比例值
    boolean isFisrtDate=true;
    private  double Distance=0;
    private BigDecimal bg;
    private double DistanceRound;
    private SensorManager mSensorManager;
    BitmapDescriptor startBD;
    BitmapDescriptor finishBD;
    boolean ServiceConnect =false;
    List<LatLng> points = new ArrayList<LatLng>();//位置点集合
    Polyline mPolyline;//运动轨迹图层
    LatLng last = new LatLng(0, 0);//上一个定位点
    MapStatus.Builder builder;
    private StepService service;

    /*
    服务
     */
    private StepService.StepBinder stepBinder;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            stepBinder=(StepService.StepBinder)iBinder;
            service=stepBinder.getService();
//            if(isFisrtDate){
//                isFisrtDate=false;
//                BaseSteps=Integer.valueOf(stepBinder.getSteps());
//            }
//            steps=Integer.valueOf(stepBinder.getSteps());
//            Distance=(steps-BaseSteps)*0.0005;
//            bg=new BigDecimal(Distance);
//            DistanceRound=bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.e("步数",""+steps);
//                    Map_distance_tv.setText(DistanceRound+ "km");
//                }
//            });
            service.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    date=new Date(System.currentTimeMillis());
                    if(isFisrtDate){
                        isFisrtDate=false;
                        BaseSteps=stepCount;
                            firstDate =new Date(System.currentTimeMillis());
                    }
//                    steps=Integer.valueOf(stepBinder.getSteps());
                    Distance=(stepCount-BaseSteps)*0.0005;
                    bg=new BigDecimal(Distance);
                    DistanceRound=bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                    Log.e("步数",""+stepCount);
//                    Map_distance_tv.setText(DistanceRound+ "km");
                    Map_distance_tv.setText(DistanceRound+" km");
                    Long time=(date.getTime()-firstDate.getTime())/1000;
                    Log.e("时间差",time.toString());
                    Map_time_tv.setText(time%(24*3600)/3600+"小时"+time%3600/60+"分"+time%60+"秒");
                }
            });
            ServiceConnect=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplication());
        setContentView(R.layout.activity_map);
        isFisrtDate=true;
        mMapView=(MapView)findViewById(R.id.bmapView);
        initView();
        startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
        finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                mCurrentZoom = mapStatus.zoom;
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }
        });
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只用gps定位，需要在室外定位。
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
    }
    /*
    控件初始化
     */
    private void initView() {
        map_iv_left=(ImageView)findViewById(R.id.map_iv_left);
        Button start = (Button) findViewById(R.id.buttonStart);
        final Button finish = (Button) findViewById(R.id.buttonFinish);
        info = (TextView) findViewById(R.id.info);
        progressBarRl = (RelativeLayout) findViewById(R.id.progressBarRl);
        Map_distance_tv=(TextView)findViewById(R.id.Map_distance_tv);
        Map_time_tv=(TextView)findViewById(R.id.Map_time_tv);
        Map_distance_tv.setText("0 km");
        Map_time_tv.setText("00时00分00秒");
        map_iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mLocClient != null && !mLocClient.isStarted()) {
                    mLocClient.start();
                    progressBarRl.setVisibility(View.VISIBLE);
                    info.setText("GPS信号搜索中，请稍后...");
                    mBaiduMap.clear();
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(ServiceConnect) {
                    unbindService(connection);
                    ServiceConnect=false;
                }
                if (mLocClient != null && mLocClient.isStarted()) {
                    mLocClient.stop();

                    progressBarRl.setVisibility(View.GONE);

                    if (isFirstLoc) {
                        points.clear();
                        last = new LatLng(0, 0);
                        return;
                    }

                    MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
                    oFinish.position(points.get(points.size() - 1));
                    oFinish.icon(finishBD);// 设置覆盖物图片
                    mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层

                    //复位
                    points.clear();
                    last = new LatLng(0, 0);
                    isFirstLoc = true;

                }
            }
        });

    }
    double lastX;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];

        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;

            if (isFirstLoc) {
                lastX = x;
                return;
            }

            locData = new MyLocationData.Builder().accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            Toast.makeText(MapActivity.this,"监听器开始接受",Toast.LENGTH_SHORT).show();
            if (location == null || mMapView == null) {
                return;
            }

            //注意这里只接受gps点，需要在室外定位。
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                info.setText("GPS信号弱，请稍后...");

                if (isFirstLoc) {//首次定位
                    //第一个点很重要，决定了轨迹的效果，gps刚开始返回的一些点精度不高，尽量选一个精度相对较高的起始点
                    LatLng ll = null;
                    ll=new LatLng(location.getLatitude(),location.getLongitude());
//                    ll = getMostAccuracyLocation(location);
                    if(ll == null){
                        return;
                    }
                    isFirstLoc = false;
                    points.add(ll);//加入集合
                    last = ll;

                    //显示当前定位点，缩放地图
                    locateAndZoom(location, ll);

                    //标记起点图层位置
                    MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
                    oStart.position(points.get(0));// 覆盖物位置点，第一个点为起点
                    oStart.icon(startBD);// 设置覆盖物图片
                    mBaiduMap.addOverlay(oStart); // 在地图上添加此图层

                    progressBarRl.setVisibility(View.GONE);

                    return;//画轨迹最少得2个点，首地定位到这里就可以返回了
                }
                Intent bindIntent=new Intent(MapActivity.this,StepService.class);
                bindService(bindIntent,connection,BIND_AUTO_CREATE);//绑定服务
                //从第二个点开始
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                //sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离大于为5米才添加到集合中
                if (DistanceUtil.getDistance(last, ll) < 5) {
                    return;
                }

                points.add(ll);//如果要运动完成后画整个轨迹，位置点都在这个集合中

                last = ll;

                //显示当前定位点，缩放地图
                locateAndZoom(location, ll);

                //清除上一次轨迹，避免重叠绘画
                mMapView.getMap().clear();

                //起始点图层也会被清除，重新绘画
                MarkerOptions oStart = new MarkerOptions();
                oStart.position(points.get(0));
                oStart.icon(startBD);
                mBaiduMap.addOverlay(oStart);

                //将points集合中的点绘制轨迹线条图层，显示在地图上
                OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(points);
                mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
//            }else {
//                Toast.makeText(MapActivity.this,"定位不是GPS",Toast.LENGTH_SHORT).show();
//            }
        }
    }
    private void locateAndZoom(final BDLocation location, LatLng ll) {
        mCurrentLat = location.getLatitude();
        mCurrentLon = location.getLongitude();
        locData = new MyLocationData.Builder().accuracy(0)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);

        builder = new MapStatus.Builder();
        builder.target(ll).zoom(mCurrentZoom);
//        builder.target(ll).zoom(12.5f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    /**
     * 首次定位很重要，选一个精度相对较高的起始点
     * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
     你可以将location.getRadius()>25中的过滤半径调大，比如>40，
     并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
     这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
     gps的精度半径一般是10-50米
     */
    private LatLng getMostAccuracyLocation(BDLocation location){

        if (location.getRadius()>400) {//gps位置精度大于40米的点直接弃用
            Toast.makeText(MapActivity.this,"大于400",Toast.LENGTH_SHORT).show();
            return null;
        }

        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

        if (DistanceUtil.getDistance(last, ll ) > 200) {
            Toast.makeText(MapActivity.this,"大于200",Toast.LENGTH_SHORT).show();
            last = ll;
            points.clear();//有任意连续两点位置大于10，重新取点
            return null;
        }
        points.add(ll);
        last = ll;
        //有5个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
        if(points.size() >= 400){
            Toast.makeText(MapActivity.this,"大于100",Toast.LENGTH_SHORT).show();
            points.clear();
            return ll;
        }
        return null;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.unRegisterLocationListener(myListener);
        if(ServiceConnect) {
            unbindService(connection);
            ServiceConnect=false;
        }
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.getMap().clear();
        mMapView.onDestroy();
        mMapView = null;
        startBD.recycle();
        finishBD.recycle();
        super.onDestroy();
    }
}
