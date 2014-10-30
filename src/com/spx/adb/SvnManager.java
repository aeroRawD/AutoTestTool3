package com.spx.adb;

import java.util.List;

public class SvnManager {
	private static final String TAG = "SvnUpdate";

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
		List<String> updateContent = update(projectPath);
		Log.d("update return content size:" + updateContent.size());
		if (updateContent.size() > 3
				|| updateContent.get(updateContent.size() - 1).contains(
						"Updated to revision")) {
			Log.d("目录'" + projectPath + "'已经更新.");
			return true;
		}
		Log.d("目录'" + projectPath + "'没有更新.");
		return false;
	}

	public SvnInfo getCurrentSvnInfo(String projectPath) {
		List<String> cmdOutput = Util.getCmdOutput("svn info " + projectPath);
		SvnInfo svnInfo = SvnInfo.getSvnInfo(cmdOutput);
		return svnInfo;
	}

	public void checkoutProject(String url, String localPath, String user,
			String pwd) {
		List<String> cmdOutput = Util.getCmdOutput("svn checkout " + url + " "
				+ localPath + " --username " + user + " --password " + pwd);
		for (String s : cmdOutput)
			Log.d("" + s);
	}

	public List<String> update(String localPath) {
		List<String> cmdOutput = Util.getCmdOutput("svn update " + localPath);
		for (String s : cmdOutput)
			Log.d("" + s);

		return cmdOutput;
	}

	public static void main(String[] args) {
		SvnManager svn = new SvnManager();
		String url = "http://trac.s.iciba.com/svn/ciba-mobile/android/Testing/TestPowerword7";
		String localpath = "d:/data/test";
		String user = "shaopengxiang";
		String pwd = "hcPoIUIPLf";

		// url = "http://trac.s.iciba.com/svn/ciba-mobile/android/Powerword7";
		// localpath="d:/data/powerword7";
		// svn.checkoutProject(url, localpath, user, pwd);
		// svn.update(localpath);

		boolean up = SvnManager.getInstance().isUpdated("D:/data/powerword7");
		System.out.println("updated:" + up);
	}
}
