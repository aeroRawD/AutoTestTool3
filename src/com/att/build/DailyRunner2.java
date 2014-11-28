package com.att.build;

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
import com.att.testcommand.TestCommand;
import com.att.testcommand.TestWifiOffCommand;
import com.log.Log;
import com.mail.MailSender;
import com.spx.adb.AppStartupTimeTest;
import com.spx.adb.Builder;
import com.spx.adb.DeviceMonitor;
import com.spx.adb.DeviceUtil;
import com.spx.adb.Installer;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class DailyRunner2 implements TestcaseRunningListener{
    private Logger logger = Log.getSlientLogger("DailyRunner");
    private static DailyRunner2 sInstance = new DailyRunner2();
    private DailyRunner2(){}
    public static DailyRunner2 getInstance(){
        return sInstance;
    }
    private PrintWriter log = null;
    private boolean isRunning = false;
    public boolean isRuning(){
        return isRunning || !runningTestCaseRunner.isEmpty();
    }
    
    private long lastPerformTime = 0L;
    private List<InstrumentTestRunner> runningTestCaseRunner = new ArrayList<InstrumentTestRunner>();

    private HashMap<String, InstrumentTestRunner> runnerMap = new HashMap<String, InstrumentTestRunner>();
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
        DailyBuilder builder = new DailyBuilder();
        builder.perform();
//        
        //检查lint告警, 并备份
        DailyLintChecker checker = new DailyLintChecker();
        checker.perform(SystemEnv.APP_PROJECT_PATH);
        
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
    
   
    public String getDailyBackPath(){
        String day = Util.getTimeStr("yyyyMMdd");
        String path = "data/backup/appapk/"+day;
        Util.makeDir(path);
        return path;
    }
    
    private void createDailyRunnerLogFile(){
        try {
            log = new PrintWriter(new FileOutputStream(getDailyBackPath()+"/runner.log", true));
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
            
            //runCmd("adb -s "+serial+" install -r Yzc.apk");
            runCmd("adb -s "+serial+" uninstall com.example.yzc");
            Installer.install(serial, "Yzc.apk");
            
            runCmd("adb -s "+serial+" shell am start -n com.example.yzc/com.example.yzc.MainActivity");
            
//            runCmd("adb -s "+serial+" shell am broadcast -a kingsoft.test.cmd.action.closewifi");
//            runCmd("adb -s "+serial+" shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
            
            runCmd("adb -s "+serial+" uninstall com.kingsoft.test");
            runCmd("adb -s "+serial+" uninstall com.kingsoft");
            runCmd("adb -s "+serial+" shell rm -r /sdcard/Robotium-Screenshots");
            runCmd("adb -s "+serial+" shell rm -r /sdcard/powerword/rec");
            
            Installer.install(serial, Builder.getInstance().getAppApkFileName());
            Installer.install(serial, Builder.getInstance().getTestAppApkFileName());
            
//            runCmd("adb -s "+serial+" install -r "+Builder.getInstance().getAppApkFileName());
//            runCmd("adb -s "+serial+" install -r "+Builder.getInstance().getTestAppApkFileName());
        }
        
    }
    
    private void startTestRunner(){
        DeviceMonitor.getInstance().findDevices();
        List<String> serials = DeviceMonitor.getInstance().getOnlineDeviceSerials();

        for(String serial:serials){
            //每个设备都要新启动一个线程运行测试用例.
            InstrumentTestRunner runner = new InstrumentTestRunner(serial, this);
            runner.start();
            runningTestCaseRunner.add(runner);
            runnerMap.put(serial, runner);
            runningSerials.add(serial);
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
        
        report.append("apk size:"+""+"\r\n");
        report.append("code lines:"+""+"\r\n");
        report.append("lint warings:"+DailyLintChecker.getCurrentRevWarnings(SystemEnv.APP_PROJECT_PATH)+"\r\n");
        report.append("findbugs warings:"+""+"\r\n");
        
        report.append("一共"+testDeviceNum+"个设备进行了测试."+":\r\n");
        
        for(int i=0;i<runningTestCaseRunner.size();i++){
            InstrumentTestRunner testCaseRunner = runningTestCaseRunner.get(i);
            report.append("-------------"+":\r\n");
            report.append("device:"+testCaseRunner.getSerial()+", name:"+testCaseRunner.getPhoneName()+":\r\n");
            report.append("共有"+testCaseRunner.getAllCaseCount()+"个用例.");
            report.append("其中"+testCaseRunner.getAllWifiOffCaseCount()+"个wifi off,");
            report.append(""+testCaseRunner.getAllWifiOnCaseCount()+"个wifi on\r\n");
            report.append(""+testCaseRunner.getAllSucceedCaseCount()+"个成功,");
            report.append(""+testCaseRunner.getAllFailedCaseCount()+"个失败;");
            report.append("-------------"+":\r\n");
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
    
    @Override
    public void onFinish(String serial, TestResult testResult) {
        logger.info(serial+" 测试结束. testResult:"+(testResult.isSucceed()?"成功":"失败"));
        testResults.put(serial, testResult);
        runningSerials.remove(serial);
        if(runningSerials.isEmpty()){
            onAllRunnerFinished();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        AndroidDebugBridge.init(false);
        DailyRunner2.getInstance().dailyPerfom();
    }


}
