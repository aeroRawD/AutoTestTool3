package com.att.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.spx.adb.Util;

public class LintResult {
    private String lintFile ;
    private List<String> data = new ArrayList<String>();
    private List<String> warningList = new ArrayList<String>();
    
    private static List<String> START_TAGS = new ArrayList<String>();
    static {
        START_TAGS.add("AndroidManifest.xml");
        START_TAGS.add("src");
        START_TAGS.add("res");
        START_TAGS.add("assets");
        START_TAGS.add("local.properties");
        START_TAGS.add("project.properties");
    }
    
    public LintResult(String lintFile){
        this.lintFile = lintFile;
        init();
    }
    
    
    
    private void init(){
        data = Util.getFileContentLines(lintFile, "gbk");
        
        boolean startParse = false;
        boolean startOneWarning = false;
        String warning = "";
        int count = 0;
        for(int i=0;i<data.size()-1;i++){
            String line = data.get(i);
            if(Util.isNull(line)) continue;
            if(!startParse){
                if(line.startsWith("Scanning ")) continue;
                if(isStartWithTags(line)){
                    startParse = true;
                    i--;
                    continue;
                }
            }else{
                if(isStartWithTags(line)){
                    warningList.add(warning);
                    count =0;
                    warning = "";
                    
                    startOneWarning = true;
                    warning+=line+"\r\n";
                    count++;
                }else{
                    startOneWarning = false;
                    warning+=line+"\r\n";
                    count++;
                }
            }
        }
        
        if(!Util.isNull(warning)){
            warningList.add(warning);
        }
        
        System.out.println("w.size:"+warningList.size());
    }
    
    private boolean isStartWithTags(String line){
        for(String tag:START_TAGS){
            if(line.startsWith(tag)) return true;
        }
        return false;
    }
    
    private String getLastLine(){
        if(data.size()<2) return null;
        String lastLine = data.get(data.size()-1);
        if(lastLine.contains("errors,")&& lastLine.contains("warnings"))
            return lastLine;
        return null;
    }
    
    public int getErrorCount(){
        String lastLine =getLastLine();
        String numbers = lastLine.substring(0, lastLine.indexOf("errors"));
        return Integer.parseInt(numbers.trim());
    }
    
    public int getWaringsCount(){
        String lastLine =getLastLine();
        String numbers = lastLine.substring(lastLine.indexOf("errors,")+7, lastLine.indexOf("warnings"));
        return Integer.parseInt(numbers.trim());
    }
    
    
    
    public List<String> getWarningList() {
        return warningList;
    }


    public String diffWith(LintResult lresult){
        StringBuilder diff = new StringBuilder();
//        diff.append(getLastLine()+"\r\n");
        StringBuilder warningAddDesc = new StringBuilder();
        int myError = this.getErrorCount();
        int yourError = lresult.getErrorCount();
        if(myError!=yourError){
            if(myError<yourError){
                //warningAddDesc.append("error减少了"+(yourError-myError)+"个;  ");
            } else{
                warningAddDesc.append("error增加了"+(myError-yourError)+"个;  ");
            }
        }else{
            warningAddDesc.append("error数量不变;  ");
        }
        
        int myWarings = this.getWaringsCount();
        int yourWarings = lresult.getWaringsCount();
        if (myWarings != yourWarings) {
            if(myWarings<yourWarings){
                //warningAddDesc.append("warings减少了"+(yourWarings-myWarings)+"个; ");
            } else{
                warningAddDesc.append("warings增加了"+(myWarings-yourWarings)+"个; ");
            }
        } else {
            warningAddDesc.append("warings数量不变; ");
        }
        warningAddDesc.append("\r\n");
        warningAddDesc.append("\r\n");
        
        List<String> myWarnings = getWarningList();
        List<String> yourWarnings = lresult.getWarningList();
        String diffWarnings = diffWaringsList(myWarnings, yourWarnings);
        
        int waringAddCount =Util.countSubstring(diffWarnings, "增加的");
        if(waringAddCount>0){
            diff.append("warings增加了"+waringAddCount+"个; \r\n");
        }else{
            diff.append("warings数量不变; \r\n");
        }
        diff.append(diffWarnings);
        return diff.toString();
    }
    
    private static final int MAX_LENGTH_OF_DIFF = 2000;
    private String diffWaringsList(List<String> _myWarnings, List<String> _yourWarnings){
        HashMap<String, String> myMap = new HashMap<String, String>();
        HashMap<String, String> yourMap = new HashMap<String, String>();
        
        List<String> myWarnings = new ArrayList<String>();
        List<String> yourWarnings = new ArrayList<String>();
        for(int i=0;i<_myWarnings.size();i++){
            String s = _myWarnings.get(i);
            String raw = s;
            if(s.indexOf(":")!=-1){
                String fileName = s.substring(0, s.indexOf(":"));
                s = s.substring(s.indexOf(":")+1);
                
                if(s.indexOf(":")!=-1){
                    s = s.substring(s.indexOf(":")+1);
                    s = fileName+":"+s;
                    myWarnings.add(s);
                    myMap.put(s, raw);
                }
            }
        }
        for(int i=0;i<_yourWarnings.size();i++){
            String s = _yourWarnings.get(i);
            String raw = s;
            if(s.indexOf(":")!=-1){
                String fileName = s.substring(0, s.indexOf(":"));
                s = s.substring(s.indexOf(":")+1);
                if(s.indexOf(":")!=-1){
                    s = s.substring(s.indexOf(":")+1);
                    s = fileName+":"+s;
                    yourWarnings.add(s);
                    yourMap.put(s, raw);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for(String s:myWarnings){
            if(!yourWarnings.contains(s)){
                if(myMap.get(s)!=null){
                    s = myMap.get(s);
                }
                sb.append("增加的告警:"+s+"\r\n");
            }
            if(sb.length()>MAX_LENGTH_OF_DIFF) return sb.toString();
        }
//        for(String s:yourWarnings){
//            if(!myWarnings.contains(s)){
//                if(yourMap.get(s)!=null){
//                    s = yourMap.get(s);
//                }
//                sb.append("减少的告警:"+s+"\r\n");
//            }
//            if(sb.length()>MAX_LENGTH_OF_DIFF) return sb.toString();
//        }
        
        return sb.toString();
    }
   
    
    public static void main(String[] args){
        LintResult lintResult1 = new LintResult("data/backup/rev/powerword7/16162/lint.txt");
        System.out.println("errors:"+lintResult1.getErrorCount());
        System.out.println("warings:"+lintResult1.getWaringsCount());
        
        LintResult lintResult2 = new LintResult("data/backup/rev/powerword7/16157/lint.txt");
        System.out.println("errors:"+lintResult2.getErrorCount());
        System.out.println("warings:"+lintResult2.getWaringsCount());
        
        System.out.println("diff:"+lintResult1.diffWith(lintResult2));
    }
}
