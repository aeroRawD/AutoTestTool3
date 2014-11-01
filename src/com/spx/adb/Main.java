package com.spx.adb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	public void run() {
		//启动设备监控
		DeviceMonitor deviceMonitor = DeviceMonitor.getInstance();
		deviceMonitor.start();
		
		// 启动SVN监控
		SvnMonitor svnMonitor = SvnMonitor.getInstance();
		svnMonitor.start();
		
		
	}

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
}
