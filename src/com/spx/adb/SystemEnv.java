package com.spx.adb;

import java.util.HashMap;
import java.util.List;

public class SystemEnv {
    
    public static final String admin = "shaopengxiang@kingsoft.com";
    
    private static String user = "shaopengxiang";
    private static String pwd ="spx";
    public static String loopStartTime = "09:00";
    public static String loopEndTime = "19:57";
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
        }
        pwd = password;
        user = username;
    }
    
    public static String getPassword(){
        return pwd;
    }
    
    public static String getUser(){
        return user;
    }
    
    
    static{
        
    }
    
	public static final String ant ="D:/Dev/Ant/apache-ant-1.9.4/bin/ant.bat";
	public static final String lint ="D:/Android/adt-bundle-windows-x86_64-20140702/sdk/tools/lint.bat";
	
	public static final String APP_PACKAGE_NAME="com.kingsoft";
	public static final String TESTAPP_PACKAGE_NAME="com.kingsoft.test";
	
	public static final String APP_PROJECT_URL = "http://trac.s.iciba.com/svn/ciba-mobile/android/Powerword7";
	public static final String APP_PROJECT_PATH = "D:/data/powerword7";
	
	public static final String TESTAPP_PROJECT_URL = "http://trac.s.iciba.com/svn/ciba-mobile/android/Testing/TestPowerword7";
	public static final String TESTAPP_PROJECT_PATH = "D:/data/test";
	
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
