package com.spx.adb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.log.Log;

public class AdbTest {
	private IDevice device = null;
	public void init(String serial) {
		

		AndroidDebugBridge.init(false);
		device = DeviceUtil.createDevice(serial);
		System.out.println("device:" + device);
		if(device ==null){
			throw new RuntimeException("failed to get device");
		}
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

	public void clickText(String text){
		ScreenUi screenUi = ScreenUi.getScreenUiInstance(device);
		screenUi.clickText(text);
	}
	
//	public void dumpUi(){
//		ScreenUi.getUiDump(device, "data/uidump.xml");
//	}
//	
//	public void parseUiDump(){
//		List<UiNode> parseUiDump = ScreenUi.parseUiDump("data/uidump.xml");
//		for(UiNode node:parseUiDump){
//			Log.d("package:"+node.getValue("package")+", class:"+node.getValue("class")+", text:"+node.getValue("text")+",bounds:"+node.getValue("bounds"));
//		}
//	}
	FileOutputStream fos = null;
	IShellOutputReceiver receiver = new IShellOutputReceiver(){
		
		@Override
		public void addOutput(byte[] data, int offset, int length) {
			Log.d("addOutput offset:"+offset+", length:"+length);
			try {
				fos.write(data, offset, length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void flush() {
			Log.d("flush ");
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
		
	};
	public void testRecord(){
		
		try {
			fos = new FileOutputStream(new File("d:/x.mp4"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			device.startScreenRecorder("/sdcard/a.mp4", new ScreenRecorderOptions.Builder().setSize(800, 600).setBitRate(8000000).setTimeLimit(60, TimeUnit.SECONDS).build(), receiver);
		} catch (TimeoutException | AdbCommandRejectedException | IOException
				| ShellCommandUnresponsiveException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.d("start");
		AdbTest test = new AdbTest();
		test.init("2008edd8f316");
		Log.d("midle");
//		test.dumpUi();
//		Log.d("after dump");
//		test.parseUiDump();
//		test.clickText("主题风格");
//		test.testAppStartupTime();
		test.testRecord();
		AndroidDebugBridge.terminate();
		Log.d("end");
	}

}
