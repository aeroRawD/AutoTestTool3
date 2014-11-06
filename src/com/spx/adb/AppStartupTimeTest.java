package com.spx.adb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.log.Log;

/**
 * 测试应用平均启动时间
 * 
 * @author SHAOPENGXIANG
 */
public class AppStartupTimeTest {
	private IDevice device = null;
	private static final int MAX_TEST_COUNT = 6;
//	private int windowWidth = 600, windowHeight = 800; 
	private int[] loc=new int[2];
	public void perform(IDevice device) {
		this.device = device;
		
		startPowerword();
		loc = ScreenUi.getScreenUiInstance(device).getScreenLocation("退出");
		if(loc==null)
			return;
		
		System.out.println("location:  x:"+loc[0]+",y:"+loc[1]);
//		try {
//			RawImage screenshot = device.getScreenshot();
//			windowWidth = screenshot.width;
//			windowHeight = screenshot.height;
//		} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
//			e.printStackTrace();
//		}
//		Log.d("windowWidth:" + windowWidth + ",windowHeight:" + windowHeight);
		
		runCmd("logcat -c");

		LogcatReceiver receiver = new LogcatReceiver();
		Thread th = new Thread(receiver);
		th.start();

		for (int i = 0; i < MAX_TEST_COUNT; i++) {
			startPowerword();
			Util.sleep(4000);
			quitPowerword();
			Util.sleep(1000);
		}
		int startUpTimeAvg = receiver.getStartupTimeAvg();
		Log.d("平均启动时间:" + startUpTimeAvg);
		receiver.stop();
		th.interrupt();
		
		saveProp(startUpTimeAvg);
	}

	private void saveProp(int avgTime) {
		try {
			File testreportFile = new File("testreport");
			if (!testreportFile.exists()) {
				testreportFile.mkdirs();
			}
			File propFile = new File("testreport/prop.txt");
			if (!propFile.exists()) {
				propFile.createNewFile();
			}
			
			PrintWriter pw = new PrintWriter(new FileWriter(propFile, true));
			pw.write("[startuptime]: ["+avgTime+"ms]\r\n");
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startPowerword() {
		runCmd("am start -n com.kingsoft/com.kingsoft.StartActivity");
	}

	private void quitPowerword() {
		
		runCmd("input tap "+loc[0]+" "+loc[1]);
	}

	private void runCmd(String cmd) {
		String output = Util.runAdbCmdGetReturn(device, cmd, 2000, true);
		// Log.d("output:" + output);
	}

	private final static class StartUpTime {
		long start, end, used;
	}

	private final class LogcatReceiver extends MultiLineReceiver implements
			Runnable {
		boolean isCancelled = false;
		static final String TAG_START_1 = "act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER";
		static final String TAG_START_2 = "cmp=com.kingsoft/.StartActivity";

		static final String TAG_END = "Displayed com.kingsoft/.Main:";
		private boolean start = true;
		private long startTime = -1, endTime = -1;
		private ArrayList<StartUpTime> usedTimeList = new ArrayList<StartUpTime>();
		private int count = 0;
		private long accu = 0L;

		@Override
		public boolean isCancelled() {
			return isCancelled;
		}

		public void stop() {
			isCancelled = true;
		}

		@Override
		public void processNewLines(String[] lines) {

			for (String line : lines) {
				// if (line.contains("ActivityManager"))
				// Log.d("processNewLines() ..." + line);
				if (line.contains("ActivityManager") && line.contains("START")
						&& line.contains("com.kingsoft/.StartActivity")
						&& start) {
					// Log.d("start line:" + line);
					startTime = getTimeFromLogcatLine(line);
					start = false;
					Log.d(line + "\t" + startTime);
					continue;
				}

				if (line.contains(TAG_END) && !start) {
					// Log.d("end line:" + line);
					start = true;
					endTime = getTimeFromLogcatLine(line);
					Log.d(line + "\t" + endTime);
					if (startTime != -1 && endTime != -1 && endTime > startTime) {
						long timeUsed = endTime - startTime;

						// 如果用时超过10s, 也认为是错误数据
						if (timeUsed < 10 * 1000) {
							accu += timeUsed;
							Log.d((count++) + ".  time used:" + timeUsed + "ms"
									+ ",   avg:" + (accu / count));

							StartUpTime sut = new StartUpTime();
							sut.start = startTime;
							sut.end = endTime;
							sut.used = timeUsed;
							usedTimeList.add(sut);
						}

					}
					startTime = -1;
					endTime = -1;
				}
			}
		}

		private long getTimeFromLogcatLine(String line) {
			String timeStr = line.substring(0, 18);
			timeStr = "2014-" + timeStr;
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS");
			Date time;
			try {
				time = sdf.parse(timeStr);
			} catch (ParseException e) {
				e.printStackTrace();
				return -1;
			}
			return time.getTime();
		}

		public int getStartupTimeAvg() {
			long times = 0L;
			long maxTime = 0L;
			long minTime = Long.MAX_VALUE;
			for (StartUpTime sut : usedTimeList) {
				times += sut.used;
				if (sut.used > maxTime) {
					maxTime = sut.used;
				}
				if (sut.used < minTime) {
					minTime = sut.used;
				}
			}

			// 去除最大和最小值
			times = times - maxTime - minTime;

			return (int) (times / (usedTimeList.size() - 2));
		}

		@Override
		public void run() {
			Log.d("run() ....");
			try {
				device.executeShellCommand("logcat -v threadtime", this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
