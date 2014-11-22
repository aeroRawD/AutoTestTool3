package com.spx.adb;

import java.util.List;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

public class Installer {
	
	private static Installer instance = new Installer();
	private Installer(){}
	
	public static Installer getInstance(){
		return instance;
	}
	
	public static void uninstall(final String serial, String packageName){
	    Util.removePackage(serial, packageName);
	}
	
	public static void install(final String serial, String apkFile){
//	    List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" install -r "+apkFile);
	    new Thread(new Runnable(){
            @Override
            public void run() {
                Util.sleep(5000);
                IDevice device = DeviceUtil.createDevice(serial);
                ScreenUi sceenUi = ScreenUi.getScreenUiInstance(device);
                
                sceenUi.clickText("确定");
                sceenUi.update();
                sceenUi.clickText("安装");
                sceenUi.update();
                sceenUi.clickText2("清理");
            }
	    }).start();
	    boolean installApk = Util.installApk(serial, apkFile);
	    
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    AndroidDebugBridge.init(false);
	    Installer.uninstall("1298b223","com.kingsoft");
	    Installer.install("1298b223", "E:/workspace/Powerword7/bin/Powerword7.apk");
	    
	}

}
