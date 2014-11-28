package com.att.build;

public interface TestcaseRunningListener {
	public void onFinish(String serial, TestResult testResult);
}
