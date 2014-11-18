package com.spx.adb;

import java.util.List;

import com.android.ddmlib.IDevice;

public class Installer {
	
	private static Installer instance = new Installer();
	private Installer(){}
	
	public static Installer getInstance(){
		return instance;
	}
	
	public static void install(final String serial, String apkFile){
//	    List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" install -r "+apkFile);
	    new Thread(new Runnable(){
            @Override
            public void run() {
                Util.sleep(5000);
                IDevice device = DeviceUtil.createDevice(serial);
                ScreenUi sceenUi = ScreenUi.getScreenUiInstance(device);
                
                sceenUi.clickText("ȷ��");
                sceenUi.update();
                sceenUi.clickText("��װ");
            }
	    }).start();
	    boolean installApk = Util.installApk(serial, apkFile);
	    
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
