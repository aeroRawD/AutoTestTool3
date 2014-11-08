package com.spx.adb;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.log.Log;

public class Builder {

    // public void buildTestProject(String testProject, String testedProject) {
    // List<String> cmdOutput = Util.getCmdOutput("ant build");
    // for(String s:cmdOutput)
    // Log.d(""+s);
    // }
    private static Builder instance = new Builder();

    private Builder() {
    }

    public static Builder getInstance() {
        return instance;
    }

    private boolean appUpdated = false;
    private boolean testAppUpdated = false;
    private boolean appBuilded = false;
    private boolean testAppBuilded = false;

    // public void buildAll() {
    // appUpdated = SvnManager.getInstance().isUpdated(
    // SystemEnv.APP_PROJECT_PATH);
    // if (appUpdated || !isApkCreated(SystemEnv.APP_PROJECT_PATH)) {
    // appBuilded = buildPowerword7(SystemEnv.APP_PROJECT_PATH, "debug");
    // }
    // testAppUpdated = SvnManager.getInstance().isUpdated(
    // SystemEnv.TESTAPP_PROJECT_PATH);
    // if (appUpdated || testAppUpdated
    // || !isApkCreated(SystemEnv.TESTAPP_PROJECT_PATH)) {
    // Util.createFile(SystemEnv.TESTAPP_PROJECT_PATH + "/ant.properties",
    // "tested.project.dir=" + SystemEnv.APP_PROJECT_PATH);
    // testAppBuilded = buildTestProject(SystemEnv.TESTAPP_PROJECT_PATH,
    // "debug");
    // }
    // }

    public boolean buildPath(String localpath, int tryTimes, StringBuilder buildError) {
        if (SystemEnv.APP_PROJECT_PATH.equals(localpath)) {
            return buildPowerword7("debug", tryTimes, buildError);
        } else if (SystemEnv.TESTAPP_PROJECT_PATH.equals(localpath)) {
            if (!Util.isFileExist(SystemEnv.TESTAPP_PROJECT_PATH
                    + "/ant.properties")) {
                Util.createFile(SystemEnv.TESTAPP_PROJECT_PATH
                        + "/ant.properties", "tested.project.dir="
                        + SystemEnv.APP_PROJECT_PATH);
            }

            return buildTestProject("debug", tryTimes, buildError);
        }
        return false;
    }

    public boolean isAppUpdated() {
        return appUpdated;
    }

    public boolean isTestAppUpdated() {
        return testAppUpdated;
    }

    public boolean isAppApkBuilded() {
        return appBuilded;
    }

    public boolean isTestAppApkBuilded() {
        return testAppBuilded;
    }

    public String getAppApkFileName() {
        return SystemEnv.APP_PROJECT_PATH + "/bin/Powerword7-debug.apk";
    }

    public String getTestAppApkFileName() {
        return SystemEnv.TESTAPP_PROJECT_PATH + "/bin/Powerword7Test-debug.apk";
    }

    public boolean buildPowerword7(String buildType, int tryTimes, StringBuilder buildError) {

        copyBuildFileToPowerword7(SystemEnv.APP_PROJECT_PATH);

        String cmd = SystemEnv.ant + " -f " + SystemEnv.APP_PROJECT_PATH
                + "/build.xml clean " + buildType;
        Log.d("cmd:" + cmd);
        // String[] cmds = new
        // String[]{ant,"-f","d:/data/powerword7/build.xml","debug"};
        return buildProject(cmd, SystemEnv.APP_PROJECT_PATH, tryTimes, buildError);
    }

    private boolean buildProject(String cmd, String projectpath, int tryTimes, StringBuilder buildError) {
        for (int i = 0; i < tryTimes; i++) {

            List<String> cmdOutput = Util.getCmdOutput(cmd);
            if (isApkBuildSuccessful(cmdOutput, projectpath)) {
                Log.d("apk编译成功");
                return true;
            }
            
            if (buildError != null)
                buildError.append(getBuildError(cmdOutput));
        }
        Log.d("apk编译失败!");
        return false;
    }
    
    private String getBuildError(List<String> output) {
        boolean startCapture = false;
        boolean stopCapture = false;
        String error = "";
        for (int i = 0; i < output.size(); i++) {
            String line = output.get(i);
            if (startCapture && !stopCapture) {
                error += line;
            }
            if (line.trim().equals("-compile:")) {
                startCapture = true;
            }
            if (line.trim().equals("BUILD FAILED")) {
                stopCapture = true;
            }
        }

        return error;
    }

    private boolean isApkBuildSuccessful(List<String> content, String path) {
        boolean buildSucceefull = false;
        for (String s : content) {
            // Log.d(""+s);
            if (s.contains("BUILD SUCCESSFUL")) {
                buildSucceefull = true;
                break;
            }
        }

        if (buildSucceefull && isApkCreated(path)) {
            Log.d("apk已经生成");
            return true;
        }
        return false;
    }

    public boolean isApkCreated(String projectpath) {
        Log.d("projectpath:" + projectpath);
        if (!projectpath.endsWith("bin")) {
            projectpath = projectpath + "/bin";
        }
        File file = new File(projectpath);
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("apk"))
                    return true;
                return false;
            }
        });

        if (files != null) {
            for (String filename : files) {
                Log.d("filename:" + filename);
            }
        } else {
            Log.d("files is null!");
        }
        return files != null && files.length != 0;
    }

    public boolean buildTestProject(String buildType, int tryTimes, StringBuilder buildError) {

        String cmd = SystemEnv.ant + " -f " + SystemEnv.TESTAPP_PROJECT_PATH
                + "/build.xml clean " + buildType;
        Log.d("cmd:" + cmd);
        return buildProject(cmd, SystemEnv.TESTAPP_PROJECT_PATH, tryTimes, buildError);
    }

    private void copyBuildFileToPowerword7(String projectpath) {
        String buildFile = "data/powerword7_build.xml";
        Util.copyFile(buildFile, projectpath + "/build.xml");
    }

    public static void main(String[] args) {
        Builder builder = new Builder();
        // builder.build();
        // Builder.getInstance().buildAll();
        // builder.buildPowerword7("d:/data/powerword7", "debug");
        // Util.createFile("d:/data/test/ant.properties",
        // "tested.project.dir=d:/data/powerword7");
        //
        // builder.buildTestProject("d:/data/test", "debug");
    }
}
