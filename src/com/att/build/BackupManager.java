package com.att.build;

import java.io.File;
import java.io.FilenameFilter;

import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class BackupManager {

    public static String getDailyBackPath(){
        String day = Util.getTimeStr("yyyyMMdd");
        String path = "data/backup/appapk/"+day;
        Util.makeDir(path);
        return path;
    }
    
    public static String getDailyRevisionBackupPath(String localPath, String rev){
        String path = "data/backup/rev/";
        if(SystemEnv.APP_PROJECT_PATH.equals(localPath)){
            path+="powerword7/";
        }else{
            path+="test/";
        }
        
        if(rev.startsWith("r")||rev.startsWith("R")){
            rev = rev.substring(1);
        }
        path += rev;
        Util.makeDir(path);
        return path;
    }
    
    public static String getLastLintFile(String localPath,final String revId){
        String pjpath ="test";
        if(SystemEnv.APP_PROJECT_PATH.equals(localPath)){
            pjpath ="powerword7";
        }
        String path = "data/backup/rev/"+pjpath;
        if(!Util.isFileExist(path)) return null;
        File file = new File(path);
        String[] childs= file.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("r")||name.startsWith("R")) return false;
                if(name.contains(revId)) return false;
                if(name.compareTo(revId)>0) return false;
                return true;
            }
        });
        //System.out.println("childs.length:"+childs.length);
        if(childs==null || childs.length==0) return null;
        String lastOne = childs[childs.length-1];
        return lastOne;
    }
    
    public static void main(String[] args){
        String rev = BackupManager.getLastLintFile(SystemEnv.APP_PROJECT_PATH, "15941");
        System.out.println("rev:"+rev);
    }
}
