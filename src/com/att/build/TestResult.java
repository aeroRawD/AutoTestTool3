package com.att.build;

public class TestResult {
	private String errorMsg = "";
	private String result = "";
	private boolean succeed = false;
	
	
	public boolean isSucceed() {
        return succeed;
    }
    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    
}
