package com.att.build;

public class InstrumentTestResult {
    
    private int total;
    private int failed;
    private int succeed;
    private int tested;
    private String testOuput;
    
    public InstrumentTestResult(){
        
    }

    public int getTested() {
        return tested;
    }

    public void setTested(int tested) {
        this.tested = tested;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSucceed() {
        return succeed;
    }

    public void setSucceed(int succeed) {
        this.succeed = succeed;
    }

    public String getTestOuput() {
        return testOuput;
    }

    public void setTestOuput(String testOuput) {
        this.testOuput = testOuput;
    }
    
    public String toString(){
        String ret ="总用例数:"+total+", 执行数目:"+tested+", 失败:"+failed+",成功:"+succeed+"\r\n";
        ret+="errors:"+testOuput;
        return ret;
    }
}
