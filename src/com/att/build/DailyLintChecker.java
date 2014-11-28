package com.att.build;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.logging.Logger;

import com.att.svn.SvnInfo;
import com.att.svn.SvnManager;
import com.log.Log;
import com.spx.adb.Builder;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class DailyLintChecker {
    private Logger logger = Log.getSlientLogger("DailyLintChecker");
    
    //private static final String localPath = SystemEnv.APP_PROJECT_PATH;
    
    public String perform(String localPath){
        int times = 5;
        while(!lint(localPath) && times>0){
            times--;
        }
        
        SvnInfo currentSvnInfo = SvnManager.getInstance().getCurrentSvnInfo(localPath);
        String revision = currentSvnInfo.getLastChangedRevId();
        String path = BackupManager.getDailyRevisionBackupPath(localPath, revision);
        Util.copyFile("testreport/testresult_lint.txt", path+"/lint.txt");
        logger.info(localPath+", revid:"+revision+", path:"+path);
        String lintfile = BackupManager.getDailyRevisionBackupPath(localPath, revision)+"/lint.txt";
        if(!isLastestLintFileCreate(localPath, revision)){
            Util.copyFile("testreport/testresult_lint.txt", lintfile);
        }
        return lintfile;
    }
    
    
    
    public static String getLintFileName(String localPath, String revision){
        return BackupManager.getDailyRevisionBackupPath(localPath,revision)+"/lint.txt";
    }
    
    public static boolean isLastestLintFileCreate(String localPath, String revision){
       String fileName = BackupManager.getDailyRevisionBackupPath(localPath,revision)+"/lint.txt";
       return Util.isFileExist(fileName);
    }
    
    public static String getCurrentRevWarnings(String localPath){
        SvnInfo currentSvnInfo = SvnManager.getInstance().getCurrentSvnInfo(localPath);
        String revision = currentSvnInfo.getRevision();
        return getWarnings(localPath, revision);
    }
    
    public static String getWarnings(String localPath, String revId){
        String lintFile = BackupManager.getDailyRevisionBackupPath(localPath, revId)+"/lint.txt";
        if(!Util.isFileExist(lintFile)) return "unknown";
        List<String> fileContentLines = Util.getFileContentLines(lintFile);
        if(fileContentLines.size()==0) return "unknown";
        String lastLine = fileContentLines.get(fileContentLines.size()-1);
        if(lastLine.indexOf("warnings")==-1 && lastLine.indexOf("errors")==-1) return "unknown";
        return lastLine;
    }
    
    private boolean lint(String localPath){
        List<String> cmdOutput = Util.getCmdOutput(SystemEnv.lint+" "+localPath+" > testreport/testresult_lint.txt");
        
        for(String s:cmdOutput){
            logger.info(s);
            if(s.contains("EXCEPTION_ACCESS_VIOLATION")) return false;
        }
        return true;
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
//        DailyLintChecker checker = new DailyLintChecker();
//        checker.perform();
        
        String lastLintFile = BackupManager.getLastLintFile(SystemEnv.APP_PROJECT_PATH, "15890");
        System.out.println("lastLintFile:"+lastLintFile);
        
    }

}
