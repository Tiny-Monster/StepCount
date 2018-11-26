package com.tinymonster.stepcount;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

import step.UpdateUiCallBack;
import step.bean.StepData;
import step.utils.DbUtils;

public class MyStepService extends Service {
    private StepBinder stepBinder=new StepBinder();
    public MyStepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return stepBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    class StepBinder extends Binder{
        public String getSteps(){
            if(DbUtils.getLiteOrm()==null){
                DbUtils.createDb(getApplication(), "jingzhi");
            }
            List<StepData> stepDatas =DbUtils.getQueryAll(StepData.class);
            int size=stepDatas.size();
            StepData TodaystepData=stepDatas.get(size-1);
            return TodaystepData.getStep();
        }
        public MyStepService getService(){
            return MyStepService.this;
        }
    }
    private UpdateUiCallBack mCallback;
    /**
     * 注册UI更新监听
     *
     * @param paramICallback
     */
    public void registerCallback(UpdateUiCallBack paramICallback) {
        this.mCallback = paramICallback;
    }
}
