package com.att.report;

import java.util.ArrayList;
import java.util.List;

import com.att.Constant;
import com.spx.adb.Util;

public class HistoryTestResult {
    
    private String serial = null;
    
    List<String> succeedTestCases = new ArrayList<String>();
    List<String> failedTestCases = new ArrayList<String>();

    private TestInfo testInfo = null;
    private String filePath = null;

    public HistoryTestResult(String serial, String filePath) {
        this.serial = serial;
        this.filePath = filePath;
        init();
    }

    private void init() {
        String testInfoFileName = filePath+"/final.txt";
        testInfo = TestInfo.createTestInfoFromFile(serial, testInfoFileName);
        
        failedTestCases = Util.getFileContentLines(filePath+"/"+Constant.TESTS_FAILS);
        succeedTestCases = Util.getFileContentLines(filePath+"/"+Constant.TESTS_SUCCEEDS);
    }
    
    public List<String> getSucceedTestCases() {
        return succeedTestCases;
    }

    public void setSucceedTestCases(List<String> succeedTestCases) {
        this.succeedTestCases = succeedTestCases;
    }

    public List<String> getFailedTestCases() {
        return failedTestCases;
    }

    public void setFailedTestCases(List<String> failedTestCases) {
        this.failedTestCases = failedTestCases;
    }

    public TestInfo getTestInfo() {
        return testInfo;
    }

    public void setTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }
    
    public boolean isTestFailed(String test){
        if(Util.isNull(test)) return false;
        for(String s:failedTestCases){
            if(test.equals(s)) return true;
        }
        
        return false;
    }

    public static void main(String[] args){
        String path ="data/backup/workspace/2008edd8f316/2014-11-27_085010";
        HistoryTestResult testResult = new HistoryTestResult("2008edd8f316", path);
        
        List<String> failedTestCases2 = testResult.getFailedTestCases();
        System.out.println("失败的用例数:"+failedTestCases2.size());
        
        List<String> succeedTestCases = testResult.getSucceedTestCases();
        System.out.println("成功的用例数:"+succeedTestCases.size());
        
        TestInfo testInfo = testResult.getTestInfo();
        if(testInfo!=null){
            System.out.println("android build:"+testInfo.getAndroidOsBuild());
            System.out.println("getPhoneCpu:"+testInfo.getPhoneCpu());
            System.out.println("getPhoneName:"+testInfo.getPhoneName());
            System.out.println("getStartUpTime:"+testInfo.getStartUpTime());
            System.out.println("getTestduration:"+testInfo.getTestduration());
            System.out.println("getTestStartTime:"+testInfo.getTestStartTime());
            System.out.println("getUseCaseCount:"+testInfo.getUseCaseCount());
            System.out.println("getUseCaseFailCount:"+testInfo.getUseCaseFailCount());
        }
    }
}
