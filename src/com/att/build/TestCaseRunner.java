package com.att.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.att.report.DeviceProp;
import com.att.report.MailContentBuilder;
import com.att.report.TestInfo;
import com.att.report.UseCase;
import com.att.report.XmlReportParser;
import com.att.svn.SvnRevisionInfo;
import com.att.testcommand.TestCommand;
import com.log.Log;
import com.mail.MailSender;
import com.spx.adb.DeviceUtil;
import com.spx.adb.Util;

public class TestCaseRunner extends Thread implements LogCatListener{
    private static Logger logger = Log.getSlientLogger("TestCaseRunner");
    private List<TestCommand> testCommands = null;
    private String serial = null;
    private StringBuilder testOuput = new StringBuilder();
    private TestcaseRunningListener listener = null;
    private boolean isRunnerRunning = false;
    private PrintWriter logcatWriter = null;
    private TestCommandListener tcListener = null;
    private List<UseCase> totalUseCase = new ArrayList<UseCase>();
    private List<UseCase> failedUseCase = new ArrayList<UseCase>();
    private List<UseCase> passUseCase = new ArrayList<UseCase>();
    private TestInfo mTestInfo = null;
    private DeviceProp prop = null;
    private String runnerStartTime;
    private long runnerStartTimeMill;
    private boolean needSendMail = true;
    private String diffFile =null;
    
    private LinkedList<LogCatMessage> lastestMessage= new LinkedList<LogCatMessage>();
    
    interface TestCommandListener{
        public void onCommandStarted(TestCommand tc);
        public void onCommandFinished(TestCommand tc);
    }
    
    public TestCaseRunner(String serial, 
            List<TestCommand> testCommands, TestcaseRunningListener listener, boolean sendMail, String diffFile) {
        this.testCommands = testCommands;
        this.serial = serial;
        this.listener = listener;
        this.needSendMail= sendMail; 
        this.diffFile= diffFile;
    }
    
    public void addTcListener(TestCommandListener tcL){
        tcListener = tcL;
    }
    
    public String getSerial(){
        return serial;
    }

    @Override
    public void run() {
        TestResult runResult = new TestResult();
        runnerStartTime = Util.getTimeStr("yyyy-MM-dd HH:mm:ss");
        runnerStartTimeMill = System.currentTimeMillis();
        try{
            isRunnerRunning = true;
            cleanupWorkspace();
            startLogcatCapture();
            
            ConsoleOutputReceiver receiver = new ConsoleOutputReceiver();
            
            try {
                for (int c = 0; c < testCommands.size(); c++) {
                    TestCommand tc = testCommands.get(c);
                    runOneCommand(tc, receiver);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.severe("Exception:" + ex.getMessage());
            }
            
            collectTestResult();
        
            if(!receiver.isFailed() && receiver.isApkValid() && Util.isNull(receiver.getErrorMsg())){
                runResult.setSucceed(true);
            }
            runResult.setErrorMsg(receiver.getErrorMsg());
            runResult.setResult(testOuput.toString());
        
        }catch(Exception ee){
            ee.printStackTrace();
            logger.severe("Exception:"+ee.getMessage());
        }
        
        logger.info("onFinish notify listener.");
        isRunnerRunning = false;
        listener.onFinish(serial, runResult);
        
        afterTest();
    }
    
    private void afterTest(){
        logger.info("afterTest() ...");
        stopLocat();
        
        logger.info("needSendMail:"+needSendMail);
        if(needSendMail){
            
            parseProp();
            
            logger.info("parseXmlResult...");
            parseXmlResult();
            
            logger.info("createTestInfo...");
            createTestInfo();
            
            //保存到本地
            logger.info("saveTestInfo...");
            saveTestInfo();
            try{
                String mailContent = generateMailContent();
                List<String> attached= getAttachedFiles();
                MailSender.getInstance().sendHtmlMail(MailSender.defaultRecipients, "[自动化测试结果邮件]请查看测试报告", mailContent, attached, "");
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        saveData();
    }
    
    private String getWorkspace(){
        return "testreport/"+serial;
    }
    
    private List<String> getAttachedFiles() {
        List<String> attached = new ArrayList<String>();
        createSingleErrorListFile();
        if(Util.isFileExist(getWorkspace()+"/"+UNION_ERRORS_FILE)){
            attached.add(getWorkspace()+"/"+UNION_ERRORS_FILE);
        }
        if(Util.isFileExist(diffFile)){
            File f = new File(diffFile);
            if(f.length()>10) {
                attached.add(diffFile);
            }
        }
        return attached;
    }
    
    /**
     * 创建汇总的error异常文件
     */
    private static final String UNION_ERRORS_FILE="union_errors.txt";
    private void createSingleErrorListFile(){
        File ws = new File(getWorkspace());
        String[] list = ws.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith("tests_result_errors.txt")) return true;
                return false;
            }
            
        });
        if(list==null || list.length==0) return;
        List<String> unionContents = new ArrayList<String>();
        for(String s:list){
            if(Util.isFileExist(getWorkspace()+"/"+s)){
                List<String> fileContentLines = Util.getFileContentLines(getWorkspace()+"/"+s);
                unionContents.addAll(fileContentLines);
            }
        }
        
        Util.createFile(getWorkspace()+"/"+UNION_ERRORS_FILE, unionContents);
    }

    private void parseProp() {
        logger.info("parseProp...");
        prop = new DeviceProp(serial);
    }
    
    private void saveTestInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append("testing_time:").append(mTestInfo.getTestStartTime()).append("\r\n");
        sb.append("testing_duration:").append(mTestInfo.getTestduration()).append("\r\n");
        sb.append("testing_coverage:").append(mTestInfo.getCoverage()).append("\r\n");
        sb.append("usecase_all:").append(mTestInfo.getUseCaseCount()).append("\r\n");
        sb.append("usecase_fail:").append(mTestInfo.getUseCaseFailCount()).append("\r\n");
        sb.append("usecase_wifi_count:").append(mTestInfo.getWifiUseCaseCount()).append("\r\n");
        sb.append("usecase_wifi_fail_count:").append(mTestInfo.getWifiUseCaseFailCount()).append("\r\n");
        sb.append("phone_name:").append(mTestInfo.getPhoneName()).append("\r\n");
        sb.append("phone_cpu:").append(mTestInfo.getPhoneCpu()).append("\r\n");
        sb.append("apk_size:").append(mTestInfo.getApkSize()).append("\r\n");
        sb.append("android_os_build:").append(mTestInfo.getAndroidOsBuild()).append("\r\n");
        sb.append("lint_warings:").append(mTestInfo.getLintWaringCount()).append("\r\n");
        sb.append("findbugs_warings:").append(mTestInfo.getFindbugsWaringCount()).append("\r\n");
        sb.append("code_lines:").append(mTestInfo.getCodeLines()).append("\r\n");
        sb.append("phone_mem_size:").append(mTestInfo.getPhoneMemSize()).append("\r\n");
        Util.createFile(this.getWorkspace()+"/final.txt", sb.toString());
    }

    private void createTestInfo(){
        mTestInfo = new TestInfo(serial);
        mTestInfo.setAndroidOsBuild(prop.getValue("ro.build.version.release"));
        
        mTestInfo.setTestStartTime(runnerStartTime);
        mTestInfo.setTestduration((System.currentTimeMillis()-runnerStartTimeMill)/1000+"s") ;
        mTestInfo.setUseCaseCount(totalUseCase.size());
        mTestInfo.setUseCaseFailCount(failedUseCase.size());
        mTestInfo.setWifiUseCaseCount(getWifiUseCount());
        mTestInfo.setWifiUseCaseFailCount(getWifiUseFailCount());
        mTestInfo.setPhoneName(prop.getValue("ro.build.product"));
        mTestInfo.setPhoneCpu(prop.getValue("ro.product.cpu.abi"));
    }
    
    private int getWifiUseCount(){
        int count=0;
        for(UseCase uc:totalUseCase){
            if(uc.getAttr("wifi").equals("on")){
                count++;
            }
        }
        return count;
    }
    
    private int getWifiUseFailCount(){
        int count=0;
        for(UseCase uc:totalUseCase){
            if(uc.getAttr("wifi").equals("on") && !uc.isPassed()){
                count++;
            }
        }
        return count;
    }
    
    private String generateMailContent(){
        String fileContent = Util.getFileContent("data/testresult.html");
        MailContentBuilder mailContentBuilder = new MailContentBuilder(fileContent, totalUseCase,failedUseCase, passUseCase, mTestInfo);
        mailContentBuilder.build();
        String mailContent = mailContentBuilder.buildHtmlMailContent();
        return mailContent;
    }
    
    private void parseXmlResult() {
        File workspace = new File("testreport/"+serial);
        if(!workspace.exists()) {
            logger.severe("testreport/"+serial+" 目录不存在,这是什么问题:");
            return;
        }
        
        String[] filelist = workspace.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("testresult") && name.endsWith(".xml")) return true;
                return false;
            }
        });
        
        XmlReportParser parser=new XmlReportParser();
        for(String s:filelist){
            parser.addXmlFile("testreport/"+serial+"/"+s);
        }
        //parser.addXmlFiles(filelist);
        parser.doParse();
        totalUseCase = parser.getTotalUseCases();
        failedUseCase = parser.getFailedUseCases();
        passUseCase =  parser.getPassedUseCases();
    }

    private void cleanupWorkspace(){
        Util.cleanDirectroy("testreport/"+serial);
    }
    
    LogCatReceiverTask logcatTask = null;
    private void stopLocat(){
        logger.info("stopLocat() ...");
        logcatTask.stop();
    }
    
    private void startLogcatCapture(){
        try {
            logcatWriter = new PrintWriter(new File("testreport/"+serial+"/logcat.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        IDevice device = DeviceUtil.createDevice(serial);
        if(device!=null && device.isOnline()){
            logcatTask = new LogCatReceiverTask(device);
            logcatTask.addLogCatListener(this);
            new Thread(logcatTask).start();
        }
        
        
    }
    
    
    private void collectTestResult(){
        logger.info("collectTestResult prop");
//        List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" pull /sdcard/Robotium-Screenshots testreport/"+serial);
        List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" shell getprop", true);
        Util.createFile("testreport/"+serial+"/prop.txt", cmdOutput);
        
        logger.info("collectTestResult meminfo");
        cmdOutput = Util.getCmdOutput("adb -s "+ serial+" shell cat /proc/meminfo", true);
        Util.createFile("testreport/"+serial+"/meminfo.txt", cmdOutput);
    }
    
    /**
     * 保存当前测试数据到备份目录
     */
    private void saveData() {
        logger.info("saveData() .... ");
        String time = Util.getTimeStr("yyyy-MM-dd_HHmmss");
        Util.copyFolder("testreport/"+serial+"", "data/backup/workspace/"+serial+"/"+time);
    }

    private void runOneCommand(TestCommand tc, ConsoleOutputReceiver receiver){
        logger.info("run test command:" + tc);
        
        try {
        List<String> preCommands = tc.getBeforeCommands(serial);
        if(preCommands!=null){
            for(String s:preCommands){
                logger.info("run before command:"+s);
                List<String> cmdOutput = Util.getCmdOutput(s);
            }
        }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("run before command failed:"+ex.getMessage());
        }
       
        String command = tc.getCommand();
        try {
            IDevice device = DeviceUtil.createDevice(serial);
            logger.info("run command for device:"+device.getName());
            receiver.setCmd(command);
            if (tcListener != null)
                tcListener.onCommandStarted(tc);
            
            Util.runAdbCmd(device, command, receiver, 1200 * 1000);
           
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("run command failed:"+ex.getMessage());
        }
        
        tc.addErrorMsg(receiver.getErrorMsg());
        tc.addShortMsg(receiver.getShortMsg());
        tc.addLongMsg(receiver.getLongMsg());
        
        try {
            List<String> afterCommands = tc.getAfterCommands(serial);
            if(afterCommands!=null){
                for(String s:afterCommands){
                    logger.info("run after command:"+s);
                    List<String> cmdOutput = Util.getCmdOutput(s);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("run after command failed:"+ex.getMessage());
        }
        
        if(tcListener!=null)
            tcListener.onCommandFinished(tc);
    }

    private final class LogcatReceiver extends MultiLineReceiver {

        @Override
        public boolean isCancelled() {
            if(isRunnerRunning){
                logcatWriter.close();
            }
            return isRunnerRunning;
        }

        @Override
        public void processNewLines(String[] lines) {
            if(logcatWriter!=null && lines!=null){
                for(String s:lines){
                    logcatWriter.println(s);
                }
                logcatWriter.flush();
            }
            
        }
        
    }
    

    private final class ConsoleOutputReceiver extends MultiLineReceiver {
        private boolean failed = false;
        private String errorMsg = "";
        private boolean apkvalid= true;
        private String shortMsg ="";
        private String longMsg ="";
        
        public void setCmd(String cmd) {
            testOuput.append("命令:"+cmd + "\r\n");
            testOuput.append("\r\n");
        }
        
        public String getShortMsg(){
            return shortMsg;
        }
        
        public String getLongMsg(){
            return longMsg;
        }
        
        public String getErrorMsg(){
            return errorMsg;
        }
        
        public boolean isFailed(){
            return failed || !Util.isNull(errorMsg);
        }
        
        public boolean isApkValid(){
            return apkvalid;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void processNewLines(String[] lines) {
            boolean append = false;
            StringBuilder temp = new StringBuilder();
            for (String line : lines) {
                if(!isIgnoredString(line)){
                    temp.append(line + "\r\n");
                    append = true;
                    logger.info(line);
                }
                
                if(line.contains("FAILURES!!!")){
                    failed = true;
                }else if(line.startsWith("junit.framework.AssertionFailedError:")){
                    errorMsg = line.substring("junit.framework.AssertionFailedError:".length());
                }
                
                if(line.contains("INSTRUMENTATION_RESULT:") && (line.contains("java.lang.RuntimeException")||line.contains("crashed"))){
                    failed = true;
                }
                
                if(line.contains("INSTRUMENTATION_FAILED") || line.contains("INSTRUMENTATION_STATUS: Error")){
                    failed = true;
                }
                
                if(line.startsWith("INSTRUMENTATION_RESULT: longMsg=")){
                    longMsg = line.substring("INSTRUMENTATION_RESULT: longMsg=".length());
                }
                
                if(line.startsWith("INSTRUMENTATION_RESULT: shortMsg=")){
                    shortMsg = line.substring("INSTRUMENTATION_RESULT: shortMsg=".length());
                }
                
                if(line.startsWith("INSTRUMENTATION_STATUS: Error=")){
                    errorMsg = line.substring("INSTRUMENTATION_STATUS: Error=".length());
                }
                
                if(line.contains("Could not find test class")){
                    apkvalid = false;
                }
            }

            if(append){
                testOuput.append(Util.getTimeStr("yyyy-MM-dd HH:mm:ss") + ":\r\n");
                testOuput.append(temp.toString());
            }
        }

    }

    private boolean isIgnoredString(String line){
        if(Util.isNull(line)||
                line.startsWith("BT INFO: 2")) {
            return true;
        }
        return false;
    }

    @Override
    public void log(List<LogCatMessage> msgList) {
        if(logcatWriter==null ||msgList==null) return;
        for(LogCatMessage lcm:msgList){
            logcatWriter.println(lcm.toString());
        }
        logcatWriter.flush();
        
        addLogcatMessages(msgList);
    }
    
    public StringBuilder getLastestLogcatMessages(){
        StringBuilder sb = new StringBuilder();
        synchronized(lastestMessage){
            for(LogCatMessage lcm:lastestMessage){
                sb.append(lcm.toString()+"\r\n");
            }
        }
        return sb;
    }
    
    private void addLogcatMessages(List<LogCatMessage> msgList){
        synchronized(lastestMessage){
            for(LogCatMessage lcm:msgList){
                lastestMessage.add(lcm);
            }
        }
        trimLogcatMessageList();
    }
    
    private void trimLogcatMessageList(){
        synchronized(lastestMessage){
            while(lastestMessage.size()>200){
                lastestMessage.removeFirst();
            }
        }
        
    }
}
