package com.att.svn;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.log.Log;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class SvnMonitor extends Thread {
	private Logger logger = Log.getSlientLogger("svn");
	private SvnUpdateListener listener = null;
	private SvnManager svnManager = SvnManager.getInstance();
	
	private static final int SVN_UPDATE_TIME= 12000;
	
	// 被监控的SVN url列表
	private List<String> watchedUrls = new ArrayList<String>();

	private static SvnMonitor sInstance = new SvnMonitor();

	private SvnMonitor() {
	};

	public static SvnMonitor getInstance() {
		return sInstance;
	}

	public void run() {
		AndroidDebugBridge.init(false);
		while (true) {
			
			try {
				checkSvnUpdate();
				
				Util.sleep(SVN_UPDATE_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void checkSvnUpdate(){
		//logger.info("checkSvnUpdate()  ");
		for(int i=0;i<watchedUrls.size();i++){
			String url = watchedUrls.get(i);
			
			checkSvnUpdate(SystemEnv.getLocalPathForUrl(url));
			Util.sleep(2000);
		}
	}
	
	/**
	 * 检查SVN是否更新
	 */
	private void checkSvnUpdate(String localProjectPath) {
		//logger.info("checkSvnUpdate()  "+localProjectPath);
		if(svnManager.isUpdated(localProjectPath)){
			logger.info(localProjectPath+"有更新!");
			SvnInfo svnInfo = svnManager.getCurrentSvnInfo(localProjectPath);
			logger.info(""+svnInfo.toString());
			
			SvnRevisionInfo revisionInfo = svnManager.getSvnRevisionDetail(localProjectPath, svnInfo.getLastChangedRevId());
			logger.info("revision info:"+revisionInfo.getRevisionDetail());
			
			String url = SystemEnv.getUrlForLocalpath(localProjectPath);
			notifySvnListeners(url, revisionInfo, localProjectPath);
		}
		
	}

	private void notifySvnListeners(String url, SvnRevisionInfo revisionInfo,
			String localpath) {
		if (listener != null) {
			listener.onSvnUpdate(url, revisionInfo, localpath);
		}
	}

	public SvnUpdateListener getListener() {
		return listener;
	}

	public void setListener(SvnUpdateListener listener) {
		this.listener = listener;
	}

	public void addWatchedUrl(String url) {
		watchedUrls.add(url);
	}
}
