package com.att.report;

import java.util.ArrayList;
import java.util.List;

import com.spx.adb.Util;

public class DeviceMemInfo {
    private List<String> data = new ArrayList<String>();

    public DeviceMemInfo(String fileName) {
        if(Util.isFileExist(fileName))
            data = Util.getFileContentLines(fileName);
    }

    public String getTotalMem() {
        for(int i=0;i<data.size();i++){
            String line = data.get(i);
            if(Util.isNull(line)) continue;
            if(line.startsWith("MemTotal: ")){
                return line.substring(line.indexOf(":")+1).trim();
            }
        }
        
        return "unknown";
    }
    
    public static void main(String[] args){
        DeviceMemInfo dmi = new DeviceMemInfo("testreport/2008edd8f316/meminfo.txt");
        System.out.println("total:"+dmi.getTotalMem());
    }
}
