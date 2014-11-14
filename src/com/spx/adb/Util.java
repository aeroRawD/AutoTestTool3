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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.log.Log;
import com.spx.adb.DeviceMonitor.Device;

public class Util {
	static Logger logger = Log.getSlientLogger(Util.class.getSimpleName());
	public static void runAdbCmd(IDevice device, String cmd,
			IShellOutputReceiver receiver, long timeout) {
	    if(device==null){
	        logger.severe("device is null when run cmd:"+cmd);
	        return;
	    }
	    int tryTimes = 3;
	    while(tryTimes>0){
	        try {
	            //logger.info("cmd:"+cmd);
	            device.executeShellCommand(cmd, receiver, timeout,
	                    TimeUnit.MILLISECONDS);
	            System.out.println("cmd finished, time:"+(new Date()));
	            return ;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        tryTimes -- ;
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
	
	public static List<String> getCmdOutput(String cmds, boolean silent){
	    java.util.StringTokenizer st = new StringTokenizer(cmds, " ");
        String[] cmdArray = cmds.split(" ");
        return getCmdOutput(cmdArray, silent);
	}
	
	public static List<String> getCmdOutput(String cmds){
		java.util.StringTokenizer st = new StringTokenizer(cmds, " ");
		String[] cmdArray = cmds.split(" ");
		return getCmdOutput(cmdArray, false);
	}
	
	public static List<String> getCmdOutput(String[] cmds, boolean silent){
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
			    if(line.contains("KB/s") || line.contains("BT INFO: 2.2")|| Util.isNull(line)){
			        
			    }else {
			        if(!silent)
			            logger.info(line);
			        
			        ret.add(line+"\r\n");
			    }
				
				//sb.append(line+"\r\n");
				
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
	
	public static void createFile(String distFile, List<String> content){
	    StringBuilder sb = new StringBuilder();
        for(String s:content){
            if(!Util.isNull(s)) sb.append(s+"\r\n");
        }
        createFile(distFile, sb.toString());
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
	
	public static void cleanDirectroy(String path){
	    try{
	        Util.deleteFile(path);
	        Util.makeDir(path);
	    }catch(Exception ex){ex.printStackTrace();}
	    
	    File f = new File(path);
	    if(Util.isFileExist(path)){
	        String[] childs = f.list();
	        if(childs!=null){
	            for(String fp: childs){
	                try{
	                    Util.deleteFile(path+"/"+fp);
	                }catch(Exception exx){
	                    exx.printStackTrace();
	                };
	            }
	        }
	    }
	    
	}
	
	public static boolean deleteFile(String filePath){
		File f = new File(filePath);
		int tryTimes = 3;
		while(f.exists() && tryTimes>0){
		    if(f.delete()) break;
		    tryTimes--;
		}
		return !f.exists();
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
	public static boolean installApk(IDevice device, String apkFileName, String packageName){
	    if(!removePackage(device, packageName)){
	        logger.log(Level.INFO, "卸载 "+ packageName+" 失败");
        }else {
            logger.log(Level.INFO, "卸载 "+ packageName+" 成功");
        }
        return installPackage(device, apkFileName);
	}
	
	public static boolean installApk(String serial, String apkFileName, String packageName){
        if(!removePackage(serial, packageName)){
            logger.log(Level.INFO, "卸载 "+ packageName+" 失败");
        }else {
            logger.log(Level.INFO, "卸载 "+ packageName+" 成功");
        }
        return installApk(serial, apkFileName);
    }
	
	public static boolean installApk(String serial, String path){
	    List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" install -r "+path);
	    //List<String> cmdOutput = Util.getCmdOutput("adb -s 7148000200000001 install -r D:/data/test/bin/Powerword7Test-debug.apk");
	    logger.log(Level.INFO, "安装apk"+path+" to ["+serial+"]");
	    for(String s:cmdOutput){
	        logger.log(Level.INFO, s);
	    }
	    sleep(2000);
        return true;
	}
	
	    public static boolean installPackage(IDevice device, String path) {
	        try {
	            String result = device.installPackage(path, true);
	            if (result != null) {
	                logger.log(Level.SEVERE, "Got error installing package: "+ result);
	                return false;
	            }
	            return true;
	        } catch (InstallException e) {
	            e.printStackTrace();
	            logger.log(Level.SEVERE, "Error installing package: " + path, e);
	            return false;
	        }
	    }
	    public static boolean removePackage(String serial, String packageName) {
	        Util.getCmdOutput("adb -s "+serial+" uninstall "+packageName);
	        return true;
	    }
	    public static boolean removePackage(IDevice device, String packageName) {
	        try {
	            String result = device.uninstallPackage(packageName);
	            if (result != null) {
	                logger.log(Level.SEVERE, "Got error uninstalling package "+ packageName + ": " +
	                        result);
	                return false;
	            }
	            return true;
	        } catch (InstallException e) {
	            logger.log(Level.SEVERE, "Error installing package: " + packageName, e);
	            return false;
	        }
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
     * get file content into a string, only for little file
     * 
     * @param file
     * @return
     */
    public static String getFileContent(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            // BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getFileContent(String fileName) {
        try {
            File file = new File(fileName);
            return getFileContent(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";

    }
    
    public static List<String> installPowerwordAppToAllOnlineDevices(){
        return installApkToAllOnlineDevices(SystemEnv.APP_PACKAGE_NAME, Builder.getInstance().getAppApkFileName());
    }
    
    public static List<String> installTestAppToAllOnlineDevices(){
        return installApkToAllOnlineDevices(SystemEnv.TESTAPP_PACKAGE_NAME, Builder.getInstance().getTestAppApkFileName());
    }
    
    /**
     * 安装应用apk, 返回安装成功的设备序列号
     * @param pacakgeName
     * @param localApkPath
     * @return
     */
    public static List<String> installApkToAllOnlineDevices(String pacakgeName, String localApkPath){
        //installSucceedDeviceList.clear();
        List<String> installedDevices = new ArrayList<String>();
        logger.info("ready to install apk:"+localApkPath);
        List<IDevice> onlineDevices = DeviceMonitor.getInstance().getOnlineDevices();
        logger.info("online device list size:"+onlineDevices.size());
        for(int i=0;i<onlineDevices.size();i++){
            IDevice device = onlineDevices.get(i);
            
            logger.info("install apk to device:"+device.getName());
            try {
                //device.installPackage(localApkPath, true);
                if(Util.installApk(device, localApkPath, pacakgeName)){
                    logger.info("install finish");
                }
                if(Util.isApkInstalled(device, pacakgeName)){
                    logger.info("install succeed!");
                    installedDevices.add(device.getSerialNumber());
                }else{
                    logger.info("install failed!");
                    Util.installApk(device.getSerialNumber(), localApkPath);
                    if(Util.isApkInstalled(device, pacakgeName)){
                        logger.info("install succeed!");
                        installedDevices.add(device.getSerialNumber());
                    }else{
                        logger.info("try install again failed!");
                    }
                }
                
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return installedDevices;
    }

    /**
     * 返回文件的内容, 以列表的格式, 每行文本作为列表的一个条目, 空行也作为一个条目
     * 
     * @param fileName
     * @return
     */
    public static List<String> getFileContentLines(String fileName) {
        File file = new File(fileName);
        return getFileContentLines(file);
    }

    public static List<String> getFileContentLines(File file) {
        List<String> lines = new ArrayList<String>();
        try {
            // BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<String> split(String str, String delim){
        java.util.StringTokenizer st = new StringTokenizer(str, delim);
        List<String> sps = new ArrayList<String>();
        while(st.hasMoreTokens()){
            String token = st.nextToken().trim();
            sps.add(token);
        }
        return sps;
    }
    
    public static void printAllThreadStack(){
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = rootGroup.getParent()) != null) {
            rootGroup = parent;
        }

        listThreads(rootGroup, "");
    }
    
 // List all threads and recursively list all subgroup
    public static void listThreads(ThreadGroup group, String indent) {
        System.out.println(indent + "Group[" + group.getName() + 
                ":" + group.getClass()+"]");
        int nt = group.activeCount();
        Thread[] threads = new Thread[nt*2 + 10]; //nt is not accurate
        nt = group.enumerate(threads, false);

        // List every thread in the group
        for (int i=0; i<nt; i++) {
            Thread t = threads[i];
            System.out.println(indent + "  Thread[" + t.getName() 
                    + ":" + t.getClass() + "]");
        }

        // Recursively list all subgroups
        int ng = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[ng*2 + 10];
        ng = group.enumerate(groups, false);

        for (int i=0; i<ng; i++) {
            listThreads(groups[i], indent + "  ");
        }
    }
    
    public static String getTempFilePath(String serial){
        String tempFile = "testreport/"+serial+"";
        Util.makeDir(tempFile);
        tempFile = tempFile+"/temp_";
        return tempFile;
    }
    
    public static String getFileNameFromPath(String remotePath){
        if(isNull(remotePath)) return "";
        if(remotePath.lastIndexOf("/")>0){
            remotePath = remotePath.substring(remotePath.lastIndexOf("/")+1);
        }
        if(remotePath.lastIndexOf("\\")>0){
            remotePath = remotePath.substring(remotePath.lastIndexOf("\\")+1);
        }
        return remotePath;
    }
    
    public static boolean getRemoteFileContent(String serial, String remotePath, List<String> ouputFileContent){
        String tempFile = getTempFilePath(serial);
        
        
        tempFile +=getFileNameFromPath(remotePath);
        Util.deleteFile(tempFile);
        
        Util.getCmdOutput("adb -s " + serial + " pull "+remotePath+" "+ tempFile);
        if(!Util.isFileExist(tempFile)){
            return false;
        }
        List<String> content = Util.getFileContentLines(tempFile);
        ouputFileContent.addAll(content);
        return true;
    }
    
    private static String formateStack(String stack){
        String formated = "";
        if(stack==null) return "";
        if(stack.length()<10) return stack;
        
//        java.util.StringTokenizer st = new StringTokenizer(stack, " at ");
        String[] stacks = stack.split(" at ");
        for (int i = 0; i < stacks.length; i++) {
            if(i>0)
            formated += " at "+stacks[i] + "\r\n";
            else{
                formated += stacks[i] + "\r\n";
            }
        }
//        while(stack.indexOf(" at ", 4)!=-1){
//            formated +=stack.substring(0, stack.indexOf(" at ", 4));
//            stack = stack.substring(stack.indexOf(" at ", 4));
//        }
        
        return formated;
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    String f = formateStack("junit.framework.AssertionFailedError: 在查词结果页面点反馈后没有进入反馈页面,fragment:0 at com.kingsoft.test.Powerword7TestCase.assertTrue2(Powerword7TestCase.java:210) at com.kingsoft.translate.TranslateFragmentTest.sendFeebackText(TranslateFragmentTest.java:1030) at com.kingsoft.translate.TranslateFragmentTest.testTrans007(TranslateFragmentTest.java:178) at java.lang.reflect.Method.invokeNative(Native Method) at android.test.InstrumentationTestCase.runMethod(InstrumentationTestCase.java:214) at android.test.InstrumentationTestCase.runTest(InstrumentationTestCase.java:199) at android.test.ActivityInstrumentationTestCase2.runTest(ActivityInstrumentationTestCase2.java:192) at com.kingsoft.test.Powerword7TestCase.callSuperRunTest(Powerword7TestCase.java:97) at com.kingsoft.test.Powerword7TestCase.access$000(Powerword7TestCase.java:24) at com.kingsoft.test.Powerword7TestCase$1.run(Powerword7TestCase.java:46) ");
        System.out.println(f);
	}

}
