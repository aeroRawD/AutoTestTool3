package com.att.build;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.att.build.TestCaseRunner.TestCommandListener;
import com.att.testcommand.TestCommand;
import com.log.Log;
import com.spx.adb.Util;

public class TestCaseRunnerHelper extends Thread implements TestCommandListener{
    private static Logger logger = Log.getSlientLogger("TestCaseRunnerHelper");
    private boolean stop = false;
    private TestCaseRunner runner = null;
    private int totalTestCount = -1;
    private int currentTestCount = -1;
    private int failedCount = -1;
    private int succedCount = -1;
    private boolean commandStarted = false;
    private long commandStartTime = 0L;
    private String currentTestInfo = "";
    private String serial = "";
    public String getSerial() {
        return serial;
    }

    private String localWorkspace = "";
    
    public TestCaseRunnerHelper(TestCaseRunner testRunner){
        this.runner =testRunner; 
        this.serial = runner.getSerial();
        localWorkspace = "testreport/"+serial+"";
        Util.makeDir(localWorkspace);
    }
    
    public boolean isTestStarted() {
        return totalTestCount != -1;
    }
    
    // 总共的测试用例数量
    public int getTotalTestCount(){
        
        if (totalTestCount >0) {
            return totalTestCount;
        }
        List<String> fileContent = new ArrayList<String>();
        if(!Util.getRemoteFileContent(runner.getSerial(), "/sdcard/powerword/tests_list.txt", fileContent)){
            return -1;
        }
        if(fileContent.size()>1){
            totalTestCount = fileContent.size();
        }
        
        return fileContent.size();
    }
    
    
    
    private void updateMeminfo(){
       List<String> cmdOutput = Util.getCmdOutput("adb -s "+serial+" shell dumpsys meminfo com.kingsoft", true);
       if(cmdOutput!=null){
           for(String line:cmdOutput){
               line= line.trim();
               
               if(line.startsWith("Dalvik Heap")){
                   //logger.info(line);
                   parseDalvik(line);
               } else if(line.startsWith("Dalvik")){
                   //logger.info(line);
                   parseDalvikHeap(line);
               } else if(line.startsWith("TOTAL")){
                   //logger.info(line);
                   parseTotal(line);
               }
           }
       }
    }
    
    private List<String> split(String line){
        java.util.StringTokenizer st = new StringTokenizer(line, " \t");
        List<String> result = new ArrayList<String>();
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            result.add(token);
        }
        return result;
    }
    
    private void parseDalvikHeap(String line){
        if(Util.isNull(line)) return;
        List<String> splits = split(line);
        //logger.info(""+line+", splits.size:"+splits.size());
        if(splits.size()==7){
            dalvikPss = splits.get(splits.size()-6);
            dalvikShareDirty = splits.get(splits.size()-5);
            dalvikPrivateDirty = splits.get(splits.size()-4);
            dalvikHeapSize = splits.get(splits.size()-3);
            dalvikHeapAlloc = splits.get(splits.size()-2);
            dalvikHeepFree = splits.get(splits.size()-1);
        }
    }
    
    private void parseDalvik(String line){
        if(Util.isNull(line)) return;
        List<String> splits = split(line);
        //logger.info(""+line+", splits.size:"+splits.size());
        if(splits.size()>=7){
            dalvikPss = splits.get(splits.size()-6);
            dalvikShareDirty = splits.get(splits.size()-5);
            dalvikPrivateDirty = splits.get(splits.size()-4);
            dalvikHeapSize = splits.get(splits.size()-3);
            dalvikHeapAlloc = splits.get(splits.size()-2);
            dalvikHeepFree = splits.get(splits.size()-1);
        }
    }
    
    private void parseTotal(String line){
        if(Util.isNull(line)) return;
        List<String> splits = split(line);
        //logger.info(""+line+", splits.size:"+splits.size());
        if(splits.size()>=7){
            totalPss = splits.get(splits.size()-6);
            totalShareDirty = splits.get(splits.size()-5);
            totalPrivateDirty = splits.get(splits.size()-4);
            totalHeapSize = splits.get(splits.size()-3);
            totalHeapAlloc = splits.get(splits.size()-2);
            totalHeepFree = splits.get(splits.size()-1);
        }
    }
    
    public void updateCurrentTestInfo(){
        List<String> fileContent = new ArrayList<String>();
        if(!Util.getRemoteFileContent(runner.getSerial(), "/sdcard/powerword/tests_result_list.txt", fileContent)){
            return;
        }
        
        currentTestCount = fileContent.size();
        
        currentTestInfo ="";
        for(String s:fileContent){
            currentTestInfo+=s+"\r\n";
        }
        
        failedCount = 0;
        succedCount = 0;
        for(String s:fileContent){
            if(s.endsWith("OK")) succedCount ++;
            else if(s.endsWith("FAIL")) failedCount ++;
        }
        
        String test=fileContent.get(fileContent.size()-1);
        addTestMemUse(test);
    }
    
    private void addTestMemUse(String test){
        if(Util.isNull(test) || test.indexOf(":")==-1){
            return;
        }
        test = test.substring(0, test.indexOf(":"));
        
        if(memUseList.size()==0) {
            MemUse memUse = new MemUse();
            memUse.index = 0;
            memUse.test = test;
            memUse.memAlloc = dalvikHeapAlloc;
            memUseList.add(memUse);
        }
        
        MemUse lastOne = memUseList.get(memUseList.size()-1);
        if(lastOne.test.equals(test)){
            return;
        }
        MemUse memUse = new MemUse();
        memUse.index = memUseList.size();
        memUse.test = test;
        memUse.memAlloc = dalvikHeapAlloc;
        memUseList.add(memUse);
    }
    
    private String getTestMemUseInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<memUseList.size();i++){
            MemUse mu = memUseList.get(i);
            sb.append(mu.index+","+mu.test+",\t\t"+mu.memAlloc+"\r\n");
        }
        return sb.toString();
    }
    
    //当前执行到的测试用例数
    public int getCurrentTestNum(){
//        List<String> fileContent = new ArrayList<String>();
//        if(!Util.getRemoteFileContent(runner.getSerial(), "/sdcard/powerword/tests_result_list.txt", fileContent)){
//            return -1;
//        }
        return currentTestCount;
    }
    
    public String getCurrentTestResult(){
//        List<String> fileContent = new ArrayList<String>();
//        if(!Util.getRemoteFileContent(runner.getSerial(), "/sdcard/powerword/tests_result_list.txt", fileContent)){
//            return null;
//        }
        return currentTestInfo;
    }
    
    public int getFailedTestCount(){
        return failedCount;
    }
    
    public int getSucceedTestCount(){
        return succedCount;
    }
    
    // 是否本轮测试结束
    public boolean isTestFinished() {
        return totalTestCount > 0 && getCurrentTestNum() == totalTestCount;
    }
    
    public boolean isRunnerDied(){
        return !runner.isAlive();
    }
    
    public int getRunningSeconds(){
        return 0;
    }

    public void stopRunner(){
        stop = true;
    }
    
    private String dalvikPss;
    private String dalvikShareDirty;
    private String dalvikPrivateDirty;
    private String dalvikHeapSize;
    private String dalvikHeapAlloc;
    private String dalvikHeepFree;
    
    private String totalPss;
    private String totalShareDirty;
    private String totalPrivateDirty;
    private String totalHeapSize;
    private String totalHeapAlloc;
    private String totalHeepFree;
    
    public void run(){
        while(!stop && !this.isInterrupted()){
            Util.sleep(5000);
            //logger.info("run()  commandStarted:"+commandStarted);
            if(!commandStarted){
                continue;
            }
            
            if(System.currentTimeMillis()-commandStartTime<10*1000){
                continue;
            }
            String info = "test info for device:"+serial+": ";
            info+="total:"+getTotalTestCount();
//            logger.info("test info for device:"+serial);
//            logger.info("total test:"+getTotalTestCount());
            
            updateCurrentTestInfo();
            info+=", tested:"+getCurrentTestNum()+", pass:"+getSucceedTestCount()+",fail:"+getFailedTestCount();
//            logger.info("now tested:"+getCurrentTestNum());
//            logger.info("Passed:"+getSucceedTestCount()+", Failed:"+getFailedTestCount());
//            logger.info("Failed:"+getFailedTestCount());
            logger.info(info);
//            String testResult = getCurrentTestResult();
//            logger.info(testResult);
            updateMeminfo();
            info = "dalvikHeapSize:"+dalvikHeapSize+", totalHeapAlloc:"+totalHeapAlloc+", dalvikHeapAlloc:"+dalvikHeapAlloc;
            logger.info(info);
            
            if(!runner.isAlive()){
                break;
            }
        }
    }

    @Override
    public void onCommandStarted(TestCommand tc) {
        totalTestCount = -1;
        currentTestCount = -1;
        failedCount = -1;
        succedCount = -1;
        commandStarted = true;
        commandStartTime = System.currentTimeMillis();
    }

    List<InstrumentTestResult> results = new ArrayList<InstrumentTestResult>();
    @Override
    public void onCommandFinished(TestCommand tc) {
        InstrumentTestResult result = new InstrumentTestResult();
        result.setTotal(totalTestCount);
        result.setFailed(failedCount);
        result.setTested(currentTestCount);
        result.setSucceed(succedCount);
        //result.setTestOuput(getCurrentTestResult());
        result.setTestOuput("Errors:"+tc.getErrorMsg()+"\r\n" +
        		"shortMsg:"+tc.getErrorMsg()+"\r\n" +
        		"longMsg:"+tc.getLongMsg());
        if(!Util.isNull(tc.getErrorMsg())){
            result.setTestOuput("Errors:"+tc.getErrorMsg()+"\r\n" +
                    "shortMsg:"+tc.getErrorMsg()+"\r\n" +
                    "longMsg:"+tc.getLongMsg()+"\r\n"+
                    runner.getLastestLogcatMessages().toString()+"\r\n"+
                    this.getTestMemUseInfo());
        }
        results.add(result);
        commandStarted = false;
    }

    public List<InstrumentTestResult> getResults() {
        return results;
    }
    
    private List<MemUse> memUseList = new ArrayList<MemUse>();
    class MemUse{
        int index;
        String test;
        String memAlloc;
    }
}
