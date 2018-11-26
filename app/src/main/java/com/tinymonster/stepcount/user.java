package com.tinymonster.stepcount;

import com.droi.sdk.core.DroiExpose;
import com.droi.sdk.core.DroiObject;

/**
 * Created by TinyMonster on 2018/5/18.
 */

public class user extends DroiObject {
    @DroiExpose
    public String name;
    @DroiExpose
    public String pwd;
    public user(){
    }
    public user(String Name,String Pwd){
        this.name=Name;
        this.pwd=Pwd;
    }
}
