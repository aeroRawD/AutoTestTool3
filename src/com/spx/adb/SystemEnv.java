package com.spx.adb;

import java.util.HashMap;
import java.util.List;

public class SystemEnv {
    
    public static final String admin = "shaopengxiang@kingsoft.com";
    
    private static String user = "shaopengxiang";
    private static String pwd ="spx";
    public static String loopStartTime = "09:00";
    public static String loopEndTime = "21:00";
    
    
    public static String priority = "all";
    public static String runMode = "test";
    
    public static String ant ="D:/Dev/Ant/apache-ant-1.9.4/bin/ant.bat";
    public static String lint ="D:/Android/adt-bundle-windows-x86_64-20140702/sdk/tools/lint.bat";
    
    public static final String APP_PACKAGE_NAME="com.kingsoft";
    public static final String TESTAPP_PACKAGE_NAME="com.kingsoft.test";
    
    public static final String APP_PROJECT_URL = "http://trac.s.iciba.com/svn/ciba-mobile/android/Powerword7";
    public static String APP_PROJECT_PATH = "D:/data/powerword7";
    
    public static final String TESTAPP_PROJECT_URL = "http://trac.s.iciba.com/svn/ciba-mobile/android/Testing/TestPowerword7";
    public static String TESTAPP_PROJECT_PATH = "D:/data/test";
    
    public static int startUpTimeTestCount = 20;
    
    public static int MAX_TEST_COUNT_PER_LOOP = 400;
    
    static {
        List<String> fileContentLines = Util
                .getFileContentLines("local.properties");
        String username = "shaopengxiang";
        String password = "spx";
        for (String line : fileContentLines) {
            if (line.startsWith("mail.user=")) {
                username = line.substring("mail.user=".length());
            }
            if (line.startsWith("mail.user.password=")) {
                password = line.substring("mail.user.password=".length());
            }
            if (line.startsWith("loop.start.time=")) {
                loopStartTime = line.substring("loop.start.time=".length());
            }
            if (line.startsWith("loop.end.time=")) {
                loopEndTime = line.substring("loop.end.time=".length());
            }
            if (line.startsWith("testcase.priority=")) {
                priority = line.substring("testcase.priority=".length());
            }
            if (line.startsWith("run.mode=")) {
                runMode = line.substring("run.mode=".length());
            }
            if (line.startsWith("ant=")) {
                ant = line.substring("ant=".length());
            }
            if (line.startsWith("lint=")) {
                lint = line.substring("lint=".length());
            }
            if (line.startsWith("app.project.path=")) {
                APP_PROJECT_PATH = line.substring("app.project.path=".length());
            }
            if (line.startsWith("test.project.path=")) {
                TESTAPP_PROJECT_PATH = line.substring("test.project.path=".length());
            }
            if (line.startsWith("startuptime.test.count=")) {
                String count = line.substring("startuptime.test.count=".length());
                try{
                    startUpTimeTestCount = Integer.parseInt(count);
                }catch(Exception ex){}
            }
            if (line.startsWith("max.test.count.perloop=")) {
                String count = line.substring("max.test.count.perloop=".length());
                try{
                    MAX_TEST_COUNT_PER_LOOP = Integer.parseInt(count);
                }catch(Exception ex){}
            }
        }
        pwd = password;
        user = username;
    }
    
    public static boolean isTestingMode(){
        if(runMode.equals("test")) return true;
        return false;
    }
    
    public static String getPassword(){
        return pwd;
    }
    
    public static String getUser(){
        return user;
    }
    
    
    static{
        
    }
    
	
	
	public static HashMap<String, String> urlMap = new HashMap<String, String>();
	static {
		urlMap.put(APP_PROJECT_URL, APP_PROJECT_PATH);
		urlMap.put(TESTAPP_PROJECT_URL, TESTAPP_PROJECT_PATH);
	}
	
	public static HashMap<String, String> pathMap = new HashMap<String, String>();
	static {
		pathMap.put(APP_PROJECT_PATH, APP_PROJECT_URL);
		pathMap.put(TESTAPP_PROJECT_PATH, TESTAPP_PROJECT_URL);
	}
	
	public static String getUrlForLocalpath(String localpath){
		return pathMap.get(localpath);
	}
	
	public static String getLocalPathForUrl(String url){
		return urlMap.get(url);
	}
	
	public static HashMap<String, String> userMap = new HashMap<String, String>();
	static {
	    userMap.put("shaopx", "shaopengxiang@kingsoft.com");
	    userMap.put("guoqin", "GuoQin@kingsoft.com");
	    userMap.put("shaopx", "GaoXiang1@kingsoft.com");
	    userMap.put("taohx", "TAOHONGXIA@kingsoft.com");
	}
}
