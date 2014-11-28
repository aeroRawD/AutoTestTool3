package com.att.svn;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.att.build.ApplicationBuilder;
import com.att.build.BackupManager;
import com.att.build.DailyRunner2;
import com.log.Log;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class SvnMonitor extends Thread {
    private Logger logger = Log.getSlientLogger("svn");
    private SvnUpdateListener listener = null;
    private SvnManager svnManager = SvnManager.getInstance();

    private static final int SVN_UPDATE_TIME = 12000;
    private static final int HEART_BEAT_TIME = 60000;

    

    // 被监控的SVN url列表
    private List<String> watchedUrls = new ArrayList<String>();

    private static SvnMonitor sInstance = new SvnMonitor();

    private SvnMonitor() {
    };

    public static SvnMonitor getInstance() {
        return sInstance;
    }

    @Override
    public void run() {
        // AndroidDebugBridge.init(false);
        while (true) {

            try {

                if (isLoopMode()) {
                    
                    //即使是在loop模式, 如果用例那边还没跑完, 也先等待.
                    if (DailyRunner2.getInstance().isRuning() ) {
                        logger.info("用例正在执行,暂不监控svn");
                        Util.sleep(HEART_BEAT_TIME);
                        continue;
                    }
                    if (!isInBuilding()) {
                        checkSvnUpdate();
                    }

                    Util.sleep(SVN_UPDATE_TIME);
                } else {
                    logger.info("当前不需要实时监控svn");
                    if (!DailyRunner2.getInstance().isRuning() ) {
                        DailyRunner2.getInstance().dailyPerfom();
                    }
                    Util.sleep(HEART_BEAT_TIME);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isLoopMode() {
        String now = Util.getTimeStr("HH:mm");
        if (now.compareTo(SystemEnv.loopEndTime) < 0 && now.compareTo(SystemEnv.loopStartTime) > 0) {
            return true;
        }
        return false;
    }

    private boolean isInBuilding() {
        return ApplicationBuilder.getInstance().isRunningTest();
    }

    private void checkSvnUpdate() {
        // logger.info("checkSvnUpdate()  ");
        for (int i = 0; i < watchedUrls.size(); i++) {
            String url = watchedUrls.get(i);

            checkSvnUpdate(SystemEnv.getLocalPathForUrl(url));
            Util.sleep(2000);
        }
    }

    /**
     * 检查SVN是否更新
     */
    private void checkSvnUpdate(String localProjectPath) {
        logger.info("checkSvnUpdate  localProjectPath:"+localProjectPath);
        if(ApplicationBuilder.getInstance().isRunningTest()){
            logger.info("正在运行测试, 等待...");
            return;
        }
        // logger.info("checkSvnUpdate()  "+localProjectPath);
        if (svnManager.isUpdated(localProjectPath)) {
            logger.info(localProjectPath + "有更新!");
            SvnInfo svnInfo = svnManager.getCurrentSvnInfo(localProjectPath);
            logger.info("" + svnInfo.toString());

            SvnRevisionInfo revisionInfo = svnManager.getSvnRevisionDetail(
                    localProjectPath, svnInfo.getLastChangedRevId());
            logger.info("revision info:" + revisionInfo.getRevisionDetail());
            
//            String revision = revisionInfo.getRevId();
//            if(!isLastestLintFileCreate(localProjectPath, revision)){
//                Util.copyFile("testreport/testresult_lint.txt", BackupManager.getDailyRevisionBackupPath(localProjectPath, revision)+"/lint.txt");
//            }

            String url = SystemEnv.getUrlForLocalpath(localProjectPath);
            notifySvnListeners(url, revisionInfo, localProjectPath);
        }

    }
    
    public static boolean isLastestLintFileCreate(String localPath, String revision){
        String fileName = BackupManager.getDailyRevisionBackupPath(localPath, revision)+"/lint.txt";
        return Util.isFileExist(fileName);
     }
    

    private void notifySvnListeners(String url, SvnRevisionInfo revisionInfo,
            String localpath) {
        if (listener != null) {
            listener.onSvnUpdate(url, revisionInfo, localpath);
        }
    }

    public SvnUpdateListener getListener() {
        return listener;
    }

    public void setListener(SvnUpdateListener listener) {
        this.listener = listener;
    }

    public void addWatchedUrl(String url) {
        watchedUrls.add(url);
    }
}
