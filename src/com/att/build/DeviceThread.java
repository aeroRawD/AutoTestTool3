package com.att.build;

import java.util.ArrayList;
import java.util.List;

import com.att.report.*;

public class DeviceThread extends Thread{
    private String serial;
    private List<UseCase> allCases = new ArrayList<UseCase>();
    private List<UseCase> allFailedCases = new ArrayList<UseCase>();
    private List<UseCase> allSucceedCases = new ArrayList<UseCase>();

    public DeviceThread(String s) {
        this.serial = s;
    }
    
    private void createAllCaseFile(){
        
    }
    
    private void getAllCaseFile(){
        
    }
    
    private void parseAllCases(){
        
    }
    
    public void run(){
        
        createAllCaseFile();
        getAllCaseFile();
        parseAllCases();
        
        
    }
    
    private void runAllWifiOffCases(){
        
    }
    
    private void runAllWifiOnCases(){
        
    }
    
    public int getAllCaseCount(){
        return allCases.size();
    }
    
    public int getAllFailedCaseCount(){
        return allFailedCases.size();
    }
    
    public int getAllSucceedCaseCount(){
        return allSucceedCases.size();
    }
    
    public int getAllWifiOnCaseCount(){
        int count =0;
        for(UseCase uc:allCases){
            if(uc.isWifiRequired()){
                count++;
            }
        }
        return count;
    }
    
    public int getAllSucceedWifiOnCaseCount(){
        int count =0;
        for(UseCase uc:allSucceedCases){
            if(uc.isWifiRequired()){
                count++;
            }
        }
        return count;
    }
    
    public int getAllWifiOffCaseCount(){
        int count =0;
        for(UseCase uc:allCases){
            if(!uc.isWifiRequired()){
                count++;
            }
        }
        return count;
    }
    
    public int getAllSucceedWifiOffCaseCount(){
        int count =0;
        for(UseCase uc:allSucceedCases){
            if(!uc.isWifiRequired()){
                count++;
            }
        }
        return count;
    }
}
