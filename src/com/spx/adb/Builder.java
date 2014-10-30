package com.spx.adb;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.List;

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

	public void buildAll() {
		boolean appUpdated = SvnManager.getInstance().isUpdated(
				SystemEnv.APP_PROJECT_PATH);
		if (appUpdated || !isApkCreated(SystemEnv.APP_PROJECT_PATH)) {
			buildPowerword7(SystemEnv.APP_PROJECT_PATH, "debug");
		}
		boolean testAppUpdated = SvnManager.getInstance().isUpdated(
				SystemEnv.TESTAPP_PROJECT_PATH);
		if (appUpdated || testAppUpdated
				|| !isApkCreated(SystemEnv.TESTAPP_PROJECT_PATH)) {
			Util.createFile(SystemEnv.TESTAPP_PROJECT_PATH + "/ant.properties",
					"tested.project.dir=" + SystemEnv.APP_PROJECT_PATH);
			buildTestProject(SystemEnv.TESTAPP_PROJECT_PATH, "debug");
		}
	}
	

	
	public String getAppApkFileName() {
		return SystemEnv.APP_PROJECT_PATH + "/bin/Powerword7-debug.apk";
	}

	public String getTestAppApkFileName() {
		return SystemEnv.TESTAPP_PROJECT_PATH + "/bin/Powerword7Test-debug.apk";
	}

	private boolean buildPowerword7(String projectpath, String buildType) {

		copyBuildFileToPowerword7(projectpath);

		String cmd = SystemEnv.ant + " -f " + projectpath + "/build.xml "
				+ buildType;
		Log.d("cmd:" + cmd);
		// String[] cmds = new
		// String[]{ant,"-f","d:/data/powerword7/build.xml","debug"};
		return buildProject(cmd, projectpath);
	}

	private boolean buildProject(String cmd, String projectpath) {
		int tryTimes = 10;
		for (int i = 0; i < tryTimes; i++) {

			List<String> cmdOutput = Util.getCmdOutput(cmd);
			if (isApkBuildSuccessful(cmdOutput, projectpath)) {
				Log.d("apk编译成功");
				return true;
			}

		}
		Log.d("apk编译失败!");
		return false;
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

	private boolean buildTestProject(String projectpath, String buildType) {

		String cmd = SystemEnv.ant + " -f " + projectpath + "/build.xml "
				+ buildType;
		Log.d("cmd:" + cmd);
		return buildProject(cmd, projectpath);
	}

	private void copyBuildFileToPowerword7(String projectpath) {
		String buildFile = "data/powerword7_build.xml";
		Util.copyFile(buildFile, projectpath + "/build.xml");
	}

	public static void main(String[] args) {
		Builder builder = new Builder();
		// builder.build();
		Builder.getInstance().buildAll();
		// builder.buildPowerword7("d:/data/powerword7", "debug");
		// Util.createFile("d:/data/test/ant.properties",
		// "tested.project.dir=d:/data/powerword7");
		//
		// builder.buildTestProject("d:/data/test", "debug");
	}
}
