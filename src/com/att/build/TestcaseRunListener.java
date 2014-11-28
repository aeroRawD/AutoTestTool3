package com.att.build;

public interface TestcaseRunListener {

    public void onTestFailed();
    
    public void onTestFinished();
}
