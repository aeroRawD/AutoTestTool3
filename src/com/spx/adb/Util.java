package com.spx.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.log.Log;

public class Util {
	static Logger logger = Log.getSlientLogger(Util.class.getSimpleName());
	public static void runAdbCmd(IDevice device, String cmd,
			IShellOutputReceiver receiver, long timeout) {
	    if(device==null){
	        logger.severe("device is null when run cmd:"+cmd);
	        return;
	    }
		try {
			logger.fine("cmd:"+cmd);
			device.executeShellCommand(cmd, receiver, timeout,
					TimeUnit.MILLISECONDS);
			//Log.d("cmd finished, time:"+(new Date()));
		} catch (Exception e) {
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
			dis = new BufferedReader(new InputStreamReader(is, "gbk"));
			while ((line = dis.readLine()) != null) {
				//Log.d("Log","输出:"+line);
				logger.fine(line);
				//sb.append(line+"\r\n");
				ret.add(line+"\r\n");
			}
			
			edis= new BufferedReader(new InputStreamReader(eis, "gbk"));
			while ((line = edis.readLine()) != null) {
				//Log.d("Log","Log.Bestpay:"+line);	
				//sb.append(line+"\r\n");
				logger.severe(line);
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
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					//System.out.println(bytesum);
					//logger.info(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				
				if(file[i].contains(".svn")){
					continue;
				}
				
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();

		}

	}
	
	public static void makeDir(String path){
	    File distF = new File(path);
        if(!distF.exists()){
            distF.mkdirs();
        }
	}
	
	public static void createFile(String distFile, String content){
//		try{
//			PrintWriter pw = new PrintWriter(new File(testProject+"/ant.properties"));
//			pw.println("tested.project.dir=d:/data/powerword7");
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
		
		try{
		    String path = distFile.substring(0, distFile.lastIndexOf("/"));
		    makeDir(path);
		    
		    File distF = new File(distFile);
		    if(!distF.exists()){
		        distF.createNewFile();
		    }
		    
			PrintWriter pw = new PrintWriter(distF);
			pw.println(content);
			pw.flush();
			pw.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static boolean isFileExist(String filePath){
		File f = new File(filePath);
		return f.exists();
	}
	
	public static boolean deleteFile(String filePath){
		File f = new File(filePath);
		return f.delete();
	}
	
	/**
	 * 判断应用是否被安装了
	 * @param device
	 * @param packageName
	 * @return
	 */
	public static boolean isApkInstalled(IDevice device, String packageName) {
		String output = runAdbCmdGetReturn(device, "pm list packages",
				4000);
		return output.contains(packageName);
	}
	
	/**
	 * 安装
	 * @param device
	 * @param apkFileName
	 * @return
	 */
	public static boolean installApk(IDevice device, String apkFileName){
		String returnStr;
		try {
			returnStr = device.installPackage(apkFileName, true);
		} catch (InstallException e) {
			e.printStackTrace();
			return false;
		}
		return Util.isNull(returnStr);
	}
	
	/**
	 * 从手机中pull 文件
	 * @param device
	 * @param remoteFile
	 * @param localFile
	 * @return
	 */
	public static boolean pullFile(IDevice device, String remoteFile, String localFile){
		try {
			device.pullFile(remoteFile, localFile);
		} catch (SyncException | IOException | AdbCommandRejectedException
				| TimeoutException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 根据日期格式返回当前时间的时间字符串
	 * @param format
	 * @return
	 */
	public static String getTimeStr(String format) {
        Date now = new Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
        return sdf.format(now);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
