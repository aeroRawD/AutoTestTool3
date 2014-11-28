package com.att.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.att.Constant;
import com.att.report.BarDataBean;
import com.att.report.DeviceProp;
import com.att.report.MailContentBuilder;
import com.att.report.TestInfo;
import com.att.report.UseCase;
import com.att.report.XmlReportParser;
import com.log.Log;
import com.mail.MailSender;
import com.spx.adb.AppStartupTimeTest;
import com.spx.adb.DeviceMonitor;
import com.spx.adb.DeviceUtil;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class InstrumentTestRunner extends Thread implements LogCatListener {
    private static Logger logger = Log.getSlientLogger("InstrumentTestRunner");
    private static int mRunId = 1;
    private PrintWriter logcatWriter = null;
    private String serial = null;
    // private ConsoleOutputReceiver receiver = null;
    private long startTime = 0L;
    private int usedTime = 0;
    private int startUpTimeAvg = -1;
    
    private LogCatReceiverTask logcatTask = null;

    private List<UseCase> allCases = new ArrayList<UseCase>();
    private List<UseCase> allFailedCases = new ArrayList<UseCase>();
    private List<UseCase> allSucceedCases = new ArrayList<UseCase>();

    private TestcaseRunningListener listener = null;
    private TestInfo mTestInfo = null;
    private DeviceProp prop = null;

    private static final String UNION_ERRORS_FILE = "union_errors.txt";
//    private boolean needSendMail = true;
    private StringBuilder testInfo = new StringBuilder();

    public InstrumentTestRunner(String serial, TestcaseRunningListener listener) {
        this.serial = serial;
        this.listener = listener;
    }

    public void run() {

        cleanupWorkspace();
        TestResult runResult = new TestResult();
        startTime = System.currentTimeMillis();
        startLogcatCapture();

        createAllCaseFile(SystemEnv.priority);
        getAllCaseFile();
        parseAllCases();

        try {
            // IDevice device = DeviceUtil.createDevice(serial);
            logger.info("run command for device:" + serial);
            logger.info("total test count:" + allCases.size());

            closeWifi(10000);
            runAllWifiOffCases();

            openWifi(30000);
            runAllWifiOnCases();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("run command failed:" + ex.getMessage());
        }
        logger.info("用例执行完成.");
        long now = System.currentTimeMillis();
        usedTime = (int) ((now - startTime) / 1000);
        stopLocat();

        runResult.setSucceed(true);
        runResult.setResult("共执行" + this.getAllCaseCount() + "个用例. 成功"
                + this.getAllSucceedCaseCount() + "个, 失败:"
                + this.getAllFailedCaseCount() + "个");
        
        doStartupTimeTest();
       

        afterTest();
        
        if (listener != null)
            listener.onFinish(serial, runResult);
    }
    
    private void doStartupTimeTest(){
//        List<String> serials = DeviceMonitor.getInstance().getOnlineDeviceSerials();
//
//        for(String serial:serials){
//            
//        }
        AppStartupTimeTest appStartupTimeTest = new AppStartupTimeTest();
        IDevice createDevice = DeviceUtil.createDevice(serial);
        appStartupTimeTest.perform(createDevice);
        startUpTimeAvg = appStartupTimeTest.getStartUpTimeAvg();
    }
    
    private String getCaseLevel(){
        if(SystemEnv.priority.equals("all")) return "全部用例";
        return SystemEnv.priority;
    }

    private void afterTest() {
        logger.info("afterTest.");
        stopLocat();

        collectTestResult();
        
        parseProp();

        createTestInfo();

        // 保存到本地
        saveTestInfo();
        
        logger.info("save Failed TestCases.");
        saveTestCases(this.allFailedCases, Constant.TESTS_FAILS);
        
        logger.info("save succeed TestCases.");
        saveTestCases(this.allSucceedCases, Constant.TESTS_SUCCEEDS);

        saveData();
        
        sendTestResultEmail();
    }
    
    private void sendTestResultEmail(){
        
        //如果是今天最后一次全测试,  那么发送汇总的结果
        if(isLastRoundOfToday()){
            sendDailyFullTestResult();
        } else {
            sendNormalFullTestResult();
        }
        
    }
    
    //发送普通的单次全用例测试结果邮件
    private void sendNormalFullTestResult(){
        try{
            String mailContent = generateMailContent(false);
            List<String> attached = getAttachedFiles();
            String inlinePng1 = createTestCaseCountBarImg();
           
            MailSender.getInstance().sendHtmlMail(MailSender.defaultRecipients,
                    "[自动化测试结果邮件]用例级别:"+getCaseLevel()+", 请查看测试报告", mailContent, attached, inlinePng1);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    //发送每天的全用例测试结果,  汇总结果
    private void sendDailyFullTestResult(){
        try{
            String mailContent = generateMailContent(true);
            List<String> attached = getAttachedFiles();
            String inlinePng1 = createTestCaseCountBarImg();
           
            MailSender.getInstance().sendHtmlMail(getMailRecipients(),
                    "[自动化测试结果邮件]今天的测试结果,请查看测试报告", mailContent, attached, inlinePng1);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private List<String> getMailRecipients() {
        List<String> recipients = new ArrayList<String>();
        recipients.add("shaopengxiang@kingsoft.com");
        if(!SystemEnv.isTestingMode()){
            recipients.add("jiangke@kingsoft.com");
            recipients.add("GuoQin@kingsoft.com");
            recipients.add("taohongxia@kingsoft.com");
            recipients.add("gaoxiang1@kingsoft.com");
        }

        return recipients;
    }
    
    //判断当前的测试,是否是今天最后一次
    private static boolean isLastRoundOfToday(){
        String now = Util.getTimeStr("HH:mm");
        if (now.compareTo(SystemEnv.loopEndTime) > 0 || now.compareTo(SystemEnv.loopStartTime) < 0) {
            return true;
        }
        return false;
    }

    private void cleanupWorkspace() {
        Util.cleanDirectroy("testreport/" + serial);
    }

    private void collectTestResult() {
        logger.info("collectTestResult.");
        //List<String> cmdOutput = Util.getCmdOutput("cmd /C start /wait "+"adb -s d926b5c4 pull /sdcard/Robotium-Screenshots d:/log/b");
        List<String> cmdOutput = Util.getCmdOutput("cmd /C start /wait "+"adb -s " + serial
                + " pull /sdcard/Robotium-Screenshots testreport/" + serial);
        logger.info("collect vidio record");
        cmdOutput = Util.getCmdOutput("cmd /C start /wait "+"adb -s " + serial
                + " pull /sdcard/powerword/rec testreport/" + serial);
        
        logger.info("collect prop");
        cmdOutput = Util.getCmdOutput("adb -s " + serial + " shell getprop", true);
        Util.createFile("testreport/" + serial + "/prop.txt", cmdOutput);

        logger.info("collect meminfo");
        cmdOutput = Util.getCmdOutput("adb -s " + serial
                + " shell cat /proc/meminfo", true);
        Util.createFile("testreport/" + serial + "/meminfo.txt", cmdOutput);
    }
    
    
    private int getLastRunId() {
        List<UseCaseRunResult> lastTestResult = UseCaseDataGenerator
                .getInstance().getLastTestResult(serial, 64);
        
        int maxRunId = 1;
        if (lastTestResult != null && lastTestResult.size()!=0) {
            for(int i=lastTestResult.size()-1;i>=0;i--){
                UseCaseRunResult urc = lastTestResult.get(i);
                String ids = urc.getValue("run_id");
                System.out.println(urc.getName()+", ids:"+ids);
                if (Util.isNull(ids)) {
                    continue;
                } else {
                    try {
                        int id = Integer.parseInt(ids);
                        if(id>maxRunId){
                            maxRunId = id;
                        }
                    }catch(Exception ex){
                        
                    } 
                }
                
            }
            mRunId = maxRunId;
            
        }
        return mRunId;
    }

    private void saveTestInfo() {
        logger.info("saveTestInfo.");
        int lastId = getLastRunId();
        StringBuilder sb = new StringBuilder();
        sb.append("run_id:").append(++lastId)
        .append("\r\n");
        sb.append("testing_time:").append(mTestInfo.getTestStartTime())
                .append("\r\n");
        sb.append("testing_duration:").append(mTestInfo.getTestduration())
                .append("\r\n");
        sb.append("testing_coverage:").append(mTestInfo.getCoverage())
                .append("\r\n");
        sb.append("usecase_all:").append(mTestInfo.getUseCaseCount())
                .append("\r\n");
        sb.append("usecase_fail:").append(mTestInfo.getUseCaseFailCount())
                .append("\r\n");
        sb.append("usecase_wifi_count:")
                .append(mTestInfo.getWifiUseCaseCount()).append("\r\n");
        sb.append("usecase_wifi_fail_count:")
                .append(mTestInfo.getWifiUseCaseFailCount()).append("\r\n");
        sb.append("phone_name:").append(mTestInfo.getPhoneName())
                .append("\r\n");
        sb.append("phone_cpu:").append(mTestInfo.getPhoneCpu()).append("\r\n");
        sb.append("apk_size:").append(mTestInfo.getApkSize()).append("\r\n");
        sb.append("android_os_build:").append(mTestInfo.getAndroidOsBuild())
                .append("\r\n");
        sb.append("lint_warings:").append(mTestInfo.getLintWaringCount())
                .append("\r\n");
        sb.append("findbugs_warings:")
                .append(mTestInfo.getFindbugsWaringCount()).append("\r\n");
        sb.append("code_lines:").append(mTestInfo.getCodeLines())
                .append("\r\n");
        sb.append("phone_mem_size:").append(mTestInfo.getPhoneMemSize())
                .append("\r\n");
        sb.append("startup.time.avg:").append(this.startUpTimeAvg)
                .append("\r\n");
        Util.createFile(this.getWorkspace() + "/final.txt", sb.toString());
        
        
    }
    
    private void saveTestCases(List<UseCase> cases, String saveFileName){
        
        if(cases!=null && cases.size()>0){
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<cases.size();i++){
                UseCase useCase = cases.get(i);
                sb.append(useCase.getAttr("classname") + "."
                        + useCase.getAttr("name")).append("\r\n");
            }
            Util.createFile(this.getWorkspace() + "/"+saveFileName, sb.toString());
        }
        
    }
    
    

    private void parseProp() {
        prop = new DeviceProp(serial);
    }

    private void createTestInfo() {
        logger.info("createTestInfo.");
        mTestInfo = new TestInfo(serial);
        mTestInfo.setAndroidOsBuild(prop.getValue("ro.build.version.release"));

        mTestInfo.setTestStartTime(Util.getTimeStr("yyyy-MM-dd HH:mm:ss",
                startTime) + "");
        mTestInfo.setTestduration((System.currentTimeMillis() - startTime)
                / 1000 + "s");
        mTestInfo.setUseCaseCount(this.getAllCaseCount());
        mTestInfo.setUseCaseFailCount(this.getAllFailedCaseCount());
        mTestInfo.setWifiUseCaseCount(this.getAllWifiOnCaseCount());
        mTestInfo.setWifiUseCaseFailCount(this.getAllWifiOnCaseCount()
                - this.getAllSucceedWifiOnCaseCount());
        mTestInfo.setPhoneName(prop.getValue("ro.build.product"));
        mTestInfo.setPhoneCpu(prop.getValue("ro.product.cpu.abi"));
        mTestInfo.setStartUpTime(this.startUpTimeAvg);
    }

    private List<String> getAttachedFiles() {
        List<String> attached = new ArrayList<String>();
        createSingleErrorListFile();
        if (Util.isFileExist(getWorkspace() + "/" + UNION_ERRORS_FILE)) {
            attached.add(getWorkspace() + "/" + UNION_ERRORS_FILE);
        }

//        // 准备图表图片文件
//        try {
//            List<String> imgFiles = createTestCaseCountBarImg();
//            attached.addAll(imgFiles);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

        return attached;
    }
    
    private List<BarDataBean> getTestCaseBarData(List<UseCaseRunResult> lastTestResult){
        List<BarDataBean> barData = new ArrayList<BarDataBean>();
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < lastTestResult.size(); i++) {
            BarDataBean data = new BarDataBean();
            
            UseCaseRunResult result = lastTestResult.get(i);
            String value = result.getValue("usecase_all");
            data.setTotal(0);
            data.setFailed(0);
            data.setTitle("#");
            try {
                data.setTotal((int) Double.parseDouble(result.getValue("usecase_all")));
                data.setFailed((int) Double.parseDouble(result.getValue("usecase_fail")));
            } catch (Exception e) {
                //e.printStackTrace();
                continue;
            }
            String runId = result.getValue("run_id");
//            if(!Util.isNull(result.getName())){
//                runId = result.getName();
//            }
//            else 
            if(Util.isNull(runId)){
                runId = ""+(mRunId++);
            }
            if(!set.contains(runId))
                set.add(runId);
            else {
                runId = "#"+runId;
                set.add(runId);
            }
            
            data.setTitle(runId);
            
            if(SystemEnv.isTestingMode()){
                barData.add(data);
            } else{
                if (data.getTotal() != 0 && data.getTotal() > 100
                        && data.getFailed() < 100) {
                    barData.add(data);
                } 
            }
           
            
            
        }
        return barData;
    }

    private String createTestCaseCountBarImg() {
        List<UseCaseRunResult> lastTestResult = UseCaseDataGenerator
                .getInstance().getLastTestResult(serial, 32);
        logger.info("lastTestResult.size:" + lastTestResult.size());
        List<BarDataBean> barDataBeans = getTestCaseBarData(lastTestResult);
        if(barDataBeans.size()==0) {
            logger.info("no history data can be used in bar chart img!");
            return null;
        }
        
        HashSet<String> set = new HashSet<String>();
        double[][] data = new double[2][barDataBeans.size()];
        String[] titles = new String[barDataBeans.size()];
        for(int i=0;i<barDataBeans.size();i++){
            BarDataBean barDataBean = barDataBeans.get(i);
            data[0][i] = barDataBean.getTotal();
            data[1][i] = barDataBean.getFailed();
            String title = barDataBean.getTitle();
            if(!set.contains(title)){
                set.add(title);
            }else{
                while(set.contains(title))
                    title = "#"+title;
                set.add(title);
            }
            titles[i] = title;
        }

        String time = Util.getTimeStr("yyyy-MM-dd_HHmmss");
        Util.makeDir("data/img/" + serial);
        String pngFile = "data/img/" + serial + "/bar_" + time + ".png";

        
        
        UseCaseDataGenerator.getInstance().createBarChart(serial, data, titles,
                pngFile);

        List<String> fileList = new ArrayList<String>();
        fileList.add(pngFile);
        return pngFile;
    }

    private void createSingleErrorListFile() {
        List<String> unionContents = new ArrayList<String>();
        for (UseCase uc : allFailedCases) {
            unionContents.add(uc.getAttr("classname") + "."
                    + uc.getAttr("name") + ":\r\n");
            unionContents.add(uc.getError() + "\r\n");
            unionContents.add(uc.getStack() + "\r\n");
        }

        Util.createFile(getWorkspace() + "/" + UNION_ERRORS_FILE, unionContents);
    }

    private String generateMailContent(boolean dailyReport) {
        String fileContent = Util.getFileContent("data/testresult.html");
        MailContentBuilder mailContentBuilder = new MailContentBuilder(
                fileContent, allCases, allFailedCases, allSucceedCases,
                mTestInfo);
        mailContentBuilder.build(dailyReport);
        String mailContent = mailContentBuilder.buildHtmlMailContent();
        return mailContent;
    }

    /**
     * 保存当前测试数据到备份目录
     */
    private void saveData() {
        logger.info("saveData...");
        String time = Util.getTimeStr("yyyy-MM-dd_HHmmss");
        Util.copyFolder("testreport/" + serial + "", "data/backup/workspace/"
                + serial + "/" + time);
    }

    private void closeWifi(int milli) {
        Util.getCmdOutput("adb -s " + serial
                + " shell am broadcast -a kingsoft.test.cmd.action.closewifi");
        Util.getCmdOutput("adb -s "
                + serial
                + " shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
        Util.sleep(milli);
    }

    private void openWifi(int milli) {
        Util.getCmdOutput("adb -s " + serial
                + " shell am broadcast -a kingsoft.test.cmd.action.openwifi");
        Util.sleep(milli);
    }

    private void runOneUseCase(int index, boolean wifi, UseCase uc) {
        String className = uc.getAttr("classname");
        String method = uc.getAttr("name");
        String test = className + "#" + method;
        final String command = "adb -s " + serial
                + " shell am instrument -w -e class " + className + "#"
                + method
                + " com.kingsoft.test/android.test.InstrumentationTestRunner";
        try {
            logger.info("开始执行第" + index + "个用例: " + test + "  ("
                    + (wifi ? "wifi on" : "wifi off") + ")");
            long startRunTime = System.currentTimeMillis();
            final Object lock = new Object();
            final List<String> cmdOutput = new ArrayList<String>();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    List<String> ouput = Util.getCmdOutput(command);
                    cmdOutput.addAll(ouput);
                    synchronized(lock){
                        lock.notify();
                    }
                }
            }).start();
            synchronized(lock){
                lock.wait(300*1000);
            }
            
            
            long used = System.currentTimeMillis() -startRunTime;
            used = used/1000;
            if(used<6) used =6;
            if(used>120) used = 120;
            
            //onTestFinished(uc, test, cmdOutput, true);
            
            if (!onTestFinished(uc, test, cmdOutput, false)) {
                logger.info("再次尝试执行失败的用例: "+ test+".");
                if(wifi){
                    this.openWifi(12000);
                }else{
                    this.closeWifi(5000);
                }
                new ScreenRecorder(serial, (int) used, test+".mp4").start();
                
                cmdOutput.clear();
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        List<String> ouput = Util.getCmdOutput(command);
                        cmdOutput.addAll(ouput);
                        synchronized(lock){
                            lock.notify();
                        }
                    }
                }).start();
                synchronized(lock){
                    lock.wait(300*1000);
                }
                onTestFinished(uc, test, cmdOutput, true);
            }
            
            Util.sleep(500);
        } catch (Throwable ex) {
            ex.printStackTrace();
            logger.severe("命令: " + test + " 执行失败!");
        }
    }

    private boolean onTestFinished(UseCase useCase, String test,
            List<String> cmdOutput, boolean addToFail) {
        boolean isSucceed = isSucceed(cmdOutput);
        if (!isSucceed) {
            logger.info("用例: " + test + " 执行失败!");
            testInfo.append(test + "  Failed!!!!\r\n");
            useCase.setError(getErrorMsg(test, cmdOutput));
            useCase.setStack(getStack(cmdOutput));
            useCase.setPassed(false);
            if(addToFail){
                allFailedCases.add(useCase);
            }
            
            logger.info("error:" + useCase.getError());
            logger.info("stack:" + useCase.getStack());
            return false;
        } else {
            logger.info("用例: " + test + " 执行成功!");
            testInfo.append(test + "  PASS!\r\n");
            useCase.setPassed(true);
            allSucceedCases.add(useCase);
            return true;
        }
    }

    private boolean isSucceed(List<String> cmdOutput) {
        boolean succeed = false;
        for (String s : cmdOutput) {
            if (s.contains("FAILURES!!!"))
                return false;
            if (s.contains("OK (1 test)"))
                return true;
        }
        return succeed;
    }

    private String getStack(List<String> cmdOutput) {
        StringBuilder stack = new StringBuilder();
        boolean start = false;
        for (String s : cmdOutput) {
            if (s.contains("Failure in"))
                start = true;
            if (start) {
                stack.append(s + "\r\n");
            }
        }
        return stack.toString();
    }

    private String getErrorMsg(String test, List<String> cmdOutput) {
        String tag = "junit.framework.AssertionFailedError:";
        for (String s : cmdOutput) {
            if (s.contains(tag)) {
                String error = s.substring(s.indexOf(tag) + tag.length());
                return error;
            }
        }
        tag = "INSTRUMENTATION_RESULT: shortMsg=";
        for (String s : cmdOutput) {
            if (s.contains(tag)) {
                String error = s.substring(s.indexOf(tag) + tag.length());
                return error;
            }
        }
        tag = "INSTRUMENTATION_RESULT: longMsg=";
        for (String s : cmdOutput) {
            if (s.contains(tag)) {
                String error = s.substring(s.indexOf(tag) + tag.length());
                return error;
            }
        }
        tag = "Caused by:";
        for (String s : cmdOutput) {
            if (s.contains(tag)) {
                String error = s.substring(s.indexOf(tag) + tag.length());
                return error;
            }
        }

        for (String s : cmdOutput) {
            if (s.contains(test) && s.contains("(")) {
                String error = s;
                return error;
            }
        }

        return "";
    }

    private void createAllCaseFile(String priority) {
        List<String> cmdOutput = Util
                .getCmdOutput("adb -s "
                        + serial
                        + " shell am instrument -e writecases true -e priority "+priority+" -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner");
    }

    private void getAllCaseFile() {
        List<String> cmdOutput = Util.getCmdOutput("adb -s " + serial
                + " pull /sdcard/powerword/allcases.xml " + this.getWorkspace()
                + "/allcases.xml");
    }

    private void parseAllCases() {
        XmlReportParser parser = new XmlReportParser();
        parser.addXmlFile(this.getWorkspace() + "/allcases.xml");
        parser.doParse();
        allCases = parser.getTotalUseCases();
    }

    private String getWorkspace() {
        return "testreport/" + serial;
    }

    int index = 1;

    private void runAllWifiOffCases() {
        logger.info("wifi off test case count:" + getAllWifiOffCaseCount());

        for (int i=0;i<allCases.size() && i<TestingConfig.TESTCASE_COUNT_MAX; i++) {
            UseCase uc = allCases.get(i);
            if (!uc.isWifiRequired()) {
                runOneUseCase(index++, false, uc);
            }
        }
    }

    private void runAllWifiOnCases() {
        logger.info("wifi on test case count:" + getAllWifiOnCaseCount());
        for (int i=0;i<allCases.size() && i<TestingConfig.TESTCASE_COUNT_MAX; i++) {
            UseCase uc = allCases.get(i);
            if (uc.isWifiRequired()) {
                runOneUseCase(index++, true, uc);
            }
        }
    }

    public int getAllCaseCount() {
        return allCases.size();
    }

    public int getAllFailedCaseCount() {
        return allFailedCases.size();
    }

    public int getAllSucceedCaseCount() {
        return allSucceedCases.size();
    }

    public int getAllWifiOnCaseCount() {
        int count = 0;
        for (UseCase uc : allCases) {
            if (uc.isWifiRequired()) {
                count++;
            }
        }
        return count;
    }

    public int getAllSucceedWifiOnCaseCount() {
        int count = 0;
        for (UseCase uc : allSucceedCases) {
            if (uc.isWifiRequired()) {
                count++;
            }
        }
        return count;
    }

    public int getAllWifiOffCaseCount() {
        int count = 0;
        for (UseCase uc : allCases) {
            if (!uc.isWifiRequired()) {
                count++;
            }
        }
        return count;
    }

    public int getAllSucceedWifiOffCaseCount() {
        int count = 0;
        for (UseCase uc : allSucceedCases) {
            if (!uc.isWifiRequired()) {
                count++;
            }
        }
        return count;
    }

    public boolean isFinished() {
        return false;
    }

    public void stopRun() {
        this.interrupt();
    }

    public boolean isSucceed() {
        return false;
    }

    // 测试耗时:秒
    public int getRunConsumedTime() {
        return usedTime;
    }

    private void stopLocat() {
        logger.info("停止logcat");
        logcatTask.stop();
    }

    private void startLogcatCapture() {
        try {
            logcatWriter = new PrintWriter(new File("testreport/" + serial
                    + "/logcat.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        IDevice device = DeviceUtil.createDevice(serial);
        if (device != null && device.isOnline()) {
            logcatTask = new LogCatReceiverTask(device);
            logcatTask.addLogCatListener(this);
            new Thread(logcatTask).start();
        }

    }

    @Override
    public void log(List<LogCatMessage> msgList) {
        if (logcatWriter == null || msgList == null)
            return;
        for (LogCatMessage lcm : msgList) {
            logcatWriter.println(lcm.toString());
        }
        logcatWriter.flush();

        addLogcatMessages(msgList);
    }

    public StringBuilder getLastestLogcatMessages() {
        StringBuilder sb = new StringBuilder();
        synchronized (lastestMessage) {
            for (LogCatMessage lcm : lastestMessage) {
                sb.append(lcm.toString() + "\r\n");
            }
        }
        return sb;
    }

    private LinkedList<LogCatMessage> lastestMessage = new LinkedList<LogCatMessage>();

    private void addLogcatMessages(List<LogCatMessage> msgList) {
        synchronized (lastestMessage) {
            for (LogCatMessage lcm : msgList) {
                lastestMessage.add(lcm);
            }
        }
        trimLogcatMessageList();
    }

    private void trimLogcatMessageList() {
        synchronized (lastestMessage) {
            while (lastestMessage.size() > 200) {
                lastestMessage.removeFirst();
            }
        }

    }

    public String getTestInfo() {
        return testInfo.toString();
    }

    public String getSerial() {
        return serial;
    }

    public String getPhoneName() {
        return prop.getValue("ro.build.product");
    }

    public static void main(String[] args) {
        DeviceMonitor.getInstance().findDevices();
        AndroidDebugBridge.init(false);
        InstrumentTestRunner runner = new InstrumentTestRunner("0149C6F415019008",
                null);
        //runner.collectTestResult();
        
        //测试图表的生成
        String createTestCaseCountBarImg = runner.createTestCaseCountBarImg();
        System.out.println("createTestCaseCountBarImg:"+createTestCaseCountBarImg.toString());
        
        //判断是否是当天最后一轮测试
        System.out.println("is last round:"+runner.isLastRoundOfToday());
        
//        int lastRunnerId = runner.getLastRunId();
//        System.out.println("lastRunnerId:"+lastRunnerId);
    }

}
