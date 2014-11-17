package com.att.svn;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import com.log.Log;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class SvnManager {
	private Logger logger = Log.getSlientLogger("svn");

	private static SvnManager instance = new SvnManager();

	private SvnManager() {
		//
	}

	public static SvnManager getInstance() {
		return instance;
	}

	/**
	 * 判断目录是否被更新了
	 * 
	 * @return
	 */
	public boolean isUpdated(String projectPath) {
	    
	    if(!Util.isFileExist(projectPath)){
	        File f=new File(projectPath);
	        f.mkdirs();
	        
	        checkoutProject(SystemEnv.getUrlForLocalpath(projectPath), projectPath, SystemEnv.getUser(), SystemEnv.getPassword());
	    }
	    
		List<String> updateContent = update(projectPath);
		
		if(Util.isFileExist(projectPath+"/s.txt")){
			Util.deleteFile(projectPath+"/s.txt");
			return true;
		}
		//logger.info("update return content size:" + updateContent.size());
		if (updateContent.size() > 3
				|| updateContent.get(updateContent.size() - 1).contains(
						"Updated to revision")) {
			logger.info("目录'" + projectPath + "'已经更新.");
			return true;
		}
		logger.info("目录'" + projectPath + "'没有更新.");
		return false;
	}
	
	public SvnRevisionInfo getSvnRevisionDetail(String projectPath, String revId){
		List<String> cmdOutput = Util.getCmdOutput("svn log --verbose -r " + revId +" "+projectPath, "GBK");
		logger.info("svn log size:" + cmdOutput.size());
		
		SvnRevisionInfo revisionInfo = new SvnRevisionInfo(cmdOutput);
		return revisionInfo;
	}

	public SvnInfo getCurrentSvnInfo(String projectPath) {
		List<String> cmdOutput = Util.getCmdOutput("svn info " + projectPath, "GBK");
		SvnInfo svnInfo = SvnInfo.getSvnInfo(cmdOutput);
		return svnInfo;
	}

	public void checkoutProject(String url, String localPath, String user,
			String pwd) {
		List<String> cmdOutput = Util.getCmdOutput("svn checkout " + url + " "
				+ localPath + " --username " + user + " --password " + pwd , "GBK");
		for (String s : cmdOutput)
			logger.info("" + s);
		
		Util.getCmdOutput("svn cleanup "+localPath, "GBK");
	}

	public List<String> update(String localPath) {
	    List<String> cmdOutput = Util.getCmdOutput("svn cleanup " + localPath, "GBK");
	    for (String s : cmdOutput)
            logger.info("" + s.trim());
	    
		cmdOutput = Util.getCmdOutput("svn update " + localPath, "GBK");
		for (String s : cmdOutput)
			logger.info("" + s.trim());

		Util.getCmdOutput("svn cleanup "+localPath, "GBK");
		
		return cmdOutput;
	}

	public static void main(String[] args) {
		SvnManager svn = new SvnManager();
		String url = "http://trac.s.iciba.com/svn/ciba-mobile/android/Testing/TestPowerword7";
		String localpath = "d:/data/test";


		// url = "http://trac.s.iciba.com/svn/ciba-mobile/android/Powerword7";
		// localpath="d:/data/powerword7";
		// svn.checkoutProject(url, localpath, user, pwd);
		// svn.update(localpath);

		boolean up = SvnManager.getInstance().isUpdated("D:/data/powerword7");
		System.out.println("updated:" + up);
	}
}
