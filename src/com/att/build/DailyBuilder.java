package com.att.build;

import com.att.svn.SvnManager;
import com.mail.MailSender;
import com.spx.adb.Builder;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class DailyBuilder {
    private SvnManager svnManager = SvnManager.getInstance();
    public void perform(){
        //更新应用
        svnManager.update(SystemEnv.APP_PROJECT_PATH);
        //更新测试应用
        svnManager.update(SystemEnv.TESTAPP_PROJECT_PATH);
        
        //编译打包
        StringBuilder buildError = new StringBuilder();
        boolean build = true;
        if(Builder.getInstance().buildPath(SystemEnv.APP_PROJECT_PATH, 6, buildError)){
            build = Builder.getInstance().buildPath(SystemEnv.TESTAPP_PROJECT_PATH, 6, buildError);
        }else{
            build = false;
        }
        
        if (!build && buildError.toString().trim().length() != 0) {
            MailSender.getInstance().sendMail(MailSender.defaultRecipients, "编译失败呀, 无法备份今天的apk", buildError.toString(), false, null);
            return;
        }
        
        //备份到本地目录
//        String day = Util.getTimeStr("yyyyMMdd");
//        Util.makeDir("data/backup/appapk/"+day);
        String path = BackupManager.getDailyBackPath();
        Util.copyFile(Builder.getInstance().getAppApkFileName(), path+"/Powerword7.apk");
        Util.copyFile(Builder.getInstance().getTestAppApkFileName(), path+"/TestPowerword7.apk");
        
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        DailyBuilder db = new DailyBuilder();
        db.perform();
    }

}
