package com.att.report;

import java.util.HashMap;
import java.util.List;

import com.android.ddmlib.IDevice;
import com.spx.adb.DeviceUtil;
import com.spx.adb.Util;

public class DeviceProp {
    private String serial = null;
    private HashMap<String,String> data = new HashMap<String,String>();
    
    public DeviceProp(String serial){
        this.serial = serial;
        init();
    }
    
    private void init(){
        //先判断prop.txt是否存在
        if(Util.isFileExist("testreport/"+serial+"/prop.txt")){
            List<String> fileContentLines = Util.getFileContentLines("testreport/"+serial+"/prop.txt");
            if(fileContentLines!=null && fileContentLines.size()>10){
                init(fileContentLines);
                return;
            }
        }
        
        IDevice device = DeviceUtil.createDevice(serial);
        if(device!=null && device.isOnline()){
            init(device);
            return;
        }
        
        initFromHistoryData(serial);
    }
    
    private void initFromHistoryData(String serial) {
        List<String> fileContentLines = Util.getFileContentLines("data/phoneinfo/"+serial+"_prop.txt");
        if(fileContentLines!=null && fileContentLines.size()>3){
            init(fileContentLines);
            return;
        }
    }

    private void init(List<String> fileContentLines){
        for(String s:fileContentLines){
            if(Util.isNull(s)) continue;
            if(s.indexOf(":")==-1) continue;
            String key = s.substring(0, s.indexOf(":"));
            String value = s.substring(s.indexOf(":")+1);
            if(Util.isNull(key)) continue;
            key = format(key);
            value = format(value);
            data.put(key, value);
        }
    }
    
    private String format(String s){
        if(Util.isNull(s)) return "";
        s = s.trim();
        if(s.startsWith("[")) s=s.substring(1);
        s = s.trim();
        if(s.endsWith("]"))s=s.substring(0, s.length()-1);
        return s;
    }
    
    private void init(IDevice device){
        data.put("ro.build.version.release", device.getProperty("ro.build.version.release"));
        data.put("ro.build.user", device.getProperty("ro.build.user"));
        data.put("ro.product.cpu.abi", device.getProperty("ro.product.cpu.abi"));
        
        data.put("ro.product.model", device.getProperty("ro.product.model"));
        data.put("ro.product.name", device.getProperty("ro.product.name"));
    }
    
    public String getValue(String key){
        return data.get(key);
    }
}
