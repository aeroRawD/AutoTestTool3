package com.spx.adb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class Util {

	public static void runAdbCmd(IDevice device, String cmd,
			MultiLineReceiver receiver, long timeout) {
		try {
			//Log.d("cmd:"+cmd+", time:"+(new Date()));
			device.executeShellCommand(cmd, receiver, timeout,
					TimeUnit.MILLISECONDS);
			//Log.d("cmd finished, time:"+(new Date()));
		} catch (TimeoutException | AdbCommandRejectedException
				| ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 在adb shell中执行指定的命令cmd, 并等待timeout毫秒, 返回屏幕输出字符串
	 * 
	 * @param device
	 *            代表与手机的连接
	 * @param cmd
	 *            命令
	 * @param timeout
	 *            毫秒数
	 * @return 命令执行结果输出
	 */
	public static String runAdbCmdGetReturn(IDevice device, String cmd,
			int timeout) {
		return runAdbCmdGetReturn(device, cmd, timeout, false);
	}

	public static String runAdbCmdGetReturn(IDevice device, String cmd,
			int timeout, boolean rawout) {
		ConsoleOutputReceiver ishellReceiver = new ConsoleOutputReceiver();
		ishellReceiver.setOutputRaw(rawout);
		runAdbCmd(device, cmd, ishellReceiver, timeout);
		return ishellReceiver.getOutput();
	}

	private static final class ConsoleOutputReceiver extends MultiLineReceiver {
		StringBuilder sb = new StringBuilder();
		private boolean raw = false;

		public void setOutputRaw(boolean raw) {
			this.raw = raw;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				if (Util.isNull(line))
					continue;
				sb.append(line);
				if (raw) {
					sb.append("\r\n");
				}
			}
		}

		public String getOutput() {
			return sb.toString();
		}
	}

	/**
	 * 执行系统本身的命令, 比如ls, del, 或者adb shell ps
	 */
	public static void runOsCmd() {
		
	}
	
	public static List<String> getCmdOutput(String cmds){
		java.util.StringTokenizer st = new StringTokenizer(cmds, " ");
		String[] cmdArray = cmds.split(" ");
		return getCmdOutput(cmdArray);
	}
	
	public static List<String> getCmdOutput(String[] cmds){
		List<String> ret = new ArrayList<String>();
		Process process = null;
		InputStream is = null;
		InputStream eis = null;
		BufferedReader dis = null;
		BufferedReader edis = null;
		String line = "";
		Runtime runtime = Runtime.getRuntime();
		try {
			process = runtime.exec(cmds);
			is = process.getInputStream();
			eis = process.getErrorStream();
			dis = new BufferedReader(new InputStreamReader(is));
			while ((line = dis.readLine()) != null) {
				//Log.d("Log","Log.Bestpay:"+line);	
				//sb.append(line+"\r\n");
				ret.add(line+"\r\n");
			}
			
			edis= new BufferedReader(new InputStreamReader(eis));
			while ((line = edis.readLine()) != null) {
				//Log.d("Log","Log.Bestpay:"+line);	
				//sb.append(line+"\r\n");
				ret.add(line+"\r\n");
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				if (dis != null) {
					dis.close();
				}
				if (is != null) {
					is.close();
				}
				if(eis!=null){
					eis.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	/**
	 * 检查判断line是否是空字符串, 包括NULL内容的字符串
	 * @param line
	 * @return
	 */
	public static boolean isNull(String line) {
		if (line == null || line.trim().length() == 0
				|| line.trim().equalsIgnoreCase("NULL"))
			return true;
		return false;
	}
	
	/**
	 * 线程休眠一段时间time
	 * @param time
	 */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
