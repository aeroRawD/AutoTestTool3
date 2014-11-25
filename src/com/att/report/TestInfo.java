package com.att.report;

import java.util.Iterator;
import java.util.Set;

import com.att.build.DailyLintChecker;
import com.spx.adb.SystemEnv;

public class TestInfo {
    private int useCaseCount;
    private int useCaseFailCount;
    private int wifiUseCaseCount;
    private int wifiUseCaseFailCount;
    
    private String resolution;
    private String testduration;
    private String androidOsBuild;
    private String phoneName;
    private String phoneCpu;
    private String testStartTime;
    private int startUpTime;
    
    private int codeLines;
    private int apkSize;
    private int lintWaringCount;
    private int findbugsWaringCount;
    private String coverage;
    private String phoneMemSize;
    private int maxHeapAlloc = 0;
    private String serial;
    
    public TestInfo(String s){
        serial = s;
    }
    
    public int getMaxHeapAlloc() {
        return maxHeapAlloc;
    }
    public void setMaxHeapAlloc(int maxHeapAlloc) {
        this.maxHeapAlloc = maxHeapAlloc;
    }
    public String getPhoneMemSize() {
        return phoneMemSize;
    }
    public void setPhoneMemSize(String phoneMemSize) {
        this.phoneMemSize = phoneMemSize;
    }
//    public String getCoverage() {
//        return coverage;
//    }
//    public void setCoverage(String coverage) {
//        this.coverage = coverage;
//    }
    public int getUseCaseCount() {
        return useCaseCount;
    }
    public void setUseCaseCount(int useCaseCount) {
        this.useCaseCount = useCaseCount;
    }
    public int getUseCaseFailCount() {
        return useCaseFailCount;
    }
    public void setUseCaseFailCount(int useCaseFailCount) {
        this.useCaseFailCount = useCaseFailCount;
    }
    public int getWifiUseCaseCount() {
        return wifiUseCaseCount;
    }
    public void setWifiUseCaseCount(int wifiUseCaseCount) {
        this.wifiUseCaseCount = wifiUseCaseCount;
    }
    public int getWifiUseCaseFailCount() {
        return wifiUseCaseFailCount;
    }
    public void setWifiUseCaseFailCount(int wifiUseCaseFailCount) {
        this.wifiUseCaseFailCount = wifiUseCaseFailCount;
    }
    public String getResolution() {
        return resolution;
    }
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    public String getTestduration() {
        return testduration;
    }
    public void setTestduration(String testduration) {
        this.testduration = testduration;
    }
    public String getAndroidOsBuild() {
        return androidOsBuild;
    }
    public void setAndroidOsBuild(String androidOsBuild) {
        this.androidOsBuild = androidOsBuild;
    }
    public String getPhoneName() {
        return phoneName;
    }
    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }
    public String getPhoneCpu() {
        return phoneCpu;
    }
    public void setPhoneCpu(String phoneCpu) {
        this.phoneCpu = phoneCpu;
    }
    public String getTestStartTime() {
        return testStartTime;
    }
    public void setTestStartTime(String testStartTime) {
        this.testStartTime = testStartTime;
    }
    public int getCodeLines() {
        return codeLines;
    }
    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }
    public int getApkSize() {
        return apkSize;
    }
    public void setApkSize(int apkSize) {
        this.apkSize = apkSize;
    }
    public int getLintWaringCount() {
        return lintWaringCount;
    }
    public void setLintWaringCount(int lintWaringCount) {
        this.lintWaringCount = lintWaringCount;
    }
    public int getFindbugsWaringCount() {
        return findbugsWaringCount;
    }
    public void setFindbugsWaringCount(int findbugsWaringCount) {
        this.findbugsWaringCount = findbugsWaringCount;
    }
    
    
    
//    public String replaceTags(String html, String tag , String value) {
//        Set<String> keys = data.keySet();
//        Iterator<String> keyIt = keys.iterator();
//        while(keyIt.hasNext()){
//            String key=keyIt.next();
//            html = html.replace("{" + key + "}",getValue(key));
//        }
//        return html;
//    }
    
    public int getStartUpTime() {
        return startUpTime;
    }

    public void setStartUpTime(int startUpTime) {
        this.startUpTime = startUpTime;
    }

    public String getCoverage(){
        int total = 500;//这个是手工用例的数量
        int cases =getUseCaseCount();
        return ""+(cases*100/total);
    }
    
    public String replaceTags(String html) {
        html = html.replace("{testing_time}",this.getTestStartTime());
        html = html.replace("{testing_duration}",this.getTestduration());
        html = html.replace("{testing_coverage}",getCoverage());
        html = html.replace("{usecase_fail}",this.getUseCaseFailCount()+"");
        html = html.replace("{usecase_all}",this.getUseCaseCount()+"");
        html = html.replace("{usecase_wifi_count}",this.getWifiUseCaseCount()+"");
        html = html.replace("{usecase_nowifi_count}",(getUseCaseCount()-getWifiUseCaseCount())+"");
        html = html.replace("{phone_name}",this.getPhoneName());
        html = html.replace("{androidos_build}",this.getAndroidOsBuild());
        html = html.replace("{lint_warnings}",DailyLintChecker.getCurrentRevWarnings(SystemEnv.APP_PROJECT_PATH));
        html = html.replace("{cpu_name}",this.getPhoneCpu());
        html = html.replace("{startup.time.avg}",this.getStartUpTime()+"");
        
        DeviceMemInfo dmi = new DeviceMemInfo("testreport/"+serial+"/meminfo.txt");
        html = html.replace("{MemTotal}",dmi.getTotalMem());
        //html = html.replace("{androidos_build}",this.getAndroidOsBuild());
        return html;
    }
    
    
    
}
