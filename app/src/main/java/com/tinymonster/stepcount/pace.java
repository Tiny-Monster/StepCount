package com.tinymonster.stepcount;

import com.droi.sdk.core.DroiExpose;
import com.droi.sdk.core.DroiObject;

/**
 * Created by TinyMonster on 2018/5/18.
 */

public class pace extends DroiObject {
    @DroiExpose
    public String name;
    @DroiExpose
    public String time;
    @DroiExpose
    public String pacenum;
    public pace(){

    }
    public pace(String name,String time,String pacenum){
        this.name=name;
        this.pacenum=pacenum;
        this.time=time;
    }
}
