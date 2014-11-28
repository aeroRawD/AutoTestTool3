package com.att.build;

import com.spx.adb.Util;

public class ScreenRecorder extends Thread{
    private String serial;
    private String file;
    private int recordtime = 60;
    public ScreenRecorder(String s, int seconds, String file){
        this.serial =s;
        this.file = file;
        recordtime =  seconds;
    }
    
    public void run(){
        Util.getCmdOutput("adb -s "+serial+" shell mkdir /sdcard/powerword/rec");
        Util.getCmdOutput("adb -s "+serial+" shell screenrecord --bit-rate 4000000 --time-limit "+recordtime+" /sdcard/powerword/rec/"+file);
    }
}
