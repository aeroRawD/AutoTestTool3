package com.spx.adb;

import java.util.List;

public class SvnUpdate {
	private static final String TAG ="SvnUpdate";
	public void checkoutProject(String url, String localpath, String user, String pwd){
		List<String> cmdOutput = Util.getCmdOutput("svn checkout "+url+" "+localpath+" --username "+user+" --password "+pwd);
		for(String s:cmdOutput)
			Log.d(""+s);
	}
	
	public static void main(String[] args){
		SvnUpdate svn = new SvnUpdate();
		String url = "http://trac.s.iciba.com/svn/ciba-mobile/android/Testing/TestPowerword7";
		String localpath="d:/data/test";
		String user = "shaopengxiang";
		String pwd ="hcPoIUIPLf";
		
		svn.checkoutProject(url, localpath, user, pwd);
	}
}
