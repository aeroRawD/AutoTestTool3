package com.att.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.att.report.TestInfo;
import com.att.testcommand.TestApiCaseCommand;
import com.att.testcommand.TestCommand;
import com.att.testcommand.TestWifiOffCommand;
import com.att.testcommand.TestWifiOffRetryCommand;
import com.att.testcommand.TestWifiOnCommand;
import com.att.testcommand.TestWifiOnRetryCommand;
import com.log.Log;
import com.mail.MailSender;
import com.spx.adb.Builder;
import com.spx.adb.DeviceMonitor;
import com.spx.adb.DeviceUtil;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class DailyRunner implements TestcaseRunningListener{
    private Logger logger = Log.getSlientLogger("DailyRunner");
    private static DailyRunner sInstance = new DailyRunner();
    private DailyRunner(){}
    public static DailyRunner getInstance(){
        return sInstance;
    }
    private PrintWriter log = null;
    private boolean isRunning = false;
    public boolean isRuning(){
        return isRunning || !runningTestCaseRunner.isEmpty();
    }
    
    private long lastPerformTime = 0L;
    private List<TestCaseRunner> runningTestCaseRunner = new ArrayList<TestCaseRunner>();
    private List<TestCaseRunnerHelper> runnerHelpers = new ArrayList<TestCaseRunnerHelper>();
    private HashMap<String, TestCaseRunner> runnerMap = new HashMap<String, TestCaseRunner>();
    private List<String> runningSerials = new ArrayList<String>();
    private HashMap<String,TestResult> testResults = new HashMap<String,TestResult>();
    
    public boolean hasBeenPerfomToday(){
        if((System.currentTimeMillis()-lastPerformTime)<10*60*60*1000){
            return true;
        }
        
        return false;
    }
    
    public void dailyPerfom(){
        
        lastPerformTime = System.currentTimeMillis();
        logger.info("dailyPerfom E....");
        isRunning = true;
        createDailyRunnerLogFile();
        
        //打包并备份
//        DailyBuilder builder = new DailyBuilder();
//        builder.perform();
//        
//        //检查lint告警, 并备份
//        DailyLintChecker checker = new DailyLintChecker();
//        checker.perform();
        
        runTestCase();
        
        log.close();
        
    }
    
    private void runTestCase(){
        logger.info("runTestCase ....");
        startCaptureLogcat();
        
        startSetupTestEnv();
        
        startTestRunner();
        
        generateTestResult();
    }
    
   
    
    
    private void createDailyRunnerLogFile(){
        try {
            log = new PrintWriter(new FileOutputStream(BackupManager.getDailyBackPath()+"/runner.log", true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void startCaptureLogcat(){
        //runCmd("start log.bat");
    }
    
    
    
    private void startSetupTestEnv(){
        DeviceMonitor.getInstance().findDevices();
        List<String> serials = DeviceMonitor.getInstance().getOnlineDeviceSerials();
        logger.info("当前在线设备数:" + serials.size());
        logger.info("当前在线设备:" + serials.toString());
        for(String serial:serials){
            logger.info("set up for device:" + serial);
            
            runCmd("adb -s "+serial+" install -r Yzc.apk");
            runCmd("adb -s "+serial+" shell am start -n com.example.yzc/com.example.yzc.MainActivity");
//            runCmd("adb -s "+serial+" shell am broadcast -a kingsoft.test.cmd.action.closewifi");
//            runCmd("adb -s "+serial+" shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
            
            runCmd("adb -s "+serial+" uninstall com.kingsoft.test");
            runCmd("adb -s "+serial+" uninstall com.kingsoft");
            
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/AUTOTEST-all.xml");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/testinfo.xml");
            runCmd("adb -s "+serial+" shell rm -r /sdcard/Robotium-Screenshots");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/testinfo.xml"); 
            
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/fails.txt");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/AUTOTEST-ok.xml");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/AUTOTEST-fail.xml");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/ok2.xml");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/fail2.xml"); 
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/ok2.xml"); 
            
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/tests_list.txt");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/tests_result_list.txt"); 
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/runningtests_list.txt");
            runCmd("adb -s "+serial+" shell rm /sdcard/powerword/tests_result_errors.txt"); 
            
            runCmd("adb -s "+serial+" install -r "+Builder.getInstance().getAppApkFileName());
            runCmd("adb -s "+serial+" install -r "+Builder.getInstance().getTestAppApkFileName());
        }
        
    }
    
    private void startTestRunner(){
        DeviceMonitor.getInstance().findDevices();
        List<String> serials = DeviceMonitor.getInstance().getOnlineDeviceSerials();
        List<TestCommand> testCommands = new ArrayList<TestCommand>();
        testCommands.add(new TestWifiOffCommand());
//        testCommands.add(new TestWifiOffRetryCommand());
//        testCommands.add(new TestWifiOnCommand());
//        testCommands.add(new TestWifiOnRetryCommand());
//        testCommands.add(new TestApiCaseCommand());
        for(String serial:serials){
            //每个设备都要新启动一个线程运行测试用例.
            TestCaseRunner runner = new TestCaseRunner(serial, testCommands, this, true, null);
            runner.start();
            runningTestCaseRunner.add(runner);
            runnerMap.put(serial, runner);
            runningSerials.add(serial);
        }
        
        for(int i=0;i<runningTestCaseRunner.size();i++){
            TestCaseRunner tcr = runningTestCaseRunner.get(i);
            TestCaseRunnerHelper testCaseRunnerHelper = new TestCaseRunnerHelper(tcr);
            testCaseRunnerHelper.start();
            tcr.addTcListener(testCaseRunnerHelper);
            runnerHelpers.add(testCaseRunnerHelper);
        }
    }
   
    private void generateTestResult() {
        
       
    }
    
    private void onAllRunnerFinished(){
        
        String report =createTestReport();
        logger.info(report);
      //邮件发送测试结果
        MailSender.getInstance().sendMail(getMailRecipients(),
                "[自动化测试]用例执行结果", report, false, null);
        
        runnerMap.clear();
        runnerHelpers.clear();
        runningTestCaseRunner.clear();
        runningSerials.clear();
        isRunning = false;
    }
    
    private List<String> getMailRecipients(){
        return MailSender.defaultRecipients;
    }
    
    private TestInfo createTestInfoFromXml(){
        return null;
    }
    
    private String createTestReportFromXml(){
        return null;
    }
    
    private String createTestReport(){
        StringBuilder report = new StringBuilder();
        int testDeviceNum = runningTestCaseRunner.size();
        report.append("一共"+testDeviceNum+"个设备进行了测试."+":\r\n");
        
        List<String> serials = new ArrayList<String>();
        for(int i=0;i<runningTestCaseRunner.size();i++){
            TestCaseRunner testCaseRunner = runningTestCaseRunner.get(i);
            report.append("device:"+testCaseRunner.getSerial()+":\r\n");
            serials.add(testCaseRunner.getSerial());
        }
        
        report.append("-------------"+":\r\n");
        for(TestCaseRunnerHelper runnerHelper:runnerHelpers){
            report.append(">>>>>设备"+runnerHelper.getSerial()+":\r\n");
            TestResult testResult = testResults.get(runnerHelper.getSerial());
            report.append("Console Output: 测试结果:"+(testResult.isSucceed()?"成功":"失败")+":\r\n");
            report.append(""+testResult.getResult()+":\r\n");
            
            List<InstrumentTestResult> results = runnerHelper.getResults();
            for(InstrumentTestResult result:results){
                report.append(""+result.toString()+":\r\n");
            }
            
            report.append("\r\n");
        }
        
        return report.toString();
    }
    
    private void runCmd(String command){
        List<String> cmdOutput = Util.getCmdOutput(command);
        appendRunnerLog(cmdOutput);
    }
    
    private void appendRunnerLog(List<String> cmdOutput){
        for(String s:cmdOutput){
            log.write(s+"\r\n");
        }
        log.flush();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        AndroidDebugBridge.init(false);
        DailyRunner.getInstance().dailyPerfom();
    }
    
    @Override
    public void onFinish(String serial, TestResult testResult) {
        logger.info(serial+" 测试结束. testResult:"+(testResult.isSucceed()?"成功":"失败"));
        testResults.put(serial, testResult);
        runningSerials.remove(serial);
        if(runningSerials.isEmpty()){
            onAllRunnerFinished();
        }
    }

}
