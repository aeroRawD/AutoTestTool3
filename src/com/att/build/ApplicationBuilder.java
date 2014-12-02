package com.att.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.att.report.LintResult;
import com.att.svn.SvnInfo;
import com.att.svn.SvnManager;
import com.att.svn.SvnRevisionInfo;
import com.att.svn.SvnUpdateListener;
import com.att.testcommand.TestApiCaseCommand;
import com.att.testcommand.TestCommand;
import com.att.testcommand.TestWifiOffCommand;
import com.att.testcommand.TestWifiOnCommand;
import com.log.Log;
import com.mail.MailSender;
import com.spx.adb.Builder;
import com.spx.adb.DeviceMonitor;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class ApplicationBuilder implements SvnUpdateListener, TestcaseRunningListener{
	private static Logger logger = Log.getSlientLogger("ApplicationBuilder");
	private List<String> installSucceedDeviceList = new ArrayList<String>();
	private List<String> runningTestDeviceList = new ArrayList<String>();
	private HashMap<String, TestResult> totalTestResults= new HashMap<String, TestResult>();
	
	private boolean isRunning = false;
	
	private static ApplicationBuilder sInstance = new ApplicationBuilder();
	public static ApplicationBuilder getInstance(){
	    return sInstance;
	}
	private ApplicationBuilder(){}
	
	public static void main(String[] args){
	    AndroidDebugBridge.init(false);
	    DeviceMonitor.getInstance().start();
	    SvnManager svnManager = SvnManager.getInstance();
        SvnInfo svnInfo = svnManager.getCurrentSvnInfo(SystemEnv.APP_PROJECT_PATH);
        logger.info("" + svnInfo.toString());

        SvnRevisionInfo revisionInfo = svnManager.getSvnRevisionDetail(
                SystemEnv.APP_PROJECT_PATH, svnInfo.getLastChangedRevId());
        logger.info("revision info:" + revisionInfo.getRevisionDetail());
        
	    ApplicationBuilder.getInstance().onSvnUpdate(SystemEnv.APP_PROJECT_URL, revisionInfo, SystemEnv.APP_PROJECT_PATH);
	}
	
	
	private SvnRevisionInfo revisionInfo = null;
	private String localPath = null;
	private String diffFile = null;
	private String lintFile = null;
	private String author = null;
	
	@Override
	public void onSvnUpdate(String url, SvnRevisionInfo revisionInfo,
			String localpath) {
	    isRunning = true;
	    diffFile =  null;
	    lintFile = null;
	    author = revisionInfo.getAuthor();
	    
		logger.info("build apks...path:"+localpath+", author:"+revisionInfo.getAuthor());
		this.revisionInfo = revisionInfo;
		logger.info("getRevId:"+revisionInfo.getRevId());
		
		this.localPath =  localpath;
		
		StringBuilder buildError = new StringBuilder();
		boolean build = Builder.getInstance().buildPath(localpath, 6, buildError);
		
		installSucceedDeviceList.clear();
		if (build) {
//			if (SystemEnv.APP_PROJECT_PATH.equals(localpath)) {
		       
			    installSucceedDeviceList = Util.installPowerwordAppToAllOnlineDevices();
//			} else if (SystemEnv.TESTAPP_PROJECT_PATH.equals(localpath)) {
			    installSucceedDeviceList = Util.installTestAppToAllOnlineDevices();
//			}
		}else {
		    //MailSender.getInstance().sendMail(MailSender.defaultRecipients, "[自动邮件]编译失败呀, 请检查"+url+"上代码是否正常.", buildError.toString(), true);
		    MailSender.getInstance().sendBuildFailedNotify(url, buildError.toString());
		}
		
		//检查lint告警, 并备份
        DailyLintChecker checker = new DailyLintChecker();
        lintFile = checker.perform(localPath);
		
        diffFile = SvnManager.getInstance().createDiffWithLastRev(revisionInfo.getRevId(), localpath);
		
        logger.info("install succeed devices:"+installSucceedDeviceList.size());
		if(installSucceedDeviceList.size()>0){
			runTestCase(revisionInfo, diffFile);
		}
	}
	
	
	private void runTestCase(SvnRevisionInfo revisionInfo, String diffFile) {
	    runningTestDeviceList.clear();
	    totalTestResults.clear();
	    logger.info("runTestCase() ...  ");
	    
	    
		//TestcaseCommand testCommand = new TestcaseCommand();
		//testCommand.addCommand("am instrument -w -e class com.kingsoft.test.api.ApiTest#testApi com.kingsoft.test/android.test.InstrumentationTestRunner");
	    List<TestCommand> testCommands = new ArrayList<TestCommand>();
        testCommands.add(new TestApiCaseCommand());
        
		logger.info("install succeed device count:"+installSucceedDeviceList.size());
		for(String dev:installSucceedDeviceList){
            logger.info("start test command for device:" + dev);
			//每个设备都要新启动一个线程运行测试用例.
			TestCaseRunner runner = new TestCaseRunner(dev, testCommands, this, false, diffFile);
			runner.start();
			runningTestDeviceList.add(dev);
		}
	}


	

    public boolean isRunningTest() {
        return runningTestDeviceList.size() > 0 || isRunning;
    }

	@Override
	public void onFinish(String serial, TestResult testResult) {
	    logger.info("device:"+serial+" test finished. test result:"+(testResult.isSucceed()?"成功":"失败")+"!");
		runningTestDeviceList.remove(serial);
		totalTestResults.put(serial, testResult);
        if (runningTestDeviceList.size() == 0) {
            onAllTestRunnerFinished();
        }
	}
	
	/**
	 * 返回所有的测试结果数目, 其实就是参与测试的设备数目
	 * @return
	 */
	private int getAllTestResultCount(){
	    return installSucceedDeviceList.size();
	}
	
	/**
	 * 返回成功的测试结果数目.
	 * @return
	 */
    private int getSucceedTestResultCount() {
        int count = 0;
        for (int i = 0; i < installSucceedDeviceList.size(); i++) {
            String serial = installSucceedDeviceList.get(i);
            if (totalTestResults.get(serial) != null) {
                if (totalTestResults.get(serial).isSucceed())
                    count++;
            }
        }
        return count;
    }
	
    public void onAllTestRunnerFinished() {
        logger.info("onAllTestRunnerFinished()  diffFile:"+diffFile);
        logger.info("onAllTestRunnerFinished()  lintFile:"+lintFile);
        try{
        
        //attached.add("data/backup/rev/powerword7/15937/15936_15937.patch");
        
        String mailContent = getMailContent(localPath);
        
        //邮件发送测试结果
        List<String> attached = new ArrayList<String>();
            if (isLintWaringsAdd() || !isCasePassed()) {
                if (diffFile != null)
                    attached.add(diffFile);
                if (lintFile != null && isLintWaringsAdd()) {
                    attached.add(lintFile);
                }
            }
        logger.info("onAllTestRunnerFinished()  attached.size():"+attached.size());
        
        MailSender.getInstance().sendMail(getMailRecipients(),
                getMailSubject(), mailContent, false, attached);
        
        //保存测试结果到本地
        saveTestResult(getMailContent(localPath));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        isRunning = false;
    }
    
    private void saveTestResult(String content) {
        String timeStr = Util.getTimeStr("MM-dd_HHmmss");
        String revId = revisionInfo.getRevId();
        
        String serials = "";
        for (int i = 0; i < installSucceedDeviceList.size(); i++) {
            String serial = installSucceedDeviceList.get(i);
            serials+="_"+serial;
        }
        
        String fileName = "testreport/revisions/" + timeStr + "_" + revId + "_"
                + serials + ".txt";
        Util.createFile(fileName, content);
    }
	
    private String getMailSubject() {
        int alltest = getAllTestResultCount();
        int succeed = getSucceedTestResultCount();
        logger.info("alltest:"+alltest);
        logger.info("succeed:"+succeed);
        String subject = "";
        // 如果测试全部成功了
        if (succeed > 0) {
            subject += "[自动化用例执行知会] API测试成功 . ";
//            if (alltest > 1) {
//                subject += alltest + "个设备全部测试通过.";
//            } else {
//                subject += "测试用例在设备" + installSucceedDeviceList.get(0)
//                        + "上测试通过.";
//            }
        } else if (succeed == 0) {
            subject += "[自动化用例执行失败]";
            if (alltest > 1) {
                subject += "在" + alltest + "个设备上全部测试失败";
            } else {
                // 只有一个设备的情况
                String seri = installSucceedDeviceList.get(0);
                subject += "在设备" + seri + "上测试不通过";
            }
            String seri = installSucceedDeviceList.get(0);
            TestResult testResult = totalTestResults.get(seri);
            subject += "[" + testResult.getErrorMsg() + "], 请关注";
        } else {
            subject += "[自动化用例执行失败]";
            int failedCount = alltest - succeed;
            subject += failedCount + "设备上测试不通过. ";
            for (int i = 0; i < installSucceedDeviceList.size(); i++) {
                String serial = installSucceedDeviceList.get(i);
                TestResult testResult = totalTestResults.get(serial);
//                if(!testResult.isSucceed()){
//                    subject += "["+testResult.getErrorMsg()+ "]";
//                }
            }
        }
        
        if (this.isLintWaringsAdd()) {
            subject += " lint告警有增加, 请关注哈";
        }
        
        return subject;
    }
    
    private boolean lintWarngingsAdd = false;
    private boolean isLintWaringsAdd(){
        return lintWarngingsAdd;
    }
	
	private String getMailContent(String localPath){
	    StringBuilder content = new StringBuilder();
	    
	    content.append("API用例执行结果:  ");
        boolean passed = false;
        StringBuilder fails = new StringBuilder();
        for (int i = 0; i < installSucceedDeviceList.size(); i++) {
            String serial = installSucceedDeviceList.get(i);
            TestResult testResult = totalTestResults.get(serial);
            if(testResult.isSucceed()) passed = true;
            fails.append(">>>>设备序号:"+serial+"\r\n");
            fails.append("Test Result:\r\n");
            fails.append(testResult.getResult());
        }
        content.append((passed? "成功!!":"失败!!")+"\r\n\r\n");
        if(!passed){
            casePassed = false;
            content.append(fails.toString());
        }else {
            casePassed = true;
        }
        
        
        String lintWarings = "";
        try{
            lintWarings+="Lint 告警: ";
            lintWarings+=createLintDiff(localPath, revisionInfo.getRevId());
            lintWarings+="\r\n";
            lintWarings+="----------------------------------------------------------------------------------------\r\n";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        if(lintWarings.indexOf("增加")!=-1){
            lintWarngingsAdd = true;
        } else {
            lintWarngingsAdd = false;
        }
        
        if (lintWarings.length() > 20) {
            content.append(lintWarings + "\r\n");
        }
        
        
       
        
        content.append("\r\n\r\n");
        content.append("Svn 更新记录:\r\n");
        content.append("--------------------------------------"+revisionInfo.getRevId()+"-------------------------------------------\r\n");
        content.append(revisionInfo.getRevisionDetail().trim()+"\r\n");
        content.append("----------------------------------------------------------------------------------------\r\n");
        content.append("\r\n");
        return content.toString();
	}
	
	private String createLintDiff(String localPath, String revId) {
	    try{
    	    logger.info("createLintDiff()  localPath:"+localPath+", revId:"+revId);
            String lastLintFile = BackupManager.getLastLintFile(localPath, revId);
            logger.info("createLintDiff()  lastLintFile:"+lastLintFile);
            lastLintFile=DailyLintChecker.getLintFileName(localPath, lastLintFile);
            logger.info("createLintDiff()  22 lastLintFile:"+lastLintFile);
            String currentLintFile = DailyLintChecker.getLintFileName(localPath, revId);
            logger.info("createLintDiff()  22 currentLintFile:"+currentLintFile);
            LintResult lintResult1 = new LintResult(lastLintFile);
            LintResult lintResult2 = new LintResult(currentLintFile);
            String diff = lintResult2.diffWith(lintResult1);
            logger.info("diff:"+diff);
            return diff;
	    }catch(Exception x){
	        x.printStackTrace();
	    }
       return "";
    }
    private List<String> getMailRecipients(){
        List<String> recipients = new ArrayList<String>();
        recipients.add("shaopengxiang@kingsoft.com");
        if(!SystemEnv.isTestingMode() && SystemEnv.APP_PROJECT_PATH.equals(localPath)){
//            recipients.add("GuoQin@kingsoft.com");
//            recipients.add("WANGZHENGZE@kingsoft.com");
//            recipients.add("guoxueling@kingsoft.com");
//            recipients.add("HUANGPUJUN@kingsoft.com");
//            recipients.add("ChenJiangang@kingsoft.com");
            if(isLintWaringsAdd() || !isCasePassed()){
                recipients.add(author+"@kingsoft.com");
            }
            
        }
        
	    return recipients;
	}
    
    private boolean casePassed = false;
    private boolean isCasePassed() {
        return casePassed;
    }
}
