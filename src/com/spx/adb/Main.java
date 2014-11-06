package com.spx.adb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.ddmlib.AndroidDebugBridge;
import com.att.build.ApplicationBuilder;
import com.att.svn.SvnMonitor;
import com.log.Log;

public class Main {
	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	ApplicationBuilder appBuiler = null;
	public void Main(){
		Log.setup();
		init();
	}

	public void run() {
		startMonitors();
	}

	private void init() {
		AndroidDebugBridge.init(false);
		
		appBuiler = ApplicationBuilder.getInstance();
	}

	private void startMonitors() {

		startDeviceMonitor();
		startSourceControlMonitor();
		
	}

	private void startDeviceMonitor() {
		// 启动设备监控
		DeviceMonitor deviceMonitor = DeviceMonitor.getInstance();
		deviceMonitor.start();
	}

	private void startSourceControlMonitor() {
		// 启动SVN监控
		SvnMonitor svnMonitor = SvnMonitor.getInstance();
		svnMonitor.addWatchedUrl(SystemEnv.APP_PROJECT_URL);
		svnMonitor.addWatchedUrl(SystemEnv.TESTAPP_PROJECT_URL);
		svnMonitor.setListener(appBuiler);
		svnMonitor.start();
	}

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
}
