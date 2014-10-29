package com.spx.adb;

import java.util.Set;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

public class AdbTest {
	private IDevice device = null;
	public void init(String serial) {
		AndroidDebugBridge.init(false);
		device = DeviceUtil.getDevice(serial);
		System.out.println("device:" + device);
		
//		String output = Util.runAdbCmdGetReturn(device, "dumpsys meminfo", 2000, true);
//		System.out.println("output:" + output);
		
		//AndroidDebugBridge.disconnectBridge();
		
		//dumpAllThread();
		
		
	}
	
	public void startApp(){
		runCmd("am start -n com.example.yzc/.MainActivity");
		
	}
	
	public void testAppStartupTime(){
		AppStartupTimeTest appStartupTimeTest = new AppStartupTimeTest();
		appStartupTimeTest.perform(device);
		
	}
	
	
	
	private void runCmd(String cmd){
		String output = Util.runAdbCmdGetReturn(device, cmd, 2000, true);
		Log.d("output:" + output);
	}
	
	
	
	private void dumpAllThread(){
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for(Thread thread: threadArray){
			printThreadStack(thread);
		}
	}
	
	private void printThreadStack(Thread thread){
		System.out.println("----------------------");
		System.out.println("thread:"+thread.getName()+", isDaemon:"+thread.isDaemon());
//		thread.dumpStack();
		StackTraceElement[] stackTrace = thread.getStackTrace();
		for(StackTraceElement ste: stackTrace){
			System.out.println(ste.toString());
		}
	}

	

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AdbTest test = new AdbTest();
		test.init("2008edd8f316");
		test.testAppStartupTime();
		AndroidDebugBridge.terminate();
	}

}
