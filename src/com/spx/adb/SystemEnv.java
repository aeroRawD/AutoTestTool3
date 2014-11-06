package com.spx.adb;

import java.util.HashMap;

public class SystemEnv {
    public static final String user = "shaopengxiang";
    public static final String pwd = "hcPoIUIPLf";
	public static final String ant ="D:/Dev/Ant/apache-ant-1.9.4/bin/ant.bat";
	
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
}
